package ganguo.oven.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.tufan.library.TufanCircleSeekBar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import ganguo.oven.AppContext;
import ganguo.oven.Config;
import ganguo.oven.Constants;
import ganguo.oven.R;
import ganguo.oven.activity.DeviceActivity;
import ganguo.oven.activity.MainActivity;
import ganguo.oven.bluetooth.BleCommand;
import ganguo.oven.bluetooth.BleEvent;
import ganguo.oven.bluetooth.OvenModule;
import ganguo.oven.bluetooth.ReceiveData;
import ganguo.oven.bluetooth.SmokerModule;
import ganguo.oven.event.OnSingleClickListener;
import ganguo.oven.utils.UIUtils;
import ganguo.oven.view.InterceptEventView;
import ganguo.oven.view.DirectionalViewPager.PagerAdapter;
import ganguo.oven.view.DirectionalViewPager.VerticalViewPager;

/**
 * Created by Wilson on 14-7-9.
 */
@SuppressLint("ValidFragment")
public class MainFragment extends BaseFragment {
	private TextView tv_probe1_off, tv_probe1_on, tv_probe2_off, tv_probe2_on;
	private TextView remainedTime, remainedTimeLabel, currentTemperature, currentTemperatureA, currentTemperatureB, smokerTime, ovenTime;
	private TextView currentTemperatureUnit, setttingTemperatureUnit; // 温度单位
	private TextView tv_c_50, tv_c_70, tv_c_90, tv_c_110, tv_c_130, tv_c_150, tv_c_160;
	private TextView tv_f_120, tv_f_150, tv_f_180, tv_f_210, tv_f_240, tv_f_270, tv_f_300, tv_f_320;
	private TextView setTemValue, setTemValueUnit,setTemLabel;
	private TextView tv_probe1_1_word, tv_probe1_1_num, tv_probe1_2_word, tv_probe1_2_num, tv_probe1_3_word, tv_probe1_3_num;
	private TextView tv_probe2_1_word, tv_probe2_1_num, tv_probe2_2_word, tv_probe2_2_num, tv_probe2_3_word, tv_probe2_3_num;
	private Button toAlertBtn;
	private Button btn_probe1_0_of_viewpage, btn_probe1_1_of_viewpage, btn_probe1_2_of_viewpage, btn_probe1_3_of_viewpage, btn_probe1_4_of_viewpage;
	private Button btn_probe2_0_of_viewpage, btn_probe2_1_of_viewpage, btn_probe2_2_of_viewpage, btn_probe2_3_of_viewpage, btn_probe2_4_of_viewpage;
	private Button btn_probe1_arrow_top, btn_probe1_arrow_bottom;
	private Button btn_probe2_arrow_top, btn_probe2_arrow_bottom;
	private ImageButton toSettingsBtn, toChartBtn;
	private ImageView iv_time_bg, img_home_logo;
	private CheckBox actionStart, cb_probe1_on_off, cb_probe2_on_off;
	private RadioGroup rgOvenSmoke;
	private RadioButton ovenModule, smokerModule;
	private RelativeLayout frameCircle, frameProbe;
	private RelativeLayout rlt_probe1_1, rlt_probe1_2, rlt_probe1_3, rlt_probe2_1, rlt_probe2_2, rlt_probe2_3;
	private View view_time;
	private View view_temp;
	private TufanCircleSeekBar seekBarSettingTemperature, seekbar_settingTime;
	private VerticalViewPager viewpager_direction_probe1, viewpager_direction_probe2;// 垂直滑动viewpage
	private InterceptEventView interceptEvent_probe1_1, interceptEvent_probe1_2, interceptEvent_probe2_1, interceptEvent_probe2_2;
	private List<View> list_probe1, list_probe2;
	private List<RelativeLayout> rlts_probe_list;
	private List<TextView> tvs_probe_list;
	
	private MainActivity mainActivity;//63145 71160 74165 77170
	private ReceiveData mReceiveData = AppContext.getInstance().getReceiveData();
	private EventBus mEventBus = EventBus.getDefault();
	private Timer timer4InitMeat;
	
	private int temperatureUnit = 0x00;
	private int settingTemperature = 0;
	private int timerHour = 0;
	private int timerMinute = 20;
	private int[][] meatAndTempList = {{63145,71160},{63145,71160,77170},{63145},{63145,71160},{74165}};//羊，牛，鱼，猪，鸡,,,,63145 71160 74165 77170 
	private String tempUnit = "℉";
	private boolean canReturnScanListPage = false;
	private int currentIndexOfViewpagerProbe1, lastIndexOfViewpagerProbe1 = -1;
	private int currentIndexOfViewpagerProbe2, lastIndexOfViewpagerProbe2 = -1;
	private int setProbe1Temp, setProbe2Temp;
	private int ovenHour,ovenMinute,smokeHour,smokeMinute;
	private boolean isMeClickOnOffBtn1, isMeClickOnOffBtn2;

	public void onEventMainThread(BleEvent event) {
		switch (event.getCommand()) {
		case NOTIFY_RECEIVE_DATA:
			// 数据接收
			mReceiveData = (ReceiveData) event.getTarget();
			if (mReceiveData == null)
				return;
			refreshData();
			break;
		case DISCONNECTED:
			// 连接断开
			OvenModule.setOvenStatus(false);
			SmokerModule.setSmokerStatus(false);
			actionStart.setChecked(false);
			setOvenEnable(false);
			break;
		case STOP_RECODE:
			toChartBtn.setVisibility(View.GONE);
			actionStart.setText("Start");
			break;
		case START_RECODE:
			toChartBtn.setVisibility(View.VISIBLE);
			actionStart.setText("Stop");
			setSeekBarSettingTemperatureStyle(false);
			break;
		case PROBE_TEMPERATURE_CHANGE:
			refreshProbeTemp();
			break;
		case SCAN_FOUND:
			BluetoothDevice device = (BluetoothDevice) event.getTarget();
			Log.i("smoke", "scan name:"+device.getName());
			if(device.getName() != null && device.getName().startsWith(Constants.DEVICE_NAME)){//找到了烟熏机
				//停止搜索
				EventBus.getDefault().post(new BleEvent(BleCommand.SCAN_STOP));
				Log.i("smoke", "true name!!!!! - "+device.getName());
				//蓝牙icon出现，其实就是换图片
				img_home_logo.setImageResource(R.drawable.bluetooth);
				canReturnScanListPage = true;
			}
			break;
		}
	}
	/*-----------------------------------------------------------------------------------------实现接口-----------------------------------------------------------------------------------------------*/
	@Override
	public void onStart() {
		super.onStart();
		mEventBus.register(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		mEventBus.unregister(this);
	}

	@Override
	public int getLayoutId() {
		return R.layout.fragment_main;
	}

	@Override
	public String getMyTAG() {
		return "MainFragment";
	}

	@Override
	public void beforeInitView() {
		mainActivity = (MainActivity) getActivity();
	}

	@Override
	public void initView() {
		view_time = getView().findViewById(R.id.view_time);
		view_temp = getView().findViewById(R.id.view_temp);

		setTemValue = (TextView) getView().findViewById(R.id.setTemValue);
		setTemValueUnit = (TextView) getView().findViewById(R.id.setTemValueUnit);
		setTemLabel = (TextView) getView().findViewById(R.id.setTemLabel);
		
		tv_c_50 = (TextView) getView().findViewById(R.id.tv_c_50);
		tv_c_70 = (TextView) getView().findViewById(R.id.tv_c_70);
		tv_c_90 = (TextView) getView().findViewById(R.id.tv_c_90);
		tv_c_110 = (TextView) getView().findViewById(R.id.tv_c_110);
		tv_c_130 = (TextView) getView().findViewById(R.id.tv_c_130);
		tv_c_150 = (TextView) getView().findViewById(R.id.tv_c_150);
		tv_c_160 = (TextView) getView().findViewById(R.id.tv_c_160);

		tv_f_120 = (TextView) getView().findViewById(R.id.tv_f_120);
		tv_f_150 = (TextView) getView().findViewById(R.id.tv_f_150);
		tv_f_180 = (TextView) getView().findViewById(R.id.tv_f_180);
		tv_f_210 = (TextView) getView().findViewById(R.id.tv_f_210);
		tv_f_240 = (TextView) getView().findViewById(R.id.tv_f_240);
		tv_f_270 = (TextView) getView().findViewById(R.id.tv_f_270);
		tv_f_300 = (TextView) getView().findViewById(R.id.tv_f_300);
		tv_f_320 = (TextView) getView().findViewById(R.id.tv_f_320);

		toSettingsBtn = (ImageButton) getView().findViewById(R.id.toSettingsBtn);
		toChartBtn = (ImageButton) getView().findViewById(R.id.toChart);
		toAlertBtn = (Button) getView().findViewById(R.id.toAlertBtn);
		remainedTime = (TextView) getView().findViewById(R.id.remainedTime);
		remainedTimeLabel = (TextView) getView().findViewById(R.id.remainedTimeLabel);
		seekBarSettingTemperature = (TufanCircleSeekBar) getView().findViewById(R.id.seekbar_settingTemperature);
		seekbar_settingTime = (TufanCircleSeekBar) getView().findViewById(R.id.seekbar_settingTime);
		iv_time_bg = (ImageView) getView().findViewById(R.id.iv_time_bg);
		img_home_logo = (ImageView) getView().findViewById(R.id.img_home_logo);
		currentTemperature = (TextView) getView().findViewById(R.id.currentTemperature);
		currentTemperatureA = (TextView) getView().findViewById(R.id.currentTemperatureA);
		currentTemperatureB = (TextView) getView().findViewById(R.id.currentTemperatureB);
		smokerTime = (TextView) getView().findViewById(R.id.smokerTime);
		ovenTime = (TextView) getView().findViewById(R.id.ovenTime);
		rgOvenSmoke = (RadioGroup) getView().findViewById(R.id.rgOvenSmoke);
		ovenModule = (RadioButton) getView().findViewById(R.id.ovenModule);
		smokerModule = (RadioButton) getView().findViewById(R.id.smokerModule);
		actionStart = (CheckBox) getView().findViewById(R.id.actionStart);

		// 温度单位
		currentTemperatureUnit = (TextView) getView().findViewById(R.id.currentTemperatureUnit);
		setttingTemperatureUnit = (TextView) getView().findViewById(R.id.setttingTemperatureUnit);

		frameCircle = (RelativeLayout) getView().findViewById(R.id.frameCircle);
		frameProbe = (RelativeLayout) getView().findViewById(R.id.frameProbe);

		cb_probe1_on_off = (CheckBox) getView().findViewById(R.id.cb_probe1_on_off);
		cb_probe2_on_off = (CheckBox) getView().findViewById(R.id.cb_probe2_on_off);
		tv_probe1_off = (TextView) getView().findViewById(R.id.tv_probe1_off);
		tv_probe1_on = (TextView) getView().findViewById(R.id.tv_probe1_on);
		tv_probe2_off = (TextView) getView().findViewById(R.id.tv_probe2_off);
		tv_probe2_on = (TextView) getView().findViewById(R.id.tv_probe2_on);

		rlt_probe1_1 = (RelativeLayout) getView().findViewById(R.id.rlt_probe1_1);
		rlt_probe1_2 = (RelativeLayout) getView().findViewById(R.id.rlt_probe1_2);
		rlt_probe1_3 = (RelativeLayout) getView().findViewById(R.id.rlt_probe1_3);
		rlt_probe2_1 = (RelativeLayout) getView().findViewById(R.id.rlt_probe2_1);
		rlt_probe2_2 = (RelativeLayout) getView().findViewById(R.id.rlt_probe2_2);
		rlt_probe2_3 = (RelativeLayout) getView().findViewById(R.id.rlt_probe2_3);

		rlts_probe_list = new ArrayList<RelativeLayout>();
		rlts_probe_list.add(rlt_probe1_1);
		rlts_probe_list.add(rlt_probe1_2);
		rlts_probe_list.add(rlt_probe1_3);
		rlts_probe_list.add(rlt_probe2_1);
		rlts_probe_list.add(rlt_probe2_2);
		rlts_probe_list.add(rlt_probe2_3);

		tv_probe1_1_word = (TextView) getView().findViewById(R.id.tv_probe1_1_word);
		tv_probe1_1_num = (TextView) getView().findViewById(R.id.tv_probe1_1_num);
		tv_probe1_2_word = (TextView) getView().findViewById(R.id.tv_probe1_2_word);
		tv_probe1_2_num = (TextView) getView().findViewById(R.id.tv_probe1_2_num);
		tv_probe1_3_word = (TextView) getView().findViewById(R.id.tv_probe1_3_word);
		tv_probe1_3_num = (TextView) getView().findViewById(R.id.tv_probe1_3_num);

		tv_probe2_1_word = (TextView) getView().findViewById(R.id.tv_probe2_1_word);
		tv_probe2_1_num = (TextView) getView().findViewById(R.id.tv_probe2_1_num);
		tv_probe2_2_word = (TextView) getView().findViewById(R.id.tv_probe2_2_word);
		tv_probe2_2_num = (TextView) getView().findViewById(R.id.tv_probe2_2_num);
		tv_probe2_3_word = (TextView) getView().findViewById(R.id.tv_probe2_3_word);
		tv_probe2_3_num = (TextView) getView().findViewById(R.id.tv_probe2_3_num);

		tvs_probe_list = new ArrayList<TextView>();
		tvs_probe_list.add(tv_probe1_1_word);
		tvs_probe_list.add(tv_probe1_1_num);
		tvs_probe_list.add(tv_probe1_2_word);
		tvs_probe_list.add(tv_probe1_2_num);
		tvs_probe_list.add(tv_probe1_3_word);
		tvs_probe_list.add(tv_probe1_3_num);
		tvs_probe_list.add(tv_probe2_1_word);
		tvs_probe_list.add(tv_probe2_1_num);
		tvs_probe_list.add(tv_probe2_2_word);
		tvs_probe_list.add(tv_probe2_2_num);
		tvs_probe_list.add(tv_probe2_3_word);
		tvs_probe_list.add(tv_probe2_3_num);

		interceptEvent_probe1_1 = (InterceptEventView) getView().findViewById(R.id.interceptEvent_probe1_1);
		interceptEvent_probe1_2 = (InterceptEventView) getView().findViewById(R.id.interceptEvent_probe1_2);
		interceptEvent_probe2_1 = (InterceptEventView) getView().findViewById(R.id.interceptEvent_probe2_1);
		interceptEvent_probe2_2 = (InterceptEventView) getView().findViewById(R.id.interceptEvent_probe2_2);

		viewpager_direction_probe1 = (VerticalViewPager) getView().findViewById(R.id.viewpager_direction_probe1);
		viewpager_direction_probe2 = (VerticalViewPager) getView().findViewById(R.id.viewpager_direction_probe2);

		View[] meatImgList = MainFragmentUtil.getMeatImg(mainActivity);
		
		list_probe1 = new ArrayList<View>();
		list_probe1.add(meatImgList[0]);
		list_probe1.add(meatImgList[1]);
		list_probe1.add(meatImgList[2]);
		list_probe1.add(meatImgList[3]);
		list_probe1.add(meatImgList[4]);

		list_probe2 = new ArrayList<View>();
		list_probe2.add(meatImgList[5]);
		list_probe2.add(meatImgList[6]);
		list_probe2.add(meatImgList[7]);
		list_probe2.add(meatImgList[8]);
		list_probe2.add(meatImgList[9]);

		viewpager_direction_probe1.setAdapter(new MyAdapter(1));
		viewpager_direction_probe2.setAdapter(new MyAdapter(2));

		btn_probe1_0_of_viewpage = (Button) getView().findViewById(R.id.btn_probe1_0_of_viewpage);
		btn_probe1_1_of_viewpage = (Button) getView().findViewById(R.id.btn_probe1_1_of_viewpage);
		btn_probe1_2_of_viewpage = (Button) getView().findViewById(R.id.btn_probe1_2_of_viewpage);
		btn_probe1_3_of_viewpage = (Button) getView().findViewById(R.id.btn_probe1_3_of_viewpage);
		btn_probe1_4_of_viewpage = (Button) getView().findViewById(R.id.btn_probe1_4_of_viewpage);

		btn_probe2_0_of_viewpage = (Button) getView().findViewById(R.id.btn_probe2_0_of_viewpage);
		btn_probe2_1_of_viewpage = (Button) getView().findViewById(R.id.btn_probe2_1_of_viewpage);
		btn_probe2_2_of_viewpage = (Button) getView().findViewById(R.id.btn_probe2_2_of_viewpage);
		btn_probe2_3_of_viewpage = (Button) getView().findViewById(R.id.btn_probe2_3_of_viewpage);
		btn_probe2_4_of_viewpage = (Button) getView().findViewById(R.id.btn_probe2_4_of_viewpage);

		btn_probe1_arrow_top = (Button) getView().findViewById(R.id.btn_probe1_arrow_top);
		btn_probe1_arrow_bottom = (Button) getView().findViewById(R.id.btn_probe1_arrow_bottom);

		btn_probe2_arrow_top = (Button) getView().findViewById(R.id.btn_probe2_arrow_top);
		btn_probe2_arrow_bottom = (Button) getView().findViewById(R.id.btn_probe2_arrow_bottom);

		
	}

	@Override
	public void initListener() {
		toSettingsBtn.setOnClickListener(singleClickListener);
		toChartBtn.setOnClickListener(singleClickListener);
		toAlertBtn.setOnClickListener(singleClickListener);
		remainedTime.setOnClickListener(singleClickListener);
		remainedTimeLabel.setOnClickListener(singleClickListener);

		rlt_probe1_1.setOnClickListener(singleClickListener);
		rlt_probe1_2.setOnClickListener(singleClickListener);
		rlt_probe1_3.setOnClickListener(singleClickListener);
		rlt_probe2_1.setOnClickListener(singleClickListener);
		rlt_probe2_2.setOnClickListener(singleClickListener);
		rlt_probe2_3.setOnClickListener(singleClickListener);
		
		img_home_logo.setOnClickListener(singleClickListener);

		rgOvenSmoke.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.rbtn_probe) {
					frameProbe.setVisibility(View.VISIBLE);//显示探针界面
					frameCircle.setVisibility(View.INVISIBLE);
					actionStart.setVisibility(View.INVISIBLE);
				} else {
					frameCircle.setVisibility(View.VISIBLE);//显示圆圈控件界面
					frameProbe.setVisibility(View.INVISIBLE);
					actionStart.setVisibility(View.VISIBLE);
					AlphaAnimation aa = new AlphaAnimation(0, 1);
					aa.setDuration(400);
					if (checkedId == R.id.ovenModule) {//点击烤箱
						view_time.setVisibility(View.GONE);//隐藏时间控件
						view_temp.startAnimation(aa);
						view_temp.setVisibility(View.VISIBLE);//显示温度控件
						remainedTime.setBackgroundResource(R.drawable.set_time);
						remainedTimeLabel.setText("Time");
						setCircleTime();
						
						// 转到oven模块
						if (mReceiveData.getOvenStatus() == 0x01) {
							actionStart.setChecked(true);//变成stop
							OvenModule.setOvenStatus(true);//存储当前oven打开状态
						} else {
							setOvenEnable(true);//为了避免去到烟熏界面会把温度控件变成灰色，所以一旦点击烤箱按钮，先把温度控件变正常
							actionStart.setChecked(false);//变成start
						}
						
						if(Constants.isTestMode){
							int ovenHour = Config.getInt(Constants.OVEN_SETTING_TIMER_HOUR);
							int ovenMinute = Config.getInt(Constants.OVEN_SETTING_TIMER_MINUTE);
							remainedTime.setText(UIUtils.formateTime(ovenHour) + "h " + UIUtils.formateTime(ovenMinute) + "min");
						}
						
					} else {//点击烟熏
						view_temp.setVisibility(View.GONE);
						view_time.startAnimation(aa);
						view_time.setVisibility(View.VISIBLE);
						remainedTime.setBackgroundResource(R.drawable.set_temp);
						remainedTimeLabel.setText("Temp");
						remainedTime.setText(seekBarSettingTemperature.getProgress() + tempUnit);
						if(Constants.isTestMode){
							if (Config.getString(Constants.LAST_TEMP_UNIT).equals("℃")) {
								remainedTime.setText(seekBarSettingTemperature.getProgress() + "℃");
							} else {
								remainedTime.setText(seekBarSettingTemperature.getProgress() + "℉");
							}
						}
						setCircleTime();
						
						// 转到smoker模块
						if (mReceiveData.getSmokedStatus() == 0x01) {
							actionStart.setChecked(true);
							SmokerModule.setSmokerStatus(true);
						} else {
							actionStart.setChecked(false);
						}
						setSeekBarSettingTemperatureStyle(false);//不能点击温度圆圈控件
						
					}
					// 切换时间刻度
					if (ovenModule.isChecked()) {
						iv_time_bg.setBackgroundResource(R.drawable.bg_circle_time);
					} else {
						iv_time_bg.setBackgroundResource(R.drawable.bg_circle_time2);
					}
				}
			}
		});
		cb_probe1_on_off.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isMeClickOnOffBtn1 = true;
				Log.e("testLog", "isMeClickOnOffBtn1:"+isMeClickOnOffBtn1);
			}
		});
		cb_probe1_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if (isChecked) {
					setProbe1Temp = Config.getInt(Constants.LAST_MEAT_TEMP_A);
					tv_probe1_off.setVisibility(View.GONE);
					tv_probe1_on.setVisibility(View.VISIBLE);
					interceptEvent_probe1_1.setVisibility(View.VISIBLE);
					interceptEvent_probe1_2.setVisibility(View.VISIBLE);
					new Timer().schedule(new TimerTask() {//setOnCheckedChangeListener先执行，setOnClickListener后执行
						@Override
						public void run() {
							Log.e("testLog", "isChecked:true - isMeClickOnOffBtn1:"+isMeClickOnOffBtn1);
							if(isMeClickOnOffBtn1){//是自己点击的，才发送命令
								isMeClickOnOffBtn1 = false;
								OvenModule.setProbeTempAndMeat1(setProbe1Temp, currentIndexOfViewpagerProbe1 + 1, currentIndexOfViewpagerProbe2 + 1, mReceiveData.getTemperatureUnit());
							}
						}
					}, 200);
				} else {
					setProbe1Temp = Constants.closeProbeTemp;
					tv_probe1_off.setVisibility(View.VISIBLE);
					tv_probe1_on.setVisibility(View.GONE);
					interceptEvent_probe1_1.setVisibility(View.GONE);
					interceptEvent_probe1_2.setVisibility(View.GONE);
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							Log.e("testLog", "isChecked:false - isMeClickOnOffBtn1:"+isMeClickOnOffBtn1);
							if(isMeClickOnOffBtn1){//是自己点击的，才发送命令
								isMeClickOnOffBtn1 = false;
								OvenModule.setProbeTempAndMeat1(Constants.closeProbeTemp, 0, currentIndexOfViewpagerProbe2 + 1, mReceiveData.getTemperatureUnit());
							}
						}
					}, 200);
				}
			}
		});
		cb_probe2_on_off.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isMeClickOnOffBtn2 = true;
				Log.e("testLog", "isMeClickOnOffBtn2:"+isMeClickOnOffBtn1);
			}
		});
		cb_probe2_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if (isChecked) {
					tv_probe2_off.setVisibility(View.GONE);
					tv_probe2_on.setVisibility(View.VISIBLE);
					setProbe2Temp = Config.getInt(Constants.LAST_MEAT_TEMP_B);
					interceptEvent_probe2_1.setVisibility(View.VISIBLE);
					interceptEvent_probe2_2.setVisibility(View.VISIBLE);
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							Log.e("testLog", "isChecked:true - isMeClickOnOffBtn2:"+isMeClickOnOffBtn1);
							if(isMeClickOnOffBtn2){//是自己点击的，才发送命令
								isMeClickOnOffBtn2 = false;
								OvenModule.setProbeTempAndMeat2(setProbe2Temp, currentIndexOfViewpagerProbe1 + 1, currentIndexOfViewpagerProbe2 + 1, mReceiveData.getTemperatureUnit());
							}
						}
					}, 200);
				} else {
					setProbe2Temp = Constants.closeProbeTemp;
					tv_probe2_off.setVisibility(View.VISIBLE);
					tv_probe2_on.setVisibility(View.GONE);
					interceptEvent_probe2_1.setVisibility(View.GONE);
					interceptEvent_probe2_2.setVisibility(View.GONE);
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							Log.e("testLog", "isChecked:false - isMeClickOnOffBtn2:"+isMeClickOnOffBtn1);
							if(isMeClickOnOffBtn2){//是自己点击的，才发送命令
								isMeClickOnOffBtn2 = false;
								OvenModule.setProbeTempAndMeat2(Constants.closeProbeTemp, currentIndexOfViewpagerProbe1 + 1, 0, mReceiveData.getTemperatureUnit());
							}
						}
					}, 200);
				}
			}
		});
		actionStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 当前是oven模块
				if (ovenModule.isChecked()) {
					if (mReceiveData.getOvenStatus() == 0x00 || actionStart.isChecked()) {
						// on
						OvenModule.startOven();
						ovenHour = Config.getInt(Constants.OVEN_SETTING_TIMER_HOUR);
						ovenMinute = Config.getInt(Constants.OVEN_SETTING_TIMER_MINUTE);
					} else {
						// off
						OvenModule.stopOven();
					}
				} else {
					// 当前是smoker模块
					if (mReceiveData.getSmokedStatus() == 0x00 || actionStart.isChecked()) {
						// on
						SmokerModule.startOven();
						smokeHour = Config.getInt(Constants.SMOKER_SETTING_TIMER_HOUR);
						smokeMinute = Config.getInt(Constants.SMOKER_SETTING_TIMER_MINUTE);
					} else {
						// off
						SmokerModule.stopOven();
					}
				}
				setCircleTime();
			}
		});
		actionStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					actionStart.setText("Stop");//按键stop的时候，圆圈都是不能点击的
					setSmokerEnable(false);
					setOvenEnable(false);
				} else {
					actionStart.setText("Start");

					if (ovenModule.isChecked()) {
						setOvenEnable(true);
					} else if (smokerModule.isChecked()) {
						setSmokerEnable(true);
					}
				}
			}
		});
		seekBarSettingTemperature.setOnStopTrackingTouchListener(new TufanCircleSeekBar.OnStopTrackingTouchListener() {
			@Override
			public void onStopTrackingTouch(TufanCircleSeekBar seekbar) {
				// 设置oven温度
				OvenModule.setOvenTemperature(seekbar.getProgress());

				// 记录，待恢复同步
				settingTemperature = seekbar.getProgress();
			}
		});
		seekbar_settingTime.setOnStopTrackingTouchListener(new TufanCircleSeekBar.OnStopTrackingTouchListener() {
			@Override
			public void onStopTrackingTouch(TufanCircleSeekBar seekbar) {
				int min = seekbar.getProgress();
				timerHour = min / 60;
				if (min >= 60) {
					timerMinute = min - timerHour * 60;
				} else {
					timerMinute = min;
				}
				
				if(ovenModule.isChecked()){
					OvenModule.setOvenTime(timerHour, timerMinute);
				}else if(smokerModule.isChecked()){
					SmokerModule.setSmokerTime(timerHour, timerMinute);
				}
			}
		});
	}
	
	private OnSingleClickListener singleClickListener = new OnSingleClickListener() {
		@Override
		public void onSingleClick(View v) {
			switch (v.getId()) {
			case R.id.rlt_probe1_1:
				if (currentIndexOfViewpagerProbe1 != 4) {// 不是最后一个
					setProbe1Temp = 63145;
				} else {
					setProbe1Temp = 74165;
				}
				Config.putInt(Constants.LAST_MEAT_TEMP_A, setProbe1Temp);
				setProbeTextRed(true, 1);
				return;
			case R.id.rlt_probe1_2:
				setProbe1Temp = 71160;
				Config.putInt(Constants.LAST_MEAT_TEMP_A, setProbe1Temp);
				setProbeTextRed(true, 2);
				return;
			case R.id.rlt_probe1_3:
				setProbe1Temp = 77170;
				Config.putInt(Constants.LAST_MEAT_TEMP_A, setProbe1Temp);
				setProbeTextRed(true, 3);
				return;
			case R.id.rlt_probe2_1:
				if (currentIndexOfViewpagerProbe2 != 4) {// 不是最后一个
					setProbe2Temp = 63145;
				} else {
					setProbe2Temp = 74165;
				}
				Config.putInt(Constants.LAST_MEAT_TEMP_B, setProbe2Temp);
				setProbeTextRed(false, 1);
				return;
			case R.id.rlt_probe2_2:
				setProbe2Temp = 71160;
				Config.putInt(Constants.LAST_MEAT_TEMP_B, setProbe2Temp);
				setProbeTextRed(false, 2);
				return;
			case R.id.rlt_probe2_3:
				setProbe2Temp = 77170;
				Config.putInt(Constants.LAST_MEAT_TEMP_B, setProbe2Temp);
				setProbeTextRed(false, 3);
				return;
			case R.id.toSettingsBtn:
				mainActivity.changeToSettings();
				return;
			case R.id.toChart:
				mainActivity.changeToChart();
				return;
			case R.id.toAlertBtn:
				mainActivity.changeToAlert();
				return;
			case R.id.remainedTime:
			case R.id.remainedTimeLabel:
				if (!Constants.isTestMode) {
					AlphaAnimation aa = new AlphaAnimation(0, 1);
					aa.setDuration(400);
					if (view_time.getVisibility() == View.VISIBLE) {
						view_time.setVisibility(View.GONE);
						view_temp.startAnimation(aa);
						view_temp.setVisibility(View.VISIBLE);
						remainedTime.setBackgroundResource(R.drawable.set_time);
						remainedTimeLabel.setText("Time");
						setCircleTime();
					} else {
						view_temp.setVisibility(View.GONE);
						view_time.startAnimation(aa);
						view_time.setVisibility(View.VISIBLE);
						remainedTime.setBackgroundResource(R.drawable.set_temp);
						remainedTimeLabel.setText("Temp");
						remainedTime.setText(seekBarSettingTemperature.getProgress() + tempUnit);
					}
				}
				return;
			case R.id.img_home_logo:
				if(Constants.isTestMode && canReturnScanListPage){
					Intent it = new Intent(getActivity(), DeviceActivity.class);//跳回搜索设备页面
					startActivity(it);
				}
				return;
			}
		}
	};
	
	@Override
	public void initData() {
		mReceiveData = AppContext.getInstance().getReceiveData();
		if(!Constants.isTestMode){
			initFirstData();
		}

		timerHour = ovenHour = Config.getInt(Constants.OVEN_SETTING_TIMER_HOUR);
		timerMinute = ovenHour = Config.getInt(Constants.OVEN_SETTING_TIMER_MINUTE);
		if (timerMinute == 0)
			timerMinute = 20;

		// 设置当前时间
		seekbar_settingTime.setProgress(timerHour * 60 + timerMinute);

		Log.e("testLog", "initData timerHour:"+timerHour + "timerMinute:"+timerMinute);
		remainedTime.setText(UIUtils.formateTime(timerHour) + "h " + UIUtils.formateTime(timerMinute) + "min");
		if (Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false)) {
			seekBarSettingTemperature.setStartProgress(50);
			seekBarSettingTemperature.setMaxProgress(110);
		} else {
			seekBarSettingTemperature.setStartProgress(120);
			seekBarSettingTemperature.setMaxProgress(200);
		}
		refreshProbeTemp();
		
		timer4InitMeat = new Timer();
		timer4InitMeat.schedule(new TimerTask() {
			@Override
			public void run() {// 延时设置红色高亮，不延时的话，会被大约50ms后的白色高亮覆盖掉
				reFreshProbeView();
				if (Constants.isTestMode) {
//					EventBus.getDefault().post(new BleEvent(BleCommand.START_SCAN_THREAD));//开启后台搜索线程
					timer4InitMeat.cancel();
					cb_probe1_on_off.setEnabled(false);
					cb_probe1_on_off.setChecked(false);
					cb_probe2_on_off.setEnabled(false);
					cb_probe2_on_off.setChecked(false);
//					interceptEvent_probe1_1.setVisibility(View.VISIBLE);
//					interceptEvent_probe1_2.setVisibility(View.VISIBLE);
//					interceptEvent_probe2_1.setVisibility(View.VISIBLE);
//					interceptEvent_probe2_2.setVisibility(View.VISIBLE);
					
					actionStart.setEnabled(false);
					remainedTime.setEnabled(false);
					remainedTimeLabel.setEnabled(false);
					setSeekBarSettingTemperatureStyle(false);
					setSeekBarSettingTimeStyle(false);
					
					if(Config.getString(Constants.LAST_OVEN_TIME) != null){
						ovenTime.setText(Config.getString(Constants.LAST_OVEN_TIME));
						
						int ovenHour = Config.getInt(Constants.OVEN_SETTING_TIMER_HOUR);
						int ovenMinute = Config.getInt(Constants.OVEN_SETTING_TIMER_MINUTE);
						remainedTime.setText(UIUtils.formateTime(ovenHour) + "h " + UIUtils.formateTime(ovenMinute) + "min");
					}
					if(Config.getString(Constants.LAST_SMOKE_TIME) != null){
						smokerTime.setText(Config.getString(Constants.LAST_SMOKE_TIME));
						
						int smokeHour = Config.getInt(Constants.SMOKER_SETTING_TIMER_HOUR);
						int smokeMinute = Config.getInt(Constants.SMOKER_SETTING_TIMER_MINUTE);
						seekbar_settingTime.setProgress(smokeHour * 60 + smokeMinute);
					}
					if(Config.getString(Constants.LAST_TEMP) != null){
						currentTemperature.setText(Config.getString(Constants.LAST_TEMP));
					}
					if(Config.getString(Constants.LAST_TEMP_A) != null){
						currentTemperatureA.setText(Config.getString(Constants.LAST_TEMP_A));
					}
					if(Config.getString(Constants.LAST_TEMP_B) != null){
						currentTemperatureB.setText(Config.getString(Constants.LAST_TEMP_B));
					}
					if(Config.getString(Constants.LAST_TEMP_UNIT) != null){
						setTemValueUnit.setText(Config.getString(Constants.LAST_TEMP_UNIT));
						if (Config.getString(Constants.LAST_TEMP_UNIT).equals("℃")) {
							Log.i("testLog", "50!!!");
							seekBarSettingTemperature.setStartProgress(50);
							seekBarSettingTemperature.setMaxProgress(110);
							setFAndC(View.GONE, View.VISIBLE);
							setttingTemperatureUnit.setText("℃");
							currentTemperatureUnit.setText("℃");
						} else {
							seekBarSettingTemperature.setStartProgress(120);
							seekBarSettingTemperature.setMaxProgress(200);
							setFAndC(View.VISIBLE, View.GONE);
							setttingTemperatureUnit.setText("℉");
							currentTemperatureUnit.setText("℉");
						}
					}
					if(Config.getInt(Constants.LAST_SETTING_TEMP) != 0){
						seekBarSettingTemperature.setProgress(Config.getInt(Constants.LAST_SETTING_TEMP));
					}
				}
			}
		}, 150);
	}
	
	private void initFirstData() {
		temperatureUnit = mReceiveData.getTemperatureUnit();
		settingTemperature = mReceiveData.getSettingTemperature();
		setTemperatureProgress();
	}
	
	/*----------------------------------------------------------------------------------收到数据后立即执行方法--------------------------------------------------------------------------------------------*/
	
	private void refreshData() {// 收到数据则执行
		refreshMeat();
		refreshOvenAndSmoker();
		refreshTimer();
		refreshByTempUnit();
		refreshProbeTemp();

		// 烤箱设置温度改变
		if (mReceiveData.getSettingTemperature() != settingTemperature && mReceiveData.getOvenStatus() == 1) {
			settingTemperature = mReceiveData.getSettingTemperature();
			setTemperatureProgress();
		}
		currentTemperature.setText(mReceiveData.getProbeTemperature() + "");
		Config.putString(Constants.LAST_TEMP, mReceiveData.getProbeTemperature() + "");
	}
	
	private void refreshMeat(){//更新肉类
		int meatSelection = mReceiveData.getMeatSelection();
		int tempAMeat = meatSelection / 16;
		int tempBMeat = meatSelection % 16;
		int[] tempMeatPair = {4,2,5,1,3};
		
		if(tempAMeat >= 1 && tempAMeat <= 5){
			Config.putInt(Constants.SETTING_MEAT_A, tempMeatPair[tempAMeat-1]);
		}
		if(tempBMeat >= 1 && tempBMeat <= 5){
			Config.putInt(Constants.SETTING_MEAT_B, tempMeatPair[tempBMeat-1]);
		}
	}
	
	private void refreshOvenAndSmoker(){
		if (ovenModule.isChecked()) {
			if (mReceiveData.getOvenStatus() == 0x01) {
				OvenModule.setOvenStatus(true);
				actionStart.setChecked(true);
				setOvenEnable(false);
				if("Time".equals(remainedTimeLabel.getText().toString())){
					//读取设备的烤箱设置时间，然后显示到界面上
					if(mReceiveData.getOvenHourTime() >= 128 && mReceiveData.getOvenMinuteTime() >= 128){
						ovenHour = mReceiveData.getOvenHourTime() - 128;
						ovenMinute = mReceiveData.getOvenMinuteTime() - 128;
						Config.putInt(Constants.OVEN_SETTING_TIMER_HOUR, ovenHour);
						Config.putInt(Constants.OVEN_SETTING_TIMER_MINUTE, ovenMinute);
						remainedTime.setText(UIUtils.formateTime(ovenHour) + "h " + UIUtils.formateTime(ovenMinute) + "min");
						seekbar_settingTime.setProgress(ovenHour * 60 + ovenMinute);
					}
				} else {
					remainedTime.setText(seekBarSettingTemperature.getProgress() + tempUnit);
				}
			} else {
				OvenModule.setOvenStatus(false);
				actionStart.setChecked(false);
				setOvenEnable(true);
			}
		} else {
//			remainedTime.setText(seekBarSettingTemperature.getProgress() + tempUnit);
			if (mReceiveData.getSmokedStatus() == 0x01) {
				SmokerModule.setSmokerStatus(true);
				actionStart.setChecked(true);
				setSmokerEnable(false);//读取设备的烟熏设置时间，然后显示到界面上
				if("Time".equals(remainedTimeLabel.getText().toString())){
					if(mReceiveData.getSmokedHourTime() >= 128 && mReceiveData.getSmokedMinuteTime() >= 128){
						smokeHour = mReceiveData.getSmokedHourTime() - 128;
						smokeMinute = mReceiveData.getSmokedMinuteTime() - 128;
						Config.putInt(Constants.SMOKER_SETTING_TIMER_HOUR, smokeHour);
						Config.putInt(Constants.SMOKER_SETTING_TIMER_MINUTE, smokeMinute);
						seekbar_settingTime.setProgress(smokeHour * 60 + smokeMinute);
						remainedTime.setText(UIUtils.formateTime(smokeHour) + "h " + UIUtils.formateTime(smokeMinute) + "min");
					}
				} else {
					remainedTime.setText(seekBarSettingTemperature.getProgress() + tempUnit);
				}
			} else {
				SmokerModule.setSmokerStatus(false);
				actionStart.setChecked(false);
				setSmokerEnable(true);
			}
		}

		if(mReceiveData.getOvenStatus() == 0x01){//烤箱已打开
			setTemValue.setVisibility(View.VISIBLE);
			setTemValueUnit.setVisibility(View.VISIBLE);
			setTemLabel.setVisibility(View.VISIBLE);
			setTemValue.setText(mReceiveData.getSettingTemperature() + "");
			setTemValueUnit.setText(tempUnit);
			setTemLabel.setText("Set temperature:");//设置界面左上角的文字 -- Set temperature:320℉
			
			currentTemperature.setTextColor(Color.parseColor("#00b6ed"));
			currentTemperatureUnit.setTextColor(Color.parseColor("#00b6ed"));
			smokerTime.setTextColor(Color.parseColor("#fff2b3"));
			ovenTime.setTextColor(Color.parseColor("#fff2b3"));
			
			cb_probe1_on_off.setEnabled(true);
			cb_probe2_on_off.setEnabled(true);
			
		}else {
			setTemValue.setVisibility(View.GONE);
			setTemValueUnit.setVisibility(View.GONE);
			setTemLabel.setVisibility(View.GONE);
			setTemValue.setText("");
			setTemValueUnit.setText("");
			setTemLabel.setText("");
			
			currentTemperature.setTextColor(Color.WHITE);
			currentTemperatureUnit.setTextColor(Color.WHITE);
			smokerTime.setTextColor(Color.WHITE);
			ovenTime.setTextColor(Color.WHITE);
			
			cb_probe1_on_off.setEnabled(false);
			cb_probe2_on_off.setEnabled(false);
		}
	}
	
	private void refreshTimer() {// 收到数据则执行
		// Smoker 时间
		if (mReceiveData.getSmokedStatus() == 0x01) {
			if (mReceiveData.getSmokedHourTime() >= 0 && mReceiveData.getSmokedMinuteTime() >= 0) {
				if(mReceiveData.getSmokedHourTime() < 128 && mReceiveData.getSmokedMinuteTime() < 128){//实时温度
					String smokerTimeStr = (UIUtils.formateTime(mReceiveData.getSmokedHourTime())) + " : " + UIUtils.formateTime(mReceiveData.getSmokedMinuteTime());
					smokerTime.setText(smokerTimeStr);
					Config.putString(Constants.LAST_SMOKE_TIME, smokerTimeStr);
				} else {//设置温度
					Config.putInt(Constants.SMOKER_SETTING_TIMER_HOUR, mReceiveData.getSmokedHourTime() - 128);
					Config.putInt(Constants.SMOKER_SETTING_TIMER_MINUTE, mReceiveData.getSmokedMinuteTime() - 128);
				}
			}
		} else {
			smokerTime.setText("00 : 00");
			Config.putString(Constants.LAST_SMOKE_TIME, "00 : 00");
		}
		
		// Oven 时间
		if (mReceiveData.getOvenStatus() == 0x01) {
			if (mReceiveData.getOvenHourTime() >= 0 && mReceiveData.getOvenMinuteTime() >= 0) {
				if(mReceiveData.getOvenHourTime() < 128 && mReceiveData.getOvenMinuteTime() < 128){//实时温度
					String ovenTimeStr = UIUtils.formateTime(mReceiveData.getOvenHourTime()) + " : " + UIUtils.formateTime(mReceiveData.getOvenMinuteTime());
					ovenTime.setText(ovenTimeStr);
					Config.putString(Constants.LAST_OVEN_TIME, ovenTimeStr);
				} else {//设置温度
					Config.putInt(Constants.OVEN_SETTING_TIMER_HOUR, mReceiveData.getOvenHourTime() - 128);
					Config.putInt(Constants.OVEN_SETTING_TIMER_MINUTE, mReceiveData.getOvenMinuteTime() - 128);
				}
			}
		} else {
			ovenTime.setText("00 : 00");
			Config.putString(Constants.LAST_OVEN_TIME, "00 : 00");
		}
	}

	private void refreshByTempUnit() {//通过温度单位更新UI
		// 设置温度单位 ℉——0x00； ℃——0x01；
		if (mReceiveData.getTemperatureUnit() == 0x00) {
			tempUnit = "℉";
			currentTemperatureUnit.setText(tempUnit);
			setttingTemperatureUnit.setText(tempUnit);
			seekBarSettingTemperature.setStartProgress(120);
			seekBarSettingTemperature.setMaxProgress(200);
			Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, false);

			if (tv_c_50.getVisibility() == View.VISIBLE) {
				seekBarSettingTemperature.setProgress(mReceiveData.getSettingTemperature() - 120);
				Config.putInt(Constants.LAST_SETTING_TEMP, mReceiveData.getSettingTemperature() - 120);
				setFAndC(View.VISIBLE, View.GONE);
			}
		} else {
			tempUnit = "℃";
			currentTemperatureUnit.setText(tempUnit);
			setttingTemperatureUnit.setText(tempUnit);
			seekBarSettingTemperature.setStartProgress(50);
			seekBarSettingTemperature.setMaxProgress(110);
			Config.putBoolean(Constants.SETTING_TEMPERATURE_UNIT, true);

			if (tv_f_120.getVisibility() == View.VISIBLE) {
				seekBarSettingTemperature.setProgress(mReceiveData.getSettingTemperature() - 50);
				Config.putInt(Constants.LAST_SETTING_TEMP, mReceiveData.getSettingTemperature() - 50);

				setFAndC(View.GONE, View.VISIBLE);
			}
		}
		Config.putString(Constants.LAST_TEMP_UNIT, tempUnit);
		// 当单位改变
		if (mReceiveData.getTemperatureUnit() != temperatureUnit) {
			setTemperatureProgress();
			temperatureUnit = mReceiveData.getTemperatureUnit();
		}
	}
	
	private void refreshProbeTemp() {//更新探针温度
		refreshTempIfTempUnitChange();
		refreshProbeSettingTemp();
		int alertTempA = mReceiveData.getProbeTemperatureA();
		int alertTempB = mReceiveData.getProbeTemperatureB();
		if (alertTempA > 1000) {
			currentTemperatureA.setText("---" + tempUnit);
			if(!Constants.isTestMode){
				Config.putString(Constants.LAST_TEMP_A, "---" + tempUnit);
			}
			
			//收到的是报警温度，所以可以更新lastMeat和lastMeatTemp
			if(alertTempA >= 32768 && alertTempA <= (32768 + 320)){
				setLastMeatAndTemp(true, mReceiveData.getMeatSelection() / 16, alertTempA - 32768);
			}
		} else {
			currentTemperatureA.setText(alertTempA == 0xFFFF ? "---" + tempUnit : alertTempA + tempUnit);
			if(!Constants.isTestMode){
				Config.putString(Constants.LAST_TEMP_A, alertTempA == 0xFFFF ? "---" + tempUnit : alertTempA + tempUnit);
			}
		}
		if (alertTempB > 1000) {
			currentTemperatureB.setText(" / " + "---" + tempUnit);
			if(!Constants.isTestMode){
				Config.putString(Constants.LAST_TEMP_B, "---" + tempUnit);
			}
			//收到的是报警温度，所以可以更新lastMeat和lastMeatTemp
			if(alertTempB >= 32768 && alertTempB <= (32768 + 320)){
				setLastMeatAndTemp(false, mReceiveData.getMeatSelection() % 16, alertTempB - 32768);
			}
		} else {
			currentTemperatureB.setText(alertTempB == 0xFFFF ? " / ---" + tempUnit : " / " + alertTempB + tempUnit);
			if(!Constants.isTestMode){
				Config.putString(Constants.LAST_TEMP_B, alertTempB == 0xFFFF ? " / ---" + tempUnit : " / " + alertTempB + tempUnit);
			}
		}
	}

	private void refreshProbeSettingTemp(){//更新探针设置温度
		int alertTempA = mReceiveData.getProbeTemperatureA();
		int alertTempB = mReceiveData.getProbeTemperatureB();
		if(alertTempA >= 32768 && alertTempA <= (32768 + 320)){
			Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, alertTempA - 32768);
		} else if(alertTempA == 65535){
			Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, -1);
		} else {
			if(Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A) == -1){
				Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, 320);
			}
		}
		if(alertTempB >= 32768 && alertTempB <= (32768 + 320)){
			Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, alertTempB - 32768);
		} else if(alertTempB == 65535){
			Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, -1);
		} else {
			if(Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B) == -1){
				Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, 320);
			}
		}
	}
	
	private void refreshTempIfTempUnitChange() {// 收到数据则执行
		if (Config.getBoolean(Constants.SETTING_ALERT_TEMPERATURE_UNIT, false) != Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false)) {
			
			if (Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false)) {//63145 71160 74165 77170
				// C
				int probeA = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
				int probeB = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);
				int ca = 0 ,cb = 0;
				Log.i("setAlertTemperature", "fahrenheitToCelsius probeA:" + probeA + " probeB:" + probeB);
				ca = MainFragmentUtil.fToC(probeA);
				cb = MainFragmentUtil.fToC(probeB);
				if (ca >= 50) {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, ca);
				} else {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, 50);
				}
				if (cb >= 50) {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, cb);
				} else {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, 50);
				}

				Log.i("setAlertTemperature", "fahrenheitToCelsius ca:" + ca + " cb:" + cb);
			} else {
				// F
				int probeA = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
				int probeB = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);
				int ca = 0 ,cb = 0;
				ca = MainFragmentUtil.cToF(probeA);
				cb = MainFragmentUtil.cToF(probeB);
				if (ca >= 120) {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, ca);
				} else {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_A, 120);
				}
				if (cb >= 120) {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, cb);
				} else {
					Config.putInt(Constants.SETTING_ALERT_TEMPERATURE_B, 120);
				}
				Log.i("setAlertTemperature", "celsiusToFahrenheit ca:" + ca + " cb:" + cb);
			}
			Config.putBoolean(Constants.SETTING_ALERT_TEMPERATURE_UNIT, Config.getBoolean(Constants.SETTING_TEMPERATURE_UNIT, false));
		}
	}
	
	/*--------------------------------------------------------------------------------------------------探针界面方法----------------------------------------------------------------------------------------*/
	
	public void reFreshProbeView() {
		if(!Constants.isTestMode){
			setTextRed(1, Config.getInt(Constants.LAST_MEAT_TEMP_A));
			setTextRed(2, Config.getInt(Constants.LAST_MEAT_TEMP_B));
		}
	}
	
	private void setTextRed(int probeType, int temp) {
		boolean pair = false;
		int meat = 0;
		if(probeType == 1){
			meat = Config.getInt(Constants.LAST_MEAT_A);
		}else{
			meat = Config.getInt(Constants.LAST_MEAT_B);
		}
		int alertTemp = 0;
		if(probeType == 1){
			alertTemp = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_A);
		}else{
			alertTemp = Config.getInt(Constants.SETTING_ALERT_TEMPERATURE_B);
		}
		for (int i = 0; i < meatAndTempList[meat].length; i++) {
			if(meatAndTempList[meat][i] / 1000 == alertTemp || meatAndTempList[meat][i] % 1000 == alertTemp){
				pair = true;
				temp = meatAndTempList[meat][i];
				break;
			}
		}
		if(pair){
			if(probeType == 1){
				Config.putInt(Constants.LAST_MEAT_TEMP_A, temp);
				cb_probe1_on_off.setChecked(true);
			}else{
				Config.putInt(Constants.LAST_MEAT_TEMP_B, temp);
				cb_probe2_on_off.setChecked(true);
			}
		} else {
			if(probeType == 1){
				cb_probe1_on_off.setChecked(false);
			}else{
				cb_probe2_on_off.setChecked(false);
			}
		}
		
		for (int i = 0; i < 3; i++) {// 先默认全部白色
			tvs_probe_list.get(i * 2 + (probeType - 1) * 6).setTextColor(Color.parseColor("#ffffff"));
			tvs_probe_list.get(i * 2 + 1 + (probeType - 1) * 6).setTextColor(Color.parseColor("#999999"));
		}
		
		if (temp == 63145 || temp == 74165) {//63145 71160 74165 77170
			tvs_probe_list.get(0 + (probeType - 1) * 6).setTextColor(Color.parseColor("#ff0000"));
			tvs_probe_list.get(1 + (probeType - 1) * 6).setTextColor(Color.parseColor("#ff0000"));
		} else if (temp == 71160) {
			tvs_probe_list.get(2 + (probeType - 1) * 6).setTextColor(Color.parseColor("#ff0000"));
			tvs_probe_list.get(3 + (probeType - 1) * 6).setTextColor(Color.parseColor("#ff0000"));
		} else if (temp == 77170) {
			tvs_probe_list.get(4 + (probeType - 1) * 6).setTextColor(Color.parseColor("#ff0000"));
			tvs_probe_list.get(5 + (probeType - 1) * 6).setTextColor(Color.parseColor("#ff0000"));
		} 
		
	}
	
	// 垂直滑动viewpage adapter by jeff
	class MyAdapter extends PagerAdapter {

		private int probeIndex;

		public MyAdapter(int probeIndex) {
			this.probeIndex = probeIndex;
		}

		@Override
		public int getCount() {
			if (probeIndex == 1) {
				return list_probe1.size();
			} else if (probeIndex == 2) {
				return list_probe2.size();
			}
			return -1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (probeIndex == 1) {
				((VerticalViewPager) container).removeView(list_probe1.get(position));
			} else if (probeIndex == 2) {
				((VerticalViewPager) container).removeView(list_probe2.get(position));
			}
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "title";
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			if (probeIndex == 1) {
				((VerticalViewPager) container).addView(list_probe1.get(position));
				return list_probe1.get(position);
			} else if (probeIndex == 2) {
				((VerticalViewPager) container).addView(list_probe2.get(position));
				return list_probe2.get(position);
			}
			return null;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void setPrimaryItem(View container, int position, Object object) {
			if (probeIndex == 1) {
				if (position != currentIndexOfViewpagerProbe1) {
					setViewPage1Index(position);
				}
			} else if (probeIndex == 2) {
				if (position != currentIndexOfViewpagerProbe2) {
					setViewPage2Index(position);
				}
			}
		}
	}

	private void setViewPage1Index(int position) {//滑动选择肉类
		currentIndexOfViewpagerProbe1 = position;
		Config.putInt(Constants.LAST_MEAT_A, position);
		switch (lastIndexOfViewpagerProbe1) {
		case -1:
		case 0:
			btn_probe1_0_of_viewpage.setBackgroundResource(R.drawable.icon_dian2);
			break;
		case 1:
			btn_probe1_1_of_viewpage.setBackgroundResource(R.drawable.icon_dian2);
			break;
		case 2:
			btn_probe1_2_of_viewpage.setBackgroundResource(R.drawable.icon_dian2);
			break;
		case 3:
			btn_probe1_3_of_viewpage.setBackgroundResource(R.drawable.icon_dian2);
			break;
		case 4:
			btn_probe1_4_of_viewpage.setBackgroundResource(R.drawable.icon_dian2);
			break;
		}
		switch (currentIndexOfViewpagerProbe1) {
		case 0:
			btn_probe1_arrow_top.setVisibility(View.INVISIBLE);
			btn_probe1_arrow_bottom.setVisibility(View.VISIBLE);
			btn_probe1_0_of_viewpage.setBackgroundResource(R.drawable.icon_dian1);

			tv_probe1_1_word.setText("Medium rare");
			tv_probe1_1_num.setText("63℃/145℉");
			tv_probe1_2_word.setText("Medium");
			tv_probe1_2_num.setText("71℃/160℉");

			setProbe1Temp = 63145;

			updateTextColor(1, 2);

			break;
		case 1:
			btn_probe1_arrow_top.setVisibility(View.VISIBLE);
			btn_probe1_arrow_bottom.setVisibility(View.VISIBLE);
			btn_probe1_1_of_viewpage.setBackgroundResource(R.drawable.icon_dian1);

			tv_probe1_1_word.setText("Medium rare");
			tv_probe1_1_num.setText("63℃/145℉");
			tv_probe1_2_word.setText("Medium");
			tv_probe1_2_num.setText("71℃/160℉");
			tv_probe1_3_word.setText("Well done");
			tv_probe1_3_num.setText("77℃/170℉");

			setProbe1Temp = 63145;

			updateTextColor(1, 3);

			break;
		case 2:
			btn_probe1_arrow_top.setVisibility(View.VISIBLE);
			btn_probe1_arrow_bottom.setVisibility(View.VISIBLE);
			btn_probe1_2_of_viewpage.setBackgroundResource(R.drawable.icon_dian1);
			
			tv_probe1_1_word.setText("Suggested");
			tv_probe1_1_num.setText("63℃/145℉");
			
			setProbe1Temp = 63145;
			
			updateTextColor(1, 1);
			
			break;
		case 3:
			btn_probe1_arrow_top.setVisibility(View.VISIBLE);
			btn_probe1_arrow_bottom.setVisibility(View.VISIBLE);
			btn_probe1_3_of_viewpage.setBackgroundResource(R.drawable.icon_dian1);

			tv_probe1_1_word.setText("Medium rare");
			tv_probe1_1_num.setText("63℃/145℉");
			tv_probe1_2_word.setText("Medium");
			tv_probe1_2_num.setText("71℃/160℉");

			setProbe1Temp = 63145;

			updateTextColor(1, 2);

			break;
		case 4:
			btn_probe1_arrow_top.setVisibility(View.VISIBLE);
			btn_probe1_arrow_bottom.setVisibility(View.INVISIBLE);
			btn_probe1_4_of_viewpage.setBackgroundResource(R.drawable.icon_dian1);

			tv_probe1_1_word.setText("Suggested");
			tv_probe1_1_num.setText("74℃/165℉");

			setProbe1Temp = 74165;

			updateTextColor(1, 1);

			break;
		}
		if(lastIndexOfViewpagerProbe1 != -1){
			Config.putInt(Constants.LAST_MEAT_TEMP_A, setProbe1Temp);
		}
		lastIndexOfViewpagerProbe1 = position;
	}

	private void setViewPage2Index(int position) {
		currentIndexOfViewpagerProbe2 = position;
		Config.putInt(Constants.LAST_MEAT_B, position);
		switch (lastIndexOfViewpagerProbe2) {
		case -1:
		case 0:
			btn_probe2_0_of_viewpage.setBackgroundResource(R.drawable.icon_dian2);
			break;
		case 1:
			btn_probe2_1_of_viewpage.setBackgroundResource(R.drawable.icon_dian2);
			break;
		case 2:
			btn_probe2_2_of_viewpage.setBackgroundResource(R.drawable.icon_dian2);
			break;
		case 3:
			btn_probe2_3_of_viewpage.setBackgroundResource(R.drawable.icon_dian2);
			break;
		case 4:
			btn_probe2_4_of_viewpage.setBackgroundResource(R.drawable.icon_dian2);
			break;
		}
		switch (currentIndexOfViewpagerProbe2) {
		case 0:
			btn_probe2_arrow_top.setVisibility(View.INVISIBLE);
			btn_probe2_arrow_bottom.setVisibility(View.VISIBLE);
			btn_probe2_0_of_viewpage.setBackgroundResource(R.drawable.icon_dian1);

			tv_probe2_1_word.setText("Medium rare");
			tv_probe2_1_num.setText("63℃/145℉");
			tv_probe2_2_word.setText("Medium");
			tv_probe2_2_num.setText("71℃/160℉");

			setProbe2Temp = 63145;

			updateTextColor(2, 2);

			break;
		case 1:
			btn_probe2_arrow_top.setVisibility(View.VISIBLE);
			btn_probe2_arrow_bottom.setVisibility(View.VISIBLE);
			btn_probe2_1_of_viewpage.setBackgroundResource(R.drawable.icon_dian1);

			tv_probe2_1_word.setText("Medium rare");
			tv_probe2_1_num.setText("63℃/145℉");
			tv_probe2_2_word.setText("Medium");
			tv_probe2_2_num.setText("71℃/160℉");
			tv_probe2_3_word.setText("Well done");
			tv_probe2_3_num.setText("77℃/170℉");

			setProbe2Temp = 63145;

			updateTextColor(2, 3);

			break;
		case 2:
			btn_probe2_arrow_top.setVisibility(View.VISIBLE);
			btn_probe2_arrow_bottom.setVisibility(View.VISIBLE);
			btn_probe2_2_of_viewpage.setBackgroundResource(R.drawable.icon_dian1);
			
			tv_probe2_1_word.setText("Suggested");
			tv_probe2_1_num.setText("63℃/145℉");
			
			setProbe2Temp = 63145;

			updateTextColor(2, 1);
			
			break;
		case 3:
			btn_probe2_arrow_top.setVisibility(View.VISIBLE);
			btn_probe2_arrow_bottom.setVisibility(View.VISIBLE);
			btn_probe2_3_of_viewpage.setBackgroundResource(R.drawable.icon_dian1);

			tv_probe2_1_word.setText("Medium rare");
			tv_probe2_1_num.setText("63℃/145℉");
			tv_probe2_2_word.setText("Medium");
			tv_probe2_2_num.setText("71℃/160℉");

			setProbe2Temp = 63145;

			updateTextColor(2, 2);

			break;
		case 4:
			btn_probe2_arrow_top.setVisibility(View.VISIBLE);
			btn_probe2_arrow_bottom.setVisibility(View.INVISIBLE);
			btn_probe2_4_of_viewpage.setBackgroundResource(R.drawable.icon_dian1);

			tv_probe2_1_word.setText("Suggested");
			tv_probe2_1_num.setText("74℃/165℉");

			setProbe2Temp = 74165;

			updateTextColor(2, 1);

			break;
		}
		if(lastIndexOfViewpagerProbe2 != -1){
			Config.putInt(Constants.LAST_MEAT_TEMP_B, setProbe2Temp);
		}
		lastIndexOfViewpagerProbe2 = position;
	}
	
	private void updateTextColor(int probeType, int num) {
		for (int i = 0; i < 3; i++) {
			if (i < num) {
				rlts_probe_list.get(i + (probeType - 1) * 3).setVisibility(View.VISIBLE);
				if (i == 0) {// 默认红色
					tvs_probe_list.get(i * 2 + (probeType - 1) * 6).setTextColor(Color.parseColor("#ff0000"));
					tvs_probe_list.get(i * 2 + 1 + (probeType - 1) * 6).setTextColor(Color.parseColor("#ff0000"));
				} else {
					tvs_probe_list.get(i * 2 + (probeType - 1) * 6).setTextColor(Color.parseColor("#ffffff"));
					tvs_probe_list.get(i * 2 + 1 + (probeType - 1) * 6).setTextColor(Color.parseColor("#999999"));
				}
			} else {
				rlts_probe_list.get(i + (probeType - 1) * 3).setVisibility(View.INVISIBLE);
			}
		}
	}
	
	/**
	 * 判断输入的肉类和温度是否匹配，匹配的话则保存起来
	 */
	private void setLastMeatAndTemp(boolean isProbeA, int meat, int temp){
		Log.e("testLog", "meat："+meat + " temp:"+temp);
		switch (meat) {
		case 0:
			if(isProbeA){
				viewpager_direction_probe1.setCurrentItem(Config.getInt(Constants.LAST_MEAT_A));
				tv_probe1_off.setVisibility(View.VISIBLE);
				tv_probe1_on.setVisibility(View.GONE);
				interceptEvent_probe1_1.setVisibility(View.GONE);
				interceptEvent_probe1_2.setVisibility(View.GONE);
				cb_probe1_on_off.setChecked(false);
			}else{
				viewpager_direction_probe2.setCurrentItem(Config.getInt(Constants.LAST_MEAT_B));
				tv_probe2_off.setVisibility(View.VISIBLE);
				tv_probe2_on.setVisibility(View.GONE);
				interceptEvent_probe2_1.setVisibility(View.GONE);
				interceptEvent_probe2_2.setVisibility(View.GONE);
				cb_probe2_on_off.setChecked(false);
			}
			return;
		case 1://猪
			meat = 3;
			break;
		case 2://牛
			meat = 1;
			break;
		case 3://鸡
			meat = 4;
			break;
		case 4://羊
			meat = 0;
			break;
		case 5://鱼
			meat = 2;
			break;
		}
		boolean pair = false;
		for (int i = 0; i < meatAndTempList[meat].length; i++) {
			if(meatAndTempList[meat][i] / 1000 == temp || meatAndTempList[meat][i] % 1000 == temp){
				pair = true;
				temp = meatAndTempList[meat][i];
				break;
			}
		}
		Log.e("testLog", "pair:"+pair);
		if(pair){
			if(isProbeA){
				Config.putInt(Constants.LAST_MEAT_A, meat);
				Config.putInt(Constants.LAST_MEAT_TEMP_A, temp);
				viewpager_direction_probe1.setCurrentItem(Config.getInt(Constants.LAST_MEAT_A));
				cb_probe1_on_off.setChecked(true);
			}else{
				Config.putInt(Constants.LAST_MEAT_B, meat);
				Config.putInt(Constants.LAST_MEAT_TEMP_B, temp);
				viewpager_direction_probe2.setCurrentItem(Config.getInt(Constants.LAST_MEAT_B));
				cb_probe2_on_off.setChecked(true);
			}
			setProbeSettingTemp(isProbeA, temp);
		} else {
			if(isProbeA){
				tv_probe1_off.setVisibility(View.VISIBLE);
				tv_probe1_on.setVisibility(View.GONE);
				interceptEvent_probe1_1.setVisibility(View.GONE);
				interceptEvent_probe1_2.setVisibility(View.GONE);
				cb_probe1_on_off.setChecked(false);
				viewpager_direction_probe1.setCurrentItem(Config.getInt(Constants.LAST_MEAT_A));
			}else{
				tv_probe2_off.setVisibility(View.VISIBLE);
				tv_probe2_on.setVisibility(View.GONE);
				interceptEvent_probe2_1.setVisibility(View.GONE);
				interceptEvent_probe2_2.setVisibility(View.GONE);
				cb_probe2_on_off.setChecked(false);
				viewpager_direction_probe2.setCurrentItem(Config.getInt(Constants.LAST_MEAT_B));
			}
		}
	}
	
	/**
	 * 根据temp设置探针设置温度
	 */
	private void setProbeSettingTemp(boolean isProbeA, int temp){
		if(isProbeA){
			switch (temp) {//{{63145,71160},{63145,71160,77170},{63145},{63145,71160},{74165}};//羊，牛，鱼，猪，鸡,,,,63145 71160 74165 77170
			case 63145:
				setProbeTextRed(true, 1);
				break;
			case 71160:
				setProbeTextRed(true, 2);
				break;
			case 77170:
				setProbeTextRed(true, 3);
				break;
			case 74165:
				tv_probe1_1_word.setTextColor(Color.parseColor("#ff0000"));
				tv_probe1_1_num.setTextColor(Color.parseColor("#ff0000"));
				break;
			}
			Config.putInt(Constants.LAST_MEAT_TEMP_A, temp);
		} else {
			switch (temp) {//{{63145,71160},{63145,71160,77170},{63145},{63145,71160},{74165}};//羊，牛，鱼，猪，鸡,,,,63145 71160 74165 77170
			case 63145:
				setProbeTextRed(false, 1);
				break;
			case 71160:
				setProbeTextRed(false, 2);
				break;
			case 77170:
				setProbeTextRed(false, 3);
				break;
			case 74165:
				tv_probe2_1_word.setTextColor(Color.parseColor("#ff0000"));
				tv_probe2_1_num.setTextColor(Color.parseColor("#ff0000"));
				break;
			}
			Config.putInt(Constants.LAST_MEAT_TEMP_B, temp);
		}
	}
	
	/*-----------------------------------------------------------------------------------------------------版本1原方法------------------------------------------------------------------------*/
	private void setTemperatureProgress() {
		if (mReceiveData.getTemperatureUnit() == 0x00) {
			seekBarSettingTemperature.setStartProgress(120);
			seekBarSettingTemperature.setMaxProgress(200);
			seekBarSettingTemperature.setProgress(mReceiveData.getSettingTemperature() - 120);
			Config.putInt(Constants.LAST_SETTING_TEMP, mReceiveData.getSettingTemperature() - 120);
		} else {
			seekBarSettingTemperature.setStartProgress(50);
			seekBarSettingTemperature.setMaxProgress(110);
			seekBarSettingTemperature.setProgress(mReceiveData.getSettingTemperature() - 50);
			Config.putInt(Constants.LAST_SETTING_TEMP, mReceiveData.getSettingTemperature() - 50);
		}
		OvenModule.setOvenTemperature(seekBarSettingTemperature.getProgress());
	}

	private void setCircleTime() {
		if(!Constants.isTestMode){
			if(ovenModule.isChecked() && mReceiveData.getOvenStatus() == 0x01){
				if("Time".equals(remainedTimeLabel.getText().toString())){
					ovenHour = Config.getInt(Constants.OVEN_SETTING_TIMER_HOUR);
					ovenMinute = Config.getInt(Constants.OVEN_SETTING_TIMER_MINUTE);
					remainedTime.setText(UIUtils.formateTime(ovenHour) + "h " + UIUtils.formateTime(ovenMinute) + "min");
					seekbar_settingTime.setProgress(ovenHour * 60 + ovenMinute);
					Log.e("testLog", "setCircleTime1 ovenHour:"+ovenHour + "ovenMinute:"+ovenMinute);
				} else {
					remainedTime.setText(seekBarSettingTemperature.getProgress() + tempUnit);
				}
			}else if(smokerModule.isChecked() && mReceiveData.getSmokedStatus() == 0x01){
				if("Time".equals(remainedTimeLabel.getText().toString())){
					smokeHour = Config.getInt(Constants.SMOKER_SETTING_TIMER_HOUR);
					smokeMinute = Config.getInt(Constants.SMOKER_SETTING_TIMER_MINUTE);
					remainedTime.setText(UIUtils.formateTime(smokeHour) + "h " + UIUtils.formateTime(smokeMinute) + "min");
					seekbar_settingTime.setProgress(smokeHour * 60 + smokeMinute);
					Log.e("testLog", "setCircleTime2 smokeHour:"+smokeHour + "smokeMinute:"+smokeMinute);
				}
			}else {
				int timeMin = seekbar_settingTime.getProgress();
				int timeHour = timeMin / 60;
				if("Time".equals(remainedTimeLabel.getText().toString())){
					if (timeMin >= 60) {
						timeMin = timeMin - timeHour * 60;
					}
					String min = timeMin > 9 ? timeMin + "" : "0" + timeMin;
					String hour = timeHour > 9 ? timeHour + "" : "0" + timeHour;
					Log.e("testLog", "setCircleTime3 timerHour:"+hour + "timerMinute:"+min);
					remainedTime.setText(hour + "h " + min + "min");
				} else {
					remainedTime.setText(seekBarSettingTemperature.getProgress() + tempUnit);
				}
				
				if(ovenModule.isChecked()){
					OvenModule.setOvenTime(timeHour, timeMin);
				} else  if(smokerModule.isChecked()){
					SmokerModule.setSmokerTime(timeHour, timeMin);
				}
			}
		}
	}

	private void setOvenEnable(boolean isEnable) {
		if (!Constants.isTestMode) {
			remainedTime.setEnabled(isEnable);
			remainedTimeLabel.setEnabled(isEnable);
			setSeekBarSettingTemperatureStyle(isEnable);
			setSeekBarSettingTimeStyle(isEnable);
		}
	}

	private void setSmokerEnable(boolean isEnable) {
		if (!Constants.isTestMode) {
			remainedTime.setEnabled(isEnable);
			remainedTimeLabel.setEnabled(isEnable);
			setSeekBarSettingTemperatureStyle(false);
			setSeekBarSettingTimeStyle(isEnable);
		}
	}

	private void setSeekBarSettingTemperatureStyle(boolean isEnable) {
		if (isEnable) {
			seekBarSettingTemperature.setEnabled(true);
			seekBarSettingTemperature.setPointerColorRes(R.color.home_seekbar_pointer_enable);
			seekBarSettingTemperature.setWheelActiveColorRes(R.color.home_seekbar_active_enable);
			seekBarSettingTemperature.setWheelUnactiveColorRes(R.color.home_seekbar_unactive_enable);
		} else {
			seekBarSettingTemperature.setEnabled(false);
			seekBarSettingTemperature.setPointerColorRes(R.color.home_seekbar_pointer_disable);
			seekBarSettingTemperature.setWheelActiveColorRes(R.color.home_seekbar_active_disable);
			seekBarSettingTemperature.setWheelUnactiveColorRes(R.color.home_seekbar_unactive_disable);
		}
	}

	private void setSeekBarSettingTimeStyle(boolean isEnable) {
		if (isEnable) {
			seekbar_settingTime.setEnabled(true);
			seekbar_settingTime.setPointerColorRes(R.color.home_seekbar_pointer_enable);
			seekbar_settingTime.setWheelActiveColorRes(R.color.home_seekbar_active_enable);
			seekbar_settingTime.setWheelUnactiveColorRes(R.color.home_seekbar_unactive_enable);
		} else {
			seekbar_settingTime.setEnabled(false);
			seekbar_settingTime.setPointerColorRes(R.color.home_seekbar_pointer_disable);
			seekbar_settingTime.setWheelActiveColorRes(R.color.home_seekbar_active_disable);
			seekbar_settingTime.setWheelUnactiveColorRes(R.color.home_seekbar_unactive_disable);
		}
	}
	
	private void setProbeTextRed(boolean isProbe1, int level){//设置探针1文字高亮，level只能等于1,2,3
		String[] colors = {"#ffffff","#999999","#ffffff","#999999","#ffffff","#999999"};
		colors[level * 2 - 1] = colors[level * 2 - 2] = "#ff0000";
		if(isProbe1){
			tv_probe1_1_word.setTextColor(Color.parseColor(colors[0]));
			tv_probe1_1_num.setTextColor(Color.parseColor(colors[1]));
			tv_probe1_2_word.setTextColor(Color.parseColor(colors[2]));
			tv_probe1_2_num.setTextColor(Color.parseColor(colors[3]));
			tv_probe1_3_word.setTextColor(Color.parseColor(colors[4]));
			tv_probe1_3_num.setTextColor(Color.parseColor(colors[5]));
		} else {
			tv_probe2_1_word.setTextColor(Color.parseColor(colors[0]));
			tv_probe2_1_num.setTextColor(Color.parseColor(colors[1]));
			tv_probe2_2_word.setTextColor(Color.parseColor(colors[2]));
			tv_probe2_2_num.setTextColor(Color.parseColor(colors[3]));
			tv_probe2_3_word.setTextColor(Color.parseColor(colors[4]));
			tv_probe2_3_num.setTextColor(Color.parseColor(colors[5]));
		}
	}
	
	private void setFAndC(int f, int c){
		tv_f_120.setVisibility(f);
		tv_f_150.setVisibility(f);
		tv_f_180.setVisibility(f);
		tv_f_210.setVisibility(f);
		tv_f_240.setVisibility(f);
		tv_f_270.setVisibility(f);
		tv_f_300.setVisibility(f);
		tv_f_320.setVisibility(f);

		tv_c_50.setVisibility(c);
		tv_c_70.setVisibility(c);
		tv_c_90.setVisibility(c);
		tv_c_110.setVisibility(c);
		tv_c_130.setVisibility(c);
		tv_c_150.setVisibility(c);
		tv_c_160.setVisibility(c);
	}
}