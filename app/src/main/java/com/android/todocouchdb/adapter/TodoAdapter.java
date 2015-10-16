package com.android.todocouchdb.adapter;

/**
 * Created by akshaymehta on 16/10/15.
 */

import android.app.*;
import android.os.*;
import android.widget.*;

import java.util.*;

import android.graphics.*;
import android.view.*;
import android.content.*;

import com.android.todocouchdb.R;
import com.couchbase.lite.Document;

public class TodoAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Document> documentList;

    public TodoAdapter(Context context, ArrayList<Document> documentList) {
        this.context = context;
        this.documentList = documentList;
    }

    public int getCount() {
        return this.documentList.size();
    }

    public Object getItem(int position) {
        return this.documentList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public int getIndexOf(String id) {
        for (int i = 0; i < this.documentList.size(); i++) {
            if (this.documentList.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TextView todoTitle = null;
        Document currentDocument;
        try {
            if (convertView == null) {
                todoTitle = new TextView(this.context);
                todoTitle.setPadding(25, 25, 25, 25);
            } else {
                todoTitle = (TextView) convertView;
            }
            currentDocument = this.documentList.get(position);
            todoTitle.setText(String.valueOf(currentDocument.getProperty("todo")));
            todoTitle.setTextColor(Color.BLACK);
            todoTitle.setBackgroundColor(Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return todoTitle;
    }

}