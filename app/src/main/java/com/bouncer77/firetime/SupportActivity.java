package com.bouncer77.firetime;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bouncer77.firetime.R;

public class SupportActivity extends AppCompatActivity {

    private EditText editTextSubject;
    private EditText editTextMsg;
    private String versionName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Стрелка назад
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        editTextMsg = findViewById(R.id.editTextMSG);
        editTextSubject = findViewById(R.id.editTextEmailSubject);
    }

    public void onClickSendMsgToSupport(View view) {

        String subject = getString(R.string.app_name) + " " + versionName;
        subject += " " + editTextSubject.getText().toString();


        String  msg = "************";
        msg += "\n Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        msg += "\n OS API Level: " + android.os.Build.VERSION.RELEASE + "(" + android.os.Build.VERSION.SDK_INT + ")";
        //msg += "\n OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        //msg += "\n Device: " + android.os.Build.DEVICE;
        //msg += "\n Model (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";
        msg += "\n Version app:" + versionName + "\n************\n";
        msg += editTextMsg.getText().toString();


        Intent intent = new Intent(Intent.ACTION_SENDTO);
        //intent.setDataAndType(Uri.parse("mailto:bouncer77.firetime@gmail.com"), "text/plain");
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:bouncer77.firetime@gmail.com")); // or just "mailto:" for blank
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        startActivity(intent);
    }
}
