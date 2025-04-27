package vcmsa.projects.coinverse


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ExpensesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        // adds the month fragment into the frame layout !
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MonthFragment())
            .commit()
    }
}
