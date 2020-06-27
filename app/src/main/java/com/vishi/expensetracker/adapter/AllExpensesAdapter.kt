package com.vishi.expensetracker.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vishi.expensetracker.R
import com.vishi.expensetracker.model.ExpenseDetails
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class AllExpensesAdapter(private val mExpensesList: List<ExpenseDetails>?) :RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mAvg: Double = 0.0
    private var mStdDev: Double = 0.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_layout, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mExpensesList?.size ?: 0
    }

    fun getAvg() {
        var total: Double = 0.0
        var totalStdDev: Double = 0.0

        for (expense in mExpensesList!!) {
            total += expense.amountSpent
        }

        mAvg = total/mExpensesList.size

        for (expense in mExpensesList!!) {
            totalStdDev += (mAvg - expense.amountSpent).pow(2)
        }

        mStdDev = sqrt(totalStdDev/mExpensesList.size)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val expense = mExpensesList?.get(position)?.amountSpent
        val desc = mExpensesList?.get(position)?.description
        val day = mExpensesList?.get(position)?.day
        val month = mExpensesList?.get(position)?.month
        val year = mExpensesList?.get(position)?.year

        getAvg()

        (holder as ExpenseViewHolder).setExpenseDetails(expense!!, desc!!, day!!, month!!, year!!, mAvg!!, mStdDev!!)
    }

    internal inner class ExpenseViewHolder(expenseView: View) : RecyclerView.ViewHolder(expenseView) {

        private val amountSpent: TextView
        private val description: TextView
        private val date: TextView

        init {
            amountSpent = expenseView.findViewById(R.id.amountSpentTextId)
            description = expenseView.findViewById(R.id.expenseDescriptionId)
            date = expenseView.findViewById(R.id.expenseDateId)
        }

        internal fun setExpenseDetails(expense: Double, desc: String, day: Int, month: Int, year: Int, avg: Double, stdDev: Double) {
            amountSpent.text = "\u20B9 " +  String.format(Locale.ENGLISH, "%.2f", expense)
            description.text = desc
            date.text = "" + day + "-" + month + "-" + year + ""

            if (expense > (avg + stdDev)) {
                amountSpent.setTextColor(Color.parseColor("#FF0012"))
            }
            else if (expense < (avg - stdDev)) {
                amountSpent.setTextColor(Color.parseColor("#14E715"))
            }
            else {
                amountSpent.setTextColor(Color.parseColor("#FFA836"))
            }
        }

    }

}