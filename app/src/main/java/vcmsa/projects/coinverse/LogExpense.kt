package vcmsa.projects.coinverse

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vcmsa.projects.coinverse.db.AppDatabase
import vcmsa.projects.coinverse.db.CategoryDao
import vcmsa.projects.coinverse.db.ReceiptImage
import vcmsa.projects.coinverse.db.ReceiptImageDao
import java.io.IOException
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LogExpense : AppCompatActivity() {

    //UI Element Variables
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvCategoryLabel: TextView
    private lateinit var etExpenseName: EditText
    private lateinit var etVendorName: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnAttachReceipt: Button
    private lateinit var btnCancel: Button
    private lateinit var btnConfirm: Button
    private lateinit var ivHome: ImageButton
    private lateinit var ivInsights: ImageButton
    private lateinit var ivAdd: ImageButton
    private lateinit var ivGoals: ImageButton
    private lateinit var ivProfile: ImageButton


    // Firebase
    private lateinit var db: FirebaseFirestore // Firestore instance
    private lateinit var auth: FirebaseAuth // Auth instance

    // Room Database DAO
    private lateinit var categoryDao: CategoryDao // DAO instance
    private lateinit var receiptImageDao: ReceiptImageDao // ADD ReceiptImage DAO instance

    //  Spinner Dat
    private val categoryNamesList = mutableListOf<String>()
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private val ADD_NEW_CATEGORY = "Add New Category"
    private lateinit var HINT_SELECT_CATEGORY: String

    // Date Picker Data
    private var selectedDateObject: Date? = null

    // Image Data
    private var selectedImageUri: Uri? = null
    private var selectedImageBytes: ByteArray? = null

    // Updated Image Picker Launcher to read bytes
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it // Store Uri
            Log.d("LogExpense", "Image URI selected: $it")
            // Launch coroutine to read bytes off the main thread
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Read the image URI into a byte array
                    contentResolver.openInputStream(it)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        Log.d("LogExpense", "Image read into ${bytes.size} bytes.")
                        // Switch back to main thread to update UI and store bytes
                        withContext(Dispatchers.Main) {
                            selectedImageBytes = bytes
                            btnAttachReceipt.text = "Receipt Selected (${bytes.size / 1024} KB)"
                        }
                    } ?: throw IOException("ContentResolver returned null InputStream for URI: $it")
                } catch (e: IOException) {
                    Log.e("LogExpense", "IOException reading image URI: $it", e)
                    handleImageReadError("Failed to read image file.")
                } catch (e: SecurityException) {
                    Log.e("LogExpense", "SecurityException reading image URI: $it", e)
                    handleImageReadError("Permission denied for reading image.")
                } catch (e: Exception) {
                    Log.e("LogExpense", "Generic Exception reading image URI: $it", e)
                    handleImageReadError("An error occurred while processing the image.")
                }
            }
        } ?: run {
            // URI is null (user cancelled or other issue)
            Log.d("LogExpense", "Image selection cancelled or failed.")
            clearSelectedImage() // Reset image selection state
        }
    }

    // Helper function to handle image reading errors and UI reset
    private suspend fun handleImageReadError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@LogExpense, message, Toast.LENGTH_LONG).show()
            clearSelectedImage()
        }
    }

    // Helper function to clear image selection state
    private fun clearSelectedImage() {
        selectedImageUri = null
        selectedImageBytes = null
        btnAttachReceipt.text = "Attach Receipt" // Reset button text
    }

    // Activity Result Launcher for AddCategoryActivity
    private val addCategoryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Refreshing categories...", Toast.LENGTH_SHORT).show()
            loadCategoriesFromDb()
        }
        // Reset selection display after returning
        spinnerCategory.setSelection(0)
        tvCategoryLabel.text = HINT_SELECT_CATEGORY
        tvCategoryLabel.setTextColor(ContextCompat.getColor(this, R.color.text_hint_color))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_expense)

        // Views By ID
        spinnerCategory = findViewById(R.id.spinnerCategory)
        tvCategoryLabel = findViewById(R.id.tvCategoryLabel)
        etExpenseName = findViewById(R.id.etExpenseName)
        etVendorName = findViewById(R.id.etVendorName)
        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        etNotes = findViewById(R.id.etNotes)
        btnAttachReceipt = findViewById(R.id.btnAttachReceipt)
        btnCancel = findViewById(R.id.btnCancel)
        btnConfirm = findViewById(R.id.btnConfirm)
        ivHome = findViewById(R.id.ivHome)
        ivInsights = findViewById(R.id.ivInsights)
        ivAdd = findViewById(R.id.ivAdd)
        ivGoals = findViewById(R.id.ivGoals)
        ivProfile = findViewById(R.id.ivProfile)

        HINT_SELECT_CATEGORY = getString(R.string.hint_select_category)

        // Initialize Room DAO
        val roomDb = AppDatabase.getDatabase(applicationContext) // Get DB instance
        categoryDao = roomDb.categoryDao()
        receiptImageDao = roomDb.receiptImageDao() // Initialize the new DAO

        // Initialize Firebase
        db = FirebaseFirestore.getInstance() // Get Firestore instance
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0 , systemBars.right, 0) // bottom = 0
            insets
        }

        // Setup UI components
        setupCategorySpinner()
        loadCategoriesFromDb()
        setupDatePicker()
        setupButtonClickListeners()
        setupNavigationBar()


    }

    // Category Spinner Setup
    private fun setupCategorySpinner() {
        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNamesList)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = parent.getItemAtPosition(position).toString()
                if (selectedCategory == ADD_NEW_CATEGORY) {
                    val intent = Intent(this@LogExpense, AddCategoryActivity::class.java)
                    addCategoryLauncher.launch(intent)
                } else if (position == 0 && categoryNamesList.size > 1 && categoryNamesList[0] == HINT_SELECT_CATEGORY) {
                    tvCategoryLabel.text = HINT_SELECT_CATEGORY
                    //tvCategoryLabel.setTextColor(ContextCompat.getColor(this@LogExpense, R.color.text_hint_color))
                } else {
                    tvCategoryLabel.text = selectedCategory
                    //tvCategoryLabel.setTextColor(ContextCompat.getColor(this@LogExpense, R.color.text_primary_color))
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        tvCategoryLabel.setOnClickListener { spinnerCategory.performClick() }
    }

    // Load Categories from Room DB
    private fun loadCategoriesFromDb() {
        // Launch a coroutine in the IO dispatcher for database access
        lifecycleScope.launch(Dispatchers.IO) {
            val fetchedNames = try {
                // Fetch all category names from the DAO
                categoryDao.getAllCategoryNames()
            } catch (e: Exception) {
                // Handle potential database errors gracefully
                Log.e("LogExpense", "Error fetching categories from DB", e)
                listOf<String>() // Return an empty list on error
            }

            // Switch back to the Main thread to update the UI
            withContext(Dispatchers.Main) {
                // Clear the existing list
                categoryNamesList.clear()

                // Add the hint as the first item
                categoryNamesList.add(HINT_SELECT_CATEGORY)

                // Add all the names fetched from the database
                categoryNamesList.addAll(fetchedNames)

                // Add the "Add New Category" option at the end
                categoryNamesList.add(ADD_NEW_CATEGORY)

                // Notify the adapter that the underlying data has changed
                categoryAdapter.notifyDataSetChanged()

                spinnerCategory.setSelection(0, false) // Select hint, don't trigger listener
                tvCategoryLabel.text = HINT_SELECT_CATEGORY
                //tvCategoryLabel.setTextColor(ContextCompat.getColor(this@LogExpense, R.color.text_hint_color))
            }
        }
    }

    // Date Picker Setup
    private fun setupDatePicker() {
        // Make the EditText non-editable directly, but clickable to trigger the dialog
        etDate.isFocusable = false
        etDate.isClickable = true

        // Set the click listener to show the dialog
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            // If a date was previously selected, start the dialog at that date
            selectedDateObject?.let {
                calendar.time = it
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Create and show the DatePickerDialog
            DatePickerDialog(
                this, // Activity context
                { _, selectedYear, selectedMonth, selectedDay ->

                    // Create a Calendar object for the selected date
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth) // Month is 0-indexed
                        set(Calendar.DAY_OF_MONTH, selectedDay)
                        // Clear time
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    // Store the selected date as a Date object
                    selectedDateObject = selectedCalendar.time
                    Log.d("LogExpense", "Date selected: $selectedDateObject")


                    // Format the date for display in the EditText (MM/dd/yyyy)
                    val displayFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    val formattedDate = displayFormat.format(selectedDateObject!!)
                    etDate.setText(formattedDate)

                    // Clear any previous error state on the date field
                    etDate.error = null

                },
                year, // Initial year to show
                month, // Initial month to show
                day // Initial day to show
            ).show() // Display the dialog
            Log.d("LogExpense","DatePickerDialog show() called.") // Log after calling show
        }
        Log.d("LogExpense", "etDate click listener set.") // Log after setting listener
    }

    // Button Click Listeners Setup
    private fun setupButtonClickListeners() {
        btnAttachReceipt.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnCancel.setOnClickListener { finish() }
        btnConfirm.setOnClickListener { handleConfirmClick() }
    }

    // Handle Confirm Click
    private fun handleConfirmClick() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get & Validate Input
        val expenseName = etExpenseName.text.toString().trim()
        val vendorName = etVendorName.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()
        val notes = etNotes.text.toString().trim()
        val dateToSave = selectedDateObject
        val selectedCategoryIndex = spinnerCategory.selectedItemPosition
        val selectedCategoryValue = spinnerCategory.selectedItem?.toString()
        var category: String? = null
        if (selectedCategoryIndex > 0 && selectedCategoryValue != ADD_NEW_CATEGORY && selectedCategoryValue != HINT_SELECT_CATEGORY) {
            category = selectedCategoryValue
        }

        // Validation Logic
        var isValid = true
        if (expenseName.isEmpty()) { etExpenseName.error = "Required"; isValid = false } else etExpenseName.error = null
        if (vendorName.isEmpty()) { etVendorName.error = "Required"; isValid = false } else etVendorName.error = null
        if (amountStr.isEmpty()) {
            etAmount.error = "Required"; isValid = false
        } else {
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) { etAmount.error = "Invalid positive amount"; isValid = false } else etAmount.error = null
        }
        if (dateToSave == null) { etDate.error = "Required"; isValid = false } else etDate.error = null

        // Category validation using the TextView
        if (category == null) {
            isValid = false
        } else {
        }

        if (!isValid) {
            Toast.makeText(this, "Please fix the errors.", Toast.LENGTH_SHORT).show()
            return
        }


        val amountValue = amountStr.toDouble() // Already validated

        // Create the Expense object for Firestore
        val expenseForFirestore = Expense(
            userId = userId,
            name = expenseName,
            category = category!!, // Not null due to validation
            vendor = vendorName.takeIf { it.isNotEmpty() },
            amount = amountValue,
            date = dateToSave,
            notes = notes.takeIf { it.isNotEmpty() }
        )

        // Start the saving process (Firestore first, then RoomDB)
        saveExpenseData(expenseForFirestore, selectedImageBytes)
    }

    // Combined Saving Function (Firestore + RoomDB)
    private fun saveExpenseData(expense: Expense, imageBytes: ByteArray?) {
        lifecycleScope.launch(Dispatchers.IO) { // Use IO dispatcher for database/network
            var firestoreDocId: String? = null // Keep track of ID for Room linking
            try {
                // --- Step 1: Save main data to Firestore ---
                val documentReference = db.collection("Expenses")
                    .add(expense) // Save the object without image bytes
                    .await() // Wait for Firestore operation to complete

                firestoreDocId = documentReference.id // Get the generated ID
                Log.d("LogExpense", "Expense data saved to Firestore with ID: $firestoreDocId")

                // --- Step 2: Save image to RoomDB ---
                if (imageBytes != null) {
                    // Create the Room entity linking the image to the Firestore doc ID
                    val receiptImage = ReceiptImage(
                        firestoreDocumentId = firestoreDocId,
                        image = imageBytes
                    )
                    receiptImageDao.insertReceipt(receiptImage) // Save image to Room
                    Log.d("LogExpense", "Receipt image saved to Room for Firestore ID: $firestoreDocId")
                } else {
                    Log.d("LogExpense", "No image bytes to save to Room.")
                }

                // --- Step 3: Success ---
                withContext(Dispatchers.Main) {
                    handleSaveSuccess()
                }

            } catch (e: Exception) { // Catch errors from Firestore or RoomDB
                Log.e("LogExpense", "Error during saving process", e)

                // Generally complex, maybe just log the inconsistency for now.
                if (firestoreDocId != null && imageBytes != null) {
                    Log.w("LogExpense", "Firestore save succeeded but Room failed for image associated with $firestoreDocId. Data might be inconsistent.")
                }

                // --- Step 4: Failure ---
                withContext(Dispatchers.Main) {
                    handleSaveError("Failed to save expense details: ${e.localizedMessage}")
                }
            }
        }
    }

    // Handle Save Success
    private fun handleSaveSuccess() {
        showLoading(false) // Hide progress
        Toast.makeText(this, "Expense logged successfully!", Toast.LENGTH_SHORT).show()
        // Navigate back or clear form
        val intent = Intent(this, ExpensesActivity::class.java)
        val intentless = Intent(this, ExpensesActivity::class.java)
        intentless.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Effectively clears the backstack - prevents access to previous activities
        startActivity(intentless) //adds home page to backstack - for cancellation
        startActivity(intent) //refreshes the expenses
        finish() // Close the activity
    }

    // Handle Save Error
    private fun handleSaveError(errorMessage: String) {
        // Make error more user-friendly if desired
        Toast.makeText(this@LogExpense, "Error: $errorMessage", Toast.LENGTH_LONG).show()
        Log.e("LogExpense", "Save Error: $errorMessage")
    }

    // Helper for showing/hiding progress
    private fun showLoading(isLoading: Boolean) {
        btnConfirm.isEnabled = !isLoading
        btnCancel.isEnabled = !isLoading
        etExpenseName.isEnabled = !isLoading
        etVendorName.isEnabled = !isLoading
        etAmount.isEnabled = !isLoading
        etDate.isEnabled = !isLoading
        etNotes.isEnabled = !isLoading
        spinnerCategory.isEnabled = !isLoading
        tvCategoryLabel.isEnabled = !isLoading
        btnAttachReceipt.isEnabled = !isLoading
    }

    // Setup Navigation Bar
    private fun setupNavigationBar() {
        val homeImageButton = findViewById<ImageButton>(R.id.ivHome)
        val insightsImageButton = findViewById<ImageButton>(R.id.ivInsights)
        val addImageButton = findViewById<ImageButton>(R.id.ivAdd)
        val goalsImageButton = findViewById<ImageButton>(R.id.ivGoals)
        val profileImageButton = findViewById<ImageButton>(R.id.ivProfile)

        homeImageButton.setOnClickListener{
            val intent = Intent(this, ExpensesActivity::class.java)
            startActivity(intent)
        }

        insightsImageButton.setOnClickListener{
            val intent = Intent(this, Insights::class.java)
            startActivity(intent)
        }

        addImageButton.setOnClickListener{
            val intent = Intent(this, LogExpense::class.java)
            startActivity(intent)
        }

        goalsImageButton.setOnClickListener{
            val intent = Intent(this, BudgetGoals::class.java)
            startActivity(intent)
        }

        profileImageButton.setOnClickListener{
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }
}