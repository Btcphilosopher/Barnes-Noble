package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class BookstoreRepository(private val db: AppDatabase) {

    private val bookDao = db.bookDao()
    private val authorDao = db.authorDao()
    private val storeDao = db.storeDao()
    private val eventDao = db.eventDao()
    private val cafeItemDao = db.cafeItemDao()
    private val cartDao = db.cartDao()
    private val libraryBookDao = db.libraryBookDao()
    private val wishlistDao = db.wishlistDao()
    private val reviewDao = db.reviewDao()
    private val userProfileDao = db.userProfileDao()
    private val orderDao = db.orderDao()

    init {
        // Pre-populate database with initial data in the background if it is empty
        CoroutineScope(Dispatchers.IO).launch {
            try {
                prepopulateIfNeeded()
            } catch (e: Exception) {
                Log.e("BookstoreRepository", "Error prepopulating database", e)
            }
        }
    }

    // --- Book Queries ---
    val allBooks: Flow<List<BookEntity>> = bookDao.getAllBooks()
    val exclusiveBooks: Flow<List<BookEntity>> = bookDao.getExclusiveBooks()
    val bestsellers: Flow<List<BookEntity>> = bookDao.getBestsellers()
    val trendingBooks: Flow<List<BookEntity>> = bookDao.getTrendingBooks()
    val staffPicks: Flow<List<BookEntity>> = bookDao.getStaffPicks()
    val comingSoonBooks: Flow<List<BookEntity>> = bookDao.getComingSoonBooks()
    val newReleases: Flow<List<BookEntity>> = bookDao.getNewReleases()

    suspend fun getBookById(id: String): BookEntity? = bookDao.getBookById(id)
    fun getBooksByCategory(category: String): Flow<List<BookEntity>> = bookDao.getBooksByCategory(category)
    fun searchBooks(query: String): Flow<List<BookEntity>> = bookDao.searchBooks(query)
    suspend fun insertBook(book: BookEntity) = bookDao.insertBook(book)

    // --- Author Queries ---
    suspend fun getAuthorById(id: String): AuthorEntity? = authorDao.getAuthorById(id)

    // --- Store Queries ---
    val allStores: Flow<List<StoreEntity>> = storeDao.getAllStores()
    suspend fun getStoreById(id: String): StoreEntity? = storeDao.getStoreById(id)

    // --- Event Queries ---
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    suspend fun registerForEvent(eventId: String): Boolean {
        val event = eventDao.getEventById(eventId) ?: return false
        val newRegistered = !event.isRegistered
        val diff = if (newRegistered) 1 else -1
        eventDao.setEventRegistration(eventId, newRegistered, diff)
        return newRegistered
    }

    // --- Cafe Queries ---
    val allCafeItems: Flow<List<CafeItemEntity>> = cafeItemDao.getAllCafeItems()
    fun getCafeItemsByCategory(category: String): Flow<List<CafeItemEntity>> = cafeItemDao.getCafeItemsByCategory(category)

    // --- Cart Queries ---
    val cartItems: Flow<List<CartItemEntity>> = cartDao.getCartItems()
    suspend fun addCartItem(item: CartItemEntity) = cartDao.insertCartItem(item)
    suspend fun updateCartItem(item: CartItemEntity) = cartDao.updateCartItem(item)
    suspend fun removeCartItem(item: CartItemEntity) = cartDao.deleteCartItem(item)
    suspend fun clearCart() = cartDao.clearCart()

    // --- Library Queries ---
    val libraryBooks: Flow<List<LibraryBookEntity>> = libraryBookDao.getLibraryBooks()
    suspend fun getLibraryBookById(bookId: String): LibraryBookEntity? = libraryBookDao.getLibraryBookById(bookId)
    suspend fun saveLibraryBook(book: LibraryBookEntity) = libraryBookDao.insertLibraryBook(book)
    suspend fun removeLibraryBook(bookId: String) = libraryBookDao.removeLibraryBook(bookId)

    // --- Wishlist Queries ---
    val wishlists: Flow<List<WishlistEntity>> = wishlistDao.getWishlists()
    suspend fun createWishlist(name: String) = wishlistDao.insertWishlist(WishlistEntity(UUID.randomUUID().toString(), name))
    suspend fun deleteWishlist(id: String) = wishlistDao.deleteWishlist(id)
    suspend fun addWishlistItem(wishlistId: String, bookId: String) = wishlistDao.insertWishlistItem(WishlistItemEntity(wishlistId, bookId))
    suspend fun removeWishlistItem(wishlistId: String, bookId: String) = wishlistDao.deleteWishlistItem(wishlistId, bookId)
    fun getWishlistBooks(wishlistId: String): Flow<List<BookEntity>> = wishlistDao.getWishlistBooks(wishlistId)
    fun isBookInAnyWishlist(bookId: String): Flow<Boolean> = wishlistDao.isBookInAnyWishlist(bookId)

    // --- Review Queries ---
    fun getReviewsForBook(bookId: String): Flow<List<ReviewEntity>> = reviewDao.getReviewsForBook(bookId)
    suspend fun addReview(review: ReviewEntity) = reviewDao.insertReview(review)
    suspend fun toggleHelpfulReview(reviewId: String, commentRatingId: String): Boolean {
        // Simple toggle for review helpful count
        reviewDao.updateHelpfulCount(reviewId, 1, true)
        return true
    }

    // --- User Profile Queries ---
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    suspend fun updateUserProfile(profile: UserProfileEntity) = userProfileDao.insertUserProfile(profile)

    // --- Order Queries ---
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    suspend fun placeOrder(order: OrderEntity) {
        orderDao.insertOrder(order)
        clearCart()
    }

    // --- Database Prepopulation ---
    private suspend fun prepopulateIfNeeded() {
        val count = allBooks.first().size
        if (count == 0) {
            Log.d("BookstoreRepository", "Prepopulating bookstore database...")

            // 1. Insert Authors
            val authors = listOf(
                AuthorEntity("a1", "Matt Haig", "Matt Haig is the number one bestselling author of Reasons to Stay Alive and Notes on a Nervous Planet, along with several award-winning novels."),
                AuthorEntity("a2", "Andy Weir", "Andy Weir built a career as a software engineer until the runaway success of his debut novel, The Martian, allowed him to pursue writing full-time."),
                AuthorEntity("a3", "Alex Michaelides", "Alex Michaelides was born in Cyprus. He has an M.A. in English literature from Trinity College, Cambridge University, and an M.F.A. in screenwriting."),
                AuthorEntity("a4", "James Clear", "James Clear is a writer and speaker focused on habits, decision-making, and continuous improvement. He is the author of the #1 NYT bestseller Atomic Habits."),
                AuthorEntity("a5", "Robert C. Martin", "Robert C. Martin (Uncle Bob) has been a software professional since 1970 and is an international speaker and co-author of the Agile Manifesto."),
                AuthorEntity("a6", "Koyoharu Gotouge", "Koyoharu Gotouge is the creator of the sensationally popular historical fantasy manga series Demon Slayer: Kimetsu no Yaiba.")
            )
            authorDao.insertAuthors(authors)

            // 2. Insert Books
            val books = listOf(
                BookEntity(
                    id = "b1",
                    title = "The Midnight Library",
                    authorId = "a1",
                    authorName = "Matt Haig",
                    description = "Between life and death there is a library, and within that library, the shelves go on forever. Every book provides a chance to try another life you could have lived.",
                    category = "Fiction",
                    publisher = "Viking",
                    isbn = "978-0525559474",
                    publicationDate = "September 29, 2020",
                    pageCount = 304,
                    priceHardback = 26.00,
                    pricePaperback = 16.00,
                    priceEbook = 9.99,
                    priceAudiobook = 14.95,
                    rating = 4.5,
                    stockCount = 12,
                    isBestseller = true,
                    isStaffPick = true,
                    isNewRelease = true,
                    coverImageId = "library"
                ),
                BookEntity(
                    id = "b2",
                    title = "Project Hail Mary",
                    authorId = "a2",
                    authorName = "Andy Weir",
                    description = "Ryland Grace is the sole survivor on a desperate, last-chance mission—and if he fails, humanity and the earth itself will perish. Except that right now, he doesn't know that.",
                    category = "Science Fiction",
                    publisher = "Ballantine Books",
                    isbn = "978-0593135204",
                    publicationDate = "May 4, 2021",
                    pageCount = 496,
                    priceHardback = 28.99,
                    pricePaperback = 18.00,
                    priceEbook = 11.99,
                    priceAudiobook = 20.00,
                    rating = 4.8,
                    stockCount = 5,
                    isBestseller = true,
                    isTrending = true,
                    coverImageId = "hail_mary"
                ),
                BookEntity(
                    id = "b3",
                    title = "The Silent Patient",
                    authorId = "a3",
                    authorName = "Alex Michaelides",
                    description = "Alicia Berenson's life is seemingly perfect. A famous painter married to an in-demand fashion photographer, she lives in a grand house with large windows overlooking a park in one of London's most desirable areas. One evening her husband Gabriel returns home late from a fashion shoot, and Alicia shoots him five times in the face, and then never speaks another word.",
                    category = "Mystery",
                    publisher = "Celadon Books",
                    isbn = "978-1250301697",
                    publicationDate = "February 5, 2019",
                    pageCount = 336,
                    priceHardback = 26.99,
                    pricePaperback = 15.99,
                    priceEbook = 8.99,
                    priceAudiobook = 12.95,
                    rating = 4.4,
                    stockCount = 8,
                    isBestseller = true,
                    isStaffPick = true,
                    coverImageId = "silent_patient"
                ),
                BookEntity(
                    id = "b4",
                    title = "Atomic Habits",
                    authorId = "a4",
                    authorName = "James Clear",
                    description = "No matter your goals, Atomic Habits offers a proven framework for improving—every day. James Clear, one of the world's leading experts on habit formation, reveals practical strategies.",
                    category = "Business",
                    publisher = "Avery",
                    isbn = "978-0735211292",
                    publicationDate = "October 16, 2018",
                    pageCount = 320,
                    priceHardback = 27.00,
                    pricePaperback = 17.00,
                    priceEbook = 12.99,
                    priceAudiobook = 18.00,
                    rating = 4.9,
                    stockCount = 20,
                    isBestseller = true,
                    isTrending = true,
                    coverImageId = "atomic_habits"
                ),
                BookEntity(
                    id = "b5",
                    title = "Clean Code",
                    authorId = "a5",
                    authorName = "Robert C. Martin",
                    description = "Even bad code can function. But if code isn't clean, it can bring a development organization to its knees. Every year, countless hours and significant resources are lost because of poorly written code.",
                    category = "Computing",
                    publisher = "Prentice Hall",
                    isbn = "978-0132350884",
                    publicationDate = "August 1, 2008",
                    pageCount = 464,
                    priceHardback = 54.99,
                    pricePaperback = 39.99,
                    priceEbook = 29.99,
                    priceAudiobook = 0.0,
                    rating = 4.7,
                    stockCount = 4,
                    isStaffPick = true,
                    coverImageId = "clean_code"
                ),
                BookEntity(
                    id = "b6",
                    title = "Demon Slayer: Kimetsu no Yaiba, Vol. 1",
                    authorId = "a6",
                    authorName = "Koyoharu Gotouge",
                    description = "In Taisho-era Japan, Tanjiro Kamado is a kindhearted boy who makes a living selling charcoal. But his peaceful life is shattered when a demon slaughters his entire family.",
                    category = "Manga",
                    publisher = "VIZ Media LLC",
                    isbn = "978-1974700523",
                    publicationDate = "July 3, 2018",
                    pageCount = 192,
                    priceHardback = 14.99,
                    pricePaperback = 9.99,
                    priceEbook = 6.49,
                    priceAudiobook = 0.0,
                    rating = 4.9,
                    stockCount = 15,
                    isTrending = true,
                    coverImageId = "demon_slayer"
                ),
                BookEntity(
                    id = "b7",
                    title = "Dune: Barnes & Noble Exclusive Edition",
                    authorId = "a2", // Set author appropriately
                    authorName = "Frank Herbert",
                    description = "This special Barnes & Noble Exclusive Edition includes a custom design cover, specialized endpapers, and an exclusive poster map. Frank Herbert's masterpiece Dune is the triumph of the imagination.",
                    category = "Science Fiction",
                    publisher = "Chilton Books",
                    isbn = "978-0441172719",
                    publicationDate = "October 1, 1965",
                    pageCount = 604,
                    priceHardback = 35.00,
                    pricePaperback = 18.00,
                    priceEbook = 0.0,
                    priceAudiobook = 0.0,
                    rating = 4.8,
                    stockCount = 10,
                    isExclusive = true,
                    isBestseller = true,
                    coverImageId = "dune_exclusive"
                ),
                BookEntity(
                    id = "b8",
                    title = "The Stars We Seek (Coming Soon)",
                    authorId = "a2",
                    authorName = "Andy Weir",
                    description = "An incredible upcoming sci-fi novel detailing the colonization of alpha centauri, filled with tense orbital physics and delightful humor. Pre-order your copy today!",
                    category = "Science Fiction",
                    publisher = "Ballantine Books",
                    isbn = "978-0593135299",
                    publicationDate = "November 10, 2026",
                    pageCount = 420,
                    priceHardback = 30.00,
                    pricePaperback = 19.99,
                    priceEbook = 14.99,
                    priceAudiobook = 22.00,
                    rating = 0.0,
                    stockCount = 0,
                    isComingSoon = true,
                    coverImageId = "coming_soon"
                )
            )
            bookDao.insertBooks(books)

            // 3. Insert Stores
            val stores = listOf(
                StoreEntity("s1", "Barnes & Noble Fifth Avenue (Flagship)", "555 Fifth Ave, New York, NY 10017", "(212) 697-3048", "9:00 AM - 9:00 PM", true, 40.7554, -73.9796),
                StoreEntity("s2", "Barnes & Noble The Grove", "189 The Grove Dr, Los Angeles, CA 90036", "(323) 525-0270", "10:00 AM - 10:00 PM", true, 34.0722, -118.3585),
                StoreEntity("s3", "Barnes & Noble Chicago Loop", "1 N State St, Chicago, IL 60602", "(312) 281-9067", "9:00 AM - 8:00 PM", true, 41.8819, -87.6278),
                StoreEntity("s4", "Barnes & Noble Seattle Downtown", "600 Pine St, Seattle, WA 98101", "(206) 264-0156", "10:00 AM - 8:00 PM", false, 47.6122, -122.3353)
            )
            storeDao.insertStores(stores)

            // 4. Insert Events
            val events = listOf(
                EventEntity("e1", "Andy Weir: 'Project Hail Mary' Special Q&A & Signing", "Author Signing", "July 12, 2026", "6:00 PM", "Meet bestselling author Andy Weir! He will be discussing his hit novel, sharing secrets of sci-fi research, and signing books.", "s1", "B&N Fifth Avenue (Flagship)", 42, false, false),
                EventEntity("e2", "Saturday Morning Children's Storytime & Crafts", "Storytime", "July 04, 2026", "10:30 AM", "Bring the little ones for an engaging reading of classic and new picture books, followed by a light coloring craft.", "s2", "B&N The Grove", 15, false, false),
                EventEntity("e3", "Manga & Anime Readers Discussion Club", "Book Club", "July 18, 2026", "7:00 PM", "Let's gather to discuss our favorite manga releases of the season, trade recommendations, and enjoy café snacks.", "s3", "B&N Chicago Loop", 8, false, false),
                EventEntity("e4", "Creative Writing Intensive Workshop", "Workshop", "July 25, 2026", "2:00 PM", "An interactive workshop focusing on worldbuilding, pacing, and developing compelling protagonists. Pre-registration ticket is required.", "s1", "B&N Fifth Avenue (Flagship)", 19, false, true, 15.00)
            )
            eventDao.insertEvents(events)

            // 5. Insert Cafe Items
            val cafeItems = listOf(
                CafeItemEntity("c1", "Signature House Blend Coffee", "Hot Drinks", 3.25, 5, "Our classic, full-bodied medium roast blend. Perfectly balanced flavor with hints of cocoa.", true, "coffee"),
                CafeItemEntity("c2", "Vanilla Caramel Latte", "Hot Drinks", 4.95, 310, "Rich espresso paired with steamed whole milk, sweet vanilla syrup, and a decadent caramel drizzle.", true, "latte"),
                CafeItemEntity("c3", "Classic Ceremonial Matcha Latte", "Cold Drinks", 5.25, 220, "Stone-ground Japanese matcha sweetened and blended with creamy oat milk over ice.", true, "matcha"),
                CafeItemEntity("c4", "Double Chocolate Chip Cookie", "Bakery", 3.45, 390, "Thick, bakery-style cookie packed with semi-sweet and milk chocolate chips. Served warm.", true, "cookie"),
                CafeItemEntity("c5", "Wild Blueberry Crumble Muffin", "Bakery", 3.75, 410, "Fluffy golden muffin bursting with real wild blueberries and topped with a sweet brown sugar streusel.", true, "muffin"),
                CafeItemEntity("c6", "Tuscan Caprese Panini", "Sandwiches", 8.95, 490, "Fresh mozzarella, vine-ripened tomatoes, sweet basil pesto, and balsamic glaze toasted on sourdough.", true, "panini"),
                CafeItemEntity("c7", "Classic Turkey Club Sandwich", "Sandwiches", 9.45, 520, "Sliced roasted turkey breast, crispy smoked bacon, provolone, lettuce, tomato, and herb mayo.", true, "turkey")
            )
            cafeItemDao.insertCafeItems(cafeItems)

            // 6. Insert User Profile
            val defaultProfile = UserProfileEntity()
            userProfileDao.insertUserProfile(defaultProfile)

            // 7. Insert Default Wishlist
            val defaultWishlist = WishlistEntity("w1", "My Reading Wishlist", true)
            wishlistDao.insertWishlist(defaultWishlist)
            wishlistDao.insertWishlistItem(WishlistItemEntity("w1", "b2")) // Hail Mary in wishlist
            wishlistDao.insertWishlistItem(WishlistItemEntity("w1", "b5")) // Clean Code in wishlist

            // 8. Insert Some Reviews
            val reviews = listOf(
                ReviewEntity("r1", "b1", "ReadingEnthusiast", 5, "An absolute masterpiece. Beautifully written and deeply moving. It really makes you appreciate the life you have and the tiny decisions that shape it.", "October 12, 2025", 28),
                ReviewEntity("r2", "b1", "BookWorm42", 4, "A great premise with delightful execution. Found myself teary-eyed in some spots. Matt Haig never disappoints.", "November 2, 2025", 12),
                ReviewEntity("r3", "b2", "ScienceNerd", 5, "Best sci-fi I've read in years! Ryland Grace and Rocky have the best dynamic. Andy Weir outdid himself, even better than The Martian!", "December 20, 2025", 85)
            )
            for (rev in reviews) {
                reviewDao.insertReview(rev)
            }

            // 9. Put some initial library books
            libraryBookDao.insertLibraryBook(LibraryBookEntity("b1", "The Midnight Library", "Matt Haig", "library", "READING", 42))
            libraryBookDao.insertLibraryBook(LibraryBookEntity("b4", "Atomic Habits", "James Clear", "atomic_habits", "FINISHED", 100))

            Log.d("BookstoreRepository", "Prepopulation completed!")
        }
    }
}
