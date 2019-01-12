package asliborneo.router;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.felipecsl.gifimageview.library.GifImageView;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class SplashScreen extends AppCompatActivity {

    private GifImageView gifImageView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

     gifImageView = (GifImageView) findViewById(R.id.gifImageView);
     progressBar = (ProgressBar)findViewById(R.id.progressBar);
     progressBar.setVisibility(progressBar.INVISIBLE);

     try {
         InputStream inputStream = getAssets().open("loading.gif");
         byte[] bytes = IOUtils.toByteArray(inputStream);
         gifImageView.setBytes(bytes);
         gifImageView.startAnimation();
     }
     catch (IOException ex)
     {

     }
     new android.os.Handler().postDelayed(new Runnable() {
         @Override
         public void run() {
             SplashScreen.this.startActivity(new Intent(SplashScreen.this,MainActivity.class));
             SplashScreen.this.finish();
         }
     }, 4000);




    }
}
