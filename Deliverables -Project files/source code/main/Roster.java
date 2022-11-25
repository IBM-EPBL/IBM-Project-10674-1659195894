package bigbrother.child_monitoring_system;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.Collections;
import java.util.Comparator;

public class Roster extends AppCompatActivity {

    private String uid;
    private DatabaseReference dRef;
    private DatabaseReference fdbSchool;
    private DatabaseReference database;
    private DatabaseReference dbClassroom;
    private RecyclerView mRecyclerView;
    private RecyclerView teacherClassRecyclerView;
    private RecyclerView.Adapter listAdapter;
    private RecyclerView.Adapter teacherClassAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager teacherClassLayoutManager;
    private ArrayList<ChildDataObject> results = new ArrayList<ChildDataObject>();
    private ArrayList<ChildDataObject> yourClass = new ArrayList<ChildDataObject>();


    private User currUser;
    private String schoolName;



    //menu test//
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBar actionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle = "Class Roster";
    private UserType userType;
    ////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_roster);

        database = FirebaseDatabase.getInstance().getReference();
        dRef = FirebaseDatabase.getInstance().getReference().child("users");
        uid = getIntent().getStringExtra("uid");


        dRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currUser = dataSnapshot.getValue(User.class);
                schoolName = currUser.getSchoolName();
                getDataSet();
                getClassroomDataSet();
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });


        //Add loading dummy data
        results.add(new ChildDataObject("Loading",""));

        // Recycler view for the bottom screen
        mRecyclerView = (RecyclerView) findViewById(R.id.all_children_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        listAdapter = new RosterCardAdapter(results);
        mRecyclerView.setAdapter(listAdapter);
        /////////////////////////////////////////////////////////////////////////////


        // RecyclerView for the top Screen
        teacherClassRecyclerView = (RecyclerView) findViewById(R.id.class_roster_view);
        teacherClassRecyclerView.setHasFixedSize(true);
        teacherClassLayoutManager = new LinearLayoutManager(this);
        teacherClassRecyclerView.setLayoutManager(teacherClassLayoutManager);
        teacherClassAdapter = new TeacherClassCardAdapter(yourClass);
        teacherClassRecyclerView.setAdapter(teacherClassAdapter);
        ////////////////////////////////////////////////////////////////////////////


        // For the bottom screen Roster of all Children
        ((RosterCardAdapter) listAdapter).setOnItemClickListener(new RosterCardAdapter
                .MyClickListener() {

            @Override
            public void onButtonClick(int position, View v) {

                /* WHEN THE ARROW BUTTON IS CLICKED WE NEED TO MOVE THIS CHILD DATAOBJECT
                *  TO THE LIST ABOVE */


                ((RosterCardAdapter) listAdapter).moveItemToClass(position, uid, schoolName);
                notifyDatabaseChange();
            }

            @Override
            public void onItemClick(int position, View v) {
                // add in if needed later
            }
        });

        // For the top screen, Your Class
        ((TeacherClassCardAdapter) teacherClassAdapter).setOnItemClickListener(new TeacherClassCardAdapter
                .MyClickListener() {

            @Override
            public void onSwitchToggle(int position, View v, boolean checked) {
                /* When user clicks the switch to toggle absence, the update the DB */
                ((TeacherClassCardAdapter) teacherClassAdapter).setChildPresence(position, schoolName, checked);
            }

            @Override
            public void onButtonClick(int position, View v) {

                /* WHEN THE ARROW DOWN BUTTON IS CLICKED WE NEED TO REMOVE THIS CHILD FROM
                * THE DATABASE */


                ((TeacherClassCardAdapter) teacherClassAdapter).deleteItemFromClass(position, uid, schoolName);
                notifyDatabaseChange();
            }

            @Override
            public void onItemClick(int position, View v) {
                // used to go to send message screen
                final Intent messageScreenIntent = new Intent(Roster.this, ClassParticipation.class);
                messageScreenIntent.putExtra("uid", uid);
                messageScreenIntent.putExtra("mac", ((TeacherClassCardAdapter) teacherClassAdapter).getChildDataAt(position).getMacAddress());
                messageScreenIntent.putExtra("childName", ((TeacherClassCardAdapter) teacherClassAdapter).getChildDataAt(position).getName());
                startActivity(messageScreenIntent);
            }
        });





        /* MENU STUFF*/
        mDrawerList = (ListView)findViewById(R.id.navList);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        getSupportActionBar().setTitle(mActivityTitle);
        addDrawerItems();
        setupDrawer();
        /* END MENU STUFF*/

    }




    private void getDataSet() {
        // We will loop through all the children under the school and populate the list

        fdbSchool = FirebaseDatabase.getInstance().getReference().child("daycare").child(schoolName).child("children");
        fdbSchool.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                results.clear();
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    String name = (String) messageSnapshot.child("name").getValue();
                    String mac = (String) messageSnapshot.child("macAddress").getValue();
                    results.add(new ChildDataObject(name, mac));
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
    }


    private void getClassroomDataSet() {

        // Loop through all the children in the classroom to populate the classroom list
        dbClassroom = database.child("daycare").child(schoolName).child("classrooms").child(uid);
        dbClassroom.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                yourClass.clear();
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {

                    if(messageSnapshot.getKey().equals("0")){
                        continue;
                    }

                    String name = (String) messageSnapshot.getValue();
                    String mac = (String) messageSnapshot.getKey();

                    Log.d("LIST OF CHILDREN", name + "   " +  mac);

                    ChildDataObject child = new ChildDataObject(name, mac);
                    getPresence(child);

                    yourClass.add(child);
                }

                Collections.sort(yourClass, new Comparator<ChildDataObject>() {
                    @Override
                    public int compare(ChildDataObject lhs, ChildDataObject rhs) {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                });
                teacherClassAdapter.notifyDataSetChanged();



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void getPresence(final ChildDataObject child) {
        fdbSchool.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Is the child present? Set the value.
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    if(child.getMacAddress().equals((String) messageSnapshot.child("macAddress").getValue())){
                        if((Boolean) messageSnapshot.child("present").getValue() != null)
                            child.setPresent((Boolean) messageSnapshot.child("present").getValue());
                    }
                }
                teacherClassAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
    }


    public void notifyDatabaseChange() {
//        currUser.setChildren(((RosterCardAdapter) listAdapter).getChildDataset());
//        fdbUsers.child(uid).setValue(currentUser);
//        fdbSchool = FirebaseDatabase.getInstance().getReference().child("daycare").child(currentUser.getSchoolName()).child("children");
//        for (ChildDataObject c : currentUser.getChildren()) {
//            fdbSchool.child(String.valueOf(c.getMacAddress())).setValue(c);
//        }
    }









    ///////////////////////////////////////////////////////////////////////////////////////////////









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
                        } else if (curUser.getType().equals(UserType.EMPLOYEE)){
                            menuArr = getResources().getStringArray(R.array.employee_menu);
                        }
                    }
                }
                mAdapter = new ArrayAdapter<String>(Roster.this, R.layout.menu_item, menuArr);
                mDrawerList.setAdapter(mAdapter);
                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (userType.equals(UserType.PARENT)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(Roster.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(Roster.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent profileScreenIntent = new Intent(Roster.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(Roster.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        } else if (userType.equals(UserType.ADMIN)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(Roster.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(Roster.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent searchScreenIntent = new Intent(Roster.this, SearchScreen.class);
                                searchScreenIntent.putExtra("uid", uid);
                                startActivity(searchScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(Roster.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 6){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(Roster.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        } else if (userType.equals(UserType.EMPLOYEE)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(Roster.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(Roster.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(Roster.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 5){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(Roster.this, LoginActivity.class);
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
    ////////////




}