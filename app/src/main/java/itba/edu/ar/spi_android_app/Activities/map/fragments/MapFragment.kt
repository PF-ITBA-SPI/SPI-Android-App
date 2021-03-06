package ar.edu.itba.spi_android_app.Activities.map.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ar.edu.itba.spi_android_app.Activities.map.MapViewModel
import ar.edu.itba.spi_android_app.R
import ar.edu.itba.spi_android_app.api.ApiSingleton
import ar.edu.itba.spi_android_app.api.clients.BuildingsClient
import ar.edu.itba.spi_android_app.api.models.Building
import ar.edu.itba.spi_android_app.api.models.Floor
import ar.edu.itba.spi_android_app.api.models.Sample
import ar.edu.itba.spi_android_app.utils.TAG
import ar.edu.itba.spi_android_app.utils.gMapsGroundOverlayOptions
import ar.edu.itba.spi_android_app.utils.scanResultToFingerprint
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import itba.edu.ar.spi_android_app.Activities.scan.ScanService
import itba.edu.ar.spi_android_app.api.clients.LocationClient

/**
 * Main positioning fragment.  Includes a Google Maps fragment, a [FloorSelectorFragment] to
 * manually change floors, and a [StatusIndicatorFragment].
 *
 * - Activities that contain this mapFragment must implement the
 * [MapFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * - Use the [MapFragment.newInstance] factory method to
 * create an instance of this mapFragment.
 *
 */
class MapFragment : Fragment(), GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, GoogleMap.OnIndoorStateChangeListener {
    // TODO: Rename and change types of parameters
    private val RequestFineLocationPermission = 42

    private var listener: OnFragmentInteractionListener? = null
    private var map: GoogleMap? = null
    private lateinit var model: MapViewModel

    private var buildingsDisposable: Disposable? = null
    private var myLocationDisposable: Disposable? = null

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var floorSelectorFragment: FloorSelectorFragment
    private lateinit var statusIndicatorFragment: StatusIndicatorFragment

    private val groundOverlays = HashMap<String, GroundOverlay>()
    private var activeGroundOverlay: GroundOverlay? = null

    private var samples: MutableCollection<Sample> = mutableListOf()
    private var marker: Marker? = null

    private lateinit var scanService: ScanService
    private lateinit var liveScanResults: MutableLiveData<List<ScanResult>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Started map super-fragment")

        if (activity != null) {
            scanService = ScanService(activity as Activity)
            liveScanResults = scanService.getLiveResults()
        }

        // Get shared view-model
        model = activity?.run {
            ViewModelProviders.of(this).get(MapViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        val buildingsClient = ApiSingleton.getInstance(this.activity as AppCompatActivity).defaultRetrofitInstance.create(BuildingsClient::class.java)
        val myLocationClient = ApiSingleton.getInstance(this.activity as AppCompatActivity).defaultRetrofitInstance.create(LocationClient::class.java)

        buildingsDisposable = buildingsClient
                .list()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { Log.d(TAG, "GET /buildings") }
                .subscribe(
                        { result ->
                            model.buildings.value = result
                            scanService.startScanning()
                        },
                        { error -> Log.e(TAG, error.message) }
                )

        liveScanResults.observe(this, Observer<List<ScanResult>> { newResults ->
            if (newResults != null && newResults.isNotEmpty()) {
                val fingerprint = scanResultToFingerprint(newResults)
                myLocationDisposable = myLocationClient
                        .queryLocation(fingerprint)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { Log.d(TAG, "Querying location, scan results: $fingerprint") }
                        .subscribe(
                                // TODO update user's location building and floor from result
                                { result ->
                                    // Location
                                    if (result.latitude != null && result.longitude != null) {
                                        val oldV = model.location.value
                                        val newV = LatLng(result.latitude!!, result.longitude!!)
                                        if (newV != oldV) {
                                            model.location.value = newV
                                            Log.d(TAG, "Updated location from $oldV to $newV")
                                        } else {
                                            Log.d(TAG, "Location unchanged, skipping update")
                                        }
                                    } else {
                                        Log.d(TAG, "Latitude and/or longitude not returned, skipping update")
                                    }

                                    // Building
                                    if (result.buildingId != null && model.buildings.value?.isEmpty() == false) {
                                        val oldV = model.locatedBuilding.value
                                        val newV = model.buildings.value!!.find { b -> b._id == result.buildingId }
                                        if (newV != oldV) {
                                            model.locatedBuilding.value = newV
                                            Log.d(TAG, "Updated located building from $oldV to $newV")
                                        }
                                    } else {
                                        Log.d(TAG, "Building ID not returned, skipping update")
                                    }

                                    // Floor
                                    if (result.floorId != null) {
                                        if (model.buildings.value?.isEmpty() == true) {
                                            Log.w(TAG, "Floor ID returned in location result but model.buildings is null or empty, skipping update")
                                        } else if (model.locatedBuilding.value == null) {
                                            Log.w(TAG, "Floor ID returned in location result but model.locatedBuilding is null, skipping update")
                                        } else {
                                            val oldV = model.locatedFloor.value
                                            val newV = model.locatedBuilding.value!!.floors!!.find { f -> f._id == result.floorId }
                                            if (newV != oldV) {
                                                model.locatedFloor.value = newV
                                                Log.d(TAG, "Updated located floor from $oldV to $newV")
                                            }
                                        }
                                    } else {
                                        Log.w(TAG, "Floor ID not returned, skipping update")
                                    }
                                    Log.d(TAG, "LOCATION RESULT: $result")
                                    val locationStr = if (model.location.value == null) "?" else "(${model.location.value!!.latitude}, ${model.location.value!!.longitude})"
                                    val buildingStr = model.locatedBuilding.value?.name ?: "?"
                                    val floorStr = model.locatedFloor.value?.name ?: "?"
                                    Snackbar.make(view!!, "Located at $locationStr on floor $floorStr of building $buildingStr", Snackbar.LENGTH_INDEFINITE).show()

                                    // Show location unknown icon if appropriate
                                    model.isLocationUnknown.value = !result.isComplete()

                                    switchOverlay(model.locatedBuilding.value!!, model.locatedFloor.value!!)
                                },
                                { error -> Log.e(TAG, error?.message ?: "Unknown error getting location") })
            }
        })

        model.location.observe(this, Observer<LatLng> { newLocation ->
            if (newLocation != null) {
                marker?.remove()
                marker = map!!.addMarker(MarkerOptions().position(newLocation))
                map!!.moveCamera(CameraUpdateFactory.newCameraPosition((CameraPosition(newLocation, 19f, 0f, 0f))))
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this mapFragment
        val result = inflater.inflate(R.layout.fragment_map, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        floorSelectorFragment = childFragmentManager.findFragmentById(R.id.floorSelectorFragment) as FloorSelectorFragment
        statusIndicatorFragment = childFragmentManager.findFragmentById(R.id.statusIndicatorFragment) as StatusIndicatorFragment
        return result
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        /*
            Before enabling the My Location layer, we MUST have been granted location permission by
            the user.
         */
        if (ContextCompat.checkSelfPermission(this.context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Show rationale and request permission.
            Logger.w("Location permission not granted, requesting")
            ActivityCompat.requestPermissions(this.activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    RequestFineLocationPermission)
            return
        }

        // Configure map
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
//            map.isBuildingsEnabled = true
//            map.isBuildingsEnabled = false
        map.isIndoorEnabled = true
        map.isTrafficEnabled = false
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        map.setOnIndoorStateChangeListener(this)
        map.uiSettings.isIndoorLevelPickerEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        // TODO set default to current GPS location
        // Start map: Move camera to starting position, set default floor number (this will trigger overlay and marker updates)
//        map.moveCamera(CameraUpdateFactory.newCameraPosition((CameraPosition(buildingLatLng(building), building.zoom!!.toFloat(), 0f, 0f))))
//        model.selectedFloorNumber.value = building.getDefaultFloor().number!!
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RequestFineLocationPermission) {
            if (Manifest.permission.ACCESS_FINE_LOCATION == permissions.getOrNull(0)
                    && PackageManager.PERMISSION_GRANTED == grantResults.getOrNull(0)) {
                this.onMapReady(this.map!!)
            } else {
                // Permission was denied. TODO Display an error message.
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * Remove the current overlay, if any, and add the overlay of the specified floor number.
     * Downloads the overlay image in the background if necessary, and creates Maps' Overlay when
     * ready.
     */
    private fun switchOverlay(building: Building, floor: Floor) {
        if (model.isChangingOverlay.value!!) {
            throw IllegalStateException("Already changing overlays, can't change overlays again")
        }
        model.isChangingOverlay.value = true
        Log.d(TAG, "Removing ground overlay...")
        activeGroundOverlay?.isVisible = false
        if (!groundOverlays.containsKey(floor._id)) {
            Log.d(TAG, "Downloading ground overlay for floor #$floor of ${building.name}...")
            var overlayUrl = floor.overlay!!.url!!
            // Use a half-resolution image for older Android because big images go over memory limit
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                overlayUrl = overlayUrl.replace(Regex("/upload/v(\\d+)/"), "/upload/w_0.5,c_scale/v$1/");
            }

            val downloadFuture = Glide
                    .with(this)
                    .asBitmap()
                    .load(overlayUrl)
                    .submit()
            AsyncTask.execute {
                val overlayBitmap = downloadFuture.get()
                Log.d(TAG, "Overlay download complete!")
                Log.d(TAG, "Adding new ground overlay...")
                val overlayOptions = gMapsGroundOverlayOptions(building.getOverlayNumber(floor.number!!), overlayBitmap)
                activity?.runOnUiThread {
                    activeGroundOverlay = map!!.addGroundOverlay(overlayOptions)
                    groundOverlays[floor._id!!] = activeGroundOverlay!!
                    model.isChangingOverlay.value = false
                }
            }
        } else {
            Log.d(TAG, "Adding cached ground overlay")
            activeGroundOverlay = groundOverlays[floor._id]
            activeGroundOverlay!!.isVisible = true
            model.isChangingOverlay.value = false
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * mapFragment to allow an interaction in this mapFragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    override fun onIndoorBuildingFocused() {
        Log.w(TAG, "Indoor building focused!!")
    }

    override fun onIndoorLevelActivated(p0: IndoorBuilding?) {
        Log.w(TAG, "Indoor level activated on building $p0")
    }

    override fun onMyLocationClick(location: Location) {
        Logger.i("My location clicked!")
        Toast.makeText(this.context, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Logger.i("My location button clicked!")
        Toast.makeText(this.context, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }
}
