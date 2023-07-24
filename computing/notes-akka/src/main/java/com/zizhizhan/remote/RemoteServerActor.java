package com.zizhizhan.remote;

import akka.actor.AbstractActor;

public class RemoteServerActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, message -> {
            System.out.format("%s received: %s\n", this.self(), message);
        }).build();
    }
}