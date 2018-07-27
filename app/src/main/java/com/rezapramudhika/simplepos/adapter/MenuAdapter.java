package com.rezapramudhika.simplepos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rezapramudhika.simplepos.R;
import com.rezapramudhika.simplepos.helper.MoneyFormat;
import com.rezapramudhika.simplepos.model.Menu;

import java.util.List;

public class MenuAdapter extends BaseAdapter{

    private Context context;
    private int layout;
    private List<Menu> menuList;

    public MenuAdapter(Context context, int layout, List<Menu> menuList) {
        this.context = context;
        this.layout = layout;
        this.menuList = menuList;
    }

    @Override
    public int getCount() {
        return menuList.size();
    }

    @Override
    public Object getItem(int position) {
        return menuList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = new ViewHolder();

        if(row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout,null);
            holder.name = row.findViewById(R.id.txtMenuName);
            holder.price = row.findViewById(R.id.txtMenuPrice);
            row.setTag(holder);
        }else{
            holder = (ViewHolder) row.getTag();
        }

        Menu menu = menuList.get(position);
        holder.name.setText(menu.getName());
        holder.price.setText(MoneyFormat.idr(Double.valueOf(String.valueOf(menu.getPrice()))));

        return row;
    }

    private class ViewHolder{
        TextView name;
        TextView price;
    }
}
