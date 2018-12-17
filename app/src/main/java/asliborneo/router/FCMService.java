package asliborneo.router;

import asliborneo.router.Model.fcm_response;
import asliborneo.router.Model.sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface FCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=BHVDJ053YwAv5dX_BiY06B1JHQOs0Q-k-6p-v743x4OTs3Y_yyk2CyMI45I0PXbrc2h9GFTjx_N5WHCt3VMZmcI"
    })
    @POST("fcm/send")
    Call<fcm_response> send_message(@Body sender body);
}