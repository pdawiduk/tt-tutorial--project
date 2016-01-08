package com.example.dawiduk.podejscie2.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by dawiduk on 8-1-16.
 */


    public class SunshineAuthenticatorService extends Service {

        private SunshineAuthenticator mAuthenticator;

        @Override
        public void onCreate() {

            mAuthenticator = new SunshineAuthenticator(this);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return mAuthenticator.getIBinder();
        }
}
