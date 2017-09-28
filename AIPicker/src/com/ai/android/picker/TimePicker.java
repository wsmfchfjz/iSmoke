package com.ai.android.picker;

import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;

import com.ai.android.picker.NumberPicker.OnValueChangeListener;

/**
 * @author Wilson
 *
 */
public class TimePicker extends FrameLayout {

	private Context mContext;
	private NumberPicker hourPicker;
	private NumberPicker minPicker;

	private Calendar mCalendar;

	public TimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mCalendar = Calendar.getInstance();
		((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.time_picker, this, true);
		hourPicker = (NumberPicker) findViewById(R.id.time_hours);
		minPicker = (NumberPicker) findViewById(R.id.time_minutes);

		hourPicker.setMinValue(0);
		hourPicker.setMaxValue(20);
		hourPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);

		minPicker.setMinValue(0);
		minPicker.setMaxValue(30);
		minPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);

		minPicker.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				mCalendar.set(Calendar.MINUTE, newVal);

			}
		});

		hourPicker.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				mCalendar.set(Calendar.HOUR, newVal);
			}
		});

	}

	public TimePicker(Context context) {
		this(context, null);
	}

	public String getTime() {
		return hourPicker.getValue() + ":" + minPicker.getValue();
	}

	public int getHourOfDay() {
		return hourPicker.getValue();
	}

	public int getHour() {
		return hourPicker.getValue();
	}

	public int getMinute() {
		return mCalendar.get(Calendar.MINUTE);
	}

	public void setCalendar(Calendar calendar) {
		this.mCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
		this.mCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
	}

}
