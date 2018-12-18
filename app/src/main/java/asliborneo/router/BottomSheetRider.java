package asliborneo.router;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;




public class BottomSheetRider extends BottomSheetDialogFragment {
    String mLocation, mDestination;
    TextView txtLocation, txtDestination, txtDistance;

    IGoogleMAPApi mService;
    static boolean Tap_on_map;

    public static BottomSheetRider newinstance(String location, String destination) {
        BottomSheetRider f = new BottomSheetRider();
        Bundle args = new Bundle();
        args.putString("location", location);
        args.putString("destination", destination);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_rider, container, false);
        TextView txtLocation = (TextView) v.findViewById(R.id.location);
        TextView txtDestination = (TextView) v.findViewById(R.id.destination);
                 txtDistance = (TextView) v.findViewById(R.id.distance);

        mService = Commons.getGoogleService();
        getPrice(mLocation, mDestination);
        if (!Tap_on_map) {
            txtLocation.setText(mLocation);
            txtDestination.setText(mDestination);
        }
        return v;
    }

    private void getPrice(String mLocation, String mDestination) {
        String requestUrl = null;
        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&"
                    + "transit_routing_preference=less_driving&"
                    + "origin=" + mLocation + "&" + "destination=" + mDestination + "&"
                    + "key=" + getResources().getString(R.string.google_browser_api);

            Log.e("LINK", requestUrl);
            mService.getPath(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_text = distance.getString("text");

                        Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));


                        JSONObject time = legsObject.getJSONObject("duration");
                        String time_text = time.getString("text");
                        Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+", ""));

                        String final_calculate = String.format("%s + %s = RM%.2f", distance_text, time_text,
                                Commons.getPrice(distance_value, time_value));

                        txtDistance.setText(final_calculate);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("ERROR", t.getMessage());
                }
            });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}



