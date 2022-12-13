package com.example.superwe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class FriendInformationTableAdapter extends ArrayAdapter {

    private List<FriendInformation> list;
    private LayoutInflater inflater;

    public FriendInformationTableAdapter(Context context, List<FriendInformation> list){
        super(context,R.layout.friend_information_item);
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        int ret = 0;
        if(list!=null){
            ret = list.size();
        }
        return ret;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FriendInformation friendInformation = (FriendInformation) this.getItem(position);

        ViewHolder viewHolder;

        if(convertView == null){

            viewHolder = new ViewHolder();

            convertView = inflater.inflate(R.layout.friend_information_item, parent,false);
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.nickname = convertView.findViewById(R.id.nickname);
            viewHolder.wx = convertView.findViewById(R.id.wx);
            viewHolder.position = convertView.findViewById(R.id._position);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(friendInformation.getName());
        viewHolder.name.setTextSize(13);
        viewHolder.nickname.setText(friendInformation.getNickname());
        viewHolder.nickname.setTextSize(13);
        viewHolder.wx.setText(friendInformation.getWx());
        viewHolder.wx.setTextSize(13);
        viewHolder.position.setText(friendInformation.getPosition());
        viewHolder.position.setTextSize(13);

        return convertView;
    }


    public static class ViewHolder{
        public TextView name;
        public TextView nickname;
        public TextView wx;
        public TextView position;
    }

}
