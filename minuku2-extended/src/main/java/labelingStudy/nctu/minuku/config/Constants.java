/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku.config;

import java.util.Calendar;

import labelingStudy.nctu.minuku.BuildConfig;

/**
 * Created by shriti on 7/17/16.
 */
public class Constants {
    public static final String ONGOING_CHANNEL_NAME = "MC";
    public static final String SURVEY_CHANNEL_NAME="MC";
    static final Calendar c = Calendar.getInstance();
    public static final String ACTION_CONNECTIVITY_CHANGE = "CONNECTIVITY_CHANGE";
    public static String currentWork;
    public static final String CHECK_SERVICE_ACTION = "checkService";
    public static final long MILLISECONDS_PER_SECOND = 1000;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;
    public static final int MaxESM = 10;
    public static final int dairy_hour = 21;
    public static final int MinHour = 9;
    public static final int MaxHour = 22;
    public static final int remind_hour = 8;
    public static final long MILLISECONDS_PER_DAY = HOURS_PER_DAY *MINUTES_PER_HOUR*SECONDS_PER_MINUTE*MILLISECONDS_PER_SECOND;
    public static final long MILLISECONDS_PER_HOUR = MINUTES_PER_HOUR*SECONDS_PER_MINUTE*MILLISECONDS_PER_SECOND;
    public static final long MILLISECONDS_PER_MINUTE = SECONDS_PER_MINUTE*MILLISECONDS_PER_SECOND;
    public final static String DATE_FORMAT_NOW_Dash = "yyyy-MM-dd HH:mm:ss Z";
    public final static String DATE_FORMAT_NOW_SLASH = "yyyy/MM/dd HH:mm:ss Z";
    public final static String DATE_FORMAT_NOW_MINUTE_SLASH = "yyyy/MM/dd HH:mm";
    public static final String DATE_FORMAT_NOW_NO_ZONE_Slash = "yyyy/MM/dd HH:mm:ss";
    public static final String DATE_FORMAT_NOW_DAY_Slash = "yyyy/MM/dd";
    public static final String DATE_FORMAT_NOW_NO_ZONE = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_NOW_DAY = "yyyy-MM-dd";
    public static final String DATE_FORMAT_NOW_HOUR = "yyyy-MM-dd HH";
    public static final String DATE_FORMAT_NOW_HOUR_MIN = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_NOW_HOUR_MIN_AMPM = "yyyy-MM-dd hh:mm a";
    public static final String DATE_FORMAT_NOW_AMPM_HOUR_MIN = "yyyy-MM-dd a hh:mm";
    public static final String DATE_FORMAT_HOUR_MIN_SECOND = "HH:mm:ss";
    public static final String DATE_FORMAT_FOR_ID = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT_HOUR_MIN = "HH:mm";
    public static final String DATE_FORMAT_HOUR = "HH";
    public static final String DATE_FORMAT_MIN = "mm";
    public static final String DATE_FORMAT_AMPM_HOUR_MIN = "a hh:mm";
    public static final String DATE_FORMAT_HOUR_MIN_AMPM = "hh:mm a";
    public static final String DATE_FORMAT_Small_HOUR_MIN = "hh:mm";
    public static final String DATE_FORMAT_DATE_TEXT = "MMM dd";
    public static final String DATE_FORMAT_DATE_TEXT_HOUR_MIN = "MMM dd HH:mm";
    public static final String DATE_FORMAT_DATE_TEXT_HOUR_MIN_SEC = "MMM dd  HH:mm:ss";
    public static final int DATA_FORMAT_TYPE_NOW=0;
    public static final int DATA_FORMAT_TYPE_DAY=1;
    public static final int DATA_FORMAT_TYPE_HOUR=2;
    public static final String SURVEY_CHANNEL_ID = "Survey_id";

    public static final int multitask_following = 9;
    public static final String[] multitasking = {"家務或責任", "移動中", "工作或學習", "用餐", "娛樂、休閒活動或運動"
                                                , "跟家人、朋友、同學或同事聊天"};
    public static final int validation_following = 4;

    public static final String DELIMITER = ";;;";
    public static final String ACTIVITY_DELIMITER = ";;";
    public static final String CONTEXT_SOURCE_DELIMITER = ":";
    public static final String DELIMITER_IN_COLUMN = "::";

    public static final String YES = "YES";
    public static final String NO = "NO";

    public static final String ANNOTATION_TAG_DETECTED_TRANSPORTATION_ACTIVITY = "detected-transportation";
    public static final String ANNOTATION_TAG_DETECTED_SITENAME = "detected-sitename";
    public static final String ANNOTATION_TAG_DETECTED_SITELOCATION = "detected-sitelocation";
    //
    public static final String ANNOTATION_Label_SITELOCATION = "SiteLocation";
    public static final String ANNOTATION_Label_TIME = "LabeledTime";


    public static int Day = c.get(Calendar.DAY_OF_MONTH);
    public static int Year = c.get(Calendar.YEAR);
    public static int Month = c.get(Calendar.MONTH) + 1;

    //sharedPrefS
    public static final String sharedPrefString = "test";
    // for restart  answer activity
    public static final String LEN_PREFIX = "Count_";
    public static final String VAL_PREFIX = "IntValue_";

    public static final String everyDayNrecordString= "everyDayNrecord";
    public static final String everyDayMrecordString= "everyDayMrecord";


    //file path
    //public static final String PICTURE_DIRECTORY= "/Android/data/com.example.accessibility_detect/files/Pictures/News_Consumption/";
    public static final String PACKAGE_DIRECTORY_PATH= "/Android/data/com.example.accessibility_detect/files/NewsConsumption.nctu.minuku_2/";
    public static final String PICTURE_DIRECTORY_PATH = "/Pictures/News_Consumption/";
//    public static final String PICTURE_DIRECTORY_PATH = "/Android/data/com.example.accessibility_detect/files/";
    //public static final String PACKAGE_DIRECTORY = "/Android/data/NewsConsumption.nctu.minuku_2/";
    public static final String VIDEO_DIRECTORY_PATH = "/MC_Video/";

    public static final String ANNOTATION_TAG_DETECTED_TRANSPORTATOIN_ACTIVITY = "detected-transportation";
    public static final String ANNOTATION_TAG_SITENAME = "Sitename";
    public static final String ANNOTATION_TAG_Label = "Label";

    public static final String ANNOTATION_Label_TRANSPORTATOIN = "Transportation";
    public static final String ANNOTATION_Label_GOAL = "Goal";
    public static final String ANNOTATION_Label_SPECIALEVENT = "SpecialEvent";
    public static final String ANNOTATION_Label_SITENAME = "Sitename";


    // Firebase config
    public static final String FIREBASE_URL = BuildConfig.UNIQUE_FIREBASE_ROOT_URL;
    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/users";
    public static final String FIREBASE_URL_MOODS = FIREBASE_URL + "/moods";
    public static final String FIREBASE_URL_NOTES = FIREBASE_URL + "/notes";
    public static final String FIREBASE_URL_NOTIFICATIONS = FIREBASE_URL + "/notifications";
    public static final String FIREBASE_URL_IMAGES = FIREBASE_URL + "/photos";
    public static final String FIREBASE_URL_LOCATION = FIREBASE_URL + "/location";
    public static final String FIREBASE_URL_SEMANTIC_LOCATION = FIREBASE_URL + "/semantic_location";
    public static final String FIREBASE_URL_QUESTIONS = FIREBASE_URL + "/questions";
    public static final String FIREBASE_URL_MCQ = FIREBASE_URL_QUESTIONS + "/mcq";
    public static final String FIREBASE_URL_FREE_RESPONSE = FIREBASE_URL_QUESTIONS + "/freeresponse";
    public static final String FIREBASE_URL_USER_SUBMISSION_STATS = FIREBASE_URL + "/submissionstats";
    public static final String FIREBASE_URL_DIABETESLOG = FIREBASE_URL + "/diabetes_log";
    public static final String FIREBASE_URL_EOD_QUESTION_ANSWER = FIREBASE_URL + "/EOD_question_answer";
    public static final String FIREBASE_URL_TAG = FIREBASE_URL + "/tags";
    public static final String FIREBASE_URL_TAG_RECENT = FIREBASE_URL + "/recent_tags";
    public static final String FIREBASE_URL_TIMELINE_PATCH = FIREBASE_URL + "/eod_timeline_notes";
    public static final String FIREBASE_URL_MISSED_REPORT_PROMPT_QNA = FIREBASE_URL + "/missed_report_prompt_QnA";
    public static final String FIREBASE_URL_DIARYSCREENSHOT = FIREBASE_URL + "/diary_screenshot";


    public static final String STOP_RECORDING = "STOP_RECORDING";
    public static final String START_RECORDING = "START_RECORDING";
    public static final String STOP_RECORD = "STOP_RECORD";
    public static final String CANCEL_RECORD = "CANCEL_RECORD";
    public static final String SKIP_CONTRIBUTE = "SKIP_CONTRIBUTE";


    // Provider stuff
    public static final String GOOGLE_AUTH_PROVIDER = "google";
    public static final String PASSWORD_PROVIDER = "password";
    //public static final String PROVIDER_DATA_DISPLAY_NAME = "displayName";

    // Google provider hashkeys
    public static final String GGL_PROVIDER_USERNAME_KEY = "username";
    public static final String GGL_PROVIDER_EMAIL_KEY = "email";

    // Shared pref ids
    public static final String ID_SHAREDPREF_EMAIL = "email";
    public static final String ID_SHAREDPREF_PROVIDER = "provider";
    //public static final String ID_SHAREDPREF_DISPLAYNAME = "displayName";

    public static final String KEY_SIGNUP_EMAIL = "SIGNUP_EMAIL";
    public static final String KEY_ENCODED_EMAIL = "ENCODED_EMAIL";

    public static final String LOG_ERROR = "Error:";


    // Prompt service related constants
    public static final int PROMPT_SERVICE_REPEAT_MILLISECONDS = 1000 * 10; // 1000 * 60 = 1 minute
    //changing from 50 mins to 15 mins, users were getting it close to bedtime
    public static final int DIARY_NOTIFICATION_SERVICE_REPEAT_MILLISECONDS = 15 * 60 * 1000; //15 minutes

    public static final long PROMPT_SURVEY_ALARM_MILLISECONDS  = 1000*60*60; //每個小時檢查一次
    public static final long PROMPT_RESET_ALARM_MILLISECONDS  = 1000*60*60*3; //每個小時檢查一次
    public static final int INVALID_INT_VALUE = -1;
    public static final long INVALID_TIME_VALUE = -1;
    public static final String INVALID_STRING_VALUE = "NA";
    // Notification related constants
    public static final String CAN_SHOW_NOTIFICATION = "ENABLE_NOTIFICATIONS";

    public static final String MOOD_REMINDER_TITLE = "How are you feeling right now?";
    public static final String MOOD_REMINDER_MESSAGE = "Tap here to report your mood.";

    public static final String MOOD_ANNOTATION_TITLE = "Tell us more about your mood";
    public static final String MOOD_ANNOTATION_MESSAGE = "Tap here answer a quick question.";

    public static final String MISSED_ACTIVITY_DATA_PROMPT_TITLE = "We want to hear from you!";
    public static final String MISSED_ACTIVITY_DATA_PROMPT_MESSAGE = "Tap here to answer some questions.";

    public static final String EOD_DIARY_PROMPT_TITLE = "Diary entry";
    public static final String EOD_DIARY_PROMPT_MESSAGE = "Tap here to complete today's diary.";

    public static final int CONTEXT_SOURCE_INVALID_VALUE_INTEGER = -9999;
    public static final long CONTEXT_SOURCE_INVALID_VALUE_LONG_INTEGER = -9999;
    public static final long CONTEXT_SOURCE_INVALID_VALUE_FLOAT = -9999;
    public static final int SENSOR_QUEUE_SIZE = 20;

    public static final String ONGOING_CHANNEL_ID = "mobile_crowdsource_id";
    public static final String RECORDING_NOTIFICATION_ID = "Accessibility Service Channel";
    public static final String REMINDER_NOTIFICATION_ID = "REMINDER_NOTIFICATION_ID";
    public static final String QUESTIONNAIRE_CHANNEL_ID = "questionnaire_channel";
    public static final String RECORDING_TITLE_CONTENT = "若同意擷取你在螢幕上的內容，請按立即開始";
    public static final String RECORDING_ONGOING_CONTENT = "目前沒有執行截圖功能";
    public static final String RECORDING_STOP_CONTENT = "正在截圖中，可按下方按鈕停止截圖";
    public static final String RECORDING_TITLE = "News Consumption 偵測新聞相關程式中";
    public static final String RECORDING_NOW = "立即開始";
    public static final String STOPRECORDING = "停止截圖";
    public static final String CANCELRECORDING = "刪除通知";
    public static final String RECORDING_CONTENT = "" ;

    public static final String QUESTIONNAIRE_TITLE_CONTENT = "偵測到新聞相關活動";//請您填寫新的問卷
    public static final String QUESTIONNAIRE_TEXT = "請記得填寫問卷";//回答與情境相關的問題
    public static final String DIARY_TITLE_CONTENT = "請填寫今天的日誌";
    public static final String DIARY_TEXT = "謝謝您的合作";
    public static final String SCRRENSHOT_TITLE_CONTENT = "News_Consumption偵測到新聞相關程式開啟";//請您填寫新的問卷
    public static final String SCRRENSHOT_TEXT = "將開始擷取你的螢幕上顯示的內容";//回答與情境相關的問題

    public static final String QUESTIONNAIRE_TITLE_MC = "請填寫問卷 - 是否完成群眾外包工作";
    public static final String QUESTIONNAIRE_TITLE_RANDOM_NOTI = "請填寫問卷 - 是否看到通知";
    public static final String QUESTIONNAIRE_TITLE_MC_NOTI = "請填寫問卷 - 是否看過群眾外包通知";
    public static final String GOOGLE_LOCAL_GUIDES = "在地嚮導";
    public static final String GOOGLE_CROWDSOURCE_APP = "CROWDSOURCE";
    public static final String SKIP_CONTRIBUTE_ACTION = "略過貢獻";
    public static final String REMINDER_TITLE = "提醒您上傳今天之前的資料";
    public static final String REMINDER_TEXT = "上傳前請先檢查是否有不願公開的照片";

    //default queue size
    public static final int DEFAULT_QUEUE_SIZE = 50;

    //specific queue sizes
    public static final int LOCATION_QUEUE_SIZE = 50;
    public static final int IMAGE_QUEUE_SIZE = 20;
    public static final int MOOD_QUEUE_SIZE = 20;

    public static final String ACCESSIBILITY_STREAM_GENERATOR = "class labelingStudy.nctu.minuku.streamgenerator.AccessibilityStreamGenerator";
    public static final String TRANSPORTATION_STREAM_GENERATOR = "class labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator";
    public static final String ACTIVITY_STREAM_GENERATOR = "class labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator";
    public static final String CONNECTIVITY_STREAM_GENERATOR = "class labelingStudy.nctu.minuku.streamgenerator.ConnectivityStreamGenerator";
    public static final String APPUSAGE_STREAM_GENERATOR = "class labelingStudy.nctu.minuku.streamgenerator.AppUsageStreamGenerator";
    public static final String BATTERY_STREAM_GENERATOR = "class labelingStudy.nctu.minuku.streamgenerator.BatteryStreamGenerator";
    public static final int MOOD_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES = 15;
    public static final int IMAGE_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES = 30;
    public static final int FOOD_IMAGE_STREAM_GENERATOR_UPDATE_FREQUENCY_MINUTES = 180;

    public static final int MOOD_NOTIFICATION_EXPIRATION_TIME = 30 * 60 /* 30 minutes*/;
    //changing missed report notification expiry to 2 hours as users are missing
    public static final int MISSED_REPORT_NOTIFICATION_EXPIRATION_TIME =  2 * 60 * 60 /* 120 minutes*/;
    //changing diary notification expiry to 2 hours as users are missing it
    public static final int DIARY_NOTIFICATION_EXPIRATION_TIME = 2 * 60 * 60 /* 120 minutes*/;

    public static final String TAPPED_NOTIFICATION_ID_KEY = "TAPPED_NOTIFICATION_ID" ;
    public static final String SELECTED_LOCATIONS = "USERPREF_SELECTED_LOCATIONS";
    public static final String APP_FULL_NAME = "Mobile Crowdsource study";
    public static final String RUNNING_APP_DECLARATION = APP_FULL_NAME + " is running in the background";
    public static final long INTERNAL_LOCATION_UPDATE_FREQUENCY = 1 * 10 * 1000; // 1 * 300 * 1000
    public static final long INTERNAL_LOCATION_LOW_UPDATE_FREQUENCY = 1 * 60 * 1000; // 1 * 300 * 1000

    public static final float LOCATION_MINUMUM_DISPLACEMENT_UPDATE_THRESHOLD = 50 ;

    public static final String DIABETES_LOG_NOTIFICATION_SOURCE = "DIABETES_LOG";

    /* from NCTU */
    public static String current_timer_state_tag = "current"; //for getTag

    public static String current_timer_state = "home";

    public final static String DATE_FORMAT_NOW = "yyyy/MM/dd HH:mm:ss";//yyyy-MM-dd HH:mm:ss Z
    public final static String DATE_FORMAT_for_storing = "yyyy-MM-dd HH:mm:ss";

    public final static String home_tag = "home";
    public final static String timer_move_tag = "timer_move";

    public static boolean tabpos = false;

    public static String DEVICE_ID = "NA";
    public static String USER_ID = "N";
    public static String GROUP_NUM = "A";
    public static int TaskDayCount = -1;

    public static final int NOTIFICATION_UPDATE_THREAD_SIZE = 1;
    public static final int STREAM_UPDATE_FREQUENCY = 30; //30s
    public static final int ISALIVE_UPDATE_DELAY = 1;
    public static final int STREAM_UPDATE_DELAY = 0;
    public static final String ACTIVITY_CONFIDENCE_CONNECTOR = ":";
    public static final int ISALIVE_UPDATE_FREQUENCY = 1 * 60 * 60;
    public static final int MAIN_THREAD_SIZE = 5;

    // appName
    public static String FACEBOOK = "FACEBOOK";
    public static String MESSENGER = "MESSENGER";
    public static String LINE = "LINE";
    public static String GOOGLE_MAP = "GOOGLE MAP (地圖)";
    public static String GOOGLE_CROWDSOURCE = "GOOGLE CROWDSOURCE";
    public static String GMAIL = "GMAIL";
    public static String INSTAGRAM = "INSTAGRAM";
    public static String YOUTUBE = "YOUTUBE";
    public static String KKBOX = "KKBOX";
    public static String SLACK = "SLACK";
    public static String NON_TARGET_APP = "NON_TARGET_APP";

    public static String CONTACT_FILE = "contacts.txt";
    public static String ALLIMG = "AllImg.txt";
    public static String DEBUG_FILE = "files";

    public static String[] ChatRobot = {"機器人測試1號", "Lab Mui"};
    public static String[] NewsPack = {"com.udn.news", "com.yahoo.mobile.client.android.newstw", "com.totvnow.ttv", "com.set.newsapp",
            "tw.com.nexttv.tvnews", "cc.nexdoor.ct.activity", "com.news.ctsapp", "net.ettoday.ETstar", "net.ettoday.phone", "com.formosatv.ftvnews",
            "com.epochtimes.tw.android", "com.ebc.news", "com.tvbs.news", "com.sina.news", "com.nextmediatw", "com.now.newsapp",
            "bbc.mobile.news.ww", "com.cnn.mobile.android.phone", "tw.com.nextmedia.magazine", "tw.net.ebc.fncyoutube", "com.nownews", "com.nextmedia",
            "com.yahoo.mobile.client.android.superapp"};
    public static String[] eventText = {"udn News", "新聞", "台視新聞", "三立新聞網", "壹電視", "旺旺中時", "華視新聞", "星光雲",
            "新聞雲", "民視新聞", "台灣大紀元", "東森新聞", "TVBS新聞", "新浪新闻", "蘋果新聞網", "Now 新聞", "BBC News", "CNN",
            "台灣壹週刊", "東森財經新聞", "今日新聞", "蘋果動新聞","Yahoo奇摩"};
    public static String At = "@";
    public static String Detect = "偵測到您";
    public static String Enter = "進入";
    public static String Post = "發佈";
    public static String Content = "內容";
    public static String Noti = "的通知";
    public static String others = "其他";
    public static String Have = "曾於 ";
    public static String Pull_down_noti_shade = " 閱讀過";
    public static String Have_clicked = "當時曾點擊";
    // for sync server
    public static final String DATA_SAVED_BROADCAST = "data.sync";
    public static final String URL_SAVE_DUMP = "http://103.124.75.188:28017/dumpdata";
    public static final String URL_SAVE_NEWS = "http://103.124.75.188:28017/newsdata";
    public static final String URL_SAVE_ISALIVE = "http://103.124.75.188:28017/isalive";
    public static final String URL_SAVE_USER = "http://103.124.75.188:28017/user";
    public static final String URL_SAVE_CSV = "http://103.124.75.188:28017/savecsv/";
    public static final String URL_SAVE_TXT = "http://103.124.75.188:28017/savetxt/";
    public static final String URL_SAVE_IMG = "http://103.124.75.188:28017/upload_img/";
    public static final String URL_CHECK_IMG = "http://103.124.75.188:28017/check_img/";
    public static final String Gmail_account = "newsatnctu@gmail.com";
    //1 means data is synced and 0 means data is not synced
    public static final int DATA_SYNCED_WITH_SERVER = 1;
    public static final int DATA_NOT_SYNCED_WITH_SERVER = 0;
    public static final int MY_SOCKET_TIMEOUT_MS = 10000; //5分鐘 300000

//    public static int dumpDataNumber = 0;
//    public static int dumpDataResponse = 0;
    //copy right
    public static final String copy_right = "Copyright©2018-2019 NCTU-MUILAB,All rights reserved.";
}
