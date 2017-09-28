package ganguo.oven.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import ganguo.oven.interfaces.Operational;

/**
 * Created by Wilson on 14-7-8.
 */
public abstract class BaseActivity extends FragmentActivity implements Operational {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beforeInitView();
        initView();
        initListener();
        initData();
    }

}
