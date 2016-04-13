/**	 ObservationApp, Copyright 2016, University of Prince Edward Island,
 550 University Avenue, C1A4P3,
 Charlottetown, PE, Canada
 *
 * 	 @author Kent Li <zhuoli@upei.ca>
 *
 *   This file is part of ObservationApp.
 *
 *   ObservationApp is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */

package HelperClass;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 3/12/2016.
 */
public class ToolBarStyler {
    public static void styleToolBar(AppCompatActivity context, Toolbar toolbar,String title){
        toolbar.setTitle(""); //The reason why set it empty first, see here: http://stackoverflow.com/questions/26486730/in-android-app-toolbar-settitle-method-has-no-effect-application-name-is-shown
        context.setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.icon_app_medium);
        toolbar.setTitle(title);
    }
}
