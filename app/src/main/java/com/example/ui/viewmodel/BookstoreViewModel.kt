package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiClient
import com.example.data.ai.GeminiContent
import com.example.data.ai.GeminiPart
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.BookstoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen {
    object Home : Screen()
    object Catalogue : Screen()
    data class BookDetail(val bookId: String) : Screen()
    object Search : Screen()
    object Library : Screen()
    object Wishlist : Screen()
    object Membership : Screen()
    object Cafe : Screen()
    object Stores : Screen()
    object Events : Screen()
    object Cart : Screen()
    object Account : Screen()
    object AiAssistant : Screen()
    object AdminDashboard : Screen()
}

class BookstoreViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = BookstoreRepository(db)

    // --- Navigation System ---
    var currentScreen by mutableStateOf<Screen>(Screen.Home)
        private set

    private val screenBackStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            screenBackStack.add(currentScreen)
            currentScreen = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            currentScreen = screenBackStack.removeAt(screenBackStack.size - 1)
            return true
        }
        return false
    }

    fun navigateHome() {
        screenBackStack.clear()
        currentScreen = Screen.Home
    }

    // --- Observed Data Streams (StateFlows) ---
    val allBooks = repository.allBooks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val exclusiveBooks = repository.exclusiveBooks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bestsellers = repository.bestsellers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val trendingBooks = repository.trendingBooks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val staffPicks = repository.staffPicks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val comingSoonBooks = repository.comingSoonBooks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val newReleases = repository.newReleases.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStores = repository.allStores.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allEvents = repository.allEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCafeItems = repository.allCafeItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cartItems = repository.cartItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val libraryBooks = repository.libraryBooks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val wishlists = repository.wishlists.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allOrders = repository.allOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Book Detail Page Review State ---
    fun getReviewsForBook(bookId: String): Flow<List<ReviewEntity>> = repository.getReviewsForBook(bookId)

    // --- Wishlist Detail Book Stream ---
    fun getWishlistBooks(wishlistId: String): Flow<List<BookEntity>> = repository.getWishlistBooks(wishlistId)
    fun isBookInAnyWishlist(bookId: String): Flow<Boolean> = repository.isBookInAnyWishlist(bookId)

    // --- Search Logic ---
    var searchQuery by mutableStateOf("")
    val searchResults = snapshotFlow { searchQuery }
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                flowOf(emptyList())
            } else {
                repository.searchBooks(query)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var recentSearches by mutableStateOf(listOf("Matt Haig", "Project Hail Mary", "Atomic Habits", "Manga", "Computing"))
        private set

    fun addRecentSearch(search: String) {
        if (search.isNotBlank()) {
            recentSearches = (listOf(search) + recentSearches.filter { it != search }).take(5)
        }
    }

    // --- Cart Actions ---
    fun addBookToCart(book: BookEntity, format: String, qty: Int = 1) {
        viewModelScope.launch {
            val price = when (format) {
                "Hardback" -> book.priceHardback
                "Paperback" -> book.pricePaperback
                "eBook" -> book.priceEbook
                "Audiobook" -> book.priceAudiobook
                else -> book.pricePaperback
            }
            repository.addCartItem(
                CartItemEntity(
                    itemType = "BOOK",
                    itemId = book.id,
                    name = book.title,
                    price = price,
                    quantity = qty,
                    selectedFormat = format
                )
            )
        }
    }

    fun addCafeToCart(item: CafeItemEntity, qty: Int = 1, extraNotes: String = "") {
        viewModelScope.launch {
            repository.addCartItem(
                CartItemEntity(
                    itemType = "CAFE",
                    itemId = item.id,
                    name = item.name,
                    price = item.price,
                    quantity = qty,
                    extraNotes = extraNotes
                )
            )
        }
    }

    fun updateCartQty(item: CartItemEntity, change: Int) {
        viewModelScope.launch {
            val newQty = item.quantity + change
            if (newQty <= 0) {
                repository.removeCartItem(item)
            } else {
                repository.updateCartItem(item.copy(quantity = newQty))
            }
        }
    }

    fun removeCartItem(item: CartItemEntity) {
        viewModelScope.launch {
            repository.removeCartItem(item)
        }
    }

    // --- Library Actions ---
    fun updateLibraryBook(bookId: String, title: String, author: String, coverId: String, status: String, progress: Int, notes: String) {
        viewModelScope.launch {
            repository.saveLibraryBook(
                LibraryBookEntity(
                    bookId = bookId,
                    title = title,
                    authorName = author,
                    coverImageId = coverId,
                    readingStatus = status,
                    progressPercent = progress,
                    personalNotes = notes,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    fun removeBookFromLibrary(bookId: String) {
        viewModelScope.launch {
            repository.removeLibraryBook(bookId)
        }
    }

    // --- Wishlist Actions ---
    fun addWishlistItemToCart(book: BookEntity, wishlistId: String) {
        viewModelScope.launch {
            addBookToCart(book, "Paperback")
            repository.removeWishlistItem(wishlistId, book.id)
        }
    }

    fun toggleBookWishlist(bookId: String) {
        viewModelScope.launch {
            val list = wishlists.value.firstOrNull() ?: return@launch
            val inWishlist = repository.isBookInAnyWishlist(bookId).first()
            if (inWishlist) {
                repository.removeWishlistItem(list.id, bookId)
            } else {
                repository.addWishlistItem(list.id, bookId)
            }
        }
    }

    fun removeWishlistItem(wishlistId: String, bookId: String) {
        viewModelScope.launch {
            repository.removeWishlistItem(wishlistId, bookId)
        }
    }

    fun toggleHelpfulReview(reviewId: String, bookId: String) {
        viewModelScope.launch {
            repository.toggleHelpfulReview(reviewId, bookId)
        }
    }

    // --- Event Registration ---
    fun toggleEventRegistration(eventId: String) {
        viewModelScope.launch {
            repository.registerForEvent(eventId)
        }
    }

    // --- Review submission ---
    fun submitReview(bookId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfileEntity()
            val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            val dateStr = dateFormat.format(Date())
            repository.addReview(
                ReviewEntity(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    reviewerName = profile.name,
                    rating = rating,
                    comment = comment,
                    date = dateStr,
                    helpfulCount = 0
                )
            )
        }
    }

    // --- Cafe Custom Order Scheduling ---
    var selectedPickupTime by mutableStateOf("As soon as possible (10-15 mins)")
    var cafeOrderNotes by mutableStateOf("")

    // --- Checkout Process ---
    var couponCode by mutableStateOf("")
    var couponAppliedMessage by mutableStateOf<String?>(null)
    var couponDiscount by mutableStateOf(0.0)

    fun applyCoupon() {
        if (couponCode.lowercase(Locale.US) == "read20") {
            couponDiscount = 0.20
            couponAppliedMessage = "20% Discount Applied!"
        } else {
            couponAppliedMessage = "Invalid coupon code"
            couponDiscount = 0.0
        }
    }

    fun checkoutSecurely() {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) return@launch

            val subtotal = items.sumOf { it.price * it.quantity }
            val discount = subtotal * couponDiscount
            val tax = (subtotal - discount) * 0.08
            val total = (subtotal - discount) + tax

            val summary = items.joinToString(", ") { "${it.name} x${it.quantity} (${it.selectedFormat})" }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val dateStr = dateFormat.format(Date())

            val orderId = "BN-" + (100000 + Random().nextInt(900000))
            val isCafeOnly = items.all { it.itemType == "CAFE" }
            val status = if (isCafeOnly) "Ready for Café Pickup" else "Processing"

            repository.placeOrder(
                OrderEntity(
                    id = orderId,
                    date = dateStr,
                    totalAmount = total,
                    status = status,
                    summary = summary
                )
            )

            // Award reward points for loyalty! 10 points per dollar
            val profile = userProfile.value ?: UserProfileEntity()
            val addedPoints = (total * 10).toInt()
            repository.updateUserProfile(
                profile.copy(
                    rewardPoints = profile.rewardPoints + addedPoints
                )
            )

            couponCode = ""
            couponDiscount = 0.0
            couponAppliedMessage = null

            // Navigate to order history/account to view receipt!
            navigateTo(Screen.Account)
        }
    }

    // --- AI Assistant Chat Flow ---
    var aiChatHistory = mutableStateListOf<GeminiContent>()
        private set

    var aiInputMessage by mutableStateOf("")
    var isAiLoading by mutableStateOf(false)

    fun sendAiMessage() {
        val query = aiInputMessage.trim()
        if (query.isEmpty() || isAiLoading) return

        aiInputMessage = ""
        aiChatHistory.add(GeminiContent(parts = listOf(GeminiPart(text = query))))
        isAiLoading = true

        viewModelScope.launch {
            val historyToSend = aiChatHistory.toList()
            val responseText = GeminiClient.getBookRecommendation(query, historyToSend)
            aiChatHistory.add(GeminiContent(parts = listOf(GeminiPart(text = responseText))))
            isAiLoading = false
        }
    }

    fun clearAiChat() {
        aiChatHistory.clear()
        aiChatHistory.add(
            GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "Hello! I am your **Barnes & Noble Reading Assistant**. \n\nI can suggest book recommendations, analyze genres, find authors similar to your favorites, design customized study or reading plans, and help answer book-related questions!\n\n**What kind of book are you looking for today?**"
                    )
                )
            )
        )
    }

    // Initialize AI chat
    init {
        clearAiChat()
    }

    // --- Admin Dashboard Stock Management ---
    fun updateBookStock(bookId: String, newStock: Int) {
        viewModelScope.launch {
            val book = repository.getBookById(bookId) ?: return@launch
            repository.insertBook(book.copy(stockCount = newStock))
        }
    }

    fun deleteReviewAdmin(review: ReviewEntity) {
        viewModelScope.launch {
            // Can be extended or handled via DAO easily. For simplicity, since it's admin,
            // we can let staff read and oversee ratings.
        }
    }
}

// Simple stateful list helper for ViewModels
private fun <T> mutableStateListOf(vararg elements: T): androidx.compose.runtime.snapshots.SnapshotStateList<T> {
    val list = androidx.compose.runtime.snapshots.SnapshotStateList<T>()
    list.addAll(elements)
    return list
}
