package com.cmpe277.lab2.mytube.Utility;

/**
 * Created by knbarve on 10/12/15.
 */
import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.cmpe277.lab2.mytube.HomeActivity;
import com.cmpe277.lab2.mytube.MainActivity;
import com.google.android.gms.auth.GoogleAuthUtil;

public class SessionManager {
    SharedPreferences pref;
    Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "MyTubePreferences";

    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_NAME = "name";

    public static final String KEY_EMAIL = "email";

    private static final String IS_DISCONNECTED = "IsDisconnected";

    private static final String TOKEN = "Token";

    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String name, String email){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.commit();

        Intent i = new Intent(_context, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

    public boolean checkLogin(){
        if(this.isLoggedIn()){
            Intent i = new Intent(_context, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
            return true;
        }
        return false;
    }

    public void setToken(String token){
        Log.d(Constatnts.TAG, "Token Set:"+token);
        editor.putString(TOKEN, token);
        editor.commit();
    }

    public String getToken(){
        String token = pref.getString(TOKEN, "");
        if(token.equalsIgnoreCase("")){
           token = requestAccessToken();
           setToken(token);
        }
        return token;
    }

    public String getLoggedInMail(){
        return  pref.getString(KEY_EMAIL, "");
    }

    public void logoutUser(){
        clearUserDetails();

        Intent i = new Intent(_context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        _context.startActivity(i);
    }

    public void clearUserDetails(){
        editor.remove(IS_LOGIN);
        editor.remove(KEY_NAME);
        editor.remove(KEY_EMAIL);
        editor.remove(TOKEN);
        editor.commit();

    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public boolean isDisconnected(){
        return pref.getBoolean(IS_DISCONNECTED, true);
    }

    public void setIsDisconnected(boolean value){
        editor.putBoolean(IS_DISCONNECTED, value);
        editor.commit();
    }

    public String requestAccessToken(){
        String accessToken = "";
        try {
            accessToken = GoogleAuthUtil.getToken(_context,getLoggedInMail(), Constatnts.SCOPE_STRING);
        } catch (Exception e) {
            Log.e(Constatnts.TAG,"Exception in Access Token Request:",e);
        }
        return  accessToken;
    }
}
