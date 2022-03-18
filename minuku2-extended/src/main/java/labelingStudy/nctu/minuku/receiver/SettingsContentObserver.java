package labelingStudy.nctu.minuku.receiver;

import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.RingerDataRecord;
import labelingStudy.nctu.minuku.streamgenerator.RingerStreamGenerator;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;

public class SettingsContentObserver extends ContentObserver {
    String TAG = "ContentObserver";
    RingerStreamGenerator mRingerStreamGenerator;
    public SettingsContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        try {
            mRingerStreamGenerator = (RingerStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(RingerDataRecord.class);
        } catch (StreamNotFoundException e) {
            labelingStudy.nctu.minuku.logger.Log.d(TAG, "Initial MyAccessibility Service Failed");
        }
        if(mRingerStreamGenerator != null)
        {
            Log.d(TAG, "Content Observer");
            mRingerStreamGenerator.getAudioRingerUpdate();
        }
        Log.v(TAG, "Settings change detected");
    }
}