package ar.edu.itba.spi_android_app.Activities.scan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import ar.edu.itba.spi_android_app.Activities.SingleFragmentActivity
import ar.edu.itba.spi_android_app.Activities.scan.fragments.ScanFragment

/**
 * Created by julianrodrigueznicastro on 25/03/2019.
 */

class ScanActivity : SingleFragmentActivity(), ScanFragment.OnFragmentInteractionListener {

    override fun onFragmentInteraction(uri: Uri) {
        Log.i(ar.edu.itba.spi_android_app.utils.TAG, "Fragment interaction!")
    }

    override fun createFragment(): Fragment {
        return ScanFragment.newInstance(
                intent.getStringExtra(BUILDING_ID),
                intent.getStringExtra(FLOOR_ID),
                intent.getDoubleExtra(LATITUDE, 0.0), // TODO do away with no default value
                intent.getDoubleExtra(LONGITUDE, 0.0)
        )
    }

    companion object {
        private const val BUILDING_ID = "ar.edu.itba.spi_android_app.Activities.scan.BUILDING_ID"
        private const val FLOOR_ID = "ar.edu.itba.spi_android_app.Activities.scan.FLOOR_ID"
        private const val LATITUDE = "ar.edu.itba.spi_android_app.Activities.scan.LATITUDE"
        private const val LONGITUDE = "ar.edu.itba.spi_android_app.Activities.scan.LONGITUDE"

        fun startIntent(from: Activity, buildingId: String, floorId: String, latitude: Double, longitude: Double): Intent {
            val intent = Intent(from, ScanActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(BUILDING_ID, buildingId)
            bundle.putSerializable(FLOOR_ID, floorId)
            bundle.putSerializable(LATITUDE, latitude)
            bundle.putSerializable(LONGITUDE, longitude)
            intent.putExtras(bundle)

            return intent
        }
    }
}