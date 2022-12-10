package com.example.superwe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class FriendGroupTableAdapter extends ArrayAdapter {

    private List<FriendGroup> list;
    private LayoutInflater inflater;

    public FriendGroupTableAdapter(Context context, List<FriendGroup> list){
        super(context,R.layout.friend_list_item);
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

        FriendGroup friendGroup = (FriendGroup) this.getItem(position);

        ViewHolder viewHolder;

        if(convertView == null){

            viewHolder = new ViewHolder();

            convertView = inflater.inflate(R.layout.friend_list_item, parent,false);
            viewHolder.groupName = convertView.findViewById(R.id.friend_group_name);
            viewHolder.friendList = convertView.findViewById(R.id.friend_list);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.groupName.setText(friendGroup.getGroupName());
        viewHolder.groupName.setTextSize(13);
        viewHolder.friendList.setText(friendGroup.getFriendList());
        viewHolder.friendList.setTextSize(13);

        return convertView;
    }

    public static class ViewHolder{
        public TextView groupName;
        public TextView friendList;
    }

}

