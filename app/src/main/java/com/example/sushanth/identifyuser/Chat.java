package com.example.sushanth.identifyuser;


import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Chat extends AppCompatActivity implements MessageDataSource.MessagesCallbacks{

    String loggedinUser;
    FirebaseAuth authenticationInstance;
    String recipient,ConvoId,howDidyouEnter;
    ArrayList<String> alreadyselecteduser = new ArrayList<String>();
    ListView listView;
    ArrayList<Message> messages;
    MessagesAdapter adapter;
    TextView displayrecipient, presntuser;
    Button sendButton, logout;
    EditText inputMessageView;
    MessageDataSource.MessagesListener listener;
    ListView chatListView;
    ArrayList<String> firebaseUsers = new ArrayList<String>();
    FirebaseDatabase firebaseDataBase;
    ArrayAdapter<String> chatListadapter;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        recipient = getIntent().getStringExtra("clickeduser");
        howDidyouEnter = getIntent().getStringExtra("onClickofButton");
        logout = (Button) findViewById(R.id.chat_logout);
        mAuth = FirebaseAuth.getInstance();
        String currentUserLoggedIn = mAuth.getCurrentUser().getDisplayName();
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent go = new Intent(getBaseContext(), Main2Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);;

                finish();
                go.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                go.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(go);
                Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_LONG ).show();
            }
        });
        presntuser = (TextView) findViewById(R.id.chat_currentuser);
        authenticationInstance = FirebaseAuth.getInstance();
        loggedinUser = authenticationInstance.getCurrentUser().getDisplayName();
        presntuser.setText(loggedinUser);
        displayrecipient = (TextView) findViewById(R.id.chat_recipientname);
        chatListView = (ListView)findViewById(R.id.chat_listofalluser);
        inputMessageView = (EditText) findViewById(R.id.chat_entermessage);
        if(howDidyouEnter == null){
            howDidyouEnter = "maporuserclick";
        }
        firebaseDataBase = FirebaseDatabase.getInstance();
        DatabaseReference firebaseDatabaseReference = firebaseDataBase.getReference();

        firebaseDatabaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot child : snapshot.getChildren()){
                    firebaseUsers.add(child.getKey());
                    //Toast.makeText(getBaseContext(),"Users are " + child.getKey(),Toast.LENGTH_SHORT).show();

                }
                firebaseUsers.remove(loggedinUser);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        chatListadapter = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_list_item_1,firebaseUsers);
        chatListView.setAdapter(chatListadapter);

        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String clickedName = parent.getItemAtPosition(position).toString();
                recipient = clickedName;
                chatListShow();

            }
        });

        if (!(howDidyouEnter.equalsIgnoreCase("onClickofButton"))){

            chatListShow();
        }
        else{
            displayrecipient.setText("Choose recipient first");
        }

    }

    public void chatListShow() {


        displayrecipient.setText(recipient);
        listView = (ListView) findViewById(R.id.chat_history);
        messages = new ArrayList<>();
        adapter = new MessagesAdapter(messages);
        adapter.clear();
        listView.setAdapter(adapter);
        String[] ids = {loggedinUser, recipient};
        Arrays.sort(ids);
        ConvoId = ids[0] + "-" + ids[1];
        listener = MessageDataSource.addMessagesListener(ConvoId, this);
        sendButton = (Button) findViewById(R.id.chat_send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });

    }
    public void sendMessage(View v){
        String message = inputMessageView.getText().toString();
        inputMessageView.setText("");
        Message msg = new Message();
        msg.setDate(new Date());
        msg.setText(message);
        msg.setSender(loggedinUser);
        MessageDataSource.saveMessage(msg, ConvoId);


    }

    @Override
    public void onMessageAdded(Message message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scrollMyListViewToBottom();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recipient = firebaseUsers.get(0);
        chatListShow();
        MessageDataSource.stop(listener);
    }

    private class MessagesAdapter extends ArrayAdapter<Message> {
        MessagesAdapter(ArrayList<Message> messages){
            super(Chat.this, R.layout.message, R.id.message, messages);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            Message message = getItem(position);

            TextView nameView = (TextView)convertView.findViewById(R.id.message);
            nameView.setText(message.getText());

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)nameView.getLayoutParams();

            int sdk = Build.VERSION.SDK_INT;
            if (message.getSender().equals(loggedinUser)){
                if (sdk >= Build.VERSION_CODES.JELLY_BEAN) {
                    nameView.setBackground(getDrawable(R.drawable.bubble_right_green));
                } else{
                    nameView.setBackgroundDrawable(getDrawable(R.drawable.bubble_right_green));
                }
                layoutParams.gravity = Gravity.RIGHT;
            }else{
                if (sdk >= Build.VERSION_CODES.JELLY_BEAN) {
                    nameView.setBackground(getDrawable(R.drawable.bubble_left_gray));
                } else{
                    nameView.setBackgroundDrawable(getDrawable(R.drawable.bubble_left_gray));
                }
                layoutParams.gravity = Gravity.LEFT;
            }

            nameView.setLayoutParams(layoutParams);


            return convertView;
        }

    }

    private void scrollMyListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(adapter.getCount() - 1);
            }
        });
    }

}
