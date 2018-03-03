package com.example.kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class WordCountApplication implements CommandLineRunner {

  @Value("${application.id.config:streams-wordcount}")
  private String applicationId;

  @Value("${bootstrap.servers.config:192.168.33.10:9092}")
  private String bootstrapServers;

  @Value("${source.topic:streams-plaintext-input}")
  private String sourceTopic;

  @Value("${sink.topic:streams-wordcount-output}")
  private String sinkTopic;

  public static void main(String[] args) {
    SpringApplicationBuilder builder = new SpringApplicationBuilder(WordCountApplication.class);
    builder
      .web(WebApplicationType.NONE)
      .bannerMode(Mode.OFF)
      .run(args);
  }

  @Override
  public void run(String... args) {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

    final StreamsBuilder builder = new StreamsBuilder();

    builder.<String, String>stream(sourceTopic)
      .flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
      .groupBy((key, value) -> value)
      .count(Materialized.as("counts-store"))
      .toStream()
      .to(sinkTopic, Produced.with(Serdes.String(), Serdes.Long()));

    final Topology topology = builder.build();
    final KafkaStreams streams = new KafkaStreams(topology, props);
    final CountDownLatch latch = new CountDownLatch(1);

    // attach shutdown handler to catch control-c
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      streams.close();
      latch.countDown();
    }, "streams-shutdown-hook"));

    try {
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }

    System.exit(0);
  }
}
