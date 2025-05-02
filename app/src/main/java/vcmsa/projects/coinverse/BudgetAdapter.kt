package vcmsa.projects.coinverse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.coinverse.ExpenseAdapter.ViewHolder

data class BudgetCategoryTotal(val category: String, val totalAmount: Double)

class BudgetAdapter(private val categoryTotals: List<BudgetCategoryTotal>) : RecyclerView.Adapter<BudgetAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val categoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        val categoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val categoryValue: TextView = itemView.findViewById(R.id.tvCategoryMax)
        val seekBar: SeekBar = itemView.findViewById(R.id.seekBarCategory)

        fun setCategoryIcon(category: String) {
            when (category.toLowerCase()) {
                "shopping" -> categoryIcon.setImageResource(R.drawable.ic_shopping)
                "transport" -> categoryIcon.setImageResource(R.drawable.ic_transport)
                "entertainment" -> categoryIcon.setImageResource(R.drawable.ic_entertainment)
                "sport" -> categoryIcon.setImageResource(R.drawable.ic_sports)
                else -> categoryIcon.setImageResource(R.drawable.ic_savings)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.budget_category_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = categoryTotals[position]
        holder.categoryName.text = currentItem.category
        holder.categoryValue.text = String.format("%.2f", currentItem.totalAmount)
        holder.seekBar.max = currentItem.totalAmount.toInt()
        holder.seekBar.progress = currentItem.totalAmount.toInt()
        holder.setCategoryIcon(currentItem.category)
    }

    override fun getItemCount() = categoryTotals.size
}