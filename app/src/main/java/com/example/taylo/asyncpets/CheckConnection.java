package com.example.taylo.asyncpets;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by taylo on 3/16/2017.
 */

public class CheckConnection {

    private Context contextPassed;

    public CheckConnection(Context context){
        this.contextPassed =context;
    }

    public boolean checkNetworkConnection(){
        ConnectivityManager myManager = (ConnectivityManager)contextPassed.getSystemService(contextPassed.CONNECTIVITY_SERVICE);
        NetworkInfo myInfo = myManager.getActiveNetworkInfo();

        if(myInfo==null){
            return false;
        }

        return ( myInfo.getState()== NetworkInfo.State.CONNECTED);
    }

    public boolean isWifiReachable() {
        ConnectivityManager mManager = (ConnectivityManager) contextPassed.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo current = mManager.getActiveNetworkInfo();
        if (current == null) {
            return false;
        }
        return (current.getType() == ConnectivityManager.TYPE_WIFI);
    }
}
