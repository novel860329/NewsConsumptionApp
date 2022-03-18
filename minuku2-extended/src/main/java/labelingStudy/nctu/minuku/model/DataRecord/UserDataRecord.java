package labelingStudy.nctu.minuku.model.DataRecord;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import labelingStudy.nctu.minukucore.model.DataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;

@Entity(tableName = "UserDataRecord")
public class UserDataRecord implements DataRecord {
    @PrimaryKey(autoGenerate = true)
    private long _id;

    @ColumnInfo(name = "creationTime")
    public long creationTime;

    @ColumnInfo(name = "userId")
    public String userId;

    @ColumnInfo(name = "ESM_number")
    public String ESM_number;

    @ColumnInfo(name = "TotalESM_number")
    public String TotalESM_number;

    @ColumnInfo(name = "Diary_number")
    public String Diary_number;

    @ColumnInfo(name = "TotalDiary_number")
    public String TotalDiary_number;

    @ColumnInfo(name = "questionnaire_startTime")
    public String questionnaire_startTime;

    @ColumnInfo(name = "questionnaire_endTime")
    public String questionnaire_endTime;

    @ColumnInfo(name = "LastESMTime_for_Q1")
    public long LastESMTime_for_Q1;

    @ColumnInfo(name = "userid_confirm")
    public boolean userid_confirm;

    @ColumnInfo(name = "time_confirm")
    public boolean time_confirm;

    @ColumnInfo(name = "app_start")
    public long app_start;

    @ColumnInfo(name = "exec")
    public boolean exec;

    @ColumnInfo(name = "DiaryClick")
    public boolean DiaryClick;

    @ColumnInfo(name = "IsKilled")
    public boolean IsKilled;

    @ColumnInfo(name = "PhoneSessionID")
    public Long PhoneSessionID;

    @ColumnInfo(name = "QuestionnaireID")
    public Integer QuestionnaireID;

    @ColumnInfo(name = "SenderList")
    public String SenderList;

    @ColumnInfo(name = "LastEsmTime")
    public Long LastEsmTime;

    /* 3/17 */
    @ColumnInfo(name = "DiarySend")
    public boolean DiarySend;

    /* 3/1 */
    @ColumnInfo(name = "CanFillDiary")
    public boolean CanFillDiary;

    /* 3/1 */
    @ColumnInfo(name = "CanFillESM")
    public boolean CanFillESM;

    /* 3/1 */
    @ColumnInfo(name = "DialogDeny")
    public boolean DialogDeny;

    @ColumnInfo(name = "readable")
    public Long readable;

    @ColumnInfo(name = "sycStatus")
    public Integer syncStatus;


    public UserDataRecord(String userId, String ESM_number, String TotalESM_number, String Diary_number, String TotalDiary_number, String questionnaire_startTime,
                          String questionnaire_endTime, long LastESMTime_for_Q1, boolean userid_confirm, boolean time_confirm, long app_start, boolean exec){
        this.creationTime = new Date().getTime();
        this.userId = userId;
        this.ESM_number = ESM_number;
        this.TotalESM_number = TotalESM_number;
        this.Diary_number = Diary_number;
        this.TotalDiary_number = TotalDiary_number;
        this.questionnaire_startTime = questionnaire_startTime;
        this.questionnaire_endTime = questionnaire_endTime;
        this.LastESMTime_for_Q1 = LastESMTime_for_Q1;
        this.userid_confirm = userid_confirm;
        this.time_confirm = time_confirm;
        this.app_start = app_start;
        this.exec = exec;
        this.DiaryClick = false;
        this.IsKilled = false;
        this.PhoneSessionID = 0L;
        this.QuestionnaireID = -1;
        this.LastEsmTime = 0L;
        this.syncStatus = 0;
        this.readable = getReadableTimeLong(this.creationTime);
        this.SenderList = "";
        this.DiarySend = false;
        this.CanFillDiary = false;
        this.CanFillESM = false;
        this.DialogDeny = false;
    }
    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public String getUserId(){
        return this.userId;
    }
    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getESM_number(){
        return this.ESM_number;
    }
    public void setESM_number(String ESM_number){
        this.ESM_number = ESM_number;
    }

    public String getDiary_number(){
        return this.Diary_number;
    }
    public void setDiary_number(String Diary_number){
        this.Diary_number = Diary_number;
    }

    public String getTotal_ESM_number(){
        return this.TotalESM_number;
    }
    public void setTotalESM_number(String TotalESM_number){
        this.TotalESM_number = TotalESM_number;
    }

    public String getTotalDiary_number(){
        return this.TotalDiary_number;
    }
    public void setTotalDiary_number(String TotalDiary_number){
        this.TotalDiary_number = TotalDiary_number;
    }

    public String getquestionnaire_startTime(){
        return this.questionnaire_startTime;
    }
    public void setquestionnaire_startTime(String questionnaire_startTime){
        this.questionnaire_startTime = questionnaire_startTime;
    }

    public String getquestionnaire_endTime(){
        return this.questionnaire_endTime;
    }
    public void setquestionnaire_endTime(String questionnaire_endTime){
        this.questionnaire_endTime = questionnaire_endTime;
    }

    public void setLastESMTime_for_Q1(long LastESMTime_for_Q1){
        this.LastESMTime_for_Q1 = LastESMTime_for_Q1;
    }
    public long getLastESMTime_for_Q1(){
        return this.LastESMTime_for_Q1;
    }

    public boolean getuserid_confirm(){
        return this.userid_confirm;
    }
    public void setuserid_confirm(boolean userid_confirm){
        this.userid_confirm = userid_confirm;
    }

    public boolean gettime_confirm(){
        return this.time_confirm;
    }
    public void settime_confirm(boolean time_confirm){
        this.time_confirm = time_confirm;
    }

    public Long getApp_start(){
        return this.app_start;
    }
    public void setApp_start(long app_start){
        this.app_start = app_start;
    }

    public boolean getExec(){
        return this.exec;
    }
    public void setExec(boolean exec){
        this.exec = exec;
    }

    public boolean getDiaryClick(){
        return this.DiaryClick;
    }
    public void setDiaryClick(boolean DiaryClick){
        this.DiaryClick = DiaryClick;
    }

    public boolean getDiarySend(){
        return this.DiarySend;
    }
    public void setDiarySend(boolean DiarySend){
        this.DiarySend = DiarySend;
    }

    public boolean getIsKilled(){
        return this.IsKilled;
    }
    public void setIsKilled(boolean IsKilled){
        this.IsKilled = IsKilled;
    }

    public Long getLastEsmTime(){
        return this.LastEsmTime;
    }
    public void setLastEsmTime(Long LastEsmTime){
        this.LastEsmTime = LastEsmTime;
    }

    public Long getPhoneSession(){
        return this.PhoneSessionID;
    }
    public void setPhoneSessionID(Long PhoneSessionID){
        this.PhoneSessionID = PhoneSessionID;
    }

    public Integer getquestionnaireID(){
        return this.QuestionnaireID;
    }
    public void setquestionnaireID(Integer questionnaireID){
        this.QuestionnaireID = questionnaireID;
    }

    public String getSenderList(){
        return this.SenderList;
    }
    public void setSenderList(String SenderList){
        this.SenderList = SenderList;
    }

    public boolean getCanFillDiary(){
        return this.CanFillDiary;
    }
    public void setCanFillDiary(boolean CanFillDiary){
        this.CanFillDiary = CanFillDiary;
    }

    public boolean getCanFillESM(){
        return this.CanFillESM;
    }
    public void setCanFillESM(boolean CanFillESM){
        this.CanFillESM = CanFillESM;
    }

    public boolean getDialogDeny(){
        return this.DialogDeny;
    }
    public void setDialogDeny(boolean DialogDeny){
        this.DialogDeny = DialogDeny;
    }
}
