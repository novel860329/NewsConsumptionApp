package labelingStudy.nctu.minuku.DBHelper;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import labelingStudy.nctu.minuku.dao.AccessibilityDataRecordDao;
import labelingStudy.nctu.minuku.dao.ActivityRecognitionDataRecordDao;
import labelingStudy.nctu.minuku.dao.AppTimesDataRecordDao;
import labelingStudy.nctu.minuku.dao.AppUsageDataRecordDao;
import labelingStudy.nctu.minuku.dao.BatteryDataRecordDao;
import labelingStudy.nctu.minuku.dao.ConnectivityDataRecordDao;
import labelingStudy.nctu.minuku.dao.FinalAnswerDao;
import labelingStudy.nctu.minuku.dao.LocationDataRecordDao;
import labelingStudy.nctu.minuku.dao.MobileCrowdsourceDataRecordDao;
import labelingStudy.nctu.minuku.dao.MyDataRecrodDao;
import labelingStudy.nctu.minuku.dao.NewsDataRecrodDao;
import labelingStudy.nctu.minuku.dao.NotificationDataRecordDao;
import labelingStudy.nctu.minuku.dao.QuestionDao;
import labelingStudy.nctu.minuku.dao.QuestionWithAnswersDao;
import labelingStudy.nctu.minuku.dao.ResponseDataRecordDao;
import labelingStudy.nctu.minuku.dao.RingerDataRecordDao;
import labelingStudy.nctu.minuku.dao.SensorDataRecordDao;
import labelingStudy.nctu.minuku.dao.SessionDataRecordDao;
import labelingStudy.nctu.minuku.dao.TelephonyDataRecordDao;
import labelingStudy.nctu.minuku.dao.TransportationModeDataRecordDao;
import labelingStudy.nctu.minuku.dao.UserDataRecordDao;
import labelingStudy.nctu.minuku.dao.VideoDataRecordDao;
import labelingStudy.nctu.minuku.model.DataRecord.AccessibilityDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.AppTimesDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.AppUsageDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.BatteryDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ConnectivityDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.CrashAppDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.FinalAnswerDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.LocationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.MobileCrowdsourceDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.MyDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.NewsDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.QuestionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.QuestionWithAnswersDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.ResponseDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SensorDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.SessionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TelephonyDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.TransportationModeDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.VideoDataRecord;

//import android.arch.persistence.room.Database;
//import androidx.arch.persistence.room.Room;
//import androidx.arch.persistence.room.RoomDatabase;

/**
 * Created by chiaenchiang on 07/03/2018.
 */
@Database(entities = {SensorDataRecord.class, AccessibilityDataRecord.class,
        BatteryDataRecord.class, ActivityRecognitionDataRecord.class,
        AppUsageDataRecord.class, RingerDataRecord.class,
        TelephonyDataRecord.class, ConnectivityDataRecord.class,
        LocationDataRecord.class, UserDataRecord.class,
        TransportationModeDataRecord.class, NotificationDataRecord.class,MobileCrowdsourceDataRecord.class,
        QuestionWithAnswersDataRecord.class, QuestionDataRecord.class, FinalAnswerDataRecord.class,
        VideoDataRecord.class, ResponseDataRecord.class, MyDataRecord.class, CrashAppDataRecord.class,
        AppTimesDataRecord.class, NewsDataRecord.class, SessionDataRecord.class},version =1)
@TypeConverters({Converters.class})
public abstract class appDatabase extends RoomDatabase {

    public abstract AppTimesDataRecordDao AppTimesDataRecordDao();
    public abstract MyDataRecrodDao MyDataRecordDao();
    public abstract NewsDataRecrodDao NewsDataRecordDao();
    public abstract SessionDataRecordDao SessionDataRecordDao();
    public abstract AccessibilityDataRecordDao accessibilityDataRecordDao();
    public abstract ActivityRecognitionDataRecordDao activityRecognitionDataRecordDao();
    public abstract AppUsageDataRecordDao appUsageDataRecordDao();
    public abstract BatteryDataRecordDao batteryDataRecordDao();
    public abstract ConnectivityDataRecordDao connectivityDataRecordDao();
    public abstract LocationDataRecordDao locationDataRecordDao();
    public abstract RingerDataRecordDao ringerDataRecordDao();
    public abstract SensorDataRecordDao sensorDataRecordDao();
    public abstract TelephonyDataRecordDao telephonyDataRecordDao();
    public abstract TransportationModeDataRecordDao transportationModeDataRecordDao();
    public abstract NotificationDataRecordDao notificationDataRecordDao();
    public abstract MobileCrowdsourceDataRecordDao mobileCrowdsourceDataRecordDao();
    public abstract QuestionWithAnswersDao questionWithAnswersDao();
    public abstract QuestionDao questionDao();
    public abstract UserDataRecordDao userDataRecordDao();
    public abstract FinalAnswerDao finalAnswerDao();
    public abstract VideoDataRecordDao videoDataRecordDao();
    public abstract ResponseDataRecordDao repsonseDataRecordDao();




    private static appDatabase INSTANCE;

    // create new database connection
    public static appDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(), appDatabase.class, "NewsCollection_v6")
                        .allowMainThreadQueries()
                        .build();
            }
            return INSTANCE;
    }

    // close database
    public static void destroyInstance(){
        if (INSTANCE.isOpen()) INSTANCE.close();
        INSTANCE = null;
    }



}
