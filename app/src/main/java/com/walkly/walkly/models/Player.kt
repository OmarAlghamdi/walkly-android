package com.walkly.walkly.models

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.tasks.await
import kotlin.math.floor
import kotlin.math.sqrt

private const val TAG = "class Player"

// amount of points player gets for defeating enemy
private const val ENEMY_LEVEL_POINTS = 10L

@Parcelize
data class Player(
    var name: String? = "2Lazy4u",
    var email: String? = "lazy@email.com",
    var currentEquipment: Equipment? = null,
    var currentHP: Long? = 100L,
    var level: Long? = 1,
    var stamina: Long? = 300L,
    var points: Long? = 0,
    var progress: Long? = 0,
    var lastUpdate: String? = null,
    var photoURL: String? = null,
    var deviceToken: String = "",
    var CompletedQuests: List<String> = listOf(),
    var steps: Long = 0,
    var friends: List<String>? = listOf(),
    var friendRequests: List<String>? = listOf()
) : Parcelable {

    @IgnoredOnParcel
    @get:Exclude
    var id: String? = null

    fun joinBattle() {
        stamina = stamina?.minus(100)
    }

    fun wearEquipment(equipment: Equipment) {
        currentEquipment = equipment
    }
}