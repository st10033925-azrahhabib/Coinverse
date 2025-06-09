package vcmsa.projects.coinverse

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.*

class CategoryDetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListExpensesAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var currentMonth: Int = -1
    private var categoryName: String? = null
    private val expenseList = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)

        // Back button
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Get category and month from intent
        categoryName = intent.getStringExtra("category")
        currentMonth = intent.getIntExtra("month", -1)

        // Set title
        val textView = findViewById<TextView>(R.id.textCategoryName)
        textView.text = "$categoryName"

        firestore = Firebase.firestore
        auth = Firebase.auth

        // Setup RecyclerView
        recyclerView = findViewById(R.id.category_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListExpensesAdapter(expenseList) { expense ->
            showExpenseDialog(expense)
        }
        recyclerView.adapter = adapter

        if (currentMonth >= 0 && categoryName != null) {
            fetchCategoryExpenses()
        }

        navigationBar()
    }

    private fun fetchCategoryExpenses() {
        val userID = auth.currentUser?.uid
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time

        if (userID != null) {
            firestore.collection("Expenses")
                .whereEqualTo("userId", userID)
                .whereEqualTo("category", categoryName)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val fetched = querySnapshot.toObjects(Expense::class.java)
                    expenseList.clear()
                    expenseList.addAll(fetched)
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("CategoryDetailsDebug", "Error fetching expenses: ${e.message}")
                }
        }
    }

    private fun showExpenseDialog(expense: Expense) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_expense_details, null)

        val amountText = dialogView.findViewById<TextView>(R.id.tvDialogAmount)
        val dateText = dialogView.findViewById<TextView>(R.id.tvDialogDate)
        val notesText = dialogView.findViewById<TextView>(R.id.tvDialogNotes)

        amountText.text = String.format("R %.2f", expense.amount)
        dateText.text = expense.date?.toString() ?: "N/A"
        notesText.text = expense.notes ?: "None"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Make dialog background transparent to show rounded corners
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()

        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        btnOk.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun navigationBar() {
        val homeLink = findViewById<ImageButton>(R.id.ivHome)
        val insightsLink = findViewById<ImageButton>(R.id.ivInsights)
        val addLink = findViewById<ImageButton>(R.id.ivAdd)
        val goalsLink = findViewById<ImageButton>(R.id.ivGoals)
        val profileLink = findViewById<ImageButton>(R.id.ivProfile)

        homeLink.setOnClickListener {
            startActivity(Intent(this, ExpensesActivity::class.java))
        }

        insightsLink.setOnClickListener {
            startActivity(Intent(this, Insights::class.java))
        }

        addLink.setOnClickListener {
            startActivity(Intent(this, LogExpense::class.java))
        }

        goalsLink.setOnClickListener {
            startActivity(Intent(this, BudgetGoals::class.java))
        }

        profileLink.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }
    }
}
