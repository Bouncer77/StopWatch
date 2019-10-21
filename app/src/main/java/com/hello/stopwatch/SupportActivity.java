package com.hello.stopwatch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SupportActivity extends AppCompatActivity {

    private EditText editTextSubject;
    private EditText editTextMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Стрелка назад
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        editTextMsg = findViewById(R.id.editTextMSG);
        editTextSubject = findViewById(R.id.editTextEmailSubject);
    }

    public void onClickSendMsgToSupport(View view) {
        String msg = editTextMsg.getText().toString();
        String subject = editTextSubject.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        //intent.setDataAndType(Uri.parse("mailto:bouncer77rus@gmail.com"), "text/plain");
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:bouncer77rus@gmail.com")); // or just "mailto:" for blank
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        startActivity(intent);
    }
}
