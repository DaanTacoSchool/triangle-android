package triangle.triangleapp.presentation.impl;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import triangle.triangleapp.R;
import triangle.triangleapp.presentation.stream.impl.StreamActivity;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        requestPermissions();

        Button btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasRequiredPermissions()) {
                    Intent cameraIntent = new Intent(MainActivity.this, StreamActivity.class);
                    startActivity(cameraIntent);
                } else {
                    requestPermissions();
                }
            }
        });


    }

    /**
     * Check if app has all the required permissions
     */
    @AfterPermissionGranted(PERMISSION_REQUEST_CODE)
    private void requestPermissions() {
        String[] perms = {Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        if (!EasyPermissions.hasPermissions(this, perms)) {

            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_required), PERMISSION_REQUEST_CODE, perms);
        }
    }

    /**
     * Requests the required permissions for this app
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // This will display a dialog directing them to enable the permission in app settings.
        new AppSettingsDialog.Builder(this)
                .setNegativeButton(getString(R.string.close_app), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .build()
                .show();
    }

    /**
     * Checks if this application has all the required permissions
     *
     * @return true if we have the required permissions else false.
     */
    private boolean hasRequiredPermissions() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA);
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasReadStoragePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int hasMicrophonePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);

        return PackageManager.PERMISSION_GRANTED == hasCameraPermission && PackageManager.PERMISSION_GRANTED == hasWriteStoragePermission && PackageManager.PERMISSION_GRANTED == hasReadStoragePermission && PackageManager.PERMISSION_GRANTED == hasMicrophonePermission;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
