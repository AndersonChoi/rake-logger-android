package mock;

import com.skp.di.rake.client.config.RakeUserConfig;
import com.skp.di.rake.client.logger.Rake;
import com.skp.di.rake.client.logger.RakeLogger;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;

import java.util.Collections;
import java.util.HashMap;

public class TestRakeFactory {
    private static HashMap<String, Rake> loggerMap;

    static {
        loggerMap = new HashMap<>();
        Collections.synchronizedMap(loggerMap);
    }

    public static Rake getLogger(String token, RakeUserConfig config) {

        Rake logger;

        if (loggerMap.containsKey(token)) {
            logger = loggerMap.get(token);
        } else {
            RakeDao dao           = new RakeDaoMemory();
            RakeHttpClient client = new TestRakeHttpClient();

            logger = new RakeLogger(dao, client); /* use TestRakeLogger */
            loggerMap.put(token, logger);
        }

        return logger;
    }
}


