package ganguo.oven;

import ganguo.oven.bluetooth.BleEvent;
import ganguo.oven.bluetooth.BleService;
import ganguo.oven.bluetooth.ReceiveData;
import ganguo.oven.bluetooth.SettingData;
import ganguo.oven.db.Point;

import org.orman.dbms.Database;
import org.orman.dbms.sqliteandroid.SQLiteAndroid;
import org.orman.mapper.MappingSession;

import android.app.Application;
import android.content.Intent;
import de.greenrobot.event.EventBus;

/**
 * Created by Tony on 1/16/15.
 */
public class AppContext extends Application {
    private static final String TAG = AppContext.class.getName();

    private EventBus mEventBus = EventBus.getDefault();
    private ReceiveData mReceiveData = new ReceiveData();
    private SettingData mSettingData = new SettingData();
    private static AppContext instance = null;

    public static AppContext getInstance() {
        return instance;
    }

    public ReceiveData getReceiveData() {
        return mReceiveData;
    }

    public SettingData getSettingData() {
        return mSettingData;
    }

    public void setSettingData(SettingData settingData) {
        mSettingData = settingData;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Config.register(this);
        mEventBus.register(this);
        initAlertSettings();
        initMeat();
        startService(new Intent(this, BleService.class));
        initOrman();
    }

    private void initOrman() {
		Database db = new SQLiteAndroid(this, "OrmanDb.db");	
		MappingSession.registerDatabase(db);
		MappingSession.registerEntity(Point.class);
		MappingSession.start();
	}
    
    /**
     * 设置默认的alert温度，默认为280华氏度
     */
    private void initAlertSettings() {
        int tempA = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
        int tempB = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);
        if (tempA < 50) {
            Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, 280);
//            Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, 165);
        }
        if (tempB < 50) {
            Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, 280);
//            Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, 165);
        }
        int lastMeatTempA = Config.getInt(Constants.LAST_MEAT_TEMP_A);
        int lastMeatTempB = Config.getInt(Constants.LAST_MEAT_TEMP_B);
        
        if(lastMeatTempA == 0){
        	Config.putInt(Constants.LAST_MEAT_TEMP_A, 63145);
        }
        if(lastMeatTempB == 0){
        	Config.putInt(Constants.LAST_MEAT_TEMP_B, 63145);
        }
        
    }

    private void initMeat(){
    	int meatA = Config.getInt(Constants.SETTING_MEAT_A);
        int meatB = Config.getInt(Constants.SETTING_MEAT_B);
        if (meatA == 0) {
            Config.putInt(Constants.SETTING_MEAT_A, 1);
        }
        if (meatB == 0) {
            Config.putInt(Constants.SETTING_MEAT_B, 1);
        }
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(new Intent(this, BleService.class));
        mEventBus.unregister(this);
    }

    public void exit() {
        stopService(new Intent(this, BleService.class));
        mEventBus.unregister(this);
        System.exit(0);
    }

    /**
     * EventBus
     */
    public void onEventMainThread(BleEvent event) {
        switch (event.getCommand()) {
            case NOTIFY_RECEIVE_DATA:
                // 数据接收
                mReceiveData = (ReceiveData) event.getTarget();

                AppContext appContext = AppContext.getInstance();
                if (appContext.getSettingData() == null) {
                    SettingData settingData = new SettingData();
                    settingData.setDeviceCode(mReceiveData.getDeviceCode());
                    appContext.setSettingData(settingData);
                }

                break;
        }
    }
}
