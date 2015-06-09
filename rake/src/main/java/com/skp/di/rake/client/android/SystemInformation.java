package com.skp.di.rake.client.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SystemInformation {
    private static final DateFormat baseTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final DateFormat localTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    static { baseTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); }

    /* ref - http://wiki.skplanet.com/pages/viewpage.action?pageId=45917550 */
    static private final String UNKNOWN = "UNKNOWN";
    static private final String RAKE_LIB = "android";

    static private String device_model;
    static private String device_id; /* ref - http://wiki.skplanet.com/pages/viewpage.action?pageId=71843315 */
    static private final String os_name = "Android";
    static private String os_version;

    static private int screen_width;
    static private int screen_height;
    static private String resolution;

    static private String app_version;
    static private String app_build_date;

    static private String carrier_name;
    static private String language_code;

    static private String manufacturer;

    private Context context;

    public SystemInformation(Context context) {
        this.context = context;

        device_model = (null == Build.MODEL) ? UNKNOWN : Build.MODEL;
        device_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (isEmpty(device_id)) device_id = UNKNOWN;

        os_version = (null == Build.VERSION.RELEASE) ? UNKNOWN : Build.VERSION.RELEASE;

        DisplayMetrics metric = getDisPlayMetrics(context);
        screen_height = metric.widthPixels;
        screen_width  = metric.heightPixels;
        resolution = new StringBuilder().append(screen_width).append("*").append(screen_height).toString();

        PackageManager pm = context.getPackageManager();
        String pn = context.getPackageName();
        app_build_date = getAppBuildDate(pm, pn);
        app_version = getAppVersion(pm, pn);

        if (isEmpty(app_version)) app_version = UNKNOWN;
        if (isEmpty(app_build_date)) app_build_date = UNKNOWN;

        carrier_name = getCurrentNetworkOperator(context);
        if (isEmpty((carrier_name))) carrier_name = UNKNOWN;

        manufacturer = (null == Build.MANUFACTURER) ? UNKNOWN : Build.MANUFACTURER;
    }

    // TODO: language_code, network_type 을 생성자로 옮기면, 좀 더 성능 개선 가능
    public JSONObject getDefaultProperties(RakeUserConfig config) throws JSONException {
        Date now = new Date();
        JSONObject defaultProperties = new JSONObject();

        String appVersion = (RakeUserConfig.Mode.DEV == config.getRunningMode()) ?
                app_version + app_build_date : app_version;

        Boolean isWifi = isWifiConnected();
        String network_type;
        if (null == isWifi) network_type = UNKNOWN;
        else if (isWifi.booleanValue()) network_type = "WIFI";
        else network_type = "NOT WIFI";

        language_code = context.getResources().getConfiguration().locale.getCountry();
        String languageCode = isEmpty(language_code) ? UNKNOWN : language_code;

        // put default non-permanent properties
        defaultProperties.put("app_version", appVersion);
        defaultProperties.put("network_type", network_type);
        defaultProperties.put("language_code", languageCode);

        // put default permanent properties
        defaultProperties.put("device_id", device_id);
        defaultProperties.put("device_model", device_model);
        defaultProperties.put("os_name", os_name);
        defaultProperties.put("os_version", os_version);
        defaultProperties.put("resolution", resolution);
        defaultProperties.put("screen_width", screen_width);
        defaultProperties.put("screen_height", screen_height);
        defaultProperties.put("carrier_name", carrier_name);
        defaultProperties.put("manufacturer", manufacturer);

        // put properties irrelevant to android system information
        defaultProperties.put("token", config.getToken());
        defaultProperties.put("base_time", baseTimeFormat.format(now));
        defaultProperties.put("local_time", localTimeFormat.format(now));
        defaultProperties.put("rake_lib", RAKE_LIB);
        defaultProperties.put("rake_lib_version", RakeMetaConfig.RAKE_CLIENT_VERSION);

        return defaultProperties;
    }

    static private boolean isEmpty(String str) {
        if (null == str) return true;
        if (str.isEmpty()) return true;

        return false;
    }


    static private DisplayMetrics getDisPlayMetrics(Context context) {
        DisplayMetrics metric = new DisplayMetrics();

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(metric);

        return metric;
    }


    static private String getAppVersion(PackageManager manager, String packageName) {
        String appVersionName = null;

        try {
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            appVersionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return appVersionName;
    }

    static private String getAppBuildDate(PackageManager pm, String pn) {
        String buildDate = null;

        try {
            ApplicationInfo ai = pm.getApplicationInfo(pn, 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            TimeZone tz = TimeZone.getDefault(); /* current TimeZone */
            formatter.setTimeZone(tz);

            buildDate = formatter.format(new Date(time));

            zf.close();
        } catch(PackageManager.NameNotFoundException e) {
            Logger.e("System information constructed with a context that apparently doesn't exist.", e);
        } catch(IOException e) {
            Logger.e("Can't create ZipFile Instance using given ApplicationInfo", e);
        }

        return buildDate;
    }

    public String getCurrentNetworkOperator(Context context) {
        String carrier_name = null;

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != telephonyManager)
            carrier_name = telephonyManager.getNetworkOperatorName();

        return carrier_name;
    }

    public Boolean isWifiConnected() {
        Boolean ret = null;

        if (PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager connManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            ret = wifiInfo.isConnected();
        }

        return ret;
    }
}
