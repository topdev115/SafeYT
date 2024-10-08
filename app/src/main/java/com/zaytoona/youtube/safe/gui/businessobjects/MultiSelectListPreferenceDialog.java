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

import java.util.List;
import java.util.Set;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.MultiSelectListPreferenceAdapter;

/**
 * A dialog builder that displays a lists items, allows to user to select multiple items and can
 * select/deselect all items.
 */
public class MultiSelectListPreferenceDialog extends SafetoonsMaterialDialog {

	private MultiSelectListPreferenceAdapter listAdapter;


	public MultiSelectListPreferenceDialog(@NonNull Context context) {
		super(context);

		// set the custom view to be placed inside this dialog
		customView(R.layout.subs_youtube_import_dialog_list, true);
	}


	public MultiSelectListPreferenceDialog(@NonNull Context context, List<MultiSelectListPreferenceItem> items) {
		this(context);
		setItems(items);
	}


	/**
	 * Set items to be displayed in this dialog.
	 *
	 * @param items A list of items to be displayed.
	 */
	public void setItems(List<MultiSelectListPreferenceItem> items) {
		listAdapter = new MultiSelectListPreferenceAdapter(items);
	}


	/**
	 * Add an item to this adapter.  Will fail if the item has already been added.
	 *
	 * @param item  Item to add.
	 *
	 * @return True if successful; false if the item is already stored in this adapter.
	 */
	public boolean addItem(MultiSelectListPreferenceItem item) {
		return listAdapter.addItem(item);
	}


	@Override
	public MaterialDialog build() {
		MaterialDialog materialDialog = super.build();

		RecyclerView list = materialDialog.getCustomView().findViewById(R.id.channel_list);
		list.setAdapter(listAdapter);
		list.setLayoutManager(new LinearLayoutManager(materialDialog.getContext()));

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


	/**
	 * @return A set of items that are selected/checked by the user.
	 */
	public List<MultiSelectListPreferenceItem> getSelectedItems() {
		return listAdapter.getSelectedItems();
	}


	/**
	 * @return A set of items that are selected/checked by the user.
	 */
	public Set<String> getSelectedItemsIds() {
		return listAdapter.getSelectedItemsIds();
	}

}
