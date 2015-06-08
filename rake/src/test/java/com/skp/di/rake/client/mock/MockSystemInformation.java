package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MockSystemInformation {
   static public JSONObject getDefaultProperties(RakeUserConfig config) throws JSONException {
      DateFormat baseTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      DateFormat localTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      JSONObject defaultProperties = new JSONObject();

      Date now = new Date();
      defaultProperties.put("app_version", "1.0.0");
      defaultProperties.put("network_type", "WIFI");
      defaultProperties.put("language_code", "KR");

      defaultProperties.put("device_id", "example_device_id");
      defaultProperties.put("device_model", "example_device_model");
      defaultProperties.put("os_name", "Android");
      defaultProperties.put("os_version", "4.4.2");

      defaultProperties.put("resolution", "1080*1920");
      defaultProperties.put("screen_width", "1920");
      defaultProperties.put("screen_height", "1080");

      defaultProperties.put("carrier_name", "SK Telecom");
      defaultProperties.put("manufacturer", "samsung");

      // put properties irrelevant to android system information
      defaultProperties.put("token", config.getToken());
      defaultProperties.put("base_time", baseTimeFormat.format(now));
      defaultProperties.put("local_time", localTimeFormat.format(now));
      defaultProperties.put("rake_lib", "android");
      defaultProperties.put("rake_lib_version", RakeMetaConfig.RAKE_CLIENT_VERSION);

      return defaultProperties;
   }
}
