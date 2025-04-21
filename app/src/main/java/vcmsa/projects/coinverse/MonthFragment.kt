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

        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        for (month in months) {
            monthTabLayout.addTab(monthTabLayout.newTab().setText(month))
        }

        return view

    }
}
