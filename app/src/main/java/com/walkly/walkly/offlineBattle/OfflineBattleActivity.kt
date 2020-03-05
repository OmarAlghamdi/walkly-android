package com.walkly.walkly.offlineBattle

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.Player
import com.walkly.walkly.models.Player.getReward
import kotlinx.android.synthetic.main.fragment_battle_activity.*

class OfflineBattle : AppCompatActivity() {

    lateinit var viewModel: OfflineBattleViewModel
    private lateinit var viewModelFactory: OfflineBattleViewModelFactory

    private lateinit var loseDialog: AlertDialog
    private lateinit var leaveDialog: AlertDialog
    private lateinit var winDialog: AlertDialog

    private lateinit var consumablesBottomSheetDialog: ConsumablesBottomSheetDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.fragment_battle_activity)

        val bundle = intent.extras
        val enemy = Enemy( bundle.getLong("playerLlevel"), bundle.getString("enemyId"), bundle.getLong("enemyHP") , bundle.getLong("enemyDmg") )

        consumablesBottomSheetDialog =
            ConsumablesBottomSheetDialog(this)


        viewModelFactory = OfflineBattleViewModelFactory(this, enemy)

//        viewModel = OfflineBattleViewModel(this, enemy)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(OfflineBattleViewModel::class.java)

        use_items.setOnClickListener {
            //            Toast.makeText(this, "coming in 418", Toast.LENGTH_LONG)
//                .show()
            consumablesBottomSheetDialog.show(supportFragmentManager, "consumableSheet")
        }

        viewModel.selectedConsumable.observe(this, Observer {
            useConsumable(it.type, it.value)
            viewModel.removeSelectedConsumable()
        })



        viewModel.playerHP.observe(this, Observer {
            player_health_bar.progress = it.toInt()
            if (it <= 0) {
                loseDialog.show()
                loseDialog.findViewById<Button>(R.id.button1)
                    .setOnClickListener {
                        endGame()
                    }
            }
        })

        viewModel.enemyHP.observe(this, Observer {
            enemy_health_bar.progress = it.toInt()
            if (it <= 0) {
                enemy.level.value?.toInt()?.let { it1 -> Player.updatePoints(it1) }
                getReward()
                winDialog.show()
                winDialog.findViewById<Button>(R.id.btn_collect)
                    .setOnClickListener {
                        endGame()
                    }
            }
        })

        viewModel.enemyImage.observe(this, Observer {
            Glide.with(this)
                .load(it)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(boss_bitmoji)
        })

        // NOTE TESTED
        // Since starting time is now where the walked distance = 0
        viewModel.walkedDistance.observe(this, Observer {
            player_step_bar.progress += it.toInt()
            Log.d("steps = ", it.toString())
        })

        leaveBattle.setOnClickListener {
            leaveDialog.show()
            leaveDialog.findViewById<Button>(R.id.btn_leave)
                .setOnClickListener {
                    endGame()
                }
            leaveDialog.findViewById<Button>(R.id.btn_stay)
                .setOnClickListener {
                    leaveDialog.dismiss()
                }
        }

        loseDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_battle_lost)
            .create()

        leaveDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_battle_leave)
            .create()

        // TODO: construct dialog based real reward

        winDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_battle_won)
            .create()

        viewModel.startBattle()
    }

    private fun getReward() {

    }

    fun endGame() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun useConsumable(consumableType: String, consumableValue: Int) {
        when (consumableType.toLowerCase()) {
            "attack" -> enemy_health_bar.progress -= consumableValue
            "health" -> player_health_bar.progress += consumableValue
        }
    }
}