package com.example.sushanth.identifyuser;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class Menu extends AppCompatActivity implements View.OnClickListener{

    Button menu_logout, menu_listview, menu_mapview, menu_chats;
    private FirebaseAuth mAuth;
    String currentUserLoggedIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        menu_logout = (Button) findViewById(R.id.menu_logout);
        menu_listview = (Button) findViewById(R.id.menu_listview);
        menu_mapview = (Button) findViewById(R.id.menu_mapview);
        menu_chats = (Button) findViewById(R.id.menu_chats);
        menu_logout.setOnClickListener(this);
        menu_listview.setOnClickListener(this);
        menu_mapview.setOnClickListener(this);
        menu_chats.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserLoggedIn = mAuth.getCurrentUser().getDisplayName();
        Toast.makeText(this, "Welcome "+ currentUserLoggedIn, Toast.LENGTH_LONG ).show();


        FragmentManager fragments = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragments.beginTransaction();
        listView fragment = new listView();
        Bundle bdl = new Bundle();
        bdl.putString("username",currentUserLoggedIn );
        fragment.setArguments(bdl);
        fragmentTransaction.replace(R.id.menu_fragmentcontainer, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.menu_logout){
            mAuth.signOut();
            Intent go = new Intent(this, Main2Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);;

            this.finish();
            go.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            go.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(go);
            Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG ).show();

        }
        if(v.getId() == R.id.menu_listview){
            FragmentManager fragments = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragments.beginTransaction();
            listView fragment = new listView();
            Bundle bdl = new Bundle();
            bdl.putString("username",currentUserLoggedIn );
            fragment.setArguments(bdl);
            fragmentTransaction.replace(R.id.menu_fragmentcontainer, fragment);
            fragmentTransaction.commit();

        }
        if(v.getId() == R.id.menu_mapview){
            FragmentManager fragments = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragments.beginTransaction();
            mapView fragment = new mapView();
            Bundle bdl = new Bundle();
            bdl.putString("username",currentUserLoggedIn );
            fragment.setArguments(bdl);
            fragmentTransaction.replace(R.id.menu_fragmentcontainer, fragment);
            fragmentTransaction.commit();

        }
        if(v.getId() == R.id.menu_chats){
            Intent go = new Intent(this,Chat.class);
            go.putExtra("onClickofButton","onClickofButton");
            startActivity(go);

        }

    }
}
