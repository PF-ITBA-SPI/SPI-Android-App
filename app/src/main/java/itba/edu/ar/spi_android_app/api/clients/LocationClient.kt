package itba.edu.ar.spi_android_app.api.clients

import ar.edu.itba.spi_android_app.api.models.Building
import ar.edu.itba.spi_android_app.api.models.Sample
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LocationClient {
    @POST("/location")
    fun create(@Path("buildingId") buildingId: String, @Body fingerprint: HashMap<String, Double>): Observable<Sample>
}