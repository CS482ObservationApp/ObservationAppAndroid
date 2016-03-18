package ViewAndFragmentClass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by zhuol on 3/13/2016.
 */
public class ScrollableTextview extends TextView{
    public ScrollableTextview(Context context){
        super(context);
    }

    public ScrollableTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollableTextview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);
    }
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            this.getParent().requestDisallowInterceptTouchEvent(true);
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            this.getParent().requestDisallowInterceptTouchEvent(false);
        }

        return super.onTouchEvent(ev);
    }
}
