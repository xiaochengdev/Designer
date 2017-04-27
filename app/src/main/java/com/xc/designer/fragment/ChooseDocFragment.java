package com.xc.designer.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.activity.DocumentOpActivity;
import com.xc.designer.bean.Document;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**0.
 * Created by Administrator on 2017/3/23.
 */

public class ChooseDocFragment extends Fragment {
    private ProgressDialog progressDialog;
    private ListView docListView;
    private List<Document> documentList;
    private List<String> dataList=new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_document,container,false);
        docListView=(ListView)view.findViewById(R.id.doc_list);
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.doc_swipe_refresh);
       // queryDocuments();

        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        docListView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataSupport.deleteAll(Document.class);
                queryDocuments();
            }
        });
        queryDocuments();
        docListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Document document=documentList.get(position);
                Intent intent=new Intent(getActivity(), DocumentOpActivity.class);
                intent.putExtra("document",document);
                startActivity(intent);
            }
        });
    }

    private void queryDocuments(){
        documentList= DataSupport.findAll(Document.class);
        if (documentList.size()>0){
            dataList.clear();
            for (Document document:documentList){
                dataList.add(document.getName());
            }
            adapter.notifyDataSetChanged();
            docListView.setSelection(0);
        }else{
            String address="http://192.168.191.1:8080/Designer/down_doclist";
            queryFromServer(address);
        }
    }

    private void queryFromServer(String address){
        showProgressDailog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                result= Utility.handleDocResponse(responseText);
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            queryDocuments();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }
    private void showProgressDailog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
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
