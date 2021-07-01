package com.scollon.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.scollon.chattingapp.models.ChatMessage
import com.scollon.chattingapp.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_left_sender_row.view.*
import kotlinx.android.synthetic.main.chat_right_sender_row.view.*

    val TAG = "CHATACTIVITY"

class ChatActivity : AppCompatActivity() {
    var user: User? = null
    val adapter = GroupieAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        


          user = intent.getParcelableExtra<User>("user") //this is the user we are texting with
        val userName = user?.username
         supportActionBar?.title = userName


            listenForMessages(user!!)

        rv_chat.adapter = adapter
/*
        val adapter = GroupieAdapter()
        adapter.add(ChatLeftItem(user, "od lewejj"))
        adapter.add(ChatRightItem("od prwejjj"))
        adapter.add(ChatLeftItem(user, "od lewejj"))
        adapter.add(ChatRightItem("od praweeej"))
        rv_chat.adapter = adapter  */


        btn_sendMessage.setOnClickListener(){

            sendMessage()

        }
    }

    private fun listenForMessages(user: User){
        val fromId = FirebaseAuth.getInstance().uid // current users id aka right chat
        val toId = user.uid //aka left uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")


        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if(chatMessage != null){
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = MessagesActivity.currentUser
                        if(currentUser != null) {
                            adapter.add(ChatRightItem(currentUser, chatMessage.text))
                        }
                    }else{
                        adapter.add(ChatLeftItem(user, chatMessage.text))
                    }

                

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




    override fun onBackPressed() {
        super.onBackPressed()
        var i = Intent(this, MessagesActivity::class.java)
        startActivity(i)
    }



    private fun sendMessage(){
        val user = intent.getParcelableExtra<User>("user")
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid
      //  val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val text = et_chatMessage.text.toString()
        if(text.isEmpty()) return
        if(toId == null) return
        if(fromId == null) return


        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()  //chat in user a node
        val reverseRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push() // chat in user b node

        val message = ChatMessage(ref.key!!, text, fromId, toId, System.currentTimeMillis())

        ref.setValue(message).addOnSuccessListener {
            Log.d("messageSending", "message was saved: ${ref.key}")
            rv_chat.scrollToPosition(adapter.itemCount -1)
        }
        reverseRef.setValue(message).addOnSuccessListener {
            Log.d("messageSending", "message was saved: ${reverseRef.key}")
        }
        et_chatMessage.text.clear()

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        val latestMessageReverseRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageRef.setValue(message)
        latestMessageReverseRef.setValue(message)

           }



     fun profClick( v: View){

        Log.d(TAG, "the profile pic has been clicked")
         var i = Intent(this, OtherUserProfileActivity::class.java)
         if(user != null){
         i.putExtra("userProf", user)
         startActivity(i)
         }
        else{
            Toast.makeText(this, "an error has accured pls try again", Toast.LENGTH_SHORT).show()
             Log.d(TAG, "user is null")
         }
    }


    }

//left side of the chat aka the messages that you receive
class ChatLeftItem(val user: User, val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.tv_chat_left.text = text

        val uri = user?.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.iv_leftProf)
    }
    override fun getLayout(): Int {
        return R.layout.chat_left_sender_row
    }
}
// right side of the chat aka the messages that you have sent
class ChatRightItem(val user: User, val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.tv_chat_right.text = text

        val uri = user?.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.iv_RightProf)

    }

    override fun getLayout(): Int {
        return R.layout.chat_right_sender_row
    }

}