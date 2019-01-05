package asliborneo.router;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import asliborneo.router.Model.Notification;
import asliborneo.router.Model.Rider;
import asliborneo.router.Model.Token;
import asliborneo.router.Model.FCMResponse;
import asliborneo.router.Model.Sender;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static asliborneo.router.Commons.driverId;
import static asliborneo.router.Commons.fcmURL;



public class Home extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,  GoogleMap.OnInfoWindowClickListener  {
    private static final int MY_PERMISSION_REQUEST_CODE = 1;
    private static final int PLAY_SERVICE_RESOLUTION_REQUEST =10 ;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer_layout;
    GoogleApiClient mgoogleApiclient;
    LocationRequest locationRequest;
    Location mLastLocation;
    LatLng pickup_location;
    GoogleMap mMap;
    Marker mcurrent;
    String mPlaceLocation,mPlaceDestination;
    Marker pick_up_location_marker,destination_location_marker;
    IFCMService mFCMService;
    Button place_pickup_request;
    NavigationView nav_view;
    AutocompleteFilter typefilter;
    BottomSheetRider bottomSheetRider;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    CircleImageView imageAvatar;
    TextView txtRiderName, txtStars;
    ImageView teksi_enabled,teksi_disabled;
    boolean isTeksi =true;

    LocationRequest mLocationRequest;
    Marker mUserMarker;

    FirebaseStorage storage;
    StorageReference storageReference;
    int radius=1;
    int distance=3;
    PlaceAutocompleteFragment place_location,place_destination;
    private static final int LIMIT=3;
    DatabaseReference Driver_available_ref;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SupportMapFragment MapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert MapFragment != null;
        MapFragment.getMapAsync(this);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();
        nav_view=(NavigationView) findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        View navigationHeaderView = navigationView.getHeaderView(0);

        if (Commons.currentUser != null) {
            txtRiderName = navigationHeaderView.findViewById(R.id.txtRiderName);
            txtRiderName.setText(String.format("%s", Commons.currentUser.getName()));
            txtStars = navigationHeaderView.findViewById(R.id.txtStars);
            txtStars.setText(String.format("%s", Commons.currentUser.getRates()));
            imageAvatar = navigationHeaderView.findViewById(R.id.imageAvatar);
            if (Commons.currentUser.getAvatarUrl() != null && !TextUtils.isEmpty(Commons.currentUser.getAvatarUrl())) {
                Picasso.with(Home.this).load(Commons.currentUser.getAvatarUrl()).into(imageAvatar);
            }

            teksi_disabled = (ImageView) findViewById(R.id.teksi_disabled);
            teksi_enabled= (ImageView) findViewById(R.id.teksi_enabled);

            teksi_enabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isTeksi=true;
                    if(isTeksi)
                    {
                        teksi_enabled.setImageResource(R.id.teksi_enabled);
                        teksi_disabled.setImageResource(R.id.teksi_disabled);
                    }
                    else
                    {
                        teksi_enabled.setImageResource(R.id.teksi_disabled);
                        teksi_disabled.setImageResource(R.id.teksi_enabled);
                    }
                    mMap.clear();
                    loadAvailabledriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                }
            });

            teksi_disabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isTeksi=true;
                    if(isTeksi)
                    {
                        teksi_enabled.setImageResource(R.id.teksi_enabled);
                        teksi_disabled.setImageResource(R.id.teksi_disabled);
                    }
                    else
                    {
                        teksi_enabled.setImageResource(R.id.teksi_disabled);
                        teksi_disabled.setImageResource(R.id.teksi_enabled);
                    }
                    mMap.clear();
                    loadAvailabledriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                }
            });

            if (Commons.currentUser.getAvatarUrl() != null && !TextUtils.isEmpty(Commons.currentUser.getAvatarUrl())) {
                Picasso.with(Home.this).load(Commons.currentUser.getAvatarUrl()).into(imageAvatar);
            }
        }

        mFCMService = Commons.getFCMService();

        place_pickup_request=(Button) findViewById(R.id.btnpickuprequest);
        place_pickup_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Commons.isDriverFound) {
                   AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                       @Override
                       public void onSuccess(Account account) {
                           request_pickup_here(account.getId());
                       }

                       @Override
                       public void onError(AccountKitError accountKitError) {

                       }
                   });
                } else{
                    Commons.sendRequestToDriver(Commons.driverId,mFCMService,getBaseContext(),mLastLocation);
                }


            }
        });

        place_location = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.location);
        place_destination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.destination);
        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                mPlaceLocation = place.getAddress().toString();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Pin Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.loc)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

            }

            @Override
            public void onError(Status status) {

            }
        });
        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                mPlaceDestination=place.getAddress().toString();
                destination_location_marker=mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.des)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));
                BottomSheetRider bottomSheetRider=BottomSheetRider.newInstance(mPlaceLocation,mPlaceDestination,false);
                bottomSheetRider.show(getSupportFragmentManager(),bottomSheetRider.getTag());

            }

            @Override
            public void onError(Status status) {

            }
        });
        typefilter=new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

       UpdateserverToken();
        setupLocation();

    }
    private void UpdateserverToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference tokens = db.getReference("Tokens");


        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Token token = new Token(instanceIdResult.getToken());
                        tokens.child(account.getId())
                                .setValue(token);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ERROR TOKEN", e.getMessage());
                        Toast.makeText(Home.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.d("ERROR ACCOUNTKIT", accountKitError.getUserFacingMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupLocation();
                }
        }
    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }


    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Home.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE,
            }, MY_PERMISSION_REQUEST_CODE);

        } else {
            buildLocationRequest();
            buildLocationCallback();
            displayLocation();
            enableMyLocation();
        }
    }


    private void buildLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocation = locationResult.getLastLocation();
                mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size() - 1);
                displayLocation();
            }
        };
    }

    private void request_pickup_here(String uid) {
        DatabaseReference pickupreference=FirebaseDatabase.getInstance().getReference("Pick Up Request");
        GeoFire geoFire=new GeoFire(pickupreference);
        geoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if(mcurrent !=null)
                    mcurrent.remove();
                mcurrent= mMap.addMarker(new MarkerOptions().title("Pick Up Here").position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude())).snippet("").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                mcurrent.showInfoWindow();
                place_pickup_request.setText("Getting Driver");
                find_driver();
            }
        });

    }

    private void find_driver() {
        DatabaseReference drivers_reference=FirebaseDatabase.getInstance().getReference(Commons.driver_location);
        GeoFire gfdrivers=new GeoFire(drivers_reference);
        final GeoQuery geoQuery=gfdrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!Commons.isDriverFound){
                    Commons.isDriverFound=true;
                    Commons.driverId=key;
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
                if(!Commons.isDriverFound&&radius<LIMIT){
                    radius++;
                    find_driver();
                }else{
                    if(!Commons.isDriverFound) {
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
        mMap=googleMap;
        try {
            boolean issucess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Home.this, R.raw.mymapstyle));
            if (!issucess)
                Toast.makeText(Home.this, "Error setting Map Style", Toast.LENGTH_LONG).show();
        }catch(Resources.NotFoundException ex){ex.printStackTrace();}
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new Custom_Info_Window(this));
        mMap.setMyLocationEnabled(true);
        enableMyLocation();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (place_location !=null && place_destination !=null)
                    if(destination_location_marker !=null)
                        destination_location_marker.remove();
                destination_location_marker=mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)).title("Destination").position(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));
                BottomSheetRider bottomSheetRider=BottomSheetRider.newInstance(String.format("%f,%f", mLastLocation.getLatitude(), mLastLocation.getLongitude()), String.format("%f,%f", latLng.latitude, latLng.longitude),true);
                bottomSheetRider.show(getSupportFragmentManager(),bottomSheetRider.getTag());
            }
        });
        //googleMap.addMarker(new MarkerOptions().title("Rider Location").position(new LatLng(37.7750, -122.4183)));
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.7750, -122.4183), 15.0f));
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, MY_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }



    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (mLastLocation != null) {
                    final double latitude = mLastLocation.getLatitude();
                    final double longitude = mLastLocation.getLongitude();
                    LatLng center = new LatLng(latitude, longitude);
                    LatLng northside = SphericalUtil.computeOffset(center, 100000, 0);
                    LatLng southside = SphericalUtil.computeOffset(center, 100000, 180);
                    LatLngBounds bounds = LatLngBounds.builder()
                            .include(northside)
                            .include(southside)
                            .build();
                    place_location.setBoundsBias(bounds);
                    place_location.setFilter(typefilter);
                    place_destination.setBoundsBias(bounds);
                    place_location.setFilter(typefilter);

                    Driver_available_ref = FirebaseDatabase.getInstance().getReference(Commons.driver_location);
                    Driver_available_ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            loadAvailabledriver(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    loadAvailabledriver(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                }
            }
        });

    }

    private void loadAvailabledriver(final LatLng location) {
        mMap.clear();
        // mcurrent = mMap.addMarker(new MarkerOptions().position(location).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude, location.longitude), 15.0f));
        DatabaseReference driverlocation=FirebaseDatabase.getInstance().getReference(Commons.driver_location);
        GeoFire gf=new GeoFire(driverlocation);
        GeoQuery geoQuery=gf.queryAtLocation(new GeoLocation(location.latitude,location.longitude),distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference(Commons.Registered_driver).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Rider user=dataSnapshot.getValue(Rider.class);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude,location.longitude)).title(user.getName()).snippet("Driver ID "+dataSnapshot.getKey()).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
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
                if (distance<=LIMIT){
                    distance++;
                    loadAvailabledriver(location);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }






    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == R.id.nav_signout) {
            Sign_Out();

        } else if (id == R.id.nav_updateInformation) {
            showUpdateInformationDialog();
        }


        return false;
    }

    private void showUpdateInformationDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Information");
        alertDialog.setMessage("Please fill information");

        LayoutInflater inflater = this.getLayoutInflater();
        View update_info_layout = inflater.inflate(R.layout.layout_update_information, null);


        final MaterialEditText nameTxt = update_info_layout.findViewById(R.id.nametxt);
        final MaterialEditText phoneTxt = update_info_layout.findViewById(R.id.phonetxt);
        final ImageView imgAvatar = update_info_layout.findViewById(R.id.imgAvatar);


        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImageAndUpload();
            }
        });

        alertDialog.setView(update_info_layout);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                String name = nameTxt.getText().toString();
                String phone = phoneTxt.getText().toString();

                final Map<String, Object> update = new HashMap<>();
                if (!TextUtils.isEmpty(name))
                    update.put("name", name);
                if (!TextUtils.isEmpty(phone))
                    update.put("phone", phone);

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Commons.Registered_Riders);
                        riderInformation.child(account.getId())
                                .updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                waitingDialog.dismiss();
                                if (task.isSuccessful())
                                    Toast.makeText(Home.this, "Information Updated!", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(Home.this, "Information update failed", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });

            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void chooseImageAndUpload() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), Commons.PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Commons.PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            Uri saveUri = data.getData();
            if (saveUri != null) {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Uploading...");
                progressDialog.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("call/" + imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();


                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(final Uri uri) {
                                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                            @Override
                                            public void onSuccess(Account account) {
                                                Map<String, Object> update = new HashMap<>();
                                                update.put("avatarUrl", uri.toString());

                                                DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Commons.Registered_Riders);
                                                riderInformation.child(account.getId())
                                                        .updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                            Toast.makeText(Home.this, "Avatar Updated!", Toast.LENGTH_LONG).show();
                                                        else
                                                            Toast.makeText(Home.this, "Avatar update failed", Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onError(AccountKitError accountKitError) {

                                            }
                                        });
                                    }
                                });
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded" + progress);

                    }
                });
            }
        }
    }


    private void Sign_Out() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

        else
            builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure ?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AccountKit.logOut();
                        Intent intent = new Intent(Home.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (!marker.getTitle().equals("You")) {
            Intent intent = new Intent(Home.this, CallDriver.class);
            intent.putExtra("driverId", marker.getSnippet().replaceAll("\\D+", ""));
            intent.putExtra("lat", mLastLocation.getLatitude());
            intent.putExtra("lng", mLastLocation.getLongitude());
            startActivity(intent);
        }
    }


}