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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.sushanth.identifyuser.MainActivity.closeActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class listView extends Fragment implements View.OnClickListener {
    ArrayList<String> usernames = new ArrayList<String>();
    String urlsent;
    private DatabaseHelper namesHelper;
    String query, query1;
    ListView listallsuers;
    Button search, listback, listnext;
    ArrayList<String> firebaseUsers = new ArrayList<String>();
    ArrayAdapter<String> adapter1;
    JSONArray data = new JSONArray();
    JSONArray userdata = new JSONArray();
    ArrayList<String> year = new ArrayList<String>();
    ArrayList<JSONObject> jsonUser = new ArrayList<JSONObject>();
    ArrayList<JSONObject> jsonUser1 = new ArrayList<JSONObject>();
    int startYear = 1970;
    int presentYear = 2017;
    Spinner countryspinner, statespinner, yearspinner;
    ArrayList<String> countries = new ArrayList<String>();
    ArrayList<String> states = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    String username;

    String selectedYear = "None";
    String  CountrySelectedInList= "None";
    String StateSelectedInlist = "None";
    int maxIDinBismarck, maxIDinSQL, lastIDinBismarck, lastIDinSQL;
    Boolean flag_loading = false;
    Boolean scrollSQL = false;
    FirebaseDatabase firebaseDataBase;

    public listView() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list_view, container, false);
        countryspinner = (Spinner) v.findViewById(R.id.countryspinner);
        statespinner = (Spinner) v.findViewById(R.id.statespinner);
        yearspinner = (Spinner) v.findViewById(R.id.yearspinner);
        listallsuers = (ListView) v.findViewById(R.id.listallusers);
        username = getArguments().getString("username");

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
        listallsuers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickeduser ="";

                String toBeProcessed = parent.getItemAtPosition(position).toString();
                toBeProcessed = toBeProcessed.replace("\n", "").replace("\r", "").replaceAll("\\s","");
                Pattern pattern = Pattern.compile("Name(.*?)Country");
                Matcher matcher = pattern.matcher(toBeProcessed);
                while (matcher.find()) {
                    clickeduser = matcher.group(1);
                    //Toast.makeText(getActivity(),clickeduser,Toast.LENGTH_SHORT).show();
                    System.out.println(matcher.group(1));
                }
                if(clickeduser.equals(username)){
                    Toast.makeText(getActivity(),"Self chat denied",Toast.LENGTH_SHORT).show();
                }
                else if(firebaseUsers.contains(clickeduser)){
                    Toast.makeText(getActivity(),"Lets chat",Toast.LENGTH_SHORT).show();

                    //start chat activity
                    startChatActivityOnClick(clickeduser);
                }
                else{
                    Toast.makeText(getActivity(),clickeduser+" does not exist in the Firebase Server!",Toast.LENGTH_SHORT).show();
                }

            }
        });


        listallsuers.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                {
                    if(flag_loading == false && scrollSQL ==false)
                    {
                        flag_loading = true;
                        additems();
                    }
                    if(flag_loading == false && scrollSQL ==true)
                    {
                        flag_loading = true;
                        additemsFromSQl();
                    }
                }
            }
        });
        search = (Button) v.findViewById(R.id.search);
        search.setOnClickListener(this);
        listback = (Button) v.findViewById(R.id.listback);
        listnext = (Button) v.findViewById(R.id.listnext);

        listback.setVisibility(View.GONE);
        listnext.setVisibility(View.GONE);

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
                adapter1 =
                        new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, usernames);
                listallsuers.setAdapter(adapter1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedYear = parent.getItemAtPosition(0).toString();
                usernames.clear();
                adapter1 =
                        new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, usernames);
                listallsuers.setAdapter(adapter1);

            }
        });

        start();


        return v;

    }

    private void startChatActivityOnClick(String clickeduser) {
        Intent go = new Intent(getContext(),Chat.class);
        go.putExtra("clickeduser",clickeduser);
        startActivity(go);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void additemsFromSQl() {

        namesHelper = (new DatabaseHelper(getContext()));
        SQLiteDatabase nameDb = namesHelper.getWritableDatabase();
        Cursor result =nameDb.rawQuery(query + " _ID<\""+lastIDinSQL+"\" ORDER BY _ID DESC limit \"25\"", null);
        if (result.getCount()>0){
            while(result.moveToNext()){
                adapter1.add("Name " + result.getString(1) + "\nCountry " + result.getString(2)
                        + "\nState " + result.getString(3) + "\nCity " + result.getString(4)
                        + "\nYear " + String.valueOf(result.getInt(5)));
                lastIDinSQL = result.getInt(0);
                lastIDinBismarck = lastIDinSQL;
                scrollSQL = true;
                nameDb.close();

            }
            //listallsuers.setAdapter(adapter1);
            //do on notify change adapater here
            flag_loading=false;
            adapter1.setNotifyOnChange(true);


        }
        else{
            scrollSQL = false;
            additems();

        }








    }

    private void additems() {
        search.setEnabled(false);


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
                    search.setEnabled(true);

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
                                    doAsync2(nameDb, jsonUser, adapter1);


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
                                adapter1 =
                                        new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, usernames);
                                listallsuers.setAdapter(adapter1);
                                getStateInformation();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                CountrySelectedInList = parent.getItemAtPosition(0).toString();
                                usernames.clear();
                                adapter1 =
                                        new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, usernames);
                                listallsuers.setAdapter(adapter1);

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
                                    adapter1 =
                                            new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, usernames);
                                    listallsuers.setAdapter(adapter1);

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    StateSelectedInlist = parent.getItemAtPosition(0).toString();
                                    usernames.clear();
                                    adapter1 =
                                            new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, usernames);
                                    listallsuers.setAdapter(adapter1);


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
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist =="None" && selectedYear =="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&country="+CountrySelectedInList;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=1&country="+CountrySelectedInList;
            query = "select * from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\"";
            usernames.clear();
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist !="None" && selectedYear =="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&" +
                    "country="+CountrySelectedInList+"&state="+StateSelectedInlist;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=1&country="+CountrySelectedInList+"&state="+StateSelectedInlist;
            query = "select * from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\"";
            usernames.clear();
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist !="None" && selectedYear !="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&country="+CountrySelectedInList+"&state="+StateSelectedInlist + "&year="+selectedYear;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&country="+CountrySelectedInList+"&state="+StateSelectedInlist + "&year="+selectedYear;
            query = "select * from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\" and YEAR =\"" + selectedYear.toString()+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and STATE=\""+StateSelectedInlist+"\" and YEAR =\"" + selectedYear.toString()+"\"";
            usernames.clear();
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
            //+countryURL+stateURL+yearURL+"
        }
        else if(CountrySelectedInList != "None" && StateSelectedInlist =="None" && selectedYear !="None") {
            urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&country="+CountrySelectedInList+"&year="+selectedYear;
            //urlsent ="http://bismarck.sdsu.edu/hometown/users?&reverse=true&page=0&pagesize=30&country="+CountrySelectedInList+"&year="+selectedYear;
            query = "select _ID from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and YEAR =\"" + selectedYear.toString()+"\" AND";
            query1 = "select MAX(_ID) from usersInDb where COUNTRY =\""+CountrySelectedInList+"\" and YEAR =\"" + selectedYear.toString()+"\"";
            usernames.clear();
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

                        //Toast.makeText(getActivity(), "bis"+ maxIDinBismarck+ " and DB" + maxIDinSQL, Toast.LENGTH_LONG ).show();
                        adapter1 =
                                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, usernames);

                        if(maxIDinBismarck == maxIDinSQL){
                            //load data from SQL Database 25 data each time
                            lastIDinSQL = maxIDinSQL+1;
                            namesHelper = (new DatabaseHelper(getContext()));
                            SQLiteDatabase nameDb = namesHelper.getWritableDatabase();
                            Cursor result =nameDb.rawQuery(query + " _ID<\""+lastIDinSQL+"\" ORDER BY _ID DESC limit \"25\"", null);


                            while(result.moveToNext()){
                                usernames.add("Name " + result.getString(1) + "\nCountry " + result.getString(2)
                                        + "\nState " + result.getString(3) + "\nCity " + result.getString(4)
                                        + "\nYear " + String.valueOf(result.getInt(5)));
                                lastIDinSQL = result.getInt(0);
                                lastIDinBismarck = lastIDinSQL;
                                scrollSQL = true;
                                nameDb.close();

                            }
                            listallsuers.setAdapter(adapter1);


                        }
                        else{
                            //Insert 25 data from the server into SQL and display from bismarck
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
            listallsuers.setAdapter(adapter1);
            if(loadFromSQl == true) {
                additemsFromSQl();
            }
            //adapter1.setNotifyOnChange(true);




        }

        protected SQLiteDatabase doInBackground(Void... words) {
            {
                for(JSONObject eachuser1:jsonUser2) {
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

                    ContentValues contentValues = new ContentValues();
                    try {
                        contentValues.put("_ID", eachuser1.getInt("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        contentValues.put("NAME", eachuser1.getString("nickname"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        contentValues.put("CITY", eachuser1.getString("city"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        contentValues.put("LONG", eachuser1.getDouble("longitude"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        contentValues.put("STATE", eachuser1.getString("state"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        contentValues.put("YEAR", eachuser1.getInt("year"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        contentValues.put("LAT", eachuser1.getDouble("latitude"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        contentValues.put("COUNTRY", eachuser1.getString("country"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    nameDb1.insertWithOnConflict("usersInDb", null, contentValues, nameDb1.CONFLICT_IGNORE);
                    try {
                        lastIDinBismarck = eachuser1.getInt("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        usernames.add("Name " + eachuser1.getString("nickname") + "\nCountry " + eachuser1.getString("country")
                                + "\nState " + eachuser1.getString("state") + "\nCity " + eachuser1.getString("city")
                                + "\nYear " + String.valueOf(eachuser1.getInt("year")));
                        lastIDinBismarck = eachuser1.getInt("id");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
            return nameDb1;

        }
    }


    public void doAsync2(SQLiteDatabase nameDb, ArrayList<JSONObject> jsonUser, ArrayAdapter adap) {

        new SampleTask2(nameDb,jsonUser,adap).execute();


    }

    class SampleTask2 extends AsyncTask<Void, Void, SQLiteDatabase> {
        Boolean loadFromSQl = false;

        ArrayList<JSONObject> jsonUser2;
        SQLiteDatabase nameDb1;
        ArrayAdapter adap;

        public SampleTask2(SQLiteDatabase nameDb1,ArrayList<JSONObject> jsonUser2, ArrayAdapter adap){
            this.jsonUser2 =  jsonUser2 ;
            this.nameDb1 = nameDb1;
            this.adap = adap;
        }






        protected void onPostExecute(SQLiteDatabase nameDb1 ) {
            nameDb1.close();
            if(loadFromSQl == true){
                additemsFromSQl();
                return;
            }


            for(JSONObject eachuser1:jsonUser2){
                try {
                    adap.add("Name " + eachuser1.getString("nickname") + "\nCountry " + eachuser1.getString("country")
                            + "\nState " + eachuser1.getString("state") + "\nCity " + eachuser1.getString("city")
                            + "\nYear " + String.valueOf(eachuser1.getInt("year")));

                    adap.setNotifyOnChange(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            flag_loading = false;
            search.setEnabled(true);




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





                try {
                    lastIDinBismarck = eachuser1.getInt("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ContentValues contentValues = new ContentValues();
                try {
                    contentValues.put("_ID", eachuser1.getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    contentValues.put("NAME", eachuser1.getString("nickname"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    contentValues.put("CITY", eachuser1.getString("city"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    contentValues.put("STATE", eachuser1.getString("state"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    contentValues.put("YEAR", eachuser1.getInt("year"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if(!(eachuser1.getDouble("latitude") ==0.0 && eachuser1.getDouble("longitude") == 0.0)){
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
                        contentValues.put("LAT", la);
                        contentValues.put("LONG", lng);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    contentValues.put("COUNTRY", eachuser1.getString("country"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                nameDb1.insertWithOnConflict("usersInDb", null, contentValues,nameDb1.CONFLICT_IGNORE);


            }
            return nameDb1;

        }
    }


    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.search){
            Log.i("rew", "user names"+usernames);
            usernames.clear();


            //check the maxID in database and MaxID in Bismarck. If same then load from database
            //If not same then refresh the database and load data from server


            //namesHelper = (new DatabaseHelper(getContext()));
            //namesHelper.clearTheDb();

            getTheDetails();

        }

    }



}
