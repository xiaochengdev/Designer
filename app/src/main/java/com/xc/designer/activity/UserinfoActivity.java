package com.xc.designer.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.CollapsibleActionView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.xc.designer.R;
import com.xc.designer.bean.User;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/28.
 */

public class UserinfoActivity extends AppCompatActivity implements View.OnClickListener{
    private ProgressDialog progressDialog;

    private EditText userInfo_name;
    private EditText userInfo_sex;
    private EditText userInfo_birth;
    private EditText userInfo_descr;
    private EditText userInfo_email;
    private EditText userInfo_id;
    private EditText userInfo_pwd;

    private ImageButton userInfo_edit;
    private ImageButton userInfo_save;

    private User userinfo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        /*Intent intent=getIntent();
        Bundle data=intent.getExtras();*/
        userinfo= Utility.getUserInfo(this);
        initView();

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar=(CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        ImageView imageView=(ImageView)findViewById(R.id.user_header_photo);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbar.setTitle(userinfo.getName());
        Glide.with(this).load(R.drawable.sea).into(imageView);

        userInfo_edit.setOnClickListener(this);
        userInfo_save.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initView(){
        userInfo_name=(EditText)findViewById(R.id.user_name);
        userInfo_sex=(EditText)findViewById(R.id.user_sex);
        userInfo_birth=(EditText)findViewById(R.id.user_birth);
        userInfo_descr=(EditText)findViewById(R.id.user_descr);
        userInfo_email=(EditText)findViewById(R.id.user_email);
        userInfo_id=(EditText)findViewById(R.id.user_id);
        userInfo_pwd=(EditText)findViewById(R.id.user_pwd); ;

        userInfo_edit=(ImageButton)findViewById(R.id.userinfo_edit);
        userInfo_save=(ImageButton)findViewById(R.id.userinfo_save);

        if (userinfo!=null){
            userInfo_name.setText(userinfo.getName());
            userInfo_sex.setText(userinfo.getSex());
            userInfo_birth.setText(userinfo.getBirth());
            userInfo_descr.setText(userinfo.getDescr());
            userInfo_email.setText(userinfo.getEmail());
            userInfo_id.setText(userinfo.getUid()+"");
            userInfo_pwd.setText(userinfo.getPwd());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.userinfo_edit:
                editStateChange(true);
                userInfo_edit.setVisibility(View.GONE);
                userInfo_save.setVisibility(View.VISIBLE);
                break;
            case R.id.userinfo_save:
                userInfoSave();
                editStateChange(false);
                userInfo_edit.setVisibility(View.VISIBLE);
                userInfo_save.setVisibility(View.GONE);
                break;
            default:
        }
    }

    private void editStateChange(boolean edit){
        userInfo_name.setEnabled(edit);
        userInfo_sex.setEnabled(edit);
        userInfo_birth.setEnabled(edit);
        userInfo_descr.setEnabled(edit);
        userInfo_email.setEnabled(edit);
        userInfo_pwd.setEnabled(edit);
    }

    private void userInfoSave(){
        userinfo.setName(userInfo_name.getText().toString().trim());
        userinfo.setSex(userInfo_sex.getText().toString().trim());
        userinfo.setEmail(userInfo_email.getText().toString().trim());
        userinfo.setBirth(userInfo_birth.getText().toString().trim());
        userinfo.setDescr(userInfo_descr.getText().toString().trim());
        userinfo.setPwd(userInfo_pwd.getText().toString().trim());
        saveToServer();
    }

    private void saveToServer(){
        String address="http://192.168.191.1:8080/Designer/user_updateUser";
        RequestBody requestBody=new FormBody.Builder()
                .add("user.name",userinfo.getName())
                .add("user.pwd",userinfo.getPwd())
                .add("user.sex",userinfo.getSex())
                .add("user.birth",userinfo.getBirth())
                .add("user.email",userinfo.getEmail())
                .add("user.descr",userinfo.getDescr())
                .add("id",userinfo.getUid()+"").build();

        showProgressDailog();
        HttpUtil.sendOkHttpRequestWithParam(address,requestBody,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(UserinfoActivity.this,"修改失败",Toast.LENGTH_SHORT).show();
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                try {
                    JSONObject json = new JSONObject(responseText);
                    int code=json.getInt("code");
                    if (code>0){
                        result=true;
                    }
                }catch (JSONException e){

                }
                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            Toast.makeText(UserinfoActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
                            Utility.saveUser(UserinfoActivity.this,userinfo);
                            //queryQuestions();
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }else {
                    closeProgressDailog();
                    Toast.makeText(UserinfoActivity.this,"修改失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showProgressDailog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDailog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
