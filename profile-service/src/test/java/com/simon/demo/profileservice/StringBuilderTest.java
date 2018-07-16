package com.simon.demo.profileservice;

import org.junit.Test;

import java.nio.ByteBuffer;

public class StringBuilderTest {

    @Test
    public void testBuilder(){
        byte[] bytes = ByteBuffer.allocate(0).array();
        System.out.println(bytes.length);

        System.out.println(bytes.clone());
    }
}
