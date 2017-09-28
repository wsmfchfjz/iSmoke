package ganguo.oven.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import ganguo.oven.AppContext;
import ganguo.oven.Constants;
import ganguo.oven.R;
import ganguo.oven.activity.MainActivity;
import ganguo.oven.bluetooth.BleCommand;
import ganguo.oven.bluetooth.BleEvent;
import ganguo.oven.bluetooth.ReceiveData;
import ganguo.oven.db.Point;
import ganguo.oven.db.PointDao;
import ganguo.oven.event.OnSingleClickListener;
import ganguo.oven.utils.TimerUtil;
import ganguo.oven.utils.UIUtils;
import ganguo.oven.view.NoScrollViewPager;

/**
 * Created by Wilson on 14-7-9.
 */
public class ChartFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener, OnChartGestureListener {
	private ImageButton toSettingsBtn, toHomeBtn;
	private MainActivity mainActivity;
	private Resources resources;
	private Button stopBtn, toAlertBtn;
	private CheckBox checkBoxA, checkBoxB, checkBoxBox;
	private TextView setTemValue, setTemValueUnit, currentTemperature, currentTemperatureA, currentTemperatureB, currentTemperatureUnit, smokerTime, ovenTime, chartLabelTem, chartLabelTime;

	private EventBus mEventBus = EventBus.getDefault();
	private ReceiveData mReceiveData = AppContext.getInstance().getReceiveData();

	private NoScrollViewPager viewPager;
	private PointDao pointDao;
	private boolean showProbe1Line, showProbe2Line, showTempLine;
	private ArrayList<String> xVals120, xVals240, xVals360, xVals480, xVals600;
	private List<LineChart> chartList;
	private int currentChartListIndex;
	private List<String> chartDateList;// chart对应的存储日期列表

	private final int max_min = 600;// 横坐标最大分钟数
	private int currentMaxX;
	public static boolean isUnitF ;
	public static boolean isInitUnit = false;//是否已经初始化温度单位

	/**
	 * EventBus
	 */
	public void onEventMainThread(BleEvent event) {
		switch (event.getCommand()) {
		case NOTIFY_RECEIVE_DATA:
			// 数据接收
			mReceiveData = (ReceiveData) event.getTarget();
			if (mReceiveData.getTemperatureUnit() == 0x00) {
				if (!isUnitF) {
					isUnitF = true;
					isInitUnit = true;
					for (int i = chartList.size() - 1; i >= 0; i--) {
						YAxis leftAxis = chartList.get(i).getAxisLeft();
						leftAxis.setAxisMaxValue(549f);// y轴最大值
						updateTempLineNum();
					}
				}
			} else {
//				Log.i("btichart", "mReceiveData 11");
				if (isUnitF) {
//					Log.i("btichart", "mReceiveData false");
					isUnitF = false;
					isInitUnit = true;
					for (int i = chartList.size() - 1; i >= 0; i--) {
//						Log.i("btichart", "mReceiveData 275");
						YAxis leftAxis = chartList.get(i).getAxisLeft();
						leftAxis.setAxisMaxValue(275f);// y轴最大值
						updateTempLineNum();
					}
				}
			}

			setViewData();

			break;
		case STOP_RECODE:
			// reset
			chartList.get(chartList.size() - 1).clearValues();
			chartList.get(chartList.size() - 1).invalidate();
			break;
		}
	}

	private void setViewData() {

		setTemValue.setText(mReceiveData.getSettingTemperature() + "");
		currentTemperature.setText(mReceiveData.getProbeTemperature() + "");

		int alertTempA = mReceiveData.getProbeTemperatureA();
		int alertTempB = mReceiveData.getProbeTemperatureB();

		// 设置温度单位 ℉——0x00； ℃——0x01；
		String tempUnit = mReceiveData.getTemperatureUnit() == 0x00 ? "℉" : "℃";

		setTemValueUnit.setText(tempUnit);
		currentTemperatureUnit.setText(tempUnit);
		chartLabelTem.setText(tempUnit);
		currentTemperatureA.setText(alertTempA == 0xFFFF ? "---" + tempUnit : alertTempA + tempUnit);
		currentTemperatureB.setText(alertTempB == 0xFFFF ? " / ---" + tempUnit : " / " + alertTempB + tempUnit);

		// Smoker 时间
		if (mReceiveData.getSmokedStatus() == 0x01) {
			String smokerTimeStr = (UIUtils.formateTime(mReceiveData.getSmokedHourTime())) + " : " + UIUtils.formateTime(mReceiveData.getSmokedMinuteTime());
			smokerTime.setText(smokerTimeStr);
		} else {
			smokerTime.setText("00 : 00");
		}

		// Oven 时间
		if (mReceiveData.getOvenStatus() == 0x01) {
			String ovenTimeStr = UIUtils.formateTime(mReceiveData.getOvenHourTime()) + " : " + UIUtils.formateTime(mReceiveData.getOvenMinuteTime());
			ovenTime.setText(ovenTimeStr);
		} else {
			ovenTime.setText("00 : 00");
		}

	}

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
		return R.layout.fragment_chart;
	}

	@Override
	public String getMyTAG() {
		return "ChartFragment";
	}

	@Override
	public void beforeInitView() {
		mainActivity = (MainActivity) getActivity();
		resources = getActivity().getResources();
	}

	@Override
	public void initView() {
		toSettingsBtn = (ImageButton) getView().findViewById(R.id.toSettingsBtn);
		toHomeBtn = (ImageButton) getView().findViewById(R.id.toHomeBtn);

		setTemValue = (TextView) getView().findViewById(R.id.setTemValue);
		setTemValueUnit = (TextView) getView().findViewById(R.id.setTemValueUnit);
		currentTemperature = (TextView) getView().findViewById(R.id.currentTemperature);
		currentTemperatureA = (TextView) getView().findViewById(R.id.currentTemperatureA);
		currentTemperatureB = (TextView) getView().findViewById(R.id.currentTemperatureB);
		currentTemperatureUnit = (TextView) getView().findViewById(R.id.currentTemperatureUnit);
		smokerTime = (TextView) getView().findViewById(R.id.smokerTime);
		ovenTime = (TextView) getView().findViewById(R.id.ovenTime);
		chartLabelTem = (TextView) getView().findViewById(R.id.chartLabelTem);
		chartLabelTime = (TextView) getView().findViewById(R.id.chartLabelTime);
		stopBtn = (Button) getView().findViewById(R.id.stopBtn);

		checkBoxA = (CheckBox) getView().findViewById(R.id.checkBoxA);
		checkBoxB = (CheckBox) getView().findViewById(R.id.checkBoxB);
		checkBoxBox = (CheckBox) getView().findViewById(R.id.checkBoxBox);

		toAlertBtn = (Button) getView().findViewById(R.id.toAlertBtn);

		if(chartList == null){
			chartList = new ArrayList<LineChart>();
		}
//		chartDateList = new ArrayList<String>();

		showProbe1Line = showProbe2Line = showTempLine = true;

		viewPager = (NoScrollViewPager) getView().findViewById(R.id.viewpager);
		initCharts();
	}

	private void initCharts() {
//		SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.CHARTS_LIST, Context.MODE_PRIVATE);
//		String charts_list = sharedPreferences.getString(Constants.CHARTS_LIST, "");
		String charts_list = "";
		Point p_charts_list = pointDao.findPointByX(12345);
		if(p_charts_list != null){
			charts_list = p_charts_list.getMyDateList();
		}
		if(chartList != null){
			chartList.clear();//容易报空指针
		}
		chartDateList.clear();
		xVals120 = new ArrayList<String>();
		xVals240 = new ArrayList<String>();
		xVals360 = new ArrayList<String>();
		xVals480 = new ArrayList<String>();
		xVals600 = new ArrayList<String>();
		for (int i = 0; i <= max_min / 5; i++) {
			xVals120.add(i + "");
			xVals240.add(i + "");
			xVals360.add(i + "");
			xVals480.add(i + "");
			xVals600.add(i + "");
		}
		for (int i = max_min / 5 + 1; i <= max_min / 5 * 2; i++) {
			xVals240.add(i + "");
			xVals360.add(i + "");
			xVals480.add(i + "");
			xVals600.add(i + "");
		}
		for (int i = max_min / 5 * 2 + 1; i <= max_min / 5 * 3; i++) {
			xVals360.add(i + "");
			xVals480.add(i + "");
			xVals600.add(i + "");
		}
		for (int i = max_min / 5 * 3 + 1; i <= max_min / 5 * 4; i++) {
			xVals480.add(i + "");
			xVals600.add(i + "");
		}
		for (int i = max_min / 5 * 4 + 1; i <= max_min; i++) {
			xVals600.add(i + "");
		}
		if (!charts_list.equals("")) {
			for (String chartDate : charts_list.split("#")) {
				chartDateList.add(chartDate);
			}
			for (int i = 0; i < chartDateList.size(); i++) {
				Log.i("btiLogChart", "chartDate:" + chartDateList.get(i));
				getLinChartByDate(chartDateList.get(i));
			}
		} else {
			String currentTime = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
//			Editor editor = sharedPreferences.edit();// 获取编辑器
//			editor.putString(Constants.CHARTS_LIST, currentTime);
//			editor.commit();// 提交修改
			Point p = new Point(12345, 0, 0, 0, "12345", currentTime);
			p.insert();
			chartDateList.add(currentTime);
			currentChartListIndex = 0;
			getLinChartByDate("");
		}
		viewPager.setAdapter(new MyAdapter());
		currentChartListIndex = chartList.size() - 1;
		viewPager.setCurrentItem(currentChartListIndex);
	}

	private void getLinChartByDate(String date) {
		LineChart mChart = new LineChart(mainActivity);
		chartList.add(mChart);

		mChart.setOnChartGestureListener(this);
		mChart.setDrawGridBackground(false);
		// no description text
		mChart.setDescription("");
		mChart.setNoDataText("");
		mChart.setNoDataTextDescription("");
		mChart.setTouchEnabled(true);// 设置是否可以触摸
		mChart.setDragEnabled(true);// 是否可以拖拽
		mChart.setScaleEnabled(true);// 是否可以缩放
		mChart.setPinchZoom(true);// true表示按两个手指比例缩放，false表示按xy轴比例缩放
		mChart.getLegend().setEnabled(false);
//		mChart.setScaleYEnabled(false);// y轴不可拉伸

		YAxis leftAxis = mChart.getAxisLeft();
		leftAxis.removeAllLimitLines();
		if (isUnitF) {
			leftAxis.setAxisMaxValue(549f);// y轴最大值
		} else {
			leftAxis.setAxisMaxValue(275f);// y轴最大值
		}
		leftAxis.setAxisMinValue(0.001f);// y轴最小值
		leftAxis.setLabelCount(10);
		leftAxis.setStartAtZero(false);
		leftAxis.enableGridDashedLine(0f, 0f, 0f);
		leftAxis.setAxisLineColor(Color.WHITE);
		leftAxis.setAxisLineWidth(1.6f);
		leftAxis.setGridColor(Color.parseColor("#888888"));
		leftAxis.setGridLineWidth(0.5f);
		leftAxis.setTextColor(Color.WHITE);
		mChart.getAxisRight().setEnabled(false);
		XAxis xAxis = mChart.getXAxis();
		xAxis.setPosition(XAxisPosition.BOTTOM);
		xAxis.setAxisLineColor(Color.WHITE);
		xAxis.setAxisLineWidth(1.6f);
		xAxis.setGridColor(Color.parseColor("#888888"));
		xAxis.setGridLineWidth(0.5f);
		xAxis.setTextColor(Color.WHITE);

		LineData data = getNewLineData(date, mChart);
		if (data != null) {
			mChart.setData(data);
			mChart.invalidate();
			mChart.setHighlightEnabled(false);// 设置是否可以高亮
		}
	}

	/**
	 * 读取数据库中的数据，得到最新的LineData
	 */
	private LineData getNewLineData(String date, LineChart mChart) {
		ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
		ArrayList<String> xVals = xVals120;// 初始化x，默认120
		List<Point> pointList = pointDao.getPointsByDate(date);
		for (Point point : pointList) {
			Log.i("btiLogChart", "db x:" + point.getX() + " temp:" + point.getBtiTemp());
		}

		// 画一条透明的曲线
		ArrayList<Entry> yValsNull = new ArrayList<Entry>();
		yValsNull.add(new Entry(1, 0));
		yValsNull.add(new Entry(600, 1));
		LineDataSet set = new LineDataSet(yValsNull, "");
		set.setColor(Color.parseColor("#00000000"));
		set.setDrawValues(false);// 不显示具体数值
		set.setDrawCircles(false);// 不显示具体圆圈
		dataSets.add(set);
		if (pointList != null && pointList.size() > 0) {
			int x = max_min;// 默认最大值600

			if (date.equals(chartDateList.get(chartDateList.size() - 1))) {// 最新的图表
				x = getXByTime(date);
				if (x == -100) {
					x = max_min;
				}
				XAxis xAxis = mChart.getXAxis();
				if (x <= max_min / 5) {
					currentMaxX = max_min / 5;
				} else if (x > max_min / 5 && x <= max_min / 5 * 2) {
					xVals = xVals240;
					currentMaxX = max_min / 5 * 2;
				} else if (x > max_min / 5 * 2 && x <= max_min / 5 * 3) {
					xVals = xVals360;
					currentMaxX = max_min / 5 * 3;
				} else if (x > max_min / 5 * 3 && x <= max_min / 5 * 4) {
					xVals = xVals480;
					currentMaxX = max_min / 5 * 4;
				} else if (x > max_min / 5 * 4 && x <= max_min) {
					xVals = xVals600;
					currentMaxX = max_min;
				}
				xAxis.setLabelsToSkip(currentMaxX / 12 - 1);
			} else {
				xVals = xVals600;
				XAxis xAxis = mChart.getXAxis();
				xAxis.setLabelsToSkip(max_min / 12 - 1);
			}

			// 3条曲线
			if (showProbe1Line) {
				initLineData(pointList, date, dataSets, x, 1);
			}
			if (showProbe2Line) {
				initLineData(pointList, date, dataSets, x, 2);
			}
			if (showTempLine) {
				initLineData(pointList, date, dataSets, x, 3);
			}
			LineData data = new LineData(xVals, dataSets);
			return data;
		}
		LineData data = new LineData(xVals, dataSets);
		return data;
	}

	private ArrayList<LineDataSet> initLineData(List<Point> pointList, String date, ArrayList<LineDataSet> dataSets, int x, int tempType) {
		String color = "";
		switch (tempType) {
		case 1:
			color = "#26ff4a";
			break;
		case 2:
			color = "#ffb951";
			break;
		case 3:
			color = "#00b6ed";
			break;
		}
		List<ArrayList<Entry>> yList = new ArrayList<ArrayList<Entry>>();
		yList.add(new ArrayList<Entry>());
		int yListIndex = 0;
		int pointListIndex = 0;
		boolean isFullLine = true;// 是实线
		for (int i = 0; i <= pointList.get(pointList.size() - 1).getX(); i++) {
			if (pointList.get(pointListIndex).getX() == i) {
				if (isFullLine) {
				} else {
					int firstX = yList.get(yListIndex).get(0).getXIndex();
					float firstY = yList.get(yListIndex).get(0).getVal();
					for (int j = firstX; j <= i; j++) {
						yList.get(yListIndex).add(new Entry(firstY, j));// 添加所有点
					}
					dataSets.add(getLine(false, yList.get(yListIndex), Color.parseColor(color)));// 添加虚线
					isFullLine = true;
					yList.add(new ArrayList<Entry>());
					yListIndex++;
				}
				int y = 0;
				switch (tempType) {
				case 1:
					y = pointList.get(pointListIndex).getProbe1Temp();
					Log.i("btichart", "1:"+y);
					break;
				case 2:
					y = pointList.get(pointListIndex).getProbe2Temp();
					Log.i("btichart", "2:"+y);
					break;
				case 3:
					y = pointList.get(pointListIndex).getBtiTemp();
					Log.i("btichart", "t:"+y);
					break;
				}
				if (isUnitF) {// 将摄氏度转成华氏度
					if(y != 0){
						y = (int) (y * 1.8 + 32);
					}
				}
				yList.get(yListIndex).add(new Entry(y, i));
				pointListIndex++;
			} else {
				if (isFullLine) {
					dataSets.add(getLine(true, yList.get(yListIndex), Color.parseColor(color)));// 添加实线
					isFullLine = false;
					ArrayList<Entry> yValsnext = new ArrayList<Entry>();// 虚线
					yList.add(yValsnext);
					yListIndex++;
					yList.get(yListIndex).add(yList.get(yListIndex - 1).get(yList.get(yListIndex - 1).size() - 1));
				} else {
				}
			}
			if (i == pointList.get(pointList.size() - 1).getX()) {
				dataSets.add(getLine(true, yList.get(yListIndex), Color.parseColor(color)));// 添加实线
			}
		}
		ArrayList<Entry> yValsEnd = new ArrayList<Entry>();// 虚线
		yValsEnd.add(yList.get(yListIndex).get(yList.get(yListIndex).size() - 1));
		int firstX = yValsEnd.get(0).getXIndex();
		float firstY = yValsEnd.get(0).getVal();
		for (int i = firstX + 1; i <= x; i++) {
			yValsEnd.add(new Entry(firstY, i));// 添加所有点
		}
		dataSets.add(getLine(false, yValsEnd, Color.parseColor(color)));// 添加虚线

		return dataSets;
	}

	private LineDataSet getLine(boolean isFullLine, ArrayList<Entry> yVals, int color) {
		LineDataSet set = new LineDataSet(yVals, "");
		if (!isFullLine) {
			set.enableDashedLine(5, 50, 0);
		}
		set.setColor(color);
		set.setLineWidth(2f);
		set.setCircleSize(3f);
		set.setDrawCircleHole(false);
		set.setValueTextSize(9f);
		set.setFillAlpha(65);
		set.setFillColor(Color.BLACK);
		set.setDrawValues(false);// 不显示具体数值
		set.setDrawCircles(false);// 不显示具体圆圈

		return set;
	}

	/**
	 * 更新温度曲线数量
	 */
	private void updateTempLineNum() {
		LineData dataCur = getNewLineData(chartDateList.get(currentChartListIndex), chartList.get(currentChartListIndex));// 先画当前页面
		chartList.get(currentChartListIndex).setData(dataCur);
		chartList.get(currentChartListIndex).invalidate();
		chartList.get(currentChartListIndex).setHighlightEnabled(false);
		for (int i = chartList.size() - 1; i >= 0; i--) {
			if (i != currentChartListIndex) {
				LineData data = null;
				if (i < chartList.size() - 1) {
					data = getNewLineData(chartDateList.get(i), chartList.get(i));
				} else {// 重画最新的图表
					data = getNewLineData(chartDateList.get(chartDateList.size() - 1), chartList.get(i));
				}
				chartList.get(i).setData(data);
				chartList.get(i).invalidate();
				chartList.get(i).setHighlightEnabled(false);
			}
		}
	}

	@Override
	public void initListener() {
		checkBoxA.setOnCheckedChangeListener(this);
		checkBoxB.setOnCheckedChangeListener(this);
		checkBoxBox.setOnCheckedChangeListener(this);

		stopBtn.setOnClickListener(singleClickListener);
		toAlertBtn.setOnClickListener(singleClickListener);
		toHomeBtn.setOnClickListener(singleClickListener);
		toSettingsBtn.setOnClickListener(singleClickListener);
	}

	@Override
	public void initData() {
		initChartView();
	}

	private OnSingleClickListener singleClickListener = new OnSingleClickListener() {
		@Override
		public void onSingleClick(View v) {
			switch (v.getId()) {
			case R.id.toSettingsBtn:
				mainActivity.changeToSettings();
				return;
			case R.id.toHomeBtn:
				mainActivity.backToHome();
				return;
			case R.id.toAlertBtn:
				mainActivity.changeToAlert();
				break;
			case R.id.stopBtn:
				mEventBus.post(new BleEvent(BleCommand.STOP_RECODE));
				mainActivity.backToHome();
				return;
			}
		}
	};

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.checkBoxA:
			if (isChecked) {
				showProbe1Line = true;
			} else {
				showProbe1Line = false;
			}
			break;
		case R.id.checkBoxB:
			if (isChecked) {
				showProbe2Line = true;
			} else {
				showProbe2Line = false;
			}
			break;
		case R.id.checkBoxBox:
			if (isChecked) {
				showTempLine = true;
			} else {
				showTempLine = false;
			}
			break;
		}
		updateTempLineNum();
	}

	int startTimeSec = 0;
	private Runnable timerRecord = new Runnable() {
		int probe1Temp = 0;
		int probe2Temp = 0;
		int btiTemp = 0;

		int lastX = -1;
		int probe1TempAddNum = 0, probe2TempAddNum = 0, btiTempAddNum = 0;

		@Override
		public void run() {

			if (MainActivity.instance == null) {
				startTimeSec = 0;
				probe1Temp = mReceiveData.getProbeTemperatureA() > 1000 ? 0 : mReceiveData.getProbeTemperatureA();
				probe2Temp = mReceiveData.getProbeTemperatureB() > 1000 ? 0 : mReceiveData.getProbeTemperatureB();
				btiTemp = mReceiveData.getProbeTemperature() > 1000 ? 0 : mReceiveData.getProbeTemperature();
				if (isUnitF) {// 将华氏度转成摄氏度
					if (probe1Temp != 0) {
						probe1Temp = (int) ((probe1Temp - 32) / 1.8);
					}
					if (probe2Temp != 0) {
						probe2Temp = (int) ((probe2Temp - 32) / 1.8);
					}
					if (btiTemp != 0) {
						btiTemp = (int) ((btiTemp - 32) / 1.8);
					}
				}
				probe1TempAddNum++;
				probe2TempAddNum++;
				btiTempAddNum++;
				return;
			}
			MainActivity.instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if(isInitUnit){
						int probe1 = mReceiveData.getProbeTemperatureA() > 1000 ? 0 : mReceiveData.getProbeTemperatureA();
						int probe2 = mReceiveData.getProbeTemperatureB() > 1000 ? 0 : mReceiveData.getProbeTemperatureB();
						int temp = mReceiveData.getProbeTemperature() > 1000 ? 0 : mReceiveData.getProbeTemperature();
						
						if (isUnitF) {// 将华氏度转成摄氏度
							if (probe1 != 0) {
								probe1 = (int) ((probe1 - 32) / 1.8);
							}
							if (probe2 != 0) {
								probe2 = (int) ((probe2 - 32) / 1.8);
							}
							if (temp != 0) {
								temp = (int) ((temp - 32) / 1.8);
							}
						}
						
						Log.i("btichart", isUnitF+"1:"+probe1Temp+" 2:"+probe2Temp+" t:"+btiTemp);
						
						probe1Temp += probe1;
						probe2Temp += probe2;
						btiTemp += temp;
						
						probe1TempAddNum++;
						probe2TempAddNum++;
						btiTempAddNum++;
						
						if (chartDateList != null) {
							int x = getXByTime(chartDateList.get(chartDateList.size() - 1));
							if (x == -100) {// 重画一张新图表
//							SharedPreferences sharedPreferences = mainActivity.getSharedPreferences(Constants.CHARTS_LIST, Context.MODE_PRIVATE);
//							String charts_list = sharedPreferences.getString(Constants.CHARTS_LIST, "");
								String charts_list = "";
								Point p_charts_list = pointDao.findPointByX(12345);
								if(p_charts_list != null){
									charts_list = p_charts_list.getMyDateList();
								}
								String currentTime = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
								if (chartDateList.size() == 4) {// 存储的图表超过4个
									// 删掉第一个
									List<Point> pointList = pointDao.getPointsByDate(charts_list.substring(0, 13));
									for (Point point : pointList) {
										point.delete();
									}
									// 添加最新一个
									currentTime = charts_list.substring(13) + "#" + currentTime;
								} else {
									currentTime = charts_list + "#" + currentTime;
								}
								
								//添加第一个数据
								Point point = new Point(0, probe1Temp / (probe1TempAddNum), probe2Temp / (probe2TempAddNum), btiTemp / (btiTempAddNum), currentTime,"");
								point.insert();
								
								lastX = 0;
								probe1Temp = 0;
								probe2Temp = 0;
								btiTemp = 0;
								probe1TempAddNum = probe2TempAddNum = btiTempAddNum = 0;
								
								p_charts_list.setMyDateList(currentTime);
								p_charts_list.update();
								initCharts();
								
							} else if (x != lastX) {// 之前数据库没添加过，现在添加
								lastX = x;// 记录已添加的x
								
								String date = chartDateList.get(chartDateList.size() - 1);
								
								Point p = pointDao.findPointByDateAndX(date, x);
								
								Log.i("btichart", "1:"+probe1Temp / (probe1TempAddNum)+" 2:"+probe2Temp / (probe2TempAddNum)+" t:"+btiTemp / (btiTempAddNum));
								
								if (p == null) {// 数据库未插入此数据，现在插入
									Point point = new Point(x, probe1Temp / (probe1TempAddNum), probe2Temp / (probe2TempAddNum), btiTemp / (btiTempAddNum), date,"");
									point.insert();
								}
								
								probe1Temp = 0;
								probe2Temp = 0;
								btiTemp = 0;
								probe1TempAddNum = probe2TempAddNum = btiTempAddNum = 0;
								
								if(chartList != null && chartList.size() > 0){
									LineData data = getNewLineData(chartDateList.get(chartDateList.size() - 1), chartList.get(chartList.size() - 1));
									chartList.get(chartList.size() - 1).setData(data);
									chartList.get(chartList.size() - 1).invalidate();
									chartList.get(chartList.size() - 1).setHighlightEnabled(false);
								}
							}
						}
					}

					// 根据x控制密度。。。。。。。。。。。。。

				}
			});
		}
	};

	private int getXByTime(String previousTime) {
		String currentTime = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

		int min_previous = Integer.parseInt(previousTime.substring(10, 12));
		int hour_previous = Integer.parseInt(previousTime.substring(8, 10));
		int day_previous = Integer.parseInt(previousTime.substring(6, 8));
		int mon_previous = Integer.parseInt(previousTime.substring(4, 6));
		int year_previous = Integer.parseInt(previousTime.substring(0, 4));

		int min_current = Integer.parseInt(currentTime.substring(10, 12));
		int hour_current = Integer.parseInt(currentTime.substring(8, 10));
		int day_current = Integer.parseInt(currentTime.substring(6, 8));
		int mon_current = Integer.parseInt(currentTime.substring(4, 6));
		int year_current = Integer.parseInt(currentTime.substring(0, 4));

		if (year_current == year_previous && mon_current == mon_previous && day_current == day_previous) {// 在同一天，可直接比较分钟
			int minNum = min_current + hour_current * 60 - min_previous - hour_previous * 60;
			if (minNum <= max_min) {// 在600分钟内
				return minNum;
			} else {
				return -100;// 超过600分钟，重画一张新的图表
			}
		} else {// 不在同一天，判断是否是第二天
			boolean isNextDayStatus1 = year_current == year_previous && mon_current == mon_previous && day_current == day_previous + 1;// 同一个月的第二天
			boolean isNextDayStatus2 = false;// 上个月的最后一天和这个月的第一天
			boolean isNextDayStatus3 = year_current == year_previous + 1 && mon_current == 1 && mon_previous == 12 && day_current == 1 && day_previous == 31;// 上一年的最后一天和这一年的第一天
			if (year_current == year_previous) {// 同一年
				if ((mon_previous == 1 || mon_previous == 3 || mon_previous == 5 || mon_previous == 7 || mon_previous == 8 || mon_previous == 10) && mon_current == mon_previous + 1 && day_current * 31 == day_previous) {
					// 1,3,5,7,8,10月的最后一天，和下个月的第一天
					isNextDayStatus2 = true;
				} else if ((mon_previous == 4 || mon_previous == 6 || mon_previous == 9 || mon_previous == 11) && mon_current == mon_previous + 1 && day_current * 30 == day_previous) {
					// 4,6,9,7,11月的最后一天，和下个月的第一天
					isNextDayStatus2 = true;
				} else if (mon_previous == 2 && mon_current == mon_previous + 1 && day_current == 1) {
					if (year_current % 4 == 0) {// 闰年，2月有29号
						if (day_previous == 29) {// 2月的最后一天，和下一个月的第一天
							isNextDayStatus2 = true;
						}
					} else {
						if (day_previous == 28) {// 2月的最后一天，和下一个月的第一天
							isNextDayStatus2 = true;
						}
					}
				}
			}
			if (isNextDayStatus1 || isNextDayStatus2 || isNextDayStatus3) {// 是第二天，可以比较时间
				int minNum = 24 * 60 + min_current + hour_current * 60 - min_previous - hour_previous * 60;
				if (minNum <= max_min) {// 在600分钟内
					return minNum;
				} else {
					return -100;// 超过600分钟，重画一张新的图表
				}
			} else {// 不是第二天，重画一张新的图表
				return -100;
			}
		}
	}

	private void initChartView() {
		int trans = resources.getColor(android.R.color.transparent);
		int white = resources.getColor(R.color.white);
		int black = resources.getColor(R.color.black);
		int gray = resources.getColor(R.color.dark_gray_oven);
		int red = resources.getColor(R.color.red_oven);
		/*
		 * ovenChart.setBackgroundColor(trans); ovenChart.setGridVis(true, true,
		 * false); ovenChart.setGridColor(white, gray, red);
		 * ovenChart.setGridWidthDip(1, 0.5f, 1); ovenChart.setTextVis(true,
		 * true, true, true); ovenChart.setTextStyle(white, 12);
		 * ovenChart.setXgrid(false, 0, 130, 13); ovenChart.setYgrid(false, 0,
		 * 550, 11);
		 * 
		 * int minutes = startTimeSec / 60; if (minutes >= 1440) {
		 * update_1440(); } else if (minutes >= 720) { update_720(); } else if
		 * (minutes >= 360) { update_360(); } else if (minutes >= 120) {
		 * update_120(); }
		 * 
		 * // add lines to chart ovenChart.addSerie(aSeria);
		 * ovenChart.addSerie(bSeria); ovenChart.addSerie(boxSeria);
		 */

		Log.d("ovenChart", "initChartView");
	}

	public ChartFragment() {
		chartDateList = new ArrayList<String>();
		if(chartList == null){
			chartList = new ArrayList<LineChart>();
		}
		String charts_list = "";
		pointDao = PointDao.getInstance();
		Point p_charts_list = pointDao.findPointByX(12345);
		if(p_charts_list != null){
			charts_list = p_charts_list.getMyDateList();
		}
		if (!charts_list.equals("")) {
			for (String chartDate : charts_list.split("#")) {
				chartDateList.add(chartDate);
			}
		} else {
			String currentTime = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
			Point p = new Point(12345, 0, 0, 0, "12345", currentTime);
			p.insert();
			chartDateList.add(currentTime);
			currentChartListIndex = 0;
		}
		TimerUtil.setInterval(timerRecord, 1000);
	}

	@Override
	public void onChartLongPressed(MotionEvent me) {
		Log.i("LongPress", "Chart longpressed.");
	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {
		Log.i("DoubleTap", "Chart double-tapped.");
	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {
		Log.i("SingleTap", "Chart single-tapped.");
	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
		Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
		Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
		XAxis xAxis = chartList.get(currentChartListIndex).getXAxis();
		if (chartList.get(currentChartListIndex).getViewPortHandler().canZoomOutMoreX()) {
			viewPager.setNoScroll(true);
			xAxis.resetLabelsToSkip();
		} else {
			viewPager.setNoScroll(false);
			if (currentChartListIndex == chartList.size() - 1) {
				if (currentMaxX == 0) {
					currentMaxX = max_min / 5 - 1;
				}
				xAxis.setLabelsToSkip(currentMaxX / 12 - 1);
			} else {
				xAxis.setLabelsToSkip(max_min / 12 - 1);
			}
		}
	}

	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY) {
		Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
	}

	class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return chartList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			// super.destroyItem(container, position, object);
			((NoScrollViewPager) container).removeView(chartList.get(position));
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// TODO Auto-generated method stub
			return "title";
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			((NoScrollViewPager) container).addView(chartList.get(position));
			return chartList.get(position);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public void setPrimaryItem(View container, int position, Object object) {
			currentChartListIndex = position;
		}

	}

}
