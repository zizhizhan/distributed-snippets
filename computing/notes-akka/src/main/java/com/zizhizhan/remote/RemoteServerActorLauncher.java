package com.zizhizhan.remote;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class RemoteServerActorLauncher {
    public static void main(String[] args) {
        Config config = ConfigFactory.load("remote.conf");
        ActorSystem actorSystem = ActorSystem.create("remote", config);
        ActorRef actor = actorSystem.actorOf(Props.create(RemoteServerActor.class), "remoteServerActor");
        actor.tell("hello!", ActorRef.noSender());
    }
}
