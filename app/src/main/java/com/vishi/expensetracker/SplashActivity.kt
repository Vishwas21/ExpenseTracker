package com.vishi.expensetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.paperdb.Paper

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 1500
    private lateinit var txtUsername: String
    private lateinit var txtPassword: String
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var topConstraintLayout: ConstraintLayout
    private lateinit var bottomConstraintLayout: ConstraintLayout

    private lateinit var topAnimation: Animation
    private lateinit var bottomAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        topConstraintLayout = findViewById(R.id.topConstraintLayoutId)
        bottomConstraintLayout = findViewById(R.id.bottomConstraintLayoutId)

        topAnimation = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.top_to_mid)
        bottomAnimation = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.bottom_to_mid)

        topConstraintLayout.animation = topAnimation
        bottomConstraintLayout.animation = bottomAnimation

        firebaseAuth = Firebase.auth
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
                        val intent = Intent(this@SplashActivity, DetailActivity::class.java)
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
