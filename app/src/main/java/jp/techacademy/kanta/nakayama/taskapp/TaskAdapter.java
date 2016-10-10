package jp.techacademy.kanta.nakayama.taskapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by nhk2204 on 2016/09/22.
 */
public class TaskAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private ArrayList<Task> mTaskArrayList;

    public TaskAdapter(Context context){
        mLayoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setTaskArrayList(ArrayList<Task> taskArrayList){
        mTaskArrayList=taskArrayList;
    }

    @Override
    public int getCount() {
        //アイテム（データ）の数を返す。
        return mTaskArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        //アイテム（データ）を返す。
        return mTaskArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        //アイテム（データ）のidを返す。
        return mTaskArrayList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Viewを返す。
        if(convertView==null){
            convertView=mLayoutInflater.inflate(android.R.layout.simple_list_item_2,null);
        }

        TextView textView1=(TextView)convertView.findViewById(android.R.id.text1);
        TextView textView2=(TextView)convertView.findViewById(android.R.id.text2);

        textView1.setText(mTaskArrayList.get(position).getTitle());

        SimpleDateFormat simpleDataFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE);
        Date date=mTaskArrayList.get(position).getDate();
        textView2.setText(simpleDataFormat.format(date));

        return convertView;
    }
}
