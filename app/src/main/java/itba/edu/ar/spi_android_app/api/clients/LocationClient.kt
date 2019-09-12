package itba.edu.ar.spi_android_app.api.clients

import ar.edu.itba.spi_android_app.api.models.LocationResult
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface LocationClient {
    @POST("/location")
    fun queryLocation(@Body fingerprint: Map<String, Double>): Observable<LocationResult>
}
