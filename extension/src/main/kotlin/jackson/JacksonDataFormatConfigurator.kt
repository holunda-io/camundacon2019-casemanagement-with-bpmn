package io.holunda.extension.casemanagement.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat
import org.camunda.spin.spi.DataFormatConfigurator
import java.text.SimpleDateFormat

class JacksonDataFormatConfigurator : DataFormatConfigurator<JacksonJsonDataFormat> {
  override fun configure(dataFormat: JacksonJsonDataFormat) {
    configureObjectMapper(dataFormat.objectMapper)
  }

  override fun getDataFormatClass(): Class<JacksonJsonDataFormat> {
    return JacksonJsonDataFormat::class.java
  }
}



fun configureObjectMapper(objectMapper: ObjectMapper = ObjectMapper()) = objectMapper.apply {
  registerModule(KotlinModule())
  registerModule(Jdk8Module())
  registerModule(JavaTimeModule())
  dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
  disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}
