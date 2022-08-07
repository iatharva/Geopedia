package com.example.geopedia.usermenu;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.geopedia.HomeUser;
import com.example.geopedia.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class Uhome extends Fragment implements OnMapReadyCallback, PermissionsListener {

    View view;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private TextView weatherTemp,weatherMain,weatherDesc,readMore,readMore1;
    private ImageView weatherPic;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Mapbox.getInstance(getActivity(),getString(R.string.mapbox_private_token));
        view = inflater.inflate(R.layout.fragment_uhome, container, false);
        weatherTemp = view.findViewById(R.id.weatherTemp);
        weatherMain = view.findViewById(R.id.weatherMain);
        weatherDesc = view.findViewById(R.id.weatherDesc);
        weatherPic = view.findViewById(R.id.weatherPic);
        readMore = view.findViewById(R.id.readMore);
        readMore1 = view.findViewById(R.id.readMore1);
        getUserLocation();
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }

    public void getUserLocation() {
        //bing api key=AsfRPflcWB10ZLk1CpTZxq3MEItpjodmsZ1VhhpUSgZjPEHHHKAlY93bL5e5GdtR
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //18.512608; 433.780374;
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        String url = "http://dev.virtualearth.net/REST/v1/Locations/" + currentLatitude + "," + currentLongitude + "?includeEntityTypes=Address&includeNeighborhood=1&key=AsfRPflcWB10ZLk1CpTZxq3MEItpjodmsZ1VhhpUSgZjPEHHHKAlY93bL5e5GdtR";
        Uri uri = Uri.parse(url);
        Uri.Builder builder = uri.buildUpon();
        parseJson(builder.toString());

        //get the location data from the api shown as below
        String url2 = "https://api.openweathermap.org/data/2.5/weather?lat=" + currentLatitude + "&lon=" + currentLongitude + "&appid=5cdc5fdffefa3d96ca03b843e5d63229";
        Uri uri2 = Uri.parse(url2);
        Uri.Builder builder2 = uri2.buildUpon();
        parseJson2(builder2.toString());

        //for making link clickable
        readMore.setMovementMethod(LinkMovementMethod.getInstance());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            readMore.setText(Html.fromHtml("<a href='https://en.wikipedia.org/wiki/Pune'>- Read more</a>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            readMore.setText(Html.fromHtml("<a href='https://en.wikipedia.org/wiki/Pune'>- Read more</a>"));
        }

        readMore1.setMovementMethod(LinkMovementMethod.getInstance());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            readMore1.setText(Html.fromHtml("<a href='https://en.wikipedia.org/wiki/Maharashtra'>- Read more</a>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            readMore1.setText(Html.fromHtml("<a href='https://en.wikipedia.org/wiki/Maharashtra'>- Read more</a>"));
        }
    }

    public void parseJson2(String url)
    {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("weather");
                    JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                    String main = jsonObject1.getString("main");
                    String description = jsonObject1.getString("description");
                    String icon = jsonObject1.getString("icon");
                    JSONObject jsonObject4 = jsonObject.getJSONObject("main");
                    //get the field named as temp
                    String temp = jsonObject4.getString("temp");

                    
                    //set the text to the fields
                    weatherMain.setText(main);
                    weatherDesc.setText(description);
                    //Get the amount from the temp and convert from Kelvin to Celsius
                    int tempCelsius = (int) (Double.parseDouble(temp) - 273.15);
                    weatherTemp.setText(tempCelsius + "°C");
                    //get the icon from the response
                    String iconUrl = "https://openweathermap.org/img/w/" + icon + ".png";
                    //set the icon to the image view using volley
                    Picasso
                            .get()
                            .load(iconUrl)
                            .placeholder( R.drawable.loadinganimation)
                            .into(weatherPic);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show());
        queue.add(stringRequest);
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
                //locationText.setText(locationName + " " + neighborhood);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> error.printStackTrace());
        //Add the request to the queue
        requestQueue.add(stringRequest);

    }

    //All required methods for map
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        Uhome.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> enableLocationComponent(style));
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(getActivity())) {

        LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(getActivity())
                .build();
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(getActivity(), loadedMapStyle)
                        .locationComponentOptions(customLocationComponentOptions)
                        .build());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.zoomWhileTracking(200,0);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.NORMAL);

        //get last know location
        Location lastKnownLocation = locationComponent.getLastKnownLocation();
        
        if (lastKnownLocation != null)
        {
            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 15), 7000);
        }



        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getActivity(), "We need user permission in order to function the app properly", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle((Style.OnStyleLoaded) style -> enableLocationComponent(style));
        } else {
            Toast.makeText(getActivity(), "User permission is not given", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        //getUserLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
