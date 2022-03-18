package labelingStudy.nctu.minuku.Utilities;

import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku.config.Constants;

import static labelingStudy.nctu.minuku.config.Constants.PICTURE_DIRECTORY_PATH;

/**
 * Created by Lawrence on 2018/3/31.
 */


public class CSVHelper {

    public static final String TAG = "CSVHelper";

    public static CSVWriter csv_writer = null;

    public static final String CSV_UserInteract = "UserInteraction.csv";
    public static final String CSV_CheckService_alive = "CheckService.csv";

    public static final String CSV_Wifi = "CheckWifi.csv";
    public static final String CSV_Act = "ActivityRecognition.csv";

    public static final String CSV_News = "NewsUrl.csv";
    public static final String CSV_Dump = "DumpData.csv";

    public static final String CSV_Img = "UploadImg.csv";
    public static final String CSV_ESM = "ESM_debug.csv";
    public static final String CSV_Diary = "Diary_debug.csv";
    public static final String CSV_DEBUG = "debug.csv";
    public static final String CSV_CAR = "CheckCAR.csv";
   // public static final String CSV_CHECK_SESSION = "CheckSession.csv";
    public static final String CSV_CHECK_ISALIVE = "CheckIsAlive.csv";
    public static final String CSV_CHECK_TRANSPORTATION = "CheckTransportationMode.csv";
   //  public static final String CSV_RUNNABLE_CHECK = "Runnable_check.csv";
    public static final String CSV_WIFI_RECEIVER_CHECK = "Wifi_Receiver_check.csv";
    public static final String CSV_EXAMINE_COMBINE_SESSION = "ExamineCombineSession.csv";
    public static final String CSV_SEND_TO_SERVER_BEFORE = "SendToServerBefore.csv";
    public static final String CSV_SEND_TO_SERVER_RESPONSE = "SendToServerResponse.csv";
    public static final String CSV_SEND_TO_SERVER_UPDATED_DATA = "SendToServerUpdateData.csv";
    public static final String REPONSE_COUNT = "ResponseCount.csv";



    public static void storeToCSV(String fileName, String... texts){

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            //Log.d(TAG, "root : " + root);
            FileOutputStream os = new FileOutputStream(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+fileName, true);
            os.write(0xef);
            os.write(0xbb);
            os.write(0xbf);
            OutputStreamWriter out = new OutputStreamWriter(os,"UTF-8");
            //csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+fileName,true));
            csv_writer = new CSVWriter(out);

            List<String[]> data = new ArrayList<String[]>();

            long timestamp = ScheduleAndSampleManager.getCurrentTimeInMillis();

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);


            List<String> textInList = new ArrayList<>();

            textInList.add(timeString);

            for(int index = 0; index < texts.length;index++){

                textInList.add(texts[index]);
            }

            String[] textInArray = textInList.toArray(new String[0]);

            data.add(textInArray);

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }/*catch (Exception e){
            //e.printStackTrace();
        }*/
    }

    public static void storeUrlCSV(String fileName, String appname, String content){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date =ScheduleAndSampleManager.getTimeString(System.currentTimeMillis(),df);
        try{
            File root = new File(Environment.getExternalStorageDirectory() + "/Pictures/News_Consumption/");
            if (!root.exists()) {
                root.mkdirs();
            }

            File root_child = new File(Environment.getExternalStorageDirectory() + PICTURE_DIRECTORY_PATH + date);
            if (!root_child.exists()) {
                root_child.mkdirs();
            }
            Log.d(TAG, "root : " + root_child);
            FileOutputStream os = new FileOutputStream(Environment.getExternalStorageDirectory() + PICTURE_DIRECTORY_PATH + date + "/" + fileName, true);
            os.write(0xef);
            os.write(0xbb);
            os.write(0xbf);
            OutputStreamWriter out = new OutputStreamWriter(os,"UTF-8");
            //csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+fileName,true));
            csv_writer = new CSVWriter(out);

            List<String[]> data = new ArrayList<String[]>();

            long timestamp = ScheduleAndSampleManager.getCurrentTimeInMillis();

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);

            //String[] textInArray = textInList.toArray(new String[0]);

            data.add(new String[]{String.valueOf(timestamp), timeString, appname, content});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }/*catch (Exception e){
            //e.printStackTrace();
        }*/
    }

    public static void storeNotiCSV(String fileName, String title, String text, String subText, String tickerText, String pack, String reason){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date =ScheduleAndSampleManager.getTimeString(System.currentTimeMillis(),df);
        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            //Log.d(TAG, "root : " + root);
            FileOutputStream os = new FileOutputStream(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+fileName, true);
            os.write(0xef);
            os.write(0xbb);
            os.write(0xbf);
            OutputStreamWriter out = new OutputStreamWriter(os,"UTF-8");
            //csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+fileName,true));
            csv_writer = new CSVWriter(out);

            List<String[]> data = new ArrayList<String[]>();

            long timestamp = ScheduleAndSampleManager.getCurrentTimeInMillis();

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);

            //String[] textInArray = textInList.toArray(new String[0]);

            data.add(new String[]{String.valueOf(timestamp), timeString, title, text, subText, tickerText, pack, reason});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }/*catch (Exception e){
            //e.printStackTrace();
        }*/
    }

    public static void storeAccessibilityCSV(String fileName, String packageName, String EventText, String EventType, String extra){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date =ScheduleAndSampleManager.getTimeString(System.currentTimeMillis(),df);
        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            //Log.d(TAG, "root : " + root);
            FileOutputStream os = new FileOutputStream(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+fileName, true);
            os.write(0xef);
            os.write(0xbb);
            os.write(0xbf);
            OutputStreamWriter out = new OutputStreamWriter(os,"UTF-8");
            //csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+fileName,true));
            csv_writer = new CSVWriter(out);

            List<String[]> data = new ArrayList<String[]>();

            long timestamp = ScheduleAndSampleManager.getCurrentTimeInMillis();

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);

            //String[] textInArray = textInList.toArray(new String[0]);

            data.add(new String[]{String.valueOf(timestamp), timeString, packageName, EventText, extra, EventType });

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }/*catch (Exception e){
            //e.printStackTrace();
        }*/
    }

    public static void userInformStoreToCSV(String fileName, long timestamp, JSONObject userInform){

//        String sFileName = "TransportationState.csv";

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            //Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+fileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);

            data.add(new String[]{timeString, userInform.toString()});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }/*catch (Exception e){
            //e.printStackTrace();
        }*/
    }

    public static void storeToCSV_IntervalSurveyUpdated(boolean clicked){

        String sFileName = "IntervalSurveyState.csv";

//        Log.d(TAG, "sFileName : " + sFileName);

        try {

            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);

//            Log.d(TAG, "root : " + root);

            if (!root.exists()) {
                root.mkdirs();
            }

            long clickedTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            String clickedTimeString = ScheduleAndSampleManager.getTimeString(clickedTime);

            String clickedString;
            if(clicked)
                clickedString = "Yes";
            else
                clickedString = "No";

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            if(clicked)
                data.add(new String[]{"", "", clickedString, clickedTimeString,""});
            else
                data.add(new String[]{"", "", clickedString, "",""});

            csv_writer.writeAll(data);

            csv_writer.close();

        } catch(IOException e) {
//            e.printStackTrace();
//            android.util.Log.e(TAG, "exception", e);
        } catch (IndexOutOfBoundsException e2){
//            e2.printStackTrace();
//            android.util.Log.e(TAG, "exception", e2);

        }

    }

    public static void TransportationState_StoreToCSV(long timestamp, String state, String activitySofar){

        String sFileName = "TransportationState.csv"; //Static.csv

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            //Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            String timeString = ScheduleAndSampleManager.getTimeString(timestamp);

            data.add(new String[]{String.valueOf(timestamp), timeString, state, String.valueOf(activitySofar)});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }/*catch (Exception e){
            //e.printStackTrace();
        }*/
    }

    public static void dataUploadingCSV(String dataType, String json){

        String sFileName = "DataUploaded.csv"; //Static.csv

        try{
            File root = new File(Environment.getExternalStorageDirectory() + Constants.PACKAGE_DIRECTORY_PATH);
            if (!root.exists()) {
                root.mkdirs();
            }

            //Log.d(TAG, "root : " + root);

            csv_writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory()+Constants.PACKAGE_DIRECTORY_PATH+sFileName,true));

            List<String[]> data = new ArrayList<String[]>();

            data.add(new String[]{dataType, json});

            csv_writer.writeAll(data);

            csv_writer.close();

        }catch (IOException e){
            //e.printStackTrace();
        }catch (Exception e){
            //e.printStackTrace();
        }
    }





}