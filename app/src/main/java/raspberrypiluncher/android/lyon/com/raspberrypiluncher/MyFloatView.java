package raspberrypiluncher.android.lyon.com.raspberrypiluncher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;



import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;

import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MyFloatView extends android.support.v7.widget.AppCompatImageView {
	private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;
    Thread   thread ;
    boolean touchormove = true;

    int whight=0;
    int wwidth=0;
    
    Context context;
    Bitmap backimg;
    Bitmap homeimg;
    Bitmap recentimg;
    Bitmap closerecentimg;
    
    boolean backkey = false;
    boolean homekey = false;
    boolean recentkey = false;
    boolean closerecent = false;
    
    ////此wmParams变量为获取的全局变量，用以保存悬浮窗口的属性  
    private WindowManager.LayoutParams wmParams = ((MyApplication)getContext().getApplicationContext()).getMywmParams();
    int vWidth=1;
	int vHeight=1;
	int  wWidth=1 ;
	int  wHeight=1;
	//recent
	private List<HashMap<String,Object>> appInfos = new ArrayList<HashMap<String, Object>>();
	private List<HashMap<String,Object>> appInfos2 = new ArrayList<HashMap<String, Object>>();
	private static int MAX_RECENT_TASKS = 13;  
	private static int repeatCount = 13;//保证上面两个值相等
	boolean recentboolean = false;
	Bitmap recenttasktimg[]= new Bitmap[MAX_RECENT_TASKS+1]  ;
	String recenttasktstr[]= new String[MAX_RECENT_TASKS+1]  ;
	boolean recenttasktboolean[]= new boolean[MAX_RECENT_TASKS+1]  ;
	//back
	int backi=1;
	WindowManager wm;

	public MyFloatView(Context context , int vWidth , int vHeight ,int  wWidth ,int  wHeight, 	WindowManager wm) {
		super(context);		
		// TODO Auto-generated constructor stub
		this.context = context;
		this.wm=wm;
		backimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.back1);
		homeimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.home1);
		recentimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.recent1);
		
		closerecentimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.closerecentlist);
		this.vWidth=vWidth;
		this.vHeight=vHeight;
		this.wWidth=wWidth;
		this.wHeight=wHeight;
		for(int i = 0; i<recenttasktimg.length;i++){
			recenttasktimg[i]=null;
		}
		
	}
	
	 @Override
	 public boolean onTouchEvent(MotionEvent event) {
		 backimg = getzoom(backimg, vWidth/9*2,vHeight/20);
	     homeimg = getzoom(homeimg, vWidth/9*2,vHeight/20);
	     recentimg = getzoom(recentimg, vWidth/9*2,vHeight/20);
		 //获取相对屏幕的坐标，即以屏幕左上角为原点  
		 x = event.getRawX();   //絕對位置
	     y = event.getRawY()-25;   //扣除掉STATUS BAR 部分//25是系统状态栏的高度  
	     Log.i("currP", "currX"+x+"====currY"+y);
	     switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN: //捕获手指触摸按下动作  
                //获取相对View的坐标，即以此View左上角为原点  
	        	mTouchStartX =  event.getX();  //相對位置
                mTouchStartY =  event.getY();
                //Toast.makeText(context, "startX"+mTouchStartX+"====startY"+mTouchStartY, Toast.LENGTH_SHORT).show();
	            //Log.d("startP", "startX"+mTouchStartX+"====startY"+mTouchStartY);
	            touchormove = true;
	            //setImageResource(R.drawable.ic_launcher2);
	            //back 鍵
	            if(mTouchStartX>0 && mTouchStartX<wmParams.width/6*2 && mTouchStartY>0 && mTouchStartY<backimg.getHeight()+20){
	            	backkey=true;
	            	backimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.back2);
	            }
	            //home 鍵
	            else if(mTouchStartX>wmParams.width/6*2 && mTouchStartX<wmParams.width/6*4 && mTouchStartY>0 && mTouchStartY<backimg.getHeight()+20){
	            	homekey=true;
	            	homeimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.home2);
	            }
	            //recent 鍵
	            else if(mTouchStartX>wmParams.width/6*4 && mTouchStartX<wmParams.width && mTouchStartY>0 && mTouchStartY<backimg.getHeight()+20){
	            	recentkey=true;
	            	recentimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.recent2);
	            }
	            //判斷recent 開啟後 的位置
	            if(recentboolean){
	            	for(int i = 0; i<appInfos.size();i++){
	            		if(		mTouchStartX>recenttasktimg[i].getWidth()*(i%4)
			            		&&		mTouchStartX<recenttasktimg[i].getWidth()*((i%4)+1)
			            		&&		mTouchStartY>backimg.getHeight()+20+recenttasktimg[i].getHeight()*(i/4)
			            		&& 		mTouchStartY>backimg.getHeight()+20+recenttasktimg[i].getHeight()*((i/4))
			            ){
	            			recenttasktboolean[i]=true;
	            		}
	            	}
	            }
	            break;
	        case MotionEvent.ACTION_MOVE:	            
	            updateViewPosition();
	            touchormove = false;
	            closerecent = false;
                backkey=false;
	            homekey=false;
	            recentkey=false;
	            backimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.back1);
	            homeimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.home1);
	    		recentimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.recent1);
	    		backimg = getzoom(backimg, vWidth/9*2,vHeight/20);
		   	    homeimg = getzoom(homeimg, vWidth/9*2,vHeight/20);
		   	    recentimg = getzoom(recentimg, vWidth/9*2,vHeight/20);
	            break;

	        case MotionEvent.ACTION_UP:
	        	 //获取相对View的坐标，即以此View左上角为原点  
	        	mTouchStartX =  event.getX();  //相對位置
                mTouchStartY =  event.getY();
                if(touchormove){
	        		//Toast.makeText(context, "2123", Toast.LENGTH_SHORT).show();
	        		//Toast.makeText(context, "startX"+mTouchStartX+" startY"+mTouchStartY, Toast.LENGTH_SHORT).show();
		        	touchormove = false;
		        	//setImageResource(R.drawable.ic_launcher);
		        	//back 鍵
		            if(mTouchStartX>0 && mTouchStartX<wmParams.width/6*2 && backkey==true && mTouchStartY>0 && mTouchStartY<backimg.getHeight()+20){
		            	//Toast.makeText(context, "backkey", Toast.LENGTH_SHORT).show();
		            	//super.onBackPressed();
		            	reloadButtons();
		            	/*if(backi==MAX_RECENT_TASKS-1){
	            			backi=1;
	            		}
		            	Thread t = new Thread( new Runnable() {
		            		int count=0;
		            		public void run() {
		            	        // TODO Auto-generated method stub
		            	        // 需要背景作的事
		            	        while (count < 10) {
		            	            count++;
		            	            try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
		            	            
		            	        }
		            	        backi=1;
		            	    }
		            	});
		            	t.start();*/
		            	backi=1;
		            	if (appInfos.get(backi).get("tag") != null) {
		            		Intent singleIntent = (Intent) appInfos.get(backi).get("tag");
		            		context.startActivity(singleIntent);
		            	}
		            	//backi++;
		            	
		            }
		            //home 鍵
		            else if(mTouchStartX>wmParams.width/6*2 && mTouchStartX<wmParams.width/6*4 && homekey==true && mTouchStartY>0 && mTouchStartY<backimg.getHeight()+20){
		            	//Toast.makeText(context, "homekey", Toast.LENGTH_SHORT).show();
		            	 Intent i= new Intent(Intent.ACTION_MAIN); 
					     i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
					     i.addCategory(Intent.CATEGORY_HOME); 
					     context.startActivity(i);  
					     
					     wmParams.width=vWidth/3*2;
						 wmParams.height=vHeight/20+20;
						 recentboolean = false;
					}
		            //recent 鍵
		            else if(mTouchStartX>wmParams.width/6*4 && mTouchStartX<wmParams.width && recentkey==true && mTouchStartY>0 && mTouchStartY<backimg.getHeight()+20){
		            	//Toast.makeText(context, "recentkey", Toast.LENGTH_SHORT).show();
		            	if(recentboolean){
		            		Toast.makeText(context, "recent close key", Toast.LENGTH_SHORT).show();
			            	recentboolean=false;
			            	wmParams.width=vWidth/3*2;
							wmParams.height=vHeight/20+20;
		            	}else{
		            		recentboolean = true;
			            	showRecentAppsDialog();
		            	}
		                //顯示dialog
		                //recentdialog.show();
		            }
		          //判斷recent 開啟後 的位置
		            if(recentboolean){
		            	for(int i = 0; i<appInfos.size();i++){
		            		if(		mTouchStartX>recenttasktimg[i].getWidth()*(i%4)
		            		&&		mTouchStartX<recenttasktimg[i].getWidth()*((i%4)+1)
		            		&&		mTouchStartY>backimg.getHeight()+20+recenttasktimg[i].getHeight()*(i/4)
		            		&& 		mTouchStartY>backimg.getHeight()+20+recenttasktimg[i].getHeight()*((i/4))
		            		&&		recenttasktboolean[i]==true
		            		){
		            			if (appInfos.get(i).get("tag") != null) {
		        					Intent singleIntent = (Intent) appInfos.get(i).get("tag");
		        					context.startActivity(singleIntent);
		        					recentboolean=false;
					            	wmParams.width=vWidth/3*2;
									wmParams.height=vHeight/20+20;
		        		        }
		            		}
		            		recenttasktboolean[i]=false;
		            	}
		            }
		        }
                closerecent = false;
                recentkey = false;
                homekey = false;
                backkey = false;
                backimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.back1);
	            homeimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.home1);
	    		recentimg = BitmapFactory.decodeResource( context.getResources(), R.drawable.recent1);
	    		backimg = getzoom(backimg, vWidth/9*2,vHeight/20);
		   	    homeimg = getzoom(homeimg, vWidth/9*2,vHeight/20);
		   	    recentimg = getzoom(recentimg, vWidth/9*2,vHeight/20);
	    		
		        //回歸初始值
	    		mTouchStartX=mTouchStartY=0;
	    		
	    		wm.updateViewLayout(this, wmParams);
	            
	    		break;
	       
	        }
	     	return false;
		}
	
	 
	


	private void updateViewPosition(){
		//載入手指觸控位置 //更新浮动窗口位置参数  
		wmParams.x=(int)(x-mTouchStartX);
		wmParams.y=(int)(y-mTouchStartY);
		if(wmParams.y<10)
			wmParams.y=10;
		if(wmParams.y>wHeight-25-vHeight/20)
			wmParams.y=wHeight-25-vHeight/20;
		if(wmParams.x<2)
			wmParams.x=2;
		if(wmParams.x>wWidth-vWidth/3*2)
			wmParams.x=wWidth-vWidth/3*2;
		//Log.d("startP", "Lyon Hsu startX "+mTouchStartX+"====startY "+mTouchStartY);
		//Log.d("startP", "Lyon Hsu wmParams.x "+wmParams.x+"====wmParams.y "+wmParams.y);
		//Log.d("startP", "Lyon Hsu X "+x+"====Y "+y);
	    wm.updateViewLayout(this, wmParams);
	    
	 }

	 public void onDraw(Canvas canvas)  
	    {  
	        super.onDraw(canvas);  
	        Paint vPaint = new Paint();  // 繪製樣式物件 
	        vPaint.setColor(Color.BLACK); 
	        backimg = getzoom(backimg, vWidth/9*2,vHeight/20);
	   	    homeimg = getzoom(homeimg, vWidth/9*2,vHeight/20);
	   	    recentimg = getzoom(recentimg, vWidth/9*2,vHeight/20);
	   	    
	   	    Paint p = new Paint();  
	   	    p.setColor(Color.GRAY);
	   	    p.setAlpha( 80 );  // Bitmap 透明度 (0 ~ 100)
	   	    p.setStyle(Paint.Style.FILL);//充滿  
	   	    p.setAntiAlias(true);// 設置畫筆的鋸齒效果  
	   	    RectF oval3 = new RectF(0,0,wmParams.width, wmParams.height);// 設置個新的長方形  
	   	    canvas.drawRoundRect(oval3, 20, 15, p);//第二個参數是x半徑，第三個参數是y半徑  
	   	   
	   	    canvas.drawBitmap(backimg, wmParams.width/6-backimg.getWidth()/2, 5, vPaint);
	   	    
	   	    p.setColor(Color.GRAY);
	   	    RectF oval2 = new RectF(0,0,backimg.getWidth(), backimg.getHeight());// 設置個新的長方形  
	   	    p.setAlpha( 100 );  // Bitmap 透明度 (0 ~ 100)
	   	    //canvas.drawRoundRect(oval2, 20, 15, p);//第二個参數是x半徑，第三個参數是y半徑  
	   	    
	        canvas.drawBitmap(homeimg, wmParams.width/6*3-homeimg.getWidth()/2, 5, vPaint);  	
	        //canvas.drawBitmap(recentimg, wmParams.width/6*5-recentimg.getWidth()/2, 5, vPaint);
	        
	        if(recentboolean && appInfos.size()>0){
	        	for(int i = 0; i<appInfos.size();i++){
	        		canvas.drawBitmap(recenttasktimg[i], recenttasktimg[i].getWidth()*(i%4),
	        					backimg.getHeight()+20+recenttasktimg[i].getHeight()*(i/4), vPaint);  
	        	}
	        }
	        else if(recentboolean){
	        	vPaint.setTextSize(50);
	        	canvas.drawText("NO Recent Task!!", wmParams.width/6-backimg.getWidth()/2, 
	        			backimg.getHeight()+20, vPaint);
	        }
	        invalidate();
	    }  
	 
	 private void showRecentAppsDialog(){
		 reloadButtons();
		 for(int i = 0; i<recenttasktimg.length;i++){
				recenttasktimg[i]=null;
		}
		 if(recentboolean){
			 if(appInfos.size()>12){
				 wmParams.width=vWidth/3*2;
				 wmParams.height= wmParams.width/4*5;
			 }
			 else if(appInfos.size()>8){
				 wmParams.width=vWidth/3*2;
				 wmParams.height= wmParams.width;
			 }
			 else if(appInfos.size()>4){
				 wmParams.width=vWidth/3*2;
				 wmParams.height= vWidth/2;
			 }else{
				 wmParams.width=vWidth/3*2;
				 wmParams.height= vWidth/3+20;
			 }
			 for(int i = 0 ;  i<appInfos.size();i++){
				 Bitmap bimg = (Bitmap) appInfos.get(i).get("icon");
				 Bitmap aimg = getzoom(bimg, vWidth/6, vWidth/6);
				 Log.d("", "Lyon hsu mouse aimg.w="+aimg.getWidth()+" "+aimg.getHeight());
				 recenttasktimg[i] = aimg ;
				 recenttasktstr[i] = (String) appInfos.get(i).get("title");
			}
		 }
		 //更新浮动窗口位置参数  
		 wm.updateViewLayout(this, wmParams);
	 }
	 public Bitmap getzoom(Bitmap bitmap,int newWidth,int newHeight){
		 Log.d("", "Lyon hsu mouse bitmap.w="+bitmap.getWidth());
			Matrix vMatrix = new Matrix();
	        // 獲得圖片的寬高
	        int width = bitmap.getWidth();
	        int height = bitmap.getHeight();
	        // 設置想要的大小
	        int newWidth1 = newWidth;
	        int newHeight1 = newHeight;
	        // 計算縮放比例
	        float scaleWidth = ((float) newWidth1) / width;
	        float scaleHeight = ((float) newHeight1) / height;
	        vMatrix.postScale(scaleWidth,scaleHeight);//放大postScale
	        
	        Bitmap vB3 = Bitmap.createBitmap( bitmap
											,0
											,0
					                       ,width
					                       ,height
					                       ,vMatrix
					                       , true
					                       );
	        Log.d("", "Lyon hsu mouse vB3.w="+vB3.getWidth()+" "+vB3.getHeight()+" scaleWidth "+scaleWidth);
	        vMatrix.reset();
	        return vB3;
		}

	
		 
	 private void reloadButtons() {
		 appInfos.clear();
	        //得到包管理器和activity管理器
		    final PackageManager pm = context.getPackageManager();
		    final ActivityManager am = (ActivityManager)
		            context.getSystemService(Context.ACTIVITY_SERVICE);
		    //从ActivityManager中取出用户最近launch过的 MAX_RECENT_TASKS + 1 个，以从早到晚的时间排序，
		    //注意这个   0x0002,它的值在launcher中是用ActivityManager.RECENT_IGNORE_UNAVAILABLE
		    //但是这是一个隐藏域，因此我把它的值直接拷贝到这里
		    final List<ActivityManager.RecentTaskInfo> recentTasks =
		            am.getRecentTasks(MAX_RECENT_TASKS , ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		
		    //这个activity的信息是我们的launcher
		    ActivityInfo homeInfo = 
		        new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
		                .resolveActivityInfo(pm, 0);
		    int numTasks = recentTasks.size();
		    for (int i = 0; i < numTasks && (i < MAX_RECENT_TASKS); i++) {
		            HashMap<String, Object> singleAppInfo = new HashMap<String, Object>();//当个启动过的应用程序的信息
		        final ActivityManager.RecentTaskInfo info = recentTasks.get(i);
		
		        Intent intent = new Intent(info.baseIntent);
		        if (info.origActivity != null) {
		            intent.setComponent(info.origActivity);
		        }
		        /**
		         * 如果找到是launcher，直接continue，后面的appInfos.add操作就不会发生了
		         */
		        if (homeInfo != null) {
		            if (homeInfo.packageName.equals(
		                    intent.getComponent().getPackageName())
		                    && homeInfo.name.equals(
		                            intent.getComponent().getClassName())) {
		                    MAX_RECENT_TASKS = MAX_RECENT_TASKS + 1;
		                continue;
		            }
		        }
		
		        //设置intent的启动方式为 创建新task()【并不一定会创建】
		        intent.setFlags((intent.getFlags()&~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
		                | Intent.FLAG_ACTIVITY_NEW_TASK);
		        //获取指定应用程序activity的信息(按我的理解是：某一个应用程序的最后一个在前台出现过的activity。)
		        final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
		        if (resolveInfo != null) {
		            final ActivityInfo activityInfo = resolveInfo.activityInfo;
		            
		            final String title = activityInfo.loadLabel(pm).toString();
		            
		            Bitmap icon = ((BitmapDrawable) activityInfo.loadIcon(pm)).getBitmap() ;;
		           
		        	
		            String packageStr=activityInfo.packageName;
		            if (title != null && title.length() > 0 && icon != null) {
		            		singleAppInfo.put("resolveInfo", resolveInfo);
		                    singleAppInfo.put("title", title);
		                    singleAppInfo.put("icon", icon);
		                    singleAppInfo.put("tag", intent);
		                    singleAppInfo.put("package", packageStr);
		                    appInfos.add(singleAppInfo);
		            }
		        }
		        
		    }
		    MAX_RECENT_TASKS = repeatCount;
		}
	   
}
