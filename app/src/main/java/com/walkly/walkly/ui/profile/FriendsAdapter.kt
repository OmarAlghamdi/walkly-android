package com.walkly.walkly.ui.profile

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walkly.walkly.R
import com.walkly.walkly.models.Friend
import com.walkly.walkly.repositories.FriendsRepository
import kotlinx.android.synthetic.main.list_friend.*
import kotlinx.android.synthetic.main.list_friend.view.*
import kotlinx.android.synthetic.main.list_friend_pending.view.*
import kotlinx.android.synthetic.main.list_user.view.*
import java.lang.Exception

private const val TAG = "FriendAdapter"


class FriendsAdapter(var friends: List<Friend>, val navController: NavController) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> PendingFriendViewHolder(
                inflater.inflate(
                    R.layout.list_friend_pending,
                    parent,
                    false
                )
            )
            1 -> FriendViewHolder(
                inflater.inflate(R.layout.list_friend, parent, false),
                navController
            )
            else -> UserViewHolder(inflater.inflate(R.layout.list_user, parent, false))
        }

    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val friend = friends[position]
        (holder as BindibleViewHolder).bind(friend)
    }

    override fun getItemViewType(position: Int): Int {
        return friends[position].type
    }
}

interface BindibleViewHolder {
    fun bind(friend: Friend)
}

class FriendViewHolder(view: View, val navController: NavController) :
    RecyclerView.ViewHolder(view), BindibleViewHolder {
    private val name: TextView = view.tv_username_1
    private val level: TextView = view.tv_level_1
    private val avatar: ImageView = view.img_avatar_1
    private val chat: Button = view.btn_chat


    override fun bind(friend: Friend) {
        chat.setOnClickListener {
            navController.navigate(
                FriendsFragmentDirections.actionFriendsFragmentToChatFragment(
                    friend.id
                )
            )
        }
        name.text = friend.name
        level.text = "Level ${friend.level}"
        try {
            Glide.with(this.itemView)
                .load(friend.photoURL)
                .into(avatar)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to load avatar")
        }
    }

}

class PendingFriendViewHolder(val view: View) : RecyclerView.ViewHolder(view), BindibleViewHolder {
    private val name: TextView = view.tv_username_0
    private val level: TextView = view.tv_level_0
    private val avatar: ImageView = view.img_avatar_0
    private val accept: Button = view.btn_accept
    private val reject: Button = view.btn_reject

    override fun bind(friend: Friend) {
        accept.setOnClickListener {
            FriendsRepository.acceptFriend(friend.id) {
                if (it) {
                    // TODO: change the view holder to FriendViewHolder
                    Toast.makeText(
                        view.context,
                        "You are now friend with ${friend.name}",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
        reject.setOnClickListener {
            FriendsRepository.rejectFriend(friend.id) {
                if (it) {
                    // TODO: remove friend from adapter list
                    Toast.makeText(view.context, "Do not be a sociopath", Toast.LENGTH_LONG)
                        .show()

                }
            }
        }
        name.text = friend.name
        level.text = "Level ${friend.level}"
        try {
            Glide.with(this.itemView)
                .load(friend.photoURL)
                .into(avatar)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to load avatar")
        }
    }
}

class UserViewHolder(val view: View) : RecyclerView.ViewHolder(view), BindibleViewHolder {
    private val name: TextView = view.tv_username_2
    private val level: TextView = view.tv_level_2
    private val avatar: ImageView = view.img_avatar_2
    private val addFriend: Button = view.btn_add

    override fun bind(friend: Friend) {
        addFriend.setOnClickListener {
            FriendsRepository.addFriend(friend.id) {
                if (it) {
                    // TODO: change the view holder to FriendViewHolder
                    Toast.makeText(
                        view.context,
                        "Friend was added successfully\nwait for their acceptance",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
        name.text = friend.name
        level.text = "Level ${friend.level}"
        try {
            Glide.with(this.itemView)
                .load(friend.photoURL)
                .into(avatar)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to load avatar")
        }
    }
}
