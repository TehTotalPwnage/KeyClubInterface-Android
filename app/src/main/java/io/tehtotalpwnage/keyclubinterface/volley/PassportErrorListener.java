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

package io.tehtotalpwnage.keyclubinterface.volley;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.net.HttpURLConnection;

import io.tehtotalpwnage.keyclubinterface.MainActivity;

public class PassportErrorListener implements Response.ErrorListener {
    private Context context;

    private String accountType;
    private String authToken;

    protected PassportErrorListener(Context context, String accountType, String authToken) {
        this.context = context;
        this.accountType = accountType;
        this.authToken = authToken;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AccountManager manager = AccountManager.get(context);
            manager.invalidateAuthToken(accountType, authToken);
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        }
    }
}
