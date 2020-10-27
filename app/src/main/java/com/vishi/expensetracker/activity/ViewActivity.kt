package com.vishi.expensetracker.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.vishi.expensetracker.R
import com.vishi.expensetracker.adapter.AllExpensesAdapter
import com.vishi.expensetracker.model.ExpenseDetails
import java.util.*

class ViewActivity : AppCompatActivity() {

    private var mCal: Calendar? = null
    private var mYear: Int? = 0
    private var mMonth: Int? = 0
    private var mDay: Int? = 0

    private lateinit var topTextView: TextView
    private lateinit var totalExpense: TextView
    private lateinit var homeButton: Button

    val databaseReference: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var dbListener: ListenerRegistration

    private var expenseList: MutableList<ExpenseDetails> = mutableListOf()
    private var mExpensesRecyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val bundle: Bundle? = intent.extras

        mYear = bundle!!.getInt("year")
        mMonth = bundle.getInt("month")
        mDay = bundle.getInt("day")

        topTextView = findViewById(R.id.textViewTopTextId)
        totalExpense = findViewById(R.id.textViewTopTotalId)
        homeButton = findViewById(R.id.homeButtonId)

        topTextView.text = "Day Expenses"

        getExpensesDetails()

        mExpensesRecyclerView = findViewById(R.id.expenses_recyclerview)
        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = RecyclerView.VERTICAL

        mExpensesRecyclerView!!.layoutManager = layoutManager

        setAdapterForView()

        homeButton.setOnClickListener {
            val home = Intent(this@ViewActivity, DetailActivity::class.java)
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            home.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(home)
        }

        updateViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbListener.remove()
    }

    private fun setAdapterForView() {
        expensesAdapter = AllExpensesAdapter(expenseList)
        mExpensesRecyclerView!!.adapter = expensesAdapter
    }

    private fun getExpensesDetails() {
        val uid = Firebase.auth.currentUser!!.uid

        dbListener = databaseReference.collection("expenses")
            .whereEqualTo("day", mDay)
            .whereEqualTo("month", (mMonth!! + 1))
            .whereEqualTo("year", mYear!!)
            .whereEqualTo("addedBy", uid)
//            .orderBy("year", Query.Direction.DESCENDING)
//            .orderBy("month", Query.Direction.ASCENDING)
//            .orderBy("day", Query.Direction.ASCENDING)
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
                                expensesAdapter!!.notifyDataSetChanged()
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val modifiedExpense =
                                    dc.document.toObject(ExpenseDetails::class.java)
                                modifiedExpense.id = dc.document.id

                                updateExpense(modifiedExpense)
                                updateViews()
                                expensesAdapter!!.notifyDataSetChanged()
                            }

                            DocumentChange.Type.REMOVED -> {
                                val deletedExpense =
                                    dc.document.toObject(ExpenseDetails::class.java)
                                deletedExpense.id = dc.document.id

                                deleteExpense(deletedExpense)
                                updateViews()
                                expensesAdapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
    }

    private fun updateViews() {

        var total = 0.0

        for (expense in expenseList) {
            total += expense.amountSpent
        }

        totalExpense.text = "\u20B9" +  String.format(Locale.ENGLISH, "%.2f", total)
    }

    private fun deleteExpense(deletedExpense: ExpenseDetails) {
        var index = 0

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
        var index = 0

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
                Toast.makeText(this@ViewActivity, "Expense Added Successfully", Toast.LENGTH_LONG).show()
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
    companion object {
        const val ADD_EXPENSE_REQUEST = 154
        var expensesAdapter: AllExpensesAdapter? = null
    }
}
