package com.example.zhangyan.receivercompont;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
        import android.view.Window;
        import android.view.WindowManager;
        import android.widget.EditText;
        import android.widget.TableLayout;

public class GetIP extends Activity {
    String ipname = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ����ȫ��
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        final Builder builder = new AlertDialog.Builder(this);   //����һ��AlertDialog.Builder����
        builder.setTitle("登录");                          // ���öԻ���ı���

        //װ��/res/layout/login.xml���沼��
        TableLayout loginForm = (TableLayout)getLayoutInflater().inflate( R.layout.login, null);
        final EditText iptext = (EditText)loginForm.findViewById(R.id.ipedittext);
        builder.setView(loginForm);                              // ���öԻ�����ʾ��View����
        // Ϊ�Ի�������һ������¼����ť
        builder.setPositiveButton("确定"
                // Ϊ��ť���ü�����
                , new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //�˴���ִ�е�¼����
                        ipname = iptext.getText().toString().trim();
                        Bundle data = new Bundle();
                        data.putString("ipname",ipname);
                        Intent intent = new Intent(GetIP.this,MainActivity.class);
                        intent.putExtras(data);
                        startActivity(intent);
                    }
                });
        // Ϊ�Ի�������һ����ȡ������ť
        builder.setNegativeButton("取消"
                ,  new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //ȡ����¼�������κ����顣
                        System.exit(1);
                    }
                });
        //����������ʾ�Ի���
        builder.create().show();
    }
}
