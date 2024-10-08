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

package com.zaytoona.youtube.safe.gui.businessobjects.adapters;

import android.content.Context;

import java.util.Collections;

import com.zaytoona.youtube.safe.businessobjects.interfaces.OrderableDatabase;

/**
 * Subclass of VideoGridAdapter that supports drag & drop reordering of the items in the grid.
 */
public class OrderableVideoGridAdapter extends VideoGridAdapter implements ItemTouchHelperAdapter {
	private OrderableDatabase database = null;

	public OrderableVideoGridAdapter(Context context, OrderableDatabase database, boolean displayMode) {
		super(context);
		this.database = database;
		this.displayMode = displayMode;
	}

	@Override
	public boolean onItemMove(int fromPosition, int toPosition) {
		if (fromPosition < toPosition) {
			for (int i = fromPosition; i < toPosition; i++) {
				Collections.swap(list, i, i + 1);
			}
		} else {
			for (int i = fromPosition; i > toPosition; i--) {
				Collections.swap(list, i, i - 1);
			}
		}
		notifyItemMoved(fromPosition, toPosition);

		if(database != null)
			database.updateOrder(list);

		return true;
	}
}
