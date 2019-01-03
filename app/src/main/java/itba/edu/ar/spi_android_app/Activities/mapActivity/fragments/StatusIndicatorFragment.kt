package itba.edu.ar.spi_android_app.Activities.mapActivity.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import itba.edu.ar.spi_android_app.Activities.mapActivity.MapViewModel
import itba.edu.ar.spi_android_app.R
import itba.edu.ar.spi_android_app.utils.TAG


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
            Log.d(TAG, "Offline status changed to $isOffline")
            offlineIcon?.visibility = if (isOffline!!) android.view.View.VISIBLE else android.view.View.INVISIBLE
        })
        model.isLocationUnknown.observe(this, Observer<Boolean>{ isLocationUnknown ->
            Log.d(TAG, "Unknown location status changed to $isLocationUnknown")
            unknownLocationIcon?.visibility = if (isLocationUnknown!!) android.view.View.VISIBLE else android.view.View.INVISIBLE
        })
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
