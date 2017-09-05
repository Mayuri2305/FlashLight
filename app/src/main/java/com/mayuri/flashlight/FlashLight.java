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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
public class FlashLight extends AppCompatActivity {
    private CameraManager objCameraManager;
    private String mCameraId;
    private ImageView ivOnOFF;
    private MediaPlayer objMediaPlayer;
    private Camera camera;
    private Parameters parameter;
    /**
     * for getting torch mode
     */
    private Boolean isTorchOn;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_light);
        ivOnOFF = (ImageView) findViewById(R.id.ivOnOFF);
        isTorchOn = false;

        /**
         * Check if device contains flashlight
         */
        ifFlashLightIsAvailableOnDevice();


        objCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    && ((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP))) {
                this.camera = Camera.open(0);
                parameter = this.camera.getParameters();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCameraId = objCameraManager.getCameraIdList()[0];
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        ivOnOFF.setOnClickListener(new View.OnClickListener() {
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
                turnOffLight();
                isTorchOn = false;
            } else {
                turnOnLight();
                isTorchOn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
public void getActionsOnFlashLightForOlderVersion(){

    try {
        if(!isTorchOn){
            turnOnTheFlash();
        }else{
            turnOffTheFlash();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    /**
     * Method for turning light ON
     */
    public void turnOnLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                objCameraManager.setTorchMode(mCameraId, true);
                playOnOffSound();
                ivOnOFF.setImageResource(R.drawable.on);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for turning light OFF
     */
    public void turnOffLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                objCameraManager.setTorchMode(mCameraId, false);
                playOnOffSound();
                ivOnOFF.setImageResource(R.drawable.off);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playOnOffSound() {
        objMediaPlayer = MediaPlayer.create(FlashLight.this, R.raw.flash_sound);
        objMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        objMediaPlayer.start();
    }
    private void turnOffTheFlash() {
        parameter.setFlashMode(Parameters.FLASH_MODE_OFF);
        this.camera.setParameters(parameter);
        this.camera.stopPreview();
        isTorchOn = false;
        ivOnOFF.setImageResource(R.drawable.on);
    }

    private void turnOnTheFlash() {
        if(this.camera != null){
            parameter = this.camera.getParameters();
            parameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
            this.camera.setParameters(parameter);
            this.camera.startPreview();
            isTorchOn = true;
            ivOnOFF.setImageResource(R.drawable.off);
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
    @Override
    protected void onStop() {
        super.onStop();
        if (isTorchOn) {
            turnOffLight();
            turnOffTheFlash();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTorchOn) {
            turnOffLight();
            turnOffTheFlash();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTorchOn) {
            turnOnLight();
            turnOffTheFlash();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        getCamera();
    }
}
