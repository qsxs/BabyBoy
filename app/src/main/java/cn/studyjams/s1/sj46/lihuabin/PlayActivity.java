package cn.studyjams.s1.sj46.lihuabin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.DrawerLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class PlayActivity extends Activity {

    int curr = 0;
    int peiyinID, fayinID, imgId;
    TextView tipText;
    App app;
    DrawerLayout drawerLayout;
    LinearLayout linearLayout;

    JSONArray question;//存放该分类的所有数据
    JSONObject obj;//存放当前问题的数据
    ImageView imageView;
    TextView tv;

    String img, text, peiyin, fayin;//图片，文本，发音，配音的文件名，不包含后缀和路径
    MediaPlayer media = new MediaPlayer();
    MediaRecorder recorder = new MediaRecorder();

    String path;
    File voice_file;
    File call_file;
    String voice_path;//保存的配音文件路径
    String call_path;//保存的发音文件路径
    private static int REQUEST_CODE = 33;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i1 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            int i2 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            int i3 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            if (i1 != PermissionChecker.PERMISSION_GRANTED
                    || i2 != PermissionChecker.PERMISSION_GRANTED
                    || i3 != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            } else {
                init();
            }
        } else {
            init();
        }
        // TODO: 2016/4/28
        //getActionBar().hide();//隐藏ActionBar


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        linearLayout = (LinearLayout) findViewById(R.id.menuView);
        app = (App) getApplication();
        imageView = (ImageView) findViewById(R.id.imageView1);
        tv = (TextView) findViewById(R.id.textView1);


        Intent i = getIntent();
        String subject = i.getStringExtra("subject");

        String subData = app.getDataBySubject(subject);


        try {
            question = new JSONArray(subData);
            load();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        if (Environment.getExternalStorageDirectory() != null) {
            path = Environment.getExternalStorageDirectory() + "/" + getPackageName();//保存录音的文件夹
        } else {
            path = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            init();
        } else {
            Toast.makeText(app, "权限不足", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //加载下一题
    private void load() {
        obj = question.optJSONObject(curr);
        media.reset();
        img = obj.optString("pic");//从Json获取图片资源的文件名
        text = obj.optString("text");//从Json获取对应的名称
        peiyin = obj.optString("call");//从Json获取配音资源的文件名
        fayin = obj.optString("voice");//从Json获取发音资源的文件名

        tv.setText(text);
        imgId = getResources().getIdentifier(img, "drawable", getPackageName());//获取默认图片资源的id
        fayinID = getResources().getIdentifier(peiyin, "raw", getPackageName());//获取默认发音资源的id
        peiyinID = getResources().getIdentifier(fayin, "raw", getPackageName());//获取默认配音资源的id
        imageView.setImageResource(imgId);
        fayinPlay();
    }

    public void click(View v) {
        switch (v.getId()) {
            case R.id.fayin:
                fayinPlay();
                break;

            case R.id.peiyin:
                peiyinPlay();
                break;

            case R.id.last:
                curr--;
                if (curr < 0) {
                    curr = 0;
                }
                load();
                break;

            case R.id.next:
                curr++;
                if (curr >= question.length()) {
                    curr = question.length() - 1;
                }
                load();
                break;

            case R.id.setFayin:
                setCall();
                drawerLayout.closeDrawer(linearLayout);//关闭侧滑菜单
                break;

            case R.id.setPeiyin:
                setVoice();
                drawerLayout.closeDrawer(linearLayout);//关闭侧滑菜单
                break;

            case R.id.close:
                PlayActivity.this.finish();
                break;

            case R.id.resetAll:
                boolean b = false;
                if (path != null) b = resetAll(new File(path));

                if (b) {
                    Toast.makeText(PlayActivity.this, R.string.reset_all_succeed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlayActivity.this, R.string.reset_all_failed, Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                Toast.makeText(PlayActivity.this, R.string.in_new_version, Toast.LENGTH_SHORT).show();
                media.reset();
                break;
        }

    }

    //自定义发音
    private void setCall() {//自定义发音
        media.reset();
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayActivity.this);
        View view = getLayoutInflater().inflate(R.layout.mydialog, null);
        builder.setView(view);

        builder.setTitle(R.string.set_call);
        tipText = (TextView) view.findViewById(R.id.tipText);
        Button recBtn = (Button) view.findViewById(R.id.recBtn);
        Button reset = (Button) view.findViewById(R.id.reset);

        recBtn.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    tipText.setText(R.string.reset_to_stop);
                    makeRec("call", fayin);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    stopREC();
                    tipText.setText(R.string.hold_to_start);
                    Toast.makeText(PlayActivity.this, R.string.newfile_save, Toast.LENGTH_SHORT).show();
                }


                return false;
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resetfile(call_file);
                Toast.makeText(PlayActivity.this, R.string.to_default_succeed, Toast.LENGTH_SHORT).show();
            }
        });


        builder.show();
    }

    //自定义配音
    private void setVoice() {
        media.reset();
        AlertDialog.Builder builder2 = new AlertDialog.Builder(PlayActivity.this);
        View view2 = getLayoutInflater().inflate(R.layout.mydialog, null);
        builder2.setView(view2);

        builder2.setTitle(R.string.set_voice);
        tipText = (TextView) view2.findViewById(R.id.tipText);
        Button recBtn2 = (Button) view2.findViewById(R.id.recBtn);
        Button reset2 = (Button) view2.findViewById(R.id.reset);
        recBtn2.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    tipText.setText(R.string.reset_to_stop);
                    makeRec("voice", peiyin);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    stopREC();
                    tipText.setText(R.string.hold_to_start);
                    Toast.makeText(PlayActivity.this, R.string.newfile_save, Toast.LENGTH_SHORT).show();
                }


                return false;
            }
        });
        reset2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resetfile(voice_file);
                Toast.makeText(PlayActivity.this, R.string.to_default_succeed, Toast.LENGTH_SHORT).show();
            }
        });


        builder2.show();
    }

    //全部恢复默认
    private boolean resetAll(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();//递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = resetAll(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();

    }

    //播放配音
    private void peiyinPlay() {
        media.reset();
        voice_path = path + "/voice/" + peiyin + ".3gp";//自定义的资源路径
        voice_file = new File(voice_path);

        if (voice_file.exists()) {
            try {
                media.setDataSource(voice_path);
                media.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            media = MediaPlayer.create(this, peiyinID);
        }
        try {
            media.start();

        } catch (IllegalStateException e) {

            e.printStackTrace();
        }
    }

    //播放发音
    private void fayinPlay() {
        media.reset();
        call_path = path + "/call/" + fayin + ".3gp";//自定义的资源路径
        call_file = new File(call_path);
        if (call_file.exists()) {
            try {
                media.setDataSource(call_path);
                media.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            media = MediaPlayer.create(this, fayinID);
        }
        try {
            media.start();

        } catch (IllegalStateException e) {

            e.printStackTrace();
        }
    }

    //打开侧滑菜单
    public void openMenu(View view) {
        drawerLayout.openDrawer(linearLayout);
    }


    //开始录音
    private void makeRec(String s, String FileName) {//第一个参数s是保存目录，第二个参数是保存的文件名
        if (path != null) {
            String filePath = path + "/" + s + "/" + FileName + ".3gp";//输出文件路径
            File file = new File(path + "/" + s);
            if (!file.exists()) {//如果路径不存在则创建
                file.mkdirs();
            }
            recorder = new MediaRecorder();
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(filePath);
            try {

                recorder.prepare();

            } catch (Exception e) {
                System.out.println(e);
            }
            recorder.start();
        } else {
            Toast.makeText(PlayActivity.this, R.string.no_sdcard, Toast.LENGTH_SHORT).show();
        }


    }

    //停止录音
    private void stopREC() {//停止录音
        try {
            recorder.stop();
        } catch (Exception e) {
            Toast.makeText(PlayActivity.this, R.string.record_too_short, Toast.LENGTH_SHORT).show();
        }
    }

    //恢复默认配音或者发音
    private boolean resetfile(File deletFile) {
        boolean b = false;
        if (deletFile != null) {

            if (deletFile.exists() && deletFile.isFile()) {
                b = deletFile.delete();
            }
        } else {
            b = true;
        }

        return b;
    }


    protected void onPause() {
        media.reset();
        super.onPause();
        System.out.println("onPause");
    }

    //屏幕横竖转换

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        System.out.println("ConfigurationChanged");
       /*
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // land do nothing is ok
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // port do nothing is ok
        }
        */
    }

}
