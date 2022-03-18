package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

@Dao
public interface UserDataRecordDao {
    @Insert
    void insertAll(UserDataRecord userDataRecordDao);

    @Query("SELECT * FROM UserDataRecord  WHERE sycStatus =:notSyncInt  GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt);

    @Query("SELECT * FROM UserDataRecord  WHERE sycStatus =:notSyncInt and readable BETWEEN :lastHour AND :targetHour GROUP BY creationTime")
    Cursor getUnsyncedData(int notSyncInt,Long targetHour, Long lastHour);

    @Query("SELECT * FROM UserDataRecord  ORDER BY _id DESC LIMIT 0 , 1")
    UserDataRecord getLastRecord();

//    @Query("DELETE FROM UserDataRecord WHERE sycStatus = :status")
//    void deleteSyncData(int status);

    @Query("UPDATE  UserDataRecord SET sycStatus = :status WHERE creationTime = :creationT")
    int updateDataStatus(long creationT, int status);

    @Query("UPDATE  UserDataRecord SET userId = :userid WHERE _id = :id")
    void updateUserid(long id, String userid);

    @Query("UPDATE  UserDataRecord SET questionnaire_startTime = :questionnaire_startTime WHERE _id = :id")
    void updateQuestionnaire_startTime(long id, String questionnaire_startTime);

    @Query("UPDATE  UserDataRecord SET questionnaire_endTime = :questionnaire_endTime WHERE _id = :id")
    void updateQuestionnaire_endTime(long id, String questionnaire_endTime);

    @Query("UPDATE  UserDataRecord SET userid_confirm = :userid_confirm WHERE _id = :id")
    void updateUserConfirm(long id, boolean userid_confirm);

    @Query("UPDATE  UserDataRecord SET time_confirm = :time_confirm WHERE _id = :id")
    void updateTimeConfirm(long id, boolean time_confirm);

    @Query("UPDATE  UserDataRecord SET LastESMTime_for_Q1 = :LastESMTime_for_Q1 WHERE _id = :id")
    void updateLastESMTime_for_Q1(long id, long LastESMTime_for_Q1);

    @Query("UPDATE  UserDataRecord SET  ESM_number = :ESM_number WHERE _id = :id")
    void updateESM(long id, String ESM_number);

    @Query("UPDATE  UserDataRecord SET  TotalESM_number = :TotalESM_number WHERE _id = :id")
    void updateTotalESM(long id, String TotalESM_number);

    @Query("UPDATE  UserDataRecord SET  Diary_number = :Diary_number WHERE _id = :id")
    void updateDiary(long id, String Diary_number);

    @Query("UPDATE  UserDataRecord SET  TotalDiary_number = :TotalDiary_number WHERE _id = :id")
    void updateTotalDiary(long id, String TotalDiary_number);

    @Query("UPDATE  UserDataRecord SET  exec = :exec WHERE _id = :id")
    void updateExec(long id, boolean exec);

    @Query("UPDATE  UserDataRecord SET  app_start = :app_start WHERE _id = :id")
    void updateAppstart(long id, long app_start);

    @Query("UPDATE  UserDataRecord SET  IsKilled = :IsKilled WHERE _id = :id")
    void updateIsKilled(long id, boolean IsKilled);

    @Query("UPDATE  UserDataRecord SET  DiaryClick = :DiaryClick WHERE _id = :id")
    void updateDiaryClick(long id, boolean DiaryClick);

    /* 3/17 */
    @Query("UPDATE  UserDataRecord SET  DiarySend = :DiarySend WHERE _id = :id")
    void updateDiarySend(long id, boolean DiarySend);

    @Query("UPDATE  UserDataRecord SET  PhoneSessionID = :PhoneSessionID WHERE _id = :id")
    void updatePhoneSessionID(long id, Long PhoneSessionID);

    @Query("UPDATE  UserDataRecord SET  QuestionnaireID = :QuestionnaireID WHERE _id = :id")
    void updateQuestionnaireID(long id, Integer QuestionnaireID);

    @Query("UPDATE  UserDataRecord SET  LastEsmTime = :LastEsmTime WHERE _id = :id")
    void updateLastEsmTime(long id, long LastEsmTime);

    @Query("UPDATE  UserDataRecord SET  SenderList = :SenderList WHERE _id = :id")
    void updateSenderList(long id, String SenderList);

    @Query("UPDATE  UserDataRecord SET  CanFillESM = :CanFillESM WHERE _id = :id")
    void updateCanFillESM(long id, boolean CanFillESM);

    @Query("UPDATE  UserDataRecord SET  CanFillDiary = :CanFillDiary WHERE _id = :id")
    void updateCanFillDiary(long id, boolean CanFillDiary);

    @Query("UPDATE  UserDataRecord SET  DialogDeny = :DialogDeny WHERE _id = :id")
    void updateDialogDeny(long id, boolean DialogDeny);
//    @Query("UPDATE  UserDataRecord SET sycStatus = :status WHERE fileName = :fileName")
//    int updateDataStatusByFileName(String fileName, int status);
//    @Query("SELECT * From UserDataRecord  WHERE sycStatus = :status GROUP BY creationTime")
//    Cursor getSyncVideoData(int status);
//    @Query("DELETE FROM UserDataRecord WHERE fileName = :fileName")
//    int deleteDataByFileName(String fileName);



}
