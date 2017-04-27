package com.xc.designer.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.bean.User;

import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/18.
 */

public class RegisteActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REG_OK=1;
    private static final int REG_FAIL=0;
    private EditText name_edit;
    private EditText passwd_edit;
    private RadioGroup sex_edit;
    private EditText birth_edit;
    private EditText email_edit;
    private EditText descr_edit;

    private Button submit;
    private Button clear;

    private String name;
    private String passwd;
    private String sex;
    private String birth;
    private String email;
    private String descr;

    private int mYear,mMonth,mDay;

    private User user=new User();

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case REG_OK:
                    AlertDialog.Builder dialog=new AlertDialog.Builder(RegisteActivity.this);
                    dialog.setTitle("提示框");
                    dialog.setMessage("注册成功！请记住你的账户: "+msg.obj.toString());
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                    break;
                case REG_FAIL:
                    Toast.makeText(RegisteActivity.this,"注册失败！",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        Calendar calendar=Calendar.getInstance();
        mYear=calendar.get(Calendar.YEAR);
        mMonth=calendar.get(Calendar.MONTH);
        mDay=calendar.get(Calendar.DAY_OF_MONTH);

        submit.setOnClickListener(this);
        clear.setOnClickListener(this);
        sex_edit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Button selected=(Button)findViewById(checkedId);
                sex=selected.getText().toString();
            }
        });

        birth_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    //Toast.makeText(RegisteActivity.this,"得到焦点",Toast.LENGTH_SHORT).show();
                    new DatePickerDialog(RegisteActivity.this, mdateListener, mYear, mMonth, mDay).show();
                }
            }
        });
    }

    private DatePickerDialog.OnDateSetListener mdateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            StringBuilder date=new StringBuilder();
            date.append(mYear).append("-").append(mMonth + 1).append("-").append(mDay);
            birth_edit.setText(date);
        }
    };


    private void initView(){
        name_edit=(EditText)findViewById(R.id.user_name);
        passwd_edit=(EditText)findViewById(R.id.user_pwd);
        sex_edit=(RadioGroup) findViewById(R.id.user_sex);
        birth_edit=(EditText)findViewById(R.id.user_birth);
        email_edit=(EditText)findViewById(R.id.user_email);
        descr_edit=(EditText)findViewById(R.id.user_descr);
        submit=(Button)findViewById(R.id.form_submit);
        clear=(Button)findViewById(R.id.form_clear);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.form_submit:
                name=name_edit.getText().toString();
                passwd=passwd_edit.getText().toString();
                //sex=sex_edit.getText().toString();
                RadioButton sexb=(RadioButton)findViewById(sex_edit.getCheckedRadioButtonId());
                sex=sexb.getText().toString();
                birth=birth_edit.getText().toString();
                email=email_edit.getText().toString();
                descr=descr_edit.getText().toString();
                regster();
                break;
            case R.id.form_clear:
                clear();
                break;
        }
    }

    public void clear(){
        name_edit.setText("");
        passwd_edit.setText("");
        //sex_edit.setText("");
        ((RadioButton)sex_edit.getChildAt(0)).setChecked(true);
        birth_edit.setText("");
        email_edit.setText("");
        descr_edit.setText("");
    }

    public void regster(){
        new Thread(new Runnable() {
          /*  String urlStr = "http://192.168.191.1:8080/Designer/user_register?name="+name
                    +"&pwd="+passwd+"&sex="+sex+"&birth="+birth+"&email="+email+"&descr="+descr;*/
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody=new FormBody.Builder()
                            .add("name",name)
                            .add("pwd",passwd)
                            .add("sex",sex)
                            .add("birth",birth)
                            .add("email",email)
                            .add("descr",descr).build();
                    Request request = new Request.Builder()
                            .url("http://192.168.191.1:8080/Designer/user_register")
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    int code=Integer.parseInt(jsonObject.get("code").toString());
                    Message msg=new Message();
                    if(code>0){
                        Integer account=Integer.valueOf(jsonObject.get("account").toString());
                        msg.what=REG_OK;
                        msg.obj=account;
                        handler.sendMessage(msg);
                    }else{
                        msg.what=REG_FAIL;
                        handler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
