package ca.zhuoliupei.observationapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.GridView;

import org.junit.Test;

import ViewAndFragmentClass.GridViewWithHeaderAndFooter;

public class NewestObservationsActivityTest extends ActivityInstrumentationTestCase2<NewestObservationsActivity> {


    public NewestObservationsActivityTest() {
        super(NewestObservationsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    //Click on the first item, see if it takes us to "ObservationDetailActivity"
    @Test
    public void testNewestObservationsItemClick()
    {

        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ObservationDetailActivity.class.getName(), null, false);

        ((GridViewWithHeaderAndFooter) getActivity().findViewById(R.id.content_gridview_NewestObsrvationActivity)).getFirstVisiblePosition();


        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                GridViewWithHeaderAndFooter grid = (GridViewWithHeaderAndFooter) getActivity().findViewById(R.id.content_gridview_NewestObsrvationActivity);

                grid.getAdapter().getView(0, null, null).performClick();


                //   grid.performItemClick(this,grid.getFirstVisiblePosition(),
                grid.requestFocusFromTouch();
                grid.setSelection(0);
                grid.performItemClick(grid.getAdapter().getView(0, null, null), 0, 0);

            }
        });

        Activity detailActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);

        assertNotNull(detailActivity);





    }

}
