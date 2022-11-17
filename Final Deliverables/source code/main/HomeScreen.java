package bigbrother.child_monitoring_system;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;



public class HomeScreen extends AppCompatActivity implements View.OnClickListener {

    private Button buttonProfile;
    private Button buttonSearch;
    private Button buttonMap;
    private Button buttonRoster;
    private String uid;
    private Button buttonNotify;

    //menu test//
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBar actionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle = "Home";
    private DatabaseReference dRef;
    private User currentUser;
    private UserType userType;
    String server_url = "http://127.0.0.1/fcm/fcm_insert.php";
    private static final int GALLERY_INTENT = 2;
//    private Button testMessagebtn;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    ////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_home_screen);
//        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        setSupportActionBar(myToolbar);

        buttonProfile = (Button) findViewById(R.id.profile);
        buttonSearch = (Button) findViewById(R.id.search);
        buttonMap = (Button) findViewById(R.id.map);
//        testMessagebtn = (Button) findViewById(R.id.testMessage);
        buttonRoster = (Button) findViewById(R.id.Roster);
        buttonNotify = (Button) findViewById(R.id.notify);


        buttonProfile.setOnClickListener(this);
        buttonSearch.setOnClickListener(this);
        buttonMap.setOnClickListener(this);
//        testMessagebtn.setOnClickListener(this);
        buttonRoster.setOnClickListener(this);
        buttonNotify.setOnClickListener(this);

        dRef = FirebaseDatabase.getInstance().getReference().child("users");
        uid = getIntent().getStringExtra("uid");
        //can make a listener for dRef.child(uid) instead of looping through them all
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(uid)) {
                        currentUser = snapshot.getValue(User.class);
                        if (!currentUser.getType().equals(UserType.ADMIN)) {
                            buttonSearch.setVisibility(View.INVISIBLE);
                            buttonNotify.setVisibility(View.INVISIBLE);
                        }
                        if(!currentUser.getType().equals(UserType.EMPLOYEE)) {
                            buttonRoster.setVisibility(View.GONE);
                        }
                        if(currentUser.getType().equals(UserType.EMPLOYEE)) {
                            PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) buttonRoster.getLayoutParams();
                            PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();
                            percentLayoutInfo.topMarginPercent = 5 * 0.01f; //15 is the percentage value you want to set it to
                            buttonRoster.setLayoutParams(layoutParams);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        //menu test//
        mDrawerList = (ListView) findViewById(R.id.navList);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        getSupportActionBar().setTitle(mActivityTitle);
        addDrawerItems();
        setupDrawer();
        ////////////
//        CalligraphyConfig.initDefault(
//                new CalligraphyConfig.Builder().setDefaultFontPath("minyna.ttf").setFontAttrId(R.attr.fontPath)
//                        .build());
//        AndroidThreeTen.init(this);
        FirebaseApp.initializeApp(this);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onClick(View v) {
        if (v == buttonProfile) {
            final Intent welcomeScreenIntent = new Intent(this, Profile.class);
            welcomeScreenIntent.putExtra("uid", uid);
            startActivity(welcomeScreenIntent);
        } else if (v == buttonSearch) {
            final Intent searchScreenIntent = new Intent(this, SearchScreen.class);
            searchScreenIntent.putExtra("uid", uid);
            startActivity(searchScreenIntent);
        } else if (v == buttonMap) {
            final Intent mapScreenIntent = new Intent(this, Map.class);
            mapScreenIntent.putExtra("uid", uid);
            startActivity(mapScreenIntent);
//        } else if (v == testMessagebtn) {
//            ArrayList<String> tokens = new ArrayList<>();
//            String token = FirebaseInstanceId.getInstance().getToken();
//            tokens.add(token);
//            Message mesg = new Message(tokens, "my title", "my message body");
//            DatabaseReference mesgRef = FirebaseDatabase.getInstance().getReference().child("messages");
//            mesgRef.push().setValue(mesg);
//        }
        } else if (v == buttonNotify) {
            final Intent adminNotifyIntent = new Intent(this, AdminNotification.class);
            adminNotifyIntent.putExtra("uid", uid);
            startActivity(adminNotifyIntent);
        } else if (v == buttonRoster) {
            final Intent rosterIntent = new Intent(this, Roster.class);

            // test to see if this button works
            Log.d("HOMESCREEN","THE ROSTER BUTTON WAS CLICKED");

            rosterIntent.putExtra("uid", uid);
            startActivity(rosterIntent);
        }
    }


    //menu test//
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
                        } else if (curUser.getType().equals(UserType.EMPLOYEE)) {
                            menuArr = getResources().getStringArray(R.array.employee_menu);
                        }
                    }
                }
                mAdapter = new ArrayAdapter<String>(HomeScreen.this, R.layout.menu_item, menuArr);
                mDrawerList.setAdapter(mAdapter);
                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (userType.equals(UserType.PARENT)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(HomeScreen.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(HomeScreen.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent profileScreenIntent = new Intent(HomeScreen.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 3) {
                                final Intent errorReportScreenIntent = new Intent(HomeScreen.this, ReportError.class);
                                errorReportScreenIntent.putExtra("uid", uid);
                                startActivity(errorReportScreenIntent);
                            } else if (position == 4) {
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(HomeScreen.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        } else if (userType.equals(UserType.ADMIN)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(HomeScreen.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(HomeScreen.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent searchScreenIntent = new Intent(HomeScreen.this, SearchScreen.class);
                                searchScreenIntent.putExtra("uid", uid);
                                startActivity(searchScreenIntent);
                            } else if (position == 3) {
                                final Intent profileScreenIntent = new Intent(HomeScreen.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4){
                                final Intent adminScreenIntent = new Intent(HomeScreen.this, AdminNotification.class);
                                adminScreenIntent.putExtra("uid", uid);
                                startActivity(adminScreenIntent);
                            } else if (position == 6){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(HomeScreen.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            } else if (position == 7){
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(intent, GALLERY_INTENT);
                            }
                        } else if (userType.equals(UserType.EMPLOYEE)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(HomeScreen.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(HomeScreen.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 3) {
                                final Intent profileScreenIntent = new Intent(HomeScreen.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4) {
                                final Intent errorReportScreenIntent = new Intent(HomeScreen.this, ReportError.class);
                                errorReportScreenIntent.putExtra("uid", uid);
                                startActivity(errorReportScreenIntent);
                            } else if (position == 5){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(HomeScreen.this, LoginActivity.class);
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
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("HomeScreen Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
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
                    Toast.makeText(HomeScreen.this, "Image Uploaded", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    ////////////
}