package vcmsa.projects.coinverse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

class MonthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_month, container, false)

        val monthTabLayout = view.findViewById<TabLayout>(R.id.monthTabLayout)

        val months = listOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")

        for (month in months) {
            monthTabLayout.addTab(monthTabLayout.newTab().setText(month))
        }

        return view
    }
}

