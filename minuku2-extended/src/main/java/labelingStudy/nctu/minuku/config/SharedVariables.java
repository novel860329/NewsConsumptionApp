package labelingStudy.nctu.minuku.config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by chiaenchiang on 15/12/2018.
 */

public class SharedVariables {
    // communication between notification Listener Service  and Accessibility Service
    public static Integer nhandle_or_dismiss = -1;

    public static Boolean ifClickedNoti = false;
    public static String notiTitle= "NA";
    public static String notiSubText = "NA";
    public static String notiText = "NA";
    public static String notiTickerText = "NA";
    public static String notiPack = "NA";
    public static String notiReason = "NA";
    public static String notiTitleForRandom = "";
    public static String notiTextForRandom = "";
    public static String notiPackForRandom = "";
    public static Long notiPostedTimeForRandom = Long.valueOf(0);

    public static boolean recordNotificationChanging = false;

    public  static String extraForQ = "";
//    public static class nPostInfo {
//        public long postedTime = 0;
//        public Integer nPost = -1;
//        public void setPostedTime(long t){
//            this.postedTime = t;
//        }
//        public void setPostedNum(int num){
//            this.nPost = num;
//        }
//        public long getPostedTime(){
//            return this.postedTime;
//        }
//        public long getPostedNum(){
//            return this.nPost;
//        }
//
//    }
//
//
//    public static Boolean ifPreviousShown(long nowTime,int targetPackCode){
//
//        for(nPostInfo tmp : nPostArray){
//            // previous 1 minutes
//            if(tmp.postedTime<nowTime && tmp.postedTime>nowTime-( 60 * 1000)){
//                    if(targetPackCode == tmp.nPost){
//                        return true;
//                    }
//            }
//        }
//        return false;
//    }




//    public static void cleanPostArray(){
//        nPostArray.clear();
//    }
//    public static List<nPostInfo> nPostArray = new ArrayList<>();

    public static boolean upload_btn = true;
    // communication between Accessibility Service and FloatingButton Activities
    public static Boolean ifClickedFAB = false;
    public static Boolean ifUserStop = false;
    // communication between Background Recording Activity  and Others
    public static Boolean ifRecordingRightNow = false;

    // communitcation between recording Activity and others
    public static String videoFileName = Constants.DEVICE_ID;
    public static Integer videoCount = 0;
    public static Boolean haveDeletedRecordingNoti = false;
    public static Boolean stopRecordReceive =  false;

    // alarm and others
    //public static boolean canSentNoti = true;   //  隨機
   // public static boolean canSentNotiMC = true;  // 主動
    //public static boolean canSentNotiMCNoti = true; // 被動
    //public static boolean canSentReminder = true;

    public static String[] Trigger_list = {"Facebook", "LineToday", "NewsApp", "google News", "LineMes", "Youtube", "Instagram", "PTT", "Messenger", "Chrome"};
    public static HashMap<String, Boolean>Last_Agree = new HashMap<String, Boolean>();
    public static HashMap<String, Long>Last_Dialog_Time = new HashMap<String, Long>();

    public static ArrayList<String> pullcontent = new ArrayList<String>();
    public static boolean NSHasPulledDown = false;

    public static boolean CanFillEsm = false;
    public static boolean CanFillDiary = false;
    public static boolean FillDiary = false;
    public static String isFinish = "1";
    public static String isDFinish = "1";
    public static Long nowESM_time = 0L;
    public static Long response_time = 0L;
    public static long submitTime = 0L;

    public static int dumpData_response_number = 0;
    public static int dumpData_interval = 0;
    public static boolean dumpData_finish = false;

    public static final String RESET = "RESET";
    public static final String SERVICE_CHECKER = "service_checker";
    public static final String REMINDER = "reminder_alarm";
    public static final String ESM_ALARM = "esm_alarm";
    public static final String ESMTEST_ALARM = "esm_test";
    public static final String PHONE_STATE = "Phone_State";
    public static final String IS_ALIVE = "IsAlive";
    public static final String SCHEDULE_ALARM = "Schedule_Alarm";
    public static final String DAIRY_ALARM = "Diary Alarm";
    public static final String CLEAN_DIARYNOTI = "Diary Noti";
    public static final String RANDOMALARMASREMINDER = "RANDOMALARMASREMINDER";

    public static final String DELETENOTIALARM = "DELETENOTIALARM";
    public static final String SURVEYCREATEALARM = "SURVEYCREATEALARM";

    public static int FacebookOpenTimes = 0;
    public static int FacebookScreenTimes = 0;

    public static int MessengerURLTimes = 0;

    public static int YoutubeOpenTimes = 0;
    public static int YoutubeScreenTimes = 0;

    public static int InstagramOpenTimes = 0;
    public static int InstagramScreenTimes = 0;

    public static int NewsappOpenTimes = 0;
    public static int NewsappScreenTimes = 0;

    public static int PPTtitleTimes = 0;

    public static int LinetodayOpenTimes = 0;
    public static int LinetodayScreenTimes = 0;

    public static int LinemesOpenTimes = 0;
    public static int LinemesScreenTimes = 0;

    public static int GooglenowOpenTimes= 0;
    public static int GooglenowScreenTimes= 0;

    public static int reminderSize = 0;
    // AnswerActivity and others
    public static Integer todayMCount = 0;
    //public static Integer todayNCount = 0;
    public static Integer dayCount = 1;
    public static Integer allMCount = 0;
    public static int diary_requestID = 1000;
    //public static Integer allNCount = 0;
    public static String todayMCountString = "todayMCountString";
    public static String todayNCountString = "todayNCountString";
    public static String dayCountString = "dayCountString" ;
    public static String allMCountString ="allMCountString";
    public static String allNCountString = "allNCountString";
    public static Long lastAnswerTime  = new Date().getTime() ;

    public static boolean isAlarmReceiverFirst = true;
    public static boolean isAlarmReceiverFirstDiary = true;
    public static boolean EnterQ1_first = true;
    public static boolean BackToQ6 = false;
    public static LinkedHashMap<String, String> Q25Answer = new LinkedHashMap<String, String>();//Q24
    public static int Q24Answer;//Q23
    public static int Q7Answer;//Q7
//    public static int Q6Answer;//Q4
    public static String Q8Answer;
    public static LinkedHashMap<Integer, Integer> Q28Answer = new LinkedHashMap<>();//Q27

    public static int D15_number;
    public static List<String> D15Answer = new ArrayList<>();
    public static boolean D32_No = false;
    public static int D11_Answer = 0;
    public static LinkedHashMap<Integer, String> D32_Answer =  new LinkedHashMap<>();;

    public static boolean NoEsm = false;
    public static Long phone_session = 1L;
    public static int Random_session_num = 0;
    public static int Random_session_counter = 0;
    static public int []everyDayMrecord =  new int[50];
   // static public int []everyDayNrecord =  new int[50];


    // NetworkStateChecker
    public static Long startAppHour = Long.valueOf(0);





    //Accessiblity Service and Questionnaires
    public static String appNameForQ = "NA";
    public static String timeForQ = "";
    public static Integer questionaireType = -1;
    public static Boolean canFillQuestionnaire = false;
    // Accessiblity Service shared to others
    public static  String visitedApp = "NA";
    public static Integer relatedId = 0;

    public static Long startAnswerTimeLong = Long.valueOf(0);

   //questionnaire
    //public static ArrayList<Integer> pageRecord = new ArrayList();

   //noti and questionnaire
    public static String NotiInfoForQ="";
    // alarm
    public static Boolean resetFire = false;
    public static Boolean survey1Fire = false; //8
    public static Boolean survey2Fire = false;  //10
    public static Boolean survey3Fire = false;  //12
    public static Boolean survey4Fire = false;  //14
    public static Boolean survey5Fire = false;  //16
    public static Boolean survey6Fire = false;  //18
    public static Boolean survey7Fire = false;  //20
    public static Boolean survey8Fire = false;  //22
    public static Boolean survey9Fire = false;
    public static Boolean survey10Fire = false;
   // public static Boolean survey11Fire = false;

//    public static Boolean survey11Fire = false;  //for random



    // requestCodes and Notification ids
    public static int requestCodeCancelSurvey = 150;  //cancel 3 types of survey
    public static int requestCodeCancelReminder  = 151;
    public static int requestCodeCreateSurvey  = 152;
    public static int requestCodeRandomSurveyAlarm = 11;  //action : RANDOMSURVEYALARM
    public static int requestCodeRandomReminderAlarm = 21;
    public static int requestCodeSurveyNoti = 0;
    public static int requestCodeReminderActionlocalGuides = 126;
    public static int requestCodeReminderActioncrowdsource = 127;
    public static int requestCodeReminderActionskipContribution = 128;
    public static int requestCodeRecordActionStartRecord = 123;
    public static int requestCodeRecordActionRecording = 124;
    public static int requestCodeRecordActionStopRecording = 125;

    public static int NotiIdActiveSurvey = 100;
//    public static int NotiIdRandomSurvey = 101;
    public static int NotiIdRandomMCNotiSurvey = 102;
    public static int NotiIdRandomReminder = 103;
    public static int NotiIdRecord = 200;

    public static int answerid = 19;



    // shared functions
    public static String getReadableTime(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_for_storing);
        String currentTimeString = sdf_now.format(time);
        return currentTimeString;
    }

    // accessiblity
    public static final String map = "map(地圖)";
    public static final String crowdsource = "crowdsource";


    // NetworkStateChecker
    public static Long getReadableTimeLong(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_for_storing);
        String currentTimeString = sdf_now.format(time);
        String[] splited = currentTimeString.split("\\s+");
        String[]day = splited[0].split("-");
        Long year = Long.valueOf(day[0]);
        Long month = Long.valueOf(day[1]);
        Long dayLong = Long.valueOf(day[2]);

        String[]timeString = splited[1].split(":");
        Long hour = Long.valueOf(timeString[0]);
        Long minute = Long.valueOf(timeString[1]);
        Long second = Long.valueOf(timeString[2]);
        Long finalTime = year*1000000+month*10000+dayLong*100+hour;

        return finalTime;
    }

    public static String dateToStamp(String time) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_for_storing);
        Date date = simpleDateFormat.parse(time);
        long ts = date.getTime();
        return String.valueOf(ts);
    }
    // fragments

//    public static int MapAnswerPositiontoId(int questionid,int pos){
//        int[]questionSize = {0,2,1,4,11,19,4,1,9,13,  4,11,19,4,1,9,    2,2,5,6,1,4,11,19,4,1,9};
//        int answerId = 1;
//        for(int i=1;i<questionid ;i++){
//
//            answerId+= questionSize[i];
////            System.out.println(questionSize[i]);
//        }
//        answerId+=pos;
//        if(questionid>=10){
//            answerId+=35;// offset
//        }
//        if(questionid>=16){
//            answerId+=51;
//        }
//
//
//        return answerId;
//    }
    public static Long ReadableTimeAddHour(long time, int add){
        Long resultTime;

        int Year = (int)time/1000000;
        time = time % 1000000;

        int Month = (int)time/10000;
        time = time % 10000;

        int Day = (int)time/100;
        time = time % 100;

        int Hour = (int)time;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Year);
        cal.set(Calendar.MONTH, Month - 1);
        cal.set(Calendar.DAY_OF_MONTH, Day);
        cal.set(Calendar.HOUR_OF_DAY, Hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        cal.add(Calendar.HOUR_OF_DAY, add);

        resultTime = getReadableTimeLong(cal.getTimeInMillis());

        return resultTime;
    }
    // for random notification as reminders for mc tasks
    // if get mc notification(as a reminder)before
    public static boolean ifGetMCNotiBeforeR = false;
    // last checked time for notifications as a reminder
    public static long lastCheckTimeR =  Long.valueOf(0);




}
