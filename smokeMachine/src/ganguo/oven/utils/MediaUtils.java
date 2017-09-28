package ganguo.oven.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import ganguo.oven.AppContext;
import ganguo.oven.R;

/**
 * Created by Tony on 2/5/15.
 */
public class MediaUtils {
    private static Handler mHandler = new Handler();
    private static MediaPlayer warningPlayer;
    private static MediaPlayer alertPlayer;
    private static MediaPlayer reminderPlayer;

    private static Runnable warningRun = new Runnable() {
        @Override
        public void run() {
            playWarning(AppContext.getInstance());
        }
    };
    private static Runnable alertRun = new Runnable() {
        @Override
        public void run() {
            playAlert(AppContext.getInstance());
        }
    };
    private static Runnable reminderRun = new Runnable() {
        @Override
        public void run() {
            playReminder(AppContext.getInstance());
        }
    };

    public static void playWarning(final Context context) {
        stopWarning();

        warningPlayer = MediaPlayer.create(context, R.raw.warning);
        warningPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mHandler.postDelayed(warningRun, 2000);
            }
        });
        warningPlayer.setLooping(false);
        warningPlayer.start();
    }

    public static void playAlert(Context context) {
        stopAlert();

        alertPlayer = MediaPlayer.create(context, R.raw.alert);
        alertPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mHandler.postDelayed(alertRun, 2000);
            }
        });
        alertPlayer.setLooping(false);
        alertPlayer.start();
    }

    public static void playReminder(Context context) {
        stopReminder();

        reminderPlayer = MediaPlayer.create(context, R.raw.reminder);
        reminderPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mHandler.postDelayed(reminderRun, 2000);
            }
        });
        reminderPlayer.setLooping(false);
        reminderPlayer.start();
    }

    public static void stopWarning() {
        mHandler.removeCallbacks(warningRun);
        if (warningPlayer != null && warningPlayer.isPlaying()) {
            warningPlayer.stop();
            warningPlayer.release();
            warningPlayer = null;
        }
    }

    public static void stopAlert() {
        mHandler.removeCallbacks(alertRun);
        if (alertPlayer != null && alertPlayer.isPlaying()) {
            alertPlayer.stop();
            alertPlayer.release();
            alertPlayer = null;
        }
    }

    public static void stopReminder() {
        mHandler.removeCallbacks(reminderRun);
        if (reminderPlayer != null && reminderPlayer.isPlaying()) {
            reminderPlayer.stop();
            reminderPlayer.release();
            reminderPlayer = null;
        }
    }

    public static void stopAll() {
        stopWarning();
        stopAlert();
        stopReminder();
    }

}
