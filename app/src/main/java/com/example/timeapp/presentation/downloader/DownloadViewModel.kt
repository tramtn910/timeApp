package com.example.timeapp.presentation.downloader

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit


class DownloadViewModel : ViewModel() {
    private val TAG = "DownloadViewModel"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads = _downloads.asStateFlow()

    private val activeCalls = mutableMapOf<Long, Call>()

    fun startDownload(url: String, title: String, thumbnailUrl: String?) {
        Log.d(TAG, "startDownload called: url=$url, title=$title")
        viewModelScope.launch {
            val safeTitle = title.ifBlank { "video_${System.currentTimeMillis()}" }
            val fileName = "$safeTitle.mp4"

            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
                Log.d(TAG, "Created Downloads directory")
            }

            val file = File(downloadDir, fileName)
            Log.d(TAG, "Target file: ${file.absolutePath}")

            val item = DownloadItem(
                id = System.currentTimeMillis(),
                title = title,
                url = url,
                thumbnailUrl = thumbnailUrl,
                filePath = file.absolutePath,
                status = DownloadStatus.PENDING
            )

            addOrUpdateItem(item)
            downloadFile(item)
        }
    }

    private fun downloadFile(item: DownloadItem) {
        Log.d(TAG, "downloadFile started for id=${item.id}, url=${item.url}")

        val file = File(item.filePath)
        val downloadedBytes = if (file.exists()) file.length() else 0L
        Log.d(TAG, "Existing downloaded bytes: $downloadedBytes")

        val request = Request.Builder()
            .url(item.url)
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36")
            .addHeader("Accept", "*/*")
            .apply {
                if (downloadedBytes > 0) {
                    addHeader("Range", "bytes=$downloadedBytes-")
                    Log.d(TAG, "Added Range header: bytes=$downloadedBytes-")
                }
            }
            .build()

        val call = okHttpClient.newCall(request)
        activeCalls[item.id] = call

        Log.d(TAG, "Enqueuing request...")

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Download failed for id=${item.id}: ${e.message}", e)
                if (!call.isCanceled()) {
                    updateItem(item.copy(status = DownloadStatus.FAILED))
                }
                activeCalls.remove(item.id)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "Response received: code=${response.code}, message=${response.message}")

                if (!response.isSuccessful) {
                    Log.e(TAG, "Unsuccessful response: code=${response.code}, message=${response.message}")
                    updateItem(item.copy(status = DownloadStatus.FAILED))
                    activeCalls.remove(item.id)
                    return
                }

                val body = response.body ?: run {
                    Log.e(TAG, "Response body is null")
                    return
                }

                val contentLength = body.contentLength()
                val totalBytes = if (contentLength != -1L) contentLength + downloadedBytes else null
                Log.d(TAG, "Content-Length: $contentLength, Total bytes: $totalBytes")

                updateItem(item.copy(status = DownloadStatus.RUNNING, bytesTotal = totalBytes))

                val input = body.byteStream()
                val output = FileOutputStream(file, true)
                val buffer = ByteArray(8192)

                var bytesRead: Int
                var accumulatedInPeriod = 0L
                var lastUpdateTime = System.currentTimeMillis()

                try {
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        if (call.isCanceled()) {
                            Log.d(TAG, "Download canceled for id=${item.id}")
                            updateItem(item.copy(status = DownloadStatus.PAUSED))
                            activeCalls.remove(item.id)
                            return
                        }

                        output.write(buffer, 0, bytesRead)
                        accumulatedInPeriod += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastUpdateTime >= 300) {
                            val elapsedSeconds = (now - lastUpdateTime) / 1000.0
                            val speed = if (elapsedSeconds > 0) {
                                (accumulatedInPeriod / elapsedSeconds / 1024).toLong()
                            } else 0L

                            updateItem(
                                item.copy(
                                    bytesDownloaded = file.length(),
                                    currentSpeed = speed,
                                    bytesTotal = totalBytes
                                )
                            )

                            Log.d(TAG, "Progress update: ${file.length()} / $totalBytes bytes, speed: $speed KB/s")

                            accumulatedInPeriod = 0L
                            lastUpdateTime = now
                        }
                    }

                    Log.d(TAG, "Download completed successfully for id=${item.id}")
                    updateItem(
                        item.copy(
                            bytesDownloaded = file.length(),
                            bytesTotal = file.length(),
                            currentSpeed = 0L,
                            status = DownloadStatus.SUCCESS
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error during download: ${e.message}", e)
                    if (!call.isCanceled()) {
                        updateItem(item.copy(status = DownloadStatus.FAILED))
                    }
                } finally {
                    try {
                        input.close()
                        output.close()
                    } catch (ignored: Exception) {}
                    activeCalls.remove(item.id)
                }
            }
        })
    }

    fun togglePauseResume(itemId: Long) {
        val item = _downloads.value.find { it.id == itemId } ?: return
        when (item.status) {
            DownloadStatus.RUNNING -> {
                activeCalls[itemId]?.cancel()
                updateItem(item.copy(status = DownloadStatus.PAUSED))
            }
            DownloadStatus.PAUSED, DownloadStatus.FAILED, DownloadStatus.PENDING -> {
                downloadFile(item) // retry/resume
            }
            else -> {}
        }
    }

    fun removeDownload(itemId: Long) {
        activeCalls[itemId]?.cancel()
        activeCalls.remove(itemId)

        val currentList = _downloads.value
        val itemToRemove = currentList.find { it.id == itemId }
        _downloads.value = currentList.filter { it.id != itemId }

        // Xóa file thật
        itemToRemove?.let {
            File(it.filePath).delete()
        }
    }

    private fun addOrUpdateItem(newItem: DownloadItem) {
        val list = _downloads.value.toMutableList()
        val index = list.indexOfFirst { it.id == newItem.id }
        if (index == -1) {
            list.add(newItem)
        } else {
            list[index] = newItem
        }
        _downloads.value = list
    }

    private fun updateItem(updated: DownloadItem) = addOrUpdateItem(updated)
}

data class DownloadItem(
    val id: Long,
    val title: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val bytesDownloaded: Long = 0,
    val bytesTotal: Long? = null,
    val currentSpeed: Long = 0, // KB/s hiện tại
    val status: DownloadStatus = DownloadStatus.PENDING,
    val filePath: String,
)

enum class DownloadStatus { PENDING, RUNNING, PAUSED, SUCCESS, FAILED, CANCELLED }