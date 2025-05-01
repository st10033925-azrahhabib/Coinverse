package vcmsa.projects.coinverse.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "receipt_images") // Define the table name
data class ReceiptImage(
    @PrimaryKey // This ID will link the image to the Firestore document
    val firestoreDocumentId: String,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) // Store the image as a Binary Large OBject
    val image: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReceiptImage

        if (firestoreDocumentId != other.firestoreDocumentId) return false
        if (!image.contentEquals(other.image)) return false // Use contentEquals for arrays

        return true
    }

    override fun hashCode(): Int {
        var result = firestoreDocumentId.hashCode()
        result = 31 * result + image.contentHashCode() // Use contentHashCode for arrays
        return result
    }
}