package com.xc.designer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.xc.designer.MainActivity;
import com.xc.designer.R;
import com.xc.designer.bean.User;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/18.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int LOGIN_OK = 1;
    private static final int LOGIN_FAIL = 0;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private EditText accountText;
    private EditText passwdText;
    private CheckBox rememberPass;
    private Button reg;
    private Button login;
    private String account;
    private String passwd;
    private User user = new User();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_FAIL:
                    Toast.makeText(LoginActivity.this, "密码或用户名错误！", Toast.LENGTH_SHORT).show();
                    break;
                case LOGIN_OK:
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        rememberPass = (CheckBox) findViewById(R.id.remember_pass);
        boolean isRemember = pref.getBoolean("remember_password", false);
        accountText = (EditText) findViewById(R.id.user_account);
        passwdText = (EditText) findViewById(R.id.user_passwd);
        reg = (Button) findViewById(R.id.user_reg);
        login = (Button) findViewById(R.id.user_login);
        if (isRemember) {
            account = pref.getString("account", "");
            passwd = pref.getString("password", "");
            accountText.setText(account);
            passwdText.setText(passwd);
            rememberPass.setChecked(true);
        }
        reg.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_reg:
                Intent intent = new Intent(LoginActivity.this, RegisteActivity.class);
                startActivity(intent);
                break;
            case R.id.user_login:
                account = accountText.getText().toString();
                passwd = passwdText.getText().toString();
                if(account.equals("")||account==null){
                    Toast.makeText(LoginActivity.this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
                    break;
                }else if (passwd.equals("")||passwd==null){
                    Toast.makeText(LoginActivity.this, "密码不能为空！", Toast.LENGTH_SHORT).show();
                    break;
                }
                login();
                break;
            default:
        }
    }

    private void login() {
        new Thread(new Runnable() {
            // wifi:192.168.191.2    app：192.168.191.1
            // 电脑IP:10.201.234.6
            String urlStr = "http://192.168.191.1:8080/Designer/user_login?id="
                    + account + "&pwd=" + passwd;

            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(urlStr)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code=Integer.parseInt(jsonObject.get("code").toString());
                    if (responseData == null || responseData.equals("null")||code<0) {
                        Message msg = new Message();
                        msg.what = LOGIN_FAIL;
                        handler.sendMessage(msg);
                    } else {
                        JSONObject user_json = jsonObject.getJSONObject("user");
                        user.setUid(Integer.valueOf(user_json.getInt("id")));
                        user.setName(user_json.getString("name"));
                        user.setPwd(user_json.getString("pwd"));
                        user.setSex(user_json.getString("sex"));
                        user.setBirth(user_json.getString("birth"));
                        user.setEmail(user_json.getString("email"));
                        user.setDescr(user_json.getString("descr"));
                        user.setVideos(user_json.getString("videos"));
                        user.setDocuments(user_json.getString("documents"));

                        editor=pref.edit();
                        if (rememberPass.isChecked()){
                            editor.putBoolean("remember_password",true);
                            editor.putString("account",account);
                            editor.putString("password",passwd);
                        }else {
                            editor.clear();
                        }
                        editor.apply();
                        Message msg = new Message();
                        msg.what = LOGIN_OK;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
