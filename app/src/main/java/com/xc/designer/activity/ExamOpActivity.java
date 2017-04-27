package com.xc.designer.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.bean.Exam;
import com.xc.designer.bean.Question;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/2.
 */

public class ExamOpActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private TextView title;
    private LinearLayout questLayout;
    private LinearLayout reflectInfo;
    private Button submit;
    private TextView result;


    private List<Map> resultList=new ArrayList<Map>();
    private List<Question> questionList;
    private Exam exam;
    private Map<String,Object> answerMap=new HashMap<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        Bundle data=intent.getExtras();
        exam=(Exam) data.get("exam");
        queryQuestions();
        setContentView(R.layout.activity_exam_view);
        initView();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean exitError=false;
                int length=questionList.size();
                for (int i=0;i<length;i++){
                    Question question=questionList.get(i);
                    Object result=answerMap.get(i+"");
                    if (result==null){
                        resultList.clear();
                        Toast.makeText(ExamOpActivity.this,"第"+(i+1)+"题还没选择",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!(result.toString()).equals(question.getAnswer())) {
                        exitError=true;
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("orderNum", i+1);
                        map.put("right", question.getAnswer());
                        map.put("analyzes", question.getAnalyzes());
                        map.put("suggestion", question.getSuggestion());
                        resultList.add(map);
                    }
                }

                StringBuilder resultInfo=new StringBuilder();
                for (int j=0;j<resultList.size();j++){
                    Map<String,Object> q=resultList.get(j);
                    resultInfo.append("第"+q.get("orderNum")+"题"+"\n正确答案:"+q.get("right")
                    +"\n 分析:"+q.get("analyzes")+"\n 建议:"+q.get("suggestion")+"\n\n");
                }

                if(exitError) {
                    result.setText(resultInfo);
                    reflectInfo.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void initView(){
        submit=(Button)findViewById(R.id.question_submit);
        result=(TextView)findViewById(R.id.result_info);
        questLayout=(LinearLayout) findViewById(R.id.question_layout);
        reflectInfo=(LinearLayout)findViewById(R.id.reflect_result);

        title=(TextView)findViewById(R.id.exam_title);
        title.setText(exam.getTitle());

        for (int i=0;i<questionList.size();i++){
            final String questionId=i+"";
            Question question=questionList.get(i);

            //问题题目及选项
            LinearLayout questionItem=new LinearLayout(this);
            questionItem.setOrientation(LinearLayout.VERTICAL);

            RadioGroup radioGroup=new RadioGroup(this);
            radioGroup.setOrientation(RadioGroup.HORIZONTAL);
            //问题题目
            TextView textView=new TextView(this);
            textView.setTextSize(20);
            textView.setText((i+1)+". "+question.getQuestion());
            textView.setGravity(Gravity.LEFT);
            questionItem.addView(textView);
            try {
                int j=0;
                JSONObject jsonObject = new JSONObject(question.getAnsoptions());
                Iterator<String> iterator=jsonObject.keys();
                while (iterator.hasNext()){
                    j++;
                    String key=iterator.next();
                    String value=jsonObject.getString(key);

                    RadioButton radioButton=new RadioButton(this);
                    radioButton.setId(i*10+j);
                    radioButton.setTextSize(18);
                    radioButton.setText(key+"."+value);
                    radioButton.setGravity(Gravity.CENTER);
                    //添加问题选项
                    radioGroup.addView(radioButton);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton radioButton=(RadioButton)findViewById(checkedId);
                    answerMap.put(questionId,radioButton.getText().charAt(0));
                }
            });

            radioGroup.setId(i);
            questionItem.addView(radioGroup);
            questLayout.addView(questionItem);
        }
    }

    public void queryQuestions(){
        long[] ids=getQuestionId(exam.getQuestions());
        questionList= DataSupport.findAll(Question.class,ids);
        if (questionList.size()>0){

        }else{
            String address="http://192.168.191.1:8080/Designer/question_findQuestionsByIds?ids="+exam.getQuestions();
            queryFromServer(address);
        }
    }

    public void queryFromServer(String address){
        showProgressDailog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(ExamOpActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                result= Utility.handleQuestionResponse(responseText);
                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            queryQuestions();
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
    private long[] getQuestionId(String questionId){
        String[] idsStr=questionId.split(",");
        long[] ids=new long[idsStr.length];
        for (int i=0;i<idsStr.length;i++){
            ids[i]=Integer.valueOf(idsStr[i]);
        }
        return ids;
    }
}
