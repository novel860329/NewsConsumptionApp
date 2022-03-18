package labelingStudy.nctu.minuku.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Lawrence on 2018/4/19.
 */

public class Utils {


    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static String getJSON(String url) {

        HttpsURLConnection con = null;
        String json = "";

        try {
            URL u = new URL(url);
            con = (HttpsURLConnection) u.openConnection();

            con.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }

            json = sb.toString();
            br.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (con != null) {

                try {

                    con.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return json;
    }
    public static final String DATE_FORMAT_HOUR_MIN_SECOND = "HH-mm-ss.SSS";
    public static final String DATE_FORMAT_NOW_DAY = "yyyy-MM-dd";
    public static final String DATE_FORMAT_NOW_HOUR_MIN = "yyyy-MM-dd-HH-mm-ss.SSS";
    public static String getTimeString(String sdf){
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(sdf);
        return df.format(date);
    }
}
