package com.scollon.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.scollon.chattingapp.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row.view.*


class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        fetchUsers()

    }
    private fun fetchUsers(){
      val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
/*
* this only works if there are not too many users
* if there would be a lot of them you would only show 10 or 20 users and change the shown users
* by the searching criteria  aka username
* so you'd iterate witch given limit and not through the whole dataBase
*/
                val adapter = GroupieAdapter()
                snapshot.children.forEach {

                    val user = it.getValue(User::class.java)

                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                }

                    adapter.setOnItemClickListener { item, view ->
                        val userItem = item as UserItem
                        val i = Intent(view.context, ChatActivity::class.java)
                        i.putExtra("user", userItem.user)
                        startActivity(i)
                }
                recyclerView_newMessage.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.tv_userRow_userName.text = user.username

        val uri = user.profileImageUrl

        Picasso.get().load(uri).into(viewHolder.itemView.iv_userRow_profilePic)
    }

    override fun getLayout(): Int {
return R.layout.user_row
    }

}