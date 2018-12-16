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
import android.widget.Button
import android.widget.LinearLayout
import itba.edu.ar.spi_android_app.Activities.mapActivity.MapViewModel
import itba.edu.ar.spi_android_app.R
import itba.edu.ar.spi_android_app.utils.TAG

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
        model.floorNumbers.value = mutableListOf(1, 2, 3) // TODO get this from current building
        model.floorNumbers.observe(this, Observer<List<Int>>{ floors ->
            // Update UI
            this.clear()
            this.setFloors(floors!!)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_floor_selector, container, false)
        layout = view.findViewById(R.id.layout)
//        layout.addView()
        return view
    }

    /**
     * Creates a floor selector button, with style and a click handler that updates the fragment's
     * and viewModel's selected floor number.
     */
    private fun button(floorNumber: Int): Button {
        val result = Button(this.context)
        result.width = 50
        result.height = 50
        result.tag = floorNumber // Find buttons by tag, not ID
        result.text = floorNumber.toString()
        result.setOnClickListener { clickedView ->
            selectedButton?.isPressed = false
            selectedButton = clickedView as Button
            selectedButton!!.isPressed = true
            model.selectedFloorNumber.value = floorNumber
            Log.d(TAG, "Clicked on floor #$floorNumber! From FloorSelectorFragment")
        }
        return result
    }

    /**
     * Removes all floor buttons and clears buttons collection.
     */
    private fun clear() {
        layout.removeAllViews()
        // TODO clear button click handlers / live data observers?
        buttons.clear()
    }

    private fun setFloors(floors: List<Int>) {
        floors
                .sorted()
                .reversed() // Vertical linear layout goes from top to bottom, so first button has to be the one with the highest floor number
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
