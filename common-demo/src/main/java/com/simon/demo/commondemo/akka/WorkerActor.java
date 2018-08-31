package com.simon.demo.commondemo.akka;

import akka.actor.UntypedAbstractActor;
import com.simon.demo.commondemo.model.Notification;

public class WorkerActor extends UntypedAbstractActor {
    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof Notification) {
            Thread.sleep(1000);
            Notification notification = (Notification) message;
            System.out.println("Sending message to " + notification.getMailbox());
        }
    }
}
