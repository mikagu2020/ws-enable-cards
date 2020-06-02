package com.example.infocard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView tvInfocards;
    Button btnActEu, btnActInt, btnInfoCards;
    int statusCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getControlViews();

        setEventListeners();

    }

    private void getControlViews() {
        tvInfocards = findViewById(R.id.respose_text_field);
        btnActEu = findViewById(R.id.btnActEu);
        btnActInt = findViewById(R.id.btnActInt);
        btnInfoCards = findViewById(R.id.btnInfoCards);
    }

    private void setEventListeners() {
        btnActEu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHttpRequest("http://10.0.2.2:4000/enablecard/user1/EUROPE");
            }
        });
        btnActInt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHttpRequest("http://10.0.2.2:4000/enablecard/user1/INTERNATIONAL");
            }
        });
        btnInfoCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHttpRequest("http://10.0.2.2:4000/infocards/user1");
            }
        });
    }

    private void sendHttpRequest(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseToString(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                       Toast.makeText(MainActivity.this,  error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                        statusCode = response.statusCode;
                        return super.parseNetworkResponse(response);
                    }
                };
        requestQueue.add(jsonObjectRequest);
    }

    private void parseToString(JSONObject response) {
        StringBuilder infoCards = new StringBuilder();
        JSONArray jsonArray = response.optJSONArray("records");
        try {
            for (int i = 0, n = jsonArray.length(); i < n; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String key = jsonObject.keys().next();
                String value = jsonObject.optString(key);
                infoCards.append(key + ": ")
                        .append(value + "\n");
            }
            tvInfocards.setText(infoCards.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}