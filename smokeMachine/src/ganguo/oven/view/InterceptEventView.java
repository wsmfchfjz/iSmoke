package ganguo.oven.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * @Description 拦截所有touch事件控件
 * @author Jeff
 * @date 14.6.13
 */
public class InterceptEventView extends RelativeLayout{

	public InterceptEventView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public InterceptEventView(Context context) {
		super(context);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
	
}
