/*
 * Copyright (c) 2017 Michael Nguyen
 *
 * This file is part of KeyClubInterface.
 *
 * KeyClubInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeyClubInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KeyClubInterface.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.tehtotalpwnage.keyclubinterface;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.tehtotalpwnage.keyclubinterface.barcode.BarcodeTrackerFactory;
import io.tehtotalpwnage.keyclubinterface.camera.CameraSourcePreview;
import io.tehtotalpwnage.keyclubinterface.camera.GraphicOverlay;
import io.tehtotalpwnage.keyclubinterface.volley.PassportErrorListener;
import io.tehtotalpwnage.keyclubinterface.volley.PassportRequest;
import io.tehtotalpwnage.keyclubinterface.volley.VolleySingleton;

import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static android.accounts.AccountManager.KEY_AUTHTOKEN;
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

        JsonObjectRequest request = new PassportRequest(Request.Method.GET, getIntent().getStringExtra(ARG_SERVER_ADDRESS) + "/api/meetings", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Response retrieved. Parsing data... ");
                String meetings[][];
                try {
                    JSONArray array = response.getJSONArray("meetings");
                    meetings = new String[array.length()][2];
                    if (array.length() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScanActivity.this);
                        builder.setTitle("No Meetings Found")
                                .setMessage("Please add a meeting on the website for tracking.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        BarcodeScanActivity.this.finish();
                                    }
                                }).show();
                    } else {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            meetings[i][0] = object.getString("id");
                            meetings[i][1] = object.getString("date_time");
                        }
                        displayMeetings(meetings);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "Error on parsing JSON...");
                    AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScanActivity.this);
                    builder.setTitle("Error on Parsing JSON")
                            .setMessage("The app received data but could not understand it.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    BarcodeScanActivity.this.finish();
                                }
                            }).show();
                    BarcodeScanActivity.this.finish();
                }

            }
        }, new PassportErrorListener(this, getIntent().getStringExtra(KEY_ACCOUNT_TYPE), getIntent().getStringExtra(KEY_AUTHTOKEN)) {
            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeScanActivity.this);
                builder.setTitle("Error on Loading Meetings")
                        .setMessage("Response Code: " + (error.networkResponse != null ? error.networkResponse.statusCode : "No Response"))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                BarcodeScanActivity.this.finish();
                            }
                        }).show();
            }
        }, getIntent().getStringExtra(KEY_AUTHTOKEN));
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
                                map.put("Authorization", "Bearer " + getIntent().getStringExtra(KEY_AUTHTOKEN));
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
        Log.d(TAG, "Process is pausing. Stopping camera.");
        stopCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCamera();
                } else {
                    finish();
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Process resumed. Attempting to restart camera.");
        try {
            mPreview.start(mCameraSource, mGraphicOverlay);
            Log.d(TAG, "Camera restarted successfully.");
        } catch (IOException e) {
            Log.d(TAG, "Unable to start camera. Printing stack trace.");
            e.printStackTrace();
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Log.d(TAG, "Setting meeting ID to " + ((String[]) parent.getItemAtPosition(pos))[0]);
        meetingId = Integer.parseInt(((String[]) parent.getItemAtPosition(pos))[0]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void displayMeetings(String[][] meetings) {
        Spinner spinner = new Spinner(BarcodeScanActivity.this);
        spinner.setOnItemSelectedListener(BarcodeScanActivity.this);
        MeetingAdapter adapter = new MeetingAdapter(BarcodeScanActivity.this, meetings);
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
                                        ActivityCompat.requestPermissions(BarcodeScanActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                                    }
                                });
                            } else {
                                ActivityCompat.requestPermissions(BarcodeScanActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                            }
                        } else {
                            getCamera();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BarcodeScanActivity.this.finish();
                        dialog.dismiss();
                    }
                }).setCancelable(false)
                .show();
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
        if (mPreview != null) {
            mPreview.release();
        }
    }

    private void stopCamera() {
        if (mPreview != null) {
            mPreview.stop();
        }
    }
}
