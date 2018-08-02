package com.byagowi.persiancalendar.view.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.util.Utils;
import com.github.praytimes.PrayTime;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

public class AthanActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = AthanActivity.class.getName();
    private TextView textAlarmName;
    private AppCompatImageView athanIconView;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.changeAppLanguage(this);

        setContentView(R.layout.activity_athan);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textAlarmName = findViewById(R.id.athan_name);
        TextView textCityName = findViewById(R.id.place);
        athanIconView = findViewById(R.id.background_image);
        athanIconView.setOnClickListener(this);

        String prayerKey = getIntent().getStringExtra(Constants.KEY_EXTRA_PRAYER_KEY);
        textAlarmName.setText(Utils.getPrayTimeText(prayerKey));
        athanIconView.setImageResource(Utils.getPrayTimeImage(prayerKey));

        textCityName.setText(getString(R.string.in_city_time) + " " + Utils.getCityName(this, true));

        play();

        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telephonyManager != null)
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Exception e) {
            // nvm
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            stop();
            finish();
        }
    }

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            stop();
            finish();
        }
    };

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                mOnAudioFocusChangeListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
            }
            if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                mOnAudioFocusChangeListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
            }
        }
    };

    private void setPrayerView(String key) {
            textAlarmName.setText(Utils.getPrayTimeText(key));
            athanIconView.setImageResource(Utils.getPrayTimeImage(key));
    }

    @Override
    public void onClick(View v) {
        stop();
        finish();
    }

    @Override
    public void onBackPressed() {
        stop();
        finish();
    }

    private void play() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setDataSource(this, Utils.getAthanUri(getApplicationContext()));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.prepare();
            mediaPlayer.start();

            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, Utils.getAthanVolume(this), 0);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void stop() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
        finish();
    }
}
