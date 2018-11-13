package raspberrypiluncher.android.lyon.com.raspberrypiluncher.tool;

import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by Gordon on 2016/12/31.
 */

public class Alert {
    public static void showAlert(Context context, CharSequence title, CharSequence message, CharSequence btnTitle) {
        android.support.v7.app.AlertDialog.Builder dlgBuilder = new android.support.v7.app.AlertDialog.Builder(context);;
        //dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dlgBuilder.setTitle(title);
        dlgBuilder.setMessage(message);
        dlgBuilder.setCancelable(false);
        dlgBuilder.setPositiveButton(btnTitle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dlgBuilder.setNeutralButton(btnTitle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dlgBuilder.show();
    }
}
