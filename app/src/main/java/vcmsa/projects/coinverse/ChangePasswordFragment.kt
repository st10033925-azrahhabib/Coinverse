package vcmsa.projects.coinverse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import vcmsa.projects.coinverse.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Use View Binding to inflate the layout for this fragment.
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Set up the click listener for the update password button.
        binding.btnUpdatePassword.setOnClickListener {
            handleChangePassword()
        }
    }

    private fun handleChangePassword() {
        val oldPass = binding.etOldPassword.text.toString().trim()
        val newPass = binding.etNewPassword.text.toString().trim()
        val confirmPass = binding.etConfirmPassword.text.toString().trim()

        if (!validateInput(oldPass, newPass, confirmPass)) {
            return
        }
        // If validation passes, proceed to change the password with Firebase.
        performPasswordChange(oldPass, newPass)
    }

    private fun validateInput(oldPass: String, newPass: String, confirmPass: String): Boolean {
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showToast("Please fill all fields")
            return false
        }
        if (newPass.length < 6) {
            showToast("New password must be at least 6 characters long")
            return false
        }
        if (newPass != confirmPass) {
            showToast("New passwords do not match")
            return false
        }
        return true
    }

    // This function handles the core Firebase re-authentication and password update logic.
    private fun performPasswordChange(oldPass: String, newPass: String) {
        showLoading(true)
        val user = auth.currentUser
        if (user == null || user.email == null) {
            showToast("Error: User not found. Please re-login.")
            showLoading(false)
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, oldPass)
        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                    showLoading(false)
                    if (updateTask.isSuccessful) {
                        showToast("Password updated successfully!")
                        dismiss()
                    } else {
                        showToast("Error: ${updateTask.exception?.message}")
                    }
                }
            } else {
                showLoading(false)
                val exception = reauthTask.exception
                val errorMessage = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect current password."
                    else -> "Re-authentication failed. Please try again."
                }
                showToast(errorMessage)
            }
        }
    }

    // A helper function to manage the visibility of the button and progress bar.
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnUpdatePassword.visibility = View.INVISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnUpdatePassword.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        context?.let { Toast.makeText(it, message, Toast.LENGTH_LONG).show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // A unique tag for finding this fragment with the FragmentManager.
        const val TAG = "ChangePasswordFragment"
        fun newInstance(): ChangePasswordFragment {
            return ChangePasswordFragment()
        }
    }
}