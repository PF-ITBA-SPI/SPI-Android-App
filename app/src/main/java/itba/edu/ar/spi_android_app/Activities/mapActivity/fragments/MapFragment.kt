package itba.edu.ar.spi_android_app.Activities.mapActivity.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.orhanobut.logger.Logger
import itba.edu.ar.spi_android_app.R
import itba.edu.ar.spi_android_app.utils.TAG


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this mapFragment must implement the
 * [MapFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this mapFragment.
 *
 */
class MapFragment : Fragment(), GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, GoogleMap.OnIndoorStateChangeListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val RequestFineLocationPermission = 42

    private var listener: OnFragmentInteractionListener? = null
    private val ITBA = LatLng(-34.603500, -58.367791)
    private var map: GoogleMap? = null

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var floorSelectorFragment: FloorSelectorFragment
    private lateinit var statusIndicatorFragment: StatusIndicatorFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
        if (ContextCompat.checkSelfPermission(this.context!!, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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

            map.moveCamera(CameraUpdateFactory.newCameraPosition((CameraPosition(ITBA, 18f, 0f, 0f))))
            map.mapType = GoogleMap.MAP_TYPE_NORMAL

            // Work with indoor maps but this only works with supported buildings, not custom ones
//            Handler().postDelayed({
//                Log.i(TAG, "Getting current building...")
//                Log.i(TAG, "Hello this is second log.")
//                map.focusedBuilding?.levels?.forEach { Log.i(TAG, "${it.name} - ${it.shortName}") }
//                Log.i(TAG, "Hello this is third log. Selected building is ${if (map.focusedBuilding == null) "Null" else "Not null"}")
//                val itba = map.focusedBuilding
//                Log.i(TAG, "Selected building: $itba")
//                if (itba != null) {
//                    val levels = itba.levels
//                    Log.i(TAG, "Levels: ${if (levels == null) "Null" else "Not null"}")
//                } else {
//                    Log.w(TAG, "ITBA is null =(")
//                }
//            }, 20 * 1000)

            map.addGroundOverlay(GroundOverlayOptions()
                    .position(ITBA, 100f)
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.test))
//                    .anchor(0f, 0f)
            )
        } else {
            // Show rationale and request permission.
            Logger.w("Location permission not granted, requesting")
            ActivityCompat.requestPermissions(this.activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    RequestFineLocationPermission)
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
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of mapFragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                MapFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
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
