package bigbrother.child_monitoring_system;

import android.graphics.Color;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class TeacherClassCardAdapter extends RecyclerView.Adapter<TeacherClassCardAdapter
        .DataObjectHolder> {

    private ArrayList<ChildDataObject> childDataset;
    private static TeacherClassCardAdapter.MyClickListener myClickListener;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView name;
        TextView macAddress;
        ImageButton buttonDownArrow, buttonEdit;
        Switch absentButton;
        TextView absent;

        public DataObjectHolder(final View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.cardChildName);

            name.setTextSize(20);
            name.setTextColor(Color.parseColor("#F99F20"));
            macAddress = (TextView) itemView.findViewById(R.id.cardChildMAC);
            macAddress.setVisibility(View.INVISIBLE);

            //create button click listener for the edit and delete buttons
            buttonDownArrow = (ImageButton) itemView.findViewById(R.id.card_delete_button);
            buttonEdit = (ImageButton) itemView.findViewById(R.id.card_edit_button);
            buttonEdit.setVisibility(View.GONE);

            buttonDownArrow.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
            buttonDownArrow.setOnClickListener(this);

            absentButton = (Switch) itemView.findViewById(R.id.absent_switch);
            absentButton.setOnClickListener(this);
            absent = (TextView) itemView.findViewById(R.id.absentText);
            absent.setText("Present");




            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if(v.getId() == buttonDownArrow.getId()) {
                myClickListener.onButtonClick(getAdapterPosition(), v);
            } else if(v.getId() == absentButton.getId()) {
                Log.d("SWITCH TOGGLE", String.valueOf(absentButton.isChecked()));
                myClickListener.onSwitchToggle(getAdapterPosition(), v, absentButton.isChecked());
            } else {
                myClickListener.onItemClick(getAdapterPosition(), v);
            }




        }
    }


    public void setOnItemClickListener(TeacherClassCardAdapter.MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public TeacherClassCardAdapter(ArrayList<ChildDataObject> myData) {
        childDataset = myData;
    }

    @Override
    public TeacherClassCardAdapter.DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);

        TeacherClassCardAdapter.DataObjectHolder dataObjectHolder = new TeacherClassCardAdapter.DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(TeacherClassCardAdapter.DataObjectHolder holder, final int position) {
        holder.name.setText(childDataset.get(position).getName());
        holder.macAddress.setText(childDataset.get(position).getMacAddress());
        holder.absentButton.setChecked(childDataset.get(position).isPresent());

    }

    public void addItem(ChildDataObject dataObj, int index) {
        childDataset.add(0,dataObj);
        notifyItemInserted(index);

    }

    /* DELETES THE ITEM FROM THE CLASS SECTION AND THE DATABASE */
    public void deleteItemFromClass(int index, String uid, String schoolName){
        // remove the child from the Class Roster
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("daycare")
                .child(schoolName);

        dRef.child("classrooms").child(uid).child(childDataset.get(index).getMacAddress())
                .removeValue();

        notifyDataSetChanged();


    }

    public void setChildPresence(int index, String schoolName, boolean checked) {
        // Push the boolean value to the child object in the database

        // Set the absence variable from the switch
        childDataset.get(index).setPresent(checked);

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("daycare")
                .child(schoolName);
        java.util.Map<String, Object> childUpdate = new HashMap<String, Object>();
        childUpdate.put("present", childDataset.get(index).isPresent());

        dRef.child("children").child(childDataset.get(index).getMacAddress()).updateChildren(childUpdate);
    }

    public ArrayList<ChildDataObject> getChildDataset(){
        if(childDataset == null) {
            childDataset = new ArrayList<ChildDataObject>();
        }
        return childDataset;
    }

    @Override
    public int getItemCount() {

        if(childDataset == null) {
            return 0;
        }

        return childDataset.size();
    }

    public ChildDataObject getChildDataAt(int pos) {
        return childDataset.get(pos);
    }

    public void replaceAt(int pos, ChildDataObject child) {
        childDataset.set(pos, child);
        notifyItemChanged(pos);
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
        public void onButtonClick(int position, View v);
        public void onSwitchToggle(int position, View v, boolean checked);
    }
}