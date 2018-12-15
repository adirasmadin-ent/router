package asliborneo.router;

import android.location.Location;

import retrofit2.Retrofit;

public class Commons {
public static Location mLastLocation;

    public static final String driver_location="Drivers";
    public static final String Registered_driver="DriverInformation";
    public static final String Registered_Riders="RidersInformation";
    public static final String pickUpRequest_tbl="PickUpRequest";
    private static double Base_Fare=2.55;
    private static double Time_Rate=0.35;
    private static double Distance_Rate=1.75;
    public  static  Boolean isDriverFound=false;
    public static String  driver_id="";

    public static String fcmURL = "https://fcm.google.com/";
    public static final java.lang.String user_field="usr";
    public static final java.lang.String password_field="pwd";
    public static double getPrice(double km,int min){
        return (Base_Fare+(Time_Rate*min)+(Distance_Rate*km));
    }




}