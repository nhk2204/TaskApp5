package jp.techacademy.kanta.nakayama.taskapp;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by nhk2204 on 2016/09/25.
 */
public class TaskApp extends Application{
    @Override
    public void onCreate(){
        super.onCreate();
        RealmConfiguration realmConfiguration=new RealmConfiguration.Builder(this).build();
        Realm.deleteRealm(realmConfiguration);//コンフィグを削除
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}
