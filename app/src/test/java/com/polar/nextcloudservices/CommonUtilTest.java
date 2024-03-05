package com.polar.nextcloudservices;

import org.junit.Test;

import static org.junit.Assert.*;

import com.polar.nextcloudservices.Utils.CommonUtil;


public class CommonUtilTest {
    @Test
    public void testURLCleanup(){
        String result1 = CommonUtil.cleanUpURLIfNeeded("https://cloud.example.com/query?domain=cloud.example.com&path=https://cloud");
        assertEquals(result1, "/query?domain=cloud.example.com&path=https://cloud");
        String result3 = CommonUtil.cleanUpURLIfNeeded("https://cloud.example.com/query?domain=cloud.example.com&path=https://cloud");
        assertEquals(result3, "/query?domain=cloud.example.com&path=https://cloud");
        String result4 = CommonUtil.cleanUpURLIfNeeded("https://cloud.example.com:8080/query");
        assertEquals(result4, "/query");
        String result5 = CommonUtil.cleanUpURLIfNeeded("https://cloud.example.com:8080/query?domain=cloud.example.com&path=https://cloud");
        assertEquals(result5, "/query?domain=cloud.example.com&path=https://cloud");
        String result6 = CommonUtil.cleanUpURLIfNeeded("https://cloud.example.com:8080/query?domain=cloud.example.com&path=https://cloud#fragment");
        assertEquals(result6, "/query?domain=cloud.example.com&path=https://cloud#fragment");
    }

    @Test
    public void testInArray(){
        final String[] items = {"foo"};
        String foo = "foo";
        String bar = "bar";
        assertTrue(CommonUtil.isInArray(foo, items));
        assertFalse(CommonUtil.isInArray(bar, items));
    }

    @Test
    public void testCleanUpURLParams(){
        assertEquals(CommonUtil.cleanUpURLParams(""), "");
        assertEquals(CommonUtil.cleanUpURLParams("https://example.com/#abcdef"),
                "https://example.com/");
        assertEquals(CommonUtil.cleanUpURLParams("https://example.com/url/example#abcdef"),
                "https://example.com/url/example");
        assertEquals(CommonUtil.cleanUpURLParams(
                "https://example.com/url/example?a=b&c=d&f=42#abcdef"),
                "https://example.com/url/example?a=b&c=d&f=42");
    }
}
