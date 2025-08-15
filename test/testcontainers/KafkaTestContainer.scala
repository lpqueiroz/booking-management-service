package testcontainers

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.util.Properties
import scala.jdk.CollectionConverters._

trait KafkaTestContainer {

  val kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
  kafkaContainer.start()

  val bootstrapServers: String = kafkaContainer.getBootstrapServers
  println(s"Kafka running at: $bootstrapServers")

  val props = new Properties()
  props.put("bootstrap.servers", bootstrapServers)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)

  producer.send(new ProducerRecord[String, String]("test-topic", "key1", "value1"))
  producer.flush()
  producer.close()

  val consumerProps = new Properties()
  consumerProps.put("bootstrap.servers", bootstrapServers)
  consumerProps.put("group.id", "test-group")
  consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  consumerProps.put("auto.offset.reset", "earliest")

  val consumer = new KafkaConsumer[String, String](consumerProps)
  consumer.subscribe(List("test-topic").asJava)

  val records = consumer.poll(Duration.ofSeconds(5))
  records.asScala.foreach { r =>
    println(s"Consumed ${r.key} -> ${r.value}")
  }

  consumer.close()
}
