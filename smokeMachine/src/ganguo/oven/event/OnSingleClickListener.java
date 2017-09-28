package ganguo.oven.event;

import android.util.Log;
import android.view.View;

/**
 * Created by Wilson on 28/1/15.
 */
public abstract class OnSingleClickListener implements View.OnClickListener {
    private static final String TAG = OnSingleClickListener.class.getName();
    private static final long DOUBLE_PRESS_INTERVAL = 600; // in millis
    private long lastPressTime;

    public abstract void onSingleClick(View v);

    @Override
    public void onClick(View v) {
        long pressTime = System.currentTimeMillis();
        if (pressTime - lastPressTime >= DOUBLE_PRESS_INTERVAL) {
            onSingleClick(v);
            lastPressTime = pressTime;
        } else {
            Log.d(TAG, "double click");
        }
    }
}
