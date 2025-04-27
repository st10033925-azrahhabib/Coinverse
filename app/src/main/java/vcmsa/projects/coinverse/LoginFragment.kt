package vcmsa.projects.coinverse

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI Elements
    private lateinit var usernameInput: EditText // Changed name to match XML ID
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button

    // Define a logging tag
    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.partial_login_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val etUsername = view.findViewById<EditText>(R.id.usernameInput)
        val etPassword = view.findViewById<EditText>(R.id.passwordInput)
        val btnLogin   = view.findViewById<Button>(R.id.confirmButton)

        btnLogin.setOnClickListener {
            val inputUser = etUsername.text.toString().trim()
            val inputPass = etPassword.text.toString()

            if (inputUser == DEMO_USERNAME && inputPass == DEMO_PASSWORD) {

                val intent = Intent(requireActivity(), ExpensesActivity::class.java)
                startActivity(intent)

                requireActivity().finish()
            } else {
                // toast on failure
                Toast.makeText(
                    requireContext(),
                    "Invalid username or password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
=======
        // Initialize Firebase Auth & Firestore
        auth = Firebase.auth
        db = Firebase.firestore

        // Reference UI elements
        usernameInput = view.findViewById(R.id.usernameInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        loginButton = view.findViewById(R.id.confirmButton)

        loginButton.setOnClickListener {
            loginUserWithUsername()
        }
    }

    private fun loginUserWithUsername() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        //Input Validation
        var isValid = true
        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
            if (isValid) usernameInput.requestFocus()
            isValid = false
        } else {
            usernameInput.error = null // Clear previous error
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            if (isValid) passwordInput.requestFocus()
            isValid = false
        } else {
            passwordInput.error = null
        }

        if (!isValid) {
            return // Stop if input is invalid
        }
        //--

        Log.d(TAG, "Attempting login for username: '$username'")
        setLoadingState(true) // Disable button

        // Query Firestore for the username
        db.collection("users")
            .whereEqualTo("username", username) // Assumes field name is "username"
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Username Found
                    val document = querySnapshot.documents[0]
                    val email = document.getString("email") // Assumes field name is "email"
                    Log.d(TAG, "Firestore found username. Document ID: ${document.id}, Email retrieved: '$email'")

                    if (email != null && email.isNotEmpty()) {
                        // Attempt Firebase Auth Sign In using the retrieved email
                        signInWithFirebase(email, password)
                    } else {
                        // Email field missing in Firebase
                        Log.e(TAG, "Firestore document for username '$username' (ID: ${document.id}) is missing 'email' field or email is empty.")
                        showLoginError("Login failed. Please try again.")
                        setLoadingState(false)
                        passwordInput.text.clear()
                    }

                } else {
                    // Username Not Found in Firestore
                    Log.w(TAG, "Firestore query found no user with username: '$username'")
                    showLoginError("Invalid username or password.")
                    setLoadingState(false)
                    passwordInput.text.clear()
                    usernameInput.requestFocus()
                }
            }
            .addOnFailureListener { e ->
                // Firestore Query Failed
                Log.e(TAG, "Firestore query failed for username: '$username'", e)
                showLoginError("Login failed: Could not reach server. Check network.")
                setLoadingState(false)
            }
    }

    private fun signInWithFirebase(email: String, password: String) {
        Log.d(TAG, "Attempting Firebase Auth sign-in with email: '$email'")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { authTask ->
                if (authTask.isSuccessful) {
                    // Login Success
                    Log.d(TAG, "Firebase Auth successful for email: $email")
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    // Clear potential previous errors
                    usernameInput.error = null
                    passwordInput.error = null
                    goToMainActivity() // Navigate after successful login
                } else {
                    // Firebase Auth Failed
                    Log.w(TAG, "Firebase Auth failed for email: $email", authTask.exception)
                    showLoginError("Invalid username or password.")
                    setLoadingState(false) // Re-enable button
                    passwordInput.requestFocus()
                }
            }
    }

    // Helper to show errors consistently
    private fun showLoginError(message: String) {
        // Use context safely within a Fragment
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    // Helper function for navigation
    private fun goToMainActivity() {
        val intent = Intent(activity, LogExpense::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    // Helper to manage loading state
    private fun setLoadingState(isLoading: Boolean) {
        loginButton.isEnabled = !isLoading
    }
}

