package com.zizhizhan.local;

import akka.actor.*;

public class LocalActor extends AbstractActor {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("local");
        ActorRef localActor = system.actorOf(Props.create(LocalActor.class), "localActor");

        ActorSelection arteryActor = system.actorSelection("akka://artery@127.0.0.1:2552/user/arteryActor");
        for (int i = 0; i < 10; i++) {
            arteryActor.tell("Local Call" + i, localActor);
        }

//        ActorSelection remoteActor = system.actorSelection("akka://example@127.0.0.1:50010/user/remoteServerActor");
//        for (int i = 0; i < 10; i++) {
//            remoteActor.tell("Local Call" + i, localActor);
//        }

        // system.terminate();
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, System.out::println).build();
    }
}
