package com.xc.designer.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.bean.LeMessage;
import com.xc.designer.bean.User;
import com.xc.designer.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/14.
 */

public class LeMsgAddActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private EditText title;
    private EditText contents;
    private Button send;

    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lemsg);

        Intent intent=getIntent();
        Bundle data=intent.getExtras();
        user=(User)data.get("user");
        initView();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeMessage leMessage=new LeMessage();
                Date date=new Date();
                leMessage.setTitle(title.getText().toString());
                leMessage.setContents(contents.getText().toString());
                leMessage.setUserid(user.getUid());
                leMessage.setUsername(user.getName());
                leMessage.setFdate(date.toString());
                sendLeMsg(leMessage);
            }
        });
    }

    private void initView(){
        title=(EditText)findViewById(R.id.lemsg_title);
        contents=(EditText)findViewById(R.id.lemsg_contents);
        send=(Button)findViewById(R.id.send);
    }

    private void sendLeMsg(LeMessage leMessage){
        String address="http://192.168.191.1:8080/Designer/msg_addLeaveMsg";
        RequestBody requestBody=new FormBody.Builder()
                .add("leaveMessage.title",leMessage.getTitle())
                .add("leaveMessage.contents",leMessage.getContents())
                .add("leaveMessage.fdate",leMessage.getFdate())
                .add("userid",leMessage.getUserid().toString())
                .add("leaveMessage.username",leMessage.getUsername())
                .build();
        showProgressDailog();
        HttpUtil.sendOkHttpRequestWithParam(address,requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(LeMsgAddActivity.this,"发表失败",Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(LeMsgAddActivity.this,"发表成功",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent();
                            setResult(RESULT_OK,intent);
                            finish();
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            Toast.makeText(LeMsgAddActivity.this,"发表失败",Toast.LENGTH_SHORT).show();
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    });

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
