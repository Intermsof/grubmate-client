package filter;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.udacity.test.R;

import java.util.Calendar;

import objects.CustomOnItemSelectedListener;
import objects.NetworkManager;
import objects.UserSingleton;

/**
 * Created by Lauren on 10/23/2017.
 */

public class FilterNewsActivity extends AppCompatActivity implements View.OnClickListener{

    private Spinner spinner;
    private Button button;
    private String currentspinner;
    private UserSingleton owner;

    private Button fromDatePicker, fromTimePicker, toDatePicker, toTimePicker;
    private EditText fromDate, fromTime, toDate, toTime;
    private int fYear, fMonth, fDay, fHour, fMinute;
    private int tYear, tMonth, tDay, tHour, tMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_news);
        setTitle("Filter");

        owner = UserSingleton.getUserInstance();

        fYear = -1;
        fMonth = -1;
        fDay = -1;
        tYear = -1;
        tMonth = -1;
        tDay = -1;
        fHour = -1;
        fMinute = -1;
        tHour = -1;
        tMinute = -1;

        addItemsOnSpinner();
        addListenerOnSpinnerItemSelection();
        addListenersOnPickers();
        addListenerOnButton();

    }

    //add the items to the spinner
    public void addItemsOnSpinner() {
        spinner = (Spinner) findViewById(R.id.spinner);
        String[] list = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(dataAdapter);
    }

    //add listeners
    public void addListenerOnSpinnerItemSelection() {
        spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    public void addListenersOnPickers() {
        fromDatePicker = (Button) findViewById(R.id.btn_from_date);
        fromTimePicker = (Button) findViewById(R.id.btn_from_time);
        fromDate = (EditText) findViewById(R.id.from_date);
        fromTime = (EditText) findViewById(R.id.from_time);

        toDatePicker = (Button) findViewById(R.id.btn_to_date);
        toTimePicker = (Button) findViewById(R.id.btn_to_time);
        toDate = (EditText) findViewById(R.id.to_date);
        toTime = (EditText) findViewById(R.id.to_time);

        fromDatePicker.setOnClickListener(this);
        fromTimePicker.setOnClickListener(this);

        toDatePicker.setOnClickListener(this);
        toTimePicker.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == fromDatePicker) {
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            fYear = c.get(Calendar.YEAR);
            fMonth = c.get(Calendar.MONTH);
            fDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    fromDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                }
            }, fYear, fMonth, fDay);
            datePickerDialog.show();
        }
        if (v == fromTimePicker) {
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            fHour = c.get(Calendar.HOUR_OF_DAY);
            fMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    fromTime.setText(hourOfDay + ":" + minute);
                    fHour = hourOfDay;
                    fMinute = minute;
                }
            }, fHour, fMinute, true);
            timePickerDialog.show();
        }
        if (v == toDatePicker) {
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            tYear = c.get(Calendar.YEAR);
            tMonth = c.get(Calendar.MONTH);
            tDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    toDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                }
            }, tYear, tMonth, tDay);
            datePickerDialog.show();
        }
        if (v == toTimePicker) {
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            tHour = c.get(Calendar.HOUR_OF_DAY);
            tMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    toTime.setText(hourOfDay + ":" + minute);
                    tHour = hourOfDay;
                    tMinute = minute;
                }
            }, tHour, tMinute, true);
            timePickerDialog.show();
        }
    }

    public void addListenerOnButton() {
        button = (Button) findViewById(R.id.filterbutton);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(FilterNewsActivity.this, "OnClickListener :" + "\nSpinner : "+ String.valueOf(spinner.getSelectedItem()),
                        Toast.LENGTH_SHORT).show();

                filterNews();
                finish();
            }
        });
    }

    public void filterNews(){
        //get current value of spinner
        currentspinner = String.valueOf(spinner.getSelectedItem());

        NetworkManager networkManager = new NetworkManager();
        networkManager.getFilteredPostsForUser(currentspinner, fHour, fMinute, tHour, tMinute);
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

}
