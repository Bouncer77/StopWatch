package com.hello.stopwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Locale;

import static com.hello.stopwatch.SettingsActivity.*;
import static java.util.Locale.getDefault;

public class MainActivity extends AppCompatActivity {

    // Последовательность чисел
    // Первое из них — старшая версия (major), второе — младшая (minor), третья — мелкие изменения (maintenance, micro).
    private static final String VERSION = "v0.1.0";

    private TextView textViewTimer;
    private TextView textViewSettingsInfo;
    private String language  = Locale.getDefault().getDisplayLanguage();; // your language
    private int spinnerlang = 0; // TODO написать номер языка в спинере настроки
    private ToggleButton toggleButtonStartPause;
    private Button buttonReset;
    private static int nstart = 0; // число нажатий на старт

    private MediaPlayer startSound, pauseSound, resetSound;


    // таймер 1 (НЕ Выключается, когда активность перестает быть видимой для пользователя)
    private int seconds = 0; // количество прошедших секунд
    private int milliseconds_timer = 0; // количество прошедших миллисекунд
    private boolean isRunning = false; // секундомер работает?
    private boolean wasRunning = false; // был запущен? (для смены оринтации экрана)

    //private Locale myLocale = Locale.getDefault();

    /*секундомер останавливается, если приложе-
      ние Stopwatch становится невидимым, и снова запускается,
      когда приложение снова оказывается на экране.*/
    public String lang = "en";
    public boolean background_running = true;
    public boolean paused_running = true;
    public boolean show_milliseconds = true;
    public int milliseconds_delta = 125; // по умолчанию 125 миллисекунд

    public static final String ISRUN = "isRunning"; // флаг отсчета времени
    public static final String WASRUN = "wasRunning"; // флаг отсчета времени до приостановки активности
    public static final String SEC = "seconds";
    public static final String MSEC = "milliseconds";

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Метод не требует писать: if (savedInstanceState != null) {...}
        // Получить предыдущее состояние секундомера,
        // если активность была уничтожена из-за поворота экрана и создана заново.
        seconds = savedInstanceState.getInt(SEC);
        milliseconds_timer = savedInstanceState.getInt(MSEC);
        isRunning = savedInstanceState.getBoolean(ISRUN);
        wasRunning = savedInstanceState.getBoolean(WASRUN);
    }

    protected void onRestoreExtrasSettings(Bundle bundle) {
        if (bundle != null) {
            background_running = (boolean) bundle.getBoolean(SWBACKGROUND, true);
            paused_running = (boolean) bundle.getBoolean(SWPAUSE, true);
            show_milliseconds = (boolean) bundle.getBoolean(SWMILLISECONDS, true);
            milliseconds_delta = (int) bundle.getInt(NUMMILLISEC, 125);
            spinnerlang = (int) bundle.getInt(LANGINT, 0);
            lang = bundle.getString(LANG);
            if (lang == null) lang = "English";
        }
    }

    protected String createSettingsInfo() {

        String info = Boolean.toString(background_running) + "   " +
                Boolean.toString(paused_running) + "   " +
                Boolean.toString(show_milliseconds) + "   " +
                Integer.toString(milliseconds_delta) + "   " +
                lang + "   " + getVersion();
        return info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Получаем ссылку на панель инструментов и назначаем ее панелью действий активности.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // MediaPlayer
        startSound = MediaPlayer.create(this, R.raw.start);
        pauseSound = MediaPlayer.create(this, R.raw.pause);
        resetSound = MediaPlayer.create(this, R.raw.reset);

        // Получить элементы View
        textViewTimer = (TextView) findViewById(R.id.textViewTimer);
        toggleButtonStartPause = (ToggleButton) findViewById(R.id.toggle_button_start_pause);
        buttonReset = (Button) findViewById(R.id.button_reset);
        textViewSettingsInfo = (TextView) findViewById(R.id.textViewSettingsInfo);

        // Получение интента с настройками
        Bundle extrasSettings = getIntent().getExtras(); // из активности настройки
        onRestoreExtrasSettings(extrasSettings);

        // Вывод информации о настройках
        String settingsInfo = createSettingsInfo();
        textViewSettingsInfo.setText(settingsInfo);

        // При первом запуске отрисовка иконки на кнопке Start
        if(nstart == 0) toggleButtonStartPause.setButtonDrawable(R.drawable.ic_play_48dp);

        // Обновление секундомера
        runTimer();
    }

    // Сохранить состояние секундомера,
    //если он готовится к уничтожению.
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SEC, seconds);
        outState.putInt(MSEC, milliseconds_timer);
        outState.putBoolean(ISRUN, isRunning);
        outState.putBoolean(WASRUN, wasRunning);
    }

    // Обновление показателей таймера
    // Метод runTimer() использует объект Handler
    //для увеличения числа секунд и обновления надписи
    private void runTimer(){

        //final TextView timeView = (TextView)findViewById(R.id.time_view);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time;
                if (show_milliseconds) {
                    time = String.format(getDefault(), "%d:%02d:%02d:%03d",
                            hours, minutes, secs, milliseconds_timer);
                } else {
                    time = String.format(getDefault(), "%d:%02d:%02d",
                            hours, minutes, secs);
                }
                textViewTimer.setText(time);

                if(isRunning) {
                    milliseconds_timer += milliseconds_delta;
                    if (milliseconds_timer >= 1000) {
                        ++seconds;
                        milliseconds_timer -= 1000;
                    }

                }
                handler.postDelayed(this, milliseconds_delta);
            }
        });
    }



    // Если активность приостанавливается,
    //остановить отсчет времени (при paused_running == false)
    // включенный Switch номер 2 (по умолчанию выключен)
    @Override
    protected void onPause() {
        super.onPause();

        if (paused_running) return;

        wasRunning = isRunning;
        isRunning = false;
    }


    public void onClickOpenSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SWBACKGROUND, !background_running);
        intent.putExtra(SWPAUSE, !paused_running);
        intent.putExtra(SWMILLISECONDS, !show_milliseconds);
        intent.putExtra(NUMMILLISEC, milliseconds_delta);
        intent.putExtra(LANGINT, spinnerlang);
        startActivity(intent);
    }

    public void showToast(CharSequence text) {
        int duraction = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duraction);
        toast.show();
    }

    public void onClickStartPauseTimer(View view) {

        boolean on = toggleButtonStartPause.isChecked();
        if (on) {
            // Вкл
            startSound.start();
            toggleButtonStartPause.setButtonDrawable(R.drawable.ic_pause_48dp);
            toggleButtonStartPause.setTextColor(getResources().getColor(R.color.colorRed));
            isRunning = true;
            showToast(getString(R.string.button_start));
        } else {
            // Выкл
            pauseSound.start();
            toggleButtonStartPause.setButtonDrawable(R.drawable.ic_play_48dp);
            toggleButtonStartPause.setTextColor(getResources().getColor(R.color.colorGreen));
            isRunning = false;
            showToast(getString(R.string.button_pause));
        }
    }

    public void onClickResetTimer(View view) {
        isRunning = false;
        resetSound.start();
        seconds = 0;
        milliseconds_timer = 0;
        showToast(getString(R.string.button_reset));

        // Привести кнопку-переключатель в исходное состояние
        toggleButtonStartPause.setChecked(false);
        toggleButtonStartPause.setButtonDrawable(R.drawable.ic_play_48dp);
        toggleButtonStartPause.setTextColor(getResources().getColor(R.color.colorGreen));
    }

    public String getVersion() {
        return VERSION;
    }

    @Override
    protected void onStart(){
        super.onStart();

        if (background_running) return;

        if (wasRunning) {
            isRunning = true;
            wasRunning = false;
        }
    }

    // Если активность свернута,
    // остановить отсчет времени (при backgroun_running == false)
    // включенный Switch номер 1 (по умолчанию выключен)
    @Override
    protected void onStop(){
        super.onStop();

        if (background_running) return;

        wasRunning = isRunning;
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (paused_running) return;

        if (wasRunning) {
            isRunning = true;
            wasRunning = false;
        }
    }
}
