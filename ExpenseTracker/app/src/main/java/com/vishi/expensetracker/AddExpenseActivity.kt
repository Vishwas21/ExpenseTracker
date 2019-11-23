package com.vishi.expensetracker

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
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

    val databaseReference: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val cal: Calendar = Calendar.getInstance()
        var mYear = cal.get(Calendar.YEAR)
        var mMonth = cal.get(Calendar.MONTH)
        var mDay = cal.get(Calendar.DAY_OF_MONTH)

        amountSpent = findViewById(R.id.editText_Amount)
        selectedDate = findViewById(R.id.editText_Date)
        descriptionEditView = findViewById(R.id.editText_Description)
        saveButton = findViewById(R.id.button_Save)

        selectedDate!!.setText("" + mDay + "/" + (mMonth + 1) + "/" + mYear)

        selectedDate!!.setOnClickListener {
            selectedDate!!.isEnabled = false

            Toast.makeText(this@AddExpenseActivity, "Here", Toast.LENGTH_LONG).show()

            datePickerDialog = DatePickerDialog(this@AddExpenseActivity, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
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
                var date = selectedDate!!.text.toString()
                description = descriptionEditView!!.text.toString()

                var splitDate = date.split("/")
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
        var newExpense = hashMapOf(
            "amountSpent" to amount,
            "day" to day,
            "month" to month,
            "year" to year,
            "description" to description
        )

        databaseReference.collection("expenses")
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
