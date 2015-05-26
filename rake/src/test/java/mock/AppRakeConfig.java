package mock;

import com.skp.di.rake.client.config.RakeUserConfig;

public class AppRakeConfig implements RakeUserConfig {

    @Override
    public Mode getRunningMode() {
        return Mode.DEV;
    }

    @Override
    public Integer getFlushInterval() {
        return 60;
    }
}
