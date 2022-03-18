package labelingStudy.nctu.minuku.model.DataRecord;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "questions")
public class QuestionDataRecord
{
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "question_id")
    private int questionId;

    private String question;

    @ColumnInfo(name = "StoreToFinal")
    private boolean StoreToFinal;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public int getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(int questionId)
    {
        this.questionId = questionId;
    }

    public String getQuestion()
    {
        return question;
    }

    public void setQuestion(String question)
    {
        this.question = question;
    }

    public void setStoreToFinal(boolean flag){
        this.StoreToFinal = flag;
    }
    public boolean getStoreToFinal(){
        return this.StoreToFinal;
    }
}
