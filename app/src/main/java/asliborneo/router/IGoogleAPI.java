package asliborneo.router;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IGoogleAPI {
    @GET("maps/api/directions/json")
    Call<Directions> getPath(String mode, String transit_routing_preference, String origin, String destination, String key);
}