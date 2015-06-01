package com.skp.di.rake.client.api;

import com.skp.di.rake.client.config.RakeMetaConfig;

public abstract class RakeUserConfig {
    /* function user should implement */

    abstract public Mode provideRunningMode();
    abstract public String provideDevToken();
    abstract public String provideLiveToken();
    abstract public int provideFlushInterval();
    abstract public int provideMaxLogTrackCount();

    /* wrapping functions for readability */

    public final Mode getRunningMode() { return provideRunningMode(); }
    public final String getDevToken()  { return provideDevToken();    }
    public final String getLiveToken() { return provideLiveToken();   }
    public int getFlushInterval() { return provideFlushInterval(); }
    public int getMaxLogTrackCount() { return provideMaxLogTrackCount(); }

    public final String getToken() {
        if (provideRunningMode() == Mode.DEV) return provideDevToken();
        else                                  return provideLiveToken();
    }

    public enum Mode {DEV, LIVE}

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (null == other) return false;
        if (this.getClass() != other.getClass()) return false;

        RakeUserConfig that = (RakeUserConfig) other;

        String thisToken = this.getLiveToken() + this.getDevToken();
        String thatToken = that.getLiveToken() + that.getDevToken();

        if (thisToken.equals(thatToken)) return true;
        else return false;
    }

    @Override
    public int hashCode() {
        /*  same as linear search.
            since the number of config will be small. at most 3? */
        return 0;
    }
}
