package com.vishi.expensetracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.vishi.expensetracker.model.ExpenseDetails
import java.text.SimpleDateFormat
import java.util.*


class DetailActivity : AppCompatActivity() {

    private var mCal: Calendar? = null
    private var mYear: Int? = 0
    private var mMonth: Int? = 0
    private var mDay: Int? = 0
    private var mMonthName: String? = ""
    private var mYestYear: Int? = 0
    private var mYestMonth: Int? = 0
    private var mYestDay: Int? = 0
    private var mYestMonthName: String? = ""

    private lateinit var totalDayExpense: TextView
    private lateinit var addExpenseButton: TextView
    private lateinit var yesterdayTotalExpense: TextView
    private lateinit var yesterdayNameLabel: TextView
    private lateinit var totalMonthExpense: TextView
    private lateinit var monthNameLabel: TextView

    val databaseReference: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var dbListener: ListenerRegistration
    private var expenseList: MutableList<ExpenseDetails> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        mCal = Calendar.getInstance()
        mYear = mCal!!.get(Calendar.YEAR)
        mMonth = mCal!!.get(Calendar.MONTH)
        mDay = mCal!!.get(Calendar.DAY_OF_MONTH)

        val cal = Calendar.getInstance()
        val month_date = SimpleDateFormat("MMMM")
        cal.set(Calendar.MONTH, mMonth!!)
        mMonthName = month_date.format(cal.time)

        val yesCal = yesterday()
        mYestYear = yesCal.get(Calendar.YEAR)
        mYestMonth = yesCal.get(Calendar.MONTH)
        mYestDay = yesCal.get(Calendar.DAY_OF_MONTH)

        cal.set(Calendar.MONTH, mYestMonth!!)
        mYestMonthName = month_date.format(cal.time)

        totalDayExpense = findViewById(R.id.totalDayExpensesId)
        addExpenseButton = findViewById(R.id.addExpenseButtonId)
        addExpenseButton.isEnabled = false

        yesterdayTotalExpense = findViewById(R.id.totalYesterdayExpenseId)
        yesterdayNameLabel = findViewById(R.id.yesterdayNameLabelId)

        totalMonthExpense = findViewById(R.id.totalMonthlyExpenseId)
        monthNameLabel = findViewById(R.id.monthNameLabelId)

        getExpensesDetails()

        addExpenseButton.setOnClickListener {
            val add = Intent(this@DetailActivity, AddExpenseActivity::class.java)
            startActivityForResult(add, ADD_EXPENSE_REQUEST)
        }

        updateViews()
    }

    private fun yesterday(): Calendar {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, mYear!!)
        cal.set(Calendar.MONTH, mMonth!!)
        cal.set(Calendar.DAY_OF_MONTH, mDay!!)
        cal.add(Calendar.DATE, -1)
        return cal
    }

    override fun onDestroy() {
        super.onDestroy()
        dbListener.remove()
    }

    private fun getExpensesDetails() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        dbListener = databaseReference.collection("expenses")
            .whereEqualTo("addedBy", uid)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.w("Error", "Listen Failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    for (dc in querySnapshot.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val newExpense = dc.document.toObject(ExpenseDetails::class.java)
                                newExpense.id = dc.document.id

                                expenseList.add(newExpense)

                                updateViews()
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val modifiedExpense = dc.document.toObject(ExpenseDetails::class.java)
                                modifiedExpense.id = dc.document.id

                                updateExpense(modifiedExpense)

                                updateViews()
                            }

                            DocumentChange.Type.REMOVED -> {
                                val deletedExpense = dc.document.toObject(ExpenseDetails::class.java)
                                deletedExpense.id = dc.document.id

                                deleteExpense(deletedExpense)

                                updateViews()
                            }
                        }
                    }
                }
            }
    }

    private fun updateViews() {
        var dayTotal = 0.0
        var yestDayTotal = 0.0
        var monthTotal = 0.0
        var yearTotal = 0.0

        for (expense in expenseList) {
            if (expense.year == mYear!!) {
                if (expense.month == (mMonth!! + 1)) {
                    if (expense.day == mDay!!) {
                        dayTotal += expense.amountSpent
                    }
                    monthTotal += expense.amountSpent
                }
                yearTotal += expense.amountSpent
            }

            if (expense.year == mYestYear!! && expense.month == (mYestMonth!! + 1) && expense.day == mYestDay!!) {
                yestDayTotal += expense.amountSpent
            }
        }

        totalDayExpense.text = "\u20B9" +  String.format(Locale.ENGLISH, "%.2f", dayTotal)
        addExpenseButton.isEnabled = true

        yesterdayTotalExpense.text = "\u20B9" + String.format(Locale.ENGLISH, "%.2f", yestDayTotal)
        var suffix: String = ""

        if (mYestDay!! >= 11 && mYestDay!! <= 20) {
            suffix = "th"
        }
        else if (mYestDay!! % 10 == 1) {
            suffix = "st"
        }
        else if (mYestDay!! % 10 == 2) {
            suffix = "nd"
        }
        else if (mYestDay!! % 10 == 3) {
            suffix = "rd"
        }
        else {
            suffix = "th"
        }

        yesterdayNameLabel.setText("" + mYestDay + suffix + " " + mYestMonthName + ", " + mYestYear)

        totalMonthExpense.text = "\u20B9" + String.format(Locale.ENGLISH, "%.2f", monthTotal)
        monthNameLabel.text = mMonthName

        if (monthTotal <= 10000) {
            totalMonthExpense.setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.colorSafeGreen))
        }
        else if (monthTotal <= 25000) {
            totalMonthExpense.setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.colorWarningOrange))
        }
        else {
            totalMonthExpense.setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.colorDangerRed))
        }
    }

    private fun deleteExpense(deletedExpense: ExpenseDetails) {
        var index: Int = 0

        for (exp in expenseList) {
            if (exp.id == deletedExpense.id) {
                break
            }
            index += 1
        }

        if (index != -1 && index < expenseList.size) {
            expenseList.removeAt(index)
        }
    }

    private fun updateExpense(modifiedExpense: ExpenseDetails) {
        var index: Int = 0

        for (exp in expenseList) {
            if (exp.id == modifiedExpense.id) {
                break
            }
            index += 1
        }

        if (index != -1 && index < expenseList.size) {
            expenseList[index].amountSpent = modifiedExpense.amountSpent
            expenseList[index].day = modifiedExpense.day
            expenseList[index].month = modifiedExpense.month
            expenseList[index].year = modifiedExpense.year
            expenseList[index].description = modifiedExpense.description
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EXPENSE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this@DetailActivity, "Expense Added Successfully", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val ADD_EXPENSE_REQUEST = 123
    }
}
