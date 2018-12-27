# xke-kingof-scaling

This project present a usage of the customs metrics API from [Kubernetes](https://kubernetes.io/) to apply autoscaling 
with [Kafka-Streams](https://kafka.apache.org/documentation/streams/) apps based on the consumer lag information. 

|About  | Requirement  | Modules  | Setup  | Usage  | Result  | Backlog  |
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|[↓](#about)  |[↓](#requirement)   |[↓](#modules)   |[↓](#setup)  |[↓](#usage)   |[↓](#result) |[↓](#backlog)|  

## [About](#about)
The project is an example from the talk: _Scale in and out with kafka-streams on kubenetes_. It was first given at the
2018 edition of [Xebicon](https://xebicon.fr), the Xebia-France Conference. 
[Slides](https://speakerdeck.com/loicdivad/scale-out-with-kafka-streams-and-kubernetes) and 
[Video](https://www.youtube.com/watch?v=gf1PJ7SJ55s) (FR) are available.

## [Requirement](#requirement)
In order to use this project and enjoy the power of autoscaling on custom metrics a few tools are necessary:
- JDK 8 [:arrow_down: installation page](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Gradle [:arrow_down: installation page](https://gradle.org/install/)
- Docker [:arrow_down: installation page](https://docs.docker.com/install/)
- Kubctl [:arrow_down: installation page](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
- Terraform [:arrow_down: installation page](https://www.terraform.io/downloads.html)
- a Google Cloud account [:credit_card: login page](https://cloud.google.com/)
- a Confluent Cloud account [:credit_card: login page](https://confluent.cloud/login)

## [Modules](#modules)

### datagen
`kos-datagen` is a generator of events. It creates an actors system where actors publish events directly to kafka with 
[akka-stream-kafka](https://doc.akka.io/docs/akka-stream-kafka/current/home.html). We want to scaling out our deployment
to keep up with an intensive flow. So we will produce a lot of events in a minimum of time period.

### streams
`kos-streams` is the streaming application. It reads events from two input topics, join them, decode 
hexadecimal to plain scala object and aggregate theme. Here is one of the latest verison of the topology: 
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
The JMX metrics are exported via [jmx-export](https://github.com/prometheus/jmx_exporter) in a prometheus format. 
On the [config.yaml](./kos-streams/docker/config.yaml) we focus on the lag metric corresponding to each partitions of 
the two input topics:

```yaml
  - pattern: "kafka.consumer<type=consumer-fetch-manager-metrics, client-id=(.*), topic=GAME-FRAME-RS, partition=(.*)><>records-lag: (.*)"
    labels: { client: $1, partition: $2, topic: GAME-FRAME-RS, metric: records-lag }
    name: "consumer_lag_game_frame_rs"
    type: GAUGE
```
_Note: the type `GAUGE` is required for the rest of the experiment_

### common
`kow-common` is library imported the two first modules. It contains the encoding use by the actors to send frames
in each event. And it contains the avro schema of the decoded frame create by the streaming app.

```scala
// -- encode a event

val event = Hit(key = O(), direction = None, impact = Critical(42), level = NewBie(), game = Neowave())

Codec.encode(event) // res0: Attempt[scodec.bits.BitVector] = Successful(BitVector(56 bits, 0xc3004f2a00d9a5))

// -- decode a event

val frame = "c3004f2a00d9a5" // frame: String = c3004f2a00d9a5

Hit.decode(frame) // res1: Option[scodec.DecodeResult[fr.xebia.ldi.common.frame.Hit]] = 
// Some(DecodeResult(Hit(O(),None,Critical(42),None,NewBie(),Neowave()),BitVector(empty)))

// -- convert a event

val message = event.asJava // message: fr.xebia.ldi.common.schema.Hit = 
// {"impact": 42, "key": "O", "direction": null, "impactType": "Critical", "level": "NewBie", "game": "Neowave"}
```

## [Setup](#setup)

#### GCP 
First and foremost, to deploy anything we will need a k8s cluster.
[Create a service account keys](https://cloud.google.com/iam/docs/creating-managing-service-account-keys) 
for your project. Rename the key to `gcp-credentials.json`. Move the key to `.terraform`. The following command should
start a dry-run of your Kubernetes cluster with GCP.

```sell
terraformm plan
```
_Note: GKE version 1.11 integrate the notion of custom metrics_

#### CCloud

[Confluent Cloud](https://confluent.cloud) let you create a Kafka clusters on the cloud provider of your choice.
During this procedure you will get a `bootstrap-servers`, a `api-key` and a `secret-key`. Run the entry point 
`Secrets.scala` with the env variable:  
- API_KEY=`api-key`  
- SECRET_KEY=`secret-key`  
- BOOTSTRAP_SERVERS=`bootstrap-servers`  

This will create de secret-maps with all the secrets encode in base64 in the file `./kubernetes/.secrets.yaml`.

#### Kubctl

```shell
gcloud container clusters get-credentials #<your-cluster-name>
```

```shell 
kubectl config get-contexts
```

```
kubectl create --filename ./kubernetes/.secrets.yaml
```

## [Usage](#usage)

## [Result](#result)

## [Backlog](#backlog)
- [ ] Add the tests case
- [ ] Configure the throughput at run time


