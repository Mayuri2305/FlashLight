package com.mayuri.flashlight;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.hardware.Camera.Parameters;

public class FlashLight extends AppCompatActivity {
    private CameraManager cameraManager;
    private String mCameraId;
    private ImageView torchOnOrOff;
    private MediaPlayer mediaPlayer;
    static Camera camera = null;
    private Parameters parameter;
    private Boolean isTorchOn;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_light);
        getSupportActionBar().hide();
        torchOnOrOff = (ImageView) findViewById(R.id.flash_light_on_off_imageview);
        isTorchOn = false;

        /**
         * Check if device contains flashlight
         */
        ifFlashLightIsAvailableOnDevice();

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    && ((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP))) {
                try {
                    releaseCamera();
                    if (mCameraId == "0") {
                        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    } else {
                        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    }
                } catch (Exception e) {
                    Log.e(getString(R.string.app_name), "failed to open Camera");
                    e.printStackTrace();
                }
                parameter = this.camera.getParameters();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCameraId = cameraManager.getCameraIdList()[0];
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        torchOnOrOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                        && ((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP))) {
                    getActionsOnFlashLightForOlderVersion();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActionsOnFlashLightForNewVersion();
                }
            }
        });
    }

    public void ifFlashLightIsAvailableOnDevice() {
        Boolean isFlashAvailable = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {
            AlertDialog alert = new AlertDialog.Builder(FlashLight.this).create();
            alert.setTitle(getString(R.string.app_name));
            alert.setMessage(getString(R.string.msg_error));
            alert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.lbl_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert.show();
            return;
        }
    }

    public void getActionsOnFlashLightForNewVersion() {
        try {
            if (isTorchOn) {
                turnOffTheFlashLightForNewVersion();
                isTorchOn = false;
            } else {
                turnOnTheFlashLightForNewVersion();
                isTorchOn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getActionsOnFlashLightForOlderVersion() {

        try {
            if (isTorchOn) {
                turnOffTheFlashLightForOldVersion();
                isTorchOn = false;
            } else {
                turnOnTheFlashLightForOldVersion();
                isTorchOn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void turnOnTheFlashLightForNewVersion() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(mCameraId, true);
                playOnOffSound();
                torchOnOrOff.setImageResource(R.drawable.flash_light_on);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void turnOffTheFlashLightForNewVersion() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(mCameraId, false);
                playOnOffSound();
                torchOnOrOff.setImageResource(R.drawable.flash_light_off);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void turnOffTheFlashLightForOldVersion() {
        parameter.setFlashMode(Parameters.FLASH_MODE_OFF);
        this.camera.setParameters(parameter);
        this.camera.stopPreview();
        playOnOffSound();
        torchOnOrOff.setImageResource(R.drawable.flash_light_off);
    }

    private void turnOnTheFlashLightForOldVersion() {
        if (this.camera != null) {
            parameter = this.camera.getParameters();
            parameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
            this.camera.setParameters(parameter);
            this.camera.startPreview();
            playOnOffSound();
            torchOnOrOff.setImageResource(R.drawable.flash_light_on);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (isTorchOn) {
            turnOffTheFlashLightForNewVersion();
            turnOffTheFlashLightForOldVersion();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTorchOn) {
            turnOffTheFlashLightForNewVersion();
            turnOffTheFlashLightForOldVersion();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTorchOn) {
            turnOnTheFlashLightForNewVersion();
            turnOffTheFlashLightForOldVersion();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getCamera();
    }

    private void releaseCamera() {

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                parameter = camera.getParameters();
            } catch (RuntimeException e) {
                System.out.println("Error: Failed to Open: " + e.getMessage());
            }
        }
    }

    private void playOnOffSound() {
        mediaPlayer = MediaPlayer.create(FlashLight.this, R.raw.flash_sound);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.start();
    }
}
