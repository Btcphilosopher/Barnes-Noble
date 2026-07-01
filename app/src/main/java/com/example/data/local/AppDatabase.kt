package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        BookEntity::class,
        AuthorEntity::class,
        StoreEntity::class,
        EventEntity::class,
        CafeItemEntity::class,
        CartItemEntity::class,
        LibraryBookEntity::class,
        WishlistEntity::class,
        WishlistItemEntity::class,
        ReviewEntity::class,
        UserProfileEntity::class,
        OrderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun authorDao(): AuthorDao
    abstract fun storeDao(): StoreDao
    abstract fun eventDao(): EventDao
    abstract fun cafeItemDao(): CafeItemDao
    abstract fun cartDao(): CartDao
    abstract fun libraryBookDao(): LibraryBookDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun reviewDao(): ReviewDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "barnes_noble_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
