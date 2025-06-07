package vcmsa.projects.coinverse
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class BudgetGoals : AppCompatActivity() {

    private lateinit var addBudget: ImageView
    private lateinit var totalTV: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var budgetRecyclerView: RecyclerView
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_goals)

        auth = Firebase.auth
        firestore = Firebase.firestore
        totalTV = findViewById(R.id.budgetTotal)

        //configures rv
        budgetRecyclerView = findViewById(R.id.recyclerBudgetCategories)
        budgetRecyclerView.layoutManager = LinearLayoutManager(this)

        //starts create budget page
        addBudget = findViewById(R.id.ivAddGoal)

        addBudget.setOnClickListener {
            val intent = Intent(this, CreateBudgetActivity::class.java)
            startActivity(intent)
        }

        fetchBudgets()
        navigationBar()
    }


    //Gets and displays a list of budgets per category
    private fun fetchBudgets() {
        val userID = auth.currentUser?.uid

        if (userID != null) {
            firestore.collection("Budget")
                .whereEqualTo("userId", userID)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val categoryTotals = mutableMapOf<String, Double>()
                    var totalBudget = 0.0

                    for (doc in querySnapshot) {
                        val category = doc.getString("category")
                        val amount = doc.getDouble("amount") ?: 0.0

                        if (category != null) {
                            categoryTotals[category] = (categoryTotals[category] ?: 0.0) + amount
                        }
                        totalBudget += amount
                    }
                    totalTV.text = "R ${String.format("%.2f", totalBudget)}"

                    val categoryTotalList = categoryTotals.map { (category, total) ->
                        BudgetCategoryTotal(category, total)
                    }.toList()

                    //update the recyclerView
                    budgetAdapter = BudgetAdapter(categoryTotalList)
                    budgetRecyclerView.adapter = budgetAdapter
                }
                .addOnFailureListener {e ->
                    Log.e("LogFailure","Error fetching budgets.")
                }
        }
    }

    //Nav Bar
    private fun navigationBar() {
        val homeLink = findViewById<ImageButton>(R.id.ivHome)
        val insightsLink = findViewById<ImageButton>(R.id.ivInsights)
        val addLink = findViewById<ImageButton>(R.id.ivAdd)
        val goalsLink = findViewById<ImageButton>(R.id.ivGoals)
        val profileLink = findViewById<ImageButton>(R.id.ivProfile)

        homeLink.setOnClickListener {
            val intent = Intent(this, ExpensesActivity::class.java)
            startActivity(intent)
        }

        insightsLink.setOnClickListener {
            val intent = Intent(this, Insights::class.java)
            startActivity(intent)
        }

        addLink.setOnClickListener {
            val intent = Intent(this, LogExpense::class.java)
            startActivity(intent)
        }

        goalsLink.setOnClickListener {
            val intent = Intent(this, BudgetGoals::class.java)
            startActivity(intent)
        }

        profileLink.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }
}

