package com.xc.designer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.xc.designer.R;
import com.xc.designer.adapter.CustomFileAdapter;
import com.xc.designer.bean.Document;
import com.xc.designer.bean.Video;
import com.xc.designer.interfaces.CustomFile;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/20.
 */

public class QueryResultActivity extends AppCompatActivity {
    private CustomFileAdapter customFileAdapter;
    private String condition;
    //private List<CustomFile> fileList;
    private List<CustomFile> dataList=new ArrayList<>();

    private List<Video> videoList;
    private List<Document> documentList;

    private ListView resultList;
    private TextView msgView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_result);

        Intent intent=getIntent();
        Bundle data=intent.getExtras();
        condition=data.getString("condition");

        queryFileFromLocal(condition);

        resultList=(ListView)findViewById(R.id.result_view);
        msgView=(TextView)findViewById(R.id.msg);

        if (dataList.isEmpty()){
            resultList.setVisibility(View.GONE);
            msgView.setVisibility(View.VISIBLE);
        }

        customFileAdapter=new CustomFileAdapter(QueryResultActivity.this,R.layout.item_custom_file,dataList);
        resultList.setAdapter(customFileAdapter);

        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomFile file=dataList.get(position);
                String kind=file.getKind();
                if (file instanceof Document){
                    Intent intent=new Intent(QueryResultActivity.this,DocumentOpActivity.class);
                    intent.putExtra("document",(Document)file);
                    startActivity(intent);
                }else {
                    Intent intent=new Intent(QueryResultActivity.this,VideoOpActivity.class);
                    intent.putExtra("video",(Video)file);
                    startActivity(intent);
                }
            }
        });

    }

    private void queryFileFromLocal(String condition){
        videoList= DataSupport.where("name like ?","%"+condition+"%").find(Video.class);
        documentList=DataSupport.where("name like ?","%"+condition+"%").find(Document.class);

        if (videoList!=null&&videoList.size()>0){
            for (int i=0;i<videoList.size();i++){
                CustomFile file=(CustomFile)videoList.get(i);
                dataList.add(file);
            }
        }

        if (documentList!=null&&documentList.size()>0){
            for (int i=0;i<documentList.size();i++){
                CustomFile file=(CustomFile)documentList.get(i);
                dataList.add(file);
            }
        }
    }
}
