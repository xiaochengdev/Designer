package com.xc.designer.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.adapter.CommentAdapter;
import com.xc.designer.bean.AnMessage;
import com.xc.designer.bean.LeMessage;
import com.xc.designer.bean.User;
import com.xc.designer.interfaces.Note;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/12.
 */

public class LeaveOpActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView listView;

    private TextView lemsgTitle;
    private TextView lemsgContents;
    private TextView lemsgId;
    private TextView lemsgDate;
    private TextView lemsgUsername;
    private TextView lemsgUserId;

    private ImageButton answer;
    private RelativeLayout rlComment;
    private TextView hideDown;
    private EditText comment_contents;
    private Button send;
    private User user;

    private List<AnMessage> ansMsgList;
    private List<Note> dataList=new ArrayList<Note>();
    private CommentAdapter adapter;
    private LeMessage leMessage;
    private int current=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leandans);
        initView();
        initEvent();
        user=Utility.getUserInfo(getApplicationContext());
        Intent intent=getIntent();
        Bundle data=intent.getExtras();
        leMessage=(LeMessage) data.get("leMsg");
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        //queryAnsMsg();
        adapter=new CommentAdapter(dataList);
        listView.setAdapter(adapter);

        lemsgTitle.setText(leMessage.getTitle());
        lemsgContents.setText(leMessage.getContents());
        lemsgId.setText(leMessage.getNid()+"");
        lemsgUserId.setText(leMessage.getUserid()+"");
        lemsgUsername.setText(leMessage.getUsername());
        lemsgDate.setText(leMessage.getFdate());

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataSupport.deleteAll(AnMessage.class,"lid=?",leMessage.getNid()+"");
                queryAnsMsg();
            }
        });
        queryAnsMsg();
    }

    public void initView(){
        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.ansmsg_refresh);
        listView=(RecyclerView) findViewById(R.id.ansmsg_list);

        lemsgTitle=(TextView)findViewById(R.id.lemsg_title);
        lemsgContents=(TextView)findViewById(R.id.lemsg_contents);
        lemsgId=(TextView)findViewById(R.id.lemsg_id);
        lemsgDate=(TextView)findViewById(R.id.lemsg_date);
        lemsgUserId=(TextView)findViewById(R.id.lemsg_userid);
        lemsgUsername=(TextView)findViewById(R.id.lemsg_username);

        answer=(ImageButton)findViewById(R.id.leavemsg_answer);
        rlComment=(RelativeLayout)findViewById(R.id.rl_comment);
        hideDown=(TextView)findViewById(R.id.hide_down);
        comment_contents=(EditText)findViewById(R.id.comment_content);
        send=(Button)findViewById(R.id.comment_send);
    }

    public void initEvent(){
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlComment.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contents=comment_contents.getText().toString();
                Date date=new Date();
                AnMessage anMessage=new AnMessage();
                anMessage.setLid(leMessage.getNid());
                anMessage.setFdate(date.toString());
                anMessage.setUsername(user.getName());
                anMessage.setUserid(user.getUid());
                anMessage.setContents(contents);
                DataSupport.deleteAll(AnMessage.class,"lid=?",leMessage.getNid()+"");
                sendAns(anMessage);
                //Toast.makeText(LeaveOpActivity.this,contents,Toast.LENGTH_SHORT).show();
            }
        });
        hideDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlComment.setVisibility(View.GONE);
            }
        });
    }



    private void queryAnsMsg(){
        Integer lid=leMessage.getNid();
        ansMsgList= DataSupport.where("lid=?",lid+"").find(AnMessage.class);
        if (ansMsgList.size()>0||current>0){
            current=0;
            dataList.clear();
            for (AnMessage anMessage:ansMsgList){
                dataList.add(anMessage);
            }
            adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        }else{
            current++;
            String address="http://192.168.191.1:8080/Designer/msg_findAnswerMsgByLid?id="+lid;
            queryFromServer(address);
        }
    }
    private void queryFromServer(String address){
        showProgressDailog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(LeaveOpActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                result = Utility.handleAnMessageResponse(responseText);
                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            queryAnsMsg();
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    private void sendAns(final AnMessage anMessage){
        String address="http://192.168.191.1:8080/Designer/msg_answer";
        RequestBody requestBody=new FormBody.Builder()
                .add("answerMessage.contents",anMessage.getContents())
                .add("answerMessage.fdate",anMessage.getFdate())
                .add("userid",anMessage.getUserid().toString())
                .add("answerMessage.username",anMessage.getUsername())
                .add("id",anMessage.getLid().toString()).build();
        showProgressDailog();
        HttpUtil.sendOkHttpRequestWithParam(address,requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(LeaveOpActivity.this,"发表失败",Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
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
                            Toast.makeText(LeaveOpActivity.this,"回复成功",Toast.LENGTH_SHORT).show();
                            queryAnsMsg();
                            refreshLayout.setRefreshing(false);
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }else{
                    Toast.makeText(LeaveOpActivity.this,"回复失败",Toast.LENGTH_SHORT).show();
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
