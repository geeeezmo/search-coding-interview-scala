package albums.challenge

import com.fasterxml.jackson.databind.{Module, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

import scala.jdk.CollectionConverters._

@Configuration
class RestConfig {
  @Bean
  def scalaModule(): Module = DefaultScalaModule

  @Bean
  def restTemplate(builder: RestTemplateBuilder, objectMapper: ObjectMapper): RestTemplate = {
    builder
      .additionalMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper) {
        setSupportedMediaTypes(
          (getSupportedMediaTypes.asScala :+ new MediaType("text", "javascript")).asJava,
        )
      })
      .build()
  }
}
