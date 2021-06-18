package com.scollon.chattingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.scollon.chattingapp.models.ChatMessage
import com.scollon.chattingapp.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.latest_message_row.*
import kotlinx.android.synthetic.main.latest_message_row.view.*


class MessagesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    companion object{
        var currentUser: User? = null
    }

    val adapter = GroupieAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        auth = Firebase.auth //I know I have two of these




        getCurrentUser()
/*
        val user = FirebaseAuth.getInstance().currentUser

        if (user!!.isEmailVerified) {
            Log.d(TAG, "Email is verified.")
        } else {
            Log.d(TAG, "Email is not verified !.")
        }
*/

        listenForLatestMessages()


        rv_latest_messages.adapter = adapter

                //  rv_latest_messages.adapter = adapter
        rv_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->

            val i = Intent(this, ChatActivity::class.java)
            val userRow = item as LatestMessageRow
            i.putExtra("user", userRow.chatUser)
            startActivity(i)

        }

        btn_tryAgain.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }
        btn_refresh.setOnClickListener {
            finish()
            startActivity(getIntent())
            overridePendingTransition(R.anim.abc_fade_in,R.anim.abc_fade_out);
        }
    }


   private fun confirmVerification(){

       val user = FirebaseAuth.getInstance().currentUser
       user?.reload()

       if (user!!.isEmailVerified) {
           unverifiedLayout.visibility = View.GONE
           Log.d("verification", "Email is verified.")
       } else {
           unverifiedLayout.visibility = View.VISIBLE
           mail.text = user.email
           Log.d("verification", "Email is not verified !.")

       }
   }


    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerView(){
        adapter.clear()

        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))

        }

    }

    private fun listenForLatestMessages(){
        val currentUserId = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$currentUserId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {


                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = chatMessage

                refreshRecyclerView()



             //   changeOrder()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_messages, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       when(item?.itemId){
            R.id.menu_sigh_out -> {
                FirebaseAuth.getInstance().signOut()
            val i = Intent(this, RegisterActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            }
           R.id.menu_new_message -> {
               val i = Intent(this, NewMessageActivity::class.java)
               startActivity(i)
           }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun getCurrentUser(){
        val currentUsersUid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$currentUsersUid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null)
        val currentUser = auth.currentUser
        if(currentUser == null){
            val i = Intent(this, RegisterActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            Log.d("FireB_registration", "new user, was not logged in")
        }else{
            confirmVerification()
        }
    }
}

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){
   var chatUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {


        val whoAreWeTexting: String
        //if we are the ones who sent the last message the other user will be the toId guy
        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            whoAreWeTexting = chatMessage.toId
        }else{
            whoAreWeTexting = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$whoAreWeTexting")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                 chatUser = snapshot.getValue(User::class.java) ?: return
                viewHolder.itemView.tv_latest_username.text = chatUser?.username
               Picasso.get().load(chatUser?.profileImageUrl).into(viewHolder.itemView.iv_latestMess_profPic)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        } )

        viewHolder.itemView.tv_text_content.text = chatMessage.text

    }

    override fun getLayout(): Int {

        return R.layout.latest_message_row

    }

}