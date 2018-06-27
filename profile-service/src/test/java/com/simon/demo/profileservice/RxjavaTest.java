package com.simon.demo.profileservice;

import io.reactivex.Flowable;
import org.junit.Test;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RxjavaTest {

    @Test
    public void testFlowable(){
        Flowable.just("Hello world").subscribe(System.out::println);
        Flowable.just("Item1", "Item2").subscribe(x -> System.out.println(x));

        int i =0;
        while(true) {
            Flowable.just(i).subscribe(x -> {
                Thread.sleep(10000);
                System.out.println("x:"+x);
            });
            i ++;
            System.out.println(i);
        }
    }

    @Test
    public void testObservable() {
        Observable.just("Hello world").subscribe(System.out::println);

        Observable observable = Observable.create((subscriber) -> {
            try {
                Thread.sleep(10000);
            } catch (Exception e){

            }
            subscriber.onNext("Hello world");

            subscriber.onCompleted();
        });

        observable.subscribe(System.out::println);
        System.out.println("end");
    }

    @Test
    public void testSingle(){
        Single.just("Hello world").subscribe(System.out::println);
    }

    @Test
    public void testObservableFrom(){
        List<String> list = new ArrayList();
        list.add("Item 1");
        list.add("Item 2");
        list.add("Item 3");

        Observable.from(list).subscribe(x -> {
            System.out.println(x);
            try {
                Thread.sleep(1000);
            }catch (Exception e){

            }
        });
    }

    @Test
    public void testObservableDefer(){
//        Observable.defer()
    }

    @Test
    public void testObservableInterval(){
        Observable observable = Observable.interval(10, TimeUnit.SECONDS);

        observable.subscribe(x -> {
            System.out.println("x:" + x);
        });

    }

    @Test
    public void testObservableRange(){
        Observable observable = Observable.range(10, 5);
        observable.subscribe(x -> {
            System.out.println(x);
        });
    }

    @Test
    public void testObservableTimer(){
        Observable observable = Observable.timer(3, TimeUnit.SECONDS);
        observable.subscribe(x -> {
            System.out.println("----");
        });
    }

    @Test
    public void testObservableRepeat(){
        Observable observable = Observable.just("Item").repeat(5);
        observable.subscribe(x -> {
            System.out.println(x);
        });
    }

    @Test
    public void testObservableMap(){
        Observable observable = Observable.just("10");
        observable.map(new Func1<String, Integer>() {
            @Override
            public Integer call(String o) {
                return Integer.parseInt(o) + 1;
            }
        }).subscribe(x -> {
            System.out.println(x);
        });
    }
}
