package com.ibuildapp.masterapp.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ibuildapp.masterapp.R;
import com.ibuildapp.masterapp.model.MyContact;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 01.12.14
 * Time: 15:08
 * To change this template use File | Settings | File Templates.
 */
//public class ContactAdapter extends ArrayAdapter<MyContact> implements Filterable {
public class ContactAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = ContactAdapter.class.getCanonicalName();
    public HashMap<String, MyContact> sourceContent;
    public List<MyContact> content;
    public LayoutInflater inflater;
    private ContactFilter filter;


    public ContactAdapter(Context context, List<MyContact> content) {
        //super(context, -1, content);
        Log.d(TAG, "CursorAdapter");
        this.content = content;
        inflater = LayoutInflater.from(context);

        sourceContent = new HashMap<String, MyContact>();
        for (int i = 0; i < content.size(); i++) {
            MyContact cont = content.get(i);
            sourceContent.put(cont.id, cont);
        }

        Collections.sort(this.content, new Comparator<MyContact>() {
            @Override
            public int compare(MyContact lhs, MyContact rhs) {
                return lhs.name.charAt(0) - rhs.name.charAt(0);
            }
        });
    }

    public List<MyContact> getCheckedList() {
        List<MyContact> resList = new ArrayList<MyContact>();
        for (MyContact c : sourceContent.values()) {
            if (c.isChecked())
                resList.add(c);
        }
        return resList;
    }

    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public Object getItem(int position) {
        return content.get(position);
    }

    @Override
    public long getItemId(int position) {
        return content.get(position).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = inflater.inflate(R.layout.masterapp_contact_item, viewGroup, false);

        TextView name = ViewHolder.get(view, R.id.name);
        TextView phone = ViewHolder.get(view, R.id.phone);
        CheckBox checkBox = ViewHolder.get(view, R.id.checkBox);


        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(content.get(i).isChecked());
        checkBox.setTag(content.get(i).id);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String key = (String) buttonView.getTag();
                sourceContent.get(key).setChecked(isChecked);
            }
        });
//        checkBox.setBackgroundResource(getItem(i).checked ? R.drawable.checkbox_checked : R.drawable.checkbox_add_contact);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox check = (CheckBox) v.findViewById(R.id.checkBox);
                check.setChecked(!check.isChecked());
            }
        });

        //
        name.setText(content.get(i).name);
        StringBuilder builder = new StringBuilder();
        for (String s : content.get(i).phones) {
            builder.append(s + " \n");
        }
        phone.setText(builder);

        return view;
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new ContactFilter();
        return filter;
    }

    static class ViewHolder {
        // I added a generic return type to reduce the casting noise in client code
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }
            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;
        }
    }

    private class ContactFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults fres = new FilterResults();
            if (TextUtils.isEmpty(constraint)) {
                fres.count = sourceContent.size();
                fres.values = new ArrayList<MyContact>(sourceContent.values());
            } else {
                String comparableStr = constraint.toString().toLowerCase();

                List<MyContact> filterRes = new ArrayList<MyContact>();
                for (MyContact c : sourceContent.values()) {
                    if (c.name.toLowerCase().contains(comparableStr))
                        filterRes.add(c);
                }

                fres.count = filterRes.size();
                fres.values = filterRes;
            }

            return fres;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Collections.sort((List<MyContact>) results.values, new Comparator<MyContact>() {
                @Override
                public int compare(MyContact lhs, MyContact rhs) {
                    return lhs.name.compareTo(rhs.name);
                }
            });

            //clear();

            ArrayList<MyContact> myContacts = new ArrayList<MyContact>((Collection<? extends MyContact>) results.values);
            content.clear();
            content.addAll(myContacts);
//            content.clear();
//            content.addAll((Collection<? extends MyContact>) );

            notifyDataSetChanged();
        }
    }
}
