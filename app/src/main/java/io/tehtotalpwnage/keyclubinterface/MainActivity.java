package io.tehtotalpwnage.keyclubinterface;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import io.tehtotalpwnage.keyclubinterface.authenticator.Authenticator;

import static io.tehtotalpwnage.keyclubinterface.authenticator.Authenticator.ARG_SERVER_ADDRESS;
import static io.tehtotalpwnage.keyclubinterface.authenticator.KeyClubAccount.ACCOUNT_TYPE;
import static io.tehtotalpwnage.keyclubinterface.authenticator.KeyClubAccount.TOKENTYPE_ACCESS;
import static io.tehtotalpwnage.keyclubinterface.authenticator.KeyClubAccount.TOKENTYPE_REFRESH;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_MESSAGE = "io.tehtotalpwnage.keyclubinterface.MESSAGE";

    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAccountManager = AccountManager.get(this);
        VolleySingleton.getInstance(this);
        final Account availableAccounts[] = mAccountManager.getAccountsByType(ACCOUNT_TYPE);

        if (availableAccounts.length == 0) {
            // Call the addAccount function to load the proper login form for our authenticator.
            final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(ACCOUNT_TYPE, TOKENTYPE_ACCESS,
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
        } else {
            Log.v(TAG, "Account found. Attempting to call getAuthToken...");
            final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(availableAccounts[0], TOKENTYPE_ACCESS, null, this, null, null);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Now running");
                    try {
                        Bundle bnd = future.getResult();

                        final String authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                        Log.d(TAG, "Retrieved auth token. Starting intent...");

                        Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
                        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, bnd.getString(AccountManager.KEY_ACCOUNT_NAME));
                        intent.putExtra(ARG_SERVER_ADDRESS, mAccountManager.getUserData(availableAccounts[0], ARG_SERVER_ADDRESS));

                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error occurred on retrieving authToken");
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void loadLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(ACCOUNT_TYPE, TOKENTYPE_REFRESH, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                Log.d("udinic", "action performed");
                try {
                    Bundle bnd = future.getResult();
                    Log.d("udinic", "AddNewAccount Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
    }
}