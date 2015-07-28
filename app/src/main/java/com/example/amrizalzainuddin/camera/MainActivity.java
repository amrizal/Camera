package com.example.amrizalzainuddin.camera;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    private static final String TAG = "CameraActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SurfaceView surface = (SurfaceView)findViewById(R.id.surfaceView);
        SurfaceHolder holder = surface.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.setFixedSize(400, 300);

        Button snap = (Button)findViewById(R.id.buttonTakePicture);
        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    private Camera camera = null;

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.release();
        camera = null;
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            int n = 1;
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            int n =1;
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Save the image JPEG data to the SD card
            FileOutputStream outStream = null;
            try {

                String path = Environment.getExternalStorageDirectory() + "/test.jpg";

                outStream = new FileOutputStream(path);
                outStream.write(data);
                outStream.close();

                //insert to media store
                mediaScan(path);

            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found", e);
            } catch (IOException e) {
                Log.e(TAG, "IO Exception", e);
            }
        }
    };

    private void mediaScan(final String filepath){
        MediaScannerConnection.MediaScannerConnectionClient mediaScannerConnectionClient = new MediaScannerConnection.MediaScannerConnectionClient() {

            private  MediaScannerConnection msc = null;
            {
                msc = new MediaScannerConnection(MainActivity.this, this);
                msc.connect();
            }
            @Override
            public void onMediaScannerConnected() {
                String mimeType = null;
                msc.scanFile(filepath, mimeType);
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                msc.disconnect();
                Log.d(TAG, "File Added at: " + uri.toString());
            }
        };
    }

    private void takePicture() {
        int faceDetectable = camera.getParameters().getMaxNumDetectedFaces();
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(camera == null)
            return;

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            // TODO Draw over the preview if required.
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(camera == null)
            return;

        camera.stopPreview();
    }
}
