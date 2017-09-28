package ganguo.oven.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

import ganguo.oven.R;

/**
 * Created by Tony on 3/17/15.
 */
public class TestActivity extends BaseActivity {
    private Button action_test;

    @Override
    public void beforeInitView() {
        setContentView(R.layout.activity_test);
    }

    @Override
    public void initView() {
        action_test = (Button) findViewById(R.id.action_test);
    }

    @Override
    public void initListener() {
        action_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlertTip(0x0b);
            }
        });
    }

    @Override
    public void initData() {

    }

    /**
     * 提示温度已经超过
     *
     * @param text
     */
    public void showAlertDialog(String title, String text) {
        AlertDialog alertDialog = new AlertDialog
                .Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        alertDialog.show();
    }

    private void setAlertTip(int code) {
        switch (code) {
            case 0xe1:
                // 0xe1:箱体探头错误
                showAlertDialog("WARNING", "CABLES NOT CONNECTED PROPERLY");
                break;
            case 0xe2:
                // 0xe2:探头a短路
                showAlertDialog("WARNING", "Probe 1:\nCABLES NOT CONNECTED PROPERLY");
                break;
            case 0xe3:
                // 0xe3:探头b短路
                showAlertDialog("WARNING", "Probe 2:\nCABLES NOT CONNECTED PROPERLY");
                break;
            case 0xe4:
                // 0xe4:马达错误
                showAlertDialog("WARNING", "PLEASE CHECK YOUR SMOKER,JAMMED BISQUETTE");
                break;
            case 0xe5:
                // 0xe5:箱体探头超温错误
                showAlertDialog("WARNING", "THE OVEN IS TOO HOT,PLEASE CHECK THE OVEN");
                break;
            case 0x06:
                // 0x06:a探头接近设定温度提示
                showAlertDialog("ALERT", getText(R.string.probe1_temp_alert).toString());
                break;
            case 0x07:
                // 0x07:b探头接近设定温度提示
                showAlertDialog("ALERT", getText(R.string.probe2_temp_alert).toString());
                break;
            case 0x08:
                // 0x08:a探头到达温度
                showAlertDialog("ALERT", "Probe 1:\nYour food is done.ENJOY!");
                break;
            case 0x09:
                // 0x09:b探头到达温度
                showAlertDialog("ALERT", "Probe 2:\nYour food is done.ENJOY!");
                break;
            case 0x0a:
                // 0x09:定时到
                showAlertDialog("ALERT", "Your food is done.ENJOY!");
                break;
            case 0x0b:
                // 0x0b:加水
                showAlertDialog("REMINDER", "PLEASE CHECK AND REFILL THE WATER BOWL");
                break;
        }
    }
}
