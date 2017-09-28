package ganguo.oven.activity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import ganguo.oven.AppContext;
import ganguo.oven.R;
import ganguo.oven.fragment.BaseFragment;
import ganguo.oven.fragment.DeviceBlueToothFragment;
import ganguo.oven.fragment.DeviceListFragment;
import ganguo.oven.utils.UIUtils;


public class DeviceActivity extends BaseActivity {
    private BaseFragment currentFragment;
    private DeviceBlueToothFragment blueToothFragment = new DeviceBlueToothFragment();
    private Dialog scanDialog;
    private View deviceFrame;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void beforeInitView() {
        setContentView(R.layout.activity_device);
    }

    @Override
    public void initView() {
        deviceFrame = findViewById(R.id.deviceFrame);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter.isEnabled()) {
            if (blueToothFragment != null) {
                blueToothFragment.hideFragment();
            }
            UIUtils.showLoading(this, "Searching for device…");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    changeToDeviceList();
                    UIUtils.hideLoading();
                }
            }, 2000);
        } else {
            replaceDeviceFragment(blueToothFragment, null);
        }
    }

    private void replaceDeviceFragment(BaseFragment fragment, Boolean slideToRightAnim) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (slideToRightAnim != null && slideToRightAnim.booleanValue()) {
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }
        if (currentFragment != null) {
            transaction.remove(currentFragment);
        }
        transaction.replace(R.id.deviceFrame, fragment, fragment.getMyTAG());
//        transaction.addToBackStack(null);
        try {
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e("", "transaction.commit", e);
            finish();
        }
        currentFragment = fragment;
    }

    public void changeToDeviceList() {
        replaceDeviceFragment(new DeviceListFragment(), true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        AppContext.getInstance().exit();
    }
}
