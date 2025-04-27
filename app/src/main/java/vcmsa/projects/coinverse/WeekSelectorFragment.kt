package vcmsa.projects.coinverse
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment

class WeekSelectorFragment : Fragment() {


    interface WeekNavigationListener {
        fun onPreviousWeekClicked()
        fun onNextWeekClicked()
    }

    private var listener: WeekNavigationListener? = null
    private var currentWeek: Int = 1
    private var totalWeeks: Int = 4

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is WeekNavigationListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement WeekNavigationListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            currentWeek = it.getInt(ARG_CURRENT_WEEK, 1)
            totalWeeks = it.getInt(ARG_TOTAL_WEEKS, 4)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("WeekSelectorFragment", "onCreateView - Week: $currentWeek")
        val view = inflater.inflate(R.layout.fragment_week_selector, container, false)

        val tvWeekLabel = view.findViewById<TextView>(R.id.tvWeekLabelFragment)
        val ivPrevious = view.findViewById<ImageView>(R.id.ivPreviousWeekFragment)
        val ivNext = view.findViewById<ImageView>(R.id.ivNextWeekFragment)


        tvWeekLabel.text = "Week $currentWeek"


        ivPrevious.isVisible = currentWeek > 1
        ivNext.isVisible = currentWeek < totalWeeks


        ivPrevious.setOnClickListener {
            Log.d("WeekSelectorFragment", "Previous arrow clicked")
            listener?.onPreviousWeekClicked()
        }

        ivNext.setOnClickListener {
            Log.d("WeekSelectorFragment", "Next arrow clicked")
            listener?.onNextWeekClicked()
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    companion object {
        private const val ARG_CURRENT_WEEK = "current_week"
        private const val ARG_TOTAL_WEEKS = "total_weeks"

        @JvmStatic
        fun newInstance(currentWeek: Int, totalWeeks: Int) =
            WeekSelectorFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CURRENT_WEEK, currentWeek)
                    putInt(ARG_TOTAL_WEEKS, totalWeeks)
                }
            }
    }
}