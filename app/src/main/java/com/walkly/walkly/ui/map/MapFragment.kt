package com.walkly.walkly.ui.map


import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import kotlin.random.Random

class MapFragment : Fragment() {
    lateinit var v : View
    //private lateinit var mapViewModel: MapViewModel
    private lateinit var linearLayout: LinearLayout
    lateinit var viewModel: MapViewModel
    private lateinit var viewModelFactory: MapViewModelFactory
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
        linearLayout = bottom_sheet
        BottomSheetBehavior.from(linearLayout).state = BottomSheetBehavior.STATE_HIDDEN

        viewModelFactory = MapViewModelFactory(this.activity!!)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MapViewModel::class.java)
        viewModel.updateDisplay()
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(viewModel)
    }

}
