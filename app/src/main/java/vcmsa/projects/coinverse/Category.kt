package vcmsa.projects.coinverse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["category_name"], unique = true)] // Ensure category names are unique
)

data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-incrementing primary key

    @ColumnInfo(name = "category_name")
    val name: String,

    @ColumnInfo(name = "icon_res_id")
    val iconResId: Int // Store the drawable resource ID (e.g., R.drawable.ic_book)
)
