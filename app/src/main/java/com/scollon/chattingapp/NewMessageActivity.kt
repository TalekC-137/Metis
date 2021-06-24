package com.scollon.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import org.w3c.dom.Text


class NewMessageActivity : AppCompatActivity() {

    var str:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        fetchUsers()


     et_search.addTextChangedListener(object: TextWatcher {
         override fun afterTextChanged(s: Editable?) {
         }
         override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
         }
         override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

             if(et_search.text.isEmpty()){
                 fetchUsers()
             }else{
                 str = s.toString();
                 fetchUsers()
             }
         }
     })
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
                    var name: String = user?.username ?: ""
                    if(str == "" || str == null){

                        if (user != null) {
                        adapter.add(UserItem(user))
                             }
                        // takes the text from the search bar and compares it to the firebase users
                        // if their name contains (not only starts but contains in general) it shows them
                        // if the search bar is empty it shows all of them
                    }else if(name.contains(str, ignoreCase = true)){
                        if (user != null) {
                            adapter.add(UserItem(user))
                        }
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