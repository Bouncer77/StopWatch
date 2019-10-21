package com.hello.stopwatch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private Spinner selectLang;
    public static final String LANG = "language";
    public static final String LANGINT = "languageint";

    private Switch swbackgroundRun;
    public static final String SWBACKGROUND = "swbackground";

    private Switch swpauseRun;
    public static final String SWPAUSE = "swpause";

    private Switch swmilliseconds;
    public static final String SWMILLISECONDS = "swmilliseconds";

    private EditText numMilliseconds;
    public static final String NUMMILLISEC = "numMilliseconds";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selectLang = (Spinner) findViewById(R.id.spinnerLanguage);
        swbackgroundRun = (Switch) findViewById(R.id.switchBackground);
        swpauseRun = (Switch) findViewById(R.id.switchPause);
        swmilliseconds = (Switch) findViewById(R.id.switchMilliseconds);
        numMilliseconds = (EditText) findViewById(R.id.editTextNumMilliseconds);

        Intent intent = getIntent();
        boolean swbg = intent.getBooleanExtra(SWBACKGROUND, false);
        boolean swp = intent.getBooleanExtra(SWPAUSE, false);
        boolean swms = intent.getBooleanExtra(SWMILLISECONDS, false);
        int numMsec = intent.getIntExtra(NUMMILLISEC, 125);
        int numSpinner = intent.getIntExtra(LANGINT, 0);

        swbackgroundRun.setChecked(swbg);
        swpauseRun.setChecked(swp);
        swmilliseconds.setChecked(swms);
        numMilliseconds.setText(Integer.toString(numMsec));

    }

    public void onClickApplySettings(View view) {

        String lang = getChooseLang();
        int num = Integer.valueOf(numMilliseconds.getText().toString());
        Intent mIntent = new Intent(this, MainActivity.class);
        mIntent.putExtra(LANG, lang);
        mIntent.putExtra(SWBACKGROUND, !(swbackgroundRun.isChecked()));
        mIntent.putExtra(SWPAUSE, !(swpauseRun.isChecked()));
        mIntent.putExtra(SWMILLISECONDS, !(swmilliseconds.isChecked()));
        mIntent.putExtra(NUMMILLISEC, num);
        mIntent.putExtra(LANGINT, selectLang.getSelectedItemPosition());
        startActivity(mIntent);
    }

    private String getChooseLang() {
        int nlang = selectLang.getSelectedItemPosition(); // номер языка в Спинере
        String lang;
        switch (nlang) {
            case 0:
                lang = "en";
                break;

            case 1:
                lang = "ru";
                break;

            default:
                lang = "en";
                break;
        }
        return lang;
    }

    public void onClickShare(View view) {
        String msg = getString(R.string.app_name);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        String chooserTitle = getString(R.string.settings_choser_title);
        Intent choosenIntent = Intent.createChooser(intent, chooserTitle);
        startActivity(choosenIntent);
    }

    public void onClickVote(View view) {
        showToast("in developing");
    }

    public void onClickSupport(View view) {
        Intent intent = new Intent(this, SupportActivity.class);
        startActivity(intent);
    }

    public void onClickAutor(View view) {
        showToast("Ivan Kosenkov");
    }

    public void showToast(CharSequence text) {
        int duraction = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(this, text, duraction);
        toast.show();
    }
}