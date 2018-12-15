package asliborneo.router;


import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;


import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import com.google.gson.Gson;

import com.google.android.gms.location.LocationListener;


import asliborneo.router.Model.Notification;
import asliborneo.router.Model.Token;
import asliborneo.router.Model.User;
import asliborneo.router.Model.fcm_response;
import asliborneo.router.Model.sender;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {


    SupportMapFragment mapFragment;

    //Location
    private GoogleMap mMap;

    private static final int PLAY_SERVICE_RESOLUTION_REQUEST = 7002;
    private static final int MY_PERMISSION_REQUEST_CODE = 7003;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    PlaceAutocompleteFragment place_location,place_destination;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;



    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;



    Marker mUserMarker,destinationMarker;

    String mLocation,mDestination;
    ImageView imgExpandable;

    Button btnPickupRequest;
    boolean isDriverFound = false;
    String driverId = "" ;
    int radius = 1;
    int distance = 1;
    private static final int LIMIT = 3;
    AutocompleteFilter typeFilter;
    DatabaseReference driversAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // mGoogleApiClient = new GoogleApiClient.Builder(this)
        //        .enableAutoManage(this, 0 /* clientId */, this)
        //      .addApi(Places.GEO_DATA_API)
        //    .build();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        imgExpandable = findViewById(R.id.imgexpandable);

        place_location=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.location);
        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                mLocation  =place.getAddress().toString();
                Commons.mLastLocation.setLatitude(place.getLatLng().latitude);
                Commons.mLastLocation.setLongitude(place.getLatLng().longitude);
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("PICKUP HERE").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));


            }

            @Override
            public void onError(Status status) {

            }
        });

        place_destination = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.destination);
        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                mDestination = place.getAddress().toString();

                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)).position(place.getLatLng()).title("PLACE DESTINATION"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

                bottom_sheet_rider_fragment bottomSheetRiderFragment=bottom_sheet_rider_fragment.newInstance(mLocation,mDestination,false);
                bottomSheetRiderFragment.show(getSupportFragmentManager(),bottomSheetRiderFragment.getTag());

            }

            @Override
            public void onError(Status status) {

            }
        });

        typeFilter=new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        btnPickupRequest = findViewById(R.id.btnpickuprequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isDriverFound)
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                else
                    sendRequestToDriver(driverId);
            }


        });

        update_firebase_token();

        buildGoogleApiClient();
        createLocationRequest();
    }

    private void update_firebase_token() {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token token=new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);

    }

    private void sendRequestToDriver(String driverId) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        tokens.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postsnapshot:dataSnapshot.getChildren()){
                    String Lat_lng;
                    Token token=postsnapshot.getValue(Token.class);
                    Lat_lng=new Gson().toJson(new LatLng(Commons.mLastLocation.getLatitude(),Commons.mLastLocation.getLongitude()));
                    String rider_token= FirebaseInstanceId.getInstance().getToken();
                    Notification data=new Notification(rider_token,Lat_lng);
                    sender content=new sender(data,token.getToken());
                    FCMService fcmService=RetrofitClient.getClient().create(FCMService.class);
                    Call<fcm_response> call=fcmService.send_message(content);
                    call.enqueue(new Callback<fcm_response>() {
                        @Override
                        public void onResponse(Call<fcm_response> call, Response<fcm_response> response) {
                            if(response.body().success==1){
                                Toast.makeText(Home.this,"Request Sent",Toast.LENGTH_LONG).show();
                            }else {
                                Toast.makeText(Home.this,"Failed!",Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<fcm_response> call, Throwable t) {
                            Log.e("fcm_error",t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void requestPickupHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Commons.pickUpRequest_tbl);

        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(Commons.mLastLocation.getLatitude(), Commons.mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (mUserMarker.isVisible())
                    mUserMarker.remove();

                mUserMarker = mMap.addMarker(new MarkerOptions()
                        .title("Pickup Here")
                        .snippet(" ")
                        .position(new LatLng(Commons.mLastLocation.getLatitude(), Commons.mLastLocation.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));

                mUserMarker.showInfoWindow();
                findDriver();
                btnPickupRequest.setText( "GETTING DRIVER ...");
            }
        });

    }




    private void findDriver() {
        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Commons.driver_location);
        GeoFire gfDrivers = new GeoFire(drivers);
        GeoQuery geoQuery =gfDrivers.queryAtLocation(new GeoLocation(Commons.mLastLocation.getLatitude(),Commons.mLastLocation.getLongitude()),
                radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!isDriverFound) {
                    isDriverFound = true;
                    driverId = key;
                    btnPickupRequest.setText("CALL DRIVER");
                    Toast.makeText(Home.this,""+key,Toast.LENGTH_SHORT).show();

                }
            }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!isDriverFound)
                {
                    radius++;
                    findDriver();
                    loadAllAvailableDriver(new LatLng(Commons.mLastLocation.getLatitude(),Commons.mLastLocation.getLongitude()));
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void startLocationUpdates() {
        //int a = PackageManager.PERMISSION_DENIED;  //careful with logic here

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    private void setUpLocation() {
        if(  (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED   ) &&
                ( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED  ) )
        {
            //request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
            }, MY_PERMISSION_REQUEST_CODE );
        } else {
            if(checkPlayServices()){


                displayLocation();


            }
        }

    }

    private void displayLocation() {
        if(  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED    &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED   )
        {
            return;
        }



        Commons.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(Commons.mLastLocation != null)
        {

            driversAvailable = FirebaseDatabase.getInstance().getReference(Commons.driver_location);
            driversAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    loadAllAvailableDriver(new LatLng(Commons.mLastLocation.getLatitude(),Commons.mLastLocation.getLongitude()));
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final double latitude = Commons.mLastLocation.getLatitude();
            final double longitude = Commons.mLastLocation.getLongitude();

            //update to firebase

            //Add marker
            if(mUserMarker != null) {
                mUserMarker.remove();
            }//remove already marker
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title("Your Location"));
            //move cam to this position
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
            //Draw animation rotate marker

            loadAllAvailableDriver( new LatLng(Commons.mLastLocation.getLatitude(),Commons.mLastLocation.getLongitude())) ;



            Log.d("Welcome", String.format("Your location was changed: %f/%f",latitude,longitude));
        } else {
            Log.d("ERROR", "Cannot get your location");
        }

    }

    private void loadAllAvailableDriver(final LatLng location) {
        mMap.clear();

            mMap.addMarker(new MarkerOptions().position(location).title("You"));


        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Commons.driver_location);
        GeoFire gf = new GeoFire(driverLocation);
        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude,location.longitude),distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference(Commons.Registered_driver)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                if(user !=null)

                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(location.latitude,location.longitude))
                                            .flat(true)
                                            .title(user.getName())
                                            .snippet("Phone: "+user.getPhone())
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(distance <=LIMIT)
                    distance++;

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval( UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RESOLUTION_REQUEST).show();
            }else{
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap =googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new Custom_Info_Window(this));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (destinationMarker !=null)
                {
                    destinationMarker.remove();
                }

                if(mLocation != null && mDestination !=null)
                destinationMarker=mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)).title("Destination").position(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));
                bottom_sheet_rider_fragment bottomSheetRiderFragment = bottom_sheet_rider_fragment.newInstance(String.format("%f,%f", Commons.mLastLocation.getLatitude(), Commons.mLastLocation.getLongitude()),String.format("%f,%f", latLng.latitude, latLng.longitude) ,true);
                bottomSheetRiderFragment.show(getSupportFragmentManager(),bottomSheetRiderFragment.getTag());
            }
        });


    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if(item.getItemId()==R.id.nav_signout){
            Sign_Out();
        }
        return false;
    }


    private void Sign_Out() {
        Paper.init(this);
        Paper.book().destroy();
        FirebaseAuth.getInstance().signOut();
        Intent intent=new Intent(Home.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        Commons.mLastLocation = location;
        displayLocation();
    }


}
