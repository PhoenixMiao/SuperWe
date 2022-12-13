package com.example.superwe;

import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class ConfigurationTableAdapter extends ArrayAdapter {

    private List<Configuration> list;
    private LayoutInflater inflater;

    public ConfigurationTableAdapter(Context context, List<Configuration> list){
        super(context,R.layout.configuration_item);
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

        Configuration configuration = (Configuration) this.getItem(position);

        ViewHolder viewHolder;

        if(convertView == null){

            viewHolder = new ViewHolder();

            convertView = inflater.inflate(R.layout.configuration_item, parent,false);
            viewHolder.configurationName = convertView.findViewById(R.id.configuration_name);
            viewHolder.configurationDescription = convertView.findViewById(R.id.configuration_description);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.configurationName.setText(configuration.getName());
        viewHolder.configurationName.setTextSize(13);
        viewHolder.configurationDescription.setText(configuration.getDescription());
        viewHolder.configurationDescription.setTextSize(13);

        return convertView;
    }

    public static class ViewHolder{
        public TextView configurationName;
        public TextView configurationDescription;
    }

}
