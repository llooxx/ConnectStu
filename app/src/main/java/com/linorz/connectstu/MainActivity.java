package com.linorz.connectstu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private ViewGroup lay;
    private ScrollView sv_text;
    private EditText ed_username, ed_password;
    private TextView tv_result;
    private Button btn_login;
    private String username_str, password_str, ip;
    private int overTime = 720;
    private boolean hasNext = true;
    private String ip_pre = "211.87";    //山大无线STU的IP网段
    private Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            if (isWifiConnected()) {
                if (ip != null) {
                    String currentTime = System.currentTimeMillis() + "";
                    String response = connect(username_str, password_str, ip, currentTime, overTime + "");
                    if (response == null) {
                        linorzPrint("服务器没回复");
                    } else if (response.contains("用户上线成功")) {
                        linorzPrint("用户上线成功,在线时长为" + (overTime / 60) + "分钟");
                    } else if (response.contains("您已经建立了连接")) {
                        linorzPrint("您已经建立了连接,无需重复登录");
                    } else if (response.contains("用户不存在")) {
                        linorzPrint("用户不存在，请检查学号是否正确");
                    } else if (response.contains("用户密码错误")) {
                        hasNext = false;
                        linorzPrint("用户密码错误");
                    } else if (response.contains("在线用户数量限制")) {
                        hasNext = false;
                        linorzPrint("在线用户数量限制");
                    } else {
                        hasNext = false;
                        linorzPrint("未知错误");
                    }
                } else {
                    linorzPrint("当前已经掉线，正在尝试重新连接");
                    ip = getHostIP();
                }
            } else {
                hasNext = false;
                linorzPrint("当前已经掉线，请确保连接上了QLSC_STU网络");
            }
            if (hasNext) {
                handler.sendMessageDelayed(Message.obtain(handler, 2), 10000);
            } else {
                handler.sendMessageDelayed(Message.obtain(handler, 4), 100);
            }
        }
    };
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    username_str = ed_username.getText().toString();
                    password_str = ed_password.getText().toString();
                    InfoTool.saveLoginInfo(MainActivity.this, new InfoTool.LoginInfo(username_str, password_str));
                    ip = getHostIP();
                    lay.setVisibility(View.GONE);
                    hasNext = true;
                    handler.sendMessageDelayed(Message.obtain(handler, 2), 100);
                    break;
                case 2:
                    new Thread(networkTask).start();
                    break;
                case 3:
                    tv_result.append(message.obj.toString());
                    sv_text.fullScroll(ScrollView.FOCUS_DOWN);
                    break;
                case 4:
                    lay.setVisibility(View.VISIBLE);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lay = (ViewGroup) findViewById(R.id.login_lay);
        ed_username = (EditText) findViewById(R.id.login_username_ed);
        ed_password = (EditText) findViewById(R.id.login_password_ed);
        btn_login = (Button) findViewById(R.id.login_login_btn);
        tv_result = (TextView) findViewById(R.id.result_tv);
        sv_text = (ScrollView) findViewById(R.id.text_sv);
        ed_username.setText(InfoTool.getLoginInfo(this).getUsername());
        ed_password.setText(InfoTool.getLoginInfo(this).getPassword());
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.sendMessageDelayed(Message.obtain(handler, 1), 100);
            }
        });

    }

    public String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip_str = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip_str)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("EEE", "SocketException");
            e.printStackTrace();
        }
        if (!hostIp.contains(ip_pre))
            hostIp = null;
        return hostIp;
    }

    public String connect(String username, String password, String ip, String currentTime, String overTime) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", username);
        map.put("password", password);
        map.put("serverType", "");
        map.put("isSavePass", "on");
        map.put("Submit1", "");
        map.put("Language", "Chinese");
        map.put("ClientIP", ip);
        map.put("timeoutvalue", "45");
        map.put("heartbeat", "240");
        map.put("fastwebornot", "False");
        map.put("StartTime", currentTime);
        map.put("shkOvertime", overTime);
        map.put("strOSName", "");
        map.put("iAdptIndex", "");
        map.put("strAdptName", "");
        map.put("strAdptStdName", "");
        map.put("strFileEncoding", "");
        map.put("PhysAddr", "");
        map.put("bDHCPEnabled", "");
        map.put("strIPAddrArray", "");
        map.put("strMaskArray", "");
        map.put("strMask", "");
        map.put("iDHCPDelayTime", "");
        map.put("iDHCPTryTimes", "");
        map.put("strOldPrivateIP", ip);
        map.put("strOldPublicIP", ip);
        map.put("strPrivateIP", ip);
        map.put("PublicIP", ip);
        map.put("iIPCONFIG", "0");
        map.put("sHttpPrefix", "http://192.168.8.10");
        map.put("title", "CAMS Portal");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "User-Agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        headers.put("Host", "192.168.8.10");
        headers.put("Origin", "http://192.168.8.10");
        headers.put("Referer", "http://192.168.8.10/portal/index_default.jsp?Language=Chinese");

        Connection.Response response = null;
        try {
            response = Jsoup.connect("http://192.168.8.10/portal/login.jsp?Flag=0")
                    .data(map)
                    .headers(headers)
                    .method(Connection.Method.POST)
                    .timeout(20000)
                    .postDataCharset("GBK")
                    .execute();
            return response.parse().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isWifiConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null) {
            return mWiFiNetworkInfo.isAvailable();
        }
        return false;
    }

    public void linorzPrint(Object o) {
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String time_str = formatter.format(curDate);
        System.out.println(time_str + o.toString());
        Message message = new Message();
        message.what = 3;
        message.obj = time_str + " " + o.toString() + "\n";
        handler.sendMessageDelayed(message, 0);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        startActivity(intent);
    }
}
