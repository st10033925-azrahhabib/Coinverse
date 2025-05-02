package vcmsa.projects.coinverse

import java.util.Date

// Data class representing a budget collection in Firestore
data class Budget(
    var userId: String = "",           // ID of the user who created the budget
    var category: String = "",         // The selected budget category
    var period: String = "",           // The budget period
    var amount: Double = 0.0,        // The budgeted amount
    var notes: String? = null,         // Optional notes
    var creationDate: Date? = null     // Timestamp when the budget was created
) {
    constructor() : this("", "", "", 0.0, null, null)
}