package itba.edu.ar.spi_android_app.Activities.mapActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.orhanobut.logger.Logger
import itba.edu.ar.spi_android_app.Activities.utils.SingleFragmentActivity

class MapActivity : SingleFragmentActivity(), GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {
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

    private var map: GoogleMap? = null
    private var fragment: SupportMapFragment = SupportMapFragment.newInstance()

    override fun createFragment(): Fragment {
        fragment.getMapAsync (this)
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
            map.setOnMyLocationButtonClickListener(this)
            map.setOnMyLocationClickListener(this)
            map.uiSettings.isMyLocationButtonEnabled = true
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
