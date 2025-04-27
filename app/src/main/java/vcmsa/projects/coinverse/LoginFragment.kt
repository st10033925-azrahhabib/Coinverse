package vcmsa.projects.coinverse

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class LoginFragment : Fragment() {

   // hardcoded login info to test
    private val DEMO_USERNAME = "user@example.com"
    private val DEMO_PASSWORD = "password123"

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

                val intent = Intent(requireActivity(), BudgetGoals::class.java)
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