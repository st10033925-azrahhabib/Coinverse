package vcmsa.projects.coinverse

import java.util.Date

data class Expense(
    val userId: String = "", // ID of the user this expense belongs to
    val name: String = "",
    val category: String = "",
    val vendor: String? = null,
    val amount: Double = 0.0,
    val date: Date? = null, // Stores the specific date as a Timestamp in Firestore
    val notes: String? = null
) {
    constructor() : this("", "", "", null, 0.0, null, null)
}