package asliborneo.router.JomRide;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import asliborneo.router.Commons.Common;
import asliborneo.router.Model.Rider;
import asliborneo.router.R;
import asliborneo.router.Service.IFCMService;
import de.hdodenhof.circleimageview.CircleImageView;


public class CallDriver extends AppCompatActivity {


    CircleImageView image_avatar;
    TextView txt_name,txt_phone,txt_rate;
    Button btn_call_driver,btn_msg_driver,btn_cancel;

    String driverId ;
    Location mLastLocation;

    IFCMService mFCMService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_driver);

        mFCMService = Common.getFCMService();

        image_avatar = findViewById(R.id.image_avatar);
        txt_name = (TextView) findViewById(R.id.txt_name);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_rate = (TextView) findViewById(R.id.txt_rate);

        btn_call_driver = (Button)findViewById(R.id.btn_call_driver);
        btn_msg_driver = (Button) findViewById( R.id.btn_msg_driver);
        btn_cancel = (Button) findViewById( R.id.btn_cancel);


        btn_call_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" +txt_phone.getText().toString()));



                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }
                startActivity(intent);
            }
        });

        if (getIntent() !=null)
        {
            driverId=getIntent().getStringExtra("driverId");
            double lat = getIntent().getDoubleExtra("lat",-1.0);
            double lng = getIntent().getDoubleExtra("lng",-1.0);

            mLastLocation = new Location("");
            mLastLocation.setLatitude(lat);
            mLastLocation.setLongitude(lng);



        }

        btn_msg_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (driverId !=null && !driverId.isEmpty())
                    Common.sendRequestToDriver(driverId,mFCMService,getBaseContext(),mLastLocation);


                finish();

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              finish();

            }
        });
        loadDriverInfo(driverId);


    }


    private void loadDriverInfo(String driverId) {
        FirebaseDatabase.getInstance()
                .getReference(Common.Registered_driver)
                .child(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Rider  driverUser = dataSnapshot.getValue(Rider.class);

                        if (driverUser != null && !driverUser.getAvatarUrl().isEmpty()) {
                            Picasso.with(getBaseContext())
                                    .load(driverUser.getAvatarUrl())
                                    .into(image_avatar);

                            txt_name.setText(driverUser.getName());
                            txt_phone.setText(driverUser.getPhone());
                            txt_rate.setText(driverUser.getRates());


                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
