package mock;

import com.skp.di.rake.client.logger.Rake;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;

import java.util.Collections;
import java.util.HashMap;

public class TestRakeFactory {
    private static HashMap<Class, Rake> loggerMap;

    static {
        loggerMap = new HashMap<>();
        Collections.synchronizedMap(loggerMap);
    }

    public static Rake getLogger(Class clazz) {

        Rake logger;

        if (loggerMap.containsKey(clazz)) {
            logger = loggerMap.get(clazz);
        } else {
            RakeDao dao = new RakeDaoMemory();
            logger = new TestRakeLogger(dao); /* use TestRakeLogger */
            loggerMap.put(clazz, logger);
        }

        return logger;
    }
}


