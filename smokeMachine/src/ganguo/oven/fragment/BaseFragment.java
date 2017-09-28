package ganguo.oven.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ganguo.oven.interfaces.Operational;

/**
 * Created by Wilson on 14-7-9.
 */
public abstract class BaseFragment extends Fragment implements Operational {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), null, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        beforeInitView();
        initView();
        initListener();
        initData();
    }

    public abstract int getLayoutId();
    public abstract String getMyTAG();
}
