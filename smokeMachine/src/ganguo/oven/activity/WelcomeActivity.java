package ganguo.oven.activity;

import android.content.Intent;
import android.widget.ImageView;

import ganguo.oven.R;


public class WelcomeActivity extends BaseActivity {

    private boolean isStopAnim = false;
    private ImageView mImageView;

    @Override
    public void beforeInitView() {
        setContentView(R.layout.activity_welcome);
    }

    @Override
    public void initView() {
        mImageView = (ImageView) findViewById(R.id.image_view);
    }

    @Override
    public void initListener() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                isStopAnim = true;
//                startActivity(new Intent(WelcomeActivity.this, DeviceActivity.class));
//                finish();
//            }
//        }, 8000);
    }

    @Override
    public void initData() {
        play(0);
    }

    private void toDeviceActivity() {
        isStopAnim = true;
        startActivity(new Intent(WelcomeActivity.this, DeviceActivity.class));
        finish();
    }

    private void play(final int pFrameNo) {
        mImageView.postDelayed(new Runnable() {
            public void run() {

                if (pFrameNo == mFrameRess.length - 1) {
                    toDeviceActivity();
                    return;
                }

                mImageView.setImageResource(mFrameRess[pFrameNo]);
                play(pFrameNo + 1);
            }
        }, 30);
    }

    private int[] mFrameRess = {
            R.drawable.landing_20003,
            R.drawable.landing_20005,
            R.drawable.landing_20007,
            R.drawable.landing_20009,
            R.drawable.landing_20011,
            R.drawable.landing_20013,
            R.drawable.landing_20015,
            R.drawable.landing_20017,
            R.drawable.landing_20019,
            R.drawable.landing_20021,
            R.drawable.landing_20023,
            R.drawable.landing_20025,
            R.drawable.landing_20027,
            R.drawable.landing_20029,
            R.drawable.landing_20031,
            R.drawable.landing_20033,
            R.drawable.landing_20035,
            R.drawable.landing_20037,
            R.drawable.landing_20039,
            R.drawable.landing_20041,
            R.drawable.landing_20043,
            R.drawable.landing_20045,
            R.drawable.landing_20047,
            R.drawable.landing_20049,
            R.drawable.landing_20051,
            R.drawable.landing_20053,
            R.drawable.landing_20055,
            R.drawable.landing_20057,
            R.drawable.landing_20059,
            R.drawable.landing_20061,
            R.drawable.landing_20063,
            R.drawable.landing_20065,
            R.drawable.landing_20067,
            R.drawable.landing_20069,
            R.drawable.landing_20071,
            R.drawable.landing_20073,
            R.drawable.landing_20075,
            R.drawable.landing_20077,
            R.drawable.landing_20079,
            R.drawable.landing_20081,
            R.drawable.landing_20083,
            R.drawable.landing_20085,
            R.drawable.landing_20087,
            R.drawable.landing_20089,
            R.drawable.landing_20091,
            R.drawable.landing_20093,
            R.drawable.landing_20095,
            R.drawable.landing_20097,
            R.drawable.landing_20099,
            R.drawable.landing_20101,
            R.drawable.landing_20103,
            R.drawable.landing_20105,
            R.drawable.landing_20107,
            R.drawable.landing_20109,
            R.drawable.landing_20111,
            R.drawable.landing_20113,
            R.drawable.landing_20115,
            R.drawable.landing_20117,
            R.drawable.landing_20119,
            R.drawable.landing_20121,
            R.drawable.landing_20123,
            R.drawable.landing_20125,
            R.drawable.landing_20127,
            R.drawable.landing_20129,
            R.drawable.landing_20131,
            R.drawable.landing_20133,
            R.drawable.landing_20135,
            R.drawable.landing_20137,
            R.drawable.landing_20139,
            R.drawable.landing_20141,
            R.drawable.landing_20143,
            R.drawable.landing_20145,
            R.drawable.landing_20147,
            R.drawable.landing_20149,
            R.drawable.landing_20151,
            R.drawable.landing_20153,
            R.drawable.landing_20155,
            R.drawable.landing_20157,
            R.drawable.landing_20159,
            R.drawable.landing_20161,
            R.drawable.landing_20163,
            R.drawable.landing_20165,
            R.drawable.landing_20167,
            R.drawable.landing_20169,
            R.drawable.landing_20171,
            R.drawable.landing_20173,
            R.drawable.landing_20175,
            R.drawable.landing_20177,
            R.drawable.landing_20179,
            R.drawable.landing_20181,
            R.drawable.landing_20183,
            R.drawable.landing_20185,
            R.drawable.landing_20187,
            R.drawable.landing_20189,
            R.drawable.landing_20191,
            R.drawable.landing_20193,
            R.drawable.landing_20195,
            R.drawable.landing_20197,
            R.drawable.landing_20199,
            R.drawable.landing_20201,
            R.drawable.landing_20203,
            R.drawable.landing_20205,
            R.drawable.landing_20207,
            R.drawable.landing_20209,
            R.drawable.landing_20211,
            R.drawable.landing_20213,
            R.drawable.landing_20215
    };
}
