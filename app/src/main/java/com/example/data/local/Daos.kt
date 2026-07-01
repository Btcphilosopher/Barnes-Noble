package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: String): BookEntity?

    @Query("SELECT * FROM books WHERE category = :category")
    fun getBooksByCategory(category: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isExclusive = 1")
    fun getExclusiveBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isBestseller = 1")
    fun getBestsellers(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isTrending = 1")
    fun getTrendingBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isStaffPick = 1")
    fun getStaffPicks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isComingSoon = 1")
    fun getComingSoonBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isNewRelease = 1")
    fun getNewReleases(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR authorName LIKE '%' || :query || '%' OR isbn LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)
}

@Dao
interface AuthorDao {
    @Query("SELECT * FROM authors WHERE id = :id")
    suspend fun getAuthorById(id: String): AuthorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthors(authors: List<AuthorEntity>)
}

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores")
    fun getAllStores(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE id = :id")
    suspend fun getStoreById(id: String): StoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<StoreEntity>)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: String): EventEntity?

    @Query("UPDATE events SET isRegistered = :isRegistered, registrationCount = registrationCount + :countDiff WHERE id = :id")
    suspend fun setEventRegistration(id: String, isRegistered: Boolean, countDiff: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
}

@Dao
interface CafeItemDao {
    @Query("SELECT * FROM cafe_items")
    fun getAllCafeItems(): Flow<List<CafeItemEntity>>

    @Query("SELECT * FROM cafe_items WHERE category = :category")
    fun getCafeItemsByCategory(category: String): Flow<List<CafeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCafeItems(items: List<CafeItemEntity>)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface LibraryBookDao {
    @Query("SELECT * FROM library_books ORDER BY lastUpdated DESC")
    fun getLibraryBooks(): Flow<List<LibraryBookEntity>>

    @Query("SELECT * FROM library_books WHERE bookId = :bookId")
    suspend fun getLibraryBookById(bookId: String): LibraryBookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibraryBook(book: LibraryBookEntity)

    @Query("DELETE FROM library_books WHERE bookId = :bookId")
    suspend fun removeLibraryBook(bookId: String)
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlists")
    fun getWishlists(): Flow<List<WishlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(wishlist: WishlistEntity)

    @Query("DELETE FROM wishlists WHERE id = :id")
    suspend fun deleteWishlist(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE wishlistId = :wishlistId AND bookId = :bookId")
    suspend fun deleteWishlistItem(wishlistId: String, bookId: String)

    @Query("SELECT b.* FROM books b INNER JOIN wishlist_items wi ON b.id = wi.bookId WHERE wi.wishlistId = :wishlistId")
    fun getWishlistBooks(wishlistId: String): Flow<List<BookEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE bookId = :bookId)")
    fun isBookInAnyWishlist(bookId: String): Flow<Boolean>
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE bookId = :bookId ORDER BY date DESC")
    fun getReviewsForBook(bookId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("UPDATE reviews SET helpfulCount = helpfulCount + :diff, isHelpfulMarked = :marked WHERE id = :id")
    suspend fun updateHelpfulCount(id: String, diff: Int, marked: Boolean)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 'current_user'")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
}
