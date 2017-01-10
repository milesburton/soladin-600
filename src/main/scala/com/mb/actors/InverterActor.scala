package com.mb.actors

import java.util

import akka.actor.{Actor, ActorLogging, ActorRef, Stash}
import akka.io.IO
import akka.util.ByteString
import com.mb.soladin.ConvertDataResponseToStatus
import rxtxio.Serial
import rxtxio.Serial._

class InverterActor(port: String, solarStatusListener: ActorRef) extends Actor with Stash with ActorLogging {

  import context.system

  var serialResponseBuffer = List[ByteString]()

  override def preStart = {
    IO(Serial) ! Open(
      port = port,
      baudRate = 9600,
      parity = Serial.NoParity,
      flowControl = Serial.NoFlowControl,
      dataBits = DataBits8,
      stopBits = Serial.OneStopBit)
  }

  override def postStop = {
    log.info("Stopped")
    system.terminate()
  }

  override def receive = {
    case Opened(operator, _) =>
      log.info("Connected to port")
      context become open(operator)
      unstashAll()
    case CommandFailed(_, error) =>
      log.error(s"Could not connect to port: $error")
      context stop self
    case other => stash()
  }


  val REQUEST_DATA = List(0x11, 0x00, 0x00, 0x00, 0xB6, 0x00, 0x00, 0x00, 0xC7).map(_.toByte)
  val RESPONSE_DATA = List(0x00, 0x00, 0x11, 0x00, 0xB6, 0xF3).map(_.toByte)

  def open(operator: ActorRef): Receive = {

    case PollInverter() =>
      log.info("Polling inverter")

      serialResponseBuffer = List()
      operator ! Write(ByteString(REQUEST_DATA.toArray))

    case ReceiveDataResponse(buffer) =>

      log.info(s"RECEIVED DATA RESPONSE: [${toString(buffer)}]")

      val status = ConvertDataResponseToStatus.convert(buffer)

      solarStatusListener ! status

    case Received(data) => {

      serialResponseBuffer = serialResponseBuffer :+ data

      val buffer = serialResponseBuffer.flatten

      buffer match {

        case a if compareDataPreamble(a, RESPONSE_DATA) && a.length == 31 =>
          self ! ReceiveDataResponse(buffer = buffer.toArray)

        case _ =>
      }

    }

    case Closed =>
      log.info("Serial port closed")
      context stop self

    case _ =>
  }


  def toString(arr: Seq[Byte]): String = {
    arr.toArray.map("%02X" format _).mkString(",")
  }


  def compareDataPreamble(subject: Seq[Byte], expected: Seq[Byte]): Boolean = {

    if (subject.length < expected.length) {
      return false
    }

    val trimmedSubject = subject.slice(0, expected.length)

    util.Arrays.equals(trimmedSubject.toArray, expected.toArray)
  }
}


case class PollInverter()

case class ReceiveDataResponse(buffer: Array[Byte])

