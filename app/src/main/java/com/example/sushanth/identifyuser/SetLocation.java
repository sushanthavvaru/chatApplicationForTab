package com.example.sushanth.identifyuser;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.sushanth.identifyuser.MainActivity.xlatitude;
import static com.example.sushanth.identifyuser.MainActivity.xlongitude;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetLocation extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    GoogleMap mMap;
    MapView mpView;
    Double latitude, longitude;
    Button mapdone;

    public SetLocation() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_set_location, container, false);
        mapdone = (Button) v.findViewById(R.id.mapdone);
        mapdone.setEnabled(false);
        mpView = (MapView)v.findViewById(R.id.map_view);
        mpView.onCreate(savedInstanceState);
        mpView.onResume();
        mpView.getMapAsync(this);

        return v;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mapdone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnLocation passData = (returnLocation) getActivity();
                passData.printlocation(latitude, longitude);
                xlatitude = latitude;
                xlongitude= longitude;

                getFragmentManager().popBackStack();
              //  Toast.makeText(getActivity(), "" + latitude + longitude, Toast.LENGTH_LONG ).show();



            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
    }
    public void onMapClick(LatLng location){
        mMap.clear();
        mapdone.setEnabled(true);
        latitude = location.latitude;
        longitude = location.longitude;
        LatLng select = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(select));


    }

    public interface returnLocation{
        public void printlocation(Double mlatitude, Double mlongitude);
    }
}
