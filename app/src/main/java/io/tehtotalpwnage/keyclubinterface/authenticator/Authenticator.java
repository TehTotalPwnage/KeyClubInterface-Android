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

package io.tehtotalpwnage.keyclubinterface.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.tehtotalpwnage.keyclubinterface.LoginActivity;
import io.tehtotalpwnage.keyclubinterface.VolleySingleton;

import static android.accounts.AccountManager.KEY_AUTHTOKEN;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

public class Authenticator extends AbstractAccountAuthenticator {
    private String TAG = Authenticator.class.getSimpleName();

    private final Context mContext;

    public static final String ARG_SERVER_ADDRESS = "SERVER_ADDRESS";

    Authenticator(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {
        Log.d(TAG, "addAccount called");

        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(LoginActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, final Account account, String authTokenType, Bundle options) {
        Log.d(TAG, "getAuthToken was called...");
        final AccountManager am = AccountManager.get(mContext);
        String authToken = am.peekAuthToken(account, authTokenType);
        if(TextUtils.isEmpty(authToken)) {
            Log.d(TAG, "authToken was empty...");
            if (authTokenType.equals(KeyClubAccount.TOKENTYPE_ACCESS)) {
                Log.d(TAG, "Attempting to retrieve refresh token...");
                // Since the authToken requested was an access token, we're gonna try to get a refresh token now.
                Bundle bundle = getAuthToken(response, account, KeyClubAccount.TOKENTYPE_REFRESH, options);
                Log.d(TAG, "Refresh token retrieved. Checking for access token...");
                if (!TextUtils.isEmpty(am.peekAuthToken(account, KeyClubAccount.TOKENTYPE_ACCESS))) {
                    Log.d(TAG, "Access token found. Setting access token...");
                    authToken = am.peekAuthToken(account, KeyClubAccount.TOKENTYPE_ACCESS);
                } else {
                    Log.d(TAG, "Access token still not found... Attempting to renew access token...");
                    final String refreshToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    final String sAddress = mContext.getSharedPreferences("Prefs", 0).getString("server", null);
                    RequestFuture<String> future = RequestFuture.newFuture();
                    StringRequest req = new StringRequest(Request.Method.POST, sAddress + "/api/oauth/grant/refresh", future, future) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("token", refreshToken);
                            return params;
                        }
                    };
                    VolleySingleton.getInstance(mContext).addToRequestQueue(req);
                    try {
                        // If the future returns a string, use the new tokens in the JSON.
                        String requestResponse = future.get();
                        Log.d(TAG, "Access token retrieved from server. Attempting to save...");
                        JSONObject object = new JSONObject(requestResponse);
                        am.setAuthToken(account, KeyClubAccount.TOKENTYPE_ACCESS, object.getString("access_token"));
                        am.setAuthToken(account, KeyClubAccount.TOKENTYPE_REFRESH, object.getString("refresh_token"));
                        am.invalidateAuthToken(KeyClubAccount.ACCOUNT_TYPE, refreshToken);
                        authToken = object.getString("access_token");
                    } catch (ExecutionException e) {
                        Log.d(TAG, "Caught ExecutionException. Checking type...");
                        if (((VolleyError) e.getCause()).networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            Log.d(TAG, "Access token renewal failed. Refresh token invalidated...");
                            // Refresh token is also having issues, so invalidate the old refresh token.
                            am.invalidateAuthToken(KeyClubAccount.ACCOUNT_TYPE, refreshToken);
                            Bundle passwordBundle = getAuthToken(response, account, KeyClubAccount.TOKENTYPE_REFRESH, options);
                            if (passwordBundle.getString(KEY_AUTHTOKEN, null) != null) {
                                // Refresh token was found.
                                String token = am.peekAuthToken(account, KeyClubAccount.TOKENTYPE_ACCESS);
                                if (!TextUtils.isEmpty(token)) {
                                    authToken = token;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "General Exception caught...");
                    }
                }
            } else {
                final String password = am.getPassword(account);
                if (password != null) {
                    SharedPreferences settings  = mContext.getSharedPreferences("Prefs", 0);
                    final String sAddress = settings.getString("server", null);
                    RequestFuture<String> future = RequestFuture.newFuture();
                    StringRequest req = new StringRequest(Request.Method.POST, sAddress + "/api/oauth/grant/password", future, future) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("email", account.name);
                            params.put("password", password);

                            return params;
                        }
                    };
                    VolleySingleton.getInstance(mContext).addToRequestQueue(req);
                    try {
                        String requestResponse = future.get();
                        JSONObject object = new JSONObject(requestResponse);
                        am.setAuthToken(account, KeyClubAccount.TOKENTYPE_REFRESH, object.getString("refresh_token"));
                        am.setAuthToken(account, KeyClubAccount.TOKENTYPE_ACCESS, object.getString("access_token"));
                        authToken = object.getString("refresh_token");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        if(!TextUtils.isEmpty(authToken)) {
            Log.v(TAG, "Token found. Returning token in bundle...");
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        Log.v(TAG, "Unable to generate an authToken. Calling the LoginActivity...");
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(LoginActivity.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(LoginActivity.ARG_AUTH_TYPE, authTokenType);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (KeyClubAccount.TOKENTYPE_REFRESH.equals(authTokenType))
            return KeyClubAccount.TOKENTYPE_REFRESH_LABEL;
        else if (KeyClubAccount.TOKENTYPE_ACCESS.equals(authTokenType))
            return KeyClubAccount.TOKENTYPE_ACCESS_LABEL;
        else
            return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        return null;
    }
}
