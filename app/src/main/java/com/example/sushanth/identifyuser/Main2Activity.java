package com.example.sushanth.identifyuser;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener  {
    private FirebaseAuth mAuth;

    EditText emailEntry, passwordEntry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        emailEntry = (EditText) findViewById(R.id.emailEntry);
        passwordEntry = (EditText) findViewById(R.id.passwordEntry);
        mAuth = FirebaseAuth.getInstance();
        Button reg = (Button) findViewById(R.id.reg);
        Button sign = (Button) findViewById(R.id.sign);
        reg.setOnClickListener(this);
        sign.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.reg){
            Intent go1 = new Intent(this, RegisterUser.class);
            startActivity(go1);

        }
        if(v.getId() == R.id.sign){
            //check for valid email and password inputs
            if(emailEntry.getText().toString().trim().equals("")){
                Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT ).show();
                return;
            }else if (!isEmailValid(emailEntry.getText().toString().trim())){
                Toast.makeText(this, "Enter Valid Email", Toast.LENGTH_SHORT ).show();
                return;
            }
            else if(passwordEntry.getText().toString().trim().equals("")){
                Toast.makeText(this, "Enter Passowrd", Toast.LENGTH_SHORT ).show();
                return;
            }

            //check for authentication
            String email = emailEntry.getText().toString().trim();
            String password = passwordEntry.getText().toString().trim();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(Main2Activity.this, new OnCompleteListener<AuthResult>(){
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful())
                        Toast.makeText(Main2Activity.this, "Wrong Username or Password", Toast.LENGTH_SHORT).show();
                    else
                    {
//                        Toast.makeText(Main2Activity.this, "Welcome", Toast.LENGTH_SHORT).show();
                        Intent loggedIn = new Intent(Main2Activity.this, Menu.class);
                        startActivity(loggedIn);
                        finish();
                    }
                }
            });




        }
    }


    public boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}


