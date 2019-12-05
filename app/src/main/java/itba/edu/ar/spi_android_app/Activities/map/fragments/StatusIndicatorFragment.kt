package ar.edu.itba.spi_android_app.Activities.map.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ar.edu.itba.spi_android_app.Activities.map.MapViewModel
import ar.edu.itba.spi_android_app.R
import ar.edu.itba.spi_android_app.utils.TAG


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * Status indicators drawn over the map for different situations, such as:
 * - Phone is offline (or server is otherwise unreachable)
 * - Unrecognized location
 *
 *
 * - Activities that contain this fragment must implement the
 * [StatusIndicatorFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * - Use the [StatusIndicatorFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class StatusIndicatorFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var offlineIcon: ImageView? = null
    private var unknownLocationIcon: ImageView? = null
    private lateinit var model: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = activity?.run {
            ViewModelProviders.of(this).get(MapViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        model.isOffline.observe(this, Observer<Boolean>{ isOffline ->
            offlineIcon?.visibility = if (isOffline!!) android.view.View.VISIBLE else android.view.View.INVISIBLE
        })
        model.isLocationUnknown.observe(this, Observer<Boolean>{ isLocationUnknown ->
            Log.d(TAG, "Unknown location status changed to $isLocationUnknown")
            unknownLocationIcon?.visibility = if (isLocationUnknown!!) android.view.View.VISIBLE else android.view.View.INVISIBLE
        })
        // Listen to connectivity changes
        val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        context!!.registerReceiver(ConnectivityChangeBroadcastReceiver(connectivityManager), IntentFilter(CONNECTIVITY_ACTION))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val result = inflater.inflate(R.layout.fragment_status_indicator, container, false)
        offlineIcon = result.findViewById(R.id.offline_icon)
        unknownLocationIcon = result.findViewById(R.id.unknown_location_icon)
        return result
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
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * Broadcast receiver that reacts to connectivity changes and updates [MapViewModel.isOffline]
     * appropriately.
     *
     * See also [Android doc on monitoring network changes][https://developer.android.com/training/monitoring-device-state/connectivity-monitoring#MonitorChanges],
     * [Android doc on received intent][https://developer.android.com/reference/android/net/ConnectivityManager#CONNECTIVITY_ACTION]
     */
    private inner class ConnectivityChangeBroadcastReceiver(private val connectivityManager: ConnectivityManager) : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            var isOffline =  intent?.getBooleanExtra(EXTRA_NO_CONNECTIVITY, false) == true
            // Definitely disconnected
            if (isOffline) {
                updateConnectivityStatus(isOffline)
                return
            }
            // Possibly disconnected because intent did not contain EXTRA_NO_CONNECTIVITY (older Android)
            // Code adapted from https://stackoverflow.com/a/4239410
            var wifiConnected = false
            var mobileConnected = false

            val netInfo: Array<NetworkInfo> = connectivityManager.getAllNetworkInfo()
            for (ni in netInfo) {
                if (ni.typeName.equals("WIFI", ignoreCase = true) && ni.isConnected) wifiConnected = true
                if (ni.typeName.equals("MOBILE", ignoreCase = true) && ni.isConnected) mobileConnected = true
                isOffline = !wifiConnected && !mobileConnected
                if (!isOffline) { // Connected on at least one interface
                    break
                }
            }
            updateConnectivityStatus(isOffline)
        }

        private fun updateConnectivityStatus(isOffline: Boolean) {
            model.isOffline.value = isOffline
            Log.i(TAG, "Connectivity state changed. Offline? $isOffline")
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
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
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StatusIndicatorFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                StatusIndicatorFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
