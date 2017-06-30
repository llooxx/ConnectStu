package com.linorz.connectstu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by linorz on 2016/7/15.
 */
public class InfoTool {
    private static final String LOGIN_INFO_SAVE_PATH = "login_info";

    public static void saveLoginInfo(Context context, LoginInfo info) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(
                LOGIN_INFO_SAVE_PATH, Activity.MODE_PRIVATE).edit();
        prefs.putString("username", info.getUsername());
        prefs.putString("password", info.getPassword());
        if (!prefs.commit()) saveLoginInfo(context, info);
    }

    public static LoginInfo getLoginInfo(Context context) {
        SharedPreferences mPref = context.getSharedPreferences(
                LOGIN_INFO_SAVE_PATH, Activity.MODE_PRIVATE);
        return new LoginInfo(mPref.getString("username", ""), mPref.getString("password", ""));
    }

    public static String getUserName(Context context) {
        SharedPreferences mPref = context.getSharedPreferences(
                LOGIN_INFO_SAVE_PATH, Activity.MODE_PRIVATE);
        return mPref.getString("username", "");
    }

    public static class LoginInfo {
        private String username;
        private String password;

        public LoginInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}