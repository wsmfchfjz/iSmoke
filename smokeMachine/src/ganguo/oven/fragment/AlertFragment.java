package ganguo.oven.fragment;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import ganguo.oven.Config;
import ganguo.oven.Constants;
import ganguo.oven.R;
import ganguo.oven.activity.MainActivity;
import ganguo.oven.bluetooth.BleCommand;
import ganguo.oven.bluetooth.BleEvent;
import ganguo.oven.bluetooth.DeviceModule;
import ganguo.oven.event.OnSingleClickListener;

/**
 * Created by Wilson on 14-7-9.
 */
public class AlertFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener {

    private ImageButton fromAlertToHomeBtn;
    private MainActivity mainActivity;
    private SeekBar seekA;
    private SeekBar seekB;
    private TextView temA, temLabelA;
    private TextView temB, temLabelB;
    private int start = 120;
    private EventBus mEventBus = EventBus.getDefault();

    /**
     * EventBus
     */
    public void onEventMainThread(BleEvent event) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_alert;
    }

    @Override
    public String getMyTAG() {
        return "AlertFragment";
    }

    @Override
    public void beforeInitView() {
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void initView() {
        fromAlertToHomeBtn = (ImageButton) getView().findViewById(R.id.fromAlertToHomeBtn);
        temLabelA = (TextView) getView().findViewById(R.id.temLabelA);
        temLabelB = (TextView) getView().findViewById(R.id.temLabelB);
        seekA = (SeekBar) getView().findViewById(R.id.seekA);
        seekB = (SeekBar) getView().findViewById(R.id.seekB);
        temA = (TextView) getView().findViewById(R.id.temA);
        temB = (TextView) getView().findViewById(R.id.temB);
    }

    @Override
    public void initListener() {
        fromAlertToHomeBtn.setOnClickListener(singleClickListener);
        seekA.setOnSeekBarChangeListener(this);
        seekB.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && temLabelA != null) {
            setTemUnit();
        }
    }

    private void setTemUnit() {
        if (Config.getBoolean(Constants.SETTING_ALERT_TEMPERATURE_UNIT, false)) {
            temLabelA.setText("℃");
            temLabelB.setText("℃");
            start = 50;
            seekA.setMax(110);
            seekB.setMax(110);
        } else {
            temLabelA.setText("℉");
            temLabelB.setText("℉");
            start = 120;
            seekA.setMax(200);
            seekB.setMax(200);
        }

        final int a = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
        final int b = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);

        seekA.post(new Runnable() {
            @Override
            public void run() {
                seekA.setProgress(a - start);
                seekB.setProgress(b - start);

                temA.setText(String.valueOf(a));
                temB.setText(String.valueOf(b));
            }
        });
        Log.d("SETTING_ALERT_TEMPERATURE", "A:" + a + " B:" + b + " start:" + start);
    }

    @Override
    public void initData() {
        mEventBus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mEventBus.unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        setTemUnit();
    }

    private OnSingleClickListener singleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.fromAlertToHomeBtn:
//                    mainActivity.backToHomeSlide();
                	mainActivity.changeToMain();
                    return;
            }
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int temp = progress + start;

        Log.d("SETTING_ALERT_TEMPERATURE", "onProgressChanged " + temp);
        switch (seekBar.getId()) {
            case R.id.seekA:
                temA.setText(String.valueOf(temp));
                return;
            case R.id.seekB:
                temB.setText(String.valueOf(temp));
                return;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int temp = seekBar.getProgress() + start;

        Log.d("SETTING_ALERT_TEMPERATURE", "onStopTrackingTouch " + temp);
        switch (seekBar.getId()) {
            case R.id.seekA:
                Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, temp);
                DeviceModule.setProbeTemperatureA(temp);
                break;
            case R.id.seekB:
                Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, temp);
                DeviceModule.setProbeTemperatureB(temp);
                break;
        }
        Config.putBoolean(Constants.SETTING_ALERT_TEMPERATURE_UNIT, Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false));
        mEventBus.post(new BleEvent(BleCommand.PROBE_TEMPERATURE_CHANGE));
    }
}
