package com.walkly.walkly.pvp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.models.PVPBattle
import com.walkly.walkly.ui.consumables.ConsumablesBottomSheetDialog
import com.walkly.walkly.ui.consumables.ConsumablesViewModel
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.android.synthetic.main.activity_pvp_battle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "PVPActivity"

class PVPActivity : AppCompatActivity() {

    private val viewModel: PVPViewModel by viewModels()
    private val consumablesViewModel: ConsumablesViewModel by viewModels()

    private lateinit var loseDialog: AlertDialog
    private lateinit var leaveDialog: AlertDialog
    private lateinit var winDialog: AlertDialog

    private lateinit var consumablesBottomSheetDialog: ConsumablesBottomSheetDialog

    private val walkedDistance = MutableLiveData<Float>()
    private lateinit var distanceUtil: DistanceUtil
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var winner: String = ""
    private lateinit var battle: PVPBattle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pvp_battle)
        supportActionBar?.hide()

        initDialogs()
        initConsumables()

        battle = intent.getParcelableExtra("battle")!!

        distanceUtil = DistanceUtil(this, walkedDistance)

        viewModel.hostHP.observe(this, Observer {
            bar_host_hp.progress = it
            if (it <= 0) {
                if (battle.host?.id == viewModel.userID) {
                    loseDialog.show()
                } else {
                    winDialog.show()
                }
            }
        })

        viewModel.opponentHP.observe(this, Observer {
            bar_opponent_hp.progress = it
            if (it <= 0) {
                if (battle.host?.id == viewModel.userID) {
                    winDialog.show()
                } else {
                    loseDialog.show()
                }
            }
        })

        walkedDistance.observe(this, Observer {
            it?.let {
                scope.launch {
                    if (battle.host?.id == viewModel.userID) {
                        viewModel.damageOpponent(it.toLong())
                    } else {
                        viewModel.damageHost(it.toLong())
                    }
                }
            }
        })

        battle.let {
            setupPlayers(battle)
            viewModel.battleID = battle.id
            viewModel.setupHealthListeners(battle.host, battle.opponent)
        }
    }

    private fun setupPlayers(battle: PVPBattle) {
        tv_host_name.text = battle.host?.name
        var hpText = "${battle.host?.name?.trim()}'s HP"
        tv_host_hp.text = hpText

        Glide.with(this)
            .load(battle.host?.avatarURL)
            .into(img_pvp_host_avatar)

        Glide.with(this)
            .load(battle.host?.equipmentURL)
            .into(img_pvp_host_equipment)

        tv_opponent_name.text = battle.opponent?.name
        hpText = "${battle.opponent?.name?.trim()}'s HP"
        tv_opponent_hp.text = hpText

        Glide.with(this)
            .load(battle.opponent?.avatarURL)
            .into(img_pvp_opponent_avatar)

        Glide.with(this)
            .load(battle.opponent?.equipmentURL)
            .into(img_pvp_opponent_equipment)

    }

    private fun initDialogs() {
        // Leave Dialog
//        val leaveInflater = layoutInflater.inflate(R.layout.dialog_battle_leave, null)
//        leaveDialog = AlertDialog.Builder(this)
//            .setView(leaveInflater)
//            .create()
//        leaveInflater.findViewById<Button>(R.id.btn_leave)
//            .setOnClickListener {
//                leaveDialog.dismiss()
////                viewModel.battleEnded = true
//                endGame()
//            }
//        leaveInflater.findViewById<Button>(R.id.btn_stay)
//            .setOnClickListener {
//                leaveDialog.dismiss()
//            }

        // Win Dialog
        val winInflater = layoutInflater.inflate(R.layout.dialog_battle_won, null)
        winDialog = AlertDialog.Builder(this)
            .setView(winInflater)
            .create()

        winInflater.findViewById<Button>(R.id.btn_collect)
            .setOnClickListener {
                winDialog.dismiss()
                endGame()
            }

        winDialog.setCancelable(false)
        winDialog.setCanceledOnTouchOutside(false)

        // Lose Dialog
        val loseInflater = layoutInflater.inflate(R.layout.dialog_battle_lost, null)
        loseDialog = AlertDialog.Builder(this)
            .setView(loseInflater)
            .create()

        loseInflater.findViewById<Button>(R.id.go_home)
            .setOnClickListener {
                loseDialog.dismiss()
                endGame()
            }

        loseDialog.setCancelable(false)
        loseDialog.setCanceledOnTouchOutside(false)

    }

    private fun initConsumables() {
        consumablesBottomSheetDialog = ConsumablesBottomSheetDialog(this)
        consumablesViewModel.selectedConsumable.observe(this, Observer {
            if (battle.host?.id == viewModel.userID) {
                viewModel.useHostConsumable(it.type, it.value)
            } else {
                viewModel.useOpponentConsumable(it.type, it.value)
            }

            consumablesViewModel.removeSelectedConsumable()
        })

        pvp_use_item.setOnClickListener {
            consumablesBottomSheetDialog.show(
                supportFragmentManager,
                ConsumablesBottomSheetDialog.TAG
            )
        }
    }

    private fun endGame() {
        val intent = Intent(this, MainActivity::class.java)
        viewModel.stopGame()
        startActivity(intent)
        finish()
    }
}