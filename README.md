## **Producer Configurations**

## ack[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#ack)

The `ack` parameter specifies the number of replicas of a partition that must receive a message before a producer can consider the write successful. We’ll study this concept in greater depth in later chapters. There are only three values that `ack` can take on:

* `ack=0`: Setting ack equal to zero implies the producer doesn’t wait to hear back from the Kafka cluster and assumes each message has been sent successfully. Obviously, this can lead to lost messages but the strategy achieves the highest throughput.
* `ack=1`: In this setting, the producer receives a confirmation once the leader replica receives the message. If the leader crashes and a new leader has not yet been elected, an error is returned to the producer which can retry sending the message. However, the message can still get lost if the leader crashes and a replica is elected as the new leader that has not received the message (known as unclean election). In this setting, the throughput is determined whether the messages are sent synchronously or asynchronously. In the latter case, the throughput is capped by the number of in-flight messages (messages that have been sent but which haven’t yet been received).
* `ack=all`: This setting returns the producer a response once all the replicas write the sent message. This ensures that more than one broker has the message, thus protecting it even in the face of crashes. The increased reliability is accompanied by increased latency as all the replicas must receive the message.

## `buffer.memory`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#buffer.memory)

This is the amount of memory the producer can use to store messages waiting to be sent to the brokers. If messages are produced at a rate faster than the speed at which they can be delivered to the Kafka broker, the producer will backup the messages in the buffer. If the buffer fills up, the producer may get blocked at `send(...)` calls or throw an exception depending on other config settings.

## `compression.type`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#compression.type)

This setting allows the messages being sent to be compressed. Remember, compressing messages means less data is transferred over the network or stored in memory/disk. The trade-off is CPU utilization, which goes up as the messages are compressed and decompressed on the sending (producer) and receiving (consumer) ends respectively. The broker stores the messages from the producer without decompressing them. Additionally, compression also increases the latency to send a message. The different algorithms supported for compression include gzip, lz4, and snappy.

## `retries`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#retries)

The `retries` parameter sets the number of times a producer retries sending a message before giving up and declaring a failure to the client. There are two kinds of failures a producer can encounter.

1. The first are failures that can’t be retried, such as “message too large” errors.
2. The second are the failures which can be retried (e.g. write failure) because of the absence of a partition leader. These failures are automatically retried by the producer and the application logic should not handle them. Rather, the application should only handle the case when retries for transient failures have been exhausted.

Another setting `retry.backoff.ms` is of interest here, which denotes the milliseconds the producer waits before re-attempting a failed send. Generally, the number of `retries` and the wait between the retries should be greater than the time it takes for a broker to recover from a crash, otherwise the producer will declare a failure too soon.

## `batch.size`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#batch.size)

This config setting controls the amount of memory (in bytes). that are batched together to be sent to the same partition. It does not control the number of messages within a batch. The producer doesn’t necessarily wait for the batch to fill to capacity before sending the messages. It may instead send half full batches or even a batch with a single message. A small `batch.size` means the producer will be sending smaller messages at a higher frequency, which adds more overhead.

## `linger.ms`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#linger.ms)

By default, Kafka sends messages in a batch as soon as a send thread becomes available. This can mean sending a batch that has a single message. To improve throughput (at the expense of increasing latency) Kafka can be configured with `linger.ms` milliseconds to wait for additional messages to be assigned to a batch before sending the batch out. A batch is sent out either when it is full or `linger.ms` milliseconds have elapsed.

## `client.id`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#client.id)

This can be any string that identifies a producer. It can be used for metrics, logging, or quotas.

## `max.in.flight.requests.per.connection`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#max.in.flight.requests.per.connection)

This setting represents the number of messages a producer can send without receiving a corresponding response from the brokers. Setting a higher value increases memory usage but also increases throughput. Setting a value too high can reduce throughput as batching becomes inefficient. Setting this value to 1 guarantees messages are written to the brokers in order despite failures and retries.

## `max.request.size`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#max.request.size)

This setting controls the maximum size of a message that can be sent by the producer. It also controls the cumulative size of all the messages in a single request. If set to 1MB, the producer can send a single message of size 1MB or 1000 messages of size 1K each. Note that the broker also has a setting that determines the maximum size of the message the broker will accept. A broker may still reject a request if the message size is larger than what it can accept.

## `max.block.ms`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#max.block.ms)

This setting determines how long the producer will block when calling the methods `send(...)` and `partitionsFor(...)`. These methods block when the producer’s send buffer is full or when the metadata isn’t available.

## `receive.buffer.bytes and send.buffer.bytes`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#receive.buffer.bytes-and-send.buffer.bytes)

These settings control the size of the TCP send and receive buffers used by sockets when writing and reading data. Setting them to -1 sets the sizes to the OS default.

## `timeout.ms`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#timeout.ms)

The config parameter `timeout.ms` represents the milliseconds the broker should wait (on behalf of the producer) to receive a response from in-sync replicas to acknowledge the message sent by the producer so that the `ack` configuration is met.

## `request.timeout.ms`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#request.timeout.ms)

The amount of time the producer will wait to receive a reply from the broker for a sent request. On reaching the timeout, the producer either attempts a retry or raises an error.

## `metadata.fetch.timeout.ms`[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#metadata.fetch.timeout.ms)

The amount of time the producer will wait for a reply when requesting metadata. On reaching the timeout, either a retry is attempted or an error is raised.

## Ordering guarantee[#](https://www.educative.io/courses/scalable-data-pipelines-kafka/myvD42j9603#Ordering-guarantee)

An interesting situation arises when the `retries` is set to non-zero and the `max.in.flight.requests.per.connection` is greater than one. This allows the producer to send out more than one batch of messages without having received acknowledgements for any. Consider the following scenario:

1. Two batches of messages are sent, one after another, destined for the same partition.
2. The first batch of messages fails to be written successfully at the Kafka broker.
3. The second batch gets written successfully.
4. The producer retries sending the first batch again which gets written successfully this time.

The consequence is that the ordering of messages isn’t preserved within the partition, which can be a critical requirement for some applications. In such situations `max.in.flight.requests.per.connection` should be set to 1, though it will severely impact the throughput of the producer.
