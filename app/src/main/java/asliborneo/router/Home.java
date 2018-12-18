package asliborneo.router;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
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
import com.google.maps.android.SphericalUtil;

import asliborneo.router.Model.Notification;
import asliborneo.router.Model.Rider;
import asliborneo.router.Model.Token;
import asliborneo.router.Model.fcm_response;
import asliborneo.router.Model.sender;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class Home extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer_layout;
    GoogleApiClient mgoogleApiclient;
    LocationRequest locationRequest;
    Location location;
    LatLng pickup_location;
    GoogleMap mMap;
    Marker mcurrent;
    String pick_up_location, destination_location;
    Marker pick_up_location_marker, destination_location_marker;
    ImageView expandable_image;
    Button place_pickup_request;
    NavigationView nav_view;
    AutocompleteFilter typefilter;
    int radius = 1;
    int distance = 3;
    PlaceAutocompleteFragment pick_up_place, destination_place;
    private static final int LIMIT = 3;
    DatabaseReference Driver_available_ref;
    private static final String TAG = "Home";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SupportMapFragment MapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment.getMapAsync(this);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();
        nav_view = (NavigationView) findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);
        place_pickup_request = (Button) findViewById(R.id.btnpickuprequest);
        place_pickup_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Commons.isDriverFound) {
                    request_pickup_here(FirebaseAuth.getInstance().getCurrentUser().getUid());
                } else {
                    sendmessagetodriver(Commons.driver_id);
                }


            }
        });
        pick_up_place = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.location);
        destination_place = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.destination);
        pick_up_place.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                pickup_location = place.getLatLng();
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
                pick_up_location = place.getAddress().toString();
                pick_up_location_marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Pick Up Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
            }

            @Override
            public void onError(Status status) {

            }
        });
        destination_place.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                destination_location = place.getAddress().toString();
                destination_location_marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
                bottom_sheet_rider_fragment bsrf = bottom_sheet_rider_fragment.newinstance(String.format("%f,%f", location.getLatitude(), location.getLongitude()), destination_location, false);
                bsrf.show(getSupportFragmentManager(), bsrf.getTag());
            }

            @Override
            public void onError(Status status) {

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        typefilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();
        mgoogleApiclient = new GoogleApiClient.Builder(Home.this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mgoogleApiclient.connect();
        init_location_request();
        update_firebase_token();

    }

    private void update_firebase_token() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);

    }

    private void sendmessagetodriver(String driver_id) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        tokens.orderByKey().equalTo(driver_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    String Lat_lng;
                    Token token = postsnapshot.getValue(Token.class);
                    Lat_lng = new Gson().toJson(new LatLng(location.getLatitude(), location.getLongitude()));
                    String rider_token = FirebaseInstanceId.getInstance().getToken();
                    Notification data = new Notification(rider_token, Lat_lng);
                    sender content = new sender(data, token.getToken());
                    FCMService fcmService = RetrofitClient.getClient().create(FCMService.class);
                    Call<fcm_response> call = fcmService.send_message(content);
                    call.enqueue(new Callback<fcm_response>() {
                        @Override
                        public void onResponse(Call<fcm_response> call, Response<fcm_response> response) {
                            if (response.body().success == 1) {
                                Toast.makeText(Home.this, "Request Sent", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Home.this, "Failed!", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<fcm_response> call, Throwable t) {
                            Log.e("fcm_error", t.getMessage());
                            ;
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void request_pickup_here(String uid) {
        DatabaseReference pickupreference = FirebaseDatabase.getInstance().getReference("Pick Up Request");
        GeoFire geoFire = new GeoFire(pickupreference);
        geoFire.setLocation(uid, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (mcurrent.isVisible())
                    mcurrent.remove();
                mcurrent = mMap.addMarker(new MarkerOptions().title("Pick Up Here").position(new LatLng(location.getLatitude(), location.getLongitude())).snippet("").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                mcurrent.showInfoWindow();
                place_pickup_request.setText("Getting Driver");
                find_driver();
            }
        });

    }

    private void find_driver() {
        DatabaseReference drivers_reference = FirebaseDatabase.getInstance().getReference("Drivers");
        GeoFire gfdrivers = new GeoFire(drivers_reference);
        final GeoQuery geoQuery = gfdrivers.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!Commons.isDriverFound) {
                    Commons.isDriverFound = true;
                    Commons.driver_id = key;
                    place_pickup_request.setText("Call Driver");
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
                if (!Commons.isDriverFound && radius < LIMIT) {
                    radius++;
                    find_driver();
                } else {
                    if (!Commons.isDriverFound) {
                        Toast.makeText(Home.this, "No Drivers available around", Toast.LENGTH_LONG).show();
                        place_pickup_request.setText("Request Pickup");
                        geoQuery.removeAllListeners();
                    }
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            boolean issucess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Home.this, R.raw.uber_style_map));
            if (!issucess)
                Toast.makeText(Home.this, "Error setting Map Style", Toast.LENGTH_LONG).show();
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new Custom_Info_Window(this));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (destination_location_marker != null)
                    destination_location_marker.remove();

                if (location !=null && latLng !=null)
                destination_location_marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)).title("Destination").position(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                bottom_sheet_rider_fragment bsrf = bottom_sheet_rider_fragment.newinstance(String.format("%f,%f", location.getLatitude(), location.getLongitude()), String.format("%f,%f", latLng.latitude, latLng.longitude), true);
                bsrf.show(getSupportFragmentManager(), bsrf.getTag());
            }
        });
        //googleMap.addMarker(new MarkerOptions().title("Rider Location").position(new LatLng(37.7750, -122.4183)));
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.7750, -122.4183), 15.0f));
    }

    private void display_location() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(mgoogleApiclient);
        if (location != null) {
            LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng northside = SphericalUtil.computeOffset(center, 100000, 0);
            LatLng southside = SphericalUtil.computeOffset(center, 100000, 180);
            LatLngBounds bounds = LatLngBounds.builder()
                    .include(northside)
                    .include(southside)
                    .build();
            pick_up_place.setBoundsBias(bounds);
            pick_up_place.setFilter(typefilter);
            destination_place.setBoundsBias(bounds);
            pick_up_place.setFilter(typefilter);
            Driver_available_ref = FirebaseDatabase.getInstance().getReference("Drivers");
            Driver_available_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    loadAvailabledriver(new LatLng(location.getLatitude(), location.getLongitude()));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            loadAvailabledriver(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    private void loadAvailabledriver(final LatLng mlocation) {
        mMap.clear();
        mcurrent = mMap.addMarker(new MarkerOptions().position(new LatLng(mlocation.latitude, mlocation.longitude)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mlocation.latitude, mlocation.longitude), 15.0f));
        DatabaseReference driverlocation = FirebaseDatabase.getInstance().getReference("Drivers");
        GeoFire gf = new GeoFire(driverlocation);
        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(mlocation.latitude, mlocation.longitude), distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference("DriverInformation").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Rider user = dataSnapshot.getValue(Rider.class);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(mlocation.latitude, mlocation.longitude)).title(user.getName()).snippet("Phone " + user.getPhone()).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
                if (distance <= LIMIT) {
                    distance++;
                    loadAvailabledriver(mlocation);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void get_location_updates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiclient, locationRequest, this);
    }

    private void init_location_request() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mgoogleApiclient.disconnect();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_signout) {
            Sign_Out();
        }
        return false;
    }

    private void Sign_Out() {
        Paper.init(this);
        Paper.book().destroy();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Home.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        display_location();
        get_location_updates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("location_connection", connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        display_location();
    }
}