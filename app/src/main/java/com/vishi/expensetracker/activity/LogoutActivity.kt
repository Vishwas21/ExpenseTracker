package com.vishi.expensetracker.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import io.paperdb.Paper

class LogoutActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Paper.init(this@LogoutActivity)

        if (Paper.book().contains("username") and Paper.book().contains("password")) {
            Paper.book().delete("username")
            Paper.book().delete("password")

            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this@LogoutActivity, "Logout Successful", Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(this@LogoutActivity, "User Already Logged Out!", Toast.LENGTH_LONG).show()
        }

//        startActivity(Intent(this@LogoutActivity, SplashActivity::class.java))
        finish()
    }
}