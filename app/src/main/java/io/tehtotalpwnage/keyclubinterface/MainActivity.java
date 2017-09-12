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
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import io.tehtotalpwnage.keyclubinterface.authenticator.AccountAdapter;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.KEY_ACCOUNT_TYPE;
import static io.tehtotalpwnage.keyclubinterface.authenticator.Authenticator.ARG_SERVER_ADDRESS;
import static io.tehtotalpwnage.keyclubinterface.authenticator.KeyClubAccount.ACCOUNT_TYPE;
import static io.tehtotalpwnage.keyclubinterface.authenticator.KeyClubAccount.TOKEN_TYPE_ACCESS;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    private final int REQUEST_CAMERA = 0;
    private final int REQUEST_STORAGE = 1;

    private Account mAccount;
    private AccountManager mAccountManager;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkAuthentication();
                } else {
                    finish();
                }
                break;
            default:
                checkPermissions();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccountManager = AccountManager.get(this);

        checkPermissions();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mAccount = (Account) parent.getItemAtPosition(pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void checkAuthentication() {
        final Account availableAccounts[] = mAccountManager.getAccountsByType(ACCOUNT_TYPE);

        if (availableAccounts.length == 0) {
            // Call the addAccount function to load the proper login form for our authenticator.
            mAccountManager.getAuthTokenByFeatures(ACCOUNT_TYPE, TOKEN_TYPE_ACCESS,
                    null, this, null, null, new AccountManagerCallback<Bundle>() {
                        @Override
                        public void run(AccountManagerFuture<Bundle> future) {
                            try {
                                String authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);

                                Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                                intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, null);
        } else if (availableAccounts.length == 1) {
            getAuth(availableAccounts[0]);
        } else {
            Log.d(TAG, "Multiple accounts were found. Prompting user for account selection...");
            Spinner spinner = new Spinner(MainActivity.this);
            spinner.setOnItemSelectedListener(MainActivity.this);
            AccountAdapter adapter = new AccountAdapter(MainActivity.this, availableAccounts);
            spinner.setAdapter(adapter);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select Meeting")
                    .setView(spinner)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getAuth(mAccount);
                        }
                    }).setCancelable(false)
                    .show();
        }
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Requesting Camera Permission");
                alertDialog.setMessage("The Camera permission is required for this app to be able to scan bar codes.");
                alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Requesting External Storage Permission");
                alertDialog.setMessage("The External Storage permission is required for this app to be able to back up member lists.");
                alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
            }
        } else {
            checkAuthentication();
        }

    }

    public void getAuth(final Account account) {
        Log.v(TAG, "Account found. Attempting to call getAuthToken.");
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, TOKEN_TYPE_ACCESS, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Awaiting authentication token on new thread.");
                try {
                    Bundle bnd = future.getResult();

                    final String authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.d(TAG, "Retrieved auth token. Starting intent...");

                    Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                    intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
                    intent.putExtra(KEY_ACCOUNT_NAME, bnd.getString(KEY_ACCOUNT_NAME));
                    intent.putExtra(KEY_ACCOUNT_TYPE, bnd.getString(KEY_ACCOUNT_TYPE));
                    intent.putExtra(ARG_SERVER_ADDRESS, mAccountManager.getUserData(account, ARG_SERVER_ADDRESS));

                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error occurred on retrieving authToken");
                    e.printStackTrace();
                }
            }
        }).start();
    }
}