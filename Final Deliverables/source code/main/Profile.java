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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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

public class Profile extends AppCompatActivity implements View.OnClickListener {

    private String uid;
    private DatabaseReference dRef;
    private User currUser;

    private TextView firstName;
    private TextView lastName;
    private TextView email;
    //private TextView password;
    private TextView schoolName;

    private Button editButton;

    //menu test//
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBar actionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle = "Profile";
    private UserType userType;
    private static final int GALLERY_INTENT = 2;
    ////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_profile);
//        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        setSupportActionBar(myToolbar);
        firstName = (TextView) findViewById(R.id.firstNameData);
        lastName = (TextView) findViewById(R.id.lastNameData);
        email = (TextView) findViewById(R.id.emailData);
        //password = (TextView) findViewById(R.id.passwordData);
        schoolName = (TextView) findViewById(R.id.schoolNameData);

        editButton = (Button) findViewById(R.id.editButton);

        dRef = FirebaseDatabase.getInstance().getReference().child("users");
        uid = getIntent().getStringExtra("uid");


        dRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currUser = dataSnapshot.getValue(User.class);
                firstName.setText(currUser.getFirstName());
                firstName.setFocusable(false);
                lastName.setText(currUser.getLastName());
                lastName.setFocusable(false);
                email.setText(currUser.getEmail());
                //password.setText(currUser.getPassword());
                schoolName.setText(currUser.getSchoolName());
                schoolName.setFocusable(false);
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });

        editButton.setOnClickListener(this);

        //menu test//
        mDrawerList = (ListView)findViewById(R.id.navList);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        getSupportActionBar().setTitle(mActivityTitle);
        addDrawerItems();
        setupDrawer();
        ////////////
    }




    public void onClick(View v) {
        if (v == editButton && editButton.getText().equals("Edit")) {
            firstName.setFocusableInTouchMode(true);
            lastName.setFocusableInTouchMode(true);
            schoolName.setFocusableInTouchMode(true);
            firstName.setBackground(getResources().getDrawable(R.drawable.textbox));
            lastName.setBackground(getResources().getDrawable(R.drawable.textbox));
            schoolName.setBackground(getResources().getDrawable(R.drawable.textbox));
            editButton.setText("Save");
            //Toast.makeText(Profile.this, "Should be editable", Toast.LENGTH_SHORT).show();
        } else if (v == editButton && editButton.getText().equals("Save")) {
             if (!checkForm()) {
                 return;
             }
            updateFirebase();
            firstName.setFocusable(false);
            lastName.setFocusable(false);
            schoolName.setFocusable(false);
            editButton.setText("Edit");
            firstName.setBackground(getResources().getDrawable(R.drawable.empty));
            lastName.setBackground(getResources().getDrawable(R.drawable.empty));
            schoolName.setBackground(getResources().getDrawable(R.drawable.empty));
            //Toast.makeText(Profile.this, "Should not be editable. Done updating", Toast.LENGTH_SHORT).show();
        } else {

            if (currUser != null && uid != null) {
                dRef.child(uid).child(uid).setValue(currUser);
            }
        }
    }

    private boolean checkForm() {
        boolean good = true;
        if (TextUtils.isEmpty(firstName.getText())) {
            firstName.setError("First Name is required");
            good = false;
        }
        if (TextUtils.isEmpty(lastName.getText())) {
            lastName.setError("Last Name is required");
            good = false;
        }
        if (TextUtils.isEmpty(schoolName.getText())) {
            schoolName.setError("school name is required");
            good = false;
        }
        if (TextUtils.isEmpty(email.getText())) {
            email.setError( "email is required" );
            good = false;
        }
        return good;
    }

    private void updateFirebase() {
        currUser.setValues(firstName.getText().toString(), lastName.getText().toString(), schoolName.getText().toString(), email.getText().toString());
        dRef.child(uid).setValue(currUser);
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
                        } else if (curUser.getType().equals(UserType.EMPLOYEE)){
                            menuArr = getResources().getStringArray(R.array.employee_menu);
                        }
                    }
                }
                mAdapter = new ArrayAdapter<String>(Profile.this, R.layout.menu_item, menuArr);
                mDrawerList.setAdapter(mAdapter);
                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (userType.equals(UserType.PARENT)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(Profile.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(Profile.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent profileScreenIntent = new Intent(Profile.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 3) {
                                final Intent errorReportScreenIntent = new Intent(Profile.this, ReportError.class);
                                errorReportScreenIntent.putExtra("uid", uid);
                                startActivity(errorReportScreenIntent);
                            } else if (position == 4){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(Profile.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        } else if (userType.equals(UserType.ADMIN)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(Profile.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(Profile.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent searchScreenIntent = new Intent(Profile.this, SearchScreen.class);
                                searchScreenIntent.putExtra("uid", uid);
                                startActivity(searchScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(Profile.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 6){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(Profile.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            } else if (position == 4){
                                final Intent adminScreenIntent = new Intent(Profile.this, AdminNotification.class);
                                adminScreenIntent.putExtra("uid", uid);
                                startActivity(adminScreenIntent);
                            } else if (position == 7){
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(intent, GALLERY_INTENT);
                            }
                        } else if (userType.equals(UserType.EMPLOYEE)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(Profile.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(Profile.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(Profile.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4) {
                                final Intent errorReportScreenIntent = new Intent(Profile.this, ReportError.class);
                                errorReportScreenIntent.putExtra("uid", uid);
                                startActivity(errorReportScreenIntent);
                            } else if (position == 5){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(Profile.this, LoginActivity.class);
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
                    Toast.makeText(Profile.this, "Image Uploaded", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    ////////////
}