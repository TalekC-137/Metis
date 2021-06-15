package com.scollon.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.scollon.chattingapp.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_left_sender_row.view.*

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

         val user = intent.getParcelableExtra<User>("user")
        val userName = user?.username
         supportActionBar?.title = userName

        val adapter = GroupieAdapter()
        adapter.add(ChatLeftItem(user))
        adapter.add(ChatRightItem())
        adapter.add(ChatLeftItem(user))
        adapter.add(ChatRightItem())
        rv_chat.adapter = adapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
        var i = Intent(this, MessagesActivity::class.java)
        startActivity(i)
    }

}

class ChatLeftItem(val user: User?): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val uri = user?.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.iv_leftProf)

    }

    override fun getLayout(): Int {
        return R.layout.chat_left_sender_row
    }

}
class ChatRightItem: Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.chat_right_sender_row
    }

}