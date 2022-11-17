package bigbrother.child_monitoring_system;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchScreen extends AppCompatActivity implements View.OnClickListener {

    private Button buttonSearch;
    private EditText searchName;
    private String uid;
    private ListView usersList;
    private DatabaseReference dRef;
    private HashMap<Integer, String> uidListView;
    private User user;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBar actionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle = "Search Users";
    private UserType userType;
    private static final int GALLERY_INTENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_search_screen);

        getSupportActionBar().setTitle("Search Users");

        buttonSearch = (Button) findViewById(R.id.sendButton);
        buttonSearch.setOnClickListener(this);
        searchName = (EditText) findViewById(R.id.searchName);
        uidListView = new HashMap<Integer, String>();

        usersList = (ListView)findViewById(R.id.usersList);
        dRef = FirebaseDatabase.getInstance().getReference().child("users");
        uid = getIntent().getStringExtra("uid");

        mDrawerList = (ListView)findViewById(R.id.navList);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        getSupportActionBar().setTitle(mActivityTitle);
        addDrawerItems();
        setupDrawer();
    }


    @Override
    public void onClick(View v) {
        if (v == buttonSearch) {
            final String nameEntered = searchName.getText().toString().trim();
            if (TextUtils.isEmpty(nameEntered)) {
                searchName.setError("Please provide the name of the user to search for");
                return;
            }

            dRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<HashMap<String,String>> list =  new ArrayList<HashMap<String, String>>();
                    String[] firstLastName = nameEntered.split("\\s+");
                    String firstName = firstLastName[0];
                    String lastName = "";
                    if (firstLastName.length > 1) {
                        lastName = firstLastName[1];
                    }
                    int index = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        HashMap<String,String> hashMap = new HashMap<String, String>();
                        if (firstLastName.length > 1 && user.getFirstName().equalsIgnoreCase(firstName)
                                && user.getLastName().equalsIgnoreCase(lastName)) {
                            hashMap.put("User",user.getFirstName() + " " + user.getLastName());
                            hashMap.put("UserType", user.getType().toString());
                            hashMap.put("Access", "Access");
                            list.add(hashMap);
                            uidListView.put(index++, snapshot.getKey());
                        } else if (firstLastName.length == 1
                                && (user.getFirstName().equalsIgnoreCase(firstName)
                                || user.getLastName().equalsIgnoreCase(firstName))) {
                            hashMap.put("User",user.getFirstName() + " " + user.getLastName());
                            hashMap.put("UserType", user.getType().toString());
                            hashMap.put("Access", "Access");
                            list.add(hashMap);
                            uidListView.put(index++, snapshot.getKey());
                        }

                    }
                    if (list.isEmpty()) {
                        HashMap<String,String> hashMap = new HashMap<String, String>();
                        hashMap.put("User", "No Results");
                        list.add(hashMap);
                    }


                    CustomSearchAdapter adapter = new CustomSearchAdapter(SearchScreen.this, list, R.layout.search_list_item, new String[] {"User", "UserType", "Access"}, new int[] {R.id.textTop, R.id.textBottom, R.id.switchAccess}, uidListView);
                    usersList.setAdapter(adapter);
                    usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            final Intent userOptionsScreen = new Intent(SearchScreen.this, UserOptionsScreen.class);
                            userOptionsScreen.putExtra("uid", uidListView.get(position));
                            userOptionsScreen.putExtra("adminUid", uid);
                            startActivity(userOptionsScreen);
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });


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
                mAdapter = new ArrayAdapter<String>(SearchScreen.this, R.layout.menu_item, menuArr);
                mDrawerList.setAdapter(mAdapter);
                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (userType.equals(UserType.PARENT)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(SearchScreen.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(SearchScreen.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent profileScreenIntent = new Intent(SearchScreen.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 3) {
                                final Intent errorReportScreenIntent = new Intent(SearchScreen.this, ReportError.class);
                                errorReportScreenIntent.putExtra("uid", uid);
                                startActivity(errorReportScreenIntent);
                            } else if (position == 4){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(SearchScreen.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        } else if (userType.equals(UserType.ADMIN)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(SearchScreen.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(SearchScreen.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent searchScreenIntent = new Intent(SearchScreen.this, SearchScreen.class);
                                searchScreenIntent.putExtra("uid", uid);
                                startActivity(searchScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(SearchScreen.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4){
                                final Intent adminScreenIntent = new Intent(SearchScreen.this, AdminNotification.class);
                                adminScreenIntent.putExtra("uid", uid);
                                startActivity(adminScreenIntent);
                            } else if (position == 6){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(SearchScreen.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            } else if (position == 7){
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(intent, GALLERY_INTENT);
                            }
                        } else if (userType.equals(UserType.EMPLOYEE)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(SearchScreen.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(SearchScreen.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(SearchScreen.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4) {
                                final Intent errorReportScreenIntent = new Intent(SearchScreen.this, ReportError.class);
                                errorReportScreenIntent.putExtra("uid", uid);
                                startActivity(errorReportScreenIntent);
                            } else if (position == 5){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(SearchScreen.this, LoginActivity.class);
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
                    Toast.makeText(SearchScreen.this, "Image Uploaded", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}