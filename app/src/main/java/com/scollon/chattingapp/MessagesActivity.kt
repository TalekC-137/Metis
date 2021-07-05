package com.scollon.chattingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.scollon.chattingapp.models.ChatMessage
import com.scollon.chattingapp.models.PostModel
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

    var mailVerif: Boolean = false
    var globalUsername:String? = "guest"
    val adapter = GroupieAdapter()
    val postAdapter = GroupieAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(1).isEnabled = false


        bottomNavigationView.setOnNavigationItemSelectedListener {
            menuItem ->
            when (menuItem.itemId) {

                R.id.menu_bottom_Profile -> {
                   rv_latest_messages.visibility = View.GONE
                    layout_profile.visibility = View.VISIBLE
                   listenFetchPosts()

                }
                R.id.menu_bottom_home -> {
                    rv_latest_messages.visibility = View.VISIBLE
                    layout_profile.visibility = View.GONE
                }

            }

            true
        }


        auth = Firebase.auth //I know I have two of these

        getCurrentUser()
        listenForLatestMessages()
        //the main "home" layout with the lates messages sent recyclerView

        rv_latest_messages.adapter = adapter
        rv_currentUser_posts.adapter = postAdapter

        rv_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->

            val i = Intent(this, ChatActivity::class.java)
            val userRow = item as LatestMessageRow
            i.putExtra("user", userRow.chatUser)
            startActivity(i)

        }

        tv_profile_username.setOnClickListener {
            et_editUsername.visibility = View.VISIBLE
            btn_editUsername.visibility = View.VISIBLE
            tv_profile_username.visibility = View.GONE
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
        fab.setOnClickListener{
            val i = Intent(this, NewMessageActivity::class.java)
            startActivity(i)
        }
        btn_post.setOnClickListener {
            postApost()
        }

        btn_editUsername.setOnClickListener {

            et_editUsername.visibility = View.GONE
            btn_editUsername.visibility = View.GONE
            tv_profile_username.visibility = View.VISIBLE

            val newUsername = et_editUsername.text.toString()
            val actualCurrentUser = auth.currentUser?.uid
            val ref = FirebaseDatabase.getInstance().getReference("users/$actualCurrentUser/username")

            if(!newUsername.isEmpty()) {
                ref.setValue(newUsername)
                tv_profile_username.text = newUsername
            }
        }


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

   private fun confirmVerification(){

       val user = FirebaseAuth.getInstance().currentUser
       user?.reload()

       if (user!!.isEmailVerified) {
           mailVerif = true
           fab.visibility = View.VISIBLE
           unverifiedLayout.visibility = View.GONE
           Log.d("verification", "Email is verified.")
       } else {
           mailVerif = false
           fab.visibility = View.GONE
           unverifiedLayout.visibility = View.VISIBLE
           mail.text = user.email
           Log.d("verification", "Email is not verified !.")

       }
   }


    fun postApost(){
        val postsText = et_post.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        if(postsText.isEmpty()) return
        if(fromId == null) return

        val ref = FirebaseDatabase.getInstance().getReference("/user-posts/$fromId").push()

        val userPost = PostModel(ref.key!!, fromId, postsText, System.currentTimeMillis())

        ref.setValue(userPost).addOnSuccessListener {
            Log.d("posting", "post has been added")
        }

        et_post.text.clear()

    }

    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerView(){
        adapter.clear()
        var i = 0
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
            i++
            if(latestMessagesMap.size == i){
                pB_mess.visibility = View.GONE
            }

        }

    }

    private fun listenForLatestMessages(){
        val currentUserId = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$currentUserId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                pB_mess.visibility = View.VISIBLE

                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()



            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                pB_mess.visibility = View.VISIBLE
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


    fun listenFetchPosts(){

            Log.d(TAG, "listener engaged")

            var fromId = currentUser?.uid

            Log.d(TAG, "listening for messages from $fromId")

            val ref = FirebaseDatabase.getInstance().getReference("/user-posts/$fromId")

            ref.addChildEventListener(object: ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val post = snapshot.getValue(PostModel::class.java)
                    if(post != null){
                        postAdapter.add(UserPosts(currentUser!!, post))
                        Log.d(TAG, "adding a post to the adapter")
                        postAdapter.add(Spacer())
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

        }
        return super.onOptionsItemSelected(item)
    }






    private fun getCurrentUser(){
        val currentUsersUid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$currentUsersUid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                currentUser = snapshot.getValue(User::class.java)

                tv_profile_username.text = currentUser?.username

                tv_profile_email.text = auth.currentUser?.email

                Picasso.get().load(currentUser?.profileImageUrl).into(iV_profile_prof)

                globalUsername = currentUser?.username

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

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


                if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                    viewHolder.itemView.tv_text_content.text = "you: " + chatMessage.text
                }else{
                    viewHolder.itemView.tv_text_content.text = chatMessage.text
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        } )



    }

    override fun getLayout(): Int {

        return R.layout.latest_message_row

    }

}