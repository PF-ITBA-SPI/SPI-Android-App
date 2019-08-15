package ar.edu.itba.spi_android_app.api.clients

import ar.edu.itba.spi_android_app.api.models.Sample
import io.reactivex.Observable
import retrofit2.http.*

interface SamplesClient {
    @GET("/buildings/{buildingId}/samples")
    fun list(@Path("buildingId") buildingId: String): Observable<List<Sample>>

    @POST("/buildings/{buildingId}/samples")
    fun create(@Path("buildingId") buildingId: String, @Body sample: Sample): Observable<Sample>

    @DELETE("/buildings/{buildingId}/samples/{sampleId}")
    fun delete(@Path("buildingId") buildingId: String, @Path("sampleId") sampleId: String): Observable<Int>
}
