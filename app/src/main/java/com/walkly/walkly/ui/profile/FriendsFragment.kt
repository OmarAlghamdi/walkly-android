package com.walkly.walkly.ui.profile

import android.opengl.Visibility
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.walkly.walkly.R
import com.walkly.walkly.models.Friend
import kotlinx.android.synthetic.main.list_friend.*

private const val TAG = "FriendsFragment"

class FriendsFragment : Fragment() {

    // Imported db stuff for the onClick method, this should be moved outside the fragment
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid

    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var searchField: EditText
    private lateinit var errorMessage: TextView

    private var adapter: FriendsAdapter? = null

    private val friendsViewModel: FriendsViewModel by lazy {
        ViewModelProviders.of(this).get(FriendsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)

        searchField = view.findViewById(R.id.et_search)

        errorMessage = view.findViewById(R.id.error_no_user_found)
        errorMessage.visibility = View.GONE

        friendsViewModel.friendsList.observe(this, Observer { list ->
            list?.let {
                if (list.isEmpty()) {
                    errorMessage.text = getString(R.string.no_friends)
                    errorMessage.visibility = View.VISIBLE
                } else {
                    errorMessage.visibility = View.GONE
                    adapter = FriendsAdapter(list)
                    friendsRecyclerView.adapter = adapter
                }
            }
        })

        friendsViewModel.searchList.observe(this, Observer {list ->
            list?.let {
                if (list.isEmpty()) {
                    errorMessage.text = getString(R.string.no_user_found)
                    errorMessage.visibility = View.VISIBLE
                } else {
                    errorMessage.visibility = View.GONE
                    adapter = FriendsAdapter(list)
                    friendsRecyclerView.adapter = adapter
                }
            }
        })

        searchField.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    val userName = v.text.toString()
                    if (userName.isNotBlank()) {
                        friendsViewModel.searchUser(userName)
                    }  else {
                        friendsViewModel.getFriends()
                    }

                    true
                } else -> false
            }
        }

        return view
    }

    private inner class FriendsHolder(view: View): RecyclerView.ViewHolder(view) {
        val friendName: TextView = itemView.findViewById(R.id.tv_username)
        val friendLevel: TextView = itemView.findViewById(R.id.tv_level)
        val friendPoints: TextView = itemView.findViewById(R.id.tv_points)
        val friendImage: ImageView = itemView.findViewById(R.id.img_avatar)
        val btnAccept: Button = itemView.findViewById(R.id.btn_accept)
        val btnReject: Button = itemView.findViewById(R.id.btn_reject)
        val btnAdd: Button = itemView.findViewById(R.id.btn_add)


    }

    private inner class FriendsAdapter(var friends: List<Friend>): RecyclerView.Adapter<FriendsHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsHolder {
            val view = layoutInflater.inflate(R.layout.list_friend, parent, false)
            return FriendsHolder(view)
        }

        override fun getItemCount(): Int {
            return friends.size
        }

        override fun onBindViewHolder(holder: FriendsHolder, position: Int) {
            val friend = friends[position]
            holder.apply {
                // Default image
                friendImage.setImageResource(R.drawable.fui_ic_anonymous_white_24dp)
                friendName.text = friend.name
                friendLevel.text = "Level: " + friend.level.toString()
                friendsViewModel.addFriend(userID!!, friend.id, friend.status, btnAdd, btnAccept, btnReject)
            }
        }

    }


}
