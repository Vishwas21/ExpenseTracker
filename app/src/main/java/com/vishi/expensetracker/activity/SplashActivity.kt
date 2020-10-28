package com.vishi.expensetracker.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.vishi.expensetracker.R
import com.vishi.expensetracker.utility.FireStoreUtil
import io.paperdb.Paper

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 1500
    private lateinit var txtUsername: String
    private lateinit var txtPassword: String
    private lateinit var mFirebaseAuth: FirebaseAuth

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

        FireStoreUtil.onFirebaseAuth()
        mFirebaseAuth = FireStoreUtil.mFirebaseAuth!!
        Paper.init(this@SplashActivity)

        if (Paper.book().contains("username") and Paper.book().contains("password")) {
            txtUsername = Paper.book().read("username")
            txtPassword = Paper.book().read("password")

            loginToFirebase()
        }
        else {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }, SPLASH_TIME_OUT)
        }
    }

    private fun loginToFirebase() {

        //Authentication
        mFirebaseAuth.signInWithEmailAndPassword(txtUsername, txtPassword)
            .addOnCompleteListener {

                if (it.isSuccessful) {
                    Toast.makeText(this@SplashActivity, "Login Successfull", Toast.LENGTH_LONG).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this@SplashActivity, DetailActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, SPLASH_TIME_OUT)
                }
                else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }, SPLASH_TIME_OUT)
                }
            }
    }
}
