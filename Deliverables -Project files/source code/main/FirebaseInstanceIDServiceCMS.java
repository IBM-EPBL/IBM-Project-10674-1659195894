package bigbrother.child_monitoring_system;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Sofanit on 3/22/2017.
 */

public class FirebaseInstanceIDServiceCMS extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        //super.onTokenRefresh();

        String token = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences sharedPreferneces = getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferneces.edit();
        editor.putString(getString(R.string.FCM_TOKEN),token);
        editor.commit();
    }

}