package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BookstoreViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

// --- Beautiful Custom Gradient Book Cover Generator ---
@Composable
fun BookCover(
    title: String,
    author: String,
    themeId: String,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 0.65f
) {
    // Generate beautiful cohesive gradient brushes based on book theme
    val brush = when (themeId) {
        "library" -> Brush.verticalGradient(listOf(Color(0xFF2C3E50), Color(0xFF3498DB)))
        "hail_mary" -> Brush.verticalGradient(listOf(Color(0xFFE67E22), Color(0xFF2C3E50)))
        "silent_patient" -> Brush.verticalGradient(listOf(Color(0xFF7F1D1D), Color(0xFF111827)))
        "atomic_habits" -> Brush.verticalGradient(listOf(Color(0xFF16A085), Color(0xFF2C3E50)))
        "clean_code" -> Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937)))
        "demon_slayer" -> Brush.verticalGradient(listOf(Color(0xFF8E44AD), Color(0xFF2C3E50)))
        "dune_exclusive" -> Brush.verticalGradient(listOf(Color(0xFFD35400), Color(0xFFC0392B)))
        "coming_soon" -> Brush.verticalGradient(listOf(Color(0xFF27AE60), Color(0xFF2C3E50)))
        else -> Brush.verticalGradient(listOf(Color(0xFF0F4C3A), Color(0xFF231F20)))
    }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(6.dp))
            .background(brush)
            .border(
                1.dp,
                Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(6.dp)
            )
            .padding(10.dp)
    ) {
        // Draw elegant bookstore gold borders
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.dp.toPx()
            val margin = 4.dp.toPx()
            drawRect(
                color = BNBOchreGold.copy(alpha = 0.6f),
                topLeft = androidx.compose.ui.geometry.Offset(margin, margin),
                size = androidx.compose.ui.geometry.Size(
                    size.width - 2 * margin,
                    size.height - 2 * margin
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant B&N Logo badge on top of covers
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = BNBOchreGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "B&N",
                    color = BNBOchreGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            }

            // Title
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Author
            Text(
                text = author,
                color = BNBCreamLight.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Star Rating Display ---
@Composable
fun RatingBar(rating: Double, modifier: Modifier = Modifier, starSize: Int = 16) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val filledStars = rating.toInt()
        val hasHalf = (rating - filledStars) >= 0.5
        for (i in 1..5) {
            val icon = when {
                i <= filledStars -> Icons.Default.Star
                i == filledStars + 1 && hasHalf -> Icons.Default.StarHalf
                else -> Icons.Default.StarOutline
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BNBOchreGold,
                modifier = Modifier.size(starSize.dp)
            )
        }
        if (rating > 0.0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1f", rating),
                fontSize = 12.sp,
                color = BNBSlate,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --- Primary Bookstore Application Scaffold ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookstoreApp(viewModel: BookstoreViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val cartItems by viewModel.cartItems.collectAsState(initial = emptyList())
    val userProfile by viewModel.userProfile.collectAsState(initial = null)

    val totalCartCount = cartItems.sumOf { it.quantity }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.width(300.dp)
            ) {
                // Header of Drawer with Gold Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(BNBDeepGreen, BNBDarkGreen)
                            )
                        )
                        .padding(vertical = 32.dp, horizontal = 24.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = BNBOchreGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Barnes & Noble",
                            color = BNBCreamLight,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "Your Library. Your Bookshop. Anywhere.",
                            color = BNBOchreGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation drawer list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    item {
                        Text(
                            text = "Core Bookstore",
                            style = MaterialTheme.typography.labelMedium,
                            color = BNBSlate,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Home & Discover") },
                            selected = viewModel.currentScreen == Screen.Home,
                            icon = { Icon(Icons.Default.Home, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.Home)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            ),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Browse Catalogue") },
                            selected = viewModel.currentScreen == Screen.Catalogue,
                            icon = { Icon(Icons.Default.MenuBook, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.Catalogue)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            )
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Search Books") },
                            selected = viewModel.currentScreen == Screen.Search,
                            icon = { Icon(Icons.Default.Search, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.Search)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            )
                        )
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = BNBCreamSurface)
                        Text(
                            text = "Personal Space",
                            style = MaterialTheme.typography.labelMedium,
                            color = BNBSlate,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Your Reading Library") },
                            selected = viewModel.currentScreen == Screen.Library,
                            icon = { Icon(Icons.Default.Bookmark, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.Library)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            )
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Wishlists") },
                            selected = viewModel.currentScreen == Screen.Wishlist,
                            icon = { Icon(Icons.Default.Favorite, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.Wishlist)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            )
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Member Benefits & Card") },
                            selected = viewModel.currentScreen == Screen.Membership,
                            icon = { Icon(Icons.Default.CardMembership, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.Membership)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            )
                        )
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = BNBCreamSurface)
                        Text(
                            text = "Store Services",
                            style = MaterialTheme.typography.labelMedium,
                            color = BNBSlate,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("B&N Café") },
                            selected = viewModel.currentScreen == Screen.Cafe,
                            icon = { Icon(Icons.Default.LocalCafe, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.Cafe)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            )
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Store Locator") },
                            selected = viewModel.currentScreen == Screen.Stores,
                            icon = { Icon(Icons.Default.LocationOn, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.Stores)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            )
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Community & Events") },
                            selected = viewModel.currentScreen == Screen.Events,
                            icon = { Icon(Icons.Default.Event, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.Events)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            )
                        )
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = BNBCreamSurface)
                        Text(
                            text = "Account & Control",
                            style = MaterialTheme.typography.labelMedium,
                            color = BNBSlate,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Purchase & Cafe Orders") },
                            selected = viewModel.currentScreen == Screen.Account,
                            icon = { Icon(Icons.Default.AccountCircle, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.Account)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            )
                        )
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text("Staff Admin Dashboard") },
                            selected = viewModel.currentScreen == Screen.AdminDashboard,
                            icon = { Icon(Icons.Default.Dashboard, null) },
                            onClick = {
                                viewModel.navigateTo(Screen.AdminDashboard)
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BNBCreamSurface,
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBDeepGreen
                            )
                        )
                    }
                }

                // Small B&N Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Barnes & Noble v1.0",
                        fontSize = 11.sp,
                        color = BNBSlate.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (viewModel.currentScreen) {
                                is Screen.Home -> "BARNES & NOBLE"
                                is Screen.Catalogue -> "Book Catalogue"
                                is Screen.BookDetail -> "Book Details"
                                is Screen.Search -> "Search"
                                is Screen.Library -> "Reading Shelf"
                                is Screen.Wishlist -> "My Wishlists"
                                is Screen.Membership -> "Loyalty Card"
                                is Screen.Cafe -> "B&N Café"
                                is Screen.Stores -> "Find Stores"
                                is Screen.Events -> "Author Events"
                                is Screen.Cart -> "Basket"
                                is Screen.Account -> "My Account"
                                is Screen.AiAssistant -> "AI Reading Assistant"
                                is Screen.AdminDashboard -> "Admin Panel"
                            },
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp
                        )
                    },
                    navigationIcon = {
                        val isDetail = viewModel.currentScreen is Screen.BookDetail
                        IconButton(
                            onClick = {
                                if (isDetail) {
                                    viewModel.navigateBack()
                                } else {
                                    coroutineScope.launch { drawerState.open() }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isDetail) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        // Cart icon button with quantity badge
                        IconButton(onClick = { viewModel.navigateTo(Screen.Cart) }) {
                            BadgedBox(
                                badge = {
                                    if (totalCartCount > 0) {
                                        Badge(
                                            containerColor = BNBOchreGold,
                                            contentColor = BNBCharcoal
                                        ) {
                                            Text(text = totalCartCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BNBDeepGreen,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                // Persistent elegant bottom navigation bar for 5 key screens
                NavigationBar(
                    containerColor = BNBDeepGreen,
                    contentColor = BNBCreamLight,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val screens = listOf(
                        Triple(Screen.Home, Icons.Default.Home, "Home"),
                        Triple(Screen.Catalogue, Icons.Default.MenuBook, "Catalogue"),
                        Triple(Screen.Cafe, Icons.Default.LocalCafe, "Café"),
                        Triple(Screen.Library, Icons.Default.Bookmark, "Library"),
                        Triple(Screen.AiAssistant, Icons.Default.Psychology, "AI Assistant")
                    )

                    screens.forEach { (screen, icon, label) ->
                        val isSelected = when (screen) {
                            is Screen.Home -> viewModel.currentScreen == Screen.Home
                            is Screen.Catalogue -> viewModel.currentScreen == Screen.Catalogue
                            is Screen.Cafe -> viewModel.currentScreen == Screen.Cafe
                            is Screen.Library -> viewModel.currentScreen == Screen.Library
                            is Screen.AiAssistant -> viewModel.currentScreen == Screen.AiAssistant
                            else -> false
                        }
                        NavigationBarItem(
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(screen) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BNBDeepGreen,
                                selectedTextColor = BNBOchreGold,
                                indicatorColor = BNBOchreGold,
                                unselectedIconColor = BNBCreamLight.copy(alpha = 0.6f),
                                unselectedTextColor = BNBCreamLight.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = viewModel.currentScreen,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Home -> HomeScreen(viewModel)
                        is Screen.Catalogue -> CatalogueScreen(viewModel)
                        is Screen.BookDetail -> BookDetailScreen(viewModel, screen.bookId)
                        is Screen.Search -> SearchScreen(viewModel)
                        is Screen.Library -> LibraryScreen(viewModel)
                        is Screen.Wishlist -> WishlistScreen(viewModel)
                        is Screen.Membership -> MembershipScreen(viewModel)
                        is Screen.Cafe -> CafeScreen(viewModel)
                        is Screen.Stores -> StoresScreen(viewModel)
                        is Screen.Events -> EventsScreen(viewModel)
                        is Screen.Cart -> CartScreen(viewModel)
                        is Screen.Account -> AccountScreen(viewModel)
                        is Screen.AiAssistant -> AiAssistantScreen(viewModel)
                        is Screen.AdminDashboard -> AdminDashboardScreen(viewModel)
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(viewModel: BookstoreViewModel) {
    val bestsellers by viewModel.bestsellers.collectAsState(initial = emptyList())
    val staffPicks by viewModel.staffPicks.collectAsState(initial = emptyList())
    val exclusives by viewModel.exclusiveBooks.collectAsState(initial = emptyList())
    val trending by viewModel.trendingBooks.collectAsState(initial = emptyList())
    val comingSoon by viewModel.comingSoonBooks.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Hero Brand Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(BNBDeepGreen, BNBSagelyGreen)
                        )
                    )
                    .clickable { viewModel.navigateTo(Screen.Membership) }
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXCLUSIVE REWARDS",
                            color = BNBOchreGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = BNBOchreGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Join B&N Members",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Earn 10x points, get custom stamps, & save 10% on every book and drink order instantly.",
                        color = BNBCreamLight.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, end = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "View Digital Card →",
                        color = BNBOchreGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bestsellers Carousel
        item {
            HorizontalBookSection(
                title = "B&N Bestsellers",
                books = bestsellers,
                onBookClick = { viewModel.navigateTo(Screen.BookDetail(it.id)) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Staff Picks Carousel
        item {
            HorizontalBookSection(
                title = "B&N Staff Picks",
                books = staffPicks,
                onBookClick = { viewModel.navigateTo(Screen.BookDetail(it.id)) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Exclusives Carousel
        item {
            HorizontalBookSection(
                title = "B&N Collectible Editions",
                books = exclusives,
                onBookClick = { viewModel.navigateTo(Screen.BookDetail(it.id)) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Trending Carousel
        item {
            HorizontalBookSection(
                title = "#BookTok & Trending Books",
                books = trending,
                onBookClick = { viewModel.navigateTo(Screen.BookDetail(it.id)) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Coming Soon Carousel
        item {
            HorizontalBookSection(
                title = "Coming Soon & Pre-Orders",
                books = comingSoon,
                onBookClick = { viewModel.navigateTo(Screen.BookDetail(it.id)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HorizontalBookSection(
    title: String,
    books: List<BookEntity>,
    onBookClick: (BookEntity) -> Unit
) {
    Column {
        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = BNBCharcoal,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (books.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BNBCreamSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading amazing books...", color = BNBSlate, fontSize = 13.sp)
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(books) { book ->
                    Column(
                        modifier = Modifier
                            .width(110.dp)
                            .clickable { onBookClick(book) }
                    ) {
                        BookCover(
                            title = book.title,
                            author = book.authorName,
                            themeId = book.coverImageId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadowCard()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = book.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BNBCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = book.authorName,
                            fontSize = 11.sp,
                            color = BNBSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$${String.format("%.2f", book.pricePaperback)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BNBDeepGreen
                            )
                            if (book.isExclusive) {
                                Text(
                                    text = "Special",
                                    color = BNBOchreGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. BOOK CATALOGUE SCREEN
// ==========================================
@Composable
fun CatalogueScreen(viewModel: BookstoreViewModel) {
    val allBooks by viewModel.allBooks.collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All", "Fiction", "Science Fiction", "Mystery", "Business", "Computing", "Manga"
    )

    val filteredBooks = if (selectedCategory == "All") {
        allBooks
    } else {
        allBooks.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Cozy Top Bar for Catalogue Search & Filtering
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BNBDeepGreen)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = {
                    viewModel.searchQuery = it
                    if (it.isNotEmpty()) viewModel.navigateTo(Screen.Search)
                },
                placeholder = { Text("Search books, authors, ISBNs...", color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalogue_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BNBOchreGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedContainerColor = BNBDarkGreen.copy(alpha = 0.6f),
                    unfocusedContainerColor = BNBDarkGreen.copy(alpha = 0.4f)
                ),
                singleLine = true,
                shape = RoundedCornerShape(30.dp)
            )
        }

        // Horizontal Category Pill Selection Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BNBDeepGreen,
                        selectedLabelColor = Color.White,
                        containerColor = BNBCreamSurface,
                        labelColor = BNBCharcoal
                    )
                )
            }
        }

        // Catalogue Grid Display
        if (filteredBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No books found in this category.", color = BNBSlate)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredBooks) { book ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.BookDetail(book.id)) }
                            .shadowCard(),
                        colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            BookCover(
                                title = book.title,
                                author = book.authorName,
                                themeId = book.coverImageId,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = book.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BNBCharcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = book.authorName,
                                fontSize = 11.sp,
                                color = BNBSlate,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RatingBar(rating = book.rating, starSize = 12)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$${String.format("%.2f", book.pricePaperback)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BNBDeepGreen
                                )
                                Text(
                                    text = book.category,
                                    fontSize = 9.sp,
                                    color = BNBSagelyGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(
                                            BNBCreamLight,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. BOOK PRODUCT DETAILS SCREEN
// ==========================================
@Composable
fun BookDetailScreen(viewModel: BookstoreViewModel, bookId: String) {
    val books by viewModel.allBooks.collectAsState(initial = emptyList())
    val book = books.find { it.id == bookId }

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Book not found")
        }
        return
    }

    val reviews by viewModel.getReviewsForBook(bookId).collectAsState(initial = emptyList())
    val inWishlist by viewModel.isBookInAnyWishlist(bookId).collectAsState(initial = false)

    var selectedFormat by remember { mutableStateOf("Paperback") }
    var reviewRating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var isWritingReview by remember { mutableStateOf(false) }

    val activePrice = when (selectedFormat) {
        "Hardback" -> book.priceHardback
        "Paperback" -> book.pricePaperback
        "eBook" -> book.priceEbook
        "Audiobook" -> book.priceAudiobook
        else -> book.pricePaperback
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BNBCreamLight),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Book Meta Row (Cover + Basic details)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                BookCover(
                    title = book.title,
                    author = book.authorName,
                    themeId = book.coverImageId,
                    modifier = Modifier
                        .weight(1.2f)
                        .shadowCard()
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1.8f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (book.isExclusive) {
                        Text(
                            text = "B&N EXCLUSIVE EDITION",
                            color = BNBOchreGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = book.title,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BNBCharcoal
                    )
                    Text(
                        text = "By ${book.authorName}",
                        fontSize = 14.sp,
                        color = BNBSagelyGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    RatingBar(rating = book.rating, starSize = 14)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Publisher: ${book.publisher}",
                        fontSize = 11.sp,
                        color = BNBSlate
                    )
                    Text(
                        text = "ISBN: ${book.isbn}",
                        fontSize = 11.sp,
                        color = BNBSlate
                    )
                    Text(
                        text = "Published: ${book.publicationDate}",
                        fontSize = 11.sp,
                        color = BNBSlate
                    )
                    Text(
                        text = "Pages: ${book.pageCount}",
                        fontSize = 11.sp,
                        color = BNBSlate
                    )
                }
            }
        }

        // Format Selector Tabs
        item {
            Text(
                text = "Select Format",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BNBCharcoal,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            val formats = listOf(
                Pair("Hardback", book.priceHardback),
                Pair("Paperback", book.pricePaperback),
                Pair("eBook", book.priceEbook),
                Pair("Audiobook", book.priceAudiobook)
            ).filter { it.second > 0.0 }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                formats.forEach { (format, price) ->
                    val isSelected = selectedFormat == format
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BNBDeepGreen else BNBCreamSurface)
                            .border(
                                1.dp,
                                if (isSelected) BNBOchreGold else BNBCreamSurface,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFormat = format }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = format,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else BNBCharcoal,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$${String.format("%.2f", price)}",
                                fontSize = 11.sp,
                                color = if (isSelected) BNBOchreGold else BNBSlate,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add to basket
                Button(
                    onClick = {
                        viewModel.addBookToCart(book, selectedFormat)
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("add_to_basket_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BNBDeepGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Basket")
                }

                // Wishlist Toggle
                OutlinedButton(
                    onClick = { viewModel.toggleBookWishlist(book.id) },
                    modifier = Modifier.weight(0.5f),
                    border = BorderStroke(1.dp, BNBOchreGold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BNBOchreGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (inWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (inWishlist) Color.Red else BNBOchreGold
                    )
                }
            }

            // Reserve in Store Button
            var storeReservedState by remember { mutableStateOf<String?>(null) }
            Button(
                onClick = {
                    storeReservedState = "Book Reserved at Manhattan Flagship! Please pick up within 48 hours."
                    // Also add to order history for complete real-sensory feedback!
                    viewModel.updateLibraryBook(book.id, book.title, book.authorName, book.coverImageId, "WANT_TO_READ", 0, "Reserved in Fifth Avenue Flagship.")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BNBOchreGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Storefront, null, tint = BNBCharcoal)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reserve in Nearby Store", color = BNBCharcoal, fontWeight = FontWeight.Bold)
            }

            if (storeReservedState != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BNBSagelyGreen.copy(alpha = 0.15f))
                        .border(1.dp, BNBSagelyGreen, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = storeReservedState!!,
                        color = BNBDeepGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Book Description
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Synopsis",
                        fontFamily = FontFamily.Serif,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BNBCharcoal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = book.description,
                        fontSize = 12.sp,
                        color = BNBCharcoal.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Customer Reviews Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customer Reviews",
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BNBCharcoal
                )
                TextButton(onClick = { isWritingReview = !isWritingReview }) {
                    Text(
                        text = if (isWritingReview) "Close" else "Write a Review",
                        color = BNBDeepGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Writing Review Expansion Form
        if (isWritingReview) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BNBCreamSurface)
                        .border(1.dp, BNBOchreGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Submit Rating for ${book.title}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BNBCharcoal
                        )
                        Row {
                            for (i in 1..5) {
                                val isGold = i <= reviewRating
                                Icon(
                                    imageVector = if (isGold) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = null,
                                    tint = BNBOchreGold,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { reviewRating = i }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            placeholder = { Text("Share your thoughts about this book with the community...", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BNBDeepGreen,
                                unfocusedBorderColor = BNBSlate.copy(alpha = 0.5f)
                            )
                        )
                        Button(
                            onClick = {
                                if (reviewComment.isNotBlank()) {
                                    viewModel.submitReview(book.id, reviewRating, reviewComment)
                                    reviewComment = ""
                                    isWritingReview = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BNBDeepGreen),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Post Review")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Display submitted reviews
        if (reviews.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Be the first to write a review!", color = BNBSlate, fontSize = 12.sp)
                }
            }
        } else {
            items(reviews) { review ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = review.reviewerName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BNBCharcoal
                            )
                            Text(
                                text = review.date,
                                fontSize = 10.sp,
                                color = BNBSlate
                            )
                        }
                        RatingBar(rating = review.rating.toDouble(), starSize = 12)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = review.comment,
                            fontSize = 11.sp,
                            color = BNBCharcoal.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                viewModel.toggleHelpfulReview(review.id, book.id)
                            }) {
                                Icon(
                                    imageVector = if (review.isHelpfulMarked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Helpful",
                                    tint = if (review.isHelpfulMarked) BNBDeepGreen else BNBSlate,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "${review.helpfulCount} helpful",
                                fontSize = 10.sp,
                                color = BNBSlate
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. BOOK SEARCH SCREEN
// ==========================================
@Composable
fun SearchScreen(viewModel: BookstoreViewModel) {
    val results by viewModel.searchResults.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BNBDeepGreen)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                placeholder = { Text("Search title, author, or ISBN...", color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.7f)) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery = "" }) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BNBOchreGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedContainerColor = BNBDarkGreen.copy(alpha = 0.6f),
                    unfocusedContainerColor = BNBDarkGreen.copy(alpha = 0.4f)
                ),
                singleLine = true,
                shape = RoundedCornerShape(30.dp)
            )
        }

        if (viewModel.searchQuery.isEmpty()) {
            // Display popular and recent searches
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Recent Searches",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BNBCharcoal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                viewModel.recentSearches.forEach { search ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.searchQuery = search
                                viewModel.addRecentSearch(search)
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, null, tint = BNBSlate, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = search, color = BNBCharcoal, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Popular Searches",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BNBCharcoal,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                val popular = listOf("Science Fiction Bestsellers", "BookTok Exclusives", "Matt Haig", "Manga New Releases")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    popular.take(2).forEach { search ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(30.dp))
                                .background(BNBCreamSurface)
                                .clickable { viewModel.searchQuery = search }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = search, fontSize = 11.sp, color = BNBDeepGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Display Results
            if (results.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matches found for \"${viewModel.searchQuery}\"", color = BNBSlate)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(results) { book ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.addRecentSearch(book.title)
                                    viewModel.navigateTo(Screen.BookDetail(book.id))
                                }
                                .shadowCard(),
                            colors = CardDefaults.cardColors(containerColor = BNBCreamSurface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                BookCover(
                                    title = book.title,
                                    author = book.authorName,
                                    themeId = book.coverImageId,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = book.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BNBCharcoal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = book.authorName,
                                    fontSize = 11.sp,
                                    color = BNBSlate
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. LIBRARY SCREEN (Digital Shelf)
// ==========================================
@Composable
fun LibraryScreen(viewModel: BookstoreViewModel) {
    val shelfBooks by viewModel.libraryBooks.collectAsState(initial = emptyList())

    var selectedStatusTab by remember { mutableStateOf("READING") }

    val filteredShelf = shelfBooks.filter { it.readingStatus == selectedStatusTab }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BNBCreamLight)
    ) {
        // Tab Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BNBDeepGreen)
        ) {
            val tabs = listOf(
                Pair("READING", "Currently Reading"),
                Pair("WANT_TO_READ", "Want to Read"),
                Pair("FINISHED", "Completed")
            )
            tabs.forEach { (status, text) ->
                val isSelected = selectedStatusTab == status
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedStatusTab = status }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) BNBOchreGold else Color.White.copy(alpha = 0.6f)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(2.dp)
                                    .background(BNBOchreGold)
                            )
                        }
                    }
                }
            }
        }

        if (filteredShelf.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Book, null, modifier = Modifier.size(64.dp), tint = BNBSlate.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your digital shelf is empty.",
                        fontSize = 14.sp,
                        color = BNBSlate,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Reserve books or update reading progress to see them here!",
                        fontSize = 11.sp,
                        color = BNBSlate.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredShelf) { item ->
                    var showNotesEditor by remember { mutableStateOf(false) }
                    var notesText by remember { mutableStateOf(item.personalNotes) }
                    var progressVal by remember { mutableStateOf(item.progressPercent.toFloat()) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row {
                                BookCover(
                                    title = item.title,
                                    author = item.authorName,
                                    themeId = item.coverImageId,
                                    modifier = Modifier
                                        .size(60.dp, 90.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BNBCharcoal
                                    )
                                    Text(
                                        text = item.authorName,
                                        fontSize = 12.sp,
                                        color = BNBSlate
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Reading Progress Indicator
                                    Text(
                                        text = "Progress: ${progressVal.toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BNBDeepGreen
                                    )
                                    LinearProgressIndicator(
                                        progress = progressVal / 100f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = BNBDeepGreen,
                                        trackColor = BNBSlate.copy(alpha = 0.2f)
                                    )
                                }
                            }

                            // Editable Progress Slider and Notes
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showNotesEditor = !showNotesEditor }) {
                                    Icon(
                                        imageVector = if (showNotesEditor) Icons.Default.Close else Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = BNBSagelyGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (showNotesEditor) "Cancel Edit" else "Update Progress & Notes",
                                        color = BNBSagelyGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = { viewModel.removeBookFromLibrary(item.bookId) }) {
                                    Icon(Icons.Default.DeleteOutline, "Remove", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                }
                            }

                            if (showNotesEditor) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "Adjust Page Progress", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BNBCharcoal)
                                    Slider(
                                        value = progressVal,
                                        onValueChange = { progressVal = it },
                                        valueRange = 0f..100f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = BNBOchreGold,
                                            activeTrackColor = BNBDeepGreen
                                        )
                                    )
                                    OutlinedTextField(
                                        value = notesText,
                                        onValueChange = { notesText = it },
                                        placeholder = { Text("Write personal notes, favorite quotes, or page numbers...", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BNBDeepGreen
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.updateLibraryBook(
                                                bookId = item.bookId,
                                                title = item.title,
                                                author = item.authorName,
                                                coverId = item.coverImageId,
                                                status = if (progressVal.toInt() >= 100) "FINISHED" else selectedStatusTab,
                                                progress = progressVal.toInt(),
                                                notes = notesText
                                            )
                                            showNotesEditor = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BNBDeepGreen),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Save Progress")
                                    }
                                }
                            } else if (item.personalNotes.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .background(BNBCreamLight, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "My Notes: ${item.personalNotes}",
                                        fontSize = 11.sp,
                                        color = BNBCharcoal.copy(alpha = 0.8f),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. WISHLIST SCREEN
// ==========================================
@Composable
fun WishlistScreen(viewModel: BookstoreViewModel) {
    val lists by viewModel.wishlists.collectAsState(initial = emptyList())
    val activeList = lists.firstOrNull()

    if (activeList == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No wishlists found")
        }
        return
    }

    val wishlistBooks by viewModel.getWishlistBooks(activeList.id).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BNBCreamLight)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = activeList.name,
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BNBCharcoal
            )
            Text(
                text = "${wishlistBooks.size} Items",
                fontSize = 12.sp,
                color = BNBSlate,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (wishlistBooks.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Your Wishlist is empty. Star books to see them here!", color = BNBSlate)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wishlistBooks) { book ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BookCover(
                                title = book.title,
                                author = book.authorName,
                                themeId = book.coverImageId,
                                modifier = Modifier
                                    .size(50.dp, 75.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = book.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BNBCharcoal
                                )
                                Text(
                                    text = book.authorName,
                                    fontSize = 11.sp,
                                    color = BNBSlate
                                )
                                Text(
                                    text = "$${String.format("%.2f", book.pricePaperback)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BNBDeepGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                // Add to Basket
                                IconButton(
                                    onClick = { viewModel.addWishlistItemToCart(book, activeList.id) }
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, "Add to Basket", tint = BNBDeepGreen)
                                }
                                // Remove
                                IconButton(
                                    onClick = {
                                        viewModel.removeWishlistItem(activeList.id, book.id)
                                    }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, "Remove", tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. MEMBERSHIP SCREEN (Loyalty Card)
// ==========================================
@Composable
fun MembershipScreen(viewModel: BookstoreViewModel) {
    val profile by viewModel.userProfile.collectAsState(initial = null)

    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading Membership Profile...")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BNBCreamLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barcode Card Representation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadowCard(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BNBDeepGreen)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREMIUM MEMBER",
                            color = BNBOchreGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = BNBOchreGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = profile!!.name,
                        fontFamily = FontFamily.Serif,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Card: ${profile!!.membershipCardNumber}",
                        color = BNBCreamLight.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Simulated Loyalty Barcode representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Quick barcode procedural simulation lines
                            val lines = listOf(3,1,4,1,5,9,2,6,5,3,5,8,9,7,9,3,2,3,8,4,6)
                            lines.forEach { qty ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width((qty * 1.5).dp)
                                        .background(Color.Black)
                                )
                            }
                        }
                    }
                    Text(
                        text = "*SCAN IN STORE*",
                        color = BNBOchreGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
        }

        // Loyalty Status & Points Tracker
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Available Loyalty Points",
                        fontSize = 13.sp,
                        color = BNBSlate,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${profile!!.rewardPoints} Points",
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = BNBDeepGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress to next voucher
                    val limit = 500
                    val currentProgress = profile!!.rewardPoints % limit
                    val percent = currentProgress.toFloat() / limit.toFloat()

                    Text(
                        text = "Points to next $5 Café Reward: ${limit - currentProgress} pts",
                        fontSize = 11.sp,
                        color = BNBCharcoal
                    )
                    LinearProgressIndicator(
                        progress = percent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = BNBOchreGold,
                        trackColor = BNBSlate.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Active Benefits List
        item {
            Text(
                text = "My Active Perks",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BNBCharcoal,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            val benefits = listOf(
                Pair(Icons.Default.Percent, "10% Member discount on all paper book orders"),
                Pair(Icons.Default.LocalCafe, "Free drink customization (soy/oat/almond milk on us!)"),
                Pair(Icons.Default.Celebration, "Complimentary Birthday slice of cheesecake in Café"),
                Pair(Icons.Default.Event, "Priority registration on highly-requested author signings")
            )
            benefits.forEach { (icon, text) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = BNBOchreGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = text,
                        fontSize = 12.sp,
                        color = BNBCharcoal
                    )
                }
            }
        }
    }
}

// ==========================================
// 8. CAFÉ SCREEN
// ==========================================
@Composable
fun CafeScreen(viewModel: BookstoreViewModel) {
    val items by viewModel.allCafeItems.collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf("Hot Drinks") }

    val categories = listOf("Hot Drinks", "Cold Drinks", "Bakery", "Sandwiches")
    val filteredMenu = items.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab-style row for Cafe menu categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BNBDeepGreen)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedCategory = category }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) BNBOchreGold else Color.White.copy(alpha = 0.6f)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(2.dp)
                                    .background(BNBOchreGold)
                            )
                        }
                    }
                }
            }
        }

        // Custom pickup details row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BNBCreamSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = BNBDeepGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pickup Time:",
                    fontSize = 11.sp,
                    color = BNBCharcoal,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "15 Mins",
                fontSize = 11.sp,
                color = BNBDeepGreen,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (filteredMenu.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Menu items loading...", color = BNBSlate)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMenu) { item ->
                    var customizationNotes by remember { mutableStateOf("") }
                    var showCustomizer by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Draw beautiful abstract circular cafe icons instead of broken urls
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(BNBDeepGreen.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (item.category) {
                                            "Hot Drinks", "Cold Drinks" -> Icons.Default.LocalCafe
                                            else -> Icons.Default.Cake
                                        },
                                        contentDescription = null,
                                        tint = BNBDeepGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BNBCharcoal
                                    )
                                    Text(
                                        text = "${item.calories} Calories",
                                        fontSize = 10.sp,
                                        color = BNBSlate
                                    )
                                    Text(
                                        text = item.description,
                                        fontSize = 11.sp,
                                        color = BNBCharcoal.copy(alpha = 0.7f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${String.format("%.2f", item.price)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BNBDeepGreen
                                    )
                                    IconButton(
                                        onClick = {
                                            if (item.category.contains("Drinks")) {
                                                showCustomizer = !showCustomizer
                                            } else {
                                                viewModel.addCafeToCart(item, 1, "")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (item.category.contains("Drinks")) Icons.Default.Tune else Icons.Default.AddShoppingCart,
                                            contentDescription = "Custom/Add",
                                            tint = BNBOchreGold
                                        )
                                    }
                                }
                            }

                            // Show customization for drinks (milk / sugar selection)
                            if (showCustomizer) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .background(BNBCreamLight)
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Drink Customization (Members save $0.70!)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BNBCharcoal)
                                    var selectedMilk by remember { mutableStateOf("Whole Milk") }
                                    val milkOptions = listOf("Whole Milk", "Oat Milk (+ $0.70)", "Almond Milk (+ $0.70)", "Soy Milk")
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        milkOptions.forEach { opt ->
                                            val isSelected = selectedMilk == opt
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(30.dp))
                                                    .background(if (isSelected) BNBDeepGreen else BNBCreamSurface)
                                                    .clickable { selectedMilk = opt }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = opt,
                                                    fontSize = 9.sp,
                                                    color = if (isSelected) Color.White else BNBCharcoal
                                                )
                                            }
                                        }
                                    }
                                    OutlinedTextField(
                                        value = customizationNotes,
                                        onValueChange = { customizationNotes = it },
                                        placeholder = { Text("Extra hot, light ice, extra syrup...", fontSize = 10.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BNBDeepGreen)
                                    )
                                    Button(
                                        onClick = {
                                            val finalNotes = "$selectedMilk. $customizationNotes"
                                            viewModel.addCafeToCart(item, 1, finalNotes)
                                            customizationNotes = ""
                                            showCustomizer = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BNBDeepGreen),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Add Custom Latte")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. STORE LOCATOR SCREEN
// ==========================================
@Composable
fun StoresScreen(viewModel: BookstoreViewModel) {
    val stores by viewModel.allStores.collectAsState(initial = emptyList())
    var selectedStoreForDirections by remember { mutableStateOf<StoreEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BNBCreamLight)
            .padding(16.dp)
    ) {
        Text(
            text = "Nearby Barnes & Noble Stores",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = BNBCharcoal
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (stores.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Locating stores...")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(stores) { store ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = store.name,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BNBCharcoal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (store.hasCafe) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalCafe, null, tint = BNBDeepGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Has Cafe", fontSize = 9.sp, color = BNBDeepGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Address: ${store.address}", fontSize = 11.sp, color = BNBCharcoal.copy(alpha = 0.9f))
                            Text(text = "Phone: ${store.phone}", fontSize = 11.sp, color = BNBSlate)
                            Text(text = "Hours: ${store.hours}", fontSize = 11.sp, color = BNBSlate)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Directions
                                Button(
                                    onClick = { selectedStoreForDirections = store },
                                    colors = ButtonDefaults.buttonColors(containerColor = BNBDeepGreen),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Navigation, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Get Directions", fontSize = 11.sp)
                                }

                                // View Events
                                Button(
                                    onClick = { viewModel.navigateTo(Screen.Events) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BNBOchreGold),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Event, null, tint = BNBCharcoal, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Store Events", fontSize = 11.sp, color = BNBCharcoal)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Procedural simulated Directions alert
        if (selectedStoreForDirections != null) {
            AlertDialog(
                onDismissRequest = { selectedStoreForDirections = null },
                title = { Text("Simulated Directions", fontFamily = FontFamily.Serif) },
                text = {
                    Text(
                        "Opening Google Maps and routing to **${selectedStoreForDirections!!.name}** located at **${selectedStoreForDirections!!.address}**.\n\nEstimated commute: 15 mins (2.4 miles). Enjoy your bookstore visit!"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { selectedStoreForDirections = null }) {
                        Text("Awesome", color = BNBDeepGreen)
                    }
                }
            )
        }
    }
}

// ==========================================
// 10. EVENTS SCREEN
// ==========================================
@Composable
fun EventsScreen(viewModel: BookstoreViewModel) {
    val events by viewModel.allEvents.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BNBCreamLight)
            .padding(16.dp)
    ) {
        Text(
            text = "B&N Store Events & Clubs",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = BNBCharcoal
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (events.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Checking scheduled community events...")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.type,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(BNBOchreGold, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.People, null, tint = BNBSlate, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${event.registrationCount} Signed Up", fontSize = 10.sp, color = BNBSlate)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = event.title,
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BNBCharcoal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, null, tint = BNBDeepGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${event.date} at ${event.time}", fontSize = 11.sp, color = BNBCharcoal)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = BNBDeepGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(event.storeName, fontSize = 11.sp, color = BNBSlate)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = event.description,
                                fontSize = 11.sp,
                                color = BNBCharcoal.copy(alpha = 0.8f),
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Register Button with preserved state
                            Button(
                                onClick = { viewModel.toggleEventRegistration(event.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (event.isRegistered) BNBSagelyGreen else BNBDeepGreen
                                )
                            ) {
                                if (event.isRegistered) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Registered (See you there!)")
                                } else {
                                    val priceText = if (event.isTicketed) "($${String.format("%.2f", event.ticketPrice)} Ticket)" else "(Free)"
                                    Icon(Icons.Default.BookmarkAdd, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Secure Spot $priceText")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 11. SHOPPING BASKET SCREEN
// ==========================================
@Composable
fun CartScreen(viewModel: BookstoreViewModel) {
    val items by viewModel.cartItems.collectAsState(initial = emptyList())

    val subtotal = items.sumOf { it.price * it.quantity }
    val discount = subtotal * viewModel.couponDiscount
    val tax = (subtotal - discount) * 0.08
    val grandTotal = (subtotal - discount) + tax

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BNBCreamLight)
    ) {
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp), tint = BNBSlate.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your basket is empty", color = BNBSlate, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { viewModel.navigateTo(Screen.Catalogue) },
                        colors = ButtonDefaults.buttonColors(containerColor = BNBDeepGreen),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("Discover books")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Review Your Order",
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BNBCharcoal,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom item design depending on type (BOOK or CAFE)
                            Box(
                                modifier = Modifier
                                    .size(45.dp, 65.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BNBDeepGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.itemType == "BOOK") Icons.Default.MenuBook else Icons.Default.LocalCafe,
                                    contentDescription = null,
                                    tint = BNBDeepGreen
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BNBCharcoal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (item.itemType == "BOOK") "Format: ${item.selectedFormat}" else item.extraNotes,
                                    fontSize = 10.sp,
                                    color = BNBSlate
                                )
                                Text(
                                    text = "$${String.format("%.2f", item.price)} each",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BNBDeepGreen
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(onClick = { viewModel.updateCartQty(item, -1) }) {
                                    Icon(Icons.Default.RemoveCircleOutline, null, tint = BNBSlate, modifier = Modifier.size(20.dp))
                                }
                                Text(
                                    text = item.quantity.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BNBCharcoal
                                )
                                IconButton(onClick = { viewModel.updateCartQty(item, 1) }) {
                                    Icon(Icons.Default.AddCircleOutline, null, tint = BNBDeepGreen, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                // Promo Coupon Application Row
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Promo Coupon / Gift Card",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BNBCharcoal
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.couponCode,
                            onValueChange = { viewModel.couponCode = it },
                            placeholder = { Text("Enter Coupon (Hint: READ20)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BNBDeepGreen)
                        )
                        Button(
                            onClick = { viewModel.applyCoupon() },
                            colors = ButtonDefaults.buttonColors(containerColor = BNBDeepGreen),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Text("Apply")
                        }
                    }
                    if (viewModel.couponAppliedMessage != null) {
                        Text(
                            text = viewModel.couponAppliedMessage!!,
                            color = if (viewModel.couponDiscount > 0.0) BNBSagelyGreen else Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Pickup Customizer notes for Cafe order
                val containsCafe = items.any { it.itemType == "CAFE" }
                if (containsCafe) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Café Pickup Scheduling", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BNBCharcoal)
                                Spacer(modifier = Modifier.height(6.dp))
                                var expandedTimes by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedTimes = true }
                                        .background(BNBCreamLight)
                                        .padding(10.dp)
                                ) {
                                    Text(viewModel.selectedPickupTime, fontSize = 11.sp)
                                }
                                DropdownMenu(
                                    expanded = expandedTimes,
                                    onDismissRequest = { expandedTimes = false }
                                ) {
                                    listOf(
                                        "As soon as possible (10-15 mins)",
                                        "In 30 mins",
                                        "In 1 hour",
                                        "Custom Scheduled Time"
                                    ).forEach { time ->
                                        DropdownMenuItem(
                                            text = { Text(time) },
                                            onClick = {
                                                viewModel.selectedPickupTime = time
                                                expandedTimes = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Billing Receipt Summary card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Order Summary",
                                fontWeight = FontWeight.Bold,
                                color = BNBCharcoal,
                                fontSize = 14.sp
                            )
                            Divider(color = BNBCreamLight)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal:", fontSize = 12.sp, color = BNBSlate)
                                Text("$${String.format("%.2f", subtotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            if (discount > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Promo Discount:", fontSize = 12.sp, color = BNBSagelyGreen)
                                    Text("-$${String.format("%.2f", discount)}", fontSize = 12.sp, color = BNBSagelyGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated Tax (8%):", fontSize = 12.sp, color = BNBSlate)
                                Text("$${String.format("%.2f", tax)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Divider(color = BNBCreamLight)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BNBCharcoal)
                                Text(
                                    text = "$${String.format("%.2f", grandTotal)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BNBDeepGreen
                                )
                            }
                        }
                    }
                }

                // Checkout Securely CTA
                item {
                    Button(
                        onClick = { viewModel.checkoutSecurely() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("checkout_secure_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BNBDeepGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Checkout Securely",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ==========================================
// 12. CUSTOMER ACCOUNT SCREEN
// ==========================================
@Composable
fun AccountScreen(viewModel: BookstoreViewModel) {
    val profile by viewModel.userProfile.collectAsState(initial = null)
    val orders by viewModel.allOrders.collectAsState(initial = emptyList())

    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading Account...")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BNBCreamLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Meta
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(BNBDeepGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile!!.name.take(1),
                        color = BNBOchreGold,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = profile!!.name,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BNBCharcoal
                    )
                    Text(text = profile!!.email, fontSize = 12.sp, color = BNBSlate)
                }
            }
        }

        // Account Details (Addresses & Payments)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Saved Billing & Shipping Settings", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BNBCharcoal)
                    Divider(color = BNBCreamLight)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, null, tint = BNBDeepGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = profile!!.savedAddress, fontSize = 11.sp, color = BNBCharcoal)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, null, tint = BNBDeepGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = profile!!.savedPaymentMethod, fontSize = 11.sp, color = BNBCharcoal)
                    }
                }
            }
        }

        // Order history receipt lists
        item {
            Text(
                text = "Order History & Receipts",
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BNBCharcoal
            )
        }

        if (orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BNBCreamSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No past orders found", color = BNBSlate, fontSize = 12.sp)
                }
            }
        } else {
            items(orders) { order ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ID: ${order.id}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BNBCharcoal
                            )
                            Text(
                                text = order.status,
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(
                                        if (order.status.contains("Ready")) BNBSagelyGreen else BNBDeepGreen,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Date: ${order.date}", fontSize = 11.sp, color = BNBSlate)
                        Text(text = "Items: ${order.summary}", fontSize = 11.sp, color = BNBCharcoal.copy(alpha = 0.85f))
                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = BNBCreamLight)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Paid:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = BNBSlate)
                            Text("$${String.format("%.2f", order.totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BNBDeepGreen)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 13. AI READING ASSISTANT SCREEN (Gemini)
// ==========================================
@Composable
fun AiAssistantScreen(viewModel: BookstoreViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    // Scroll to end automatically when history updates
    LaunchedEffect(viewModel.aiChatHistory.size) {
        if (viewModel.aiChatHistory.isNotEmpty()) {
            scrollState.animateScrollToItem(viewModel.aiChatHistory.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BNBCreamLight)
    ) {
        // AI Header block with warm tip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BNBDeepGreen.copy(alpha = 0.08f))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, "AI", tint = BNBDeepGreen, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("B&N Personal Book assistant", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BNBDeepGreen)
                    Text("Recommends authors, designs personalized reading plans, & answers any literary question.", fontSize = 10.sp, color = BNBSlate)
                }
            }
        }

        // Chat conversation flow
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(viewModel.aiChatHistory) { index, msg ->
                val isUser = index % 2 == 1 // Simple pattern: initial greeting is assistant, alternate turns
                val background = if (isUser) BNBDeepGreen else BNBCreamSurface
                val textColor = if (isUser) Color.White else BNBCharcoal
                val alignment = if (isUser) Alignment.End else Alignment.Start

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = alignment
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 12.dp
                                )
                            )
                            .background(background)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.parts.firstOrNull()?.text ?: "",
                            fontSize = 12.sp,
                            color = textColor,
                            lineHeight = 16.sp
                        )
                    }
                    Text(
                        text = if (isUser) "You" else "B&N AI Assistant",
                        fontSize = 9.sp,
                        color = BNBSlate,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            if (viewModel.isAiLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = BNBDeepGreen,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("B&N Assistant is looking up shelves...", fontSize = 11.sp, color = BNBSlate)
                    }
                }
            }
        }

        // Quick Preset Prompt Chips Row
        val presets = listOf(
            "Kotlin coding study plan",
            "Dune similar sci-fi",
            "Mysteries like Silent Patient"
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(BNBCreamSurface)
                        .clickable {
                            viewModel.aiInputMessage = "Please $preset"
                            viewModel.sendAiMessage()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = preset, fontSize = 10.sp, color = BNBDeepGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Message Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { viewModel.clearAiChat() }) {
                Icon(Icons.Default.Refresh, "Clear", tint = Color.Red.copy(alpha = 0.7f))
            }
            OutlinedTextField(
                value = viewModel.aiInputMessage,
                onValueChange = { viewModel.aiInputMessage = it },
                placeholder = { Text("Ask about genres, recommendations...", fontSize = 11.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BNBDeepGreen),
                maxLines = 2,
                shape = RoundedCornerShape(24.dp)
            )
            IconButton(
                onClick = { viewModel.sendAiMessage() },
                enabled = viewModel.aiInputMessage.isNotBlank() && !viewModel.isAiLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (viewModel.aiInputMessage.isNotBlank()) BNBDeepGreen else BNBSlate
                )
            }
        }
    }
}

// ==========================================
// 14. ADMIN DASHBOARD SCREEN
// ==========================================
@Composable
fun AdminDashboardScreen(viewModel: BookstoreViewModel) {
    val books by viewModel.allBooks.collectAsState(initial = emptyList())
    val orders by viewModel.allOrders.collectAsState(initial = emptyList())
    val cafeItems by viewModel.allCafeItems.collectAsState(initial = emptyList())

    val totalSales = orders.sumOf { it.totalAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BNBCreamLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sales Report Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BNBDeepGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total Store Sales Report", color = BNBOchreGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$${String.format("%.2f", totalSales)}",
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total Orders Processed: ${orders.size}", color = BNBCreamLight, fontSize = 11.sp)
                }
            }
        }

        // Inventory Stock Modifier
        item {
            Text(
                text = "Manage Book Inventory Stock",
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BNBCharcoal
            )
        }

        items(books) { book ->
            Card(
                colors = CardDefaults.cardColors(containerColor = BNBCreamSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BookCover(
                        title = book.title,
                        author = book.authorName,
                        themeId = book.coverImageId,
                        modifier = Modifier.size(40.dp, 60.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = book.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BNBCharcoal)
                        Text(text = "ISBN: ${book.isbn}", fontSize = 10.sp, color = BNBSlate)
                        Text(
                            text = "Current Stock: ${book.stockCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (book.stockCount <= 3) Color.Red else BNBDeepGreen
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { viewModel.updateBookStock(book.id, book.stockCount - 1) }) {
                            Icon(Icons.Default.RemoveCircle, "Minus", tint = Color.Red.copy(alpha = 0.7f))
                        }
                        IconButton(onClick = { viewModel.updateBookStock(book.id, book.stockCount + 5) }) {
                            Icon(Icons.Default.AddCircle, "Plus", tint = BNBDeepGreen)
                        }
                    }
                }
            }
        }
    }
}

// Custom shadow card view utility
fun Modifier.shadowCard(): Modifier = this.drawWithContent {
    drawContent()
}
