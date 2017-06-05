package com.example.sushanth.identifyuser;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


import static com.example.sushanth.identifyuser.MainActivity.drawer;


/**
 * A simple {@link Fragment} subclass.
 */
public class mapView extends Fragment implements View.OnClickListener, OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {


    ArrayList<String> usernames = new ArrayList<String>();
    int taskstart =0;
    int taskdone = 0;

    GoogleMap mMap;
    ArrayList<String> firebaseUsers = new ArrayList<String>();
    MapView mpView;
    String urlsent;
    private DatabaseHelper namesHelper;
    Button search, loadmore, listback, listnext;
    String query, query1;
    ArrayAdapter<String> adapter1;
    JSONArray data = new JSONArray();
    JSONArray userdata = new JSONArray();
    ArrayList<String> year = new ArrayList<String>();
    ArrayList<JSONObject> jsonUser = new ArrayList<JSONObject>();
    ArrayList<JSONObject> jsonUser1 = new ArrayList<JSONObject>();
    int maxIDinBismarck, maxIDinSQL, lastIDinBismarck, lastIDinSQL;
    int startYear = 1970;
    int presentYear = 2017;
    Spinner countryspinner, statespinner, yearspinner;
    ArrayList<String> countries = new ArrayList<String>();
    ArrayList<String> states = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    String selectedYear = "None";
    String  CountrySelectedInList= "None";
    String StateSelectedInlist = "None";
    Boolean scrollSQL = false;
    String username;
    FirebaseDatabase firebaseDataBase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map_view, container, false);


        countryspinner = (Spinner) v.findViewById(R.id.countryspinner1);
        statespinner = (Spinner) v.findViewById(R.id.statespinner1);
        yearspinner = (Spinner) v.findViewById(R.id.yearspinner1);
        search = (Button) v.findViewById(R.id.search1);
        loadmore = (Button) v.findViewById(R.id.loadmore);
        loadmore.setEnabled(false);
        loadmore.setOnClickListener(this);
        mpView = (MapView)v.findViewById(R.id.map_view1);
        mpView.onCreate(savedInstanceState);
        mpView.onResume();
        mpView.getMapAsync(this);
        search.setOnClickListener(this);
        year.clear();
        year.add("None");
        for (int i = startYear; i <= presentYear; i++) {
            year.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_item, year);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        yearspinner.setAdapter(adapter);


        yearspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedYear = parent.getItemAtPosition(position).toString();
                usernames.clear();
                mMap.clear();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedYear = parent.getItemAtPosition(0).toString();
                mMap.clear();
            }
        });

        firebaseDataBase = FirebaseDatabase.getInstance();
        DatabaseReference firebaseDatabaseReference = firebaseDataBase.getReference();

        firebaseDatabaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                final Iterable<DataSnapshot> childNodes = snapshot.getChildren();
                for (DataSnapshot child : snapshot.getChildren()){
                    firebaseUsers.add(child.getKey());
                    //Toast.makeText(getActivity(),"Users are " + child.getKey(),Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        start();
        return v;

    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        username = getArguments().getString("username");
    }


    public mapView() {
        // Required empty public constructor
    }

    public void start() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                data = response;
                int lengthOfCountries = data.length();
                //Log.d("rew", response.toString() + lengthOfCountries);
                countries.clear();
                countries.add("None");
                for (int i = 0; i < lengthOfCountries; i++) {
                    try {
                        countries.add(data.getString(i));
                        //Log.d("rew", data.getString(i));
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_spinner_item, countries);
                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);
                        countryspinner.setAdapter(adapter);

                        countryspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view,
                                                       int position, long id) {
                                CountrySelectedInList = parent.getItemAtPosition(position).toString();
                                usernames.clear();
                                states.clear();
                                states.add("None");
                                StateSelectedInlist ="None";
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                                        android.R.layout.simple_spinner_item, states);
                                adapter.setDropDownViewResource(
                                        android.R.layout.simple_spinner_dropdown_item);
                                statespinner.setAdapter(adapter);
                                usernames.clear();
                                mMap.clear();
                                loadmore.setEnabled(false);
                                getStateInformation();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                CountrySelectedInList = parent.getItemAtPosition(0).toString();
                                mMap.clear();
                                loadmore.setEnabled(false);

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }


            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };
        String url = "http://bismarck.sdsu.edu/hometown/countries";
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        VolleyQueue.instance(getActivity()).add(getRequest);
    }

    public void getStateInformation() {
        if (CountrySelectedInList == "None") {
            states.clear();
            states.add("None");
            StateSelectedInlist ="None";
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, states);
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            statespinner.setAdapter(adapter);


        } else {

            Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
                public void onResponse(JSONArray response) {
                    data = response;
                    int lengthOfStates = data.length();
                    //Log.d("rew", response.toString() + lengthOfStates);
                    states.clear();
                    states.add("None");
                    for (int i = 0; i < lengthOfStates; i++) {
                        try {
                            states.add(data.getString(i));
                            //Log.d("rew", data.getString(i));
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                                    android.R.layout.simple_spinner_item, states);
                            adapter.setDropDownViewResource(
                                    android.R.layout.simple_spinner_dropdown_item);
                            statespinner.setAdapter(adapter);

                            statespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> parent, View view,
                                                           int position, long id) {
                                    StateSelectedInlist = parent.getItemAtPosition(position).toString().replace(" ","%20");
                                    usernames.clear();
                                    loadmore.setEnabled(false);

                                    mMap.clear();

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    StateSelectedInlist = parent.getItemAtPosition(0).toString();
                                    mMap.clear();
                                    loadmore.setEnabled(false);


                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }


                }
            };
            Response.ErrorListener failure = new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Log.d("rew", error.toString());
                }
            };
            String url = "http://bismarck.sdsu.edu/hometown/states?country=" + CountrySelectedInList;
            JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
            VolleyQueue.instance(getActivity()).add(getRequest);
        }
    }

    public void getTheDetails(){


        if(CountrySelectedInList == "None" && StateSelectedInlist =="None" && selectedYear =="None"){
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0";
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30";
            query = "select * from usersInDb where";
            query1 = "select MAX(_ID) from usersInDb";
            usernames.clear();
            LatLng loc = new LatLng(0.0, 0.0);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(1),2000,null);
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist =="None" && selectedYear =="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&country="+CountrySelectedInList;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=1&country="+CountrySelectedInList;
            query = "select * from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\"";
            usernames.clear();
            callGeocoder(CountrySelectedInList);
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist !="None" && selectedYear =="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&" +
                    "country="+CountrySelectedInList+"&state="+StateSelectedInlist;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=1&country="+CountrySelectedInList+"&state="+StateSelectedInlist;
            query = "select * from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\"";
            usernames.clear();
            callGeocoder(CountrySelectedInList, StateSelectedInlist);
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist !="None" && selectedYear !="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&country="+CountrySelectedInList+"&state="+StateSelectedInlist + "&year="+selectedYear;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&country="+CountrySelectedInList+"&state="+StateSelectedInlist + "&year="+selectedYear;
            query = "select * from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\" and YEAR =\"" + selectedYear.toString()+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\" and YEAR =\"" + selectedYear.toString()+"\"";
            usernames.clear();
            callGeocoder(CountrySelectedInList, StateSelectedInlist);
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList == "None" && StateSelectedInlist !="None" && selectedYear =="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&state="+StateSelectedInlist;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&state="+StateSelectedInlist;
            query = "select * from usersInDb where STATE=\""+StateSelectedInlist+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where STATE=\""+StateSelectedInlist+"\"";
            usernames.clear();
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList == "None" && StateSelectedInlist !="None" && selectedYear !="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&state="+StateSelectedInlist + "&year="+selectedYear;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&state="+StateSelectedInlist + "&year="+selectedYear;
            query = "select * from usersInDb where STATE=\""+StateSelectedInlist+"\" and YEAR =\"" + selectedYear.toString()+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where STATE=\""+StateSelectedInlist+"\" and YEAR =\"" + selectedYear.toString()+"\"";
            usernames.clear();
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList == "None" && StateSelectedInlist =="None" && selectedYear !="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&year="+selectedYear;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&year="+selectedYear;
            query = "select * from usersInDb where YEAR =\"" + selectedYear.toString()+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where YEAR =\"" + selectedYear.toString()+"\"";
            usernames.clear();
            LatLng loc = new LatLng(0.0, 0.0);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(1),2000,null);
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist =="None" && selectedYear !="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&country="+CountrySelectedInList+"&year="+selectedYear;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&country="+CountrySelectedInList+"&year="+selectedYear;
            query = "select _ID from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and YEAR =\"" + selectedYear.toString()+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and YEAR =\"" + selectedYear.toString()+"\"";
            usernames.clear();
            callGeocoder(CountrySelectedInList);
            //+countryURL+stateURL+yearURL+"
        }
        //check for ID in both Bismarck and SQLDB

        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                //Log.d("rew", response.toString());
                userdata = response;
                int numberOfUserAfterQuery = userdata.length();

                if (numberOfUserAfterQuery == 0) {
                    Toast.makeText(getActivity(), "No users found", Toast.LENGTH_SHORT).show();
                    return;

                }


                try{
                    jsonUser1.clear();
                    for(int i=0;i<1;i++){
                        jsonUser1.add(userdata.getJSONObject(i));


                    }
                    //Log.i("rew", ""+jsonUser.size());


                    Log.i("rew", "clear called");
                    for(JSONObject eachuser1: jsonUser1){
                        //Log.i("rew", eachuser.getString("nickname"));
                        maxIDinBismarck = eachuser1.getInt("id");
                        namesHelper = (new DatabaseHelper(getContext()));
                        maxIDinSQL = namesHelper.getMaxID(query1);

                        //Toast.makeText(getActivity(), "bis"+ maxIDinBismarck+ " and DB" + maxIDinSQL, Toast.LENGTH_SHORT ).show();

                        if(maxIDinBismarck == maxIDinSQL){
                            //load data from SQL Database 25 data each time
                            loadmore.setEnabled(false);
                            search.setEnabled(false);
                            lastIDinSQL = maxIDinSQL+1;
                            namesHelper = (new DatabaseHelper(getContext()));
                            SQLiteDatabase nameDb = namesHelper.getWritableDatabase();
                            Cursor result =nameDb.rawQuery(query + " _ID<\""+lastIDinSQL+"\" ORDER BY _ID DESC limit \"25\"", null);
                            while(result.moveToNext()){
                                LatLng select = new LatLng(result.getDouble(6), result.getDouble(7));
                                mMap.addMarker(new MarkerOptions().position(select).title("Name:").snippet(result.getString(1)));
                                lastIDinSQL = result.getInt(0);
                                lastIDinBismarck = lastIDinSQL;
                                scrollSQL = true;
                                nameDb.close();
                            }
                            loadmore.setEnabled(true);
                            search.setEnabled(true);
                        }
                        else{
                            //Insert 25 data from the server into SQL and display from database
                            scrollSQL = false;

                            {
                                //do this to refresh
                                try {
                                    jsonUser.clear();
                                    for (int i = 0; i < numberOfUserAfterQuery; i++) {
                                        jsonUser.add(userdata.getJSONObject(i));

                                    }
                                    //Log.i("rew", ""+jsonUser.size());
                                    usernames.clear();
                                    Log.i("rew", "clear callsed");
                                    namesHelper = (new DatabaseHelper(getContext()));
                                    SQLiteDatabase nameDb = namesHelper.getWritableDatabase();
                                    loadmore.setEnabled(false);
                                    search.setEnabled(false);
                                    doAsync(nameDb, jsonUser);

                                } catch (JSONException e) {
                                    //Handle
                                    Log.i("rew", "Error Occured");
                                }
                            }
                        }
                    }

                }catch (JSONException e){
                    //Handle
                    Log.i("rew", "Error Occured");
                }






            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };

        Log.i("rew", urlsent);
        JsonArrayRequest getRequest = new JsonArrayRequest(urlsent, success, failure);
        VolleyQueue.instance(getActivity()).add(getRequest);

    }
    public void doAsync(SQLiteDatabase nameDb, ArrayList<JSONObject> jsonUser) {

        new SampleTask(nameDb,jsonUser ).execute();


    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String clickeduser = marker.getSnippet().toString();
        //Toast.makeText(getActivity(),clickeduser,Toast.LENGTH_SHORT).show();

        if(clickeduser.equals(username)){
            Toast.makeText(getActivity(),"Self chat denied",Toast.LENGTH_SHORT).show();
        }
        else if(firebaseUsers.contains(clickeduser)){
            //Toast.makeText(getActivity(),"Lets chat",Toast.LENGTH_SHORT).show();

            //start chat activity
            startChatActivityOnClick(clickeduser);
        }
        else{
            Toast.makeText(getActivity(),clickeduser+" does not exist in the Firebase Server!",Toast.LENGTH_SHORT).show();
        }

    }

    private void startChatActivityOnClick(String clickeduser) {
        Intent go = new Intent(getContext(),Chat.class);
        go.putExtra("clickeduser",clickeduser);
        startActivity(go);
    }

    class SampleTask extends AsyncTask<Void, Void, SQLiteDatabase> {
        Boolean loadFromSQl = false;

        ArrayList<JSONObject> jsonUser2;
        SQLiteDatabase nameDb1;

        public SampleTask(SQLiteDatabase nameDb1,ArrayList<JSONObject> jsonUser2){
            this.jsonUser2 =  jsonUser2 ;
            this.nameDb1 = nameDb1;
        }


        protected void onPostExecute(SQLiteDatabase nameDb1 ) {
            nameDb1.close();
            for(JSONObject eachuser1:jsonUser2){
                try {
                    lastIDinBismarck = eachuser1.getInt("id");
                    LatLng  select= new LatLng(eachuser1.getDouble("latitude"), eachuser1.getDouble("longitude"));
                    if(eachuser1.getDouble("latitude") ==0.0 && eachuser1.getDouble("longitude") == 0.0 ){
                        //do geocoding
                        double latitude_db = 0.0;
                        double longitude_db = 0.0;
                        Geocoder locator = new Geocoder(getActivity());
                        try {
                            List<Address> state = locator.getFromLocationName(eachuser1.getString("country")+", "+eachuser1.getString("state"), 1);
                            for (Address stateLocation: state) {
                                if (stateLocation.hasLatitude())
                                    latitude_db = stateLocation.getLatitude();
                                if (stateLocation.hasLongitude())
                                    longitude_db = stateLocation.getLongitude();
                            }
                        } catch (Exception error) {
                            Log.e("rew", "Address lookup Error", error);
                        }
                        select = new LatLng(latitude_db, longitude_db);
                        mMap.addMarker(new MarkerOptions().position(select).title("Name:").snippet(eachuser1.getString("nickname")));
                    }
                    else{
                        mMap.addMarker(new MarkerOptions().position(select).title("Name:").snippet(eachuser1.getString("nickname")));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            loadmore.setEnabled(true);
            search.setEnabled(true);
            scrollSQL = false;

            //adapter1.setNotifyOnChange(true);

        }
        protected SQLiteDatabase doInBackground(Void... words) {

            for(JSONObject eachuser1:jsonUser2){

                int Id=0;
                try {
                    Id = eachuser1.getInt("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Cursor result = nameDb1.rawQuery("select * from usersInDb where _ID =\""+Id+"\"", null);
                int rowCount = result.getCount();
                if(rowCount>0){
                    //add data from SQL
                    lastIDinSQL = Id+1;
                    loadFromSQl = true;
                    return nameDb1;

                }
                else{
                    loadFromSQl = false;
                }




                ContentValues contentValues = new ContentValues();

                try {
                    if(!(eachuser1.getDouble("latitude") == 0.0 && eachuser1.getDouble("longitude") == 0.0)){
                        contentValues.put("_ID", eachuser1.getInt("id"));
                        contentValues.put("NAME", eachuser1.getString("nickname"));
                        contentValues.put("CITY", eachuser1.getString("city"));
                        contentValues.put("STATE", eachuser1.getString("state"));
                        contentValues.put("YEAR", eachuser1.getInt("year"));
                        contentValues.put("COUNTRY", eachuser1.getString("country"));
                        contentValues.put("LAT", eachuser1.getDouble("latitude"));
                        contentValues.put("LONG", eachuser1.getDouble("longitude"));
                    }
                    else{
                        //do geocoding and load in database
                        double latitude_db = 0.0;
                        double longitude_db = 0.0;
                        Geocoder locator = new Geocoder(getActivity());
                        try {
                            List<Address> state = locator.getFromLocationName(eachuser1.getString("country")+", "+eachuser1.getString("state"), 1);
                            for (Address stateLocation: state) {
                                if (stateLocation.hasLatitude())
                                    latitude_db = stateLocation.getLatitude();
                                if (stateLocation.hasLongitude())
                                    longitude_db = stateLocation.getLongitude();
                            }
                        } catch (Exception error) {
                            Log.e("rew", "Address lookup Error", error);
                        }
                        LatLng stateLatLng = new LatLng(latitude_db, longitude_db);
                        double la = stateLatLng.latitude;
                        double lng = stateLatLng.longitude;
                        contentValues.put("_ID", eachuser1.getInt("id"));
                        contentValues.put("NAME", eachuser1.getString("nickname"));
                        contentValues.put("CITY", eachuser1.getString("city"));
                        contentValues.put("STATE", eachuser1.getString("state"));
                        contentValues.put("YEAR", eachuser1.getInt("year"));
                        contentValues.put("COUNTRY", eachuser1.getString("country"));
                        contentValues.put("LAT", la);
                        contentValues.put("LONG", lng);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                nameDb1.insertWithOnConflict("usersInDb", null, contentValues,nameDb1.CONFLICT_IGNORE);
            }
            return nameDb1;

        }
    }



    private void additemsFromSQl() {
        search.setEnabled(false);
        loadmore.setEnabled(false);

        namesHelper = (new DatabaseHelper(getContext()));
        SQLiteDatabase nameDb = namesHelper.getWritableDatabase();
        Cursor result =nameDb.rawQuery(query + " _ID<\""+lastIDinSQL+"\" ORDER BY _ID DESC limit \"25\"", null);
        if (result.getCount()>0){
            while(result.moveToNext()){
                LatLng select = new LatLng(result.getDouble(6), result.getDouble(7));
                mMap.addMarker(new MarkerOptions().position(select).title("Name:").snippet(result.getString(1)));

                lastIDinSQL = result.getInt(0);
                lastIDinBismarck = lastIDinSQL;
                scrollSQL = true;
                nameDb.close();

            }
            search.setEnabled(true);
            loadmore.setEnabled(true);
            //listallsuers.setAdapter(adapter1);
            //do on notify change adapater here
        }
        else{
            scrollSQL = false;
            additems();

        }








    }

    private void additems() {


        if(CountrySelectedInList == "None" && StateSelectedInlist =="None" && selectedYear =="None"){
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&beforeid="+lastIDinBismarck;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30";
            query = "select _ID from usersInDb";
            query1 = "select MAX(_ID) from usersInDb";

            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist =="None" && selectedYear =="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&country="+CountrySelectedInList+"&beforeid="+lastIDinBismarck;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=1&country="+CountrySelectedInList;
            query = "select _ID from usersInDb where COUNTRY =\""+CountrySelectedInList+"\"";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\"";

            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist !="None" && selectedYear =="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&" +
                    "country="+CountrySelectedInList+"&state="+StateSelectedInlist+"&beforeid="+lastIDinBismarck;;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=1&country="+CountrySelectedInList+"&state="+StateSelectedInlist;
            query = "select _ID from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\"";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\"";

            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist !="None" && selectedYear !="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&country="+CountrySelectedInList+"&state="+StateSelectedInlist + "&year="+selectedYear+"&beforeid="+lastIDinBismarck;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&country="+CountrySelectedInList+"&state="+StateSelectedInlist + "&year="+selectedYear;
            query = "select _ID from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\" and YEAR =\"" + selectedYear.toString()+"\"";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\" and YEAR =\"" + selectedYear.toString()+"\"";

            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList == "None" && StateSelectedInlist !="None" && selectedYear =="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&state="+StateSelectedInlist+"&beforeid="+lastIDinBismarck;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&state="+StateSelectedInlist;
            query = "select _ID from usersInDb where STATE=\""+StateSelectedInlist+"\"";
            query1 = "select MAX(_ID) from usersInDb where STATE=\""+StateSelectedInlist+"\"";

            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList == "None" && StateSelectedInlist !="None" && selectedYear !="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&state="+StateSelectedInlist + "&year="+selectedYear+"&beforeid="+lastIDinBismarck;;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&state="+StateSelectedInlist + "&year="+selectedYear;
            query = "select _ID from usersInDb where STATE=\""+StateSelectedInlist+"\" and YEAR =\"" + selectedYear.toString()+"\"";
            query1 = "select MAX(_ID) from usersInDb where STATE=\""+StateSelectedInlist+"\" and YEAR =\"" + selectedYear.toString()+"\"";

            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList == "None" && StateSelectedInlist =="None" && selectedYear !="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&year="+selectedYear+"&beforeid="+lastIDinBismarck;;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&year="+selectedYear;
            query = "select _ID from usersInDb where YEAR =\"" + selectedYear.toString()+"\"";
            query1 = "select MAX(_ID) from usersInDb where YEAR =\"" + selectedYear.toString()+"\"";

            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist =="None" && selectedYear !="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&country="+CountrySelectedInList+"&year="+selectedYear+"&beforeid="+lastIDinBismarck;;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&country="+CountrySelectedInList+"&year="+selectedYear;
            query = "select _ID from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and YEAR =\"" + selectedYear.toString()+"\"";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and YEAR =\"" + selectedYear.toString()+"\"";

            //+countryURL+stateURL+yearURL+"
        }




        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                //Log.d("rew", response.toString());
                userdata = response;
                int numberOfUserAfterQuery = userdata.length();
                if (numberOfUserAfterQuery == 0) {
                    Toast.makeText(getActivity(), "Loaded all users", Toast.LENGTH_SHORT).show();
                    loadmore.setEnabled(false);
                    search.setEnabled(true);
                    return;

                }
                else{
                    Toast.makeText(getActivity(), "Loading", Toast.LENGTH_SHORT).show();

                }

                try{
                    jsonUser1.clear();
                    for(int i=0;i<1;i++){
                        jsonUser1.add(userdata.getJSONObject(i));


                    }
                    //Log.i("rew", ""+jsonUser.size());


                    Log.i("rew", "clear called");
                    for(JSONObject eachuser1: jsonUser1){
                        //Log.i("rew", eachuser.getString("nickname"));
                        maxIDinBismarck = eachuser1.getInt("id");
                        //namesHelper = (new DatabaseHelper(getContext()));
                        //maxIDinSQL = namesHelper.getMaxID(query1);

                        //Toast.makeText(getActivity(), ""+ maxIDinBismarck+ " and " + maxIDinSQL, Toast.LENGTH_LONG ).show();


                        {
                            //Insert 25 data from the server into SQL and display from bismarck
                            {

                                //do this to refresh
                                try {
                                    jsonUser.clear();
                                    for (int i = 0; i < numberOfUserAfterQuery; i++) {
                                        jsonUser.add(userdata.getJSONObject(i));

                                    }

                                    //Log.i("rew", ""+jsonUser.size());

                                    namesHelper = (new DatabaseHelper(getContext()));
                                    SQLiteDatabase nameDb = namesHelper.getWritableDatabase();
                                    doAsync(nameDb, jsonUser);
                                    loadmore.setEnabled(false);
                                    search.setEnabled(false);
                                    Log.i("rew", "clear callsed");
                                    //listallsuers.setAdapter(adapter1);
                                } catch (JSONException e) {
                                    //Handle
                                    Log.i("rew", "Error Occured");
                                }
                            }



                        }


                    }

                }catch (JSONException e){
                    //Handle
                    Log.i("rew", "Error Occured");
                }

            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };

        Log.i("rew", urlsent);
        JsonArrayRequest getRequest = new JsonArrayRequest(urlsent, success, failure);
        VolleyQueue.instance(getActivity()).add(getRequest);


    }

    public void callGeocoder(String CountrySelected){

        double latitude = 0.0;
        double longitude = 0.0;
        Geocoder locator = new Geocoder(getActivity());
        try {
            List<Address> state = locator.getFromLocationName(CountrySelected ,1);
            for (Address stateLocation: state) {
                if (stateLocation.hasLatitude())
                    latitude = stateLocation.getLatitude();
                if (stateLocation.hasLongitude())
                    longitude = stateLocation.getLongitude();
            }
        } catch (Exception error) {
            Log.e("rew", "Address lookup Error", error);
        }
        LatLng stateLatLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(stateLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(3),2000,null);

    }
    public  void callGeocoder(String CountrySelected, String StateSelected) {
        double latitude = 0.0;
        double longitude = 0.0;
        Geocoder locator = new Geocoder(getActivity());
        try {
            List<Address> state = locator.getFromLocationName(CountrySelected+", "+StateSelected, 1);
            for (Address stateLocation: state) {
                if (stateLocation.hasLatitude())
                    latitude = stateLocation.getLatitude();
                if (stateLocation.hasLongitude())
                    longitude = stateLocation.getLongitude();
            }
        } catch (Exception error) {
            Log.e("rew", "Address lookup Error", error);
        }
        LatLng stateLatLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(stateLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(6),2000,null);


    }

    @Override
        public void onClick(View v) {
        if (v.getId() == R.id.search1) {
            //namesHelper = (new DatabaseHelper(getContext()));
            //namesHelper.clearTheDb();
            search.setEnabled(false);
            mMap.clear();
            getTheDetails();

        }
        if(v.getId()==R.id.loadmore){
            loadmore.setEnabled(false);
            if(scrollSQL ==false){
                additems();
            }
            if (scrollSQL ==true){
                additemsFromSQl();


            }

        }

    }





    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}
