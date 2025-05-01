package vcmsa.projects.coinverse.db

import androidx.room.*

@Dao
interface ReceiptImageDao {

    // Insert a receipt image. If one exists with the same ID, replace it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receiptImage: ReceiptImage)

    // Get a receipt image based on the Firestore document ID it's linked to.
    @Query("SELECT * FROM receipt_images WHERE firestoreDocumentId = :docId LIMIT 1")
    suspend fun getReceiptImageByFirestoreId(docId: String): ReceiptImage?

    // Delete a receipt image using its linked Firestore document ID.
    @Query("DELETE FROM receipt_images WHERE firestoreDocumentId = :docId")
    suspend fun deleteReceiptImageByFirestoreId(docId: String)
}