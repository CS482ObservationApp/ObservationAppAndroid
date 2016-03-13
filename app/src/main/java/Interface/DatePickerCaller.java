package Interface;

import android.widget.DatePicker;

/**
 * Created by zhuol on 3/12/2016.
 */
public interface DatePickerCaller {
    void handleDatePickerSetData(DatePicker view, int year, int month, int day);
    void handleDatePickerCancelled();
}
