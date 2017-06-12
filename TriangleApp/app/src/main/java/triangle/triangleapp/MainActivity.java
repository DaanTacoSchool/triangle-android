package triangle.triangleapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
  private String TAG = "MainActivity";
  private Context myContext;





  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initMain();

  }

  private void initMain(){

      Button startCamera;
      startCamera = (Button) findViewById(R.id.button_switchCamera);
      startCamera.setOnClickListener(startCameraClick);
      myContext = this;

    }
  View.OnClickListener startCameraClick = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Intent i = new Intent(myContext,CameraActivity.class);
      startActivity(i);

    }
  };

}
