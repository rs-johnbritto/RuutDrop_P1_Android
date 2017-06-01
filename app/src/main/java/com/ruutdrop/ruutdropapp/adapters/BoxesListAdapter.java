package com.ruutdrop.ruutdropapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ruutdrop.ruutdropapp.R;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * created by Aman on 12/12/16.
 */
public class BoxesListAdapter  extends BaseAdapter {
    private static LayoutInflater inflater=null;
    private JSONArray items;

    private Context context;
    public BoxesListAdapter(Context context, JSONArray items) {
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
        this.context = context;

    }
    public int getCount() {
        return items.length();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }


    public static class ViewHolder{

        public TextView tag;

    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;

        if (convertView == null) {

            vi = inflater.inflate(R.layout.row_tag, null);


            holder = new ViewHolder();
            holder.tag = (TextView) vi.findViewById(R.id.boxtag);

            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        try {
            String tagName = items.getJSONObject(position).optString("tag");

            holder.tag.setText(tagName);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vi;
    }


}
