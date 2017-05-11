package cn.studyjams.s1.sj46.lihuabin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import android.app.Application;

public class App extends Application {
    HashMap<String, String> subjectData= new HashMap<String, String>();


    public void onCreate() {
        getSubjectList();
        super.onCreate();
    }
    public Object[] getSubjectList() {

        InputStream json= getResources().openRawResource(R.raw.data);
        InputStreamReader ips= new InputStreamReader(json);
        BufferedReader bf= new BufferedReader(ips);
        String jsonString="";
        String s;

        try {
            while((s=bf.readLine())!=null){
                jsonString=jsonString+s;
            }
            JSONArray ja= new JSONArray(jsonString);
            for (int i = 0; i < ja.length(); i++) {
                String sub=ja.optJSONObject(i).optString("subject");
                String data=ja.optJSONObject(i).optString("data");
                subjectData.put(sub, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subjectData.keySet().toArray();
    }


    public String  getDataBySubject(String subject){
        return subjectData.get(subject);
    }

    Map<String, String> getMap(){
        return subjectData;
    }
}
