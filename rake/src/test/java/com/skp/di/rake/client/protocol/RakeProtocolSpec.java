package com.skp.di.rake.client.protocol;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeProtocolSpec {

    @Test
    public void test() {

    }

}

/*
 인코딩 전
[{"_$schemaId":"55261906e4b0ef56c2c18ed2","_$encryptionFields":[],"properties":{"log_version":"15.04.09:1.5.26:57","network_type":"NOT WIFI","app_version":"1.0","screen_width":2560,"device_id":"a8cac7a1ca580768","resolution":"1440*2560","recv_host":"","ip":"","os_version":"4.4.4","recv_time":"","local_time":"20150413172346281","language_code":"KR","device_model":"SM-N910S","rake_lib_version":"r0.5.0_c0.3.14","os_name":"Android","token":"17d7c63735d1d1ec81a97e4c44d47acc8420ed15","rake_lib":"android","manufacturer":"samsung","action":"action4","_$body":{"field1":"field1 value","field4":"field4 value","field3":"field3 value"},"base_time":"20150413172346281","carrier_name":"SKTelecom","screen_height":1440},"_$fieldOrder":{"network_type":12,"log_version":20,"screen_width":9,"app_version":16,"device_id":3,"resolution":8,"recv_host":15,"recv_time":2,"os_version":7,"ip":14,"local_time":1,"device_model":4,"language_code":13,"rake_lib_version":18,"os_name":6,"token":19,"manufacturer":5,"rake_lib":17,"action":21,"_$body":22,"base_time":0,"carrier_name":11,"screen_height":10},"_$projectId":"projectId"}]

 */
