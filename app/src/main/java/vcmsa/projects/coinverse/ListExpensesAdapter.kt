package vcmsa.projects.coinverse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListExpensesAdapter(
    private val expenses: List<Expense>,
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<ListExpensesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView = itemView.findViewById(R.id.EntryName)
        val amountTV: TextView = itemView.findViewById(R.id.tvAmount)

        fun bind(expense: Expense) {
            nameTV.text = expense.name
            amountTV.text = String.format("- R %.2f", expense.amount)

            itemView.setOnClickListener {
                onItemClick(expense)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_rv, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = expenses.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(expenses[position])
    }
}
