package bigbrother.child_monitoring_system;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by kstra on 2/13/2017.
 */

public class UserOptionsScreen extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private DatabaseReference dRef;
    private FirebaseAuth firebaseAuth;
    private User currUser;
    private User adminUser;
    private String uid;
    private ValueEventListener currentListener;


    private TextView firstName;
    private TextView lastName;
    private TextView email;
    private TextView childFirstName;
    private TextView childLastName;
    private TextView schoolName;

    private Button removeButton;

    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBar actionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle = "User Options";
    private UserType userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_user_options);
        firstName = (TextView) findViewById(R.id.firstNameData);
        lastName = (TextView) findViewById(R.id.lastNameData);
        email = (TextView) findViewById(R.id.emailData);
        childFirstName = (TextView) findViewById(R.id.childFirstNameData);
        childLastName = (TextView) findViewById(R.id.childLastNameData);
        schoolName = (TextView) findViewById(R.id.schoolNameData);

        dRef = FirebaseDatabase.getInstance().getReference().child("users");
        uid = getIntent().getStringExtra("uid");
        final String adminUid = getIntent().getStringExtra("adminUid");

        firebaseAuth = FirebaseAuth.getInstance();

        removeButton = (Button) findViewById(R.id.removeButton);

        currentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currUser = dataSnapshot.getValue(User.class);
                firstName.setText(currUser.getFirstName());
                firstName.setFocusable(false);
                lastName.setText(currUser.getLastName());
                lastName.setFocusable(false);
                email.setText(currUser.getEmail());
                childFirstName.setFocusable(false);
                childLastName.setFocusable(false);
                schoolName.setText(currUser.getSchoolName());
                schoolName.setFocusable(false);
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        };

        dRef.child(uid).addValueEventListener(currentListener);

        dRef.child(adminUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adminUser = dataSnapshot.getValue(User.class);
                Toast.makeText(UserOptionsScreen.this, "adminEmail: " + adminUser.getEmail() + " adminPwd: " + adminUser.getPassword(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });

        removeButton.setOnClickListener(this);

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
        if (v == removeButton) {
            removeUser();
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
                mAdapter = new ArrayAdapter<String>(UserOptionsScreen.this, R.layout.menu_item, menuArr);
                mDrawerList.setAdapter(mAdapter);
                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (userType.equals(UserType.PARENT)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(UserOptionsScreen.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(UserOptionsScreen.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent profileScreenIntent = new Intent(UserOptionsScreen.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 4){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(UserOptionsScreen.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        } else if (userType.equals(UserType.ADMIN)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(UserOptionsScreen.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(UserOptionsScreen.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 2) {
                                final Intent searchScreenIntent = new Intent(UserOptionsScreen.this, SearchScreen.class);
                                searchScreenIntent.putExtra("uid", uid);
                                startActivity(searchScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(UserOptionsScreen.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 6){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(UserOptionsScreen.this, LoginActivity.class);
                                startActivity(loginScreenIntent);
                            }
                        } else if (userType.equals(UserType.EMPLOYEE)) {
                            if (position == 0) {
                                final Intent homeScreenIntent = new Intent(UserOptionsScreen.this, HomeScreen.class);
                                homeScreenIntent.putExtra("uid", uid);
                                startActivity(homeScreenIntent);
                            } else if (position == 1) {
                                final Intent mapScreenIntent = new Intent(UserOptionsScreen.this, Map.class);
                                mapScreenIntent.putExtra("uid", uid);
                                startActivity(mapScreenIntent);
                            } else if (position == 3){
                                final Intent profileScreenIntent = new Intent(UserOptionsScreen.this, Profile.class);
                                profileScreenIntent.putExtra("uid", uid);
                                startActivity(profileScreenIntent);
                            } else if (position == 5){
                                FirebaseAuth.getInstance().signOut();
                                final Intent loginScreenIntent = new Intent(UserOptionsScreen.this, LoginActivity.class);
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

    public void removeUser() {
        dRef.child(uid).removeEventListener(currentListener);
        firebaseAuth.signOut();
        //auth is now signed out
        firebaseAuth.signInWithEmailAndPassword(currUser.getEmail(), currUser.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //  If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                        } else {
                            // delete current user
                            final String currentUid = firebaseAuth.getCurrentUser().getUid();
                            Log.w(TAG, "signing in user: " + currentUid + " was successful");
                            firebaseAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    boolean success = task.isSuccessful();
                                    if (success) {
                                        Log.w(TAG, "User: " + currentUid + " was successfully deleted.");
                                        dRef.child(currentUid).setValue(null);
                                    } else {
                                        Log.w(TAG, "Unable to delete User: " + currentUid);
                                    }
                                    firebaseAuth.signOut();
                                    firebaseAuth.signInWithEmailAndPassword(adminUser.getEmail(), adminUser.getPassword())
                                            .addOnCompleteListener(UserOptionsScreen.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (!task.isSuccessful()) {
                                                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                                                        Log.w(TAG, "Logging Admin back in Failed");
                                                    } else {
                                                        Log.w(TAG, "Admin is back in control");
                                                    }
                                                    finish();
                                                }
                                            });
                                }
                            });
                        }
                    }
                });

//        // remove user
////        Toast.makeText(UserOptionsScreen.this, uid, Toast.LENGTH_LONG).show();
//
//        // temporarily sign out current admin


//        // temporarily sign in user
//        firebaseAuth.signInWithEmailAndPassword(currUser.getEmail(), currUser.getPassword())
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
////                        progressDialog.hide();
//
//                        // If sign in fails, display a message to the user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "signInWithEmail:failed", task.getException());
//                            Toast.makeText(UserOptionsScreen.this, "Auth Failed",
//                                    Toast.LENGTH_SHORT).show();
//
//                        } else {
//                            // delete current user
//                            firebaseAuth.getCurrentUser().delete();
//                            firebaseAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        Log.d(TAG, "OK! Works fine!");
//                                    } else {
//                                        Log.w(TAG, "Something is wrong!");
//                                    }
//                                }
//                            });
//                        }
//                    }
//                });
//
//        // log in admin again
//        firebaseAuth.signInWithEmailAndPassword(admin.getEmail(), admin.getPassword())
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
////                        progressDialog.hide();
//
//                        // If sign in fails, display a message to the user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "signInWithEmail:failed", task.getException());
//                            Toast.makeText(UserOptionsScreen.this, "Auth Failed",
//                                    Toast.LENGTH_SHORT).show();
//
//                        } else {
//                        }
//                    }
//                });
    }
}