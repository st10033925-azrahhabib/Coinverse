package vcmsa.projects.coinverse.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long // Use suspend for coroutines

    // Get all categories, ordered alphabetically by name
    @Query("SELECT * FROM categories ORDER BY category_name ASC")
    suspend fun getAllCategories(): List<Category> // Use suspend for coroutines

    // Get only category names
    @Query("SELECT category_name FROM categories ORDER BY category_name ASC")
    suspend fun getAllCategoryNames(): List<String> // Use suspend for coroutines
}