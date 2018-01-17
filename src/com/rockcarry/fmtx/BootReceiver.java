package com.rockcarry.fmtx;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import java.io.*;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "FmTxReceiver";
    private static final String PREFS_FMTX_SETTINGS     = "PREFS_FMTX_SETTINGS";
    private static final String ACTION_SET_FMTX_STATE   = "android.fm.action.set_fmtx_state";
    private static final String ACTION_PHONE_CALL_START = "android.phone.call.start";
    private static final String ACTION_PHONE_CALL_STOP  = "android.phone.call.stop";
    private static final String FMTX_STATE_VALUE        = "fmtx_state_value";
    private static final String FMTX_FREQ_VALUE         = "fmtx_freq_value";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (getFmTxOnOff(context)) {
                setFmTxFreq(context, true, getFmTxFreq(context));
            }
        }
        else if (action.equals(ACTION_SET_FMTX_STATE)) {
            boolean state = intent.getExtras().getBoolean(FMTX_STATE_VALUE);
            int     freq  = intent.getExtras().getInt    (FMTX_FREQ_VALUE );
            if (freq == 0) freq = getFmTxFreq(context);
            setFmTxFreq(context, state, freq);
        }
        else if (action.equals(ACTION_PHONE_CALL_START)) {
            if (getFmTxOnOff(context)) {
                restoreAllStreamVolume(context);
            }
        }
        else if (action.equals(ACTION_PHONE_CALL_STOP)) {
            if (getFmTxOnOff(context)) {
                setAllStreamVolumeMax(context);
            }
        }
    }

    public static void setFmTxFreq(Context context, boolean state, int freq) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_FMTX_SETTINGS, 0).edit();
        writeFile("/dev/apical", "fmtx " + (state ? freq : 0));
        editor.putBoolean(FMTX_STATE_VALUE, state);
        editor.putInt    (FMTX_FREQ_VALUE , freq );
        editor.commit();
        showNotification(context, state, String.format("%.1f MHz", freq / 10.0f));
        if (state) {
            setAllStreamVolumeMax(context);
        } else {
            restoreAllStreamVolume(context);
        }
    }

    public static Boolean getFmTxOnOff(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_FMTX_SETTINGS, 0);
        return sp.getBoolean(FMTX_STATE_VALUE, false);
    }

    public static int getFmTxFreq(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_FMTX_SETTINGS, 0);
        return sp.getInt(FMTX_FREQ_VALUE, 900);
    }

    static void setAllStreamVolumeMax(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_FMTX_SETTINGS, 0).edit();
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int cur_ring_volume  = am.getStreamVolume(AudioManager.STREAM_RING );
        int cur_music_volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int cur_alarm_volume = am.getStreamVolume(AudioManager.STREAM_ALARM);
        int cur_notif_volume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        int max_ring_volume  = am.getStreamMaxVolume(AudioManager.STREAM_RING );
        int max_music_volume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int max_alarm_volume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int max_notif_volume = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

        // set audio output device for fmtx
        am.setWiredDeviceConnectionState(AudioManager.DEVICE_OUT_ANLG_DOCK_HEADSET, 1, "fmtx");

//      if (cur_ring_volume != max_ring_volume) {
            editor.putInt("last_ring_volume", cur_ring_volume);
            am.setStreamVolume(AudioManager.STREAM_RING, max_ring_volume, 0);
//      }

//      if (cur_music_volume != max_music_volume) {
            editor.putInt("last_music_volume", cur_music_volume);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, max_music_volume, 0);
//      }

//      if (cur_alarm_volume != max_alarm_volume) {
            editor.putInt("last_alarm_volume", cur_alarm_volume);
            am.setStreamVolume(AudioManager.STREAM_ALARM, max_alarm_volume, 0);
//      }

//      if (cur_notif_volume != max_notif_volume) {
            editor.putInt("last_notif_volume", cur_notif_volume);
            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, max_notif_volume, 0);
//      }

        editor.commit();
    }

    static void restoreAllStreamVolume(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_FMTX_SETTINGS, 0);
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int cur_ring_volume   = am.getStreamVolume(AudioManager.STREAM_RING );
        int cur_music_volume  = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int cur_alarm_volume  = am.getStreamVolume(AudioManager.STREAM_ALARM);
        int cur_notif_volume  = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        int last_ring_volume  = sp.getInt("last_ring_volume" , -1);
        int last_music_volume = sp.getInt("last_music_volume", -1);
        int last_alarm_volume = sp.getInt("last_alarm_volume", -1);
        int last_notif_volume = sp.getInt("last_notif_volume", -1);
        int max_ring_volume   = am.getStreamMaxVolume(AudioManager.STREAM_RING );
        int max_music_volume  = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int max_alarm_volume  = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int max_notif_volume  = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

        // set audio output device for fmtx
        am.setWiredDeviceConnectionState(AudioManager.DEVICE_OUT_ANLG_DOCK_HEADSET, 0, "fmtx");

        if (last_ring_volume  != -1 && cur_ring_volume == max_ring_volume) {
            am.setStreamVolume(AudioManager.STREAM_RING, last_ring_volume, 0);
        }
        if (last_music_volume != -1 && cur_music_volume == max_music_volume) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, last_music_volume, 0);
        }
        if (last_alarm_volume != -1 && cur_alarm_volume == max_alarm_volume) {
            am.setStreamVolume(AudioManager.STREAM_ALARM, last_alarm_volume, 0);
        }
        if (last_notif_volume != -1 && cur_notif_volume == max_notif_volume) {
            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, last_notif_volume, 0);
        }
    }

    private static final int NOTIFICATION_ID = 1;
    private static Notification        mNotification = new Notification();
    private static NotificationManager mNotifyManager= null;
    private static void showNotification(Context context, boolean show, String msg) {
        if (mNotifyManager == null) mNotifyManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (show) {
            PendingIntent pi    = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
            mNotification.flags = Notification.FLAG_ONGOING_EVENT;
            mNotification.icon  = R.drawable.fmicon;
            mNotification.setLatestEventInfo(context, context.getResources().getString(R.string.app_name), msg, pi);
            mNotifyManager.notify(NOTIFICATION_ID, mNotification);
        }
        else {
            mNotifyManager.cancel(NOTIFICATION_ID);
        }
    }

    private static void writeFile(String file, String text) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, false);
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}


