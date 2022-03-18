package labelingStudy.nctu.minuku.stream;

import java.util.ArrayList;
import java.util.List;

import labelingStudy.nctu.minuku.model.DataRecord.AppTimesDataRecord;
import labelingStudy.nctu.minukucore.model.DataRecord;
import labelingStudy.nctu.minukucore.stream.AbstractStreamFromDevice;

public class AppTimesStream extends AbstractStreamFromDevice<AppTimesDataRecord> {
    public AppTimesStream(int maxSize) {
        super(maxSize);
    }

    @Override
    public List<Class<? extends DataRecord>> dependsOnDataRecordType() {
        return new ArrayList<>();
    }
}
