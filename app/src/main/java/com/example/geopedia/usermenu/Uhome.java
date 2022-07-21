package com.example.geopedia.usermenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.geopedia.HomeUser;
import com.example.geopedia.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Uhome extends Fragment {

    TextView locationText;
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_uhome, container, false);
        locationText = view.findViewById(R.id.locationText);
        getUserLocation();
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getUserLocation();
    }

    public void getUserLocation() {
        //bing api key=AsfRPflcWB10ZLk1CpTZxq3MEItpjodmsZ1VhhpUSgZjPEHHHKAlY93bL5e5GdtR
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        String url = "http://dev.virtualearth.net/REST/v1/Locations/" + currentLatitude + "," + currentLongitude + "?includeEntityTypes=Address&includeNeighborhood=1&key=AsfRPflcWB10ZLk1CpTZxq3MEItpjodmsZ1VhhpUSgZjPEHHHKAlY93bL5e5GdtR";
        Uri uri = Uri.parse(url);
        Uri.Builder builder = uri.buildUpon();
        parseJson(builder.toString());
    }
    public void parseJson(String url)
    {
        //Create a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        //Create a string request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            //Parse the response
            try {
                JSONObject jsonObject = new JSONObject(response);
                //Location Name is present in resourceSets(object) -> resources(list containing one object) ->  address (object) -> Value present in the Key named "locality" and neighborhood
                JSONObject resourceSets = jsonObject.getJSONObject("resourceSets");
                JSONArray resources = resourceSets.getJSONArray("resources");
                JSONObject resourcesObject = resources.getJSONObject(0);
                JSONObject address = resourcesObject.getJSONObject("address");
                String locationName = address.getString("locality");
                String neighborhood = address.getString("neighborhood");
                locationText.setText(locationName + " " + neighborhood);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> error.printStackTrace());
        //Add the request to the queue
        requestQueue.add(stringRequest);

    }
}
