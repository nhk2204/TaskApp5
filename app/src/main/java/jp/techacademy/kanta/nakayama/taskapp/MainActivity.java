package jp.techacademy.kanta.nakayama.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    private Realm mRealm;
    private RealmResults<Task> mTaskRealmResults;
    private RealmResults<Category> mCategoryRealmResults;
    private RealmChangeListener mRealmListener=new RealmChangeListener(){
        @Override
        public void onChange() {
            reloadListView();
        }
    };

    private ListView mListView;
    private TaskAdapter mTaskAdapter;

    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //「絞込み」ボタンを押したときの動作
        Button searchButton=(Button)findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadSearchListView();
            }
        });

        //「戻る」ボタンを押したときの動作
        Button returnButton=(Button)findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadListView();
                //Spinnerの位置を先頭のものに戻す（先頭は必ず空欄が入る）。
                mSpinner.setSelection(0);
            }
        });

        //「+」ボタンを押したときの動作（タスクの追加）
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,InputActivity.class);
                startActivity(intent);
            }
        });

        //Realmの設定
        mRealm=Realm.getDefaultInstance();
        //Taskについて
        mTaskRealmResults=mRealm.where(Task.class).findAll();
        mTaskRealmResults.sort("date", Sort.DESCENDING);
        //同様にカテゴリも放り込む
        mCategoryRealmResults=mRealm.where(Category.class).findAll();
        mCategoryRealmResults.sort("categoryName",Sort.DESCENDING);
        mRealm.addChangeListener(mRealmListener);

        //ListViewの設定
        mTaskAdapter=new TaskAdapter(MainActivity.this);
        mListView=(ListView)findViewById(R.id.listView1);

        //Spinnerの設定
        mSpinner=(Spinner)findViewById(R.id.search_text);

        //ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //入力・編集する画面に推移させる。
                Task task=(Task)parent.getAdapter().getItem(position);

                Intent intent=new Intent(MainActivity.this,InputActivity.class);
                task.setToIntent(intent);
                startActivity(intent);
            }
        });

        //ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //タスクの削除処理
                final Task task=(Task)parent.getAdapter().getItem(position);

                //ダイアログを表示する
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle()+"を削除しますか？");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RealmResults<Task> results=mRealm.where(Task.class).equalTo("id",task.getId()).findAll();
                        mRealm.beginTransaction();
                        results.clear();
                        mRealm.commitTransaction();

                        Intent resultIntent=new Intent(getApplicationContext(),TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent=PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                        AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL",null);

                AlertDialog dialog=builder.create();
                dialog.show();

                return true;
            }
        });

        //動くかどうかの確認用
        if(mTaskRealmResults.size()==0){
            //addTestTasks();
        }
        reloadListView();
    }

    private void reloadListView(){
        ArrayList<Task> taskArrayList=new ArrayList<>();

        for(int i=0;i<mTaskRealmResults.size();i++){
            Task task=new Task();

            task.setId(mTaskRealmResults.get(i).getId());
            task.setTitle(mTaskRealmResults.get(i).getTitle());
            task.setContents(mTaskRealmResults.get(i).getContents());
            task.setCategory(mTaskRealmResults.get(i).getCategory());
            task.setDate(mTaskRealmResults.get(i).getDate());

            taskArrayList.add(task);
        }
        mTaskAdapter.setTaskArrayList(taskArrayList);
        mListView.setAdapter(mTaskAdapter);
        mTaskAdapter.notifyDataSetChanged();

        ArrayList<Category> categoryArrayList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Spinnerの先頭に空欄を入れる。
        //「戻る」で戻った場合にここを選ばせるため
        adapter.add("");
        //2段目に「カテゴリなしを検索」を設定。
        adapter.add("カテゴリなしを検索");
        for (int i = 0; i < mCategoryRealmResults.size(); i++) {
            Category category = new Category();

            category.setCategoryId(mCategoryRealmResults.get(i).getCategoryId());
            category.setCategoryName(mCategoryRealmResults.get(i).getCategoryName());

            adapter.add(category.getCategoryName());
        }
        mSpinner.setAdapter(adapter);
    }

    private void reloadSearchListView(){
        ArrayList<Task> searchTaskArrayList=new ArrayList<>();
        Spinner searchCategoryEditText=(Spinner)findViewById(R.id.search_text);
        if(searchCategoryEditText.getSelectedItem()!=null) {
            if(searchCategoryEditText.getSelectedItemPosition()!=1) {
                String searchCategory = searchCategoryEditText.getSelectedItem().toString();

                for (int i = 0; i < mTaskRealmResults.size(); i++) {
                    //Log.d("AAA","AAA");
                    Task B = mTaskRealmResults.get(i);
                    Category A = B.getCategory();
                    if (A != null) {
                        String compareCategory = A.getCategoryName();
                        if (compareCategory.equals(searchCategory)) {
                            Task task = new Task();

                            task.setId(mTaskRealmResults.get(i).getId());
                            task.setTitle(mTaskRealmResults.get(i).getTitle());
                            task.setContents(mTaskRealmResults.get(i).getContents());
                            task.setCategory(mTaskRealmResults.get(i).getCategory());
                            task.setDate(mTaskRealmResults.get(i).getDate());

                            searchTaskArrayList.add(task);
                        }
                    }
                }
            }else{
                //カテゴリなしを検索する。
                for (int i = 0; i < mTaskRealmResults.size(); i++) {
                    //Log.d("AAA","AAA");
                    Task B = mTaskRealmResults.get(i);
                    Category A = B.getCategory();
                    if (A == null) {
                        Task task = new Task();

                        task.setId(mTaskRealmResults.get(i).getId());
                        task.setTitle(mTaskRealmResults.get(i).getTitle());
                        task.setContents(mTaskRealmResults.get(i).getContents());
                        task.setCategory(mTaskRealmResults.get(i).getCategory());
                        task.setDate(mTaskRealmResults.get(i).getDate());

                        searchTaskArrayList.add(task);

                    }
                }
            }
            mTaskAdapter.setTaskArrayList(searchTaskArrayList);
            mListView.setAdapter(mTaskAdapter);
            mTaskAdapter.notifyDataSetChanged();
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        mRealm.close();
    }

    //テスト用のTaskを設定。
    protected void addTestTasks(){
        Category category=new Category();
        category.setCategoryId(0);
        category.setCategoryName("NAKAYAMA");

        Category category2=new Category();
        category2.setCategoryId(1);
        category2.setCategoryName("OSUMI");

        Task task=new Task();
        task.setTitle("HANAKO");
        task.setContents("NAKAYAMA");
        task.setDate(new Date());
        task.setCategory(category);
        task.setId(0);

        Task task2=new Task();
        task2.setTitle("KANTA");
        task2.setContents("NAKAYAMA");
        task2.setDate(new Date());
        task2.setCategory(category);
        task2.setId(1);

        Task task3=new Task();
        task3.setTitle("MIDORI");
        task3.setContents("OSUMI");
        task3.setDate(new Date());
        task3.setCategory(category2);
        task3.setId(2);

        Task task4=new Task();
        task4.setTitle("YASUHIKO");
        task4.setContents("NAKAYAMA");
        task4.setDate(new Date());
        task4.setCategory(category);
        task4.setId(3);

        Task task5=new Task();
        task5.setTitle("PONSHI");
        task5.setContents("(DOG)");
        task5.setDate(new Date());
        //task5.setCategory(category);
        task5.setId(4);

        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(task);
        mRealm.copyToRealmOrUpdate(task2);
        mRealm.copyToRealmOrUpdate(task3);
        mRealm.copyToRealmOrUpdate(task4);
        mRealm.copyToRealmOrUpdate(task5);
        mRealm.copyToRealmOrUpdate(category);
        mRealm.copyToRealmOrUpdate(category2);
        mRealm.commitTransaction();
    }
}