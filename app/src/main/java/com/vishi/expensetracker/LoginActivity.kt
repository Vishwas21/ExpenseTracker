package com.vishi.expensetracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.paperdb.Paper


class LoginActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var loading: ProgressBar

    //String Values
    private lateinit var txtUsername: String
    private lateinit var txtPassword: String

    //Firebase
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        firebaseAuth = Firebase.auth

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        login = findViewById(R.id.login)
        loading = findViewById(R.id.loading)

        login.setOnClickListener {
            login.isEnabled = false

            txtUsername = username.text.toString()
            txtPassword = password.text.toString()

            if (txtUsername.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Please enter a valid email id!", Toast.LENGTH_LONG).show()
                login.isEnabled = true
            }
            else if (txtPassword.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Please enter a valid password!", Toast.LENGTH_LONG).show()
                login.isEnabled = true
            }
            else {
                loginToFirebase()
            }
        }
    }

    private fun loginToFirebase() {

        //Authentication
        firebaseAuth.signInWithEmailAndPassword(txtUsername, txtPassword)
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
                    login.isEnabled = true
                }
            }
    }
}