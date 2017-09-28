package ganguo.oven.fragment;

import ganguo.oven.R;
import ganguo.oven.utils.UIUtils;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainFragmentUtil {
	
	public static int fToC(int probe){
		int c = 0;
		switch (probe) {
		case 145:
			c = 63;
			break;
		case 160:
			c = 71;
			break;
		case 165:
			c = 74;
			break;
		case 170:
			c = 77;
			break;
		default:
			c = UIUtils.fahrenheitToCelsius(probe);
			break;
		}
		return c;
	}
	
	public static int cToF(int probe){
		int f = 0;
		switch (probe) {
		case 63:
			f = 145;
			break;
		case 71:
			f = 160;
			break;
		case 74:
			f = 165;
			break;
		case 77:
			f = 170;
			break;
		default:
			f = UIUtils.celsiusToFahrenheit(probe);
			break;
		}
		return f;
	}
	
	
	public static RelativeLayout[] getMeatImg(Context mainActivity){
		RelativeLayout[] rlt = new RelativeLayout[10];
		
		RelativeLayout probe1_rlt1 = new RelativeLayout(mainActivity);
		ImageView probe1_iv1 = new ImageView(mainActivity);
		probe1_iv1.setBackgroundResource(R.drawable.sheep);
		RelativeLayout.LayoutParams probe1_lp_iv1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		probe1_lp_iv1.addRule(RelativeLayout.CENTER_IN_PARENT);
		probe1_rlt1.addView(probe1_iv1, probe1_lp_iv1);

		RelativeLayout probe1_rlt2 = new RelativeLayout(mainActivity);
		ImageView probe1_iv2 = new ImageView(mainActivity);
		probe1_iv2.setBackgroundResource(R.drawable.cattle);
		RelativeLayout.LayoutParams probe1_lp_iv2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		probe1_lp_iv2.addRule(RelativeLayout.CENTER_IN_PARENT);
		probe1_rlt2.addView(probe1_iv2, probe1_lp_iv2);

		RelativeLayout probe1_rlt3 = new RelativeLayout(mainActivity);
		ImageView probe1_iv3 = new ImageView(mainActivity);
		probe1_iv3.setBackgroundResource(R.drawable.fish);
		RelativeLayout.LayoutParams probe1_lp_iv3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		probe1_lp_iv3.addRule(RelativeLayout.CENTER_IN_PARENT);
		probe1_rlt3.addView(probe1_iv3, probe1_lp_iv3);

		RelativeLayout probe1_rlt4 = new RelativeLayout(mainActivity);
		ImageView probe1_iv4 = new ImageView(mainActivity);
		probe1_iv4.setBackgroundResource(R.drawable.pig);
		RelativeLayout.LayoutParams probe1_lp_iv4 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		probe1_lp_iv4.addRule(RelativeLayout.CENTER_IN_PARENT);
		probe1_rlt4.addView(probe1_iv4, probe1_lp_iv4);

		RelativeLayout probe1_rlt5 = new RelativeLayout(mainActivity);
		ImageView probe1_iv5 = new ImageView(mainActivity);
		probe1_iv5.setBackgroundResource(R.drawable.chicken);
		RelativeLayout.LayoutParams probe1_lp_iv5 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		probe1_lp_iv5.addRule(RelativeLayout.CENTER_IN_PARENT);
		probe1_rlt5.addView(probe1_iv5, probe1_lp_iv5);
		
		RelativeLayout probe2_rlt1 = new RelativeLayout(mainActivity);
		ImageView probe2_iv1 = new ImageView(mainActivity);
		probe2_iv1.setBackgroundResource(R.drawable.sheep);
		RelativeLayout.LayoutParams probe2_lp_iv1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		probe2_lp_iv1.addRule(RelativeLayout.CENTER_IN_PARENT);
		probe2_rlt1.addView(probe2_iv1, probe2_lp_iv1);

		RelativeLayout probe2_rlt2 = new RelativeLayout(mainActivity);
		ImageView probe2_iv2 = new ImageView(mainActivity);
		probe2_iv2.setBackgroundResource(R.drawable.cattle);
		RelativeLayout.LayoutParams probe2_lp_iv2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		probe2_lp_iv2.addRule(RelativeLayout.CENTER_IN_PARENT);
		probe2_rlt2.addView(probe2_iv2, probe2_lp_iv2);

		RelativeLayout probe2_rlt3 = new RelativeLayout(mainActivity);
		ImageView probe2_iv3 = new ImageView(mainActivity);
		probe2_iv3.setBackgroundResource(R.drawable.fish);
		RelativeLayout.LayoutParams probe2_lp_iv3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		probe2_lp_iv3.addRule(RelativeLayout.CENTER_IN_PARENT);
		probe2_rlt3.addView(probe2_iv3, probe2_lp_iv3);

		RelativeLayout probe2_rlt4 = new RelativeLayout(mainActivity);
		ImageView probe2_iv4 = new ImageView(mainActivity);
		probe2_iv4.setBackgroundResource(R.drawable.pig);
		RelativeLayout.LayoutParams probe2_lp_iv4 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		probe2_lp_iv4.addRule(RelativeLayout.CENTER_IN_PARENT);
		probe2_rlt4.addView(probe2_iv4, probe2_lp_iv4);
		
		RelativeLayout probe2_rlt5 = new RelativeLayout(mainActivity);
		ImageView probe2_iv5 = new ImageView(mainActivity);
		probe2_iv5.setBackgroundResource(R.drawable.chicken);
		RelativeLayout.LayoutParams probe2_lp_iv5 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		probe2_lp_iv5.addRule(RelativeLayout.CENTER_IN_PARENT);
		probe2_rlt5.addView(probe2_iv5, probe2_lp_iv5);
		
		rlt[0] = probe1_rlt1;
		rlt[1] = probe1_rlt2;
		rlt[2] = probe1_rlt3;
		rlt[3] = probe1_rlt4;
		rlt[4] = probe1_rlt5;
		rlt[5] = probe2_rlt1;
		rlt[6] = probe2_rlt2;
		rlt[7] = probe2_rlt3;
		rlt[8] = probe2_rlt4;
		rlt[9] = probe2_rlt5;
		
		return rlt;
	}
	
}
