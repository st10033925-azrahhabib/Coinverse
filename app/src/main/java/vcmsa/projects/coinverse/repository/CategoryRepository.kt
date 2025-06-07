package vcmsa.projects.coinverse.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vcmsa.projects.coinverse.db.CategoryDao
import vcmsa.projects.coinverse.firebase.CategoryFirestore
import vcmsa.projects.coinverse.mappers.toFirestoreCategory


 //The Repository acts as the single source of truth and mediator for data operations.
class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val firestore: CategoryFirestore
) {

     //This is the main function to sync all local data to the cloud.
    suspend fun syncAllLocalCategoriesToFirestore() {
        withContext(Dispatchers.IO) {
            // Get the current user's ID
            val userId = Firebase.auth.currentUser?.uid ?: return@withContext

            // Get all categories from the local Room database using CategoryDAO.
            val localCategories = categoryDao.getAllCategories()

            // Use the mapper to convert the list of local Categories into a list of Firestore-ready FirestoreCategory objects.
            val firestoreCategories = localCategories.map { localCategory ->
                localCategory.toFirestoreCategory(userId)
            }

            // If the list is not empty, save the converted data to Firestore.
            if (firestoreCategories.isNotEmpty()) {
                firestore.syncCategories(firestoreCategories)
            }
        }
    }
}