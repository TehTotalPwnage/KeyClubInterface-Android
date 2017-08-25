package io.tehtotalpwnage.keyclubinterface;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;
import static io.tehtotalpwnage.keyclubinterface.authenticator.Authenticator.ARG_SERVER_ADDRESS;

public class BarcodeScanActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final int REQUEST_CAMERA = 0;
    private final String TAG = BarcodeScanActivity.class.getSimpleName();

    private int meetingId;

    private BarcodeTrackerFactory mFactory;
    private CameraSource mCameraSource;
    private GraphicOverlay mGraphicOverlay;
    private CameraSourcePreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.overlay);

        StringRequest request = new StringRequest(Request.Method.GET, getIntent().getStringExtra(ARG_SERVER_ADDRESS) + "/meetings", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                List<String> meetings = new ArrayList<>();
                String meetings[][] = null;
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        meetings = new String[array.length()][2];
                        meetings[i][0] = object.getString("id");
                        meetings[i][1] = object.getString("date_time");
//                        meetings.add(object.getString("date_time"));
                    }
                } catch (Exception e) {
                    BarcodeScanActivity.super.finish();
                }
                Spinner spinner = new Spinner(BarcodeScanActivity.this);
                spinner.setOnItemSelectedListener(BarcodeScanActivity.this);
                MeetingAdapter adapter = new MeetingAdapter(BarcodeScanActivity.this, meetings);
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(BarcodeScanActivity.this, R.layout.meeting_spinner, R.id.textView3, meetings);
                spinner.setAdapter(adapter);
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScanActivity.this);
                builder.setTitle("Select Meeting")
                        .setView(spinner)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (ContextCompat.checkSelfPermission(BarcodeScanActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(BarcodeScanActivity.this, Manifest.permission.CAMERA)) {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BarcodeScanActivity.this);
                                        alertDialog.setTitle("Requesting Camera Permission");
                                        alertDialog.setMessage("The Camera permission is required for this app to be able to scan bar codes.");
                                        alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                ActivityCompat.requestPermissions(BarcodeScanActivity.this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA);
                                            }
                                        });
                                    } else {
                                        ActivityCompat.requestPermissions(BarcodeScanActivity.this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA);
                                    }
                                }  else {
                                    getCamera();
                                }
                            }
                        }).setCancelable(false)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScanActivity.this);
                builder.setTitle("Error on Loading Meetings")
                        .setMessage("Response Code: ")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                BarcodeScanActivity.this.finish();
                            }
                        }).show();
            }
        });
        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Meeting Data?")
                .setMessage("This action will send " + ((TextView) findViewById(R.id.textView2)).getText() +
                        " members to the server for meeting.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        StringRequest request = new StringRequest(Request.Method.POST, getIntent().getStringExtra(ARG_SERVER_ADDRESS) + "/api/meetings/" + meetingId,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d(TAG, "Received response from server: " + response);
                                        Toast.makeText(BarcodeScanActivity.this, "Meeting information sent successfully!", LENGTH_LONG).show();
                                        BarcodeScanActivity.super.onBackPressed();
                                        dialog.dismiss();
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(BarcodeScanActivity.this, "Error on sending data to server...", LENGTH_LONG).show();
                                        dialog.dismiss();
                                    }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> map = new HashMap<>();
                                map.put("Accept", "application/json");
                                map.put("Authorization", "Bearer " + getIntent().getStringExtra(AccountManager.KEY_AUTHTOKEN));
                                return map;
                            }

                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> map = new HashMap<>();
                                map.put("_method", "PUT");
                                for (String id : mFactory.getList()) {
                                    map.put("members[]", id);
                                }
                                return map;
                            }
                        };
                        VolleySingleton.getInstance(BarcodeScanActivity.this).addToRequestQueue(request);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BarcodeScanActivity.super.onBackPressed();
                        dialog.dismiss();
                    }
                }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCamera();
                } else {
                    finish();
                }
                return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mPreview.start(mCameraSource, mGraphicOverlay);
        } catch (IOException e) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseCamera();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Log.d(TAG, "Setting meeting ID to " + ((String[]) parent.getItemAtPosition(pos))[0]);
        meetingId = Integer.parseInt(((String[]) parent.getItemAtPosition(pos))[0]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void getCamera() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "There is no camera available on this device.", LENGTH_LONG).show();
            finish();
        } else {
            try {
                BarcodeDetector detector = new BarcodeDetector.Builder(this).build();
                BarcodeTrackerFactory factory = new BarcodeTrackerFactory(this, mGraphicOverlay);
                mFactory = factory;
                detector.setProcessor(new MultiProcessor.Builder<>(factory).build());

                if (!detector.isOperational()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("Missing Dependencies")
                            .setMessage("The Google Play Mobile Vision APIs are missing.")
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).show();
                    return;
                }

                mCameraSource = new CameraSource.Builder(this, detector)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedFps(15.0f)
                        .setAutoFocusEnabled(true)
                        .build();

                mPreview.start(mCameraSource, mGraphicOverlay);



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseCamera() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }
}
