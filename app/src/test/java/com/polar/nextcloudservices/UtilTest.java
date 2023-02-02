package com.polar.nextcloudservices;

import org.junit.Test;

import static org.junit.Assert.*;

import static org.junit.Assert.*;


public class UtilTest {
    @Test
    public void testURLCleanup(){
        String result1 = Util.cleanUpURLIfNeeded("https://cloud.example.com/query?domain=cloud.example.com&path=https://cloud");
        assertEquals(result1, "/query?domain=cloud.example.com&path=https://cloud");
        String result2 = Util.cleanUpURLIfNeeded("cloud.example.com/query?domain=cloud.example.com&path=https://cloud");
        assertEquals(result2, "/query?domain=cloud.example.com&path=https://cloud");
        String result3 = Util.cleanUpURLIfNeeded( "https://cloud.example.com/query?domain=cloud.example.com&path=https://cloud");
        assertEquals(result3, "/query?domain=cloud.example.com&path=https://cloud");
        String result4 = Util.cleanUpURLIfNeeded( "https://cloud.example.com:8080/query?domain=cloud.example.com&path=https://cloud.example.com:8080");
        assertEquals(result4, "/query?domain=cloud.example.com&path=https://cloud.example.com:8080");
    }
}
