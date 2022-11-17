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

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MapInputDialog extends android.support.v4.app.DialogFragment {

    private Button accept, cancel;
    private EditText roomName, piMAC;

    private OnCompleteListener mListener;

    public MapInputDialog() {
        //empty
    }

    public static MapInputDialog newInstance() {
        MapInputDialog dialog = new MapInputDialog();
        return dialog;
    }


    //create interface to pass the data back to the previous activity
    public static interface OnCompleteListener {
        public abstract void onComplete(String name, String mac);
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
        roomName = (EditText) view.findViewById(R.id.childName);
        roomName.setHint("Room's Name");
        piMAC = (EditText) view.findViewById(R.id.childMAC);
        piMAC.setHint("Raspberry Pi's MAC Address");

        final OnCompleteListener mListenerTemp = this.mListener;
        accept = (Button) view.findViewById(R.id.buttonOK);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if(TextUtils.isEmpty(roomName.getText().toString().trim())) {
                    roomName.setError("Please input a name");
                    return;
                } else if(TextUtils.isEmpty(piMAC.getText().toString().trim())) {
                    piMAC.setError("Please provide the Raspberry Pi's MAC address");
                    return;
                }

                mListenerTemp.onComplete(roomName.getText().toString().trim(), piMAC.getText().toString().trim());
                Log.d("DIALOG", piMAC.getText().toString().trim());
                dismiss();
            }
        });


        cancel = (Button) view.findViewById(R.id.buttonCancel);
        //make click listeners for the buttons
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mListenerTemp.onComplete(null, null);
                dismiss();
            }
        });




        // Fetch arguments from bundle and set title
        String title = "Setup New Locator";
        TextView tx = (TextView) view.findViewById(R.id.childMenuTitle);
        tx.setText(title);
        // Show soft keyboard automatically and request focus to field
        roomName.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }



}