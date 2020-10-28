package com.vishi.expensetracker.utility

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class FireStoreUtil {
    companion object FireStoreUtilObj {
        var mFireStoreDatabaseReference: FirebaseFirestore? = null
        var mFireStoreCurrentUserId: String? = null
        var mFirebaseAuth: FirebaseAuth? = null
        private var fireStoreUtil: FireStoreUtil? = null

        fun onFireStoreReference() {
            if (fireStoreUtil == null) {
                fireStoreUtil = FireStoreUtil()
                mFireStoreDatabaseReference = FirebaseFirestore.getInstance()
            }
        }

        fun setFireStoreUserId() {
            if (mFirebaseAuth == null) {
                onFirebaseAuth()
            }
            if (mFireStoreCurrentUserId == null){
                mFireStoreCurrentUserId = mFirebaseAuth!!.currentUser!!.uid
            }
        }

        fun onFirebaseAuth() {
            if (mFirebaseAuth == null) {
                mFirebaseAuth = Firebase.auth
            }
        }
    }
}