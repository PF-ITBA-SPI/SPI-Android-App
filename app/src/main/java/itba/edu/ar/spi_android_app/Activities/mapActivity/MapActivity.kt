package itba.edu.ar.spi_android_app.Activities.mapActivity

import android.support.v4.app.Fragment
import itba.edu.ar.spi_android_app.Activities.utils.SingleFragmentActivity

class MapActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment {
        return MapFragment()
    }

}
