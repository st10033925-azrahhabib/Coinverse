package vcmsa.projects.coinverse

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class CategoryTotal(val category: String, val totalAmount: Double)

class ExpenseAdapter(private val categoryTotals: List<CategoryTotal>, private val currentMonth: Int) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryTV = itemView.findViewById<TextView>(R.id.tvCategory)
        val totalAmountTV = itemView.findViewById<TextView>(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = categoryTotals[position]
        holder.categoryTV.text = currentItem.category
        holder.totalAmountTV.text = String.format("- R %.2f", currentItem.totalAmount)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CategoryDetailsActivity::class.java)
            intent.putExtra("category", currentItem.category)
            intent.putExtra("month", currentMonth)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = categoryTotals.size
}