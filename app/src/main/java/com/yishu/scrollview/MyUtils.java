package com.yishu.scrollview;

import android.content.Context;
import android.view.WindowManager;

/**
 * Created by simon on 17/6/5.
 */

class MyUtils {
    public static int getScreenMetrics(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        return width;
    }
}
