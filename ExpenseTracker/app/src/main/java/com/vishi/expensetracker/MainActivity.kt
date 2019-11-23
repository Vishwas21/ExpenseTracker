package com.vishi.expensetracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.vishi.expensetracker.adapter.AllExpensesAdapter
import com.vishi.expensetracker.model.ExpenseDetails

import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val email: String = "ytriveni21167@gmail.com"
    private val password: String = "Admintriveni"

    private var mCal: Calendar? = null
    private var mYear: Int? = 0
    private var mMonth: Int? = 0
    private var mDay: Int? = 0

    val databaseReference: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var authReference: FirebaseAuth
    private lateinit var dbListner: ListenerRegistration

    private var expenseList: MutableList<ExpenseDetails> = mutableListOf()
    private var mExpensesRecyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        supportActionBar!!.hide()

        authReference = FirebaseAuth.getInstance()

        mCal = Calendar.getInstance()
        mYear = mCal!!.get(Calendar.YEAR)
        mMonth = mCal!!.get(Calendar.MONTH)
        mDay = mCal!!.get(Calendar.DAY_OF_MONTH)

        authReference.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Login Successfull", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this@MainActivity, "Login Unsuccessfull", Toast.LENGTH_LONG).show()
                }
            }

        fab.setOnClickListener { _ ->
            var add = Intent(this@MainActivity, AddExpenseActivity::class.java)
            startActivityForResult(add, ADD_EXPENSE_REQUEST)
        }

        getExpensesDetails()

        mExpensesRecyclerView = findViewById(R.id.expenses_recyclerview)
        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = RecyclerView.VERTICAL

        mExpensesRecyclerView!!.layoutManager = layoutManager

        setAdapterForView()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbListner.remove()
    }

    private fun setAdapterForView() {
        expensesAdapter = AllExpensesAdapter(expenseList)
        mExpensesRecyclerView!!.adapter = expensesAdapter
    }

    private fun getExpensesDetails() {
        dbListner = databaseReference.collection("expenses")
            .whereEqualTo("day", mDay)
            .whereEqualTo("month", (mMonth!! + 1))
            .whereEqualTo("year", mYear!!)
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
                                var newExpense = dc.document.toObject(ExpenseDetails::class.java)
                                newExpense.id = dc.document.id

                                expenseList.add(newExpense)

                                expensesAdapter!!.notifyDataSetChanged()
                            }

                            DocumentChange.Type.MODIFIED -> {
                                var modifiedExpense = dc.document.toObject(ExpenseDetails::class.java)
                                modifiedExpense.id = dc.document.id

                                updateExpense(modifiedExpense)
                                expensesAdapter!!.notifyDataSetChanged()
                            }

                            DocumentChange.Type.REMOVED -> {
                                var deletedExpense = dc.document.toObject(ExpenseDetails::class.java)
                                deletedExpense.id = dc.document.id

                                deleteExpense(deletedExpense)
                                expensesAdapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
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
                Toast.makeText(this@MainActivity, "Expense Added Successfully", Toast.LENGTH_LONG).show()
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
