package com.simon.demo.profileservice;

import io.reactivex.Flowable;
import org.junit.Test;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RxjavaTest {

    @Test
    public void testFlowable() {
        Flowable.just("Hello world").subscribe(System.out::println);
        Flowable.just("Item1", "Item2").subscribe(x -> System.out.println(x));

        int i = 0;
        while (true) {
            Flowable.just(i).subscribe(x -> {
                Thread.sleep(10000);
                System.out.println("x:" + x);
            });
            i++;
            System.out.println(i);
        }
    }
//
//    @Test
//    public void testObservable() {
//        Observable.just("Hello world").subscribe(System.out::println);
//
//        Observable observable = Observable.create((subscriber) -> {
//            try {
//                Thread.sleep(10000);
//            } catch (Exception e) {
//
//            }
//            subscriber.onNext("Hello world");
//
//            subscriber.onCompleted();
//        });
//
//        observable.subscribe(System.out::println);
//        System.out.println("end");
//    }
//
//    @Test
//    public void testSingle() {
//        System.out.println(Thread.currentThread());
//
//        Single.just("Hello").subscribe(x -> {
//            System.out.println(Thread.currentThread() + ":" + x);
//        });
//        Scheduler scheduler = Schedulers.computation();
//        Scheduler.Worker worker  = scheduler.createWorker();
//
//        Action0 innerAction = () -> {
//            try {
//                Thread.sleep(1000);
//            }catch (Exception e){
//
//            }
//            System.out.println(Thread.currentThread() + ":" + "innerAction");
//        };
//
//        Action0 outerAction = () -> {
//            try {
//                Thread.sleep(1000);
//            }catch (Exception e){
//
//            }
//            System.out.println(Thread.currentThread() + ":" + "outerAction");
//            worker.schedule(innerAction);
//        };
//
//        worker.schedule(outerAction);
//
//        Single.just("Hello world").subscribeOn(scheduler).subscribe(x -> {
//            System.out.println(Thread.currentThread() + ":" + x);
//        });
//
//
//        while (true);
//    }
//
//    @Test
//    public void testObservableFrom() {
//        List<String> list = new ArrayList();
//        list.add("Item 1");
//        list.add("Item 2");
//        list.add("Item 3");
//
//        Observable.from(list).subscribe(x -> {
//            System.out.println(x);
//            try {
//                Thread.sleep(1000);
//            } catch (Exception e) {
//
//            }
//        });
//    }
//
//    @Test
//    public void testObservableDefer() {
////        Observable.defer()
//    }
//
//    @Test
//    public void testObservableInterval() {
//        Observable observable = Observable.interval(10, TimeUnit.SECONDS);
//
//        observable.subscribe(x -> {
//            System.out.println("x:" + x);
//        });
//
//    }
//
//    @Test
//    public void testObservableRange() {
//        Observable observable = Observable.range(10, 5);
//        observable.subscribe(x -> {
//            System.out.println(x);
//        });
//    }
//
//    @Test
//    public void testObservableTimer() {
//        Observable observable = Observable.timer(3, TimeUnit.SECONDS);
//        observable.subscribe(x -> {
//            System.out.println("----");
//        });
//    }
//
//    @Test
//    public void testObservableRepeat() {
//        Observable observable = Observable.just("Item").repeat(5);
//        observable.subscribe(x -> {
//            System.out.println(x);
//        });
//    }
//
//    @Test
//    public void testObservableMap() {
//        Observable observable = Observable.just("10");
//        observable.map(new Func1<String, Integer>() {
//            @Override
//            public Integer call(String o) {
//                return Integer.parseInt(o) + 1;
//            }
//        }).subscribe(x -> {
//            System.out.println(x);
//        });
//    }

    @Test
    public void testOptional(){
        Optional<String> optional = Optional.ofNullable("abc");
        System.out.println(optional.isPresent());
        System.out.println(optional.get());
        System.out.println(optional.orElse("Default"));
        System.out.println(optional.map(x -> x+"aa").orElse("Default"));
    }

    @Test
    public void testStream(){
        List<Map<String, Object>> list = new ArrayList<>();
        for(int i=0; i<1000; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("no", i);
            map.put("value", i);
            list.add(map);
        }

        List<Integer> l = list.parallelStream()
                .filter(x -> (int)x.get("no") > 0)
                .map(x -> (int)x.get("value"))
                .sorted((Integer o1, Integer o2) -> o2-o1).collect(Collectors.toList());

        System.out.println(l);

        Integer total = list.parallelStream()
                .filter(x -> (int)x.get("no") > 998)
                .map(x -> (int)x.get("value")).reduce(0, Integer::sum);

        System.out.println(total);
    }

}
