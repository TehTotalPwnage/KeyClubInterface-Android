package io.tehtotalpwnage.keyclubinterface.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tehtotalpwnage on 9/3/17.
 */

public class PassportRequest extends JsonObjectRequest {
    private String authToken;

    public PassportRequest(int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener,
                           String authToken)  {
        super(method, url, null, listener, errorListener);
        this.authToken = authToken;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Authorization", "Bearer " + authToken);
        return headers;
    }
}
