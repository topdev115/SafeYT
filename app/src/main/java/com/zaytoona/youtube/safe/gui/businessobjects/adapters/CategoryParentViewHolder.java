package com.zaytoona.youtube.safe.gui.businessobjects.adapters;


import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.firebase.MultiCheckCategory;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class CategoryParentViewHolder extends GroupViewHolder {

    private TextView parentName;
    private ImageView arrow;
    private ImageView icon;

    public CategoryParentViewHolder(View itemView) {
        super(itemView);
        parentName = (TextView) itemView.findViewById(R.id.list_item_parent_name);
        arrow = (ImageView) itemView.findViewById(R.id.list_item_parent_arrow);
        icon = (ImageView) itemView.findViewById(R.id.list_item_parent_icon);
    }

    public void setParentTitle(ExpandableGroup parent) {

        if (parent instanceof MultiCheckCategory) {
            parentName.setText(parent.getTitle());
            icon.setBackgroundResource(((MultiCheckCategory) parent).getIconResId());
        }
    }

    @Override
    public void expand() {
        animateExpand();
    }

    @Override
    public void collapse() {
        animateCollapse();
    }

    private void animateExpand() {
        RotateAnimation rotate =
                new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.setAnimation(rotate);
    }

    private void animateCollapse() {
        RotateAnimation rotate =
                new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.setAnimation(rotate);
    }
}
