package itba.edu.ar.spi_android_app.Activities.mapActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.orhanobut.logger.Logger
import itba.edu.ar.spi_android_app.Activities.utils.SingleFragmentActivity
import itba.edu.ar.spi_android_app.R

class MapActivity : SingleFragmentActivity(), GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, GoogleMap.OnIndoorStateChangeListener {
    val TAG = "SPI_ITBA"

    override fun onIndoorBuildingFocused() {
        Log.w(TAG, "Indoor building focused!!")
    }

    override fun onIndoorLevelActivated(p0: IndoorBuilding?) {
        Log.w(TAG, "Indoor level activated on building $p0")
    }

    private val RequestFineLocationPermission = 42

    override fun onMyLocationClick(location: Location) {
        Logger.i("My location clicked!")
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Logger.i("My location button clicked!")
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    private val ITBA = LatLng(-34.603500, -58.367791)
    private var map: GoogleMap? = null
    private var fragment: SupportMapFragment = SupportMapFragment.newInstance(GoogleMapOptions()
        .camera(CameraPosition(ITBA, 18f, 0f, 0f)) // ITBA
//        .camera(CameraPosition(LatLng(40.643365, -73.781709), 18f, 0f, 0f)) // JFK
//        .camera(CameraPosition(LatLng(21.332, -157.92), 18f, 0f, 0f)) // Honolulu
        .mapType(GoogleMap.MAP_TYPE_NORMAL))

    override fun createFragment(): Fragment {
        fragment.getMapAsync(this)
        return fragment
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        /*
            Before enabling the My Location layer, we MUST have been granted location permission by
            the user.
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
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
            ActivityCompat.requestPermissions(this,
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
}
