package com.vishi.expensetracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.vishi.expensetracker.model.ExpenseDetails
import java.util.*

class DetailActivity : AppCompatActivity() {

    private var mCal: Calendar? = null
    private var mYear: Int? = 0
    private var mMonth: Int? = 0
    private var mDay: Int? = 0

    private lateinit var totalDayExpense: TextView
    private lateinit var addExpenseButton: TextView

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

        totalDayExpense = findViewById(R.id.totalDayExpensesId)
        addExpenseButton = findViewById(R.id.addExpenseButtonId)
        addExpenseButton.isEnabled = false

        getExpensesDetails()

        addExpenseButton.setOnClickListener {
            var add = Intent(this@DetailActivity, AddExpenseActivity::class.java)
            startActivityForResult(add, ADD_EXPENSE_REQUEST)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        dbListener.remove()
    }

    private fun getExpensesDetails() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        dbListener = databaseReference.collection("expenses")
            .whereEqualTo("day", mDay)
            .whereEqualTo("month", (mMonth!! + 1))
            .whereEqualTo("year", mYear!!)
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
                                var newExpense = dc.document.toObject(ExpenseDetails::class.java)
                                newExpense.id = dc.document.id

                                expenseList.add(newExpense)

                                updateViews()
                            }

                            DocumentChange.Type.MODIFIED -> {
                                var modifiedExpense = dc.document.toObject(ExpenseDetails::class.java)
                                modifiedExpense.id = dc.document.id

                                updateExpense(modifiedExpense)

                                updateViews()
                            }

                            DocumentChange.Type.REMOVED -> {
                                var deletedExpense = dc.document.toObject(ExpenseDetails::class.java)
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

        for (expense in expenseList) {
            if (expense.day == mDay) {
                dayTotal += expense.amountSpent
            }
        }

        totalDayExpense.text = "\u20B9 " +  String.format(Locale.ENGLISH, "%.2f", dayTotal)
        addExpenseButton.isEnabled = true
    }

    private fun deleteExpense(deletedExpense: ExpenseDetails) {
        var index: Int = 0

        for (exp in expenseList) {
            if (exp.id == deletedExpense.id) {
                break
            }
            index = index + 1
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
            index = index + 1
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
