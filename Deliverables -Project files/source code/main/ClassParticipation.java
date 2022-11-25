package bigbrother.child_monitoring_system;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

public class ClassParticipation extends AppCompatActivity implements View.OnClickListener {

    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBar actionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle = "Send Notification";
    private UserType userType;
    private DatabaseReference dRef;
    private String uid;
    private String mac;
    private String childName;
    private String parentUID;
    private DatabaseReference dbChildren;
    private Button sendNote;
    private EditText titleText;
    private EditText msgText;
    private TextView parentNameText;
    private TextView childNameText;
    private User teacherUser;
    private User parentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbChildren = FirebaseDatabase.getInstance().getReference().child("daycare").child("Georgia Tech").child("children");
        dRef = FirebaseDatabase.getInstance().getReference();
        uid = getIntent().getStringExtra("uid");
        mac = getIntent().getStringExtra("mac");
        childName = getIntent().getStringExtra("childName");
        setContentView(R.layout.activity_class_participation);

        sendNote = (Button) findViewById(R.id.SendNoteButton);
        titleText = (EditText) findViewById(R.id.msg_title);
        msgText = (EditText) findViewById(R.id.message);
        parentNameText = (TextView) findViewById(R.id.parentName);
        childNameText = (TextView) findViewById(R.id.childName);
        childNameText.setText(childName);

        sendNote.setOnClickListener(this);

        dRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                teacherUser = dataSnapshot.getValue(User.class);
                dRef.child("daycare").child(teacherUser.getSchoolName()).child("children").child(mac).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("parentUID").getValue() == null) {
                            return;
                        }
                        parentUID = dataSnapshot.child("parentUID").getValue().toString();
                        dRef.child("users").child(parentUID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                parentUser = dataSnapshot.getValue(User.class);
                                parentNameText.setText(parentUser.getFirstName() + " " + parentUser.getLastName());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });

        mDrawerList = (ListView) findViewById(R.id.navList);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        getSupportActionBar().setTitle(mActivityTitle);
        addDrawerItems();
        setupDrawer();
    }

    public void onClick(View v) {
        if (v == sendNote) {
            ArrayList<String> tokens = new ArrayList<>();
            String token = parentUser.getToken(); // change to specific parent
            tokens.add(token);
            String title = titleText.getText().toString();
            String tempMsgText = msgText.getText().toString();
            if (!title.equals("") || !tempMsgText.equals("")) {
                Message mesg = new Message(tokens, title, tempMsgText);
                DatabaseReference mesgRef = FirebaseDatabase.getInstance().getReference().child("messages");
                mesgRef.push().setValue(mesg);

                /**
                 * Switch to child list screen
                 */
                final Intent rosterIntent = new Intent(ClassParticipation.this, Roster.class);
                rosterIntent.putExtra("uid", uid);
                startActivity(rosterIntent);
            } else if (title.equals("")) {
                Toast.makeText(ClassParticipation.this, "Enter a title.", Toast.LENGTH_SHORT).show();
            } else if (tempMsgText.equals("")) {
                Toast.makeText(ClassParticipation.this, "Enter a message.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addDrawerItems() {
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] menuArr = getResources().getStringArray(R.array.parent_menu);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(uid)) {
                        User curUser = snapshot.getValue(User.class);
                        userType = curUser.getType();
                        if (curUser.getType().equals(UserType.ADMIN)) {
                            menuArr = getResources().getStringArray(R.array.admin_menu);
                        } else if (curUser.getType().equals(UserType.EMPLOYEE)){
                            menuArr = getResources().getStringArray(R.array.employee_menu);
                        }
                    }
                }
                mAdapter = new ArrayAdapter<String>(ClassParticipation.this, R.layout.menu_item, menuArr);
                mDrawerList.setAdapter(mAdapter);
                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (userType.equals(UserType.PARENT)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(ClassParticipation.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(ClassParticipation.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent profileScreenIntent = new Intent(ClassParticipation.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(ClassParticipation.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        } else if (userType.equals(UserType.ADMIN)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(ClassParticipation.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(ClassParticipation.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent searchScreenIntent = new Intent(ClassParticipation.this, SearchScreen.class);
                                searchScreenIntent.putExtra("uid", uid);
                                startActivity(searchScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(ClassParticipation.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4){
                                final Intent adminScreenIntent = new Intent(ClassParticipation.this, AdminNotification.class);
                                adminScreenIntent.putExtra("uid", uid);
                                startActivity(adminScreenIntent);
                            } else if (position == 6){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(ClassParticipation.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        } else if (userType.equals(UserType.EMPLOYEE)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(ClassParticipation.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(ClassParticipation.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(ClassParticipation.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 5){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(ClassParticipation.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}