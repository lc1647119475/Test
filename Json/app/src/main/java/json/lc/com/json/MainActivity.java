package json.lc.com.json;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class MainActivity extends Activity {
    Calendar c =Calendar.getInstance();
    String year=String.valueOf(c.get(Calendar.YEAR));
    String month=String.valueOf(c.get(Calendar.MONTH)+1);
    String day=String.valueOf(c.get(Calendar.DAY_OF_MONTH));
    String date=year+month+day;
    String httpUrl="http://api.meiriyiwen.com/v2/day?date="+date+"&version=4";
    private TextView tv_title,tv_author,tv_content,tv_last;
    private ImageView iv_random,iv_menu;
    private JSONObject jsonObject;
    private GestureDetector mDetevtor;
    private ScrollView scrollView;
    private Toast toast;
    private int x;
    private int y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDetevtor=new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //判断纵向滑动幅度是否过大，过大则不能切换
                if(Math.abs(e2.getRawY()-e1.getRawY())>200){
                    return true;
                }
                //判断滑动速度是否慢
                if (Math.abs(velocityX) < 100) {
                    return true;
                }
                //向右滑，上一页
                if (e2.getRawX() - e1.getRawX() > 200) {
                    back();
                    Json();
                    return true;
                }
                //向左滑，下一页
                if (e1.getRawX() - e2.getRawX() > 200) {
                    if(day.equals(String.valueOf(c.get(Calendar.DAY_OF_MONTH)))){
                        toast=Toast.makeText(MainActivity.this,"没有了",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                        return true;
                    }
                    next();
                    Json();
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_author= (TextView) findViewById(R.id.tv_author);
        tv_content= (TextView) findViewById(R.id.tv_content);
        tv_last= (TextView) findViewById(R.id.tv_last);
        scrollView= (ScrollView) findViewById(R.id.scrollView);
        x =(int)scrollView.getX();
        y =(int)scrollView.getY();
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetevtor.onTouchEvent(event);
                return false;
            }
        });
        iv_menu= (ImageView) findViewById(R.id.iv_menu);
        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        iv_random= (ImageView) findViewById(R.id.iv_random);
        iv_random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
                Json();
            }
        });
        Json();
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                scrollView.scrollTo(x, y);
                String title=jsonObject.getString("title");
                tv_title.setText(title);
                String author=jsonObject.getString("author");
                tv_author.setText(author);
                String content=jsonObject.getString("content");
                content=content.replaceAll("<p>","\n\t\t\t\t");
                content=content.replaceAll("</p>","");
                tv_content.setText(content);
                tv_last.setText("(完)");
            } catch (JSONException e) {
                e.printStackTrace();
                tv_content.setText(e.toString());
            }
        }
    };
    public void Json(){
       new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL(httpUrl);
                    HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    if(responseCode==200){
                        InputStream is = conn.getInputStream();
                        String result=Utils.getTextFromStream(is);
                         jsonObject = new JSONObject(result);
                        Message msg =handler.obtainMessage();
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public void back(){
        day=String.valueOf(Integer.valueOf(day)-1);
        String date=year+month+day;
        httpUrl="http://api.meiriyiwen.com/v2/day?date="+date+"&version=4";
        toast=Toast.makeText(MainActivity.this,"前一天",Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
    public void next(){
        day=String.valueOf(Integer.valueOf(day)+1);
        String date=year+month+day;
        httpUrl="http://api.meiriyiwen.com/v2/day?date="+date+"&version=4";
        toast=Toast.makeText(MainActivity.this,"后一天",Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
}
