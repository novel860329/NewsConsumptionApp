package labelingStudy.nctu.minuku.dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.QuestionWithAnswersDataRecord;

@Dao
public interface QuestionWithAnswersDao
{
    @Insert
    void insertAllChoicesOfQuestion(List<QuestionWithAnswersDataRecord> choices);

    @Query("UPDATE answer_choices SET  ans_choice_state = :selectState WHERE question_id = :questionId AND ans_choice_pos =:optionId")
    void updateQuestionWithChoice(String selectState, String questionId, String optionId);

    @Query("UPDATE answer_choices SET  detected_time = :answerTime WHERE question_id = :questionId AND ans_choice_pos =:optionId")
    void updateQuestionWithDetectedTime(String answerTime, String questionId, String optionId);

    @Query("UPDATE answer_choices SET  ans_choice_state = :selectState, ans_choice_pos = :optionId WHERE question_id = :questionId")
    void updateQuestionPicture(String selectState, String questionId, String optionId);

    @Query("UPDATE answer_choices SET  detected_time = :answerTime, ans_choice_pos = :optionId WHERE question_id = :questionId ")
    void updateQuestionPictureTime(String answerTime, String questionId, String optionId);

    @Query("UPDATE answer_choices SET  ans_choice_state = :selectState, ans_choice_pos = :optionId WHERE question_id = :questionId AND ans_choice = :choice")
    void updateQuestionOther(String selectState, String questionId, String optionId, String choice);

    @Query("UPDATE answer_choices SET  detected_time = :answerTime, ans_choice_pos = :optionId WHERE question_id = :questionId AND ans_choice = :choice")
    void updateQuestionOtherTime(String answerTime, String questionId, String optionId, String choice);

    @Query("UPDATE answer_choices SET  ans_choice = :answer_choice WHERE question_id = :questionId AND ans_choice_pos = :position")
    void updateQuestionName(String answer_choice, String questionId, String position);

    @Query("SELECT ans_choice_state FROM answer_choices WHERE question_id = :questionId AND ans_choice_pos =:optionId ORDER BY id DESC LIMIT 1")
    String isChecked(String questionId, String optionId);
//    @Query("SELECT ans_choice_state FROM FinalAnswer WHERE question_id = :questionId AND answer_choice_pos =:optionId ORDER BY id DESC LIMIT 1")
//    String isCheckedFA(String questionId, String optionId);
//
//    @Query("SELECT id FROM FinalAnswer WHERE question_id = :questionId AND answer_choice_pos =:optionId ORDER BY id DESC LIMIT 1")
//    Long latestIdFA(String questionId, String optionId);

//    @Query("SELECT detected_time FROM answer_choices WHERE question_id = :questionId AND ans_choice_pos =:optionId")
//    String getLastTimeDetectedTime(String questionId, String optionId);

    @Query("SELECT ans_choice FROM answer_choices WHERE ans_choice_state >:selected AND question_id = :questionId")
    String getQuestionsWithChoices(String selected, String questionId);

    @Query("SELECT * FROM answer_choices WHERE ans_choice_state != :selected")
    Cursor getAllQuestionsWithChoices(String selected);

    @Query("SELECT * FROM answer_choices WHERE question_id = :questionId")
    Cursor getQuestionsWithID(String questionId);

    @Query("SELECT * FROM answer_choices")
    List<QuestionWithAnswersDataRecord> getAll();

    @Query("SELECT * FROM answer_choices")
    Cursor getAllCursor();


    @Query("DELETE FROM answer_choices")
    void deleteAllChoicesOfQuestion();
}
