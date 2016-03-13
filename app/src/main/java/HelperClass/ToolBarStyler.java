package HelperClass;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 3/12/2016.
 */
public class ToolBarStyler {
    public static void styleToolBar(AppCompatActivity context, Toolbar toolbar,String title){
        toolbar.setTitle(""); //The reason why set it empty first, see here: http://stackoverflow.com/questions/26486730/in-android-app-toolbar-settitle-method-has-no-effect-application-name-is-shown
        context.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.icon_app);
        toolbar.setTitle(title);
    }
}
