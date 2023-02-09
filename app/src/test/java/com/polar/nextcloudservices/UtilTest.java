package com.polar.nextcloudservices;

import org.junit.Test;

import static org.junit.Assert.*;


public class UtilTest {
    @Test
    public void testURLCleanup(){
        String result1 = Util.cleanUpURLIfNeeded("https://cloud.example.com/query?domain=cloud.example.com&path=https://cloud");
        assertEquals(result1, "/query?domain=cloud.example.com&path=https://cloud");
        String result3 = Util.cleanUpURLIfNeeded("https://cloud.example.com/query?domain=cloud.example.com&path=https://cloud");
        assertEquals(result3, "/query?domain=cloud.example.com&path=https://cloud");
        String result4 = Util.cleanUpURLIfNeeded("https://cloud.example.com:8080/query");
        assertEquals(result4, "/query");
        String result5 = Util.cleanUpURLIfNeeded("https://cloud.example.com:8080/query?domain=cloud.example.com&path=https://cloud");
        assertEquals(result5, "/query?domain=cloud.example.com&path=https://cloud");
        String result6 = Util.cleanUpURLIfNeeded("https://cloud.example.com:8080/query?domain=cloud.example.com&path=https://cloud#fragment");
        assertEquals(result6, "/query?domain=cloud.example.com&path=https://cloud#fragment");
    }
}
