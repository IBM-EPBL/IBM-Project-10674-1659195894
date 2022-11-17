package bigbrother.child_monitoring_system;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static java.security.CryptoPrimitive.MAC;

public class ChildCardAdapter extends RecyclerView
        .Adapter<ChildCardAdapter
        .DataObjectHolder> {
    private static String LOG_TAG = "ChildCardAdapter";
    private ArrayList<ChildDataObject> childDataset;
    private static MyClickListener myClickListener;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView name;
        TextView macAddress;
        ImageButton buttonDelete, buttonEdit;

        public DataObjectHolder(final View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.cardChildName);
            macAddress = (TextView) itemView.findViewById(R.id.cardChildMAC);

            //create button click listener for the edit and delete buttons
            buttonDelete = (ImageButton) itemView.findViewById(R.id.card_delete_button);
            buttonEdit = (ImageButton) itemView.findViewById(R.id.card_edit_button);

            buttonDelete.setOnClickListener(this);
            buttonEdit.setOnClickListener(this);


            Log.i(LOG_TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if(v.getId() == buttonDelete.getId()) {
                myClickListener.onButtonClick(getAdapterPosition(), v);
            } else if(v.getId() == buttonEdit.getId()) {
                myClickListener.onEditButtonClick(getAdapterPosition(), v);
            }

            myClickListener.onItemClick(getAdapterPosition(), v);


        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public ChildCardAdapter(ArrayList<ChildDataObject> myData) {
        childDataset = myData;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, final int position) {
        holder.name.setText(childDataset.get(position).getName());
        holder.macAddress.setText(childDataset.get(position).getMacAddress());

    }

    public void addItem(ChildDataObject dataObj, int index) {
        childDataset.add(0,dataObj);
        notifyItemInserted(index);

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
        public void onEditButtonClick(int position, View v);
    }
}