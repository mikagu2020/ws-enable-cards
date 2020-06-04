package com.example.infocard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;

    TextView tvLastActiveEur, tvLastActiveInt, tvCardEurope, tvCardInternational, tvCurrentCard;
    Button btnActEu, btnActInt, btnInfoCards;
    String statusCode;
    Map<String, String> records;
    final static String LAST_ACTIVE_EUR = "lastactive_eur";
    final static String LAST_ACTIVE_INT = "lastactive_int";
    final static String CARD_EUROPE = "card_europe";
    final static String CARD_INTERNATIONAL = "card_international";
    final static String CURRENT_CARD = "current_card";
    final static String UE = "DE AT BE BG CY HR DK SK SI ES EE FI FR EL HU IE IT LV LT LU MT NL PL PT UK CZ RO SE";

    double longitude, latitude;
    private boolean permisosFineLocation = false;
    private static final int PERMISOS_LOCATION = 1;
    private static String codPais;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getControlViews();

        setEventListeners();

        records = new HashMap<String, String>();

        obtenerCoordenadas();

    }

    private void getControlViews() {
        tvLastActiveEur = findViewById(R.id.last_active_eur);
        tvLastActiveInt = findViewById(R.id.last_active_int);
        tvCardEurope = findViewById(R.id.card_europe);
        tvCardInternational = findViewById(R.id.card_international);
        tvCurrentCard = findViewById(R.id.current_card);

        btnActEu = findViewById(R.id.btnActEu);
        btnActInt = findViewById(R.id.btnActInt);
        btnInfoCards = findViewById(R.id.btnInfoCards);
    }

    private void setEventListeners() {
        btnActEu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UE.contains(codPais)) {
                    sendHttpRequest("http://10.0.2.2:4000/enablecard/user1/EUROPE");
                } else {
                    Toast.makeText(MainActivity.this, "La tarjeta no se puede activar porque NO ESTÁS DENTRO DE LA UE", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnActInt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!UE.contains(codPais)) {
                    sendHttpRequest("http://10.0.2.2:4000/enablecard/user1/INTERNATIONAL");
                } else {
                    Toast.makeText(MainActivity.this, "La tarjeta no se puede activar porque NO ESTÁS FUERA DE LA UE", Toast.LENGTH_SHORT).show();
                }
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
                        loadInfo();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Map<String, String> headers = response.headers;
                statusCode = headers.get("X-Android-Response-Source");
                return super.parseNetworkResponse(response);
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void parseToString(JSONObject response) {
        //StringBuilder infoCards = new StringBuilder();
        JSONArray jsonArray = response.optJSONArray("records");
        int n = jsonArray.length();
        try {
            for (int i = 0; i < n; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String key = jsonObject.keys().next();
                String value = jsonObject.optString(key);
                records.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadInfo() {
        tvLastActiveEur.setText("Última activacion europea: \n" + records.get(LAST_ACTIVE_EUR));
        tvLastActiveInt.setText("Última activacion internacional: \n" + records.get(LAST_ACTIVE_INT));
        tvCardEurope.setText("Tarjeta europea: \n" + records.get(CARD_EUROPE));
        tvCardInternational.setText("Tarjeta internacional: \n" + records.get(CARD_INTERNATIONAL));
        tvCurrentCard.setText("Última tarjeta activada: \n" + records.get(CURRENT_CARD));
    }


    private void obtenerCoordenadas() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISOS_LOCATION);

        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                            Geocoder geocoder = new Geocoder(MainActivity.this);
                            try {
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                Address address = addresses.get(0);
                                codPais = address.getCountryCode();
                                Toast.makeText(MainActivity.this, codPais, Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISOS_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    obtenerCoordenadas();
                } else {
                    cancelar();
                }
                break;

            // Aquí más casos dependiendo de los permisos
            // case OTRO_CODIGO_DE_PERMISOS...
        }
    }

    private void cancelar() {

    }

}