package com.example.superwe;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationActivity extends AppCompatActivity {
    @Override
    public void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        //初始化配置项数据
        List<Configuration> list = new ArrayList<>();
//        if(lacksPermission(this,Manifest.permission.BIND_ACCESSIBILITY_SERVICE))list.add(new Configuration("无障碍服务","关闭"));
//        else list.add(new Configuration("无障碍服务","开启"));
//        if(lacksPermission(this,Manifest.permission.SYSTEM_ALERT_WINDOW))list.add(new Configuration("悬浮窗权限","关闭"));
//        else list.add(new Configuration("悬浮窗权限","开启"));
        list.add(new Configuration("自动点赞", Hawk.get(Constant.Companion.getAUTO_ZAN())?"开启":"关闭"));
        list.add(new Configuration("自动收红包", Hawk.get(Constant.Companion.getAUTO_RECEIVE_LUCKY_MONEY())?"开启":"关闭"));
        boolean autoReply = Hawk.get(Constant.Companion.getAUTO_RECEIVE_LUCKY_MONEY());
        list.add(new Configuration("自动回复", autoReply?"开启":"关闭"));
        if(autoReply)list.add(new Configuration("自动回复内容",Hawk.get(Constant.Companion.getAUTO_REPLY_CONTENT())));
        list.add(new Configuration("导出文件位置",Constant.Companion.getEXPORT_CONTACT_LOACTION()));

        //渲染list
        ListView tableListView = findViewById(R.id.configuration_list);
        ConfigurationTableAdapter adapter = new ConfigurationTableAdapter(this, list);
        tableListView.setAdapter(adapter);
        //渲染title
        ViewGroup tableTitle = findViewById(R.id.configuration_table_title);
        tableTitle.setBackgroundColor(Color.rgb(177, 173, 172));
    }

    /**
     * 判断是否缺少权限
     */
    private static boolean lacksPermission(Context mContexts, String permission) {
        return ContextCompat.checkSelfPermission(mContexts, permission) ==
                PackageManager.PERMISSION_DENIED;
    }
}
