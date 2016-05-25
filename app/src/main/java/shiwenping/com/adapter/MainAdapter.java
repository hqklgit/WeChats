package shiwenping.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import shiwenping.com.wechats.Page;
import shiwenping.com.wechats.R;

/**
 * Created by bilinshengshi on 16/5/24.
 */
/*
 * HaiChecker
 * 16/5/24
 *
 */

public class MainAdapter extends BaseAdapter {

    private List<Page> data;
    private LayoutInflater inflater;

    public MainAdapter(Context mContext,List<Page> data){
        inflater = LayoutInflater.from(mContext);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateData(List<Page> data){
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Hodler h = null;
        if (convertView == null){
            convertView = this.inflater.inflate(R.layout.main_item,null);
            h = new Hodler();
            h.group = (TextView) convertView.findViewById(R.id.group);
            h.name = (TextView) convertView.findViewById(R.id.name);
            h.number = (TextView) convertView.findViewById(R.id.number);
            convertView.setTag(h);
        }else{
            h = (Hodler) convertView.getTag();
        }
        h.name.setText(data.get(position).getName());
        h.number.setText(data.get(position).getNumber());
        h.group.setText(data.get(position).getWegroup());
        return convertView;
    }


    class Hodler{
        TextView name,group,number;
    }
}
