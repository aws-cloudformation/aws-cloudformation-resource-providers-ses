package software.amazon.ses.receiptfilter;

import java.util.Map;
import org.json.JSONObject;
import org.json.JSONTokener;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-ses-receiptfilter.json");
    }
}
