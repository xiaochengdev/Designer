package com.xc.designer.activity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.adapter.CommentAdapter;
import com.xc.designer.bean.AnMessage;
import com.xc.designer.bean.Document;
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
 * Created by Administrator on 2017/3/23.
 */

public class DocumentOpActivity extends AppCompatActivity implements View.OnClickListener{
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView listView;

    private TextView docIdView;
    private TextView docNameView;
    private TextView docDescrView;
    private ImageButton docDownload;
    private ImageButton docComment;
    private ImageButton docAddlike;

    private RelativeLayout rlComment;
    private TextView hideDown;
    private EditText comment_contents;
    private Button send;
    private User user;

    private Document document;

    private List<AnMessage> ansMsgList;
    private List<Note> dataList=new ArrayList<Note>();
    private CommentAdapter adapter;
    private int current=0;

    private List<String> documentIds=new ArrayList<String>();
    private int added=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_view);

        Intent intent=getIntent();
        Bundle data=intent.getExtras();
        document=(Document)data.get("document");

        initView();

        if (document!=null){
            docIdView.setText(document.getDid()+"");
            docNameView.setText(document.getName());
            docDescrView.setText(document.getDescr());
        }
        user= Utility.getUserInfo(getApplicationContext());
        initUserDocuments();
        if (documentIds.contains(document.getDid()+"")){
            docAddlike.setImageResource(R.drawable.ic_action_heart_red);
        }
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        adapter=new CommentAdapter(dataList);
        listView.setAdapter(adapter);
        initEvent();
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataSupport.deleteAll(AnMessage.class,"docid=?",document.getDid()+"");
                queryAnsMsgByDocId();
            }
        });
        queryAnsMsgByDocId();
    }

    private void initView(){
        docIdView=(TextView)findViewById(R.id.doc_id);
        docNameView=(TextView)findViewById(R.id.doc_name);
        docDescrView=(TextView)findViewById(R.id.doc_descr);

        docDownload=(ImageButton)findViewById(R.id.doc_download);
        docComment=(ImageButton)findViewById(R.id.doc_comment);
        docAddlike=(ImageButton)findViewById(R.id.doc_addlike);

        //评论
        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.ansmsg_refresh);
        listView=(RecyclerView) findViewById(R.id.ansmsg_list);
        rlComment=(RelativeLayout)findViewById(R.id.rl_comment);
        hideDown=(TextView)findViewById(R.id.hide_down);
        comment_contents=(EditText)findViewById(R.id.comment_content);
        send=(Button)findViewById(R.id.comment_send);
    }

    private void initEvent(){
        docDownload.setOnClickListener(this);
        docComment.setOnClickListener(this);
        docAddlike.setOnClickListener(this);
        send.setOnClickListener(this);
        hideDown.setOnClickListener(this);
    }

    private void initUserDocuments(){
        String documentsStr=user.getDocuments()==null?"":user.getDocuments();
        String[] documents;
        if (!documentsStr.equals("")){
            documents=documentsStr.split(",");
            for (int i=0;i<documents.length;i++){
                documentIds.add(documents[i].trim());
            }
        }
    }

    private void docDownload(String address){
        //创建下载任务,downloadUrl就是下载链接
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(address));
//指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir("/download/", document.getName());
//获取下载管理器
        DownloadManager downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
//将下载任务加入下载队列，否则不会进行下载
        downloadManager.enqueue(request);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.doc_download:
                String address="http://192.168.191.1:8080/Designer/down_documentDown?file_name="+document.getName();
                docDownload(address);
                break;
            case R.id.doc_comment:
                rlComment.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                comment_contents.setFocusable(true);
                break;
            case R.id.doc_addlike:
                added++;
                addLike();
                break;
            case R.id.comment_send:
                String contents=comment_contents.getText().toString();
                Date date=new Date();
                AnMessage anMessage=new AnMessage();
                anMessage.setDocid(document.getDid());
                anMessage.setFdate(date.toString());
                anMessage.setUsername(user.getName());
                anMessage.setUserid(user.getUid());
                anMessage.setContents(contents);
                DataSupport.deleteAll(AnMessage.class,"docid=?",document.getDid()+"");
                sendAnsToDoc(anMessage);
                break;
            case R.id.hide_down:
                rlComment.setVisibility(View.GONE);
                break;
            default:
        }
    }

    private void queryAnsMsgByDocId(){
        Integer docid=document.getDid();
        ansMsgList= DataSupport.where("docid=?",docid+"").find(AnMessage.class);
        if (ansMsgList.size()>0||current>0){
            current=0;
            dataList.clear();
            for (AnMessage anMessage:ansMsgList){
                dataList.add(anMessage);
            }
            adapter.notifyDataSetChanged();
            //refreshLayout.setRefreshing(false);
        }else{
            current++;
            String address="http://192.168.191.1:8080/Designer/msg_findAnswerMsgByDocId?id="+docid;
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
                        Toast.makeText(DocumentOpActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
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
                            queryAnsMsgByDocId();
                            refreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    private void sendAnsToDoc(final AnMessage anMessage){
        String address="http://192.168.191.1:8080/Designer/msg_answerToDoc";
        RequestBody requestBody=new FormBody.Builder()
                .add("answerMessage.contents",anMessage.getContents())
                .add("answerMessage.fdate",anMessage.getFdate())
                .add("userid",anMessage.getUserid().toString())
                .add("answerMessage.username",anMessage.getUsername())
                .add("id",anMessage.getDocid().toString()).build();
        showProgressDailog();
        HttpUtil.sendOkHttpRequestWithParam(address,requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(DocumentOpActivity.this,"发表失败",Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
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
                            Toast.makeText(DocumentOpActivity.this,"回复成功",Toast.LENGTH_SHORT).show();
                            queryAnsMsgByDocId();
                            refreshLayout.setRefreshing(false);
                        }
                    });
                }else{
                    Toast.makeText(DocumentOpActivity.this,"回复失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void addLike(){
        //String documentsStr=user.getDocuments()==null?"":user.getDocuments();
        String docId=(document.getDid()+"").trim();

        if (documentIds.contains(docId)||added%2==0){
            //Toast.makeText(VideoOpActivity.this,"已存在收藏夹中",Toast.LENGTH_SHORT).show();
            documentIds.remove(docId);
            String documentStr = documentIds.toString();
            documentStr = documentStr.substring(1, documentStr.length() - 1).trim();
            user.setDocuments(documentStr);
            addVideoToLike("delete");
        }else {
            documentIds.add(document.getDid() + "");
            String documentStr = documentIds.toString();
            documentStr = documentStr.substring(1, documentStr.length() - 1).trim();
            user.setDocuments(documentStr);
            addVideoToLike("add");
        }
    }
    private void addVideoToLike(final String type){

        String address="http://192.168.191.1:8080/Designer/user_addUserDoc";
        RequestBody requestBody=new FormBody.Builder()
                .add("id",user.getUid()+"")
                .add("user.documents",user.getDocuments())
                .build();
        showProgressDailog();
        HttpUtil.sendOkHttpRequestWithParam(address,requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(DocumentOpActivity.this,"操作失败",Toast.LENGTH_SHORT).show();
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
                            String msg="操作成功";
                            if (type.equals("add")){
                                msg="添加成功";
                                docAddlike.setImageResource(R.drawable.ic_action_heart_red);
                            }else if(type.equals("delete")){
                                msg="删除成功";
                                docAddlike.setImageResource(R.drawable.ic_action_heart_blue);
                            }
                            Toast.makeText(DocumentOpActivity.this,msg,Toast.LENGTH_SHORT).show();
                            Utility.saveUser(DocumentOpActivity.this,user);
                            queryAnsMsgByDocId();
                            refreshLayout.setRefreshing(false);
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }else{
                    Toast.makeText(DocumentOpActivity.this,"操作失败",Toast.LENGTH_SHORT).show();
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
