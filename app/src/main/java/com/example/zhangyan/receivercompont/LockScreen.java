package com.example.zhangyan.receivercompont;

/**
 * Created by zhangyan on 2017/5/22.
 */
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

public class LockScreen {
    Point mLpsize;
    //    Button mBtnUnlock;
    ViewGroup mView;
    final WindowManager mWindowManager;
    final WindowManager.LayoutParams mLp;

    public LockScreen(Context mContext) {
        mView= (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.lock_screen_view,null);
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mLp=new WindowManager.LayoutParams();
//        mBtnUnlock= (Button) mView.findViewById(R.id.btn_unlock);

//        mBtnUnlock.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                show(false);
//            }
//        });
        initLp();
    }
    private void initLp(){
        mLp.type=WindowManager.LayoutParams.TYPE_SYSTEM_ERROR; //然这个任务无理由显示在上端。
        mLp.format= PixelFormat.RGBA_8888;//  只是改变这个的颜色

        mLp.flags |=WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD // 当只有它不是安全锁住键盘时，当前设置会导致键盘关闭
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  // 就算锁住屏幕页显示该窗口
                | WindowManager.LayoutParams.FLAG_FULLSCREEN     // 全屏显示
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;  // 这个窗口占满整个屏幕，不留任何边界

        mLp.systemUiVisibility=View.SYSTEM_UI_FLAG_LOW_PROFILE; //隐藏BAR栏
        mLp.systemUiVisibility = View.STATUS_BAR_HIDDEN;
//        mLp.systemUiVisibility=View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        mLp.width=WindowManager.LayoutParams.MATCH_PARENT;
        mLp.height=WindowManager.LayoutParams.MATCH_PARENT; //充满整个父容器
    }

    public void show(boolean flag){
        if(flag){
            mWindowManager.addView(mView,mLp);
        }else {
            mWindowManager.removeView(mView);
        }
    }
}
//
//public class StartupActivity extends Activity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_startup);
//
//        /*
//         * 隐藏运行Android 4.xxx系统的平板的屏幕下方的状态栏需要root权限
//         */
//        closeBar();
//    }
//
//    @Override
//    protected void onDestroy() {
//        showBar();
//        super.onDestroy();
//    }
//
//    /**
//     * 关闭Android导航栏，实现全屏
//     */
//    private void closeBar() {
//        try {
//            String command;
//            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib service call activity 42 s16 com.android.systemui";
//            ArrayList<String> envlist = new ArrayList<String>();
//            Map<String, String> env = System.getenv();
//            for (String envName : env.keySet()) {
//                envlist.add(envName + "=" + env.get(envName));
//            }
//            String[] envp = envlist.toArray(new String[0]);
//            Process proc = Runtime.getRuntime().exec(
//                    new String[] { "su", "-c", command }, envp);
//            proc.waitFor();
//        } catch (Exception ex) {
//            // Toast.makeText(getApplicationContext(), ex.getMessage(),
//            // Toast.LENGTH_LONG).show();
//        }
//    }
//
//    /**
//     * 显示导航栏
//     */
//    public static void showBar() {
//        try {
//            String command;
//            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
//            ArrayList<String> envlist = new ArrayList<String>();
//            Map<String, String> env = System.getenv();
//            for (String envName : env.keySet()) {
//                envlist.add(envName + "=" + env.get(envName));
//            }
//            String[] envp = envlist.toArray(new String[0]);
//            Process proc = Runtime.getRuntime().exec(
//                    new String[] { "su", "-c", command }, envp);
//            proc.waitFor();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}