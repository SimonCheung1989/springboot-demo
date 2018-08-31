package com.simon.demo.commondemo.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import com.simon.demo.commondemo.model.Notification;

public class MasterActor extends UntypedAbstractActor {

    private ActorRef workerActor;

    static public Props props(ActorRef workerActor) {
        return Props.create(MasterActor.class, () -> new MasterActor(workerActor));
    }

    private MasterActor(ActorRef workerActor) {
        this.workerActor = workerActor;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof Notification) {
            Thread.sleep(100);
            System.out.println("do something...[" + ((Notification) message).getUserId() + "]");
            workerActor.tell(message, getSelf());
        }


    }
}
