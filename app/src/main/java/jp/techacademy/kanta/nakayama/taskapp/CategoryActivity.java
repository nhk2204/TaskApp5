package jp.techacademy.kanta.nakayama.taskapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class CategoryActivity extends AppCompatActivity {
    //Realmの設定。
    private Realm mRealm;
    private RealmResults<Category> mCategoryRealmResults;
    private RealmChangeListener mRealmListener=new RealmChangeListener(){
        @Override
        public void onChange() {
            reloadListView();
        }
    };


    private Button button;
    private EditText editText;
    private Category mCategory;

    private Task intentTask;
    private Category intentCategory;

    private ListView mListView;
    private CategoryAdapter mCategoryAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        //ActionBarを設定する
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Realmの設定
        mRealm= Realm.getDefaultInstance();
        mCategoryRealmResults=mRealm.where(Category.class).findAll();
        mCategoryRealmResults.sort("categoryName", Sort.DESCENDING);
        mRealm.addChangeListener(mRealmListener);

        //ListViewの設定
        mCategoryAdapter=new CategoryAdapter(CategoryActivity.this);
        mListView=(ListView)findViewById(R.id.listView1);

        //Intent
        //前画面から引き継いだTaskをmTaskに、CategoryをmCategoryに代入
        //直接intentTask,intentCategory煮代入しようとするとエラーする。
        //意味不明。
        Intent intent=getIntent();
        Task task=new Task();
        task.getFromIntent(intent);
        intentTask=task;

        Category category=new Category();
        category.getFromIntent(intent);
        intentCategory=category;


        //新規作成ボタンとそれに使うEditTextを追加
        button=(Button)findViewById(R.id.search_button);
        editText=(EditText)findViewById(R.id.search_text);
        //ボタンに機能を追加
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newCategoryName=editText.getText().toString();
                Realm realm=Realm.getDefaultInstance();
                RealmResults<Category> categoryRealmResults=realm.where(Category.class).findAll();

                //追加可能かどうかを決めるboolean
                boolean MakeFlag;
                MakeFlag=true;

                if(newCategoryName.equals("")){
                    //new_category_editが空のときの処理。
                    //ダイアログを表示し入力を求める。
                    AlertDialog.Builder builder=new AlertDialog.Builder(CategoryActivity.this);
                    builder.setTitle("ERR");
                    builder.setMessage("カテゴリが入力されていません。");
                    builder.setNegativeButton("OK",null);
                    AlertDialog dialog=builder.create();
                    dialog.show();

                }else{
                    for(int i=0;i<categoryRealmResults.size();i++){
                        if(newCategoryName.equals(categoryRealmResults.get(i).getCategoryName())){
                            //newCategoryNameがすでに存在している場合の処理。
                            //ダイアログを表示しすでに存在していることを告げ処理を抜ける。
                            AlertDialog.Builder builder=new AlertDialog.Builder(CategoryActivity.this);
                            builder.setTitle("ERR");
                            builder.setMessage("そのカテゴリはすでに存在しています。");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            MakeFlag=false;
                            AlertDialog dialog=builder.create();
                            dialog.show();
                        }
                    }
                    if(MakeFlag) {
                        mCategory = new Category();
                        int identifier;
                        if (categoryRealmResults.max("categoryId") != null) {
                            identifier = categoryRealmResults.max("categoryId").intValue() + 1;
                        } else {
                            identifier = 0;
                        }
                        mCategory.setCategoryId(identifier);
                        mCategory.setCategoryName(newCategoryName);

                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(mCategory);
                        realm.commitTransaction();

                        reloadListView();
                    }
                    realm.close();
                }
            }
        });

        //ListViewをタップしたときの処理
        //(選択されたカテゴリをIntentしてInputActivityに戻る）
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category category=(Category)parent.getAdapter().getItem(position);

                Realm realm=Realm.getDefaultInstance();

                //この時点で新規作成しておく。
                if(intentTask.getId()==-1){
                    //新規作成の場合
                    RealmResults<Task> taskRealmResults=realm.where(Task.class).findAll();

                    int identifier;
                    if(taskRealmResults.max("id")!=null){
                        identifier=taskRealmResults.max("id").intValue()+1;
                    }else{
                        identifier=0;
                    }
                    intentTask.setId(identifier);
                }

                //Intent
                //前画面から引き継いだTaskをmTaskに、CategoryをmCategoryに代入
                Intent intent=new Intent(CategoryActivity.this,InputActivity.class);
                intentTask.setToIntent(intent);
                category.setToIntent(intent);

                startActivity(intent);

            }
        });

        //ListViewを長い押ししたときの処理
        //（categoryの削除）
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //Categoryの削除処理
                final Category category=(Category)parent.getAdapter().getItem(position);

                //ダイアログを作成し確認する。
                final AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
                builder.setTitle("削除");
                builder.setMessage(category.getCategoryName()+"を削除しますか？");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    //削除可能か否かを決めるboolean
                    boolean DelFlag=true;

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RealmResults<Task> taskResults=mRealm.where(Task.class).findAll();
                        int CompCateId;
                        for(int i=0;i<taskResults.size();i++){
                            CompCateId=taskResults.get(i).getCategory().getCategoryId();
                            if(CompCateId==category.getCategoryId()){
                                //new_category_editが空のときの処理。
                                //ダイアログを表示し入力を求める。
                                DelFlag =false;
                                AlertDialog.Builder builder=new AlertDialog.Builder(CategoryActivity.this);
                                builder.setTitle("ERR");
                                builder.setMessage("そのカテゴリはTaskにて使用されています。");
                                builder.setNegativeButton("OK",null);
                                AlertDialog dialogErr=builder.create();
                                dialogErr.show();
                            }
                        }

                        if(DelFlag==true) {
                            //Realm野中から所定のcategoryを削除。
                            RealmResults<Category> results = mRealm.where(Category.class).equalTo("categoryId", category.getCategoryId()).findAll();
                            mRealm.beginTransaction();
                            results.clear();
                            mRealm.commitTransaction();
                        }
                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL",null);
                AlertDialog dialog= builder.create();
                dialog.show();
                return true;
            }
        });

        reloadListView();
    }

    //画面の更新
    private void reloadListView(){
        ArrayList<Category> categoryArrayList=new ArrayList<>();

        for(int i=0;i<mCategoryRealmResults.size();i++){
            Category category=new Category();

            category.setCategoryId(mCategoryRealmResults.get(i).getCategoryId());
            category.setCategoryName(mCategoryRealmResults.get(i).getCategoryName());

            categoryArrayList.add(category);
        }
        mCategoryAdapter.setCategoryList(categoryArrayList);
        mListView.setAdapter(mCategoryAdapter);
        mCategoryAdapter.notifyDataSetChanged();
    }
}
