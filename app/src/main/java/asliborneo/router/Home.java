package asliborneo.router;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModel;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import asliborneo.router.Commons.Common;
import asliborneo.router.Helper.Custom_Info_Window;
import asliborneo.router.JomRide.BottomSheetRider;
import asliborneo.router.JomRide.CallDriver;
import asliborneo.router.JomRide.DriverStatus;
import asliborneo.router.Model.Rider;
import asliborneo.router.Model.Token;
import asliborneo.router.Service.IFCMService;


import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;


public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, ValueEventListener, GoogleMap.OnMapClickListener{

    private static final int MY_PERMISSION_REQUEST_CODE = 11;

    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer_layout;

    private static final int RC_SIGN_IN = 123;


    private static final String TAG = "MainActivity";

    FirebaseFirestore mFirestore;
//    private static final int LIMIT = 50;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.text_current_search)
    TextView mCurrentSearchView;

    @BindView(R.id.text_current_sort_by)
    TextView mCurrentSortByView;

    @BindView(R.id.recycler_restaurants)
    RecyclerView mRestaurantsRecycler;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;;

    private Query mQuery;

    CardView filterBar;

    private ViewModel mViewModel;





    LatLng pickup_location;
    GoogleMap mMap;
    Marker mcurrent;
    String mPlaceLocation, mPlaceDestination;
    Marker pick_up_location_marker, destination_location_marker;
    IFCMService mFCMService;
    Button place_pickup_request;
    NavigationView nav_view;
    AutocompleteFilter typefilter;
    boolean isButtonClicked = false;
    View mapView;
    boolean isUberX=false;

    static int UPDATE_INTERVAL = 5000;
    static int FASTEST_INTERVAL = 3000;
    static int DISPLACEMENT = 10;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    CircleImageView imageAvatar;
    TextView txtRiderName, txtStars;
    ImageView teksiEnabled, teksi_disabled;
    boolean isTeksi = false;
    Button pin;
    private Menu menu;

    LocationRequest mLocationRequest;

    ImageView btnBack;
    FirebaseStorage storage;
    StorageReference storageReference;
    int radius = 1;
    int distance = 3;
    PlaceAutocompleteFragment place_location, place_destination;
    private static final int LIMIT = 3;
    DatabaseReference Driver_available_ref;
    private BroadcastReceiver mCancelBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            place_pickup_request.setText("REQUEST PICK UP");
        }
    };

    LatLng latLng;


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
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);


        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_shopping_cart_black_24dp);
        toolbar.setOverflowIcon(drawable);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        filterBar = findViewById(R.id.filter_bar);
        filterBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this,ListStore.class);
                startActivity(intent);

            }
        });

        mFCMService = Common.getFCMService();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCancelBroadCast, new IntentFilter(Common.CANCEL_BROADCAST));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.pin);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "screen captured", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        View navigationHeaderView = navigationView.getHeaderView(0);

        if (Common.currentUser != null) {

            txtRiderName = navigationHeaderView.findViewById(R.id.txtRiderName);
            txtRiderName.setText(String.format("%s", Common.currentUser.getName()));
            txtStars = navigationHeaderView.findViewById(R.id.txtStars);
            txtStars.setText(String.format("%s", Common.currentUser.getWallet()));
            imageAvatar = navigationHeaderView.findViewById(R.id.imageAvatar);
            if (Common.currentUser.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl())) {
                Picasso.with(Home.this).load(Common.currentUser.getAvatarUrl()).into(imageAvatar);
            }

            teksi_disabled = (ImageView) findViewById(R.id.teksi_disabled);
            teksiEnabled = (ImageView) findViewById(R.id.ic_enabled);

            teksiEnabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isTeksi = true;
                    if (Common.currentUser != null) {

                        teksiEnabled.setBackgroundColor(getResources().getColor(R.color.buttonPickupRequest));
                        teksi_disabled.setBackgroundColor(getResources().getColor(R.color.grey_300));
                        Toast.makeText(Home.this, "enabled clicked", Toast.LENGTH_SHORT).show();
                    } else {
                        teksiEnabled.setBackgroundColor(getResources().getColor(R.color.grey_300));
                        teksi_disabled.setBackgroundColor(getResources().getColor(R.color.buttonPickupRequest));
                    }
                    mMap.clear();
                    loadAvailabledriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                }
            });

            teksi_disabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!isTeksi) {
                        teksiEnabled.setBackgroundColor(getResources().getColor(R.color.buttonPickupRequest));
                        teksi_disabled.setBackgroundColor(getResources().getColor(R.color.grey_300));
                    } else {
                        teksiEnabled.setBackgroundColor(getResources().getColor(R.color.grey_300));
                        teksi_disabled.setBackgroundColor(getResources().getColor(R.color.buttonPickupRequest));
                    }
                    mMap.clear();
                    loadAvailabledriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                }
            });

            if (Common.currentUser.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl())) {
                Picasso.with(Home.this).load(Common.currentUser.getAvatarUrl()).into(imageAvatar);
            }
        }


        mFCMService = Common.getFCMService();

        place_pickup_request = (Button) findViewById(R.id.btnpickuprequest);
        place_pickup_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Common.isDriverFound) {
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            request_pickup_here(account.getId());
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {

                        }
                    });
                } else {

                    Common.sendRequestToDriver(Common.driverId, mFCMService, getBaseContext(), Common.mLastLocation);
                }


            }
        });

        place_location = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.location);

        place_location.setHint(" location");

        place_destination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.destination);


        place_destination.setHint(" destination");
        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                mPlaceLocation = place.getAddress().toString();
                destination_location_marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Pin Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

            }

            @Override
            public void onError(Status status) {

            }
        });

        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                mPlaceDestination = place.getAddress().toString();

                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17.0f));
                if (mPlaceDestination !=null) {
                    BottomSheetRider bottomSheetRider = BottomSheetRider.newInstance(mPlaceLocation, mPlaceDestination, false);
                    bottomSheetRider.show(getSupportFragmentManager(), bottomSheetRider.getTag());
                }
            }


            @Override
            public void onError(Status status) {

            }
        });
        place_location.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        place_destination.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        typefilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        updateFirebaseToken();
        setupLocation();

        showDialogUpdateCartype();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Home.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE,
            }, MY_PERMISSION_REQUEST_CODE);

        }
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
                Common.mLastLocation = locationResult.getLastLocation();
                Common.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size() - 1);
                displayLocation();
            }
        };
    }




    private void updateFirebaseToken()
    {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference tokens  = db.getReference(Common.tokenTable);
                if (Common.currentUser !=null) {
                    Token token = new Token(FirebaseInstanceId.getInstance().getToken());
                    tokens.child(account.getId()).setValue(token);
                }
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });
    }

//    private void sendRequestToDriver(String driver_id) {
//        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference(Commons.tokenTable);
//        tokens.orderByKey().equalTo(driver_id).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot postsnapshot:dataSnapshot.getChildren()){
//
//                    Token token=postsnapshot.getValue(Token.class);
//                    String json_lat_lng=new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
//                    String rider_token= FirebaseInstanceId.getInstance().getToken();
//                    Notification data=new Notification(rider_token,json_lat_lng);
//                    Sender content=new Sender(data,token.getToken());
//                    mFCMService.sendMessage(content)
//                            .enqueue(new Callback<FCMResponse>() {
//                                @Override
//                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
//                                    if(response.body().success==1){
//                                        Toast.makeText(Home.this,"Request Sent",Toast.LENGTH_LONG).show();
//                                    }else {
//                                        Toast.makeText(Home.this,"Failed!",Toast.LENGTH_LONG).show();
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<FCMResponse> call, Throwable t) {
//                                    Log.e("fcm_error",t.getMessage());;
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void request_pickup_here(String uid) {
        DatabaseReference pickupreference=FirebaseDatabase.getInstance().getReference("Pick Up Request");
        GeoFire geoFire=new GeoFire(pickupreference);
        geoFire.setLocation(uid, new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if(mcurrent !=null)
                    mcurrent.remove();
                mcurrent= mMap.addMarker(new MarkerOptions().title("Pick Up Here").position(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude())).snippet("").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                mcurrent.showInfoWindow();
                place_pickup_request.setText("Getting Driver");
                find_driver();
            }
        });

    }
    //    private void showDialogUpdateCartype() {
//        final AlertDialog.Builder updateInfo=new AlertDialog.Builder(Home.this);
//        updateInfo.setTitle("");
//        updateInfo.setMessage("PLEASE CHOOSE YOUR RIDE");
//        View carType=LayoutInflater.from(Home.this).inflate(R.layout.layout_update_car_type,null);
//
//        final RadioButton defaultCar= carType.findViewById(R.id.economy);
//        final RadioButton teksiDriver= carType.findViewById(R.id.luxury);
//
//
//        if(Common.currentUser !=null)
//            if (Common.currentUser.getCarType().equals("Economy"))
//                defaultCar.setChecked(true);
//
//            else
//            if (Common.currentUser.getCarType().equals("Luxury"))
//                teksiDriver.setChecked(true);
//
//
//
//
//        updateInfo.setView(carType);
//        updateInfo.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//
//                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
//                    @Override
//                    public void onSuccess(Account account) {
//                        Map<String,Object> updateInfo=new HashMap<>();
//                        if(teksiDriver.isChecked()) {
//                            updateInfo.put("carType", teksiDriver.getText().toString());
//                            Intent intent = new Intent(Home.this, CheckoutActivity.class);
//                            startActivity(intent);
//                            finish();
//                        }
//                        if (defaultCar.isChecked()) {
//                            updateInfo.put("carType", defaultCar.getText().toString());
//                            Intent intent = new Intent(Home.this, CheckoutActivity.class);
//                            startActivity(intent);
//                            finish();
//                        } else
//                            updateInfo.put("driverStatus", "NOT VERIFIED DRIVER");
//
//                        DatabaseReference driver_information_reference=FirebaseDatabase.getInstance().getReference(Common.Registered_driver);
//                        driver_information_reference.child(account.getId()).updateChildren(updateInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) {
//
//                                    showDriverStatus();
//
//                                }
//
//                            }
//                        });
//
//                        driver_information_reference.child(account.getId())
//                                .addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                        Common.currentUser = dataSnapshot.getValue(Rider.class);
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                    }
//                                });
//                    }
//
//                    @Override
//                    public void onError(AccountKitError accountKitError) {
//
//                    }
//                });
//
//
//
//            }
//
//        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//            }
//        }).show();
//    }
    private void showDialogUpdateCartype() {
        final AlertDialog.Builder updateInfo=new AlertDialog.Builder(Home.this);
        updateInfo.setTitle("");
        updateInfo.setMessage("PLEASE CHOOSE YOUR RIDE");
        View carType=LayoutInflater.from(Home.this).inflate(R.layout.layout_update_car_type,null);

        final RadioButton defaultCar= carType.findViewById(R.id.economy);
        final RadioButton teksiDriver= carType.findViewById(R.id.luxury);


        if(Common.currentUser !=null)
            if (Common.currentUser.getCarType().equals("Economy"))
                defaultCar.setChecked(true);

            else
            if (Common.currentUser.getCarType().equals("Luxury"))
                teksiDriver.setChecked(true);




        updateInfo.setView(carType);
        updateInfo.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final DatabaseReference[] driverLocation = new DatabaseReference[1];
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        Map<String,Object> updateInfo=new HashMap<>();
                        if(teksiDriver.isChecked()) {

                            FirebaseDatabase.getInstance().getReference(Common.driver_location).child("Economy");
                            updateInfo.put("carType", teksiDriver.getText().toString());
                            request_pickup_here(account.getId());

                        }
                        if (defaultCar.isChecked()) {
                            FirebaseDatabase.getInstance().getReference(Common.driver_location).child("Luxury");
                            updateInfo.put("carType", defaultCar.getText().toString());
                            request_pickup_here(account.getId());

                        } else
                            updateInfo.put("driverStatus", "NOT VERIFIED DRIVER");

                        DatabaseReference driver_information_reference=FirebaseDatabase.getInstance().getReference(Common.Registered_driver);
                        driver_information_reference.child(account.getId()).updateChildren(updateInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

//                                    showDriverStatus();

                                }

                            }
                        });

                        driver_information_reference.child(account.getId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Common.currentUser = dataSnapshot.getValue(Rider.class);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });



            }

        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void showDriverStatus() {
        Intent intent = new Intent(Home.this,DriverStatus.class);
        startActivity(intent);
    }

    private void find_driver() {
        DatabaseReference drivers_reference=FirebaseDatabase.getInstance().getReference("Drivers");
        GeoFire gfdrivers=new GeoFire(drivers_reference);
        final GeoQuery geoQuery=gfdrivers.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!Common.isDriverFound){
                    Common.isDriverFound=true;
                    Common.driverId=key;
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
                if(!Common.isDriverFound&&radius<LIMIT){
                    radius++;
                    find_driver();
                }else{
                    if(!Common.isDriverFound) {
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        enableMyLocation();

        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 420, 420);}
        try {
            boolean issucess = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Home.this, R.raw.mymapstyle));
            if (!issucess)
                Toast.makeText(Home.this, "Error setting Map Style", Toast.LENGTH_LONG).show();
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }

        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new Custom_Info_Window(this));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onMapClick(LatLng latLng) {
                if (place_location !=null && place_destination !=null)

                    mMap.clear();
                if(destination_location_marker !=null)
                    destination_location_marker.remove();
                destination_location_marker=    mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.des)).title("Destination").position(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));

                BottomSheetRider bottomSheetRider=BottomSheetRider.newInstance(String.format("%f,%f", Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), String.format("%f,%f", latLng.latitude, latLng.longitude),true);
                bottomSheetRider.show(getSupportFragmentManager(),bottomSheetRider.getTag());

            }
        });
        //googleMap.addMarker(ic_new MarkerOptions().title("Rider Location").position(ic_new LatLng(37.7750, -122.4183)));
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ic_new LatLng(37.7750, -122.4183), 15.0f));
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
                if (Common.mLastLocation != null) {
                    final double latitude = Common.mLastLocation.getLatitude();
                    final double longitude = Common.mLastLocation.getLongitude();
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

                    Driver_available_ref = FirebaseDatabase.getInstance().getReference(Common.driver_location);
                    Driver_available_ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            loadAvailabledriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    loadAvailabledriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                }
            }
        });
    }

    private void loadAvailabledriver(final LatLng location) {
        mMap.clear();


        // mcurrent = mMap.addMarker(new MarkerOptions().position(location).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude, location.longitude), 15.0f));
        DatabaseReference driverlocation=FirebaseDatabase.getInstance().getReference("Drivers");
        GeoFire gf=new GeoFire(driverlocation);
        mcurrent = mMap.addMarker(new MarkerOptions().position(location)
                .title(getResources().getString(R.string.you))
        );
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));




        if (isUberX)

            driverlocation = FirebaseDatabase.getInstance().getReference(Common.driver_location).child("routeMyId").child("driverStatus").child("platNumber");
        else
            driverlocation = FirebaseDatabase.getInstance().getReference(Common.driver_location).child("routeMyId").child("driverStatus").child("platNumber");


        GeoQuery geoQuery=gf.queryAtLocation(new GeoLocation(location.latitude,location.longitude),distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference("DriverInformation").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Rider user=dataSnapshot.getValue(Rider.class);
                        String name;
                        String platNumber;
                        String phone;
                        String status;

                        if(user.getName()!=null) name=user.getName();
                        else name="NOT VERIFIED DRIVER";

                        if (user.getPhone()!=null)phone="Phone: "+user.getPhone();
                        else phone="Phone: Unknown";
                        if (user.getPlatNumber()!=null)platNumber="platNumber: "+user.getPlatNumber();
                        else platNumber="platNumber: Unknown";

                        mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude,location.longitude)).title(user.getName()).snippet("Phone "+dataSnapshot.getKey()).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
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



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_cart) {

        } else if (id ==R.id.nav_orders) {

        } else if (id == R.id.nav_backjomeat) {
            finish();
        }
        if (item.getItemId() == R.id.nav_signout) {
            Sign_Out();

        } else if (id == R.id.nav_updateInformation) {
            showUpdateInformationDialog();
        }
//        if (id == R.id.nav_update_cartype) {
//
//
//        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                        DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.Registered_Riders);
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
        startActivityForResult(Intent.createChooser(intent, "Select picture"), Common.PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Common.PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
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

                                                DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.Registered_Riders);
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

        if (!marker.getTitle().equals("You") && !marker.getTitle().equals(this)) {
            Intent intent = new Intent(Home.this, CallDriver.class);
            intent.putExtra("driverId", marker.getSnippet().replaceAll("\\D+", ""));
            intent.putExtra("lat", Common.mLastLocation.getLatitude());
            intent.putExtra("lng", Common.mLastLocation.getLongitude());
            startActivity(intent);
        }
    }


    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        loadAvailabledriver(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCancelBroadCast);
        super.onDestroy();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        pin = findViewById(R.id.pin);
        pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }



}