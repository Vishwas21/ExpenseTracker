package com.vishi.expensetracker.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.vishi.expensetracker.R
import com.vishi.expensetracker.utility.FireStoreUtil
import java.sql.Timestamp
import java.util.*


class AddExpenseActivity : AppCompatActivity() {

    var amountSpent: EditText? = null
    var selectedDate: EditText? = null
    var descriptionEditView: EditText? = null
    var saveButton: Button? = null
    var datePickerDialog: DatePickerDialog? = null

    //Properties to store set values
    var amount: Int? = null
    var day: Int? = null
    var month: Int? = null
    var year: Int? = null
    var description: String? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabaseReference: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)
        
        FireStoreUtil.onFirebaseAuth()
        FireStoreUtil.onFireStoreReference()

        mDatabaseReference = FireStoreUtil.mFireStoreDatabaseReference!!
        mAuth = FireStoreUtil.mFirebaseAuth!!

        val cal: Calendar = Calendar.getInstance()
        val mYear = cal.get(Calendar.YEAR)
        val mMonth = cal.get(Calendar.MONTH)
        val mDay = cal.get(Calendar.DAY_OF_MONTH)

        amountSpent = findViewById(R.id.editText_Amount)
        selectedDate = findViewById(R.id.editText_Date)
        descriptionEditView = findViewById(R.id.editText_Description)
        saveButton = findViewById(R.id.button_Save)

        selectedDate!!.setText("" + mDay + "/" + (mMonth + 1) + "/" + mYear)

        selectedDate!!.setOnClickListener {
            selectedDate!!.isEnabled = false

            Toast.makeText(this@AddExpenseActivity, "Here", Toast.LENGTH_LONG).show()

            datePickerDialog = DatePickerDialog(this@AddExpenseActivity,
                { _, year, month, dayOfMonth ->
    //                selectedDate!!.text = "" + dayOfMonth + "-" + (month + 1) + "-" + year
                    selectedDate!!.setText("" + dayOfMonth + "/" + (month + 1) + "/" + year)
                    selectedDate!!.isEnabled = true
                }, mYear, mMonth, mDay)

            datePickerDialog!!.show()
        }

        saveButton!!.setOnClickListener { _ ->
            saveButton!!.isEnabled = false

            if (amountSpent!!.text.toString() != "" && descriptionEditView!!.text.toString() != "") {
                amount = amountSpent!!.text.toString().toInt()
                val date = selectedDate!!.text.toString()
                description = descriptionEditView!!.text.toString()

                val splitDate = date.split("/")
                day = splitDate[0].toInt()
                month = splitDate[1].toInt()
                year = splitDate[2].toInt()

                saveData(amount!!, day!!, month!!, year!!, description!!)
            }
            else {
                Toast.makeText(this@AddExpenseActivity, "Please fill all the details", Toast.LENGTH_LONG).show()
                saveButton!!.isEnabled = true
            }
        }
    }

    private fun saveData(amount: Int, day: Int, month: Int, year: Int, description: String) {
        val uid = Firebase.auth.currentUser!!.uid

        val timestamp: Date = Timestamp(System.currentTimeMillis())

        val newExpense = hashMapOf(
            "amountSpent" to amount,
            "day" to day,
            "month" to month,
            "year" to year,
            "description" to description,
            "addedBy" to uid,
            "timeAdded" to timestamp
        )

        mDatabaseReference.collection("expenses")
            .add(newExpense)
            .addOnSuccessListener {
                intent = Intent()
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this@AddExpenseActivity, "Error : " + it.stackTrace, Toast.LENGTH_LONG).show()
                intent = Intent()
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
            }
    }
}
