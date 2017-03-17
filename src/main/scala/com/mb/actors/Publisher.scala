package com.mb.actors

import akka.actor.{Actor, ActorLogging, PoisonPill, Stash}
import com.mb.soladin.SolarStatus
import org.joda.time.DateTime
import skinny.http.{HTTP, Request}


class Publisher(val key: String, val systemId: String) extends Actor with Stash with ActorLogging {

  override def receive: Receive = {

    case ss: SolarStatus =>

      //http://skinny-framework.org/documentation/http-client.html
      val request = Request("http://pvoutput.org/service/r2/addstatus.jsp")
        .header("X-Pvoutput-Apikey",key)
        .header("X-Pvoutput-SystemId",systemId)
        .formParams("d" -> new DateTime().toString("yyyyMMdd"),
          "t" -> new DateTime().toString("HH:mm"),
          "v2"->ss.gridOutputWatts,
          "v5"->ss.inverterTemperature,
          "v6"->ss.solarVoltage/10)

      val response = HTTP.post(request)

      // REF: http://pvoutput.org/help.html#api
      response.status match {
        case ok if ok == 200 =>
          log.info("Data Published")

        case unauthorised if unauthorised == 401 =>
          log.error("Invalid credentials. Please validate")
          self ! PoisonPill

        case forbidden if forbidden == 403 =>
          log.error("Validate access to the API.")
          self ! PoisonPill

        case _ =>
          log.error("Unknown status received")
          log.error(response.status.toString)
          log.error(new String(response.body))
          log.error(response.headers.mkString(","))

          self ! PoisonPill

      }

    case _ =>
  }

}
