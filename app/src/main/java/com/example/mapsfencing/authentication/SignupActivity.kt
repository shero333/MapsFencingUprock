package com.example.mapsfencing.authentication

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mapsfencing.R
import com.example.mapsfencing.admin.AdminMapsActivity
import com.example.mapsfencing.databinding.ActivitySignupBinding
import com.example.mapsfencing.models.User
import com.example.mapsfencing.preferences.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.Serializable
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var signupBinding: ActivitySignupBinding
    private lateinit var username : String
    private lateinit var fullname : String
    private lateinit var confirm_password : String
    private lateinit var contact : String
    private lateinit var password : String
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signupBinding = ActivitySignupBinding.inflate(layoutInflater)

        setContentView(signupBinding.root)

        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(R.color.icon_splash_background)

        firebaseAuth = FirebaseAuth.getInstance()

        preferenceManager = PreferenceManager(this)

        signupBinding.signupButton.setOnClickListener {

            if (!Objects.requireNonNull(signupBinding.usernameEdtSignup.text).toString()
                    .isEmpty()
            ) username = signupBinding.usernameEdtSignup.text
                .toString() else signupBinding.usernameEdtSignup.error = "Please enter email"

            if (!Objects.requireNonNull(signupBinding.fullnameEdtSignup.text).toString()
                    .isEmpty()
            ) fullname = signupBinding.fullnameEdtSignup.text
                .toString() else signupBinding.fullnameEdtSignup.error = "Please enter fullname"

            if (!Objects.requireNonNull(signupBinding.passwordEdtSignup.text).toString()
                    .isEmpty()
            ) password = signupBinding.passwordEdtSignup.text
                .toString() else signupBinding.passwordEdtSignup.error = "Please enter password"

            if (!Objects.requireNonNull(signupBinding.confirmPasswordEdtSignup.text).toString()
                    .isEmpty()
            ) confirm_password = signupBinding.confirmPasswordEdtSignup.text
                .toString() else signupBinding.confirmPasswordEdtSignup.error = "Please enter password"

            if (!Objects.requireNonNull(signupBinding.contactEdtSignup.text).toString().isEmpty()) {
                if (signupBinding.contactEdtSignup.text.toString().length == 11) {
                    contact = signupBinding.contactEdtSignup.text.toString()
                } else {
                    signupBinding.contactEdtSignup.error = "Invalid contact!"
                }
            } else signupBinding.contactEdtSignup.error = "Please enter contact"

            if (signupBinding.passwordEdtSignup.text != null && signupBinding.usernameEdtSignup.text != null && signupBinding.fullnameEdtSignup.text != null && signupBinding.contactEdtSignup.text != null) {
                if (password != null && confirm_password != null) {
                    if (password == confirm_password) {

                        if (signupBinding.usernameEdtSignup.text.toString().contains("@")) {
                            if (!preferenceManager.isLoggedIn()) {
                                firebaseAuth.createUserWithEmailAndPassword(username, password)
                                    .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                                        if (task.isSuccessful) {
                                            val user = User(
                                                username,
                                                fullname,
                                                password,
                                                contact,
                                                (Long.MAX_VALUE - System.currentTimeMillis()).toString())

                                            //realtime database
                                            FirebaseDatabase.getInstance().reference
                                                .child("Users")
                                                .child(user.id.toString())
                                                .setValue(user)

                                            preferenceManager.storeuser(user)
                                            Log.d("signup_user", user.toString())

                                            //signin
                                            val login = Intent(this@SignupActivity, LoginActivity::class.java)
                                            startActivity(login)
                                            finish()

                                            Toast.makeText(this@SignupActivity,"User Signed up!",Toast.LENGTH_SHORT).show()

//                                            //Snack bar
//                                            val parentLayout = findViewById<View>(android.R.id.content)
//                                            val snackbar = Snackbar.make(parentLayout, "Signup Successful",
//                                                Snackbar.LENGTH_LONG)
//                                            snackbar.setAction("Close") { view: View? -> snackbar.dismiss() }
//                                            snackbar.setActionTextColor(resources.getColor(android.R.color.holo_red_light))
//                                            snackbar.show()
                                        }
                                    }
                            } else {
                                //Snack bar
                                val parentLayout = findViewById<View>(android.R.id.content)
                                val snackbar = Snackbar.make(parentLayout, "user already exists",
                                    Snackbar.LENGTH_LONG)
                                snackbar.setAction("Close") { view: View? -> snackbar.dismiss() }
                                snackbar.setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                                snackbar.show()
                            }
                        } else signupBinding.usernameEdtSignup.error = "Invalid email"
                    } else {
                        signupBinding.passwordEdtSignup.error = "Passwords do not match!"
                    }
                } else {
                    signupBinding.passwordEdtSignup.error = "Please enter password"
                    signupBinding.confirmPasswordEdtSignup.error = "Please confirm password"
                }
            } else {
                signupBinding.usernameEdtSignup.error = "Empty fields are not allowed"
                signupBinding.passwordEdtSignup.error = "Empty fields are not allowed"
                signupBinding.contactEdtSignup.error = "Empty fields are not allowed"
                signupBinding.fullnameEdtSignup.error = "Empty fields are not allowed"
                signupBinding.confirmPasswordEdtSignup.error = "Empty fields are not allowed"
            }

        }

//        signupBinding.hidePasswordSignup1.setOnClickListener {
//            ShowHidePass(signupBinding.root.rootView)
//        }

        signupBinding.loginLink.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            finish()
        }
    }

//    fun ShowHidePass(view: View) {
//        if (view.id == R.id.hide_password_login) {
//            if (signupBinding.passwordEdtSignup.transformationMethod
//                    .equals(PasswordTransformationMethod.getInstance())
//            ) {
//                assert(signupBinding.hidePasswordSignup1 != null)
//                signupBinding.hidePasswordSignup1.setImageResource(R.drawable.hide_password)
//
//                //Show Password
//                signupBinding.passwordEdtSignup.transformationMethod = HideReturnsTransformationMethod.getInstance()
//            } else {
//                assert(signupBinding.hidePasswordSignup1 != null)
//                signupBinding.hidePasswordSignup1.setImageResource(R.drawable.show_password)
//
//                //Hide Password
//                signupBinding.passwordEdtSignup.transformationMethod = PasswordTransformationMethod.getInstance()
//            }
//        }
//    }

}