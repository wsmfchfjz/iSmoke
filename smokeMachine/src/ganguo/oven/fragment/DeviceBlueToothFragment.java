package ganguo.oven.fragment;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ganguo.oven.AppContext;
import ganguo.oven.R;
import ganguo.oven.activity.DeviceActivity;
import ganguo.oven.utils.UIUtils;

/**
 * Created by Wilson on 14-7-9.
 */
public class DeviceBlueToothFragment extends BaseFragment implements View.OnClickListener {
    private DeviceActivity deviceActivity;
    private Button settingBtn;
    private Button cancelBtn;
    private Dialog scanDialog;
    private ViewGroup blueToothNotice;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_device_bluetooth;
    }

    @Override
    public String getMyTAG() {
        return "DeviceBlueToothFragment";
    }

    @Override
    public void beforeInitView() {
        deviceActivity = (DeviceActivity) getActivity();
    }

    @Override
    public void initView() {
        settingBtn = (Button) getView().findViewById(R.id.jumpSettingsBtn);
        cancelBtn = (Button) getView().findViewById(R.id.cancelBtn);
        blueToothNotice = (ViewGroup) getView().findViewById(R.id.blueToothNotice);
    }

    @Override
    public void initListener() {
        cancelBtn.setOnClickListener(this);
        settingBtn.setOnClickListener(this);
    }

    @Override
    public void initData() {
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.jumpSettingsBtn:
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivityForResult(intent, 108);
                return;
            case R.id.cancelBtn:
                getActivity().finish();
                AppContext.getInstance().exit();
                return;
        }
    }

    private void changeToDeviceList() {
        blueToothNotice.setVisibility(View.GONE);
        UIUtils.showLoading(getActivity(), "Searching for device…");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                deviceActivity.changeToDeviceList();
                UIUtils.hideLoading();
            }
        }, 2000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 108) {
            BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mAdapter.isEnabled()) {
                changeToDeviceList();
            }
        }
    }

    public void hideFragment() {
        if (blueToothNotice != null) {
            blueToothNotice.setVisibility(View.GONE);
        }
    }

}
