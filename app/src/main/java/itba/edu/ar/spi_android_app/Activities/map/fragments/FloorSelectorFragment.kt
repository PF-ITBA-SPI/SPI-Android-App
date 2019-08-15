package ar.edu.itba.spi_android_app.Activities.map.fragments

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
import android.widget.Button
import android.widget.LinearLayout
import ar.edu.itba.spi_android_app.Activities.map.MapViewModel
import ar.edu.itba.spi_android_app.R
import ar.edu.itba.spi_android_app.api.models.Floor
import ar.edu.itba.spi_android_app.utils.TAG

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * [FloorSelectorFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FloorSelectorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FloorSelectorFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private lateinit var layout: LinearLayout
    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var model: MapViewModel
    private var buttons: MutableCollection<Button> = mutableListOf()
    private var selectedButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }

        model = activity?.run {
            ViewModelProviders.of(this).get(MapViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        model.floors.observe(this, Observer<List<Floor>>{ floors ->
            // Update UI
            this.clear()
            this.setFloors(floors!!)
        })
        model.selectedFloorNumber.observe(this, Observer<Int>{ selectedFloorNumber ->
            // Disable button for selected floor, enable all others
            buttons.forEach { b -> b.isEnabled = (b.tag as Floor).number!! != selectedFloorNumber }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_floor_selector, container, false)
        layout = view.findViewById(R.id.layout)
        return view
    }

    /**
     * Creates a floor selector button, with style and a click handler that updates the fragment's
     * and viewModel's selected floor number.
     */
    private fun button(floor: Floor): Button {
        val button = Button(this.context)
        button.width = 50
        button.height = 50
        button.tag = floor // Find buttons by tag, not ID
        button.text = floor.name
        button.setOnClickListener { clickedView ->
            if (!model.isChangingOverlay.value!!) {
                selectedButton = clickedView as Button
                model.selectedFloorNumber.value = floor.number
                Log.d(TAG, "Selected floor #$floor")
            } else {
                Log.d(TAG, "Already switching overlays, ignoring floor #$floor click")
            }
        }
        return button
    }

    /**
     * Removes all floor buttons and clears buttons collection.
     */
    private fun clear() {
        layout.removeAllViews()
        // TODO clear button click handlers / live data observers?
        buttons.clear()
    }

    private fun setFloors(floors: List<Floor>) {
        floors
                .sortedByDescending { f -> f.number } // Vertical linear layout goes from top to bottom, so first button has to be the one with the highest floor number
                .forEach { floor ->
                    val newButton = button(floor)
                    buttons.add(newButton)
                    layout.addView(newButton)
                }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
//            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
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
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"
        // The request code must be 0 or greater.
        private val PLUS_ONE_REQUEST_CODE = 0

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FloorSelectorFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): FloorSelectorFragment {
            val fragment = FloorSelectorFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
