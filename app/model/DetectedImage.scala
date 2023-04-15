package model

case class DetectedImage(url: String,
                         contentType: Option[String] = None,
                         width: Option[Int] = None,
                         height: Option[Int]= None,
                         alt: Option[String] = None)