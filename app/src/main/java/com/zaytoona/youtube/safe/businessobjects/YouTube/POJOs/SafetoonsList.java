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

package com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.Thumbnail;
import com.zaytoona.youtube.safe.businessobjects.db.model.PrivateListInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A POJO class to store a YouTube Playlist.
 */
public class SafetoonsList implements Serializable {

	public static int SAFETOONS_LIST_TYPE_PLAY_LIST = 0;
	public static int SAFETOONS_LIST_TYPE_PRIVATE_LIST = 1;
	public static int SAFETOONS_LIST_TYPE_CHANNEL = 2;

	private String              id;
	private String              title;
	private String              category;
	private String              description;
	private DateTime            publishDate;
	private int                 videoCount = 0;
	private String              thumbnailUrl;
	private List<YouTubeVideo>  videos = new ArrayList<>();
	private int                 listType;

	public SafetoonsList(Playlist playlist, String category, int listType) {
		id = playlist.getId();

		this.listType = listType;

		if(playlist.getSnippet() != null) {
			title = playlist.getSnippet().getTitle();
			description = playlist.getSnippet().getDescription();
			publishDate = playlist.getSnippet().getPublishedAt();

			this.category = category;

			if(playlist.getSnippet().getThumbnails() != null) {
				Thumbnail thumbnail = playlist.getSnippet().getThumbnails().getHigh();
				if(thumbnail != null)
					thumbnailUrl = thumbnail.getUrl();
			}
		}

		if(playlist.getContentDetails() != null) {
			videoCount = playlist.getContentDetails().getItemCount().intValue();
		}
	}

	public SafetoonsList(PrivateListInfo privateListInfo, int listType) {

		id = privateListInfo.getPrivateListID();
		this.listType = listType;

		title = privateListInfo.getPrivateListID();

		if(privateListInfo.getPrivateListThumbNailUrl() != null)
			thumbnailUrl = privateListInfo.getPrivateListThumbNailUrl();

		//description = playlist.getSnippet().getDescription();

		//publishDate = privateListInfo.getPublishDate();

		videoCount = privateListInfo.getNoOfVideos();

		publishDate = privateListInfo.getUpdateDate();
		////////////////
		/*
		if(playlist.getSnippet() != null) {
			title = playlist.getSnippet().getTitle();
			description = playlist.getSnippet().getDescription();
			publishDate = playlist.getSnippet().getPublishedAt();

			if(playlist.getSnippet().getThumbnails() != null) {
				Thumbnail thumbnail = playlist.getSnippet().getThumbnails().getHigh();
				if(thumbnail != null)
					thumbnailUrl = thumbnail.getUrl();
			}
		}

		if(playlist.getContentDetails() != null) {
			videoCount = playlist.getContentDetails().getItemCount().intValue();
		}
		*/
	}


	public SafetoonsList(YouTubeChannel channel, int listType) {

		if(channel != null) {
			id = channel.getId();
			this.listType = listType;

			title = channel.getTitle();

			thumbnailUrl = channel.getThumbnailNormalUrl();
		}

		//description = playlist.getSnippet().getDescription();
		//publishDate = playlist.getSnippet().getPublishedAt();

		//videoCount = channel.getYouTubeVideos().size();
		////////////////
		/*
		if(playlist.getSnippet() != null) {
			title = playlist.getSnippet().getTitle();
			description = playlist.getSnippet().getDescription();
			publishDate = playlist.getSnippet().getPublishedAt();

			if(playlist.getSnippet().getThumbnails() != null) {
				Thumbnail thumbnail = playlist.getSnippet().getThumbnails().getHigh();
				if(thumbnail != null)
					thumbnailUrl = thumbnail.getUrl();
			}
		}

		if(playlist.getContentDetails() != null) {
			videoCount = playlist.getContentDetails().getItemCount().intValue();
		}
		*/
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public String getId() {
		return id;
	}

	public int getType() {
		return listType;
	}

	public String getTitle() {
		return title;
	}

	public String getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public int getVideoCount() {
		return videoCount;
	}

	public List<YouTubeVideo> getVideos() {
		return videos;
	}

	/**
	 * Gets the {@link #publishDate} as a pretty string.
	 */
	public String getPublishDatePretty() {
		return (publishDate != null)
						? new PrettyTimeEx().format(publishDate)
						: "???";
	}
}
