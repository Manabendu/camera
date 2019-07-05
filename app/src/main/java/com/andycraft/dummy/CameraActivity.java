package com.andycraft.dummy;

import android.content.ContentValues;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.andycraft.cameraone.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class CameraActivity extends AppCompatActivity {

    private SurfaceView preview = null;
    private SurfaceHolder previewHolder = null;
    private Camera camera = null;
    private boolean inPreview = false;
    private ImageView takePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        takePicture =  findViewById(R.id.take_picture);

        preview =  findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder
                .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        preview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                takePicture.setEnabled(false);
                camera.autoFocus(myAutoFocusCallback);

            }

        });


        takePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                camera.takePicture(null, null, photoCallback);
                inPreview = false;
            }

        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (camera == null) {
                camera = Camera.open();
                camera.setDisplayOrientation(90);
                Camera.Parameters params = camera.getParameters();
                params.set("rotation", 90);
                camera.setParameters(params);

            }
        } catch (Exception e) {
            finish();
        }
    }


    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }
        if (camera != null)
            camera.release();
        camera = null;
        inPreview = false;

        super.onPause();
    }

    private Camera.Size getBestPreviewSize(int width,
                                           int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return (result);
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (Throwable t) {
                Log.e("TAG",
                        "Exception in setPreviewDisplay()", t);
                Toast.makeText(CameraActivity.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();

            parameters.setPictureFormat(PixelFormat.JPEG);

            camera.setParameters(parameters);
            camera.startPreview();

            inPreview = true;
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

    Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            Uri uriTarget = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new ContentValues());
//            SyncStateContract.Constants.currImageURI = uriTarget;

            OutputStream imageFileOS;

            try {

                imageFileOS = getContentResolver().openOutputStream(uriTarget);
                imageFileOS.write(data);
                imageFileOS.flush();
                imageFileOS.close();

                Toast.makeText(CameraActivity.this, "Image saved: " + uriTarget.toString(), Toast.LENGTH_LONG).show();

            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            }
            setResult(RESULT_OK);
            finish();

        }
    };
    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
            takePicture.setEnabled(true);
        }
    };

}

