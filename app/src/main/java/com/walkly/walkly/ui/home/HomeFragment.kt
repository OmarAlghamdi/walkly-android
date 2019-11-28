package com.walkly.walkly.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.walkly.walkly.LoginActivity
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import kotlinx.android.synthetic.main.fragment_home.*


@SuppressLint("Registered")
class HomeFragment : Fragment(), View.OnClickListener {
    lateinit var v: View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_home, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signOutButton.setOnClickListener(this)
        verifyEmailButton.setOnClickListener(this)
        viewLeaderboard.setOnClickListener {
            view.findNavController().navigate(R.id.action_navigation_home_to_leaderboardFragment)
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.signOutButton -> signOut()
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        updateUI()
    }

    private fun updateUI() {

        var intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)

    }

}