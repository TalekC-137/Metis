package com.scollon.chattingapp
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.scollon.chattingapp.models.User
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        tv_already_acc.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.abc_fade_in,R.anim.abc_fade_out);
        }

        btn_register.setOnClickListener(){
//registering a new user in the Firebase
            var emailRegister = et_email_register.text.toString()
            var nameRegister = et_name_register.text.toString()
            var passwordRegister = et_password_register.text.toString()


            if(emailRegister.isEmpty() || nameRegister.isEmpty() || passwordRegister.isEmpty()){
                Toast.makeText(this, "enter ALL of the information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(emailRegister, passwordRegister)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("FireB_registration", "createUserWithEmail:success")
                        val user = auth.currentUser
                        if (user != null) {
                            Log.d("FireB_registration","userUid ${user.uid}")
                            uploadImageToFirebase()
                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d("FireB_registration", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()

                    }
                }
        }


        btn_photo_register.setOnClickListener(){

            val i = Intent(Intent.ACTION_PICK)
            i.type = "image/*"
            startActivityForResult(i, 0)
        }




    }
    var photoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //photo was selected
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){

            photoUri = data.data
            //getBitmap is deprecated but still works so no need to change it (for now)
            val bitMap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)

            iv_roundImage.setImageBitmap(bitMap)
            btn_photo_register.visibility = View.INVISIBLE
            val bitMapDrawable = BitmapDrawable(bitMap)
            btn_photo_register.background = bitMapDrawable
            btn_photo_register.text = ""


        }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null)
        val currentUser = auth.currentUser
        if(currentUser != null){
            val i = Intent(this, MessagesActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            Log.d("FireB_registration", "user was logged in and automatically moved to the messages activity")
        }
    }



    private fun uploadImageToFirebase(){
        if(photoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")


        ref.putFile(photoUri!!).addOnSuccessListener {

            ref.downloadUrl.addOnSuccessListener {
                Log.d("FireB_registration", "downloadUrl $it")
                 saveUserToFirebaseDatabase(it.toString())

            }

        }
    }

    private fun saveUserToFirebaseDatabase(ImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val username = et_name_register.text.toString()

       val user = User(uid, username, ImageUrl)
        ref.setValue(user).addOnSuccessListener {

            val i = Intent(this, MessagesActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)

            Log.d("FireB_registration", "user has been saved into the firebase")

        }.addOnFailureListener{

                Log.d("FireB_registration", "user has NOT been saved into the firebase")

            }

    }

}

