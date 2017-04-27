package com.xc.designer.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.adapter.UserDocAdapter;
import com.xc.designer.adapter.UserVideoAdapter;
import com.xc.designer.bean.Document;
import com.xc.designer.bean.User;
import com.xc.designer.bean.Video;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/19.
 */

public class UserDocActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private List<Document> docList;
    private List<Document> dataList=new ArrayList<>();
    private User user;
    private UserDocAdapter adapter;

    private ListView listView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdoc);
        user=(User) Utility.getUserInfo(this);

        listView=(ListView)findViewById(R.id.user_doclist);
        adapter=new UserDocAdapter(UserDocActivity.this,R.layout.item_userdoc,dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Document document=docList.get(position);
                Intent intent=new Intent(UserDocActivity.this,DocumentOpActivity.class);
                intent.putExtra("document",document);
                startActivity(intent);
            }
        });
        queryUserDoc();
    }

    private List<Document> queryDocByDids(long[] ids){
        List<Document> list=new ArrayList<Document>();
        for (int i=0;i<ids.length;i++){
            List<Document> data= DataSupport.where("did=?",String.valueOf(ids[i])).find(Document.class);
            if (data!=null) {
                list.add(data.get(0));
            }
        }
        return list;
    }

    private void queryUserDoc(){
        String idsStr=user.getDocuments().replace(" ","");
        String[] idStr=idsStr.split(",");
        if (idsStr.length()==0){
            return;
        }
        long[] ids=getDocIds(idsStr);
        docList=queryDocByDids(ids);
        if (docList.size()>0){
            dataList.clear();
            for (Document document:docList){
                dataList.add(document);
            }
            adapter.notifyDataSetChanged();
        }else{
            String address="http://192.168.191.1:8080/Designer/down_findDocsByIds?ids="+idsStr;
            queryUserDocFromServer(address);
        }
    }

    private void queryUserDocFromServer(String address){
        showProgressDailog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(UserDocActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                result= Utility.handleVideoResponse(responseText);
                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            queryUserDoc();
                        }
                    });
                }
            }
        });
    }

    private void showProgressDailog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(UserDocActivity.this);
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

    private long[] getDocIds(String docId){
        //获取视频id数组
        String[] idStr=docId.split(",");
        long[] docIds=new long[idStr.length];
        for (int i=0;i<idStr.length;i++){
            docIds[i]=Integer.valueOf(idStr[i].trim());
        }
        return docIds;
    }
}
