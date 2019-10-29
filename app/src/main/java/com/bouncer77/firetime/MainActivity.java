package com.bouncer77.firetime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Locale;

import static com.bouncer77.firetime.SettingsActivity.*;
import static java.util.Locale.getDefault;

public class MainActivity extends AppCompatActivity {

    // Последовательность чисел
    // Первое из них — старшая версия (major), второе — младшая (minor), третья — мелкие изменения (maintenance, micro).
    //private static final String VERSION = "v0.1.0";

    private TextView textViewTimer;
    private TextView textViewSettingsInfo;
    private String language  = Locale.getDefault().getDisplayLanguage();; // your language
    private int spinnerlang = 0; // TODO написать номер языка в спинере настроки
    private Button buttonStartPause;
    private Button buttonReset;
    private boolean isMute = false;
    private boolean isLock = false;
    private boolean isPause = false; // состояние пауза, когда виден значек паузы, а время идет

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
    public static final String ISLOCK = "lockStatus";
    public static final String ISMUTE = "muteStatus";
    public static final String ISPause = "isPause"; // Button start, pause


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
        isLock = savedInstanceState.getBoolean(ISLOCK);
        isMute = savedInstanceState.getBoolean(ISMUTE);
        isPause = savedInstanceState.getBoolean(ISPause);

        Button buttonPlayPause = (Button) findViewById(R.id.button_start_pause);
        if (isPause) {
            buttonPlayPause.setText(getString(R.string.button_pause));
            buttonPlayPause.setTextColor(getApplication().getResources().getColor(R.color.colorRed));
            buttonPlayPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_48dp, 0, 0, 0);
        }


        ImageView imageViewLock = (ImageView) findViewById(R.id.imageViewLock);
        ImageView imageViewMute = (ImageView) findViewById(R.id.imageViewVolume);
        if (isLock) {
            imageViewLock.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_red_48dp));
        }
        if (isMute) {
            imageViewMute.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_off_red_48dp));
        }
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
                Integer.toString(milliseconds_delta);
                //lang + "   " + getVersion();
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
        buttonStartPause = (Button) findViewById(R.id.button_start_pause);
        buttonReset = (Button) findViewById(R.id.button_reset);
        textViewSettingsInfo = (TextView) findViewById(R.id.textViewSettingsInfo);




        // Получение интента с настройками
        Bundle extrasSettings = getIntent().getExtras(); // из активности настройки
        onRestoreExtrasSettings(extrasSettings);

        // Вывод информации о настройках
        String settingsInfo = createSettingsInfo();
        textViewSettingsInfo.setText(settingsInfo);

        // При первом запуске отрисовка иконки на кнопке Start
        //if(nstart == 0) toggleButtonStartPause.setButtonDrawable(R.drawable.ic_play_48dp);

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
        outState.putBoolean(ISLOCK, isLock);
        outState.putBoolean(ISMUTE, isMute);
        outState.putBoolean(ISPause, isPause);
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
        if (isLock) return;
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

        if (isLock) return;

        Button buttonPlayPause = (Button) view;
        if (isPause) {
            isPause = false;
            isRunning = false;
            if (!isMute) pauseSound.start();
            showToast(getString(R.string.button_pause));
            buttonPlayPause.setText(getString(R.string.button_start));
            //buttonPlayPause.setTextColor(getColor(R.color.colorPrimary)); // API 23
            buttonPlayPause.setTextColor(getApplication().getResources().getColor(R.color.colorGreen));
            buttonPlayPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_48dp, 0, 0, 0);
        } else {
            isPause = true;
            isRunning = true;
            if (!isMute) startSound.start();
            showToast(getString(R.string.button_start));
            buttonPlayPause.setText(getString(R.string.button_pause));
            buttonPlayPause.setTextColor(getApplication().getResources().getColor(R.color.colorRed));
            buttonPlayPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_48dp, 0, 0, 0);
        }

        /*boolean on = toggleButtonStartPause.isChecked();
        if (isLock) {
            //TODO проблема лишнего нажатия на кнопку при запуск->блокировка->пауза->снятиеблокировки->"!старт
            if (on) {
                toggleButtonStartPause.setText(getString(R.string.remove_lock));
            } else {
                toggleButtonStartPause.setText(getString(R.string.remove_lock));
            }
            return;
        }*/

        /*if (isStop) {
            // Вкл
            if (!isMute) startSound.start();
            toggleButtonStartPause.setButtonDrawable(R.drawable.ic_pause_48dp);
            toggleButtonStartPause.setTextColor(getResources().getColor(R.color.colorRed));
            isRunning = true;
            showToast(getString(R.string.button_start));
        } else {
            // Выкл
            if (!isMute) pauseSound.start();
            toggleButtonStartPause.setButtonDrawable(R.drawable.ic_play_48dp);
            toggleButtonStartPause.setTextColor(getResources().getColor(R.color.colorGreen));
            isRunning = false;
            showToast(getString(R.string.button_pause));
        }*/
    }

    public void onClickResetTimer(View view) {
        if (isLock) return;
        isRunning = false;
        if (!isMute) resetSound.start();
        seconds = 0;
        milliseconds_timer = 0;
        showToast(getString(R.string.button_reset));

        // Привести кнопку-переключатель в исходное состояние
        isPause = false;
        Button buttonPlayPause = (Button) findViewById(R.id.button_start_pause);
        buttonPlayPause.setText(getString(R.string.button_start));
        //buttonPlayPause.setTextColor(getColor(R.color.colorPrimary)); // API 23
        buttonPlayPause.setTextColor(getApplication().getResources().getColor(R.color.colorGreen));
        buttonPlayPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_48dp, 0, 0, 0);

        /*toggleButtonStartPause.setChecked(false);
        toggleButtonStartPause.setButtonDrawable(R.drawable.ic_play_48dp);
        toggleButtonStartPause.setTextColor(getResources().getColor(R.color.colorGreen));*/
    }

    /*public static String getVersion() {
        return VERSION;
    }*/

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

    public void onClickVolumeUpOff(View view) {
        if (isLock) return;
        ImageView imageView = (ImageView) view;
        if (isMute) {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_up_white_48dp));
            showToast(getString(R.string.volume_on));
            isMute = false;
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_off_red_48dp));
            showToast(getString(R.string.volume_off));
            isMute = true;
        }
    }

    public void onClickLockUnlock(View view) {
        ImageView imageView = (ImageView) view;
        if (isLock) {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_open_green_48dp));
            showToast(getString(R.string.unlock));
            isLock = false;
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_red_48dp));
            showToast(getString(R.string.lock));
            isLock = true;
        }
    }
}
