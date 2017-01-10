package com.mb

import akka.actor.{ActorSystem, Props}
import com.mb.actors.{InverterActor, PollInverter, Publisher}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern._

import scala.concurrent.Await

object Main extends App {

  val parser = new scopt.OptionParser[Config]("VE.Direct To ThinkSpeak") {
    opt[String]('s', "serialPort") required() action { (serialPort, c) =>
      c.copy(serialPort = Some(serialPort))
    } text "Please specify serialPort"

    opt[String]('k', "apiKey") required() action { (apiKey, c) =>
      c.copy(apiKey = Some(apiKey))
    } text "Please specify PVOutput API Key"

    opt[String]('i', "systemId") required() action { (systemId, c) =>
      c.copy(systemId = Some(systemId))
    } text "Please specify PVOutput System Id"

    opt[Int]('t', "sampleTimeInSeconds") required() action { (t, c) =>
      c.copy(sampleTimeInSeconds = Some(t))
    } text "Please specify sample time in seconds"

  }

  val optionalConfig = parser.parse(args, Config())

  if (optionalConfig.isEmpty) {
    System.exit(0)
  }

  val config = optionalConfig.get

  val system = ActorSystem("reactor")


  // TODO should publish to a router rather than passing in an actor reference
  // TODO: publisher should fail if the API is rejecting us.
  val publisher = system.actorOf(Props(new Publisher(config.apiKey.get, config.systemId.get)), "inverterInteractor")
  val childProps = Props(classOf[InverterActor], config.serialPort.get, publisher)

  val pollTimeInSeconds = config.sampleTimeInSeconds.getOrElse(60).seconds

  val publisherSupervisorProps = BackoffSupervisor.props(
    Backoff.onStop(
      childProps,
      childName = "rxtx",
      minBackoff = 30.seconds,
      maxBackoff = 30.seconds,
      randomFactor = 0.2
    ))

  val publisherSupervisorActor = system.actorOf(publisherSupervisorProps, name = "rxtxSupervisor")

  system.scheduler.schedule(0.seconds, pollTimeInSeconds, publisherSupervisorActor, PollInverter())

  Await.result(system.whenTerminated, Duration.Inf)
}

case class Config(serialPort: Option[String] = None, apiKey: Option[String] = None, systemId: Option[String] = None, sampleTimeInSeconds: Option[Int] = None)