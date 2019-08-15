package ar.edu.itba.spi_android_app.Activities.map.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ar.edu.itba.spi_android_app.Activities.map.EXTRA_BUILDING
import ar.edu.itba.spi_android_app.Activities.map.MapViewModel
import ar.edu.itba.spi_android_app.Activities.map.fragments.FloorSelectorFragment
import ar.edu.itba.spi_android_app.Activities.map.fragments.StatusIndicatorFragment
import ar.edu.itba.spi_android_app.Activities.scan.ScanActivity
import ar.edu.itba.spi_android_app.R
import ar.edu.itba.spi_android_app.api.ApiSingleton
import ar.edu.itba.spi_android_app.api.clients.SamplesClient
import ar.edu.itba.spi_android_app.api.models.Building
import ar.edu.itba.spi_android_app.api.models.Sample
import ar.edu.itba.spi_android_app.utils.TAG
import ar.edu.itba.spi_android_app.utils.buildingLatLng
import ar.edu.itba.spi_android_app.utils.gMapsGroundOverlayOptions
import ar.edu.itba.spi_android_app.utils.gMapsMarkerOptions
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.IndoorBuilding
import com.google.android.gms.maps.model.Marker
import com.orhanobut.logger.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

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
class MapFragment : Fragment(), GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, GoogleMap.OnIndoorStateChangeListener, View.OnClickListener {
    // TODO: Rename and change types of parameters
    private val RequestFineLocationPermission = 42

    private var listener: OnFragmentInteractionListener? = null
    private var map: GoogleMap? = null
    private lateinit var model: MapViewModel


    private lateinit var mapFragment: SupportMapFragment
    private lateinit var floorSelectorFragment: FloorSelectorFragment
    private lateinit var statusIndicatorFragment: StatusIndicatorFragment

    private val groundOverlays = HashMap<Int, GroundOverlay>()
    private var activeGroundOverlay: GroundOverlay? = null

    private lateinit var building: Building
    private var samples: MutableCollection<Sample> = mutableListOf()
    private val markers = HashMap<Int, MutableList<Marker>>()

    private lateinit var samplesClient: SamplesClient
    private var samplesDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            building = it.getSerializable(EXTRA_BUILDING) as Building
        }
        Log.d(TAG, "Started map super-fragment with building ID ${building._id}")

        // Get shared view-model
        model = activity?.run {
            ViewModelProviders.of(this).get(MapViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        // Set floors and listen to floor changes
        model.floors.value = building.floors
        model.selectedFloorNumber.observe(this, Observer<Int> { floorNumber ->
            switchOverlay(floorNumber!!)
            switchMarkers(floorNumber)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val view = getView()
        if(view != null) {
            val fab = view.findViewById(R.id.fab) as FloatingActionButton
            fab.setOnClickListener(this)
        }
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

        // Start map: Move camera to starting position, set default floor number (this will trigger overlay and marker updates)
        map.moveCamera(CameraUpdateFactory.newCameraPosition((CameraPosition(buildingLatLng(building), building.zoom!!.toFloat(), 0f, 0f))))
        model.selectedFloorNumber.value = building.getDefaultFloor().number!!

        // Query existing samples and draw them on the map
        samplesClient = ApiSingleton.getInstance(context!!).defaultRetrofitInstance.create(SamplesClient::class.java)
        samplesDisposable = samplesClient
                .list(building._id!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { Log.d(ar.edu.itba.spi_android_app.utils.TAG, "GET /buildings/${building._id}/samples") }
                .subscribe(
                        { result -> run {
                                samples.addAll(result)
                                mapSamplesToMarkers(samples)
                            }
                        },
                        { error -> Log.e(ar.edu.itba.spi_android_app.utils.TAG, "Error getting samples: ${error.message}") }
                )
        // React to marker clicks
        map.setOnMarkerClickListener { marker ->
            Log.d(TAG, "Tapped on marker with ${(marker.tag as Sample)}")
            false // Return false to indicate we have not consumed the event and default behavior should continue
        }
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
     * Map [Sample]s to Google Maps' markers, populating [markers] as appropriate. Set initial
     * visibility of markers according to the selected floor.
     */
    private fun mapSamplesToMarkers(samples: Collection<Sample>) {
        activity?.runOnUiThread {
            samples.forEach { sample ->
                val markerOptions = gMapsMarkerOptions(sample)
                val marker = map!!.addMarker(markerOptions)
                marker.tag = sample
                val sampleFloorNumber = building.floors!!.find { f -> f._id == sample.floorId }!!.number!!
                marker.isVisible = sampleFloorNumber == model.selectedFloorNumber.value
                markers.getOrPut(sampleFloorNumber) {mutableListOf()}.add(marker)
            }
        }
    }

    /**
     * Remove the current overlay, if any, and add the overlay of the specified floor number.
     * Downloads the overlay image in the background if necessary, and creates Maps' Overlay when
     * ready.
     */
    private fun switchOverlay(floorNumber: Int) {
        if (model.isChangingOverlay.value!!) {
            throw IllegalStateException("Already changing overlays, can't change overlays again")
        }
        model.isChangingOverlay.value = true
        Log.d(TAG, "Removing ground overlay...")
        activeGroundOverlay?.isVisible = false
        if (!groundOverlays.containsKey(floorNumber)) {
            Log.d(TAG, "Downloading ground overlay for floor #$floorNumber of ${building.name}...")
            val downloadFuture = Glide
                    .with(this)
                    .asBitmap()
                    .load(building.getFloorNumber(floorNumber)!!.overlay!!.url)
                    .submit()
            AsyncTask.execute {
                val overlayBitmap = downloadFuture.get()
                Log.d(TAG, "Overlay download complete!")
                Log.d(TAG, "Adding new ground overlay...")
                val overlayOptions = gMapsGroundOverlayOptions(building.getOverlayNumber(floorNumber), overlayBitmap)
                activity?.runOnUiThread {
                    activeGroundOverlay = map!!.addGroundOverlay(overlayOptions)
                    groundOverlays[floorNumber] = activeGroundOverlay!!
                    model.isChangingOverlay.value = false
                }
            }
        } else {
            Log.d(TAG, "Adding cached ground overlay")
            activeGroundOverlay = groundOverlays[floorNumber]
            activeGroundOverlay!!.isVisible = true
            model.isChangingOverlay.value = false
        }
    }

    /**
     * Iterate over all markers and set visible only those in the specified floor number.
     *
     * @param floorNumber The floor number of markers to make visible. All other floors will hide
     * their markers.
     */
    private fun switchMarkers(floorNumber: Int) {
        markers.entries.forEach { entry ->
            val visible = floorNumber == entry.key
            entry.value.forEach { m -> m.isVisible = visible }
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this mapFragment using the provided parameters.
         *
         * @param building Building to start focused on.
         * @return A new instance of mapFragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(building: Building) =
                MapFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(EXTRA_BUILDING, building)
                    }
                }
    }

    override fun onClick(v: View){
        when (v.id) {
            R.id.fab -> {
                map?.let {
                    val target = it.cameraPosition.target
                    Logger.i(target.latitude.toString())
                    Toast.makeText(this.context, "Calibrating", Toast.LENGTH_LONG).show()
                    startActivity(ScanActivity.startIntent(activity!!, building._id!!, model.floors.value!![model.selectedFloorNumber.value!!]._id!!, target.latitude, target.longitude))
                }
            }
        }
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
