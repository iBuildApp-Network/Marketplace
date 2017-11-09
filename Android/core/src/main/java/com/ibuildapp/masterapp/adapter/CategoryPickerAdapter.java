package com.ibuildapp.masterapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ibuildapp.masterapp.R;
import com.ibuildapp.masterapp.model.CategoryEntity;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 21.07.14
 * Time: 15:13
 * To change this template use File | Settings | File Templates.
 */
public class CategoryPickerAdapter extends BaseAdapter {

    private Context context;
    private List<CategoryEntity> content;
    private LayoutInflater inflater;

    public CategoryPickerAdapter( Context context, List<CategoryEntity> content) {
        this.context = context;
        this.content = content;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public Object getItem(int i) {
        return content.get(i);
    }

    @Override
    public long getItemId(int i) {
        return content.get(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        if ( v == null )
            v = inflater.inflate(R.layout.masterapp_category_list_item, null);

        TextView text = (TextView) v.findViewById(R.id.category_list_name);
        text.setText(content.get(i).title);

        return v;
    }
}
