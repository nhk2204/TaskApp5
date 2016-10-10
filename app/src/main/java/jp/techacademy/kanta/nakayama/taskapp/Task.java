package jp.techacademy.kanta.nakayama.taskapp;

import android.content.Intent;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by nhk2204 on 2016/09/22.
 */
public class Task extends RealmObject{
    private String title;   //タイトル
    private String contents;    //内容
    private Date date;  //日時
    private Category category;    //カテゴリ

    //idをプライマリキーとして設定
    @PrimaryKey
    private int id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title){
        this.title=title;
    }

    public String getContents(){
        return contents;
    }

    public void setContents(String contents){
        this.contents=contents;
    }

    public Category getCategory(){
        return category;
    }

    public void setCategory(Category category){
        this.category=category;
    }

    public Date getDate(){
        return date;
    }

    public void setDate(Date date){
        this.date=date;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id=id;
    }

    //指定したintentにそれぞれのデータを格納する。
    public void setToIntent(Intent intent){
        intent.putExtra(IntentIDs.EXTRA_TASK_ID,this.id);
        intent.putExtra(IntentIDs.EXTRA_TASK_TITLE,this.title);

        Calendar ExtraCal=Calendar.getInstance();
        ExtraCal.setTime(this.date);
        intent.putExtra(IntentIDs.EXTRA_TASK_DATE_YEAR,ExtraCal.get(Calendar.YEAR));
        intent.putExtra(IntentIDs.EXTRA_TASK_DATE_MONTH,ExtraCal.get(Calendar.MONTH));
        intent.putExtra(IntentIDs.EXTRA_TASK_DATE_DAY,ExtraCal.get(Calendar.DAY_OF_MONTH));
        intent.putExtra(IntentIDs.EXTRA_TASK_DATE_HOUR,ExtraCal.get(Calendar.HOUR_OF_DAY));
        intent.putExtra(IntentIDs.EXTRA_TASK_DATE_MINUTE,ExtraCal.get(Calendar.MINUTE));
        intent.putExtra(IntentIDs.EXTRA_TASK_CONTENTS,this.contents);

        if(this.category!=null){
            intent.putExtra(IntentIDs.EXTRA_CATEGORY_ID, this.category.getCategoryId());
            intent.putExtra(IntentIDs.EXTRA_CATEGORY_NAME, this.category.getCategoryName());
        }
    }

    public void getFromIntent(Intent intent){
        this.id=intent.getIntExtra(IntentIDs.EXTRA_TASK_ID,-1);
        this.title=intent.getStringExtra(IntentIDs.EXTRA_TASK_TITLE);
        this.contents=intent.getStringExtra(IntentIDs.EXTRA_TASK_CONTENTS);

        int categoryId=intent.getIntExtra(IntentIDs.EXTRA_CATEGORY_ID,-1);
        String categoryName=intent.getStringExtra(IntentIDs.EXTRA_CATEGORY_NAME);
        if(categoryId==-1){
            this.category=null;
        }else {
            Category category=new Category();
            category.setCategoryId(categoryId);
            category.setCategoryName(categoryName);
            this.category = category;
        }
        int mYear,mMonth,mDay,mHour,mMinute;
        mYear=intent.getIntExtra(IntentIDs.EXTRA_TASK_DATE_YEAR,2112);
        mMonth=intent.getIntExtra(IntentIDs.EXTRA_TASK_DATE_MONTH,9);
        mDay=intent.getIntExtra(IntentIDs.EXTRA_TASK_DATE_DAY,3);
        mHour=intent.getIntExtra(IntentIDs.EXTRA_TASK_DATE_HOUR,9);
        mMinute=intent.getIntExtra(IntentIDs.EXTRA_TASK_DATE_MINUTE,3);
        GregorianCalendar calendar=new GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute);
        Date ExtraDate=calendar.getTime();
        this.date=ExtraDate;
    }
}
