package com.zaytoona.youtube.safe.businessobjects.db.model;

import com.google.api.client.util.DateTime;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.PrettyTimeEx;
import com.zaytoona.youtube.safe.businessobjects.db.PrivateListsTable;


public class PrivateListInfo {

    private String privateListID;
    private int privateListShow;
    private String privateListThumbNailUrl;
    private int noOfVideos;
    private DateTime createDate;
    private DateTime updateDate;

    /**
     * The video update date in pretty format (e.g. "17 hours ago").
     */
    private transient String updateDatePretty;

    /**
     * The time when the updateDatePretty was calculated.
     */
    private transient long updateDatePrettyCalculationTime;

    /** updateDate will remain valid for 1 hour. */
    private final static long UPDATE_DATE_VALIDITY_TIME = 60 * 60 * 1000L;

    public PrivateListInfo(String privateListID, int privateListShow, String privateListThumbNailUrl, int noOfVideos, DateTime createDate, DateTime updateDate) {
        this.privateListID = privateListID;
        this.privateListShow = privateListShow;
        this.privateListThumbNailUrl = privateListThumbNailUrl;
        this.noOfVideos = noOfVideos;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    public String getPrivateListID() {
        return privateListID;
    }

    public void setPrivateListID(String privateListID) {
        this.privateListID = privateListID;
    }

    public int getPrivateListShow() {
        return privateListShow;
    }

    public void setPrivateListShow(int privateListShow) {
        this.privateListShow = privateListShow;
    }

    public String getPrivateListThumbNailUrl() {
        return privateListThumbNailUrl;
    }

    public void setPrivateListThumbNailUrl(String privateListThumbNailUrl) {
        this.privateListThumbNailUrl = privateListThumbNailUrl;
    }

    public int getNoOfVideos() {
        return noOfVideos;
    }

    public void setNoOfVideos(int noOfVideos) {
        this.noOfVideos = noOfVideos;
    }

    public DateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(DateTime createDate) {
        this.createDate = createDate;
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    /**
     * Sets the updateDate and updateDatePretty.
     */
    /*
    private void setUpdateDate(DateTime updatehDate) {
        this.updateDate = updatehDate;
        this.updateDatePretty = null;
    }
*/
    /**
     * Gets the {@link #updateDate} as a pretty string.
     */
    /*
    public String getUpdateDatePretty() {
        long now = System.currentTimeMillis();
        // if pretty is not yet calculated, or the update date was generated more than (1 hour) UPDATE_DATE_VALIDITY_TIME ago...
        if (updateDatePretty == null || (UPDATE_DATE_VALIDITY_TIME < now - updateDatePrettyCalculationTime)) {
            this.updateDatePretty = (updateDate != null) ? new PrettyTimeEx().format(updateDate) : "???";
            this.updateDatePrettyCalculationTime = now;
        }
        return updateDatePretty;
    }
*/
    /**
     * Given that {@link #updateDatePretty} is being cached once generated, this method will allow
     * you to regenerate and reset the {@link #updateDatePretty}.
     */
    /*
    public void forceRefreshUpdateDatePretty() {
        // Will force the updateDatePretty to be regenerated.  Refer to getUpdateDatePretty()
        this.updateDatePretty = null;
    }
*/
}
