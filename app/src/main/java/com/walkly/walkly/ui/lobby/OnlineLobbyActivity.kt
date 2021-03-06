package com.walkly.walkly.ui.lobby

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestoreException
import com.walkly.walkly.R
import com.walkly.walkly.models.BattlePlayer
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.onlineBattle.OnlineBattleActivity
import com.walkly.walkly.ui.profile.*
import kotlinx.android.synthetic.main.activity_online_lobby.*
import kotlinx.android.synthetic.main.dialog_invite_friend.view.*
import kotlinx.android.synthetic.main.dialog_wear_equipment.view.progressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "OnlineLobbyActivity"

class OnlineLobbyActivity : AppCompatActivity(), EquipmentAdapter.OnEquipmentUseListener,
    InviteFriendsAdapter.OnFriendInviteListener {

    private lateinit var wearEquipmentDialog: AlertDialog
    private lateinit var wearEquipmentBuilder: AlertDialog.Builder
    private lateinit var adapter: EquipmentAdapter

    private lateinit var leaveDialog: AlertDialog
    private lateinit var hostLeaveDialog: AlertDialog
    private lateinit var cancelledDialog: AlertDialog

    private lateinit var inviteFriendsDialog: AlertDialog
    private lateinit var inviteFriendsBuilder: AlertDialog.Builder
    private lateinit var inviteFriendsAdapter: InviteFriendsAdapter

    private val viewModel: LobbyViewModel by viewModels()
    private val equipmentViewModel: WearEquipmentViewModel by viewModels()
    private val inviteFriendsViewModel: FriendsViewModel by viewModels()
    private var playerCount: Int = 1
    private var isHost = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // To remove status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_lobby)
        supportActionBar!!.hide()

        val battle = intent.getParcelableExtra<OnlineBattle>("battle")
        battle?.let {
            setupEnemy(battle.enemy!!)
            updatePlayers(battle.players)
            viewModel.setupPlayersListener(battle.id!!)
            playerCount = battle.playerCount!!
            if (battle.playerCount == 1) {
                displayBattleControls()
                isHost = true
            } else {
                displayWaiting()
            }
        }

        start_button.isEnabled = false
        start_button.background.alpha = 100

        viewModel.playerList.observe(this, Observer { list ->
            list?.let {
                Log.d(TAG, "List: $list")
                playerCount = list.size
                if (playerCount > 1) {
                    start_button.isEnabled = true
                    start_button.background.alpha = 255
                }
                updatePlayers(list as MutableList<BattlePlayer>)
            }
        })

        initDialogs()

        // Wear Equipment Dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_wear_equipment, null, false)
        wearEquipmentBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
        wearEquipmentDialog = wearEquipmentBuilder.create()
        //To make the background for the dialog Transparent
        wearEquipmentDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        adapter =
            EquipmentAdapter(mutableListOf(), this)
        val rv = dialogView.findViewById(R.id.equipment_recycler_view) as RecyclerView
        rv.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        rv.adapter = adapter

        dialogView.progressBar.visibility = View.VISIBLE
        equipmentViewModel.equipments.observe(this, Observer { list ->
            dialogView.progressBar.visibility = View.GONE
            adapter.equipmentList = list
            if (list.size < 5) {
                rv.layoutManager =
                    GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
            } else {
                rv.layoutManager =
                    GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)
            }
            adapter.notifyDataSetChanged()
        })

        // Invites only for the host
        if (battle?.playerCount == 1) {
            // Invite Friends Dialog
            val inviteDialog = layoutInflater.inflate(R.layout.dialog_invite_friend, null, false)
            inviteFriendsBuilder = AlertDialog.Builder(this)
                .setView(inviteDialog)
            inviteFriendsDialog = inviteFriendsBuilder.create()
            //To make the background for the dialog Transparent
            inviteFriendsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            inviteFriendsAdapter = InviteFriendsAdapter(mutableListOf(), this)
            val r = inviteDialog.findViewById(R.id.invite_friends_recycler_view) as RecyclerView
            r.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)
            r.adapter = inviteFriendsAdapter

            inviteDialog.progressBar.visibility = View.VISIBLE
            inviteFriendsViewModel.friendsList.observe(this, Observer { list ->
                inviteDialog.progressBar.visibility = View.GONE
                if (list.isEmpty()) {
                    inviteDialog.error_message.visibility = View.VISIBLE
                } else {
                    inviteDialog.error_message.visibility = View.GONE
                    inviteFriendsAdapter.friends = list
                    inviteFriendsAdapter.notifyDataSetChanged()
                }
            })
        }

        viewModel.battleState.observe(this, Observer {
            if (it == "In-game") {
                if (!isHost) {
                    viewModel.currentPlayer.joinBattle()
                }
                val intent = Intent(this, OnlineBattleActivity::class.java)
                intent.putExtra("battle", viewModel.battle)
                startActivity(intent)
                this.finish()
            } else if (it == "Cancelled") {
                cancelledDialog.show()
            }
        })

        start_button.setOnClickListener {
            CoroutineScope(IO).launch {
                try {
                    viewModel.changeBattleState("In-game")
                    // TODO: Display "Game starting in 3.."
                } catch (e: FirebaseFirestoreException) {
                    Log.d(TAG, "Error occurred: ${e.message}")
                }
            }
        }

        btn_change_equipment_lobby.setOnClickListener {
            wearEquipmentDialog.show()
        }

        btn_invite_friends_lobby.setOnClickListener {
            inviteFriendsDialog.show()
        }

        publicize_switch.setOnCheckedChangeListener { _, isChecked ->
            CoroutineScope(IO).launch {
                try {
                    if (isChecked) {
                        viewModel.changeBattlePublicity("public")
                    } else {
                        viewModel.changeBattlePublicity("private")
                    }
                } catch (e: FirebaseFirestoreException) {
                    Log.d(TAG, "Error occurred: ${e.message}")
                }
            }
        }

        btn_leave.setOnClickListener {
            leaveDialog.show()
        }

        host_btn_cancel.setOnClickListener {
            hostLeaveDialog.show()
        }

    }

    private fun displayBattleControls() {
        start_button.visibility = View.VISIBLE
        btn_invite_friends_lobby.visibility = View.VISIBLE
        host_btn_cancel.visibility = View.VISIBLE
        publicize_switch.visibility = View.VISIBLE
        btn_leave.visibility = View.GONE
        loading_bar.visibility = View.GONE
        tv_waiting_lobby.visibility = View.GONE
    }

    private fun displayWaiting() {
        start_button.visibility = View.GONE
        btn_invite_friends_lobby.visibility = View.GONE
        host_btn_cancel.visibility = View.GONE
        publicize_switch.visibility = View.GONE
        loading_bar.visibility = View.VISIBLE
        btn_leave.visibility = View.VISIBLE
        tv_waiting_lobby.visibility = View.VISIBLE
    }

    private fun initDialogs() {
        // Leave Dialog
        val leaveInflater = layoutInflater.inflate(R.layout.dialog_leave_lobby, null)
        leaveDialog = AlertDialog.Builder(this)
            .setView(leaveInflater)
            .create()
        leaveDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        leaveInflater.findViewById<Button>(R.id.btn_leave)
            .setOnClickListener {
                CoroutineScope(IO).launch {
                    viewModel.removeCurrentPlayer()
                    withContext(Main) {
                        leaveDialog.dismiss()
                        finish()
                    }
                }
            }

        leaveInflater.findViewById<Button>(R.id.btn_stay)
            .setOnClickListener {
                leaveDialog.dismiss()
            }


        // Host Cancel Dialog
        val cancelInflater = layoutInflater.inflate(R.layout.dialog_leave_lobby_host, null)
        hostLeaveDialog = AlertDialog.Builder(this)
            .setView(cancelInflater)
            .create()
        hostLeaveDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        cancelInflater.findViewById<Button>(R.id.btn_yes)
            .setOnClickListener {
                CoroutineScope(IO).launch {
                    viewModel.removeListeners()
                    viewModel.cancelBattle()
                    withContext(Main) {
                        hostLeaveDialog.dismiss()
                        finish()
                    }
                }
            }

        cancelInflater.findViewById<Button>(R.id.btn_no)
            .setOnClickListener {
                hostLeaveDialog.dismiss()
            }

        // Battle Cancelled Dialog
        val battleCancelledInflater = layoutInflater.inflate(R.layout.dialog_battle_cancelled, null)
        cancelledDialog = AlertDialog.Builder(this)
            .setView(battleCancelledInflater)
            .create()
        cancelledDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        cancelledDialog.setCancelable(false)
        cancelledDialog.setCanceledOnTouchOutside(false)

        battleCancelledInflater.findViewById<Button>(R.id.btn_leave)
            .setOnClickListener {
                cancelledDialog.dismiss()
                finish()
            }
    }

    private fun setupPlayer(
        player: BattlePlayer? = null,
        nameTextView: TextView,
        avatarImage: ImageView,
        equipmentImage: ImageView
    ) {
        if (player != null) {
            nameTextView.text = player.name

            Glide.with(this)
                .load(player.avatarURL)
                .into(avatarImage)

            equipmentImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(player.equipmentURL)
                .into(equipmentImage)
        } else {
            nameTextView.text = "Waiting"
            avatarImage.setImageResource(R.drawable.ic_account_circle_black_24dp)
            equipmentImage.visibility = View.INVISIBLE
        }
    }

    private fun updatePlayers(players: MutableList<BattlePlayer>) {
        // Set current player to position 0
        for (i in players.indices) {
            if (players[i].id == viewModel.userID) {
                val temp = players[0]
                players[0] = players[i]
                players[i] = temp
            }
        }

        if (playerCount == 4) {
            setupPlayer(
                players[3],
                tv_player3_name_lobby,
                img_player3_avatar_lobby,
                img_player3_equipment_lobby
            )
        } else {
            setupPlayer(
                null,
                tv_player3_name_lobby,
                img_player3_avatar_lobby,
                img_player3_equipment_lobby
            )
        }

        if (playerCount >= 3) {
            setupPlayer(
                players[2],
                tv_player2_name_lobby,
                img_player2_avatar_lobby,
                img_player2_equipment_lobby
            )
        } else {
            setupPlayer(
                null,
                tv_player2_name_lobby,
                img_player2_avatar_lobby,
                img_player2_equipment_lobby
            )
        }

        if (playerCount >= 2) {
            setupPlayer(
                players[1],
                tv_player1_name_lobby,
                img_player1_avatar_lobby,
                img_player1_equipment_lobby
            )
        } else {
            setupPlayer(
                null,
                tv_player1_name_lobby,
                img_player1_avatar_lobby,
                img_player1_equipment_lobby
            )
        }

        if (playerCount >= 1) {
            setupPlayer(
                players[0],
                current_player_name,
                current_player_avatar,
                current_player_equipment_lobby
            )
        }
    }

    private fun setupEnemy(enemy: Enemy) {
        tv_enemy_name.text = enemy.name
        val health = "Health: ${enemy.health}"
        tv_enemy_health.text = health
        val level = "Level: ${enemy.level}"
        tv_enemy_level.text = level

        Glide.with(this)
            .asGif()
            .load(enemy.image)
            .into(enemy_image)
    }

    override fun onBackPressed() {
        if (isHost) {
            hostLeaveDialog.show()
        } else {
            leaveDialog.show()
        }
    }

    override fun onEquipmentClick(position: Int) {
        val equipment = adapter.equipmentList[position]
        equipmentViewModel.selectEquipment(equipment)
        CoroutineScope(IO).launch {
            viewModel.changeEquipment(equipment)
            withContext(Main) { wearEquipmentDialog.dismiss() }
        }
    }

    override fun onFriendInviteClick(position: Int) {
        val friend = inviteFriendsAdapter.friends[position]
        CoroutineScope(IO).launch {
            viewModel.inviteFriend(friend.id)
//            withContext(Main) { inviteFriendsDialog.dismiss() }
        }
    }
}
