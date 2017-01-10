package com.mb.soladin

object ConvertDataResponseToStatus {

  def convert(buffer: Array[Byte]): SolarStatus = {

    val signedBuffer = buffer.map(Unsigned.toInt)

    val solarVoltage = signedBuffer(9) << 8 | signedBuffer(8)
    val solarPowerWatts = signedBuffer(11) << 8 | signedBuffer(10)
    val gridFrequencyHz = (signedBuffer(13) << 8) | signedBuffer(12)
    val gridVoltageRms = signedBuffer(15) << 8 | signedBuffer(14)
    val gridOutputWatts = signedBuffer(19) << 8 | signedBuffer(18)
    val gridOutputWattsTotal = signedBuffer(22) << 16 | signedBuffer(21) << 8 | signedBuffer(20)
    val inverterTemperature = signedBuffer(23)
    val inverterRuntimeMinutes = signedBuffer(26) << 16 | signedBuffer(25) << 8 | signedBuffer(24)

    SolarStatus(
      solarVoltage = solarVoltage,
      solarPowerWatts = solarPowerWatts,
      gridFrequencyHz = gridFrequencyHz,
      gridVoltageRms = gridVoltageRms,
      gridOutputWatts = gridOutputWatts,
      gridOutputWattsTotal = gridOutputWattsTotal,
      inverterTemperature = inverterTemperature,
      inverterRuntimeMinutes = inverterRuntimeMinutes
    )
  }

}

case class SolarStatus(
                        solarVoltage: Int = 0,
                        solarPowerWatts: Int = 0,
                        gridFrequencyHz: Int = 0,
                        gridVoltageRms: Int = 0,
                        gridOutputWatts: Int = 0,
                        gridOutputWattsTotal: Int = 0,
                        inverterTemperature: Int = 0,
                        inverterRuntimeMinutes: Int = 0
                      )



object Unsigned {

  def toInt(byte: Byte): Int = java.lang.Byte.toUnsignedInt(byte)

}