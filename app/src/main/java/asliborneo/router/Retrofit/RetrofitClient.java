package asliborneo.router.Retrofit;

import com.google.android.gms.location.places.PlaceReport;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit=null;
    private static Retrofit retrofit_obj_for_Directions_api=null;
    public static Retrofit getClient(String fcmURL){
        if (retrofit==null){
            retrofit=new Retrofit.Builder()
                    .baseUrl(fcmURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    public static Retrofit getDirectionClient(){
        if (retrofit_obj_for_Directions_api==null){
            retrofit_obj_for_Directions_api=new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit_obj_for_Directions_api;
    }


}

