package com.walkly.walkly.offlineBattle

import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.walkly.walkly.models.Consumable
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.models.Player
import com.walkly.walkly.repositories.ConsumablesRepository
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.coroutines.*
import java.time.temporal.TemporalAmount
import java.util.*

class OfflineBattleViewModel (activity: AppCompatActivity, enemy: Enemy) : ViewModel(){

    private val D_TAG = "offline-battle"

    private val activity = activity

    // used to specify how fact enemy hits
    private val FREQuNCY = 3000L
    // used to convert player level to HP
    private val HP_MULTIPLAYER = 100

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var distanceUtil: DistanceUtil
    val walkedDistance = MutableLiveData<Float>()

    // BAD DESIGN: should get refactored
    private val auth = FirebaseAuth.getInstance()
    private var baseEnemyHP = 0.0
    private var enemyDamage = 0L
    private var currentEnemyHp = 0.0
    private var enemyHpPercentage = 100L
    private var basePlayerHP = 1L
    private var currnetPlayerHP = 0L
    private var playerDamage = 0L

    // view observes these
    val enemyImage = MutableLiveData<String>()
    val enemyHP = MutableLiveData<Long>()
    val playerHP = MutableLiveData<Long>()

    private val _consumables = MutableLiveData<List<Consumable>>()
    val consumables: LiveData<List<Consumable>>
        get() = _consumables

    private val _selectedConsumable = MutableLiveData<Consumable>()
    val selectedConsumable: LiveData<Consumable>
        get() = _selectedConsumable

    init {

        getConsumables()

        // BAD DESIGN: should get refactored
        if (auth.currentUser != null) {

            // get damage player can do based on equipment
            Player.equipment.observe(activity, androidx.lifecycle.Observer {
                it.value.observe(activity, androidx.lifecycle.Observer {value ->
                    playerDamage = value
                })
            })
        }
        // get the starting player HP
        Player.level.observe(activity, androidx.lifecycle.Observer {
            basePlayerHP = it * HP_MULTIPLAYER
            currnetPlayerHP = basePlayerHP
            playerHP.value = (currnetPlayerHP / basePlayerHP) * 100
            Log.d(D_TAG, "it is = " + it)
            Log.d(D_TAG, "base player hp = " + baseEnemyHP)
            Log.d(D_TAG,  "current player hp = " + currnetPlayerHP)
        })
        // get the starting enemy HP
        enemy.HP.observe(activity, androidx.lifecycle.Observer {
            baseEnemyHP = it.toDouble()
            currentEnemyHp = baseEnemyHP
            enemyHP.value = baseEnemyHP.toLong()
        })
        // get enemy image
        enemy.image.observe(activity, androidx.lifecycle.Observer {
            enemyImage.value = it
        })
        // get enemy damage
        enemy.damage.observe(activity, androidx.lifecycle.Observer {
            enemyDamage = it
        })



        // reduce enemy HP by distance walked * equipment value
        walkedDistance.observe(activity, androidx.lifecycle.Observer {
            currentEnemyHp -= it / playerDamage
            enemyHpPercentage = ((currentEnemyHp * 100.0) / baseEnemyHP).toLong()
            enemyHP.value = enemyHpPercentage
            Log.d(D_TAG, "distance = " + it)
        })

        // reduce player HP by time * enemy damage



    }

    fun startBattle(){
        distanceUtil = DistanceUtil(activity, Calendar.getInstance().timeInMillis, 500, walkedDistance)
        distanceUtil.startUpdates()

        scope.launch {
            damagePlayer()
        }
    }

    // WARNING: won't work while screen is off
    suspend fun damagePlayer(){
        var playerHppercentage = 100L
        while (true){
            delay(FREQuNCY)
            Log.d(D_TAG, "current player hp = " + currnetPlayerHP)
            currnetPlayerHP -= enemyDamage
            playerHppercentage = (currnetPlayerHP * 100) / basePlayerHP
//            currnetPlayerHP = ((currnetPlayerHP - enemyDamage) * 100) / basePlayerHP
            playerHP.value = playerHppercentage
        }
    }

    private fun getConsumables() {
        if (_consumables.value != null) {
            _consumables.value = ConsumablesRepository.consumableList
        } else {
            ConsumablesRepository.getConsumables { list ->
                _consumables.value = list
            }
        }
    }

    fun selectConsumable(consumable: Consumable) {
        _selectedConsumable.value = consumable
    }

    fun removeSelectedConsumable() {
        ConsumablesRepository.removeConsumable(selectedConsumable.value!!) { updatedList ->
            _consumables.value = updatedList
        }
    }
}