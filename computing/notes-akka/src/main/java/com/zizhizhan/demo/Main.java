package com.zizhizhan.demo;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Main {
    public static class PrintActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class, System.out::println)
                    .build();
        }
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("example");
        ActorRef printActor = system.actorOf(Props.create(PrintActor.class), "printActor");
        printActor.tell("Hello, Akka!", ActorRef.noSender());
    }
}