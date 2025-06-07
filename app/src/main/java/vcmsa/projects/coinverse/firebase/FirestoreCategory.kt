package vcmsa.projects.coinverse.firebase

data class FirestoreCategory(
    val name: String = "",
    val userId: String = "",          // Tracks who owns this category.
    val iconIdentifier: String = "" // A stable String name for the icon.
)
