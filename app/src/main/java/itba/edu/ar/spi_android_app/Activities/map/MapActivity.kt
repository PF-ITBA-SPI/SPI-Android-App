package ar.edu.itba.spi_android_app.Activities.map

import android.net.Uri
import android.support.v4.app.Fragment
import android.util.Log
import ar.edu.itba.spi_android_app.Activities.SingleFragmentActivity
import ar.edu.itba.spi_android_app.Activities.map.fragments.MapFragment
import ar.edu.itba.spi_android_app.api.models.Building
import ar.edu.itba.spi_android_app.utils.TAG

/**
 * Parameter name that identifies the building that the map activity should start focused on.
 */
const val EXTRA_BUILDING = "ar.edu.itba.spi_android_app.extra.BUILDING"

class MapActivity : SingleFragmentActivity(), MapFragment.OnFragmentInteractionListener {

    override fun onFragmentInteraction(uri: Uri) {
        Log.i(TAG, "Fragment interaction!")
    }

    override fun createFragment(): Fragment {
        return MapFragment.newInstance(intent.getSerializableExtra(EXTRA_BUILDING) as Building)
    }
}
