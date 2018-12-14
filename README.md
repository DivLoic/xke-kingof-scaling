xke-kingof-scaling
---------
This project present a usage of the customs metrics API from Kubernetes to apply autoscaling to kafka-streams apps based
on the consumer lag information. 

1. [About](#about)
2. [Requirement](#requirement)
3. [Modules](#modules)
4. [Setup](#setup)
5. [Usage](#usage)
6. [Result](#result)
7. [Backlog](#backlog)
 
## [About](#about)
The project is an experiment from the talk: Scale in and out with kafka-streams on kubenetes. It was first given at the
2018 edition of the Xebicon, the Xebia France Conference.

## [Requirement](#requirement)
In order to run the experiment and enjoy the power of autoscaling on custom metrics a few 
- [ ] JDK 8
- [ ] Gradle
- [ ] Docker
- [ ] Kubctl
- [ ] Terraform
- [ ] a Google Cloud account
- [ ] a Confluent Cloud account

## [Modules](#modules)

### datagen
`kos-datagen` is a generator of events. It creates un actors system where actors publish events directly to kafka with 
[akka-stream-kafka](...). The all point of scaling out our deployment is to keep up with an intensive flow. 
So we will poduce a lot of events in a minimum of time.

### streams
`kow-streams` is the streaing application. It reads events from two input topics, join them, decode 
hexadecimal to plain scala object and aggregate theme. (...) Here is one of the latest verison of the topology: 
```
Topologies:
   Sub-topology: 0
    Source: KSTREAM-SOURCE-0000000000 (topics: [GAME-FRAME-RQ]) --> KSTREAM-WINDOWED-0000000002
    Source: KSTREAM-SOURCE-0000000001 (topics: [GAME-FRAME-RS]) --> KSTREAM-WINDOWED-0000000003
    Processor: KSTREAM-WINDOWED-0000000002 (stores: [KSTREAM-JOINTHIS-0000000004-store]) --> KSTREAM-JOINTHIS-0000000004 <-- KSTREAM-SOURCE-0000000000
    Processor: KSTREAM-WINDOWED-0000000003 (stores: [KSTREAM-JOINOTHER-0000000005-store]) --> KSTREAM-JOINOTHER-0000000005 <-- KSTREAM-SOURCE-0000000001
    Processor: KSTREAM-JOINOTHER-0000000005 (stores: [KSTREAM-JOINTHIS-0000000004-store]) --> KSTREAM-MERGE-0000000006 <-- KSTREAM-WINDOWED-0000000003
    Processor: KSTREAM-JOINTHIS-0000000004 (stores: [KSTREAM-JOINOTHER-0000000005-store]) --> KSTREAM-MERGE-0000000006 <-- KSTREAM-WINDOWED-0000000002
    Processor: KSTREAM-MERGE-0000000006 (stores: []) --> KSTREAM-FLATMAPVALUES-0000000007 <-- KSTREAM-JOINTHIS-0000000004, KSTREAM-JOINOTHER-0000000005
    Processor: KSTREAM-FLATMAPVALUES-0000000007 (stores: []) --> KSTREAM-AGGREGATE-0000000009 <-- KSTREAM-MERGE-0000000006
    Processor: KSTREAM-AGGREGATE-0000000009 (stores: [KSTREAM-AGGREGATE-STATE-STORE-0000000008]) --> KTABLE-TOSTREAM-0000000010 <-- KSTREAM-FLATMAPVALUES-0000000007
    Processor: KTABLE-TOSTREAM-0000000010 (stores: []) --> KSTREAM-SINK-0000000011 <-- KSTREAM-AGGREGATE-0000000009
    Sink: KSTREAM-SINK-0000000011 (topic: SESSIONS) <-- KTABLE-TOSTREAM-0000000010
``` 
The JMX metrics are exported via [jmx-export]() in a prometheus format. On the [
config.yaml](./kos-streams/docker/config.yaml) we focus on the lag metric corresponding to each partions of the two 
input topics. Note that the type `GAUGE` is required for the 

```yaml
  - pattern: "kafka.consumer<type=consumer-fetch-manager-metrics, client-id=(.*), topic=GAME-FRAME-RS, partition=(.*)><>records-lag: (.*)"
    labels: { client: $1, partition: $2, topic: GAME-FRAME-RS, metric: records-lag }
    name: "consumer_lag_game_frame_rs"
    type: GAUGE
```

### common

## [Setup](#setup)

## [Usage](#usage)

## [Result](#result)

## [Backlog](#backlog)
- [ ] Add the tests case
- [ ] Configure the throughput at run time
- [ ]  

