package cn.studyjams.s1.sj46.lihuabin;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

    ListView listView;
    public HashMap<String, String> subjectData = new HashMap<String, String>();
    App app;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setAlpha(0.5f);
        app = (App) getApplication();

        Object[] subject = app.getSubjectList();

        listView.setAdapter(new ArrayAdapter(this,
                R.layout.listlayout, subject));

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int arg2,
                                    long arg3) {
                Intent i = new Intent(MainActivity.this, PlayActivity.class);
                i.putExtra("subject", ((TextView) v).getText());


                startActivity(i);
            }
        });

    }

}
