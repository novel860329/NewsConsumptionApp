package labelingStudy.nctu.minuku.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.MobileCrowdsourceDataRecord;
import labelingStudy.nctu.minuku.streamgenerator.AccessibilityStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.MobileCrowdsourceStreamGenerator;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;

import static labelingStudy.nctu.minuku.config.Constants.Have;
import static labelingStudy.nctu.minuku.config.Constants.Have_clicked;
import static labelingStudy.nctu.minuku.config.Constants.Noti;
import static labelingStudy.nctu.minuku.config.Constants.Pull_down_noti_shade;
import static labelingStudy.nctu.minuku.config.Constants.QUESTIONNAIRE_TITLE_MC;
import static labelingStudy.nctu.minuku.config.Constants.STOP_RECORDING;
import static labelingStudy.nctu.minuku.config.SharedVariables.NSHasPulledDown;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdActiveSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRandomMCNotiSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRandomReminder;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRecord;
import static labelingStudy.nctu.minuku.config.SharedVariables.crowdsource;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.haveDeletedRecordingNoti;
import static labelingStudy.nctu.minuku.config.SharedVariables.ifClickedFAB;
import static labelingStudy.nctu.minuku.config.SharedVariables.ifClickedNoti;
import static labelingStudy.nctu.minuku.config.SharedVariables.ifRecordingRightNow;
import static labelingStudy.nctu.minuku.config.SharedVariables.ifUserStop;
import static labelingStudy.nctu.minuku.config.SharedVariables.map;
import static labelingStudy.nctu.minuku.config.SharedVariables.nhandle_or_dismiss;
import static labelingStudy.nctu.minuku.config.SharedVariables.pullcontent;
import static labelingStudy.nctu.minuku.config.SharedVariables.recordNotificationChanging;
import static labelingStudy.nctu.minuku.config.SharedVariables.relatedId;
import static labelingStudy.nctu.minuku.config.SharedVariables.visitedApp;
import static labelingStudy.nctu.minuku.service.MobileCrowdsourceRecognitionService.clearBlackList;
import static labelingStudy.nctu.minuku.service.MobileCrowdsourceRecognitionService.ifScrollDownNShadePack;
import static labelingStudy.nctu.minuku.service.MobileCrowdsourceRecognitionService.ifScrollDownNotificationShade;
import static labelingStudy.nctu.minuku.service.MobileCrowdsourceRecognitionService.ifTextContainClickedPack;
import static labelingStudy.nctu.minuku.service.MobileCrowdsourceRecognitionService.matchAppName;


/**
 * Created by chiaenchiang on 08/03/2018.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MobileAccessibilityService extends AccessibilityService {

    private final String TAG = "MobileAccessibilityServ";
    private final String TAG2 = "checkMCRecord";
    private final String TAG3 = "newAccess";


    private static AccessibilityStreamGenerator accessibilityStreamGenerator;
    private static MobileCrowdsourceStreamGenerator mobileCrowdsourceStreamGenerator;
    private static NotificationListenService notificationListenService = new NotificationListenService();
    public static boolean  enterFlag = false;

    public Boolean countDownisRunning = false;


    Intent intent;  // for closing floating button

    public MobileAccessibilityService() {

        super();

    }
    static long leave_app_time = 0;
    static long enter_app_time = 0;

    ArrayList<String> allcontent = new ArrayList<String>();

    ArrayList<String> userActions = new ArrayList<String>();
    String finalcontent ;
    String finalaction ;
    ArrayList<String> CheckIfDuplicate = new ArrayList<String>();

    //static List<Integer> appStack = new ArrayList<Integer>();
   // static boolean isAboveThreshold = false;

    boolean app_from_noti = false;
    // for notification shade pull down
    boolean possibleScrollDownPack = false;
    boolean NSPullingDown = false;
    Boolean probablySeen = false;
    String scrollDownTime = " " ;
    int countDown;


    static boolean is_mobile_crowdsource_task = false;
    boolean if_possible_mc = false;
    String lastTimeContent = "";
    String lastTimeContentAction ="";
    int packCode = -1;

    @SuppressLint({"NewApi", "LongLogTag"})
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void logViewHierarchy(AccessibilityNodeInfo nodeInfo, final int depth) {

        if (nodeInfo == null) return;

        String spacerString = "";
        for (int i = 0; i < depth; ++i) {
            spacerString += '-';
        }
        String str = "";
        //Log the info you care about here... I choce classname and view resource name, because they are simple, but interesting.
        if ((nodeInfo.getClassName() != null) && (nodeInfo.getText() != null) ) {
            Log.d("TAG", spacerString +" "+ nodeInfo.getText());

            str = spacerString +" "+ nodeInfo.getText().toString();

            if(!CheckIfDuplicate.contains(str)){
                    CheckIfDuplicate.add(str);
                    //showToast(str);
                    Log.d("check duplicate string to add : ",str);
                    str = clearBlackList(str);
//                    if(visitedApp.equals(crowdsource)){
//                        str = clearBlackList(str);
//                    }
//                    else {
//                        // map or others should follow strict rules  strictkeywords
//                        for (String tmp : strictKeyWords) {
//                            if (str.contains(tmp)) {
//                                str = clearBlackList(str);
//                                Log.d(TAG, "str all content:" + tmp);
//                            }
//                        }
//                    }
                  allcontent.add(str);
            }

            if(!if_possible_mc)
                if_possible_mc = MobileCrowdsourceRecognitionService.enterLocalGuideOrGCrowdsource(this,str,packCode);
          //  str = str.toLowerCase();

           // deleteRepeated(str);

        }
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            logViewHierarchy(nodeInfo.getChild(i), depth + 1);

        }
    }
    public void logViewHierarchyForNotiShade(AccessibilityNodeInfo nodeInfo, final int depth) {
        if (nodeInfo == null) return;
        String pack = "";
        String text = "";

        if(nodeInfo.getPackageName()!=null){
            //Log.d(TAG3,"pack "+nodeInfo.getPackageName().toString());
            pack = nodeInfo.getPackageName().toString();
        }
        if(!ifScrollDownNShadePack(pack)){
            possibleScrollDownPack = false;   //   for storing record
            //Log.d(TAG3,"notScrolldown pack");
            NSPullingDown = false;   // for ?????????????????? pulling down
            return;
        }else{
            possibleScrollDownPack = true;
            scrollDownTime = getReadableTime(System.currentTimeMillis());
        }

        if(nodeInfo.getText() != null){
            text = nodeInfo.getText().toString();
            //Log.d(TAG3,"text "+nodeInfo.getText().toString());
        }


        // allcontent for database, pullcontent for checking

        if(!pullcontent.contains(text)&& text.length()!=0) {
            pullcontent.add(text);
            allcontent.add(text);
           // CSVHelper.storeToCSV("checkpull.csv",pullcontent.toString());
        }
//        if(allcontent!=null)
//            Log.d(TAG3,"all : "+allcontent.toString());
//        if(pullcontent!=null)
//            Log.d(TAG3,"pull : "+pullcontent.toString());
        if(ifScrollDownNotificationShade(text)){
            NSHasPulledDown = true ;   //??????????????????notification ???????????? for non mobile crowdsource
            NSPullingDown = true;   // for mobile crowdsource  ????????????pulling down notification shade ???????????????
        }


//        String spacerString = "";
//        for (int i = 0; i < depth; ++i) {
//            spacerString += '-';
//        }
//        String str = "";
//        //Log the info you care about here... I choce classname and view resource name, because they are simple, but interesting.
//        if ((nodeInfo.getClassName() != null) && (nodeInfo.getText() != null) ) {
//            Log.d(TAG3, spacerString +" "+ nodeInfo.getText());
//            str = spacerString + " " +nodeInfo.getClassName() +" -- "+ nodeInfo.getText();
//        }


            for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
//                if (i == nodeInfo.getChildCount() - 1) {
//                    Log.d(TAG3, "----Last One ------------------------------");
//                }
                logViewHierarchyForNotiShade(nodeInfo.getChild(i), depth + 1);
            }


    }








    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onServiceConnected() {
       // Log.d("BootCompleteReceiver", "Mobile Accessibility config success!");
        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        accessibilityServiceInfo.notificationTimeout = 5000;
        setServiceInfo(accessibilityServiceInfo);

    }

    public MobileAccessibilityService(AccessibilityStreamGenerator accessibilityStreamGenerator) {

        super();
        /*try {
            this.accessibilityStreamGenerator = (AccessibilityStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(AccessibilityDataRecord.class);


        } catch (StreamNotFoundException e) {
            this.accessibilityStreamGenerator = accessibilityStreamGenerator;

        }*/

    }



    @SuppressLint({"NewApi", "LongLogTag"})
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void enterMobileCrowdsourceApp(int packCode){
        Log.d("enterLeave","enterMobileCrowdsourceApp");

        if (enter_app_time == 0) {
            clearAllData();
            Log.d("enterLeave","enterMobileCrowdsourceAppFirst");
            if (packCode == 4) visitedApp = map;
            else if (packCode == 5) visitedApp = crowdsource;


            // checkiffromnotificaiton

//            if (ifClickedNoti && nhandle_or_dismiss==packCode) {
//                // isNotiMobileCrowdsource = pref.getBoolean("is_noti_mobile_crowdsource",false);
//                //showToast("is_noti_mobile_crowdsource : "+isNotiMobileCrowdsource);
//              //  storeToNotirecord();
//
//            }

            //showToast("app_from_noti : "+app_from_noti);
            enter_app_time = System.currentTimeMillis() ;
            Log.d(TAG2,"enter time : "+enter_app_time);
//            if(!reminded_flag)
//                triggerNotificationsForReminding();
           // runforOnce = true;
            // rep.startRepeatingTask();
//            if(!ifRecordingRightNow) {
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
//                    intent = new Intent(this, FloatingActionButtonService.class);
//                    Log.d("FloatingActionButtonService", "in access to check floating");
//                    intent.putExtra("floating_appear", true);
//                    startService(intent);
//                    //TODO start notification
//
//                }
//            }
            enterFlag = true;


        }
        if((if_possible_mc|| packCode==5) && haveDeletedRecordingNoti==false){

//            if(!notificationListenService.checkRecordingNotiExist(this)){
//                notificationListenService.startRecordingNotification(this, NotiIdRecord);
//            }
//            if(ifRecordingRightNow && (notificationListenService.ifRecordingTransit(this,RECORDING_TITLE_CONTENT)
//                    || notificationListenService.ifRecordingTransit(this,RECORDING_STOP_CONTENT) ) ){
//                notificationListenService.updateRecordingNotification(this, NotiIdRecord);
//                Log.d(TAG,"recording : ");
//            }
//            if(((!ifRecordingRightNow||stopRecordReceive) && notificationListenService.ifRecordingTransit(this,RECORDING_ONGOING_CONTENT))){
//                notificationListenService.stopRecordingNotification(this, NotiIdRecord);
//
//            }

        }

        AccessibilityNodeInfo source = getRootInActiveWindow();
        if(source!=null){
            logViewHierarchy(source,0);
        }



    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void leaveMobileCrowdsourceApp(){
        Log.d("enterLeave","leaveMobileCrowdsourceApp");
        /* check Connection*/
        int pack;
        if((visitedApp.equals( map))){
            pack = 4;
        }else if((visitedApp.equals(crowdsource))){
            pack = 5;
        }else{
            pack = -1;
        }
         /* check Connection*/

        if ((visitedApp.equals(map)) || (visitedApp.equals(crowdsource))) {
           // Boolean prepare_to_record = (!(ifRecordingRightNow));  //&&(ifClickedFAB)

            //  ???????????????

             if(if_possible_mc){
                 Log.d("enterLeave","leaveMobileCrowdsourceApp___IsMC");
                 Log.d(TAG2, "is mc true");
                 if (leave_app_time == 0)
                     leave_app_time = System.currentTimeMillis() ;
                 storeMCrecord(visitedApp, app_from_noti, enter_app_time, leave_app_time);
             }
            if( if_possible_mc) {  //canSentNotiMC &&
                // ???????????????priority ??????
                if(!notificationListenService.checkAnyNotiExist(this, NotiIdRandomMCNotiSurvey) || !notificationListenService.checkAnyNotiExist(this,NotiIdRandomReminder)){
                    notificationListenService.createNotification(this,visitedApp, enter_app_time, 0, NotiIdActiveSurvey, QUESTIONNAIRE_TITLE_MC);
                    // kill random  and sent
                    //NotificationListenService.cancelNotification(this,NotiIdRandomMCNotiSurvey);
                    //NotificationListenService.cancelNotification(this,NotiIdRandomSurvey);
                }
//                else {
//                   notificationListenService.setAfterMinutesAlarm(this,"",15,requestCodeCancelSurvey, DELETENOTIALARM,NotiIdRandomMCNotiSurvey);
//                }

                // triggerNotifications(visitedApp,enter_app_time , 0);
                //canSentNotiMC = false;
            }

//                if(intent!=null)
//                    stopService(intent);
            //relatedId++;
            clearAllData();


            //




//            if(is_mobile_crowdsource_task ){ // ?????????MC tasks ????????????????????????????????? ??? ????????????
//
//                if(ifRecordingRightNow || ifUserStop) {    // ???????????????????????? ?????? ????????????
//                    Log.d("enterLeave","leaveMobileCrowdsourceApp___IsMC");
//                    Log.d(TAG2, "is mc true");
//                    if (leave_app_time == 0)
//                        leave_app_time = System.currentTimeMillis() ;
//                    storeMCrecord(visitedApp, app_from_noti, enter_app_time, leave_app_time);
//
//                    if(canSentNoti) {
//                        triggerNotifications(visitedApp,enter_app_time , 0);
//                        canSentNoti = false;
//                    }
//                    relatedId++;
//                    if(ifRecordingRightNow) {
//                        if(intent!=null)
//                            stopService(intent);
//                        stopRecording();
//                    }
//
//                    clearAllData();
//                }else{
//
//                    Log.d(TAG,"user starts to record or forget to record");  //
//                    if(!ifClickedFAB){  // user forget to record and leave
//                        if(canSentNoti) {
//                            triggerNotifications(visitedApp,enter_app_time , 0);
//                            canSentNoti = false;
//                        }
//                        relatedId++;
//                        clearAllData();
//                        if(intent!=null)
//                            stopService(intent);
//                    }else{
//                        // user start to record
//                    }
//                    //???????????????????????????
//
//                }
//
//            }
//            else{  //???????????? mobilecrowdsource  ????????????????????? ???????????????app
//                Log.d("enterLeave","leaveMobileCrowdsourceApp___notMC");
//                // TODO ????????????bug
//                // ?????????????????????????????????
//                Log.d(TAG2,"is mc false");
//
//                if(ifRecordingRightNow) {
//                    if(intent!=null)
//                        stopService(intent);
//                    stopRecording();
//                    if(canSentNoti) {
//                        triggerNotifications(visitedApp,enter_app_time , 0);
//                        canSentNoti = false;
//                    }
//                    relatedId++;
//                }else{
//
//                    Log.d(TAG,"user starts to record or forget to record");  //
//                    if(!ifClickedFAB){  // user forget to record and leave
//                        clearAllData();
//                        if(intent!=null)
//                            stopService(intent);
//                        if(canSentNoti) {
//                            triggerNotifications(visitedApp,enter_app_time , 0);
//                            canSentNoti = false;
//                        }
//                        relatedId++;
//                    }else{
//                        // ???????????????
//                    }
//                    //??????????????????
//                }
//                clearAllData();
//            }

        }
        if(ifRecordingRightNow){
            stopRecording();
        }
        notificationListenService.cancelRecordNotiPendingIntent(this);
        notificationListenService.cancelNotification(this,NotiIdRecord);
    }
    public void clearAllData(){
        // ????????????
        enter_app_time = 0;
        leave_app_time = 0;
        CheckIfDuplicate.clear();
        is_mobile_crowdsource_task = false;
        userActions.clear();
        visitedApp = "";
        enterFlag = false;
        app_from_noti = false;
        ifUserStop = false;
        ifClickedFAB = false;
        ifClickedNoti = false;
        if_possible_mc = false;
        haveDeletedRecordingNoti = false;

    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void enterOtherTargetApp(int packCode){
        String pack = "";
        String text = "";
        String type = "";
        String extra = "";
        //checkConnection("enterOtherTargetApp",packCode);

        Log.d("checkNonMobile","enterOtherTargetApp : "+packCode);
        if (packCode == 3 ||packCode == 10 ||packCode == 11||packCode == 12||packCode == 14||packCode == 15||packCode == 1) {  // target other kinds of app 10 gmail 11 instagram
            // ??????????????????????????????????????????app ????????????????????????

//            int todayTotal = todayNCount + todayMCount;
            Log.d("checkNonMobile","enterOtherTargetApp : enterflag false");
            //Log.d("checkNonMobile","can sent noti : "+ canSentNotiMC);
            Long now = new Date().getTime();
            String extraInfo = " ";
            String which = "None";
          //  Log.d(TAG3,"canSentNoti : "+canSentNoti);
//            if(canSentNoti) {
            if((ifClickedNoti && (nhandle_or_dismiss==packCode))){
                Log.d("checkNonMobile", "enterOtherTargetApp : triggernoti");
                extraInfo = Have_clicked +matchAppName(packCode) + Noti;
                which = "1";
               // triggerNotifications(matchAppName(packCode), now, 1,extraInfo );
            }else if(probablySeen){
                extraInfo = Have + scrollDownTime + Pull_down_noti_shade + matchAppName(packCode) + Noti; // ?????? ... ????????? ...??????
              //  triggerNotifications(matchAppName(packCode), now, 1, extraInfo);
                which = "2";
            }
            probablySeen = false;
   //         relatedId++;
//                canSentNoti = false;
//            }
            ifClickedNoti = false;
            nhandle_or_dismiss = -1;
            pack = matchAppName(packCode);
            text = extraInfo;
            type = "enterOtherTarge???tApp - "+which;
            extra = "enterTime : "+getReadableTime(now) ;
            storeAccessRecord(pack,text,type,extra);

        }
    }


    @SuppressLint({"NewApi", "LongLogTag"})
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (MobileCrowdsourceRecognitionService.ifScreenLight(pm)) {
//            KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//            if (MobileCrowdsourceRecognitionService.ifScreenLock(myKM)) {
//                // not available possible seen
//                source = getRootInActiveWindow();
//                if (source != null) {
//
//                    logViewHierarchy(source, 0);
//
//                }else{
//                    Log.d(TAG,"null");
//                }
//
//            }

//            SharedPreferences pref = getSharedPreferences("edu.nctu.minuku", MODE_PRIVATE);
//            pref.edit()
//                    .putLong("state_accessibility", System.currentTimeMillis() / 1000L)
//                    .apply();
//            pref.edit()
//                    .putBoolean("ifUserInteract", true)
//                    .apply();
//            if(getWindowTitleFromEvent(accessibilityEvent)!=null)
//                Log.d(TAG3,getWindowTitleFromEvent(accessibilityEvent).toString());
            // TODO Auto-generated method stub
            int eventType = accessibilityEvent.getEventType();
            String pack = "";
            String text = "";
            String type = "";
            String extra = "";
            //long time = -1;
            switch (eventType) {
                case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                    type = "TYPE_VIEW_FOCUSED";
                    Log.d("type", "TYPE_VIEW_FOCUSED");
                    break;
                case AccessibilityEvent.TYPE_VIEW_CLICKED:
                    type = "TYPE_VIEW_CLICKED";
                    Log.d("type", "TYPE_VIEW_CLICKED");
                    //  Log.d(TAG,type);
                    break;
                case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
                    type = "TYPE_VIEW_CONTEXT_CLICKED";
                    break;
                case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                    type = "TYPE_VIEW_LONG_CLICKED";
                    //    Log.d(TAG,type);
                    break;
                case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                    type = "TYPE_VIEW_SCROLLED";
                    //     Log.d(TAG,type);
                    break;
                case AccessibilityEvent.TYPE_VIEW_SELECTED:
                    type = "TYPE_VIEW_SELECTED";
                    //    Log.d(TAG,type);
                    break;
                case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                    type = "TYPE_VIEW_TEXT_CHANGED";
                    //    Log.d(TAG,type);
                    break;
                case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                    type = "TYPE_NOTIFICATION_STATE_CHANGED";
                    Log.d("nhandle_packCode_ifClicked"," TYPE_NOTIFICATION_STATE_CHANGED" + accessibilityEvent.getPackageName().toString());
                    Log.d("nhandle_packCode_ifClicked"," TYPE_NOTIFICATION_STATE_CHANGED"+ accessibilityEvent.getText().toString());
                    break;
//                    case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
//                        type = "TYPE_WINDOWS_CHANGED";
//                        Log.d(TAG3,"TYPE_WINDOWS_CHANGEDTYPE_WINDOWS_CHANGED");
//
//                        List<AccessibilityWindowInfo> windows = getWindows();
//                        if(windows!=null){
//                            Log.d(TAG3,"not null");
//                        }else{
//                            Log.d(TAG3,"null");
//                        }
//                        if(windows.get(0)!=null)
//                            Log.d(TAG3,"0 :"+windows.get(0).getTitle().toString());
//                        else{
//                            Log.d(TAG3,"0 : NULL");
//                        }
//                        ArrayList<AccessibilityWindowInfo> systemWindows = new ArrayList<>();
//                        ArrayList<AccessibilityWindowInfo> applicationWindows = new ArrayList<>();
//                        ArrayList<AccessibilityWindowInfo> accessibilityOverlayWindows = new ArrayList<>();
//                        Log.d(TAG3,"size:"+windows.size());
//                        for (int i = 0; i < windows.size(); i++) {
//                            AccessibilityWindowInfo window = windows.get(i);
//                            switch (window.getType()) {
//                                case AccessibilityWindowInfo.TYPE_APPLICATION:
//                                    Log.d(TAG3,"TYPE_APPLICATION");
//                                    if (window.getParent() == null) {
//                                        applicationWindows.add(window);
//                                    }
//                                    break;
//                                case AccessibilityWindowInfo.TYPE_SYSTEM:
//                                    Log.d(TAG3,"TYPE_SYSTEM");
//                                    systemWindows.add(window);
//                                    break;
//                                case AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY:
//                                    Log.d(TAG3,"TYPE_ACCESSIBILITY_OVERLAY");
//                                    accessibilityOverlayWindows.add(window);
//                                    break;
//                                default: // fall out
//                            }
//                        }
//                        Log.d(TAG3,"app size : "+applicationWindows.size());
//                        Log.d(TAG3,"system size : "+systemWindows.size());
//                        Log.d(TAG3,"overlay size : "+accessibilityOverlayWindows.size());
//                        if (applicationWindows.size() == 0) {
//                            if (systemWindows.size() > 0) {
//                                if(systemWindows.get(0).getTitle()!=null)
//                                    Log.d(TAG3,systemWindows.get(0).getTitle().toString());
//                            }
//                        }

//                        break;
//            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                type = "TYPE_WINDOW_STATE_CHANGED";
//                Log.d(TAG,type);
//                break;
//            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
//                type = "TYPE_WINDOW_STATE_CHANGED";
//                Log.d(TAG,type);
//                break;
//            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
//                type = "TYPE_WINDOW_STATE_CHANGED";
//                Log.d(TAG,type);
//                break;
//            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
//                type = "TYPE_WINDOW_CONTENT_CHANGED";
//                break;
//            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
//                eventText = "TYPE_WINDOWS_CHANGED";
//                Log.d(TAG,type);
//                break;
//            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
//                type="TYPE_TOUCH_EXPLORATION_GESTURE_START";
//                Log.d(TAG,type);
//                break;
//            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
//                type="TYPE_TOUCH_EXPLORATION_GESTURE_END";
//                Log.d(TAG,type);
//                break;
//            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
//                type="TYPE_TOUCH_INTERACTION_END";
//                Log.d(TAG,type);
//                break;
//            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
//                type="TYPE_TOUCH_INTERACTION_END";
//                Log.d(TAG,type);
//                break;
//
//            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
//                type="TYPE_TOUCH_INTERACTION_END";
//                Log.d(TAG,type);
//                break;
//            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
//                type="TYPE_TOUCH_INTERACTION_START";
//                Log.d(TAG,type);
//                break;
//

//            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
//                type="TYPE_VIEW_HOVER_ENTER";
//                Log.d(TAG,type);
//                break;
//            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
//                type="TYPE_VIEW_HOVER_EXIT";
//                Log.d(TAG,type);
//                break;

            }

            if (accessibilityEvent.getPackageName() != null) {
                pack = accessibilityEvent.getPackageName().toString();
                // Log.d(TAG,"pack : "+ pack);
            }
            String currentApp = "";
            currentApp =  getForegroundTask();
            if (accessibilityEvent.getClassName() != null) {
                text = accessibilityEvent.getClassName().toString();
                Log.d(TAG,"class : "+ text);
            }
            if (accessibilityEvent.getText() != null) {
                text += ":" + accessibilityEvent.getText().toString();
                //TODO testing the attribute.
                  Log.d(TAG,"text : "+ text);
            }
            if (accessibilityEvent.getContentDescription() != null) {
                extra = accessibilityEvent.getContentDescription().toString();
                // Log.d(TAG,"extra : "+ extra);
            }


            //Log.d(TAG,"getRecentActivity : "+currentApp2);
            if (!MobileCrowdsourceRecognitionService.ifunwantedPack(pack)&&!MobileCrowdsourceRecognitionService.ifunwantedPack(currentApp)&&!type.equals("TYPE_NOTIFICATION_STATE_CHANGED")) { //filter out ????????????pack ??? ??????????????????

                // Log.d(TAG, "eventType: "+ eventType);
                //time = getCurrentTimeInMillis();

                //  Log.d(TAG,"event : " + accessibilityEvent.toString());



                if (text != null) {
                    if ((text.matches(".*\\w.*") &&  type != "") || (extra.toString().contains("New task displayed")) || (extra.toString().contains("??????????????????"))) { //TODO ??????
                        if(!possibleScrollDownPack) {
                            text = clearBlackList(text);
                            text = type + " | " + text + " | " + extra;
                            userActions.add(text);
                        }
                    }
                }

                Log.d(TAG, "pack = " + pack + " text = " + text + " type = " + type + " extra = " + extra);
                packCode = -1;
                Boolean useGMSorSearchBoxinGoogleMap = visitedApp.equals(map)&&(packCode == 16 ||packCode == 17);
                if((MobileCrowdsourceRecognitionService.matchAppCode(currentApp)==4 && visitedApp.equals(map)) ||MobileCrowdsourceRecognitionService.matchAppCode(pack)==4 ||useGMSorSearchBoxinGoogleMap){
                    packCode = 4;
                }else if((MobileCrowdsourceRecognitionService.matchAppCode(currentApp)==5 && visitedApp.equals(crowdsource))||MobileCrowdsourceRecognitionService.matchAppCode(pack)==5){
                    packCode = 5;
                }else{
                    if(currentApp!=""){
                        packCode = MobileCrowdsourceRecognitionService.matchAppCode(currentApp);
                    }else{
                        packCode = MobileCrowdsourceRecognitionService.matchAppCode(pack);
                    }
                }
                if(packCode!=4 && packCode!=5 && pack!=""){  //  ??????current app????????????????????????????????? ????????????
                    packCode = MobileCrowdsourceRecognitionService.matchAppCode(pack);
                }

               // checkConnection("onAccessEvent",packCode);
                if(pack=="") pack = currentApp;
                if ((packCode == 4) || (packCode == 5)||countDownisRunning){ // map
                    Log.d("nhandle_packCode_ifClicked"," countDownisRunning: "+countDownisRunning);
                    enterMobileCrowdsourceApp(packCode);
                }else {
                    if (enterFlag) {
                        if(!NSPullingDown && packCode !=7) {  // ??????????????????notificationbar  ???????????????  // ???systemui ????????????
                            delayAndCheckLeave();
                        }else{
                            //Log.d(TAG3, "NSHasPulledDown");
                        }
                    } else{
                        // check pull downcontent
                        probablySeen = (ifTextContainClickedPack(packCode)!=-1)&& NSHasPulledDown;
                        Log.d(TAG3,"ifTextContainClickedPack : "+ifTextContainClickedPack(packCode));
                        Log.d(TAG3,"NSHasPulledDown "+NSHasPulledDown);
                        Log.d(TAG3,"probablySeen : "+probablySeen);

                        if((ifClickedNoti && (nhandle_or_dismiss==packCode))|| probablySeen) { //past oneMinute //ifPreviousShown(System.currentTimeMillis(),packCode)  // latest : (notiList.checkIfExist(packCode)&&(nhandle_or_dismiss==packCode)&& NSHasPulledDown)
                            // ????????????
                            //Log.d("nhandle_packCode_ifClicked"," checkIfExist : "+notiList.checkIfExist(packCode));
                            delayAndCheckEnter();  //  ?????????ticker notification ?????????????????????app ???????????????
//                                cleanPostArray();
                        }

                    }
                }
            }
            else {
                AccessibilityNodeInfo source = getRootInActiveWindow();
                if(source!=null){
                    logViewHierarchyForNotiShade(source,0);
                }
                // unwanted package
            }

            storeAccessRecord(pack,text,type,extra);

//
//            if(enter_app_time!=0|| possibleScrollDownPack ||) {
//                try {
//                    // store data
//                    // ??????app?????????  //???????????????
//                    if(possibleScrollDownPack){
//                        pack = "Notification Shade";
//                        text = scrollDownTime;
//                        type = " ";
//                        extra = " ";
//                        finalcontent = allcontent.toString();
//                    }else {
//                        finalcontent = ArrayListtoString(allcontent);
//                    }
//                    if(lastTimePostContent!=null){
//                        if(finalcontent.contains(lastTimePostContent))
//                            finalcontent = finalcontent.replaceAll(lastTimePostContent,"");
//                    }
//                    if (!lastTimePostContent.equals(finalcontent) &&finalcontent.length() != 0) { //!lastTimePostContent.equals(finalcontent) &&
//
//                            Log.d(TAG,"CheckStore "+"Access App:"+pack+"text : "+text);
//                            JSONObject object = new JSONObject();
//                            try {
//                                object.put("pack",pack);
//                                object.put("text",text);
//                                object.put("type",type);
//                                object.put("extra",extra);
//                                object.put("finalcontent",finalcontent);
//                                object.put("mcid",relatedId);
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            CSVHelper.storeToCSV("CheckStoreAccess.csv",object.toString());
//
//                        accessibilityStreamGenerator.setLatestInAppAction(pack, text, type, extra, finalcontent, relatedId);
//                        accessibilityStreamGenerator.updateStream();
//                        lastTimePostContent = finalcontent;
//                        allcontent.clear();
//                        finalcontent = "";
//                        }
//                } catch (Exception e) {
//
//                }
//            }else {
//                if(pack!="")
//                    accessibilityStreamGenerator.setLatestInAppAction(pack, text, type, extra, "", -1);//????????????
//            }

        }
    }


    public void storeAccessRecord(String pack,String text,String type,String extra){
        if(possibleScrollDownPack ||enter_app_time!=0) {  //ifClickedNoti && (nhandle_or_dismiss==packCode))||
                try {
                    // store data
                    // ??????app?????????  //???????????????
                    if(possibleScrollDownPack){
                        pack = "Notification Shade";
                        text = scrollDownTime;
                        type = " ";
                        extra = " ";
                        finalcontent = allcontent.toString();

                    }else {
                        finalcontent = ArrayListtoString(allcontent);
                    }
                    if(lastTimeContent!=null){
                        if(finalcontent.contains(lastTimeContent))
                            finalcontent = finalcontent.replaceAll(lastTimeContent,"");
                    }
                    finalcontent = finalcontent.replaceAll("\\[", "").replaceAll("\\]","");
                    if (!lastTimeContent.equals(finalcontent) &&finalcontent.length() != 0) { //!lastTimePostContent.equals(finalcontent) &&

                            Log.d(TAG,"CheckStore "+"app:"+pack);
                            Log.d(TAG,"CheckStore "+"finalcontent:"+finalcontent);
                            JSONObject object = new JSONObject();
                            try {
                                object.put("pack",pack);
                                object.put("text",text);
                                object.put("type",type);
                                object.put("extra",extra);
                                object.put("finalcontent",finalcontent);
                                object.put("mcid",relatedId);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        if(accessibilityStreamGenerator!=null){
                            //accessibilityStreamGenerator.setLatestInAppAction(pack, text, type, extra, finalcontent, relatedId);
                            accessibilityStreamGenerator.updateStream();
                            object.put("accessibility","not null");
                        }else{
                            object.put("accessibility","null");
                        }

                        CSVHelper.storeToCSV("CheckStoreAccess.csv",object.toString());

                        lastTimeContent = finalcontent;
                        allcontent.clear();
                        finalcontent = "";
                        }
                } catch (Exception e) {

                }
            }else {
                if(!pack.trim().isEmpty()&&!text.trim().isEmpty() && accessibilityStreamGenerator!=null){
                    //accessibilityStreamGenerator.setLatestInAppAction(pack, text, type, extra, "", -1);//????????????
                }else if(accessibilityStreamGenerator==null){
                    CSVHelper.storeToCSV("CheckStoreAccess.csv","accessibilityStreamGenerator null");
                }

            }
    }






    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void delayAndCheckEnter(){

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    enterOtherTargetApp(packCode);
                }
            };
            final Handler handler = new Handler();
            handler.postDelayed(myRunnable, 1000*3);
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void delayAndCheckLeave(){
        if(countDownisRunning == false) {
            countDownisRunning = true;
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    if ((packCode != 4) && (packCode != 5)) {
                        Log.d(TAG2, "is_mobile_c_task : " + is_mobile_crowdsource_task);
                        if(!recordNotificationChanging)
                            leaveMobileCrowdsourceApp();
                        else
                            recordNotificationChanging = false;
                        Log.d("countDownTimer ", "packCode not 4 5 : " + packCode);
                    }
                    Log.d("countDownTimer ", "packCode test: " + packCode);
                    countDownisRunning = false;
                }
            };
            final Handler handler = new Handler();
            handler.postDelayed(myRunnable, 1000*3);
        }



//        if(countDownisRunning == false && timerCount==0) {
//            Timer timer = new Timer();
//            timer.scheduleAtFixedRate(task, INTERVAL, INTERVAL);
//
//            timerCount+=1;
//            Log.d("countDownTimer ","Timer count : "+timerCount);
//            Log.d("countDownTimer ","packCode : "+packCode);
//        }
    }




    @Override
    public void onInterrupt() {

    }


    public void storeMCrecord(String mApp,boolean ifClickNoti,Long mstartTasktime,Long mendTasktime){

        try {
            this.mobileCrowdsourceStreamGenerator = (MobileCrowdsourceStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(MobileCrowdsourceDataRecord.class);
        } catch (StreamNotFoundException e) {
            e.printStackTrace();
        }

        finalaction = ArrayListtoString(userActions); //????????????user actions
        if (!lastTimeContentAction.equals(finalaction) && finalaction.length() != 0) {
            Log.d(TAG,"CheckStore "+"MC mApp:"+mApp+"ifClickNoti : "+ifClickNoti);
            JSONObject object = new JSONObject();
            try {
                object.put("mApp",mApp);
                object.put("ifClickNoti",ifClickNoti);
                object.put("mstartTasktime",getReadableTime(mstartTasktime));
                object.put("mendTasktime",getReadableTime(mendTasktime));
                object.put("finalaction",finalaction);
                object.put("mcid",relatedId);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            CSVHelper.storeToCSV("MC_record.csv",object.toString());
            mobileCrowdsourceStreamGenerator.setMCDataRecord(mApp,ifClickNoti,mstartTasktime,mendTasktime,finalaction,relatedId);
            mobileCrowdsourceStreamGenerator.updateStream();
            lastTimeContentAction = finalaction;
            userActions.clear();
            finalaction = "";
        }
    }


    public String ArrayListtoString(ArrayList<String>list){
        String final_str = "";
        for(String str:list){
            // final check
            str = clearBlackList(str);
            final_str +=str;
            //Log.d(TAG,str);
        }
        return final_str;

    }
    public void deleteRepeated(String str){
        allcontent.add(str);
        Set<String> hs = new HashSet<>();
        hs.addAll(allcontent);
        allcontent.clear();
        allcontent.addAll(hs);
    }

    public static void storeJsontoCSV(JSONObject json, String filename){
        Iterator keys = json.keys();
        Log.d("storeJsontoCSV : ", "json" + json.toString());
        CSVHelper.storeToCSV(filename, json.toString());
        while (keys.hasNext())
            json.remove(keys.next().toString());
    }











    private String getForegroundTask() {
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*100, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        Log.e(TAG, "Current App in foreground is: " + currentApp);

        return currentApp;
    }








    public void stopRecording(){
        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(STOP_RECORDING);
        Toast.makeText(this,"stopRecording",Toast.LENGTH_LONG);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadCastIntent);

    }

}
