package vcmsa.projects.coinverse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vcmsa.projects.coinverse.db.AppDatabase
import vcmsa.projects.coinverse.db.CategoryDao
import java.util.Date

class CreateBudgetActivity : AppCompatActivity() {
    // UI Element Variables
    private lateinit var spinnerCategoryBudget: Spinner
    private lateinit var tvCategoryLabelBudget: TextView
    private lateinit var spinnerPeriodBudget: Spinner
    private lateinit var tvPeriodLabelBudget: TextView
    private lateinit var etAmountBudget: EditText
    private lateinit var etNotesBudget: EditText
    private lateinit var btnCancelBudget: Button
    private lateinit var btnConfirmBudget: Button
    private lateinit var ivHome: ImageButton
    private lateinit var ivInsights: ImageButton
    private lateinit var ivAdd: ImageButton
    private lateinit var ivGoals: ImageButton
    private lateinit var ivProfile: ImageButton

    // Firebase
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Room Database DAO
    private lateinit var categoryDao: CategoryDao

    // Category Spinner Data
    private val categoryNamesList = mutableListOf<String>()
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private val ADD_NEW_CATEGORY = "Add New Category"
    private lateinit var HINT_SELECT_CATEGORY: String

    // Period Spinner Data
    private val periodList = mutableListOf<String>()
    private lateinit var periodAdapter: ArrayAdapter<String>
    private lateinit var HINT_SELECT_PERIOD: String
    private var selectedPeriod: String? = null // To store selected period

    // addCategoryLauncher
    private val addCategoryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Refreshing categories...", Toast.LENGTH_SHORT).show()
            loadCategoriesFromDb() // Reload categories
        }
        // Reset category selection display
        spinnerCategoryBudget.setSelection(0)
        tvCategoryLabelBudget.text = HINT_SELECT_CATEGORY
        tvCategoryLabelBudget.setTextColor(ContextCompat.getColor(this, R.color.text_hint_color))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_createbudget)

        navigationBar()

        // Find Views By ID
        spinnerCategoryBudget = findViewById(R.id.spinnerCategoryBudget)
        tvCategoryLabelBudget = findViewById(R.id.tvCategoryLabelBudget)
        spinnerPeriodBudget = findViewById(R.id.spinnerPeriodBudget)
        tvPeriodLabelBudget = findViewById(R.id.tvPeriodLabelBudget)
        etAmountBudget = findViewById(R.id.etAmountBudget)
        etNotesBudget = findViewById(R.id.etNotesBudget)
        btnCancelBudget = findViewById(R.id.btnCancelBudget)
        btnConfirmBudget = findViewById(R.id.btnConfirmBudget)
        ivHome = findViewById(R.id.ivHome)
        ivInsights = findViewById(R.id.ivInsights)
        ivAdd = findViewById(R.id.ivAdd)
        ivGoals = findViewById(R.id.ivGoals)
        ivProfile = findViewById(R.id.ivProfile)

        // Get Hint Strings
        HINT_SELECT_CATEGORY = getString(R.string.hint_select_category)
        HINT_SELECT_PERIOD = getString(R.string.hint_budget_period)

        // Initialize Room DAO
        categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Setup UI components
        setupCategorySpinner()
        loadCategoriesFromDb()
        setupPeriodSpinner()
        setupButtonClickListeners()
    }

    // Category Spinner Setup
    private fun setupCategorySpinner() {
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNamesList)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoryBudget.adapter = categoryAdapter

        tvCategoryLabelBudget.text = HINT_SELECT_CATEGORY
        tvCategoryLabelBudget.setTextColor(ContextCompat.getColor(this, R.color.text_hint_color))

        spinnerCategoryBudget.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = parent.getItemAtPosition(position).toString()

                if (selectedCategory == ADD_NEW_CATEGORY) {
                    val intent = Intent(this@CreateBudgetActivity, AddCategoryActivity::class.java)
                    addCategoryLauncher.launch(intent)
                } else if (position == 0 && categoryNamesList.isNotEmpty() && categoryNamesList[0] == HINT_SELECT_CATEGORY) {
                    tvCategoryLabelBudget.text = HINT_SELECT_CATEGORY
                    tvCategoryLabelBudget.setTextColor(ContextCompat.getColor(this@CreateBudgetActivity, R.color.text_hint_color))
                } else {
                    tvCategoryLabelBudget.text = selectedCategory
                    tvCategoryLabelBudget.setTextColor(ContextCompat.getColor(this@CreateBudgetActivity, R.color.text_primary_color))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                tvCategoryLabelBudget.text = HINT_SELECT_CATEGORY
                tvCategoryLabelBudget.setTextColor(ContextCompat.getColor(this@CreateBudgetActivity, R.color.text_hint_color))
            }
        }
        tvCategoryLabelBudget.setOnClickListener { spinnerCategoryBudget.performClick() }
    }

    // Load Categories from Room DB
    private fun loadCategoriesFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val fetchedNames = try {
                categoryDao.getAllCategoryNames()
            } catch (e: Exception) {
                Log.e("CreateBudget", "Error fetching categories", e)
                listOf()
            }
            withContext(Dispatchers.Main) {
                val previouslySelectedCategory = if (spinnerCategoryBudget.selectedItemPosition > 0 &&
                    spinnerCategoryBudget.selectedItemPosition < categoryNamesList.size -1 &&
                    categoryNamesList.isNotEmpty() &&
                    categoryNamesList[spinnerCategoryBudget.selectedItemPosition] != ADD_NEW_CATEGORY) {
                    categoryNamesList[spinnerCategoryBudget.selectedItemPosition]
                } else { null }

                categoryNamesList.clear()
                categoryNamesList.add(HINT_SELECT_CATEGORY)
                categoryNamesList.addAll(fetchedNames)
                categoryNamesList.add(ADD_NEW_CATEGORY)
                categoryAdapter.notifyDataSetChanged()

                val restoredPosition = categoryNamesList.indexOf(previouslySelectedCategory).takeIf { it != -1 } ?: 0
                spinnerCategoryBudget.setSelection(restoredPosition, false)

                if (restoredPosition == 0 || categoryNamesList[restoredPosition] == HINT_SELECT_CATEGORY) {
                    tvCategoryLabelBudget.text = HINT_SELECT_CATEGORY
                    tvCategoryLabelBudget.setTextColor(ContextCompat.getColor(this@CreateBudgetActivity, R.color.text_hint_color))
                } else if (categoryNamesList[restoredPosition] != ADD_NEW_CATEGORY) {
                    tvCategoryLabelBudget.text = categoryNamesList[restoredPosition]
                    tvCategoryLabelBudget.setTextColor(ContextCompat.getColor(this@CreateBudgetActivity, R.color.text_primary_color))
                }
            }
        }
    }

    // Budget Period Spinner Setup
    private fun setupPeriodSpinner() {
        periodList.clear()
        periodList.add(HINT_SELECT_PERIOD)
        periodList.add("January")
        periodList.add("February")
        periodList.add("March")
        periodList.add("April")
        periodList.add("May")
        periodList.add("June")
        periodList.add("July")
        periodList.add("August")
        periodList.add("September")
        periodList.add("October")
        periodList.add("November")
        periodList.add("December")


        periodAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periodList)
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPeriodBudget.adapter = periodAdapter

        tvPeriodLabelBudget.text = HINT_SELECT_PERIOD
        tvPeriodLabelBudget.setTextColor(ContextCompat.getColor(this, R.color.text_hint_color))

        spinnerPeriodBudget.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    selectedPeriod = null
                    tvPeriodLabelBudget.text = HINT_SELECT_PERIOD
                    tvPeriodLabelBudget.setTextColor(ContextCompat.getColor(this@CreateBudgetActivity, R.color.text_hint_color))
                } else {
                    selectedPeriod = parent.getItemAtPosition(position).toString()
                    tvPeriodLabelBudget.text = selectedPeriod
                    tvPeriodLabelBudget.setTextColor(ContextCompat.getColor(this@CreateBudgetActivity, R.color.text_primary_color))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedPeriod = null
                tvPeriodLabelBudget.text = HINT_SELECT_PERIOD
                tvPeriodLabelBudget.setTextColor(ContextCompat.getColor(this@CreateBudgetActivity, R.color.text_hint_color))
            }
        }
        tvPeriodLabelBudget.setOnClickListener { spinnerPeriodBudget.performClick() }
    }


    // Button Click Listeners Setup
    private fun setupButtonClickListeners() {
        btnCancelBudget.setOnClickListener {
            navigateToBudgetGoals()
        }

        btnConfirmBudget.setOnClickListener {
            handleConfirmClick() // Validate and save
        }
    }

    // Handle Confirm Click Logic
    private fun handleConfirmClick() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get Selected Category
        val selectedCategoryIndex = spinnerCategoryBudget.selectedItemPosition
        val selectedCategoryValue = spinnerCategoryBudget.selectedItem?.toString()
        var category: String? = null
        if (selectedCategoryIndex > 0 && selectedCategoryValue != ADD_NEW_CATEGORY && selectedCategoryValue != HINT_SELECT_CATEGORY) {
            category = selectedCategoryValue
        }

        // Get Selected Period
        val period = selectedPeriod

        // Get Amount and Notes
        val amountStr = etAmountBudget.text.toString().trim()
        val notes = etNotesBudget.text.toString().trim()

        // Validation
        var isValid = true

        // Validate Category
        if (category == null) {
            Toast.makeText(this, "Please select a budget category", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validate Period
        if (period == null) {
            Toast.makeText(this, "Please select a budget period", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validate Amount
        var amountValue: Double? = null
        if (amountStr.isEmpty()) {
            etAmountBudget.error = "Required"
            isValid = false
        } else {
            amountValue = amountStr.toDoubleOrNull()
            if (amountValue == null || amountValue <= 0) {
                etAmountBudget.error = "Invalid positive amount"
                isValid = false
            } else {
                etAmountBudget.error = null // Clear error if valid
            }
        }

        if (!isValid) {
            Toast.makeText(this, "Please fix the errors.", Toast.LENGTH_LONG).show()
            return
        }

        // Proceed if Valid
        btnConfirmBudget.isEnabled = false
        btnCancelBudget.isEnabled = false


        // Create the Budget object to save
        val budgetToSave = Budget(
            userId = userId,
            category = category!!,
            period = period!!,
            amount = amountValue!!,
            notes = notes.takeIf { it.isNotEmpty() },
            creationDate = Date()
        )

        // Save to Firestore
        saveBudgetToFirestore(budgetToSave)
    }

    // Save Budget Data to Firestore
    private fun saveBudgetToFirestore(budget: Budget) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.collection("Budget")
                    .add(budget)
                    .await()

                Log.d("CreateBudget", "Budget saved successfully to Firestore.")
                withContext(Dispatchers.Main) {
                    handleSaveSuccess()
                }
            } catch (e: Exception) {
                Log.e("CreateBudget", "Error saving budget to Firestore", e)
                withContext(Dispatchers.Main) {
                    handleSaveError("Failed to save budget: ${e.localizedMessage}")
                }
            }
        }
    }

    // Handle Save Success
    private fun handleSaveSuccess() {
        Toast.makeText(this, "Budget created successfully!", Toast.LENGTH_SHORT).show()
        navigateToBudgetGoals()
    }

    // Handle Save Error
    private fun handleSaveError(errorMessage: String) {
        btnConfirmBudget.isEnabled = true
        btnCancelBudget.isEnabled = true
        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
    }

    // Navigation Helper
    private fun navigateToBudgetGoals() {
        val intent = Intent(this, BudgetGoals::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
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