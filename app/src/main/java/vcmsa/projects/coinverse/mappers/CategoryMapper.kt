package vcmsa.projects.coinverse.mappers

import vcmsa.projects.coinverse.R // Import your app's resource class
import vcmsa.projects.coinverse.db.Category
import vcmsa.projects.coinverse.firebase.FirestoreCategory

fun Category.toFirestoreCategory(userId: String): FirestoreCategory {
    val iconStringIdentifier = when (this.iconResId) {
        // icon list
        R.drawable.ic_book -> "book"
        R.drawable.ic_shopping -> "shopping"
        R.drawable.ic_movie -> "movie"
        R.drawable.ic_coffee -> "coffee"
        R.drawable.ic_engagement -> "engagement"
        R.drawable.ic_transport -> "transport"
        R.drawable.ic_laptop -> "laptop"
        R.drawable.ic_savings -> "savings"
        R.drawable.ic_dog -> "dog"
        R.drawable.ic_cat -> "cat"
        R.drawable.ic_guitar -> "guitar"
        R.drawable.ic_health -> "health"
        R.drawable.ic_entertainment -> "entertainment"
        R.drawable.ic_smoking -> "smoking"
        R.drawable.ic_tea -> "tea"
        R.drawable.ic_music -> "music"
        R.drawable.ic_sports -> "sports"

        else -> "default_icon"
    }

    // Return the newly created Firestore-ready object.
    return FirestoreCategory(
        name = this.name,
        userId = userId,
        iconIdentifier = iconStringIdentifier
    )
}