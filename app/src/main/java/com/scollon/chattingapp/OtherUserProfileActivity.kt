package com.scollon.chattingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.scollon.chattingapp.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_other_user_profile.*

class OtherUserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user_profile)


        val user = intent.getParcelableExtra<User>("userProf")
        val userName = user?.username

        tv_otherUsername.text = user?.username
        Picasso.get().load(user?.profileImageUrl).into(iv_otherUserProfPic)
    }
}