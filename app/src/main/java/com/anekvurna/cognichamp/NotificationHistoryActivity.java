package com.anekvurna.cognichamp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.anekvurna.cognichamp.SanathUtilities.setProgressBar;

public class NotificationHistoryActivity extends DrawerActivity {
    private List<String> listTexts;
    private ListView listView;
    private ArrayAdapter adapter;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);
        setTitle("Notifications history");
       /* FindCallback first = new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                parseObjects = objects;
                initializeListView();
            }
        };
        getNotifications(first);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("NEW");
        registerReceiver(broadcastReceiver, intentFilter)*/

        initializeListView();
        getNotifications();
    }

    void getNotifications()
    {
        setProgressBar(this, false, "Fetching...");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customerMessages").child(currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setProgressBar(NotificationHistoryActivity.this, false, "Loading...");
                listTexts.clear();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    String message = dataSnapshot1.getValue(String.class);
                    showToast("message: " + message);
                    listTexts.add(message);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*ParseQuery query = new ParseQuery("NotificationThree");
        query.orderByDescending("date");
        query.fromLocalDatastore();
        query.findInBackground(callback);*/
    }



    /*BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null) return;
            FindCallback second = new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    parseObjects = objects;
                    putData();
                    adapter.notifyDataSetChanged();
                }
            };
            getNotifications(second);
        }
    };*/

    void initializeListView()
    {
        listView = (ListView)findViewById(R.id.notifs_list);
        listTexts = new ArrayList<>();
        listTexts.add("Fetching lists...");
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_view_layout, listTexts);
        listView.setAdapter(adapter);
        listView.setClickable(false);
    }

    /*void putData()
    {
        listTexts.clear();
        for(ParseObject object : parseObjects){
            String text = object.getString("message");
            listTexts.add(text);
        }
    }*/

    void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
