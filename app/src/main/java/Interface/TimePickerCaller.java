package Interface;

import android.widget.TimePicker;

/**
 * Created by zhuol on 3/3/2016.
 */
public interface TimePickerCaller {
    void handleTimePickerSetData(TimePicker view, int hourOfDay, int minute);
    void handleTimePickerCancelled();
}
