package vcmsa.projects.coinverse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

class MonthFragment : Fragment() {

    private lateinit var monthTabLayout: TabLayout
    private val months = listOf("January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December")

    var onMonthSelected: ((Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_month, container, false)

        monthTabLayout = view.findViewById<TabLayout>(R.id.monthTabLayout)

        months.forEach { month -> monthTabLayout.addTab(monthTabLayout.newTab().setText(month)) }

        monthTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let {index -> onMonthSelected?.invoke(index)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        selectCurrentMonth()

        //Old redundant code
//        val months = listOf("January", "February", "March", "April", "May", "June",
//            "July", "August", "September", "October", "November", "December")
//
//        for (month in months) {
//            monthTabLayout.addTab(monthTabLayout.newTab().setText(month))
//        }

        return view
    }

    //Sets and selects the current month by default
    fun selectCurrentMonth() {
        val currentMonthIndex = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        monthTabLayout.getTabAt(currentMonthIndex)?.select()
        onMonthSelected?.invoke(currentMonthIndex)
    }
}

