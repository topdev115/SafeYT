package com.zaytoona.youtube.safe.gui.businessobjects.adapters;


import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;

import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;
import com.zaytoona.youtube.safe.R;

public class PlayListChildViewHolder extends CheckableChildViewHolder {

    private CheckedTextView childCheckedTextView;

    public PlayListChildViewHolder(View itemView) {
        super(itemView);
        childCheckedTextView = itemView.findViewById(R.id.list_item_multicheck_child_name);
    }

    @Override
    public Checkable getCheckable() {
        return childCheckedTextView;
    }

    public void setName(String name) {
        childCheckedTextView.setText(name);
    }
}