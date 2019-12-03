package com.zaytoona.youtube.safe.businessobjects.firebase;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class VideoReport {

    @Exclude
    public String reportId;
    public String reportedVideo;
    public String reporter;
    public String reportingReason;
    public String date;

    public VideoReport() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public VideoReport(String reportId, String reportedVideo, String reporter, String reportingReason, String date) {
        this.reportId = reportId;
        this.reportedVideo = reportedVideo;
        this.reporter = reporter;
        this.reportingReason = reportingReason;
        this.date = date;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> report = new HashMap<>();
        //report.put("reportId", reportId);
        report.put("reportedVideo", reportedVideo);
        report.put("reporter", reporter);
        report.put("reportingReason", reportingReason);
        report.put("date", date);

        return report;
    }

}