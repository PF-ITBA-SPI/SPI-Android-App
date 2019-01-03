package itba.edu.ar.spi_android_app.Activities.mapActivity

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

/**
 * ViewModel shared between map view and floor selector fragments, used to communicate between them.
 * Shared data includes floor numbers of current building and currently selected floor number.
 */
class MapViewModel : ViewModel() {
    var floorNumbers = MutableLiveData<List<Int>>()
    var selectedFloorNumber = MutableLiveData<Int>()
    var isOffline = MutableLiveData<Boolean>().apply { value = false }
    var isLocationUnknown = MutableLiveData<Boolean>().apply { value = false }
}
