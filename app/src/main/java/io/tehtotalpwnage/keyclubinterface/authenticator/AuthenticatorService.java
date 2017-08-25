package io.tehtotalpwnage.keyclubinterface.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
    //public AuthenticatorService() {
    //}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        Authenticator authenticator = new Authenticator(this);
        return authenticator.getIBinder();
    }
}