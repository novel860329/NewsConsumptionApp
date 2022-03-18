package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import labelingStudy.nctu.minukucore.model.DataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

@Entity(tableName = "AppTimesDataRecord")
public class AppTimesDataRecord implements DataRecord {
    @PrimaryKey(autoGenerate = true)
    public long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "FacebookOpenTimes")
    public int FacebookOpenTimes = 0;

    @ColumnInfo(name = "FacebookScreenTimes")
    public int FacebookScreenTimes = 0;

    @ColumnInfo(name = "MessengerURLTimes")
    public int MessengerURLTimes = 0;

    @ColumnInfo(name = "YoutubeOpenTimes")
    public int YoutubeOpenTimes = 0;

    @ColumnInfo(name = "YoutubeScreenTimes")
    public int YoutubeScreenTimes = 0;

    @ColumnInfo(name = "InstagramOpenTimes ")
    public int InstagramOpenTimes  = 0;

    @ColumnInfo(name = "InstagramScreenTimes")
    public int InstagramScreenTimes = 0;

    @ColumnInfo(name = "NewsappOpenTimes ")
    public int NewsappOpenTimes  = 0;

    @ColumnInfo(name = "NewsappScreenTimes")
    public int NewsappScreenTimes = 0;

    @ColumnInfo(name = "PPTtitleTimes")
    public int PPTtitleTimes = 0;

    @ColumnInfo(name = "LinetodayOpenTimes ")
    public int LinetodayOpenTimes = 0 ;

    @ColumnInfo(name = "LinetodayScreenTimes")
    public int LinetodayScreenTimes = 0;

    @ColumnInfo(name = "LineUrlTimes")
    public int LineUrlTimes = 0 ;

    @ColumnInfo(name = "GooglenowOpenTimes")
    public int GooglenowOpenTimes  = 0;

    @ColumnInfo(name = "GooglenowScreenTimes")
    public int GooglenowScreenTimes = 0;

    @ColumnInfo(name = "ChromeOpenTimes")
    public int ChromeOpenTimes = 0;

    @ColumnInfo(name = "ChromeScreenTimes")
    public int ChromeScreenTimes = 0;

    @ColumnInfo(name = "readable")
    public long readable;

    @ColumnInfo(name = "sycStatus")
    public Integer syncStatus;

    @ColumnInfo(name = "ReadNews")
    public boolean ReadNews;

    @ColumnInfo(name = "phone_sessionid")
    public Long phone_sessionid;
    @ColumnInfo(name = "screenshot")
    public String screenshot;

    @ColumnInfo(name = "ImageName")
    public String ImageName;

    @ColumnInfo(name = "sessionid")
    public String sessionid;

//    @ColumnInfo(name = "AccessibilityUrl")
//    public String AccessibilityUrl;
//
//    @ColumnInfo(name = "NotificationUrl")
//    public String NotificationUrl;

    public AppTimesDataRecord(int FacebookOpenTimes,int FacebookScreenTimes,int MessengerURLTimes,
                              int YoutubeOpenTimes,int YoutubeScreenTimes,int InstagramOpenTimes,
                              int InstagramScreenTimes,int NewsappOpenTimes,int NewsappScreenTimes,
                              int  PPTtitleTimes,int LinetodayOpenTimes,int LinetodayScreenTimes,
                              int LineUrlTimes, int GooglenowOpenTimes,
                              int GooglenowScreenTimes, int ChromeOpenTimes, int ChromeScreenTimes,
                              boolean ReadNews, long phone_sessionid, String sessionid, String screenshot, String ImageName) {
        this.creationTime = System.currentTimeMillis();
        this.FacebookOpenTimes = FacebookOpenTimes;
        this.FacebookScreenTimes = FacebookScreenTimes;
        this.MessengerURLTimes = MessengerURLTimes;
        this.YoutubeOpenTimes = YoutubeOpenTimes;
        this.YoutubeScreenTimes = YoutubeScreenTimes;
        this.InstagramOpenTimes = InstagramOpenTimes;
        this.InstagramScreenTimes = InstagramScreenTimes;
        this.NewsappOpenTimes = NewsappOpenTimes;
        this.NewsappScreenTimes = NewsappScreenTimes;
        this.PPTtitleTimes =  PPTtitleTimes;
        this.LinetodayOpenTimes = LinetodayOpenTimes;
        this.LinetodayScreenTimes = LinetodayScreenTimes;
        this.LineUrlTimes = LineUrlTimes;
        this.GooglenowOpenTimes = GooglenowOpenTimes;
        this.GooglenowScreenTimes = GooglenowScreenTimes;
        this.ChromeOpenTimes = ChromeOpenTimes;
        this.ChromeScreenTimes = ChromeScreenTimes;
        this.readable = getReadableTimeLong(this.creationTime);
        this.syncStatus = 0;
        this.ReadNews = ReadNews;
        this.phone_sessionid = phone_sessionid;
        this.screenshot = screenshot;
        this.ImageName = ImageName;
        this.sessionid = sessionid;
    }
    public long getCreationTime() {
        return this.creationTime ;
    }
    public int getFacebookOpenTimes(){return this.FacebookOpenTimes;}
    public int getFacebookScreenTimes(){return this.FacebookScreenTimes;}
    public int getMessengerURLTimes(){return this.MessengerURLTimes;}
    public int getYoutubeOpenTimes(){return this.YoutubeOpenTimes;}
    public int getYoutubeScreenTimes(){return this.YoutubeScreenTimes;}
    public int getInstagramOpenTimes(){return this.InstagramOpenTimes;}
    public int getInstagramScreenTimes(){return this.InstagramScreenTimes;}
    public int getNewsappOpenTimes(){return this.NewsappOpenTimes;}
    public int getNewsappScreenTimes(){return this.NewsappScreenTimes;}
    public int getPPTtitleTimes(){return this.PPTtitleTimes;}
    public int getLinetodayOpenTimes(){return this.LinetodayOpenTimes;}
    public int getLinetodayScreenTimes(){return this.LinetodayScreenTimes;}
    public int getLineUrlTimes(){return this.LineUrlTimes;}
    public int getGooglenowOpenTimes(){return this.GooglenowOpenTimes;}
    public int getGooglenowScreenTimes(){return this.GooglenowScreenTimes;}
    public int getChromeOpenTimes(){return this.ChromeOpenTimes;}
    public int getChromeScreenTimes(){return this.ChromeScreenTimes;}
    public boolean getReadNews() {
        return this.ReadNews;
    }

    public void setFacebookOpenTimes(int t){this.FacebookOpenTimes = t;}
    public void setFacebookScreenTimes(int t){ this.FacebookScreenTimes = t;}
    public void setMessengerURLTimes(int t){ this.MessengerURLTimes = t;}
    public void setYoutubeOpenTimes(int t){ this.YoutubeOpenTimes = t;}
    public void setYoutubeScreenTimes(int t){ this.YoutubeScreenTimes = t;}
    public void setInstagramOpenTimes(int t){ this.InstagramOpenTimes = t;}
    public void setInstagramScreenTimes(int t){ this.InstagramScreenTimes = t;}
    public void setNewsappOpenTimes(int t){ this.NewsappOpenTimes = t;}
    public void setNewsappScreenTimes(int t){ this.NewsappScreenTimes = t;}
    public void setPPTtitleTimes(int t){ this.PPTtitleTimes = t;}
    public void setLinetodayOpenTimes(int t){ this.LinetodayOpenTimes = t;}
    public void setLinetodayScreenTimes(int t){ this.LinetodayScreenTimes = t;}
    public void setLineUrlTimes(int t){ this.LineUrlTimes = t;}
    public void setGooglenowOpenTimes(int t){ this.GooglenowOpenTimes = t;}
    public void setGooglenowScreenTimes(int t){ this.GooglenowScreenTimes = t;}
    public void setChromeOpenTimes(int t){ this.ChromeOpenTimes = t;}
    public void setChromeScreenTimes(int t){ this.ChromeScreenTimes = t;}
    public long getReadable() {
        return this.readable  ;
    }
    public void setsyncStatus(Integer syncStatus){
        this.syncStatus = syncStatus;
    }
    public Integer getsyncStatus(){
        return this.syncStatus;
    }
}
