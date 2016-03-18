package HelperClass;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

/**
 * Created by zhuol on 3/17/2016.
 */
public class AnimationUtil {
    public static RotateAnimation getRotateAnimation(){
        RotateAnimation rotateAnimation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.RESTART);
        rotateAnimation.setDuration(1000);
        return  rotateAnimation;
    }
}
