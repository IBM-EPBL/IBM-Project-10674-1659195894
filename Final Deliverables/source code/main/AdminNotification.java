package bigbrother.child_monitoring_system;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminNotification extends AppCompatActivity implements View.OnClickListener {
    private Button buttonSend;
    private EditText messageText;
    private EditText messageSubject;
    private String uid;
    private Spinner toSpinner;
    private DatabaseReference dRef;


    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBar actionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle = "Send Notificaton";

    private ArrayList<String> parents = new ArrayList<>();
    private ArrayList<String> employees = new ArrayList<>();
    private ArrayList<String> admins = new ArrayList<>();
    private ArrayList<String> all = new ArrayList<>();
    private String targetGroup;
    private static final int GALLERY_INTENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_admin_notification);


        toSpinner = (Spinner) findViewById(R.id.toField);

        buttonSend = (Button) findViewById(R.id.sendButton);
        buttonSend.setOnClickListener(this);
        dRef = FirebaseDatabase.getInstance().getReference().child("users");
        uid = getIntent().getStringExtra("uid");
        messageText = (EditText) findViewById(R.id.messageText);
        messageSubject = (EditText) findViewById(R.id.messageSubject);


        mDrawerList = (ListView) findViewById(R.id.navList);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        actionBar.setTitle(mActivityTitle);

        addDrawerItems();
        setupDrawer();
        populateToSpinner();
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                targetGroup = parent.getItemAtPosition(pos).toString();
//                Toast.makeText(AdminNotification.this, "TargetGroup: " + targetGroup, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        populateUserLists();
    }


    @Override
    public void onClick(View v) {
        if (v == buttonSend) {
            final String message = messageText.getText().toString().trim();
            final String subject = messageSubject.getText().toString().trim();
            boolean error = false;
            if  (TextUtils.isEmpty(subject)) {
                messageSubject.setError("Please provide a message subject");
                error = true;
            }
            if (TextUtils.isEmpty(message)) {
                messageText.setError("Please provide a message to send");
                error = true;
            }
            if (error) {
                return;
            }
            Message mesg;
//            Toast.makeText(AdminNotification.this, "TargetGroup: " + targetGroup, Toast.LENGTH_SHORT).show();
            switch (targetGroup) {
                case ("All") :
//                    Toast.makeText(AdminNotification.this, "All case", Toast.LENGTH_SHORT).show();
                    mesg = new Message(all, subject, message);
                    break;
                case ("Parents") :
//                    Toast.makeText(AdminNotification.this, "Parents case", Toast.LENGTH_SHORT).show();
                    mesg = new Message(parents, subject, message);
                    break;
                case ("Employees") :
//                    Toast.makeText(AdminNotification.this, "Employee case", Toast.LENGTH_SHORT).show();
                    mesg = new Message(employees, subject, message);
                    break;
                case ("Administrators") :
//                    Toast.makeText(AdminNotification.this, "Admin case", Toast.LENGTH_SHORT).show();
                    mesg = new Message(admins, subject, message);
                    break;
                default:
//                    Toast.makeText(AdminNotification.this, "Default case", Toast.LENGTH_SHORT).show();
                    mesg = new Message(all, subject, message);
                    break;
            }
            DatabaseReference mesgRef = FirebaseDatabase.getInstance().getReference().child("messages");
            mesgRef.push().setValue(mesg);

        }
    }

    private void addDrawerItems() {
        String[] menuArr = getResources().getStringArray(R.array.admin_menu);

        mAdapter = new ArrayAdapter<String>(AdminNotification.this, R.layout.menu_item, menuArr);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    final Intent homeScreenIntent = new Intent(AdminNotification.this, HomeScreen.class);
                    homeScreenIntent.putExtra("uid", uid);
                    startActivity(homeScreenIntent);
                } else if (position == 1) {
                    final Intent mapScreenIntent = new Intent(AdminNotification.this, Map.class);
                    mapScreenIntent.putExtra("uid", uid);
                    startActivity(mapScreenIntent);
                } else if (position == 2) {
                    final Intent searchScreenIntent = new Intent(AdminNotification.this, SearchScreen.class);
                    searchScreenIntent.putExtra("uid", uid);
                    startActivity(searchScreenIntent);
                } else if (position == 3){
                    final Intent profileScreenIntent = new Intent(AdminNotification.this, Profile.class);
                    profileScreenIntent.putExtra("uid", uid);
                    startActivity(profileScreenIntent);
                } else if (position == 4){
                    final Intent adminScreenIntent = new Intent(AdminNotification.this, AdminNotification.class);
                    adminScreenIntent.putExtra("uid", uid);
                    startActivity(adminScreenIntent);
                } else if (position == 6){
                    FirebaseAuth.getInstance().signOut();
                    final Intent loginScreenIntent = new Intent(AdminNotification.this, LoginActivity.class);
                    startActivity(loginScreenIntent);
                } else if (position == 7){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_INTENT);
                }
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

    private void populateUserLists() {
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
//                    Toast.makeText(AdminNotification.this, "Value of user: " + user.getFirstName() + "'s token: " + user.getToken(), Toast.LENGTH_LONG).show();
                    if (user.getType() != null) {
                        if (user.getType() == UserType.ADMIN) {
                            admins.add(user.getToken());
                        } else if (user.getType() == UserType.PARENT) {
                            parents.add(user.getToken());
                        } else if (user.getType() == UserType.EMPLOYEE) {
                            employees.add(user.getToken());
                        }
                        all.add(user.getToken());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    private void populateToSpinner() {
        List<String> list = new ArrayList<>();
        list.add("All");
        list.add("Parents");
        list.add("Employees");
        list.add("Administrators");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(AdminNotification.this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(dataAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && resultCode ==RESULT_OK) {
            Uri uri = data.getData();
            StorageReference storage = FirebaseStorage.getInstance().getReference();
            StorageReference filepath = storage.child("Daycare").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(AdminNotification.this, "Image Uploaded", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}