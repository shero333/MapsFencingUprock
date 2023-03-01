package com.example.mapsfencing.authentication

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mapsfencing.R
import com.example.mapsfencing.admin.AdminMapsActivity
import com.example.mapsfencing.databinding.ActivityLoginBinding
import com.example.mapsfencing.models.User
import com.example.mapsfencing.notification_services.MyFirebaseMessagingService
import com.example.mapsfencing.preferences.PreferenceManager
import com.example.mapsfencing.user.UserMapsActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var signinBinding: ActivityLoginBinding
    private lateinit var username : String
    private lateinit var password : String
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signinBinding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(signinBinding.root)

        FirebaseMessaging.getInstance().subscribeToTopic("all")

        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = this.resources.getColor(R.color.icon_splash_background)

        firebaseAuth = FirebaseAuth.getInstance()

        preferenceManager = PreferenceManager(this)

        signinBinding.loginButton.setOnClickListener {

            if (Objects.requireNonNull(signinBinding.usernameEdt.text).toString().isNotEmpty())
                username = signinBinding.usernameEdt.text.toString()
            else
                signinBinding.usernameEdt.error = "Please enter email"

            if (Objects.requireNonNull(signinBinding.passwordEdt.text).toString().isNotEmpty()
            ) password = signinBinding.passwordEdt.text
                .toString() else signinBinding.passwordEdt.error = "Please enter password"

            if (signinBinding.passwordEdt.text != null && signinBinding.usernameEdt.text != null) {

                if (signinBinding.usernameEdt.text.toString().contains("admin") &&
                    signinBinding.passwordEdt.text.toString().contains("123456")){

                    preferenceManager.storeuser(User(username,"admin",null,password,null))
                    preferenceManager.storeLoginStatus()

                    startActivity(Intent(this@LoginActivity, AdminMapsActivity::class.java))
                    finish()

                    Toast.makeText(this@LoginActivity,"Admin LoggedIn",Toast.LENGTH_LONG).show()

                    //Snack bar
                    val snackBar = Snackbar.make(findViewById(android.R.id.content),
                        "Admin LoggedIn",
                        Snackbar.LENGTH_LONG
                    )

                    snackBar.setAction("Close") { view: View? ->
                        // Call your action method here
                        snackBar.dismiss()
                    }
                    snackBar.show()
                }else{

                    firebaseAuth.signInWithEmailAndPassword(username,password).addOnCompleteListener {
                        if (it.isSuccessful){
                            preferenceManager.storeLoginStatus()

                            preferenceManager.storeuser(User(username,username,null,password,(Long.MAX_VALUE - System.currentTimeMillis()).toString()))

                            startActivity(Intent(this@LoginActivity, UserMapsActivity::class.java))
                            finish()

                            Toast.makeText(this@LoginActivity,"User LoggedIn",Toast.LENGTH_LONG).show()

                            startService(
                                Intent(
                                    this,
                                    MyFirebaseMessagingService::class.java
                                )
                            )


//                            //Snack bar
//                            val snackBar = Snackbar.make(findViewById(android.R.id.content),
//                                "User LoggedIn!",
//                                Snackbar.LENGTH_LONG
//                            )
//
//                            snackBar.setAction("Close") { view: View? ->
//                                // Call your action method here
//                                snackBar.dismiss()
//                            }
//                            snackBar.show()

                        }else{
                            //Snack bar
                            val snackBar = Snackbar.make(findViewById(android.R.id.content),
                                "We are currently unable to signin",
                                Snackbar.LENGTH_LONG
                            )

                            snackBar.setAction("Close") { view: View? ->
                                // Call your action method here
                                snackBar.dismiss()
                            }
                            snackBar.show()
                        }
                    }
                }
            } else {
                signinBinding.usernameEdt.error = "Empty fields are not allowed"
                signinBinding.passwordEdt.error = "Empty fields are not allowed"
            }

        }

        signinBinding.registerLink.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }

//        signinBinding.hidePasswordLogin.setOnClickListener { ShowHidePass(signinBinding.root.rootView) }

    }

//    private fun ShowHidePass(view: View) {
//        if (view.id == R.id.hide_password_login) {
//            if (signinBinding.passwordEdt.transformationMethod
//                    .equals(PasswordTransformationMethod.getInstance())
//            ) {
//                signinBinding.hidePasswordLogin.setImageResource(R.drawable.hide_password)
//
//                //Show Password
//                signinBinding.passwordEdt.transformationMethod = HideReturnsTransformationMethod.getInstance()
//            } else {
//                signinBinding.hidePasswordLogin.setImageResource(R.drawable.show_password)
//
//                //Hide Password
//                signinBinding.passwordEdt.transformationMethod = PasswordTransformationMethod.getInstance()
//            }
//        }
//    }

}