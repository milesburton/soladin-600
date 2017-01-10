package com.mb.soladin

import org.scalatest.{FlatSpec, Matchers}


class convertDataResponseToStatus$Test extends FlatSpec with Matchers {

  it should "convert status response" in {

    val EXAMPLE_RESPONSE = List(0x00, 0x00, 0x11, 0x00, 0xB6, 0xF3, 0x00, 0x00, 0xE3, 0x03, 0x06, 0x00, 0x90, 0x13, 0xF3, 0x00, 0x00, 0x00, 0x04, 0x00, 0xA9, 0x3B, 0x00, 0x13, 0x34, 0xA3, 0x02, 0x00, 0x00, 0x00, 0x10).map(_.toByte).toArray
    val buffer = EXAMPLE_RESPONSE


    val expected = SolarStatus(
      solarVoltage = 995,
      solarPowerWatts = 6,
      gridFrequencyHz = 5008,
      gridVoltageRms = 243,
      gridOutputWatts = 4,
      gridOutputWattsTotal = 15273,
      inverterTemperature = 19,
      inverterRuntimeMinutes = 172852
    )

    val actual = ConvertDataResponseToStatus.convert(buffer)

    actual should be(expected)

  }

}
