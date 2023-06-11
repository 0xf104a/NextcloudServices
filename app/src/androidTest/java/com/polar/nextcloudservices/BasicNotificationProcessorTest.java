package com.polar.nextcloudservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.polar.nextcloudservices.Notification.Processors.basic.AppNameMapper;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BasicNotificationProcessorTest {
    /**
     * Checks that mapper for apps works maps all mappable apps
     */
    @Test
    public void testAppNameMapperStaticMap() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        for(String appName : AppNameMapper.MAPPABLE_APPS){
            String result = AppNameMapper.getPrettifiedAppNameFromMapping(appContext, appName);
            assertNotNull(result);
            assertEquals(result, AppNameMapper.getPrettifiedAppName(appContext, appName));
        }
    }

    /**
     * Checks that mapper prettifies through opportunistic format guessing
     */
    @Test
    public void testAppNameMapperNonStatic(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String name = "my_app_name";
        assertEquals("My app name ", AppNameMapper.getPrettifiedAppName(appContext, name));
    }
}
