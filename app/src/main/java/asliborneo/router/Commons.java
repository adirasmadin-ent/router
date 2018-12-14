package asliborneo.router;

public class Commons {
    public static final String driver_location="Drivers";
    public static final String Registered_driver="DriverInformation";
    public static final String Registered_Riders="RidersInformation";
    public static final String Pickup_Request="PickUpRequest";
    private static double Base_Fare=2.55;
    private static double Time_Rate=0.35;
    private static double Distance_Rate=1.75;
    public  static  Boolean isDriveravailable=false;
    public static String  driver_id="";
    public static final java.lang.String user_field="usr";
    public static final java.lang.String password_field="pwd";
    public static double getPrice(double km,int min){
        return (Base_Fare+(Time_Rate*min)+(Distance_Rate*km));
    }


}