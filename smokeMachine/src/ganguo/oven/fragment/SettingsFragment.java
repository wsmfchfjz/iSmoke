package ganguo.oven.fragment;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import ganguo.oven.AppContext;
import ganguo.oven.Config;
import ganguo.oven.Constants;
import ganguo.oven.R;
import ganguo.oven.activity.MainActivity;
import ganguo.oven.bluetooth.BleCommand;
import ganguo.oven.bluetooth.BleEvent;
import ganguo.oven.bluetooth.SettingData;
import ganguo.oven.event.OnSingleClickListener;
import ganguo.oven.utils.AndroidUtils;

/**
 * Created by Wilson on 14-7-9.
 */
public class SettingsFragment extends BaseFragment {

    private ImageButton fromSettingToHomeBtn;
    private ImageView iv_temperature_unit;
    private View moreGroup1, moreGroup2, moreGroup3, moreGroup4, moreGroup5;
    private MainActivity mainActivity;
    private EventBus eventBus = EventBus.getDefault();


    @Override
    public int getLayoutId() {
        return R.layout.fragment_setting;
    }

    @Override
    public String getMyTAG() {
        return "SettingsFragment";
    }

    @Override
    public void beforeInitView() {
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void initView() {
        moreGroup1 = getView().findViewById(R.id.moreGroup1);
        moreGroup2 = getView().findViewById(R.id.moreGroup2);
        moreGroup3 = getView().findViewById(R.id.moreGroup3);
        moreGroup4 = getView().findViewById(R.id.moreGroup4);
        moreGroup5 = getView().findViewById(R.id.moreGroup5);

        fromSettingToHomeBtn = (ImageButton) getView().findViewById(R.id.fromSettingToHomeBtn);

        iv_temperature_unit = (ImageView) getView().findViewById(R.id.iv_temperature_unit);

        if (Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false)) {
            // ℃
            iv_temperature_unit.setImageResource(R.drawable.more_button_oc);
        } else {
            // ℉
            iv_temperature_unit.setImageResource(R.drawable.more_button_of);
        }

        String version = AndroidUtils.getAppVersionName(getActivity());
        ((TextView) getView().findViewById(R.id.tv_version)).setText("Version " + version);
    }

    @Override
    public void initListener() {
        fromSettingToHomeBtn.setOnClickListener(singleClickListener);
        iv_temperature_unit.setOnClickListener(singleClickListener);
        moreGroup1.setOnClickListener(singleClickListener);
        moreGroup2.setOnClickListener(singleClickListener);
        moreGroup3.setOnClickListener(singleClickListener);
        moreGroup4.setOnClickListener(singleClickListener);
        moreGroup5.setOnClickListener(singleClickListener);
    }

    @Override
    public void initData() {


    }

    private OnSingleClickListener singleClickListener = new OnSingleClickListener() {

        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.fromSettingToHomeBtn:
//                    mainActivity.backToHomeSlide();
                	mainActivity.changeToMain();
                    return;
                case R.id.iv_temperature_unit:
                    if (Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false)) {
                        // ℉
                        iv_temperature_unit.setImageResource(R.drawable.more_button_of);
                        Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, false);

                        SettingData setting = AppContext.getInstance().getSettingData();
                        setting.setCommand((byte) 0x03);
                        setting.setCommandData((short) 0x00);
                        eventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
                    } else {
                        // ℃
                        iv_temperature_unit.setImageResource(R.drawable.more_button_oc);
                        Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, true);

                        SettingData setting = AppContext.getInstance().getSettingData();
                        setting.setCommand((byte) 0x03);
                        setting.setCommandData((short) 0x01);
                        eventBus.post(new BleEvent(BleCommand.SETTING_COMMAND, setting));
                    }
                    break;
                case R.id.moreGroup1:
                    // Bradley Home: www.bradleysmoker.com

                    Uri uri = Uri.parse("http://www.bradleysmoker.com");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);

                    break;
                case R.id.moreGroup2:
                    // Recipes: https://www.north-america.bradleysmoker.com/recipes/
//                    Uri uri2 = Uri.parse("");
//                    Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
//                    startActivity(intent2);

                    break;
                case R.id.moreGroup3:

                    Uri uri3 = Uri.parse("https://www.north-america.bradleysmoker.com/recipes/");
                    Intent intent3 = new Intent(Intent.ACTION_VIEW, uri3);
                    startActivity(intent3);

                    break;
                case R.id.moreGroup4:

                    Intent it = new Intent(Intent.ACTION_CALL);
                    it.setData(Uri.parse("tel:10086"));
                    startActivity(it);

                    break;
                case R.id.moreGroup5:
                    Intent data = new Intent(Intent.ACTION_SENDTO);
                    data.setData(Uri.parse("mailto:1@qq.com"));
                    data.putExtra(Intent.EXTRA_SUBJECT, "");
                    data.putExtra(Intent.EXTRA_TEXT, "");
                    startActivity(data);
                    break;
            }
        }
    };

}
