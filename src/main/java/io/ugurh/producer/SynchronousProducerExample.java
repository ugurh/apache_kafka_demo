package io.ugurh.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;

public class SynchronousProducerExample {

    public static void main(String[] args) {
        // Set-up mandatory properties
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", "localhost:9092");
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        // Create an object of KafkaProducer and pass in the properties
        KafkaProducer producer = new KafkaProducer<String, String>(kafkaProps);

        // Create a record/message that we want to send to Kafka. Note, in this example we are
        // passing the topic, the key and the value. Recall, that the message key is optional.
        ProducerRecord<String, String> record = new ProducerRecord<>("ugurh-topic", "my-key",
                "my-value");
        try {
            // Send the message to Kafka. The send(...) method returns a Future object for RecordMetadata.
            Future<RecordMetadata> future = producer.send(record);

            // We perform a get() on the future object, which turns the send call synchronous
            RecordMetadata recordMetadata = future.get();

            // The RecordMetadata object contains the offset and partition for the message.
            System.out.println(String.format("Message written to partition %s with offset %s", recordMetadata.partition(),
                    recordMetadata.offset()));

        } catch (Exception e) {
            System.out.println("Exception sending message " + e.getMessage());
        } finally {
            // When you're finished producing records, you can flush the producer to ensure it has all been
            // written to Kafka and then close the producer to free its resources.
            producer.flush();
            producer.close();
        }
    }
}
