package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authorId: String,
    val authorName: String,
    val description: String,
    val category: String,
    val publisher: String,
    val isbn: String,
    val publicationDate: String,
    val pageCount: Int,
    val priceHardback: Double,
    val pricePaperback: Double,
    val priceEbook: Double,
    val priceAudiobook: Double,
    val rating: Double,
    val stockCount: Int,
    val isExclusive: Boolean = false,
    val isBestseller: Boolean = false,
    val isTrending: Boolean = false,
    val isStaffPick: Boolean = false,
    val isComingSoon: Boolean = false,
    val isNewRelease: Boolean = false,
    val coverImageId: String = "default_book"
)

@Entity(tableName = "authors")
data class AuthorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val biography: String,
    val imageUrl: String = ""
)

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val hours: String,
    val hasCafe: Boolean = true,
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // "Author Signing", "Book Club", "Storytime", "Workshop"
    val date: String,
    val time: String,
    val description: String,
    val storeId: String,
    val storeName: String,
    val registrationCount: Int = 0,
    val isRegistered: Boolean = false,
    val isTicketed: Boolean = false,
    val ticketPrice: Double = 0.0
)

@Entity(tableName = "cafe_items")
data class CafeItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // "Hot Drinks", "Cold Drinks", "Bakery", "Sandwiches"
    val price: Double,
    val calories: Int,
    val description: String,
    val isAvailable: Boolean = true,
    val imageUrl: String = ""
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemType: String, // "BOOK" or "CAFE"
    val itemId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val selectedFormat: String = "Paperback", // For books
    val extraNotes: String = "" // For café drinks
)

@Entity(tableName = "library_books")
data class LibraryBookEntity(
    @PrimaryKey val bookId: String,
    val title: String,
    val authorName: String,
    val coverImageId: String,
    val readingStatus: String, // "WANT_TO_READ", "READING", "FINISHED"
    val progressPercent: Int = 0,
    val personalNotes: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlists")
data class WishlistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "wishlist_items", primaryKeys = ["wishlistId", "bookId"])
data class WishlistItemEntity(
    val wishlistId: String,
    val bookId: String
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val helpfulCount: Int = 0,
    val isHelpfulMarked: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "current_user",
    val name: String = "Jane Doe",
    val email: String = "jane.doe@example.com",
    val isMember: Boolean = true,
    val membershipCardNumber: String = "BN-982-147-302",
    val rewardPoints: Int = 450,
    val savedAddress: String = "123 Main Street, New York, NY 10001",
    val savedPaymentMethod: String = "Visa ending in 4321",
    val balance: Double = 25.0
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val date: String,
    val totalAmount: Double,
    val status: String,
    val summary: String
)
