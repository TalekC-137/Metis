package com.scollon.chattingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.scollon.chattingapp.models.PostModel
import com.scollon.chattingapp.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_other_user_profile.*
import kotlinx.android.synthetic.main.chat_left_sender_row.view.*
import kotlinx.android.synthetic.main.other_user_posts.view.*
import java.text.SimpleDateFormat

class OtherUserProfileActivity : AppCompatActivity() {

    val adapter = GroupieAdapter()
    var user:User? = null

    val TAG = "userProfile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user_profile)



         user = intent.getParcelableExtra<User>("userProf")

        tv_otherUsername.text = user?.username
        Picasso.get().load(user?.profileImageUrl).into(iv_otherUserProfPic)


        var id = user?.uid

        listenForPosts()
        rv_user_posts.adapter = adapter

    }


    private fun listenForPosts(){

        Log.d(TAG, "listener engaged")

        var fromId = user?.uid // id of a user of whoms profile we're right now

        Log.d(TAG, "listening for messages from $fromId")

        val ref = FirebaseDatabase.getInstance().getReference("/user-posts/$fromId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val post = snapshot.getValue(PostModel::class.java)
                if(post != null){
                    adapter.add(UserPosts(user!!, post))
                    Log.d(TAG, "adding a post to the adapter")
                    adapter.add(Spacer())
                }



            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

    }
}

class UserPosts(val user: User, val postModel: PostModel): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        var timeDifference:Long = 0

        if(System.currentTimeMillis() - postModel.timeStamp >= 3600000){
            timeDifference = (System.currentTimeMillis() - postModel.timeStamp) /  3600000
            val timeText:String = timeDifference.toString() + " h"
            Log.d(TAG, "hours" + timeText)
            viewHolder.itemView.tv_post_time.text = timeText
        }else{
            timeDifference = (System.currentTimeMillis() - postModel.timeStamp) /  60000
            val timeText:String = timeDifference.toString() + " m"

            viewHolder.itemView.tv_post_time.text = timeText
        }

        viewHolder.itemView.tv_post_username.text = user.username

        viewHolder.itemView.tv_post_post.text = postModel.text

        val profImgUrl = user.profileImageUrl

        Picasso.get().load(profImgUrl).into(viewHolder.itemView.iv_post_profpic)


    }

    override fun getLayout(): Int {
        return R.layout.other_user_posts
    }

}
class Spacer(): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {



    }

    override fun getLayout(): Int {
        return R.layout.empty_space
    }

}
