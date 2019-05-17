package com.example.inst.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.inst.R
import com.example.inst.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_namepass.*

class RegisterActivity : AppCompatActivity(), EmailFragment.Listener,
    NamePassFragment.Listener{
    private var mEmail: String? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.frame_layout, EmailFragment()).commit()
        }
    }

    override fun onNext(email: String) {
        if (email.isNotEmpty()) {
            mEmail = email
            mAuth.fetchSignInMethodsForEmail(email) { signInMethods ->
                if (signInMethods.isEmpty()) {
                    supportFragmentManager.beginTransaction().replace(R.id.frame_layout, NamePassFragment())
                        .addToBackStack(null).commit()
                } else showToast("This email already exists")
            }
        } else showToast("Please enter email")
    }

    private fun FirebaseAuth.fetchSignInMethodsForEmail(email: String, onSuccess: (List<String>) -> Unit){
        fetchSignInMethodsForEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess(it.result!!.signInMethods ?: emptyList<String>())
            } else showToast(it.exception!!.message!!)
        }
    }

    override fun onRegister(fullName: String, password: String) {
        if (fullName.isNotEmpty() && password.isNotEmpty()) {
            val email = mEmail
            if (email != null) {
                mAuth.createUserWithEmailAndPassword(email, password) {
                    mDatabase.createUser(it.user.uid, makeUser(fullName, email)){
                        startHomeActivity()
                    }
                }
            } else {
                Log.e("RegisterActivity", "onRegister: email is null")
                showToast("Please enter email")
                supportFragmentManager.popBackStack()
            }
        } else showToast("Please enter full name and password")
    }

    private fun DatabaseReference.createUser(uid: String, user: User, onSuccess: () -> Unit){
        child("users").child(uid).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                unknownRegisterError(it)
            }
        }
    }

    private fun FirebaseAuth.createUserWithEmailAndPassword(email: String, password: String, onSuccess: (AuthResult) -> Unit){
        createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess(it.result!!)
            } else {
                unknownRegisterError(it)
            }
        }
    }

    private fun unknownRegisterError(it: Task<*>) {
        Log.e("RegisterActivity", "failed to create user profile", it.exception)
        showToast("Something wrong happened. Please try again later")
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun makeUser(fullName: String, email: String): User {
        val username = makeUsername(fullName)
        return User(name = fullName, username = username, email = email)
    }

    private fun makeUsername(fullName: String) = fullName.toLowerCase().replace(" ", ".")
}

class EmailFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onNext(email: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateButtonAndInputs(login_button, email_input)
        login_button.setOnClickListener {
            val email = email_input.text.toString()
            mListener.onNext(email)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}

class NamePassFragment : Fragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onRegister(fullName: String, password: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register_namepass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateButtonAndInputs(register_button, full_name_input, password_input)
        register_button.setOnClickListener {
            val fullName = full_name_input.text.toString()
            val password = password_input.text.toString()
            mListener.onRegister(fullName, password)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}
