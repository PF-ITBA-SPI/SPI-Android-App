package ar.edu.itba.spi_android_app.Activities.map

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ar.edu.itba.spi_android_app.api.models.Building
import ar.edu.itba.spi_android_app.api.models.Floor
import com.google.android.gms.maps.model.LatLng

/**
 * ViewModel shared between map view and floor selector fragments, used to communicate between them.
 * Shared data includes floor numbers of current building and currently selected floor number.
 */
class MapViewModel : ViewModel() {
    var buildings = MutableLiveData<List<Building>>()
    var floors = MutableLiveData<List<Floor>>()
    var selectedFloorNumber = MutableLiveData<Int>()
    var isOffline = MutableLiveData<Boolean>().apply { value = false }
    var isLocationUnknown = MutableLiveData<Boolean>().apply { value = false }
    var isChangingOverlay = MutableLiveData<Boolean>().apply { value = false }

    // Values returned from location algorithm
    var locatedBuilding = MutableLiveData<Building>()
    var locatedFloorNumber = MutableLiveData<Int>()
    var location = MutableLiveData<LatLng>()

}
