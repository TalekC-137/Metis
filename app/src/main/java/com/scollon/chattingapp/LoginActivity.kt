package com.scollon.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        btn_login.setOnClickListener(){

            var emailLogin = et_email_login.text.toString()
            var passwordLogin = et_password_login.text.toString()

            auth.signInWithEmailAndPassword(emailLogin, passwordLogin)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("FireB_login", "signInWithEmail:success")
                        val user = auth.currentUser
                        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                        val i = Intent(this, MessagesActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d("FireB_login", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()

                    }
                }



        }




    }
}