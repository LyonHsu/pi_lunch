package raspberrypiluncher.android.lyon.com.raspberrypiluncher;

import android.app.Application;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * Created by i_hfuhsu on 2017/9/18.
 */

public class MyApplication extends Application {
    /**
     * 創建全局變數
     * 註意在AndroidManifest.xml中的Application節點添加android:name=".MyApplication"屬性
     *
     */
    private WindowManager.LayoutParams wmParams=new WindowManager.LayoutParams();

    public WindowManager.LayoutParams getMywmParams(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowManager.LayoutParams params = new
                    WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    PixelFormat.TRANSLUCENT);
            params.gravity= Gravity.CENTER;
            params.x=0;
            params.y=0;
            return params;
        }

        return wmParams;
    }
}
