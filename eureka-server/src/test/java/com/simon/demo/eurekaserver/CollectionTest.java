package com.simon.demo.eurekaserver;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CollectionTest {

    @Test
    public void testStream(){
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);

        Optional<Integer> optional = list.parallelStream().max(new Comparator(){

            @Override
            public int compare(Object o1, Object o2) {
                Integer int1 = (Integer)o1;
                Integer int2 = (Integer)o2;
                return int2-int1;
            }
        });

        List<Integer> list2 = list.parallelStream().map((t) -> t+1).collect(Collectors.toList());

        System.out.println(optional.get());
        System.out.println(list2);
    }
}
