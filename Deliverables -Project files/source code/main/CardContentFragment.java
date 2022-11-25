package bigbrother.child_monitoring_system;

/**
 * Created by Luka on 2/15/2017.
 */

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class CardContentFragment extends AppCompatActivity implements ChildInputFragment.OnCompleteListener {


    private DatabaseReference fdbUsers;
    private DatabaseReference fdbSchool;
    private User currentUser;
    private ArrayList<ChildDataObject> results = new ArrayList<ChildDataObject>();;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "CardContentFragment";
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_child_menu);

        uid = getIntent().getStringExtra("uid");

        fdbUsers = FirebaseDatabase.getInstance().getReference().child("users");

        fdbUsers.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ChildCardAdapter(getDataSet());
        mRecyclerView.setAdapter(mAdapter);

        // On click listener for the FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                showChildDialog(null, -1);

            }
        });


        ((ChildCardAdapter) mAdapter).setOnItemClickListener(new ChildCardAdapter
                .MyClickListener() {

            @Override
            public void onEditButtonClick(int pos, View v) {
                showChildDialog(((ChildCardAdapter) mAdapter).getChildDataAt(pos), pos);
            }

            @Override
            public void onButtonClick(int position, View v) {
                ((ChildCardAdapter) mAdapter).deleteItem(position);
                ((ChildCardAdapter) mAdapter).getChildDataset();
                notifyDatabaseChange();
            }

            @Override
            public void onItemClick(int position, View v) {
                // add in if needed later
            }
        });

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//

//        // On click listener for the FAB
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                showChildDialog(null, -1);
//
//            }
//        });
//
//
//        ((ChildCardAdapter) mAdapter).setOnItemClickListener(new ChildCardAdapter
//                .MyClickListener() {
//
//            @Override
//            public void onEditButtonClick(int pos, View v) {
//                showChildDialog(((ChildCardAdapter) mAdapter).getChildDataAt(pos), pos);
//            }
//
//            @Override
//            public void onButtonClick(int position, View v) {
//                ((ChildCardAdapter) mAdapter).deleteItem(position);
//                ((ChildCardAdapter) mAdapter).getChildDataset();
//                notifyDatabaseChange();
//            }
//
//            @Override
//            public void onItemClick(int position, View v) {
//                // add in if needed later
//            }
//        });
//    }


    /**
     * Implementing the OnComplete interface to pass ChildObjects back
     */
    public void onComplete(ChildDataObject child) {
        //add the child card to the recyclerview only if it has changed
        ((ChildCardAdapter) mAdapter).addItem(child, 0);

        ((ChildCardAdapter) mAdapter).getChildDataset();
    }

    /**
     * Implementing the onComplete interface for editing ChildObjects
     */
    public void onComplete(ChildDataObject child, int pos) {

        if (pos == -1) {
            ((ChildCardAdapter) mAdapter).addItem(child, 0);
            notifyDatabaseChange();
            return;
        }

        //replace the object with the newly edited one
        ((ChildCardAdapter) mAdapter).replaceAt(pos, child);
        notifyDatabaseChange();
    }


    private ArrayList<ChildDataObject> getDataSet() {
        results = new ArrayList<ChildDataObject>();
        // Here is where the dataset will be taken from the database on Activity Load
        //results = new ArrayList<ChildDataObject>();
        fdbUsers.child(uid).child("children").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                results.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    results.add(data.getValue(ChildDataObject.class));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //currentUser.setChildren(results);
        return results;
    }

    public void notifyDatabaseChange() {
        currentUser.setChildren(((ChildCardAdapter) mAdapter).getChildDataset());
        fdbUsers.child(uid).setValue(currentUser);
        fdbSchool = FirebaseDatabase.getInstance().getReference().child("daycare").child(currentUser.getSchoolName()).child("children");
        for (ChildDataObject c : currentUser.getChildren()) {
            fdbSchool.child(String.valueOf(c.getMacAddress())).setValue(c);
        }
    }

    private void showChildDialog(ChildDataObject child, int pos) {
        FragmentManager fm = getSupportFragmentManager();
        ChildInputFragment childInputFragment = ChildInputFragment.newInstance("New Child", child, pos, uid);
        childInputFragment.show(fm, "child_input_fragment");
    }

}