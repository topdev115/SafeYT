package com.zaytoona.youtube.safe.businessobjects.firebase;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class VideoRecommend {

    @Exclude
    public String recommendId;
    public String recommendedVideo;
    public String recommender;
    public String recommendingComment;
    public String date;

    public VideoRecommend() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public VideoRecommend(String recommendId, String recommendedVideo, String recommender, String recommendingComment, String date) {
        this.recommendId = recommendId;
        this.recommendedVideo = recommendedVideo;
        this.recommender = recommender;
        this.recommendingComment = recommendingComment;
        this.date = date;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> report = new HashMap<>();
        //report.put("recommendId", recommendId);
        report.put("recommendedVideo", recommendedVideo);
        report.put("recommender", recommender);
        report.put("recommendingComment", recommendingComment);
        report.put("date", date);

        return report;
    }

}