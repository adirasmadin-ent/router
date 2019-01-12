package asliborneo.router.Commons;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import asliborneo.router.Retrofit.FCMClient;
import asliborneo.router.Retrofit.GoogleMAPApi;
import asliborneo.router.Service.IFCMService;
import asliborneo.router.Service.IGoogleMAPApi;
import asliborneo.router.Model.DataMessage;
import asliborneo.router.Model.FCMResponse;
import asliborneo.router.Model.Rider;
import asliborneo.router.Model.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Common {

public static Rider currentUser;

    public static double userWallet = 0.0;
    public static final String CANCEL_BROADCAST = "cancel_pickup";
    public static final String tokenTable ="Tokens";
    public static final String driver_location="Drivers";
    public static final String Registered_driver="DriverInformation";
    public static final String Registered_Riders="RidersInformation";
    public static final String pickUpRequest_tbl="PickUpRequest";
    public static final String rateDetail_table="Driver_Rating";
    public static int PICK_IMAGE_REQUEST=9999;
    private static double Base_Fare=2.50;
    private static double Time_Rate=0.25;
    private static double Distance_Rate=0.50;
    public  static  Boolean isDriverFound=false;
    public static String  driverId="";
    public static final String googleAPIUrl ="https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static final java.lang.String user_field="usr";
    public static final java.lang.String password_field="pwd";
    public static double getPrice(double km,int min){
        return (Base_Fare+(Time_Rate*min)+(Distance_Rate*km));
    }

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

    public static IGoogleMAPApi getGoogleService()
    {
        return GoogleMAPApi.getClient(googleAPIUrl).create(IGoogleMAPApi.class);
    }
    public static void sendRequestToDriver(String driverId, final IFCMService mFCMService, final Context context, final Location currentLocation) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference(Common.tokenTable);
        tokens.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postsnapshot:dataSnapshot.getChildren()){

                    Token token=postsnapshot.getValue(Token.class);
                    String rider_token= FirebaseInstanceId.getInstance().getToken();

                    Map<String,String> content = new HashMap<>();
                    if (rider_token != null) {
                        content.put("customer",rider_token);
                    }
                    content.put("lat",String.valueOf(currentLocation.getLatitude()));
                   content.put("lng",String.valueOf(currentLocation.getLongitude()));

                    DataMessage dataMessage = new DataMessage(token.getToken(),content);



                    mFCMService.sendMessage(dataMessage)
                            .enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                    if (response.body() !=null)
                                        if(response.body().success==1){
                                            Toast.makeText(context, "Request Sent", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(context,"Failed!",Toast.LENGTH_LONG).show();
                                        }
                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                    Log.e("fcm_error",t.getMessage());;
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }





}