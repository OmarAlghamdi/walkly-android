package com.walkly.walkly.ui.map


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.JsonObject
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.walkly.walkly.R
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.Enemy.Companion.generateRandomEnemies
import com.walkly.walkly.models.Player
import com.walkly.walkly.offlineBattle.OfflineBattle
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlin.random.Random

class MapFragment : Fragment(), OnMapReadyCallback, PermissionsListener {
    lateinit var v : View
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapViewModel: MapViewModel
    private lateinit var linearLayout: LinearLayout
    private lateinit var mapboxMap: MapboxMap
    private lateinit var  symbol1: Symbol
    private lateinit var  symbol2: Symbol
    private lateinit var  symbol3: Symbol
    private lateinit var  camera: LatLng
    private lateinit var  enemies: Array<Enemy>
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!.applicationContext, getString(R.string.access_token))
        v =  inflater.inflate(R.layout.fragment_map, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       updateDisplay()

        linearLayout = bottom_sheet
        //hide the bottom sheet
        BottomSheetBehavior.from(linearLayout).state = BottomSheetBehavior.STATE_HIDDEN
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }
    override fun onMapReady(mapboxMap: MapboxMap) {
        enemies = generateRandomEnemies(Player.level.value!!)
        this.mapboxMap = mapboxMap
        mapboxMap.uiSettings.isLogoEnabled = false
        mapboxMap.uiSettings.isZoomGesturesEnabled = false
        mapboxMap.uiSettings.isQuickZoomGesturesEnabled = false
        mapboxMap.uiSettings.isScrollGesturesEnabled = false
        mapboxMap.setStyle(Style.Builder().fromUri("mapbox://styles/mapbox/cjerxnqt3cgvp2rmyuxbeqme7"))
        {
            enableLocationComponent(it)
            val symbolManager = SymbolManager(mapView, mapboxMap, it)
            symbolManager.iconAllowOverlap = true
            camera = mapboxMap.cameraPosition.target

            mHandler = Handler()
            mRunnable = Runnable {
                symbolManager.deleteAll()

                camera = mapboxMap.cameraPosition.target
                var enemy_1_json = JsonObject()
                enemy_1_json.addProperty("num",0)
                var enemy_2_json = JsonObject()
                enemy_2_json.addProperty("num",1)
                var enemy_3_json = JsonObject()
                enemy_3_json.addProperty("num",2)
                symbol1 = symbolManager.create(
                    SymbolOptions()
                        .withData(enemy_1_json)
                        .withLatLng(LatLng(camera.latitude+ getRandomLat(), camera.longitude+getRandomLat()))
                        .withIconImage("zoo-15")
                        .withIconSize(2.5f))

                symbol2 = symbolManager.create(
                    SymbolOptions()
                        .withData(enemy_2_json)
                        .withLatLng(LatLng(camera.latitude+getRandomLat(), camera.longitude))
                        .withIconImage("fire-station-15")
                        .withIconSize(2.5f))

                symbol3 = symbolManager.create(
                    SymbolOptions()
                        .withData(enemy_3_json)
                        .withLatLng(LatLng(camera.latitude, camera.longitude+getRandomLat()))
                        .withIconImage("rocket-15")
                        .withIconSize(2.5f))

                mHandler.postDelayed(
                    mRunnable, // Runnable
                    60000 // Delay in milliseconds
                )
            }
            mHandler.postDelayed(
                mRunnable, // Runnable
                0 // Delay in milliseconds
            )

            symbolManager?.addClickListener { symbol ->
                var enemy_num = symbol.data!!.asJsonObject.get("num").asInt
                var curen = enemies[enemy_num]
                join_button.setOnClickListener {

                    // decreasing energy on battle join
                    Player.joinedBattle()
                    val intent = Intent(activity, OfflineBattle::class.java)
                    val bundle = Bundle()
                    bundle.putString("enemyId", curen.id.value)
                    bundle.putLong("enemyHP", curen.HP.value!!)
                    bundle.putLong("enemyDmg", curen.damage.value!!)
                    bundle.putString("enemyLvl", curen.level.value.toString())
                    intent.putExtras(bundle)
                    startActivity(intent)
                    activity?.finish()
                }
                curen.name.observe(this, Observer {
                    bottom_sheet_text.setText(it.toString())
                })
                curen.level.observe(this, Observer {
                    bottom_sheet_lvl.setText("Level: "+ it.toString())
                })
                curen.HP.observe(this, Observer {
                    bottom_sheet_health.setText("HP: "+it.toString())
                })

                curen.image.observe(this, Observer{
                    Glide.with(this)
                        .load(it)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(bossgif)
                    Toast.makeText(this.context, it, Toast.LENGTH_LONG).show()

                })

                BottomSheetBehavior.from(linearLayout).state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

    }

    private  fun updateDisplay(){

        Player.level.observe(this, Observer {
            user_level.text = "LEVEL $it"
        })

        Player.progress.observe(this, Observer {
            progressBar2.progress = it.toInt()
        })

        Player.stamina.observe(this, Observer {
            Log.d("stamina from map2", it.toString())
            join_button.isClickable = true
            join_button.background.alpha = 255

            if(it >= 300){
                //3 balls
                stamina1full.visibility = View.VISIBLE
                stamina2full.visibility = View.VISIBLE
                stamina3full.visibility = View.VISIBLE

            }else if(it >= 200 ){
                //2 balls
                stamina1full.visibility = View.VISIBLE
                stamina2full.visibility = View.VISIBLE
                stamina3full.visibility = View.INVISIBLE

            }else if(it >= 100){
                //1 ball
                stamina1full.visibility = View.VISIBLE
                stamina2full.visibility = View.INVISIBLE
                stamina3full.visibility = View.INVISIBLE

            }else{
                //no balls
                stamina1full.visibility = View.INVISIBLE
                stamina2full.visibility = View.INVISIBLE
                stamina3full.visibility = View.INVISIBLE

                // player cannot join a battle
                join_button.isClickable = false
                join_button.background.alpha = 100
            }

        })
    }

    private fun getRandomLat(): Double{
        return (Random.nextInt(-200, 200).toDouble() /100000) + 0.001
    }
    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(activity!!.applicationContext)) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(activity!!.applicationContext)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(activity!!.applicationContext, R.color.colorPrimary))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(activity!!.applicationContext, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(activity!!)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
       // Toast.makeText(this, "NOOOOOOOOOOOOOOOO", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
          Toast.makeText(activity!!.applicationContext, "YESSSSS", Toast.LENGTH_LONG).show()
          //finish()
        }
    }


}
