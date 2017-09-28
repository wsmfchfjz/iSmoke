package ganguo.oven.bluetooth;

import android.util.Log;

import ganguo.oven.AppContext;
import ganguo.oven.Config;
import ganguo.oven.Constants;

/**
 * Created by Tony on 1/28/15.
 */
public class SmokerModule {
    private static final String TAG = SmokerModule.class.getName();
    private static boolean mIsOn = false;

    public static void setSmokerTime(int timerHour, int timerMinute) {
        Config.putInt(Constants.SMOKER_SETTING_TIMER_HOUR, timerHour);
		Config.putInt(Constants.SMOKER_SETTING_TIMER_MINUTE, timerMinute);
    }

    public static void setSmokerStatus(boolean isOn) {
        mIsOn = isOn;
        if (!isOn) {
            AppContext.getInstance().getReceiveData().setSmokedStatus((byte) 0x00);
        }
    }

    public static void startOven() {
        AppContext.getInstance().getReceiveData().setSmokedStatus((byte) 0x01);

        DeviceModule.setSmokerTime(Config.getInt(Constants.SMOKER_SETTING_TIMER_HOUR), Config.getInt(Constants.SMOKER_SETTING_TIMER_MINUTE));
        DeviceModule.setSmokerStatus(true);
        DeviceModule.sendAlertProbeTemperature();

        AppContext.getInstance().getReceiveData().setSmokedStatus((byte) 0x01);
        Log.i(TAG, "startOven");
    }

    public static void stopOven() {
        AppContext.getInstance().getReceiveData().setSmokedStatus((byte) 0x00);
        DeviceModule.setSmokerStatus(false);

        Log.i(TAG, "stopOven");
    }

    public static boolean isSmokerOn() {
        return mIsOn;
    }
}
