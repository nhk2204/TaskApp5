package jp.techacademy.kanta.nakayama.taskapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;

public class InputActivity extends AppCompatActivity {

    private int mYear,mMonth,mDay,mHour,mMinute;
    private Button mDateButton,mTimeButton,mCategoryButton;
    private EditText mTitleEdit,mContentEdit;
    private Task mTask;

    //input画面で年月日ボタンを押した際の動作（年月日の選択）
    private View.OnClickListener mOnDateClickListener=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog=new DatePickerDialog(InputActivity.this,new DatePickerDialog.OnDateSetListener(){
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mYear=year;
                    mMonth=monthOfYear;
                    mDay=dayOfMonth;
                    String dateString=mYear +"/" +String.format("%02d",(mMonth+1)) +"/"+String.format("%02d",mDay);
                    mDateButton.setText(dateString);
                }
            },mYear,mMonth,mDay);
            datePickerDialog.show();
        }
    };

    //input画面で時間ボタンを押した際の動作（時間の選択）
    private View.OnClickListener mOnTimeClickListener=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog=new TimePickerDialog(InputActivity.this,new TimePickerDialog.OnTimeSetListener(){
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour=hourOfDay;
                    mMinute=minute;
                    String timeString=String.format("%02d",mHour)+":"+String.format("%02d",mMinute);
                    mTimeButton.setText(timeString);
                }
            },mHour,mMinute,false);
            timePickerDialog.show();
        }
    };

    //カテゴリボタンを押した際の動作（カテゴリ選択、作成画面への移動）
    private View.OnClickListener mOnCategoryClickListener=(new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(InputActivity.this,CategoryActivity.class);

            String title=mTitleEdit.getText().toString();
            String content=mContentEdit.getText().toString();
            //String category=mCategoryEdit.getText().toString();

            mTask.setTitle(title);
            mTask.setContents(content);
            //mTask.setCategory(category);
            GregorianCalendar calendar=new GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute);
            Date date=calendar.getTime();
            mTask.setDate(date);

            mTask.setToIntent(intent);
            startActivity(intent);
        }
    });

    //input画面で「決定」ボタンを押した際の動作（タスクの追加）
    private View.OnClickListener mOnDoneClickListener=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            addTask();
            Intent intent=new Intent(InputActivity.this,MainActivity.class);
            startActivity(intent);
            //finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        //ActionBarを設定する
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //UI部品の設定
        mDateButton=(Button)findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton=(Button)findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        mCategoryButton=(Button)findViewById(R.id.category_edit_text);
        mCategoryButton.setOnClickListener(mOnCategoryClickListener);

        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);
        mTitleEdit=(EditText)findViewById(R.id.title_edit_text);
        mContentEdit=(EditText)findViewById(R.id.content_edit_text);

        //Intent
        //mTaskに直接放り込もうとするとエラーする。
        //理由不明。
        Intent intent=getIntent();
        Task task=new Task();
        task.getFromIntent(intent);

        mTask=task;

        if(mTask.getId()==-1){
            //新規作成の場合
            Calendar calendar=Calendar.getInstance();
            mYear=calendar.get(Calendar.YEAR);
            mMonth=calendar.get(Calendar.MONTH);
            mDay=calendar.get(Calendar.DAY_OF_MONTH);
            mHour=calendar.get(Calendar.HOUR_OF_DAY);
            mMinute=calendar.get(Calendar.MINUTE);

        }else{
            //更新の場合
            mTitleEdit.setText(mTask.getTitle());
            mContentEdit.setText(mTask.getContents());

            //if分の条件がないとエラーが出る。
            //おそらくだが、mTask.getCategory()(=null)のものに.getCategoryName()メソッドを問おうとしたためエラーしたものと思われる。
            //もとがnullなのでCategoryとしての機能を持たないのだろう。
            if(mTask.getCategory()!=null) {
                mCategoryButton.setText(mTask.getCategory().getCategoryName());
            }
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(mTask.getDate());
            mYear=calendar.get(Calendar.YEAR);
            mMonth=calendar.get(Calendar.MONTH);
            mDay=calendar.get(Calendar.DAY_OF_MONTH);
            mHour=calendar.get(Calendar.HOUR_OF_DAY);
            mMinute=calendar.get(Calendar.MINUTE);

            String dateString=mYear+"/"+String.format("%02d",(mMonth+1))+"/"+String.format("%02d",mDay);
            String timeString=String.format("%02d",mHour)+":"+String.format("%02d",mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);
        }
    }

    private void addTask(){
        Realm realm=Realm.getDefaultInstance();

        if(mTask.getId()==-1){
            //新規作成の場合
            mTask=new Task();
            RealmResults<Task> taskRealmResults=realm.where(Task.class).findAll();

            int identifier;
            if(taskRealmResults.max("id")!=null){
                identifier=taskRealmResults.max("id").intValue()+1;
            }else{
                identifier=0;
            }
            mTask.setId(identifier);
        }

        String title=mTitleEdit.getText().toString();
        String content=mContentEdit.getText().toString();
        //String category=mCategoryEdit.getText().toString();

        mTask.setTitle(title);
        mTask.setContents(content);
        //mTask.setCategory(category);
        GregorianCalendar calendar=new GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute);
        Date date=calendar.getTime();
        mTask.setDate(date);

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(mTask);
        realm.commitTransaction();

        realm.close();

        Intent resultIntent=new Intent(getApplicationContext(),TaskAlarmReceiver.class);
        //resultIntent.putExtra(MainActivity.EXTRA_TASK,mTask);

        mTask.setToIntent(resultIntent);
        PendingIntent resultPendingIntent=PendingIntent.getBroadcast(
                this,
                mTask.getId(),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),resultPendingIntent);
    }
}
