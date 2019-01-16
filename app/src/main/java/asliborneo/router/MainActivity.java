package asliborneo.router;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;
import io.paperdb.Paper;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.content.pm.PackageManager.GET_SIGNATURES;


public class MainActivity extends AppCompatActivity {
    FButton btnSignIn,btnContinue;
    private static final int REQUEST_CODE = 11;

    FirebaseAuth auth;
    FirebaseDatabase db;
    private static final String TAG = "MainActivity";


    DatabaseReference Rider;
    MaterialEditText email,password,name,phone;
    RelativeLayout rootlayout;
    static String extractNumer;

    TextView txt_forgot_password, txtRegister, txtSignin;

    TextView txtUrl;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/antaro.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_main);
        auth=FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();
        Rider=db.getReference(Common.Registered_Riders);
        asliborneo.router.Utils.GifImageView gifImageView = (asliborneo.router.Utils.GifImageView) findViewById(R.id.GifImageView);
        gifImageView.setGifImageResource(R.drawable.logo);


        printKeyHash();
        Common.currentUser= new asliborneo.router.Model.Rider();
        txtUrl=(TextView) findViewById(R.id.txtUrl);
        txtUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_forgot_password_dialog();
            }
        });

        btnContinue=(FButton) findViewById(R.id.btnContinue);
        rootlayout=(RelativeLayout) findViewById(R.id.rootlayout);
        Paper.init(this);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneLogin();
            }
        });
//        btnSignIn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                show_login_dialog();
//            }
//        });
//        String username=Paper.book().read(Common.user_field);
//        String password=Paper.book().read(Common.password_field);
//        if(username!=null&&password!=null){
//            if(!TextUtils.isEmpty(username)&&!TextUtils.isEmpty(password)){
//                auto_login(username,password);
//            }
//        }

        if (AccountKit.getCurrentAccessToken() !=null)
        {
            AccessToken accessToken = AccountKit.getCurrentAccessToken();
            if (accessToken != null)
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        Rider.child(account.getPhoneNumber().toString())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        Common.currentUser = dataSnapshot.getValue(Rider.class);
                                        Intent homeIntent = new Intent(MainActivity.this, MainMenu.class);
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
                });
        }

    }

    private void auto_login(String username, String password) {
        final android.app.AlertDialog waitingdialog=new SpotsDialog(MainActivity.this);
        waitingdialog.show();
        auth.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            FirebaseDatabase.getInstance().getReference(Common.Registered_Riders).child(account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Common.currentUser=dataSnapshot.getValue(asliborneo.router.Model.Rider.class);
                                    waitingdialog.dismiss();
                                    Toast.makeText(MainActivity.this,"Login Sucess",Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(MainActivity.this,MainMenu.class));
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {

                        }
                    });


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waitingdialog.dismiss();
                Toast.makeText(MainActivity.this,"Login failed "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }


    private void show_forgot_password_dialog(){
        AlertDialog.Builder forgot_password_dialog=new AlertDialog.Builder(MainActivity.this);
        forgot_password_dialog .setTitle("Forgot Password");
        forgot_password_dialog .setMessage("Please Enter Your Email");
        LayoutInflater inflater=LayoutInflater.from(MainActivity.this);
        View v=inflater.inflate(R.layout.forgot_password_layout,null);
        forgot_password_dialog.setView(v);
        final MaterialEditText emailtxt=(MaterialEditText) v.findViewById(R.id.emailtxt);
        forgot_password_dialog .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                final android.app.AlertDialog waiting_dialog=new SpotsDialog(MainActivity.this);
                waiting_dialog.show();
                if(!TextUtils.isEmpty(emailtxt.getText().toString())) {
                    auth.sendPasswordResetEmail(emailtxt.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialogInterface.dismiss();
                            waiting_dialog.dismiss();
                            Snackbar.make(rootlayout, "Reset Link is Sent to Your Email", Snackbar.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialogInterface.dismiss();
                            waiting_dialog.dismiss();
                            Snackbar.make(rootlayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }else{
                    waiting_dialog.dismiss();
                    Snackbar.make(rootlayout,"Please Enter Email",Snackbar.LENGTH_LONG).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void show_register_dialog(){
        final AlertDialog.Builder register_dialog=new AlertDialog.Builder(MainActivity.this);
        register_dialog.setTitle("Register");
        register_dialog.setMessage("Use Email to Register");
        final View v=LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_register,null);
        email=(MaterialEditText) v.findViewById(R.id.emailtxt);
        password=(MaterialEditText) v.findViewById(R.id.passwordtxt);
        name=(MaterialEditText) v.findViewById(R.id.nametxt);
//        phone=(MaterialEditText) v.findViewById(R.id.phone);
        register_dialog.setView(v);
        register_dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(email.getText().toString())){
                    Toast.makeText(MainActivity.this,"Please Enter Email",Toast.LENGTH_LONG).show();
                }else if (TextUtils.isEmpty(password.getText().toString())){
                    Toast.makeText(MainActivity.this,"Please Enter Password",Toast.LENGTH_LONG).show();
                }else if (password.getText().toString().length() < 6){
                    Toast.makeText(MainActivity.this,"Password too short",Toast.LENGTH_LONG).show();

                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {

                            if (TextUtils.isEmpty(account.getPhoneNumber().toString())){
                                Toast.makeText(MainActivity.this,"Please Enter Phone",Toast.LENGTH_LONG).show();

                            }
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {

                        }
                    });
                }else if (TextUtils.isEmpty(name.getText().toString())){
                    Toast.makeText(MainActivity.this,"Please Enter Name",Toast.LENGTH_LONG).show();
                }else{
                    auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                final asliborneo.router.Model.Rider user=new asliborneo.router.Model.Rider();

                                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                    @Override
                                    public void onSuccess(final Account account) {

                                        Rider.child(account.getId()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(MainActivity.this,"Registration Sucess",Toast.LENGTH_LONG).show();

                                                phoneLogin();
                                                user.setName(name.getText().toString());

                                                user.setPhone(account.getPhoneNumber().toString());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(AccountKitError accountKitError) {

                                    }
                                });

                            }
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this,"Registration failed "+e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();

        Rider.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        if (dataSnapshot.child(account.getPhoneNumber().toString()).exists())
                        {
                            Toast.makeText(MainActivity.this,"Phone Number Already Registered",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                @Override
                                public void onSuccess(Account account) {
                                    Common.currentUser=dataSnapshot.getValue(asliborneo.router.Model.Rider.class);
                                    Toast.makeText(MainActivity.this,"Successfully Registered",Toast.LENGTH_LONG).show();
                                    finish();
                                }

                                @Override
                                public void onError(AccountKitError accountKitError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (AccountKit.getCurrentAccessToken() != null) {
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    Rider.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Common.currentUser=dataSnapshot.getValue(asliborneo.router.Model.Rider.class);
                                    Intent homeIntent = new Intent(MainActivity.this, MainMenu.class);
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
    }
    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("asliborneo.router",
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


                    Rider.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Common.currentUser=dataSnapshot.getValue(asliborneo.router.Model.Rider.class);
                                    Intent homeIntent = new Intent(MainActivity.this, MainMenu.class);
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


                                Rider.orderByKey().equalTo(userPhone)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.child(userPhone).exists()) {
                                                    asliborneo.router.Model.Rider user = new asliborneo.router.Model.Rider();
                                                    user.setPhone(account.getPhoneNumber().toString());
                                                    user.setName(account.getPhoneNumber().toString());
                                                    user.setAvatarUrl("");
                                                    user.setRates("0.0");
                                                    user.setCarType("NON MEMBERSHIP");

                                                    Rider.child(userPhone)
                                                            .setValue(user)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    Rider.child(userPhone)
                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                    Common.currentUser=dataSnapshot.getValue(asliborneo.router.Model.Rider.class);
                                                                                    Intent homeIntent = new Intent(MainActivity.this, MainMenu.class);
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
                                                    Rider.child(userPhone)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    Common.currentUser=dataSnapshot.getValue(asliborneo.router.Model.Rider.class);
                                                                    Intent homeIntent = new Intent(MainActivity.this, MainMenu.class);
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




}




