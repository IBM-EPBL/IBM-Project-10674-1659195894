package bigbrother.child_monitoring_system;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Map extends AppCompatActivity implements MapInputDialog.OnCompleteListener, FloorView.onCircleCreateInterface {

    private ImageView floorPlanIV;
    private Drawable floorplan;
    private StorageReference storage;
    private FloorView floorView;
    private Button uploadImageButton;
    private static final int GALLERY_INTENT = 2;

    public static HashMap<String, RoomData> rooms = new HashMap<>();
    public static List<ChildDataObject> children = Collections.synchronizedList(new ArrayList<ChildDataObject>());

    private DatabaseReference db;
    private DatabaseReference dbChildren;

    private String currentRoomName, currentPiMAC;

    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBar actionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle = "Map";
    private UserType userType;
    private DatabaseReference dRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create the database reference
        db = FirebaseDatabase.getInstance().getReference().child("daycare").child("Georgia Tech").child("mapdata");
        dbChildren = FirebaseDatabase.getInstance().getReference().child("daycare").child("Georgia Tech").child("children");
        dRef = FirebaseDatabase.getInstance().getReference().child("users");
        uid = getIntent().getStringExtra("uid");
        //load the rooms for the FloorView
        onLoad();


//        final ProgressDialog dialog = new ProgressDialog(Map.this);
//        dialog.setTitle("Loading map...");
//        dialog.setMessage("Please wait.");
//        dialog.setIndeterminate(true);
//        dialog.setCancelable(false);
//        dialog.show();


        setContentView(R.layout.activity_map);

        floorPlanIV = (ImageView) findViewById(R.id.floorPlanIV);
        //uploadImageButton = (Button) findViewById(R.id.uploadImageButton);

//        uploadImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
//                startActivityForResult(intent, GALLERY_INTENT);
//            }
//        });
        storage = FirebaseStorage.getInstance().getReference();

        //set the interface for listening to circle creation
        floorView = (FloorView) findViewById(R.id.floorView);
        floorView.setViewListener(this);

        storage.child("Daycare/daycare2.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(Map.this)
                        .load(uri)
                        .into(floorPlanIV);
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("download failed");
            }
        });

        mDrawerList = (ListView) findViewById(R.id.navList);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        getSupportActionBar().setTitle(mActivityTitle);
        addDrawerItems();
        setupDrawer();

        dRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userType = dataSnapshot.getValue(User.class).getType();
                //set the UserType
                floorView.setUserType(userType);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onLoad() {

        //for the children at that particular school
        dbChildren.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                children.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    java.util.Map<?,?> child = (java.util.Map<?,?>) snapshot.getValue();

                    if(child.get("name") == null
                            || child.get("macAddress") == null
                            || child.get("locationMAC") == null
                            || child.get("timestamp") == null)
                        continue;

                    ChildDataObject ch = new ChildDataObject(child.get("name").toString(),
                            child.get("macAddress").toString(), child.get("locationMAC").toString(),
                            child.get("timestamp").toString());
                    addChildToList(ch);
                }
                floorView.invalidate();
                Log.d("MAP-CLASS", "Grabbed the children data!!!");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //for the rooms of the particular school
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rooms.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RoomData room = snapshot.getValue(RoomData.class);
                    Log.d("MAPCLASS","THE ROOM WAS LOADED AS " + room.getMac());
                    addRoomToList(room);
                }
                floorView.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //method to add a child to the static arraylist
    public void addChildToList(ChildDataObject child) {
        this.children.add(child);
    }

    // method to call to inner class for adding rooms to pass to view
    public void addRoomToList(RoomData room) {
        this.rooms.put(room.getMac(), room);
    }

    @Override
    public void onComplete(String name, String mac) {
        floorView.onComplete(name, mac);
    }


    @Override
    public void onCircleCreate() {
        showMapDialog();
    }

    @Override
    public void onCircleCreate(String mac, int radius, int xCor, int yCor) {
        //method for getting this info and putting it inside the database so that
        //the teachers and parents can retrieve this information to make their own map
//a        Log.d("MAPCLASS", mac);


        // check if there is a child node of name "mapdata" and create if not
        java.util.Map<String, Object> mapdata = new HashMap<>();
        mapdata.put(mac, new RoomData(mac, xCor, yCor, radius));
        db.updateChildren(mapdata);

    }

    @Override
    public void onCircleDelete(String mac) {
        db.child(mac).removeValue();
    }

    private void showMapDialog() {
        FragmentManager fm = getSupportFragmentManager();
        MapInputDialog mapInputDialog = MapInputDialog.newInstance();
        mapInputDialog.show(fm, "child_input_fragment");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && resultCode ==RESULT_OK) {
            Uri uri = data.getData();

            StorageReference filepath = storage.child("Daycare").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(Map.this, "Image Uploaded", Toast.LENGTH_LONG).show();
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
                mAdapter = new ArrayAdapter<String>(Map.this, R.layout.menu_item, menuArr);
                mDrawerList.setAdapter(mAdapter);
                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (userType.equals(UserType.PARENT)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(Map.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(Map.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent profileScreenIntent = new Intent(Map.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 3) {
                                final Intent errorReportScreenIntent = new Intent(Map.this, ReportError.class);
                                errorReportScreenIntent.putExtra("uid", uid);
                                startActivity(errorReportScreenIntent);
                            } else if (position == 4){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(Map.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        } else if (userType.equals(UserType.ADMIN)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(Map.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(Map.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent searchScreenIntent = new Intent(Map.this, SearchScreen.class);
                                searchScreenIntent.putExtra("uid", uid);
                                startActivity(searchScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(Map.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4){
                                final Intent adminScreenIntent = new Intent(Map.this, AdminNotification.class);
                                adminScreenIntent.putExtra("uid", uid);
                                startActivity(adminScreenIntent);
                            } else if (position == 6){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(Map.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            } else if (position == 7){
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(intent, GALLERY_INTENT);
                            }
                        } else if (userType.equals(UserType.EMPLOYEE)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(Map.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(Map.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(Map.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4) {
                                final Intent errorReportScreenIntent = new Intent(Map.this, ReportError.class);
                                errorReportScreenIntent.putExtra("uid", uid);
                                startActivity(errorReportScreenIntent);
                            } else if (position == 5){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(Map.this, LoginActivity.class);
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