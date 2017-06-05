package com.example.sushanth.identifyuser;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class Countryfragment extends Fragment {
    JSONArray data = new JSONArray();
    ListView countryList;
    Button countryCancel;
    public static String CountrySelected, StateSelected;

    ArrayList<String> countries = new ArrayList<String>();
    ArrayList<String> states = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    public Countryfragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_countryfragment, container, false);
        countryList = (ListView) v.findViewById(R.id.countrylist);
        countryCancel = (Button) v.findViewById(R.id.countrycancel);

        start();

        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        countryCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });




    }
    public void start() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                data = response;
                int lengthOfCountries = data.length();
                Log.d("rew", response.toString()+ lengthOfCountries);
                for (int i=0;i<lengthOfCountries; i++){
                    try{
                        countries.add(data.getString(i));
                        //Log.d("rew", data.getString(i));
                        adapter =
                                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, countries);
                        countryList.setAdapter(adapter);

                        countryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                CountrySelected = parent.getItemAtPosition(position).toString();
                                Log.i("rew", parent.getItemAtPosition(position).toString());

                                getStateInformation();

                            }});

                    }catch(JSONException e){
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
        String url ="http://bismarck.sdsu.edu/hometown/countries";
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        VolleyQueue.instance(getActivity()).add(getRequest);
    }




    public void getStateInformation(){

        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                data = response;
                int lengthOfStates = data.length();
                Log.d("rew", response.toString()+ lengthOfStates);
                for (int i=0;i<lengthOfStates; i++){
                    try{
                        states.add(data.getString(i));
                        //Log.d("rew", data.getString(i));
                        adapter =
                                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, states);
                        countryList.setAdapter(adapter);

                        countryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                StateSelected = parent.getItemAtPosition(position).toString();
                                returnCountryAndState sendData = (returnCountryAndState) getActivity();
                                sendData.printCountryAndState(CountrySelected, StateSelected);
                                Log.i("rew", parent.getItemAtPosition(position).toString());
                                getFragmentManager().popBackStack();

                            }});
                    }catch(JSONException e){
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
        String url ="http://bismarck.sdsu.edu/hometown/states?country="+CountrySelected;
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        VolleyQueue.instance(getActivity()).add(getRequest);


    }

    public interface returnCountryAndState{
        public void printCountryAndState(String country, String state);
    }
}
