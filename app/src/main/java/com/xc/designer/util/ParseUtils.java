package com.xc.designer.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/18.
 */

public class ParseUtils {
    public static void parseJSONWithJSONObject(String jsonData){
        try{
            JSONArray jsonArray=new JSONArray(jsonData);
            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
            }
        }catch (Exception e){

        }
    }
}
