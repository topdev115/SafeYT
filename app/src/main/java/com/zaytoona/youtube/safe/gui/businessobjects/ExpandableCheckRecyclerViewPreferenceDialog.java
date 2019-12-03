/*
 * Safetoons
 * Copyright (C) 2018  Ramon Mifsud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zaytoona.youtube.safe.gui.businessobjects;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.firebase.MultiCheckCategory;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.MultiCheckFirebaseAdapter;

import java.util.List;

/**
 * A dialog builder that displays categorized lists of items, allows to user to select multiple items and can
 * select/deselect all items.
 */
public class ExpandableCheckRecyclerViewPreferenceDialog extends SafetoonsMaterialDialog {

	private MultiCheckFirebaseAdapter listAdapter = null;

	public ExpandableCheckRecyclerViewPreferenceDialog(@NonNull Context context) {
		super(context);

		// set the custom view to be placed inside this dialog
		customView(R.layout.public_play_lists_import_dialog, true);
	}


	public ExpandableCheckRecyclerViewPreferenceDialog(@NonNull Context context, List<MultiCheckCategory> categories) {
		this(context);
		setCategories(categories);
	}

	/**
	 * Set Categories\Items to be displayed in this dialog.
	 *
	 * @param categories A list of items to be displayed.
	 */
	public void setCategories(List<MultiCheckCategory> categories) {
		listAdapter = new MultiCheckFirebaseAdapter(categories);
	}


	@Override
	public MaterialDialog build() {
		MaterialDialog materialDialog = super.build();

		RecyclerView recycler = materialDialog.getCustomView().findViewById(R.id.recycler_multi_check_expandable);

		recycler.setLayoutManager(new LinearLayoutManager(context));

		recycler.setAdapter(listAdapter);
		recycler.setLayoutManager(new LinearLayoutManager(materialDialog.getContext()));

		Button selectAllButton = materialDialog.getCustomView().findViewById(R.id.select_all_button);
		selectAllButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				listAdapter.selectAll();
			}
		});
		Button selectNoneButton = materialDialog.getCustomView().findViewById(R.id.select_none_button);
		selectNoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				listAdapter.selectNone();
			}
		});


		return materialDialog;
	}
}
