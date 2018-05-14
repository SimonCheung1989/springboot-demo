package com.simon.demo.profileservice;

import org.junit.Test;

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
}
