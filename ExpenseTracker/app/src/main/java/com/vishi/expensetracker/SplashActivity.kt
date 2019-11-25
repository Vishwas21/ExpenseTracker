package com.vishi.expensetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import io.paperdb.Paper

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 3000
    private lateinit var txtUsername: String
    private lateinit var txtPassword: String
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()
        Paper.init(this@SplashActivity)

        if (Paper.book().contains("username") and Paper.book().contains("password")) {
            txtUsername = Paper.book().read("username")
            txtPassword = Paper.book().read("password")

            loginToFirebase()
        }
        else {
            Handler().postDelayed({
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }, SPLASH_TIME_OUT)
        }
    }

    private fun loginToFirebase() {

        //Authentication
        firebaseAuth.signInWithEmailAndPassword(txtUsername, txtPassword)
            .addOnCompleteListener {

                if (it.isSuccessful) {
                    Toast.makeText(this@SplashActivity, "Login Successfull", Toast.LENGTH_LONG).show()
                    Handler().postDelayed({
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, SPLASH_TIME_OUT)
                }
                else {
                    Handler().postDelayed({
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }, SPLASH_TIME_OUT)
                }
            }
    }
}
