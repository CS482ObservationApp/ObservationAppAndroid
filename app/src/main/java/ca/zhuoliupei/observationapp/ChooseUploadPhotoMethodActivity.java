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
package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;


/**
 * This Activity class is used to provide a simple UI for picking a method to upload image
 * This Activity returns a "result" extras in the intent
 * The result is either CHOOSE_PHOTO_FROM_CAMERA or CHOOSE_PHOTO_FROM_GALARY
 */
public class ChooseUploadPhotoMethodActivity extends Activity {

    public static final int CHOOSE_PHOTO_FROM_CAMERA =1;
    public static final int CHOOSE_PHOTO_FROM_GALARY =2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_upload_method);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addOnClickListeners();
    }

    private void addOnClickListeners(){
        addLinearLayoutOnClick();
        addUploadFromGallaryOnClick();
        addTakePhotoOnClick();
    }
    private void addLinearLayoutOnClick(){
        // If user clicks on blank space, not the two options, finish the activity
        findViewById(R.id.root_LL_ChooseUploadPhotoMethodActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }
    private void addTakePhotoOnClick(){
        findViewById(R.id.imgTakeAPhoto_ChooseUploadMethodActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("result",CHOOSE_PHOTO_FROM_CAMERA);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
    private void addUploadFromGallaryOnClick(){
        findViewById(R.id.imgUploadFromGallary_ChooseUploadMethodActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("result",CHOOSE_PHOTO_FROM_GALARY);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

}
