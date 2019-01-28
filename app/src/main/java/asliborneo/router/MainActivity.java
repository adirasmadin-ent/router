package asliborneo.router;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



import com.rengwuxian.materialedittext.MaterialEditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import asliborneo.router.Commons.Common;
import asliborneo.router.Model.Rider;
import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import static android.content.pm.PackageManager.GET_SIGNATURES;

public class MainActivity extends AppCompatActivity {
    private GoogleApiClient googleApiClient;
    public static final int SIGN_IN_CODE_GOOGLE = 157;
    FButton btnLogIn;
    TextView txtForgotPassword;


    FButton btnContinue;
    private static final int REQUEST_CODE = 11;

    FirebaseAuth auth;
    FirebaseDatabase db;
    private static final String TAG = "MainActivity";


    private AdView mAdView;

    DatabaseReference users;
    MaterialEditText email, password, name, phone;
    RelativeLayout rootlayout;
    static String extractNumer;

    TextView txt_forgot_password, txtRegister, txtSignin;

    TextView txtUrl;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/NotoSans.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.Registered_driver);
        rootlayout = findViewById(R.id.rootlayout);
        rootlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,Home.class);
                startActivity(intent);

            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        Common.currentUser = new asliborneo.router.Model.Rider();

        if (AccountKit.getCurrentAccessToken() != null) {
            AccessToken accessToken = AccountKit.getCurrentAccessToken();
            if (accessToken != null) {
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                                 @Override
                                                 public void onSuccess(Account account) {
                                                     users.child(account.getPhoneNumber().toString())
                                                             .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                 @Override
                                                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                     Common.currentUser = dataSnapshot.getValue(Rider.class);
                                                                     Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                                                     startActivity(homeIntent);

                                                                 }


                                                                 @Override
                                                                 public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                 }
                                                             });
                                                 }

                                                 @Override
                                                 public void onError(AccountKitError accountKitError) {

                                                 }

                                             }

                );
            }
        } else {
            phoneLogin();
        }

    }


    public void phoneLogin() {
        final Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, REQUEST_CODE);
        printKeyHash();
    }

    private void printKeyHash() {
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo("asliborneo.router",
                    GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        if (AccountKit.getCurrentAccessToken() != null) {
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {


                    users.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Common.currentUser = dataSnapshot.getValue(asliborneo.router.Model.Rider.class);
                                    Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                    startActivity(homeIntent);
                                    finish();
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
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                showErrorActivity(loginResult.getError());
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
            } else {
                if (loginResult.getAccessToken() != null) {
                    toastMessage = "Success:" + loginResult.getAccessToken().getAccountId();
                } else {
                    toastMessage = String.format(
                            "Success:%s...",
                            loginResult.getAuthorizationCode().substring(0, 10));
                }


            }

            // Surface the result to your user in an appropriate way.
            Toast.makeText(
                    this,
                    toastMessage,
                    Toast.LENGTH_LONG)
                    .show();
            if (requestCode == REQUEST_CODE) {
                AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
                if (result.getError() != null) {
                    Toast.makeText(this, "" + result.getError().getErrorType().getMessage(), Toast.LENGTH_LONG).show();

                } else if (result.wasCancelled()) {
                    Toast.makeText(this, "Cancel login", Toast.LENGTH_LONG).show();
                } else {
                    if (result.getAccessToken() != null) {
                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                            @Override
                            public void onSuccess(final Account account) {
                                final String userPhone = account.getId();


                                users.orderByKey().equalTo(userPhone)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.child(userPhone).exists()) {
                                                    asliborneo.router.Model.Rider user = new asliborneo.router.Model.Rider();
                                                    user.setPhone(account.getPhoneNumber().toString());
                                                    user.setName(account.getPhoneNumber().toString());
                                                    user.setAvatarUrl("");
                                                    user.setRates("0.0");
                                                    user.setCarType("Economy");

                                                    users.child(userPhone)
                                                            .setValue(user)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    users.child(userPhone)
                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                    Common.currentUser = dataSnapshot.getValue(asliborneo.router.Model.Rider.class);
                                                                                    Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                                                                    startActivity(homeIntent);
                                                                                    finish();
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                }
                                                                            });
                                                                }


                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    users.child(userPhone)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    Common.currentUser=dataSnapshot.getValue(asliborneo.router.Model.Rider.class);
                                                                    Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                                                    startActivity(homeIntent);
                                                                    finish();
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                                }


                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }//else

                            @Override
                            public void onError(AccountKitError accountKitError) {
                                Toast.makeText(MainActivity.this, "" + accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        }
    }

    private void showErrorActivity(AccountKitError error) {
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}