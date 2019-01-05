package asliborneo.router;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import asliborneo.router.Model.Rate;
import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class Rate_Driver extends AppCompatActivity {
    Button btn_submit;
    MaterialRatingBar ratingBar;
    MaterialEditText  comment;
    FirebaseDatabase database;
    DatabaseReference rating_detail_ref,Driverinformationref;
    double rating_stars;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_driver);
        database=FirebaseDatabase.getInstance();
        rating_detail_ref=database.getReference(Commons.rateDetail_table);
        Driverinformationref=database.getReference(Commons.Registered_driver);
        btn_submit=(Button) findViewById(R.id.btn_submit);
        comment=(MaterialEditText) findViewById(R.id.comment);
        ratingBar=(MaterialRatingBar) findViewById(R.id.ratingbar);
        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                rating_stars=rating;
            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Submit_Rating_Detail();
            }
        });
    }

    private void Submit_Rating_Detail() {
        final AlertDialog waiting_dialog=new SpotsDialog(Rate_Driver.this);
        waiting_dialog.show();
        Rate rate=new Rate();
        rate.setRates(String.valueOf(rating_stars));
        rate.setComments(comment.getText().toString());
        rating_detail_ref.child(Commons.driverId).push().setValue(rate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                rating_detail_ref.child(Commons.driverId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        double average_stars=0.0;
                        int count=0;
                        for(DataSnapshot postsnapshot:dataSnapshot.getChildren()){
                            Rate rate=postsnapshot.getValue(Rate.class);
                            average_stars+=Double.parseDouble(rate.getRates());
                            count++;
                        }
                        double final_average_stars=average_stars/count;
                        DecimalFormat df=new DecimalFormat("#.#");
                        String value_update=df.format(final_average_stars);
                        Map<String,Object> driver_rating_update=new HashMap<>();
                        driver_rating_update.put("rates",value_update);
                        Driverinformationref.child(Commons.driverId).updateChildren(driver_rating_update).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    waiting_dialog.dismiss();
                                    finish();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waiting_dialog.dismiss();
                                Toast.makeText(Rate_Driver.this,e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waiting_dialog.dismiss();
                Toast.makeText(Rate_Driver.this,e.getMessage(),Toast.LENGTH_LONG).show();

            }
        });
    }
}