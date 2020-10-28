package com.vishi.expensetracker.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vishi.expensetracker.R
import com.vishi.expensetracker.utility.FireStoreUtil
import io.paperdb.Paper


class LoginActivity : AppCompatActivity() {

    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var login: Button
    private lateinit var progressBar: ProgressBar

    //String Values
    private lateinit var txtUsername: String
    private lateinit var txtPassword: String

    //Firebase
    private lateinit var mFirebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        FireStoreUtil.onFirebaseAuth()
        mFirebaseAuth = FireStoreUtil.mFirebaseAuth!!

        username = findViewById(R.id.usernameTextInputEditTextId)
        password = findViewById(R.id.passwordTextInputEditTextId)
        login = findViewById(R.id.loginButtonId)
        progressBar = findViewById(R.id.progressBarId)

        login.setOnClickListener {
            login.text = ""
            login.isEnabled = false
            progressBar.visibility = View.VISIBLE

            txtUsername = username.text.toString()
            txtPassword = password.text.toString()

            if (txtUsername.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Please enter a valid email id!", Toast.LENGTH_LONG).show()
                login.text = getString(R.string.action_log_in)
                login.isEnabled = true
                progressBar.visibility = View.INVISIBLE
            }
            else if (txtPassword.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Please enter a valid password!", Toast.LENGTH_LONG).show()
                login.text = getString(R.string.action_log_in)
                login.isEnabled = true
                progressBar.visibility = View.INVISIBLE
            }
            else {
                loginToFirebase()
            }
        }
    }

    private fun loginToFirebase() {

        //Authentication
        mFirebaseAuth.signInWithEmailAndPassword(txtUsername, txtPassword)
            .addOnCompleteListener {

                if (it.isSuccessful) {
                    Paper.init(this@LoginActivity)
                    Paper.book().write("username", txtUsername)
                    Paper.book().write("password", txtPassword)

                    Toast.makeText(this@LoginActivity, "Login Successfull", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@LoginActivity, DetailActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    password.setText("")
                    Toast.makeText(this@LoginActivity, "Login Unsuccessfull", Toast.LENGTH_LONG).show()
                    login.text = getString(R.string.action_log_in)
                    login.isEnabled = true
                    progressBar.visibility = View.INVISIBLE
                }
            }
    }
}