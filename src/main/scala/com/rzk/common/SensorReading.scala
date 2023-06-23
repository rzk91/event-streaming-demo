package com.rzk.common

import com.rzk.util.implicits.enrichJson
import com.rzk.bots.thresholdmonitor.ThresholdMonitorConfig
import com.typesafe.scalalogging.LazyLogging
import org.apache.flink.api.common.typeinfo.TypeInformation

case class SensorReading(
  timestamp: Long,
  deviceId: String,
  sensor: String,
  value: Double
) extends TimestampedObject

object SensorReading extends LazyLogging {

  def apply(event: Event, config: ThresholdMonitorConfig): Option[SensorReading] =
    event
      .filter(_.deviceId.equalsIgnoreCase(config.deviceId))
      .map(_.content)
      .flatMap { json =>
        for {
          sensor <- json.keySet.find(_.equalsIgnoreCase(config.sensor))
          value  <- json.doubleOption(sensor)
        } yield (sensor, value)
      }
      .fold[Option[SensorReading]] {
        logger.debug(s"[${event.deviceId}] Ignoring $event since it does not satisfy $config")
        None
      } { case (sensor, value) =>
        Some(
          new SensorReading(
            event.timestamp,
            event.deviceId,
            sensor,
            value
          )
        )
      }

  implicit lazy val sensorReadingTypeInfo: TypeInformation[SensorReading] = TypeInformation.of(classOf[SensorReading])
}
