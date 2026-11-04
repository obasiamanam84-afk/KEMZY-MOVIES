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
import androidx.compose.foundation.shape.RoundedCornerShape
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
        setContent {
            KemyMoviesTheme {
                KemyMoviesApp()
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
