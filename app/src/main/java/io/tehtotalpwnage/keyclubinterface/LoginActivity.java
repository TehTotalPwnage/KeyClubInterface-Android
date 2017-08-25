/*
 * Copyright 2017 Michael Nguyen
 *
 * This file is part of Music.php-Android.
 *
 * Music.php-Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Music.php-Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Music.php-Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.tehtotalpwnage.keyclubinterface;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.tehtotalpwnage.keyclubinterface.authenticator.Authenticator;

import static io.tehtotalpwnage.keyclubinterface.authenticator.KeyClubAccount.TOKENTYPE_ACCESS;
import static io.tehtotalpwnage.keyclubinterface.authenticator.KeyClubAccount.TOKENTYPE_REFRESH;

public class LoginActivity extends AccountAuthenticatorActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private AccountManager mAccountManager;
    private String mAuthTokenType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAccountManager = AccountManager.get(getBaseContext());

        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null) {
            mAuthTokenType = TOKENTYPE_REFRESH;
        }
    }

    /**
     * Attempts to log in using the provided credentials.
     * @param view - The view that called the login function.
     */
    public void login(View view) {
        // Retrieve the credentials provided by the form.
        final String email = ((EditText) findViewById(R.id.editText2)).getText().toString();
        final String password = ((EditText) findViewById(R.id.editText3)).getText().toString();
        final String sAddress = ((EditText) findViewById(R.id.editText1)).getText().toString();

        // From the intent that called this activity, get the account type to be created.
        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        // Create the string request that will be used to attempt to log in.
        StringRequest req = new StringRequest(Request.Method.POST, sAddress + "/api/oauth/grant/password", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // Parse the JSON data retrieved from the server.
                    Log.v(TAG, "Attempting to parse login data...");
                    JSONObject json = new JSONObject(response);
                    String aToken = json.getString("access_token");
                    String rToken = json.getString("refresh_token");

                    // Create the intent containing all of the relevant information to return.
                    final Intent res = new Intent();
                    res.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
                    res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                    res.putExtra(AccountManager.KEY_AUTHTOKEN, aToken);
                    res.putExtra(PARAM_USER_PASS, password);

                    String accountName = email;
                    String accountPassword = res.getStringExtra(PARAM_USER_PASS);
                    final Account account = new Account(accountName, res.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

                    // Check to see if the authenticator was called with the intent of adding a new account.
                    if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
                        // Creating the account on the device and setting the auth token we got.
                        mAccountManager.addAccountExplicitly(account, accountPassword, null);
                        mAccountManager.setAuthToken(account, TOKENTYPE_REFRESH, rToken);
                        mAccountManager.setAuthToken(account, TOKENTYPE_ACCESS, aToken);
                        mAccountManager.setUserData(account, Authenticator.ARG_SERVER_ADDRESS, sAddress);
                    } else {
                        mAccountManager.setPassword(account, accountPassword);
                        mAccountManager.setAuthToken(account, TOKENTYPE_REFRESH, rToken);
                        mAccountManager.setAuthToken(account, TOKENTYPE_ACCESS, aToken);
                    }
                    // Finish up storage.
                    setAccountAuthenticatorResult(res.getExtras());
                    SharedPreferences settings = getSharedPreferences("Prefs", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("server", sAddress);
                    editor.apply();
                    setResult(RESULT_OK, res);
                    finish();
                } catch (JSONException e) {
                    Log.v(TAG, "Error on parse...");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "Failure");
                AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                alert.setTitle("Login Error");
                if (error.networkResponse != null) {
                    alert.setMessage("There was an error logging into the server... (" + error.networkResponse.statusCode + ")");
                } else {
                    alert.setMessage("There was an error logging into the server... (Unknown)");
                }
                alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}