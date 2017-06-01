package com.ruutdrop.ruutdropapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ruutdrop.ruutdropapp.adapters.BoxesListAdapter;
import com.ruutdrop.ruutdropapp.R;

import org.json.JSONArray;

/**
 * created by Aman on 12/12/16.
 */
public class BoxesFragment extends Fragment{

    private JSONArray tags;
    private Context context;
    private ListView boxesList;

    public static BoxesFragment newInstance(JSONArray tags, Context context) {
        BoxesFragment fragment = new BoxesFragment();
        fragment.tags = tags;
        fragment.context=context;
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boxes, null);
        boxesList=(ListView) view.findViewById(R.id.boxesList);
        boxesList.setAdapter(new BoxesListAdapter(getActivity(),this.tags));
        return view;
    }
}
