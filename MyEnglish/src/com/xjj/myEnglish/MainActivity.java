package com.xjj.myEnglish;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.xjj.myEnglish.R;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	final String MP3_folder = "/0 My_English";
	//final String TODAY = "today";

	final static Calendar c = Calendar.getInstance();     
    static int cYear = c.get(Calendar.YEAR);     
    static int cMonth = c.get(Calendar.MONTH);     
    static int cDay = c.get(Calendar.DAY_OF_MONTH); 
    static String date = String.format("%02d%02d%02d", cYear%100, cMonth+1,cDay);
	//static String date = DateFormat.format("yyMMdd", new Date(System.currentTimeMillis())).toString();//要播放的日期，初始化为当天
 
	SharedPreferences sharedPref;
	String currentPrefValue;
	
	File sdDir;
	String path;
	String fileName;
	
	MediaPlayer mediaPlayer; 
	
	TextView textViewInfo;
	TextView textViewTimeRemaining;
	Button buttonControl;
	Button buttonExit;
	SeekBar seekBar;

	Timer mTimer;
	TimerTask mTimerTask;  
	boolean isChanging=false;//互斥变量，防止定时器与SeekBar拖动时进度冲突
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
        buttonExit = (Button) findViewById(R.id.buttonExit);
        buttonExit.setOnClickListener(new OnClickListener() { //退出
			@Override
			public void onClick(View v) {
				stopPlayerAndTimer();
				MainActivity.this.finish();
			}
		});

        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekbar());  

        
        buttonControl = (Button) findViewById(R.id.buttonControl);
        buttonControl.setTextColor(android.graphics.Color.BLUE);
        buttonControl.setOnClickListener(new OnClickListener() { //控制：暂停、继续、重新开始
			@Override
			public void onClick(View v) {
				if (buttonControl.getText().equals( 
								getResources().getString(R.string.restart))) {//重新开始
					playMyEnglish(0);
					buttonControl.setText(getResources().getString(R.string.pause));
					return;
				}
				
				//if (buttonControl.getText().equals( //继续播放
				//		getResources().getString(R.string.Continue))) {
				if(!mediaPlayer.isPlaying()){
					if (mediaPlayer != null) {
						mediaPlayer.start();
						buttonControl.setText(getResources().getString(R.string.pause));
					}
				}
				else{// if (buttonControl.getText().equals(  //暂停
					//	getResources().getString(R.string.pause))) {
					if (mediaPlayer != null) {
						mediaPlayer.pause();
						buttonControl.setText(getResources().getString(R.string.Continue));
					}
				}
			}
		});
        
        textViewInfo = (TextView) findViewById(R.id.textViewInfo);
        textViewTimeRemaining = (TextView) findViewById(R.id.textViewTimeRemaining);
        textViewTimeRemaining.setTextColor(android.graphics.Color.BLUE);
        
		//读取保存的进度，如果日期匹配，就从该进度开始播放：
		String savedDate = sharedPref.getString("Date", "");
		int savedProgress = 0;
		if(date.equals(savedDate)){
			savedProgress = sharedPref.getInt("Progress", 0);
		}
        playMyEnglish(savedProgress);
        //Log.i("test", "重新开始2");
    }
    
    private void playMyEnglish(int initProgress){//initProgress：从哪里开始播
    	
    	//if(mediaPlayer!=null && mediaPlayer.isPlaying())
    	//	return;
    	
    	//if(date==null)//如果没有指定日期，则播放当天的文件
    	//	date = DateFormat.format("yyMMdd", new Date(System.currentTimeMillis())).toString();
        
		currentPrefValue = sharedPref.getString("prefix","AD"); //获取系统设置中的值
		
        String prefix=currentPrefValue;
        
       // if(isAD)
       // 	prefix = "AD";
       // else
       // 	prefix = "SC";
        
        sdDir = Environment.getExternalStorageDirectory();//获取SD卡路径
        path = sdDir.getPath()+MP3_folder+"/";  //MP3存放路径
        fileName = prefix + date + ".MP3"; //文件名：e.g. AD130911.MP3
        
        File f=new File(path + fileName);

                
        //textViewInfo.setText("目标文件：" + path + fileName);
        String today = String.format("%d年%d月%d日", cYear, cMonth+1,cDay);;
        textViewInfo.setText("今天是：" + today);
        textViewInfo.append("\n目标文件：" + fileName);
        
        if(f.exists()){
        	//textViewInfo.append("\n文件存在。");
        	try {
        		//if(mediaPlayer==null)
        			mediaPlayer = new MediaPlayer();
				
        		mediaPlayer.setDataSource(path + fileName);
				mediaPlayer.prepare();
				
				int duration_milisecond = mediaPlayer.getDuration(); //总时长（毫秒）
				seekBar.setMax(duration_milisecond);//设置进度条的最大值 
				

				
				//----------定时器记录播放进度---------//     
                mTimer = new Timer();    
                mTimerTask = new TimerTask() {    
                    @Override    
                    public void run() {     
                        if(isChanging==true) {   //正在被拖动，暂时不要更新进度条
                            return;    
                        }
                        int current_position_milisecond = mediaPlayer.getCurrentPosition();
                        //seekBar.setProgress(current_position_milisecond);//移到Handler里面处理
                                                
                        int time_remaining_second = (mediaPlayer.getDuration() - current_position_milisecond)/1000;
                        int time_remaining_minute = time_remaining_second/60;
                        
                        String sTimeRemaining = "剩余时间：";
                        if(time_remaining_minute > 0)
                        	sTimeRemaining += time_remaining_minute + "分";
                        sTimeRemaining += time_remaining_second%60 + "秒";
                        
                        Message msg = new Message();   
                        msg.what = 1;
                        Bundle bundle = new Bundle();   
                        bundle.putInt("current_position_milisecond", current_position_milisecond);   
                        bundle.putString("sTimeRemaining", sTimeRemaining);   
                        msg.setData(bundle);   
                        mHandler.sendMessage(msg);
                        
                        //textViewTimeRemaining.setText(sTimeRemaining);
                    }    
                };   
                mTimer.schedule(mTimerTask, 0, 10);  

                //设置从哪里开始播
    			if(initProgress>0){
    				mediaPlayer.seekTo(initProgress);
    				seekBar.setProgress(initProgress);
    			}
				mediaPlayer.start();
				
				textViewInfo.append("\n正在播放文件：" + fileName);
				
				int duration_minute = duration_milisecond/1000/60; //总时长：分钟
				int duration_second = duration_milisecond/1000%60; //总时长：秒
								
				textViewInfo.append("\n文件总时长：" + duration_minute + "分" + duration_second + "秒");

				
				buttonControl.setText(getResources().getString(R.string.pause));
				buttonControl.setEnabled(true);
				
		        mediaPlayer.setOnCompletionListener(new OnCompletionListener(){
		            @Override
		            public void onCompletion(MediaPlayer mp) {
		            	buttonControl.setText(getResources().getString(R.string.restart));
		            	mTimerTask.cancel();//停止定时器
		                mp.release();
		            }
		         });
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else{
        	textViewInfo.append("\n找不到文件!");
        	textViewTimeRemaining.setText("剩余时间：0");
        	buttonControl.setEnabled(false);
        }	
     }

    Handler mHandler = new Handler(){   //用于更新显示剩余时间
        public void handleMessage(Message msg) {  
            switch (msg.what) {      
            case 1:      
                //setTitle("hear me?"); 
                int current_position_milisecond = msg.getData().getInt("current_position_milisecond");   
                String sTimeRemaining = msg.getData().getString("sTimeRemaining");  
                
                seekBar.setProgress(current_position_milisecond);
            	textViewTimeRemaining.setText(sTimeRemaining);
                break;      
            }      
            super.handleMessage(msg);  
        }  
    };  
    
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		//保存播放进度和日期：
		SharedPreferences.Editor editor = sharedPref.edit();
		//editor.putInt("Progress", mediaPlayer.getCurrentPosition());
		editor.putInt("Progress", seekBar.getProgress());
		editor.putString("Date", date);
		editor.commit();
		
		stopPlayerAndTimer();
		super.onDestroy();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
    	switch(item.getItemId()){
    	case R.id.action_settings:  //启动设置Activity
    		//open settings
    		startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			//MainActivity.this.finish();
    		
    		//SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
    		currentPrefValue = sharedPref.getString("prefix","AD"); //获取系统设置中的值
    		//Log.i("Current setting", currentPrefValue);
    		
    		return true;
    	
    	case R.id.action_specify_date:	//指定日期播放
  
    		
            new DatePickerDialog(this,     //指定日期对话框
                    mDateSetListener,     
                    cYear, cMonth, cDay).show();
    		
    		return true;
    		
    	default:
    		return super.onOptionsItemSelected(item);
    	}
	}

    private DatePickerDialog.OnDateSetListener mDateSetListener =     
            new DatePickerDialog.OnDateSetListener() {     
        
                public void onDateSet(DatePicker view, int year,      
                                      int monthOfYear, int dayOfMonth) {
                	
                	date = String.format("%02d%02d%02d", year%100, monthOfYear + 1,dayOfMonth);//设置要播放的某天：yymmdd
                	
                	stopPlayerAndTimer();
                	playMyEnglish(0);
                	//Log.d("DatePickerDialog Testing", s);
   
                }     
        }; 

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		String newPrefValue = sharedPref.getString("prefix","AD"); //获取系统设置中的值
		
		if(!newPrefValue.equals(currentPrefValue)){
			stopPlayerAndTimer();
			playMyEnglish(0);//设置已经改变，重新播放
			//Log.i("test", "重新开始1");
		}
		
	}


	private void stopPlayerAndTimer(){
		if(mTimerTask != null)
			mTimerTask.cancel();//停止定时器
		if (mediaPlayer != null)
			mediaPlayer.release();//停止播放器
	}

	//进度条处理   
    class MySeekbar implements OnSeekBarChangeListener {  
        public void onProgressChanged(SeekBar seekBar, int progress,  
                boolean fromUser) {  
        }  
  
        public void onStartTrackingTouch(SeekBar seekBar) {  
            isChanging=true;    
        }  
  
        public void onStopTrackingTouch(SeekBar seekBar) {  
        	mediaPlayer.seekTo(seekBar.getProgress());
        	
        	
            int time_remaining_second = (mediaPlayer.getDuration() - seekBar.getProgress())/1000;
            int time_remaining_minute = time_remaining_second/60;
            
            String sTimeRemaining = "剩余时间：";
            if(time_remaining_minute > 0)
            	sTimeRemaining += time_remaining_minute + "分";
            sTimeRemaining += time_remaining_second%60 + "秒";
            textViewTimeRemaining.setText(sTimeRemaining);
            
            isChanging=false;    
        }  
  
    }  

}
