package bigbrother.child_monitoring_system;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RosterCardAdapter extends RecyclerView.Adapter<RosterCardAdapter
        .DataObjectHolder> {

    private ArrayList<ChildDataObject> childDataset;
    private static RosterCardAdapter.MyClickListener myClickListener;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView name;
        TextView macAddress;
        ImageButton buttonUpArrow, buttonEdit;
        Switch absentButton;

        public DataObjectHolder(final View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.cardChildName);
            macAddress = (TextView) itemView.findViewById(R.id.cardChildMAC);

            //create button click listener for the edit and delete buttons
            buttonUpArrow = (ImageButton) itemView.findViewById(R.id.card_delete_button);
            buttonEdit = (ImageButton) itemView.findViewById(R.id.card_edit_button);
            buttonEdit.setVisibility(View.INVISIBLE);

            absentButton = (Switch) itemView.findViewById(R.id.absent_switch);
            absentButton.setVisibility(View.GONE);

            buttonUpArrow.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
            buttonUpArrow.setColorFilter(Color.BLUE);

            buttonUpArrow.setOnClickListener(this);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if(v.getId() == buttonUpArrow.getId()) {
                myClickListener.onButtonClick(getAdapterPosition(), v);
            }

            myClickListener.onItemClick(getAdapterPosition(), v);


        }
    }

    public void setOnItemClickListener(RosterCardAdapter.MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public RosterCardAdapter(ArrayList<ChildDataObject> myData) {
        childDataset = myData;
    }

    @Override
    public RosterCardAdapter.DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);

        RosterCardAdapter.DataObjectHolder dataObjectHolder = new RosterCardAdapter.DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(RosterCardAdapter.DataObjectHolder holder, final int position) {
        holder.name.setText(childDataset.get(position).getName());
        holder.macAddress.setText(childDataset.get(position).getMacAddress());

    }

    public void addItem(ChildDataObject dataObj, int index) {
        childDataset.add(0,dataObj);
        notifyItemInserted(index);

    }

    /* MOVES THE ITEM TO THE YOUR CLASS SECTION */
    public void moveItemToClass(int index, String uid, String schoolName){
        // move to upper list by adding it to the database

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("daycare")
                .child(schoolName);

        // add the new child to the class
        dRef.child("classrooms").child(uid).child(childDataset.get(index).getMacAddress())
                .setValue(childDataset.get(index).getName());

    }

    public void deleteItem(int index) {
        childDataset.remove(index);
        notifyItemRemoved(index);
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
    }

}