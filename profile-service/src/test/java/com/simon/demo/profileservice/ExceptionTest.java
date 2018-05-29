package com.simon.demo.profileservice;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class ExceptionTest {

    @Test
    public void testException(){

        try{
            String str = null;
            System.out.println(str.toString());
        } catch (NullPointerException e) {
            System.out.println("NullpointerException");
            throw e;
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }

    }

    @Test
    public void testTime(){
        Long expireValue = 1527559851 * 1000L;

        java.util.Date expiredAt = new java.util.Date(expireValue);
        Calendar cal = Calendar.getInstance();


        //Default has 2 minutes buffer
        long buffer = 2 * 60 * 1000L;

        if (cal.getTimeInMillis() - expiredAt.getTime() < buffer ) {
            System.out.println(cal.getTimeInMillis() - expiredAt.getTime());
        }

        System.out.println(expiredAt.getTime());
        System.out.println(cal.getTimeInMillis());
        System.out.println(System.currentTimeMillis());
    }
}
