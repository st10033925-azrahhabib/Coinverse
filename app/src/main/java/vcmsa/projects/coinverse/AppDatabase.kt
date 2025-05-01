package vcmsa.projects.coinverse.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Category::class, ReceiptImage::class], version = 2, exportSchema = false) // Add Category entity
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao // Abstract function to get the DAO
    abstract fun receiptImageDao(): ReceiptImageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coinverse_database" // Name of the database file
                )
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
