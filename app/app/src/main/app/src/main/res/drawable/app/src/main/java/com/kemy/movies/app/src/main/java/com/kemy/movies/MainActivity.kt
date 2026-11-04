package com.kemy.movies

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContent {
                KemyMoviesTheme {
                    KemyMoviesApp()
                }
            }
        } catch (t: Throwable) {
            setContent {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Startup Error:\n${t.localizedMessage}\n\n${t.stackTraceToString()}",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

enum class Screen {
    HOME, DETAILS, PLAYER, WATCHLIST
}

@Composable
fun KemyMoviesApp() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedMovie by remember { mutableStateOf<MovieItem?>(null) }
    var selectedGenre by remember { mutableStateOf("All") }
    val moviesList = remember { mutableStateListOf(*MovieDatabase.movies.toTypedArray()) }

    fun toggleWatchlist(movieId: String) {
        val index = moviesList.indexOfFirst { it.id == movieId }
        if (index != -1) {
            val item = moviesList[index]
            moviesList[index] = item.copy(isInWatchlist = !item.isInWatchlist)
        }
    }

    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
    }

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.PLAYER) {
                NavigationBar(containerColor = SurfaceDark) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = { currentScreen = Screen.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = AccentGold,
                            indicatorColor = AccentGold,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.WATCHLIST,
                        onClick = { currentScreen = Screen.WATCHLIST },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Watchlist") },
                        label = { Text("Watchlist") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = AccentGold,
                            indicatorColor = AccentGold,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    items = moviesList,
                    selectedGenre = selectedGenre,
                    onGenreSelected = { selectedGenre = it },
                    onMediaClick = { movie ->
                        selectedMovie = movie
                        currentScreen = Screen.DETAILS
                    },
                    onWatchlistToggle = { toggleWatchlist(it) }
                )
                Screen.DETAILS -> selectedMovie?.let { movie ->
                    DetailsScreen(
                        movie = movie,
                        onBack = { currentScreen = Screen.HOME },
                        onPlayClick = { currentScreen = Screen.PLAYER },
                        onWatchlistToggle = { toggleWatchlist(movie.id) }
                    )
                }
                Screen.PLAYER -> selectedMovie?.let { movie ->
                    PlayerScreen(
                        movie = movie,
                        onBack = { currentScreen = Screen.DETAILS }
                    )
                }
                Screen.WATCHLIST -> WatchlistScreen(
                    items = moviesList.filter { it.isInWatchlist },
                    onMediaClick = { movie ->
                        selectedMovie = movie
                        currentScreen = Screen.DETAILS
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    items: List<MovieItem>,
    selectedGenre: String,
    onGenreSelected: (String) -> Unit,
    onMediaClick: (MovieItem) -> Unit,
    onWatchlistToggle: (String) -> Unit
) {
    val featuredMovie = items.firstOrNull { it.isFeatured } ?: items.firstOrNull()
    val filteredItems = if (selectedGenre == "All") items else items.filter { it.genres.contains(selectedGenre) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if (featuredMovie != null) {
            item {
                FeaturedHero(
                    movie = featuredMovie,
                    onOpen = { onMediaClick(featuredMovie) },
                    onWatchlistToggle = { onWatchlistToggle(featuredMovie.id) }
                )
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(MovieDatabase.genres) { genre ->
                    val isSelected = genre == selectedGenre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) AccentGold else SurfaceElevated)
                            .clickable { onGenreSelected(genre) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = genre,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Trending Now",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    MediaCard(item = item, onClick = { onMediaClick(item) })
                }
            }
        }

        item {
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Cinema Originals",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredItems.reversed(), key = { "rev-${it.id}" }) { item ->
                    MediaCard(item = item, onClick = { onMediaClick(item) })
                }
            }
        }
    }
}

@Composable
fun FeaturedHero(movie: MovieItem, onOpen: () -> Unit, onWatchlistToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(440.dp)
            .clickable { onOpen() }
    ) {
        FastPosterImage(imageUrl = movie.backdropUrl, contentDescription = movie.title, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Transparent, BackgroundDark.copy(alpha = 0.85f), BackgroundDark)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = "FEATURED",
                style = MaterialTheme.typography.labelSmall,
                color = AccentGold,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "${movie.releaseYear} • ${movie.genres.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(4.dp))
                    Text("Watch Details", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                FilledTonalIconButton(
                    onClick = onWatchlistToggle,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = SurfaceElevated)
                ) {
                    Icon(
                        imageVector = if (movie.isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist",
                        tint = if (movie.isInWatchlist) AccentGold else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DetailsScreen(
    movie: MovieItem,
    onBack: () -> Unit,
    onPlayClick: () -> Unit,
    onWatchlistToggle: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                FastPosterImage(imageUrl = movie.backdropUrl, contentDescription = movie.title, modifier = Modifier.fillMaxSize())
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(0.6f), RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(movie.title, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = RatingStar, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(movie.rating, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.width(12.dp))
                    Text("${movie.releaseYear} • ${movie.duration}", color = TextSecondary, fontSize = 13.sp)
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(6.dp))
                    Text("PLAY MOVIE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(18.dp))
                Text("Overview", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(movie.overview, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 22.sp)
            }
        }
    }
}

@Composable
fun PlayerScreen(movie: MovieItem, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        EnhancedMoviePlayer(videoUrl = movie.videoUrl, modifier = Modifier.fillMaxSize())
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(0.6f), RoundedCornerShape(50))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}

@Composable
fun WatchlistScreen(items: List<MovieItem>, onMediaClick: (MovieItem) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Watchlist", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No movies in your watchlist yet.", color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(items, key = { it.id }) { movie ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                            .clickable { onMediaClick(movie) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(60.dp, 80.dp).clip(RoundedCornerShape(8.dp))) {
                            FastPosterImage(imageUrl = movie.posterUrl, contentDescription = movie.title, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(movie.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("${movie.releaseYear} • ${movie.duration}", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
