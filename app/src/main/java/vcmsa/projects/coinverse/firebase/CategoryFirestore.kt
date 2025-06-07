package vcmsa.projects.coinverse.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryFirestore {

    private val categoryCollection = FirebaseFirestore.getInstance().collection("categories")

    /**
     * Saves a list of categories to Firestore using a batch write.
     * This is helps for syncing multiple items at once.
     */
    suspend fun syncCategories(categories: List<FirestoreCategory>) {
        val batch = FirebaseFirestore.getInstance().batch()
        for (category in categories) {

            val documentId = "${category.userId}_${category.name}"

            val docRef = categoryCollection.document(documentId)
            batch.set(docRef, category)
        }
        // Commit all the operations as a single transaction.
        batch.commit().await()
    }
}