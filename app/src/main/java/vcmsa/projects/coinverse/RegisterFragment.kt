package vcmsa.projects.coinverse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.content.Intent
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI Elements
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var passwordConfirmInput: EditText
    private lateinit var registerButton: Button

    // Define a logging tag
    private val TAG = "RegisterFragment" // Updated tag

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.partial_register_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth & Firestore
        auth = Firebase.auth
        db = Firebase.firestore

        // Get references using IDs
        usernameInput = view.findViewById(R.id.usernameInput)
        emailInput = view.findViewById(R.id.EmailInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        passwordConfirmInput = view.findViewById(R.id.passwordConfirmInput)
        registerButton = view.findViewById(R.id.confirmButton)

        registerButton.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val username = usernameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = passwordConfirmInput.text.toString().trim() // Get confirm password

        // Input Validation
        var isValid = true
        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
            if (isValid) usernameInput.requestFocus()
            isValid = false
        } else {
            usernameInput.error = null
            // IMPORTANT: Add check for USERNAME uniqueness here before proceeding
            // This requires another Firestore query. See note below.
        }

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            if (isValid) emailInput.requestFocus()
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // Validate Email Format
            emailInput.error = "Please enter a valid email address"
            if (isValid) emailInput.requestFocus()
            isValid = false
        } else {
            emailInput.error = null
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            if (isValid) passwordInput.requestFocus()
            isValid = false
        } else if (password.length < 6) { // Check password length
            passwordInput.error = "Password must be at least 6 characters"
            if (isValid) passwordInput.requestFocus()
            isValid = false
        } else {
            passwordInput.error = null
        }

        // Add password confirmation check
        if (confirmPassword.isEmpty()) {
            passwordConfirmInput.error = "Please confirm your password"
            if (isValid) passwordConfirmInput.requestFocus()
            isValid = false
        } else if (password != confirmPassword) {
            passwordConfirmInput.error = "Passwords do not match"
            if (isValid) passwordConfirmInput.requestFocus()
            // Clear both password fields on mismatch
            passwordInput.text.clear()
            passwordConfirmInput.text.clear()
            isValid = false
        } else {
            passwordConfirmInput.error = null
        }

        if (!isValid) {
            return // Stop if any validation failed
        }

        // **NOTE:** Ideally, check if username already exists in Firestore *before* creating Auth user.
        // This prevents creating an Auth user if the desired username is taken.
        // We'll proceed without it for now, matching the Kindred example, but add a check later if needed.

        Log.d(TAG, "Attempting Firebase registration with email: '$email'")
        setLoadingState(true)

        // Create User with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        // Save Username and Email to Firestore
                        saveUserDataToFirestore(user.uid, username, email) { success ->
                            setLoadingState(false)

                            if (success) {
                                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                goToMainActivity() // Navigate on success
                            } else {
                                // Firestore save failed, but Auth succeeded.
                                Toast.makeText(context, "Account created, but failed to save profile data.", Toast.LENGTH_LONG).show()
                                goToMainActivity()
                            }
                        }
                    } ?: run {
                        // Task success but currentUser is null
                        Log.e(TAG, "User creation success but currentUser is null.")
                        showRegistrationError("Registration failed. Please try again.")
                        setLoadingState(false)
                    }
                } else {
                    // Auth registration failed
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    handleRegistrationFailure(task.exception)
                    setLoadingState(false)
                }
            }
    }

    // Saves user data to Firestore
    private fun saveUserDataToFirestore(uid: String, username: String, email: String, onComplete: (Boolean) -> Unit) {
        val userDocument = db.collection("users").document(uid)
        // Store username in lowercase
        val userData = hashMapOf(
            "username" to username,
            "email" to email // Store email
        )

        userDocument.set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "Username and email saved to Firestore for UID: $uid")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving user data to Firestore for UID: $uid", e)
                Toast.makeText(context, "Error saving profile data.", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
    }

    // Helper to handle registration errors
    private fun handleRegistrationFailure(exception: Exception?) {
        var errorMessage = exception?.message ?: "Registration failed."
        Log.w(TAG, "Registration failure details: $errorMessage")

        // Check for Firebase Auth exceptions
        if (errorMessage.contains("email address is already in use", ignoreCase = true)) {
            emailInput.error = "This email is already registered"
            emailInput.requestFocus()
            errorMessage = "This email address is already registered."
        } else if (errorMessage.contains("WEAK_PASSWORD", ignoreCase = true)) {
            passwordInput.error = "Password is too weak (min 6 characters)"
            passwordInput.requestFocus()
            errorMessage = "Password is too weak."
        } else if (errorMessage.contains("network error", ignoreCase = true)) {
            errorMessage = "Network error. Please check your connection."
        }


        showRegistrationError(errorMessage)
    }

    // Helper to show errors
    private fun showRegistrationError(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    // Helper function for navigation
    private fun goToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    // Helper to manage loading state
    private fun setLoadingState(isLoading: Boolean) {
        registerButton.isEnabled = !isLoading
    }
}
