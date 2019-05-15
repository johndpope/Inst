package com.example.inst.activities

import android.content.Intent
import android.os.Bundle
import com.example.inst.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity(0) {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupBottomNavigation()
        mAuth = FirebaseAuth.getInstance()

        sign_out_text.setOnClickListener {
            mAuth.signOut()
        }
        mAuth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        /*
        auth.signInWithEmailAndPassword("example@gmail.com", "123456")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i(TAG, "signIn: success")
                } else {
                    Log.i(TAG, "signIn: failure", it.exception)
                }
            }
        */
    }

    override fun onStart() {
        super.onStart()

        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
