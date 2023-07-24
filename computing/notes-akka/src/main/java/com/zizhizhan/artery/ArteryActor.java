package com.zizhizhan.artery;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;

public class ArteryActor extends AbstractActor {

    public static void main(String[] args) throws IOException {
        Config config = ConfigFactory.load("artery.conf");
        ActorSystem system = ActorSystem.create("artery", config);
        ActorRef actor = system.actorOf(Props.create(ArteryActor.class), "arteryActor");
        actor.tell("hello artery!", ActorRef.noSender());
        // system.terminate();

        System.in.read();
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder().match(String.class, message -> {
            System.out.format("%s received: %s from %s\n", this.self(), message, this.sender());
        }).build();
    }
}
