package com.walkly.walkly.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.walkly.walkly.LoginActivity
import com.walkly.walkly.R
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment(), View.OnClickListener {

    private lateinit var profileViewModel: ProfileViewModel
    lateinit var v : View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v =  inflater.inflate(R.layout.fragment_profile, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signOutButton.setOnClickListener(this)
        verifyEmailButton.setOnClickListener(this)
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