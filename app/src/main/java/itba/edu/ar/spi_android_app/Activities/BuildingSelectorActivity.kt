package ar.edu.itba.spi_android_app.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import ar.edu.itba.spi_android_app.Activities.map.EXTRA_BUILDING
import ar.edu.itba.spi_android_app.Activities.map.MapActivity
import ar.edu.itba.spi_android_app.api.ApiSingleton
import ar.edu.itba.spi_android_app.api.clients.BuildingsClient
import ar.edu.itba.spi_android_app.api.models.Building
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_building_selector.*
import ar.edu.itba.spi_android_app.R

class BuildingSelectorActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var buildingsDisposable: Disposable? = null
    private var selectedBuilding: Building? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_selector)

        // Create an adapter to populate and style the spinner
        val adapter = ArrayAdapter<Building>(this, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val buildingsClient = ApiSingleton.getInstance(this).defaultRetrofitInstance.create(BuildingsClient::class.java)
        buildingsDisposable = buildingsClient
                .list()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { Log.d(ar.edu.itba.spi_android_app.utils.TAG, "GET /buildings") }
                .subscribe(
                        { result ->
                            run {
                                adapter.clear()
                                adapter.addAll(result)
                                adapter.notifyDataSetChanged()
                            }
                        },
                        { error -> Log.e(ar.edu.itba.spi_android_app.utils.TAG, error.message) }
                )
        buildings_spinner.adapter = adapter
        buildings_spinner.onItemSelectedListener = this
        buildings_spinner.emptyView = buildings_placeholder

        button.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java).apply { putExtra(EXTRA_BUILDING, selectedBuilding!!) })
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedBuilding = parent!!.getItemAtPosition(position) as Building
        button.isEnabled = true
        Log.d(ar.edu.itba.spi_android_app.utils.TAG, "Selected $selectedBuilding")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        selectedBuilding = null
        button.isEnabled = false
        Log.d(ar.edu.itba.spi_android_app.utils.TAG, "Selected nothing!")
    }
}
