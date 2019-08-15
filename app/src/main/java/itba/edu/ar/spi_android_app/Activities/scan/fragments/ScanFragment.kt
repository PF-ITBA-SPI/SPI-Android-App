package ar.edu.itba.spi_android_app.Activities.scan.fragments

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ar.edu.itba.spi_android_app.R
import ar.edu.itba.spi_android_app.api.ApiSingleton
import ar.edu.itba.spi_android_app.api.clients.SamplesClient
import ar.edu.itba.spi_android_app.api.models.Sample
import ar.edu.itba.spi_android_app.utils.TAG
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * A fragment which scans nearby WiFi networks.
 * Activities that contain this fragment must implement the
 * [ScanFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ScanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScanFragment : Fragment() {
    private lateinit var layout: ConstraintLayout
    private lateinit  var samplesClient: SamplesClient
    private var samplesDisposable: Disposable? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var resultList = ArrayList<ScanResult>()

    private lateinit var wifiManager: WifiManager

    private var buildingId: String? = null
    private var floorId: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                Log.d(TAG, wifiManager.scanResults.toString())
                samplesClient = ApiSingleton.getInstance(context).defaultRetrofitInstance.create(SamplesClient::class.java)
                samplesDisposable = samplesClient
                        .create(buildingId!!, Sample(buildingId!!, floorId!!, latitude!!, longitude!!, wifiManager.scanResults))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { Log.d(TAG, "POSTing /buildings/$buildingId/samples") }
                        .subscribe(
                                { result -> run {
                                    Log.i(TAG, "Result: $result")
                                    activity!!.onBackPressed()
                                }
                                },
                                { error ->
                                    Log.e(TAG, "Error POSTing samples: ${error.message}")
                                    Log.e(TAG, error.toString())
                                    Log.e(TAG, Log.getStackTraceString(error))
                                }
                        )
            } else {
                Log.d(TAG, "Scan Failed")
            }
        }
    }
    private val MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context?.registerReceiver(wifiScanReceiver, intentFilter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_scan, container, false)
        layout = view.findViewById(R.id.layout)
        return view
    }

    fun askPermissions(): Boolean {
        Log.d(TAG, "CHECKING PERMISSIONS")
        if (ContextCompat.checkSelfPermission(activity as Activity, Manifest.permission.CHANGE_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            Log.d(TAG, "ASKING FOR PERMISSIONS")
            ActivityCompat.requestPermissions(this.activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE)
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startScanning()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun startScanning() {
        if(askPermissions()){
            Log.d(TAG, "START SCAN")
            wifiManager.startScan()
        }
        Handler().postDelayed({
            startScanning()
        }, 30000)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        askPermissions()
        wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
        startScanning()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
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
         * @return A new instance of fragment FloorSelectorFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(buildingId: String, floorId: String, latitude: Double, longitude: Double): ScanFragment {
            val fragment = ScanFragment()
            fragment.buildingId = buildingId
            fragment.floorId = floorId
            fragment.latitude = latitude
            fragment.longitude = longitude
            return fragment
        }
    }

}// Required empty public constructor
