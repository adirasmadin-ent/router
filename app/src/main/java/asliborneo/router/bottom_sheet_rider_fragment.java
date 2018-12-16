package asliborneo.router;


import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class bottom_sheet_rider_fragment extends BottomSheetDialogFragment {

    String mLocation,mDestination;
    TextView txtLocation,txtDestination,txtdistance;
    static boolean Tap_on_map;
    String final_calculate;

    public static bottom_sheet_rider_fragment newInstance(String location,String destination) {
        Bundle args = new Bundle();
        bottom_sheet_rider_fragment bottomSheetRiderFragment = new bottom_sheet_rider_fragment();
        args.putString("location", location);
        args.putString("destination", destination);

        bottomSheetRiderFragment.setArguments(args);
        return bottomSheetRiderFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");



    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_rider, container, false);
        txtLocation = view.findViewById(R.id.txtlocation);
        txtDestination = view.findViewById(R.id.txtdestination);
        txtdistance = view.findViewById(R.id.txtdistance);

        getPrice(mLocation,mDestination);

        if (!Tap_on_map) {
            txtLocation.setText(mLocation);
            txtDestination.setText(mDestination);
        }

        return view;
    }
    private void getPrice(String mLocation, String mDestination) {
        IGoogleAPI service=RetrofitClient.get_direction_client().create(IGoogleAPI.class);
        Call<Directions> call=service.getPath("driving","less_driving",mLocation,mDestination,"AIzaSyDWnTae7WYgjfAEFgWav4xHsv8X__NFSRc");
        call.enqueue(new Callback<Directions>() {
            @Override
            public void onResponse(Call<Directions> call, Response<Directions> response) {
                if(response.body()!=null)
                    if( response.body().routes.size()>0)
                        if(response.body().routes.get(0).legs.size()>0) {
                    String distance_text = response.body().routes.get(0).legs.get(0).distance.text;
                    double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));
                    String time_text = response.body().routes.get(0).legs.get(0).duration.text;
                    Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+", ""));

                    if(final_calculate !=null)
                   final_calculate= String.format(getString(R.string.final_calculate), distance_text, time_text, Commons.getPrice(distance_value, time_value));
                    txtdistance.setText(final_calculate);
                    txtdistance.setText(String.format("%s km",Commons.getPrice(distance_value,time_value)));

                    String start_address=response.body().routes.get(0).legs.get(0).start_address;
                    String end_address=response.body().routes.get(0).legs.get(0).end_address;
                  if (Tap_on_map)
                    txtLocation.setText(start_address);
                    txtDestination.setText(end_address);



                }else{
                    Log.e("cost_response",response.toString());
                }
            }

            @Override
            public void onFailure(Call<Directions> call, Throwable t) {
                Log.e("cost_error",t.getMessage());
            }
        });
    }
}