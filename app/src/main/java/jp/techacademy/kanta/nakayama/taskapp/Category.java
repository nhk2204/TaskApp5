package jp.techacademy.kanta.nakayama.taskapp;

import android.content.Intent;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by nhk2204 on 2016/10/09.
 */
public class Category extends RealmObject {
    private String categoryName;

    @PrimaryKey
    private int categoryId;

    public String getCategoryName(){
        return categoryName;
    }
    public void setCategoryName(String categoryName){
        this.categoryName=categoryName;
    }

    public int getCategoryId(){
        return categoryId;
    }
    public void setCategoryId(int categoryId){
        this.categoryId=categoryId;
    }

    public void setToIntent(Intent intent){
        intent.putExtra(IntentIDs.EXTRA_CATEGORY_ID,this.categoryId);
        intent.putExtra(IntentIDs.EXTRA_CATEGORY_NAME,this.categoryName);
    }

    public void getFromIntent(Intent intent){
        this.categoryId=intent.getIntExtra(IntentIDs.EXTRA_CATEGORY_ID,-1);
        this.categoryName=intent.getStringExtra(IntentIDs.EXTRA_CATEGORY_NAME);
    }
}
