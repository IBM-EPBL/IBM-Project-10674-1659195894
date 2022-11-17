package bigbrother.child_monitoring_system;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

public class ReportError extends AppCompatActivity implements View.OnClickListener {

    private Button settingsButton;
    private EditText errorReportET;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_error);

        settingsButton = (Button) findViewById(R.id.settingsButton);
        errorReportET = (EditText) findViewById(R.id.errorReport);
        uid = getIntent().getStringExtra("uid");

        settingsButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == settingsButton) {
            String message = errorReportET.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                errorReportET.setError("Please enter your error report");
                return;
            } else {
                DatabaseReference mesgRef = FirebaseDatabase.getInstance().getReference().child("errorReports");
                mesgRef.push().setValue(message);
                Intent homeScreen = new Intent(this, HomeScreen.class);
                homeScreen.putExtra("uid", uid);
                startActivity(homeScreen);
            }
        }
    }

}