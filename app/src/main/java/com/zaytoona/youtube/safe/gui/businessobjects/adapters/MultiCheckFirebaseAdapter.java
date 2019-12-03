package com.zaytoona.youtube.safe.gui.businessobjects.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.firebase.MultiCheckCategory;
import com.zaytoona.youtube.safe.businessobjects.firebase.MultiCheckPlayList;
import com.zaytoona.youtube.safe.gui.businessobjects.MultiSelectListPreferenceItem;

import java.util.List;

public class MultiCheckFirebaseAdapter extends
        CheckableChildRecyclerViewAdapter<CategoryParentViewHolder, PlayListChildViewHolder> {

    private List<MultiCheckCategory> groups = null;

    public MultiCheckFirebaseAdapter(List<MultiCheckCategory> groups) {
        super(groups);

        this.groups = groups;
    }

    @Override
    public PlayListChildViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_multicheck_child, parent, false);
        return new PlayListChildViewHolder(view);
    }

    @Override
    public void onBindCheckChildViewHolder(PlayListChildViewHolder holder, int position,
                                           CheckedExpandableGroup group, int childIndex) {
        final MultiCheckPlayList child = (MultiCheckPlayList) group.getItems().get(childIndex);
        holder.setName(child.getName());
    }

    @Override
    public CategoryParentViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_multicheck_parent, parent, false);
        return new CategoryParentViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(CategoryParentViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {
        holder.setParentTitle(group);
    }

    /**
     * Select/Check (the tickbox) for all items in this adapter.
     */
    public void selectAll() {

        for(MultiCheckCategory group : groups) {

            for(int i = 0; i < group.selectedChildren.length; i++) {
                group.checkChild(i);
            }
        }

        notifyDataSetChanged();
    }


    /**
     * Deselect/Uncheck (the tickbox) for all items in this adapter.
     */
    public void selectNone() {
        for(MultiCheckCategory group : groups) {

            group.clearSelections();
        }

        notifyDataSetChanged();
    }


}
