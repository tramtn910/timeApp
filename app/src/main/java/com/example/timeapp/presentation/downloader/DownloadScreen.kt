package com.example.timeapp.presentation.downloader

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

//@Preview(showBackground = true, backgroundColor = 0xFF121212)
//@Composable
//fun VideoDownloadItemPreview() {
//    VideoDownloadItemCompose(
//        item = DownloadItem(
//            id = 1L,
//            title = "Big Buck Bunny",
//            url = "",
//            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
//            bytesDownloaded = 25_000_000,
//            bytesTotal = 100_000_000,
//            currentSpeed = 512,
//            status = DownloadStatus.RUNNING
//        ),
//        onRemove = {}
//    )
//}

@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel
) {
    val downloads by viewModel.downloads.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = downloads,
            key = { it.id }
        ) { item ->
            VideoDownloadItemCompose(
                item = item,
                onRemove = { viewModel.removeDownload(item.id) },
                viewModel = viewModel
            )
        }

        item {
            Button(
                onClick = {
                    Log.d("DEBUG_BUTTON", "TEST DOWNLOAD BUTTON PRESSED!!!")
                    viewModel.startDownload(

                        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        title = "video title",
                        thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("test downloadnnnnnn")
            }
        }
    }
}

@Composable
fun VideoDownloadItemCompose(
    item: DownloadItem,
    onRemove: () -> Unit,
    viewModel: DownloadViewModel
) {
    val progress = if (item.bytesTotal != null && item.bytesTotal > 0) {
        item.bytesDownloaded.toFloat() / item.bytesTotal.toFloat()
    } else 0f

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            AsyncImage(
                model = item.thumbnailUrl ?: "https://via.placeholder.com/80x80?text=Video",
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,

            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color.Gray)
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (item.status == DownloadStatus.RUNNING) {
                    Text(
                        text = "${item.currentSpeed} KB/s",
                        color = Color(0xFFFF5722),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                }

                // Dung lượng
                Text(
                    text = "${formatBytes(item.bytesDownloaded)} / ${item.bytesTotal?.let { formatBytes(it) } ?: "??"}",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFF5722),
                    trackColor = Color(0xFF333333)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (item.status == DownloadStatus.PAUSED) {
                        Text("Paused", color = Color(0xFFFF5722), fontWeight = FontWeight.Medium)
                    } else if (item.status == DownloadStatus.SUCCESS) {
                        Text("Hoàn thành", color = Color.Green)
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    Icon(
                        imageVector = if (item.status == DownloadStatus.PAUSED || item.status == DownloadStatus.FAILED)
                            Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF333333), CircleShape)
                            .padding(8.dp)
                            .clickable {
                                viewModel.togglePauseResume(item.id)
                            }
                    )
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.2f KB".format(bytes / 1024.0)
        else -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
    }
}