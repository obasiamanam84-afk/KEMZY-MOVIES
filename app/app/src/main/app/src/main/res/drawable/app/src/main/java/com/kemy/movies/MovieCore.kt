package com.kemy.movies

import android.media.audiofx.LoudnessEnhancer
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

// ==========================================
// 1. CINEMA THEME & COLORS
// ==========================================
val BackgroundDark = Color(0xFF090A0F)
val SurfaceDark = Color(0xFF13151F)
val SurfaceElevated = Color(0xFF1C1F2E)
val AccentGold = Color(0xFFFFC107)
val AccentGoldGlow = Color(0xFFFFD54F)
val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFF9E9EA7)
val RatingStar = Color(0xFFFFB800)

@Composable
fun KemyMoviesTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = AccentGold,
        secondary = AccentGoldGlow,
        background = BackgroundDark,
        surface = SurfaceDark,
        onPrimary = BackgroundDark,
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}

// ==========================================
// 2. MOVIE DATA MODEL & DATABASE
// ==========================================
data class MovieItem(
    val id: String,
    val title: String,
    val releaseYear: String,
    val duration: String,
    val rating: String,
    val genres: List<String>,
    val overview: String,
    val posterUrl: String,
    val backdropUrl: String,
    val videoUrl: String,
    val isFeatured: Boolean = false,
    var isInWatchlist: Boolean = false
)

object MovieDatabase {
    val genres = listOf("All", "Action", "Sci-Fi", "Drama", "Animation", "Thriller")

    val movies = listOf(
        MovieItem(
            id = "1",
            title = "Tears of Steel",
            releaseYear = "2024",
            duration = "12 min",
            rating = "8.8",
            genres = listOf("Sci-Fi", "Action"),
            overview = "In a dystopian future, a group of warriors and scientists gather at the Oude Kerk in Amsterdam to stage a crucial historical event in a desperate bid to rescue the world from rampaging robots.",
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            isFeatured = true
        ),
        MovieItem(
            id = "2",
            title = "Big Buck Bunny",
            releaseYear = "2023",
            duration = "10 min",
            rating = "8.5",
            genres = listOf("Animation", "Action"),
            overview = "A large and lovable rabbit deals with bullying forest creatures in the most humorous, cinematic way possible.",
            posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ),
        MovieItem(
            id = "3",
            title = "Sintel: Dragon Quest",
            releaseYear = "2024",
            duration = "15 min",
            rating = "9.1",
            genres = listOf("Animation", "Drama"),
            overview = "A lonely young woman searches the ends of the earth to rescue an injured baby dragon she nursed back to health.",
            posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        ),
        MovieItem(
            id = "4",
            title = "Elephants Dream",
            releaseYear = "2023",
            duration = "11 min",
            rating = "8.3",
            genres = listOf("Sci-Fi", "Drama"),
            overview = "Two strange explorers navigate the endless mechanical rooms of a giant machine that manifests their own minds.",
            posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80",
            backdropUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        )
    )
}

// ==========================================
// 3. UI COMPONENTS (Poster & Movie Card)
// ==========================================
@Composable
fun FastPosterImage(imageUrl: String, contentDescription: String?, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = { Box(Modifier.fillMaxSize().background(SurfaceElevated)) },
        error = { Box(Modifier.fillMaxSize().background(SurfaceElevated)) }
    )
}

@Composable
fun MediaCard(item: MovieItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.width(140.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceElevated)
        ) {
            FastPosterImage(imageUrl = item.posterUrl, contentDescription = item.title, modifier = Modifier.fillMaxSize())

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = RatingStar, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(3.dp))
                Text(item.rating, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${item.releaseYear} • ${item.genres.firstOrNull() ?: "Cinema"}", color = TextSecondary, fontSize = 11.sp, maxLines = 1)
    }
}

// ==========================================
// 4. VIDEO PLAYER + LOUDNESS AUDIO BOOST
// ==========================================
@OptIn(UnstableApi::class)
@Composable
fun EnhancedMoviePlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var loudnessEnhancer by remember { mutableStateOf<LoudnessEnhancer?>(null) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()
            setAudioAttributes(audioAttributes, true)

            setMediaItem(ExoMediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    // Boost volume by +1000mB (+10dB) to match adverts and power external MP3 speakers
    DisposableEffect(exoPlayer) {
        try {
            val sessionId = exoPlayer.audioSessionId
            if (sessionId != C.AUDIO_SESSION_ID_UNSET) {
                loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                    setTargetGain(1000)
                    enabled = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            loudnessEnhancer?.release()
            exoPlayer.release()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
