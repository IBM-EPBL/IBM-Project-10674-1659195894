package bigbrother.child_monitoring_system;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ChildInputFragment extends android.support.v4.app.DialogFragment {

    private Button accept, cancel;
    private EditText childName, childMAC;

    private OnCompleteListener mListener;

    public ChildInputFragment() {
        //empty
    }

    public static ChildInputFragment newInstance(String title, ChildDataObject child, int pos, String uid) {
        ChildInputFragment frag = new ChildInputFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("uid", uid);

        //if the object is null that means the dialog is being open by the FAB and not
        //the edit button
        if(child != null) {
            args.putString("name", child.getName());
            args.putString("mac", child.getMacAddress());
        }
        args.putInt("position", pos);
        frag.setArguments(args);
        return frag;
    }


    //create interface to pass the data back to the previous activity
    public static interface OnCompleteListener {
        public abstract void onComplete(ChildDataObject child);
        public abstract void onComplete(ChildDataObject child, int pos);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if(context instanceof Activity) {
            a =(Activity) context;
        } else {
            a = null;
        }

        try {
            this.mListener = (OnCompleteListener) a;
        } catch (final ClassCastException e) {
            throw new ClassCastException(a.toString() + " must implement OnCompleteListener");
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.child_input_fragment, container);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        childName = (EditText) view.findViewById(R.id.childName);
        childName.setHint("Child's Name");
        childMAC = (EditText) view.findViewById(R.id.childMAC);
        childMAC.setHint("Child Bracelet MAC");

        cancel = (Button) view.findViewById(R.id.buttonCancel);
        //make click listeners for the buttons
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dismiss();
            }
        });

        final OnCompleteListener mListenerTemp = this.mListener;
        accept = (Button) view.findViewById(R.id.buttonOK);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if(TextUtils.isEmpty(childName.getText().toString().trim())) {
                    childName.setError("Please input a name");
                    return;
                } else if(TextUtils.isEmpty(childMAC.getText().toString().trim())) {
                    childMAC.setError("Please provide the bracelet's MAC address");
                    return;
                }

                ChildDataObject child = new ChildDataObject(childName.getText().toString(), childMAC.getText().toString());
                child.setParentUID(getArguments().getString("uid"));
                Log.d("CHILD INPUT FRAG", "" + getArguments().getInt("position"));

                mListenerTemp.onComplete(child, getArguments().getInt("position"));
                dismiss();
            }
        });

        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        childName.setText(getArguments().getString("name"));
        childMAC.setText(getArguments().getString("mac"));
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        childName.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }



}