package be.vdab.thegoldenhour;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by vdabcursist on 11/10/2017.
 */

public class AppController extends Application {
    private static final String TAG = "Appcontroller";
    private static AppController instance;
    public static synchronized AppController getInstance() {
        return instance;
    }


    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public RequestQueue getRequestQueue() {
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue (Request<T> request, String tag) {
        request.setTag(TextUtils.isEmpty(tag)?TAG:tag);
        getRequestQueue().add(request);
    }

    public <T> void addToRequestQueue (Request<T> request) {
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public void CancelPendingRequests (Object tag){
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }



}
