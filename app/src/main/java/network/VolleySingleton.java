package network;

import android.content.Context;

import com.android.volley.RequestQueue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


/**
 * Created by Carl on 11/15/2018.
 */

public class VolleySingleton {
    public final String base_url = "https://druglane.herokuapp.com/";
     public final String client_path =  "https://druglane.herokuapp.com/client/";
     public final String message_path = "https://druglane.herokuapp.com/message/";
    private static VolleySingleton myInstance;

    public RequestQueue rq;

    public static Context cxt;

    private VolleySingleton(Context c){
        cxt = c;
        rq = getRequestQueue();
    }

    //get instance
    public static synchronized VolleySingleton getInstance(Context c){
        if(myInstance == null){
            myInstance = new VolleySingleton(c);
        }
        return myInstance;
    }

    public RequestQueue getRequestQueue(){
        if(rq == null){
            rq = Volley.newRequestQueue(cxt.getApplicationContext());
        }

        return  rq;
    }

    public <T> void addToRequestQueue(Request<T> r){
        getRequestQueue().add(r);
    }

    public JSONObject postData(String url, JSONObject params){
        final JSONObject[] res = new JSONObject[1];
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, base_url+url, params, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        res[0] = response;
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                }){

            /** Passing some request headers* */
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap();
                headers.put("Content-Type", "application/json");
                headers.put("Type", "inspection_app");
                headers.put("Userid", "17");
                return headers;
            }
        };

        getRequestQueue().add(jsonObjectRequest);
        return res[0];
    }

    public  JSONObject getData(String url){
        final JSONObject[] res = new JSONObject[1];
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, base_url+url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        res[0] = response;
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                }){

            /** Passing some request headers* */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return super.getHeaders();
            }
        };

        getRequestQueue().add(jsonObjectRequest);
        return res[0];
    }

    public JSONObject login(String url, JSONObject params){
        final JSONObject[] res = new JSONObject[1];
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, base_url+url, params, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        res[0] = response;
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                }){

            /** Passing some request headers* */
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                HashMap<String,String> headers = new HashMap();
                headers.put("Content-Type", "application/json");
                headers.put("Type", "inspection_app");
                return headers;
            }
        };

        getRequestQueue().add(jsonObjectRequest);
        return res[0];
    }

    public Map<String, String> getHeaders(String user_id){


        HashMap<String,String> headers = new HashMap();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Type", "inspection_app");
        headers.put("Userid", user_id);
        headers.put("Token", "none");
        return headers;
    }
}
