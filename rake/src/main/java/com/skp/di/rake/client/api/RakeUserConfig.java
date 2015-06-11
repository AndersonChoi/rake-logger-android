package com.skp.di.rake.client.api;

public abstract class RakeUserConfig {

    /* function user should implement */
    abstract public RUNNING_ENV provideRunningMode();
    abstract public String provideDevToken();
    abstract public String provideLiveToken();
    abstract public int provideFlushIntervalAsSecond();
    abstract public int provideMaxLogTrackCount();
    abstract public boolean provideWillPrintDebugInfo();

    /* wrapping functions for readability */
    public final RUNNING_ENV getRunningMode() { return provideRunningMode(); }
    public final String getDevToken()  { return provideDevToken();    }
    public final String getLiveToken() { return provideLiveToken();   }
    public int getFlushInterval() { return provideFlushIntervalAsSecond(); }
    public int getMaxLogTrackCount() { return provideMaxLogTrackCount(); }
    public boolean printDebugInfo() { return provideWillPrintDebugInfo(); };

    public final String getToken() {
        if (provideRunningMode() == RUNNING_ENV.DEV) return provideDevToken();
        else                                  return provideLiveToken();
    }

    public enum RUNNING_ENV {DEV, LIVE}

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
