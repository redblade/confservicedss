# ConfServiceDSS

ConfServiceDSS is a smart orchestrator that provides resource and latency optimisation in edge/cloud multi-clusters.
It provides native support to Kubernetes orchestration and monitoring, and offers more advanced features when working together with the other [Pledger](https://pledger-project.eu/) core components.
 
ConfService and DSS core components are two functional components built as a unique artefact in Java, from now on called "ConfServiceDSS". 
**Please refer to the [doc](doc/README.md) folder for the main documentation**
For more details, please refer to the Pledger website, [deliverables section](https://pledger-project.eu/content/deliverables)


### Setup of the development environment

ConfServiceDSS is based on [JHipster 6.10.5](https://www.jhipster.tech/documentation-archive/v6.10.5) with [this model file](jhipster-jdl.jdl). Before you can build this project, you must install and configure the following dependencies on your machine:

- [OpenJDK](https://openjdk.java.net/) or [AdoptJDK](https://adoptopenjdk.net/) v1.8: Java Developer Kit is used to run the project services
- [Maven](https://maven.apache.org/) v3.3: Maven is used to build the project
- [MySQL](https://www.mysql.com/downloads/) v5.6: MySQL is used to persist configuration and DSS data
- [Node.js](https://nodejs.org/en/) v12.16: Node is used to run a development web server and build the project.
- [npm](https://docs.npmjs.com/) v6.14: npm is used to install Node dependencies and to launch the development front end service

After installing Node, run the following command to install development tools. You will also need to run this command when dependencies change in [package.json](package.json).

```
npm install
```

**Before launching the project, you need a [Kafka](https://kafka.apache.org/) service running** and the following topics to be configured:
- configuration
- sla_violation
- deployment
- deployment_feedback
- benchmarking
- app_profiler

### Building and launching from command line for development purposes:
The project can be imported as a Maven project in Eclipse and compiled. 
Some environment variables must be set before launching ConfServiceDSS: Kafka's are mandatory, Mail's are optional.

```
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_CONSUMER_KEY_DESERIALIZER=org.apache.kafka.common.serialization.StringDeserializer
export KAFKA_CONSUMER_VALUE_DESERIALIZER=org.apache.kafka.common.serialization.JsonDeserializer
export KAFKA_PRODUCER_KEY_SERIALIZER=org.apache.kafka.common.serialization.StringSerializer 
export KAFKA_PRODUCER_VALUE_SERIALIZER=org.springframework.kafka.support.serializer.JsonSerializer

export SPRING_MAIL_BASEURL=mybaseurl
export SPRING_MAIL_FROM=myaddress
export SPRING_MAIL_HOST=myserver
export SPRING_MAIL_PORT=myport
export SPRING_MAIL_USERNAME=myuser
export SPRING_MAIL_PASSWORD=mypass

```

After setting up the environment variables, you can run the following commands in two separate terminals for development, to have browser auto-refresh when files change on the hard drive (backend will listen on port 8080, frontend on port 9000)

```
mvn -Dspring.profiles.active=prod -DskipTests=true
npm start
```

By default, ConfServiceDSS expects MySQL to be running on localhost:3306 with user/pass root/root with a schema "confservice" already created. MySQL parameters can be overridden with the following additional environment variables:

```
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/confservice
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root
```


### Building, packaging and launching as jar

To build the ConfServiceDSS jar optimized for production, run:

```
mvn -Pprod clean package -DskipTests=true 

```

To launch the executable as a jar, run:

```
export $(grep -v '^#' doc/env/confservicedss.env | xargs)
java -jar target/*.jar -Pprod


```

### Packaging and launching as a Docker container

To package your ConfServiceDSS as a local Docker image, run:

```
mvn package -Pprod jib:dockerBuild -DskipTests=true -Dimage=confservicedss 

```

To package your ConfServiceDSS as a Docker image and push it to a Docker registry, run:

```
mvn package -Pprod jib:dockerBuild -DskipTests=true -Djib.allowInsecureRegistries=true  -Dimage=myregistry/confservicedss

```

To launch your ConfServiceDSS from a Docker image, use env variables (eg. [this](doc/env/confservicedss.env), to be changed) and run:

```
docker run -v $(pwd):/var/tmp --env-file doc/env/confservicedss.env -p8080:8080 confservicedss
```

### Initial configuration data (SQL script)

Once ConfServiceDSS is started the first time, load the basic configuration data (eg. dump_base.sql, dump_kind.sql, dump_kind_faredge.sql or any other SQL) into the DB, otherwise there will be no configuration, not even a user defined.
Please note: 
- dump_base.sql is the basic configuration with just users and no infrastructures loaded
- dump_kind.sql is the configuration used for the KinD cloud-edge environment (see doc/kind for details)
- dump_kind_faredge.sql is the configuration used for the KinD cloud-edge-faredge environment (see doc/kind for details)

To load a SQL script, a security table (db_lock) needs first to be dropped, it is used as a safety measure to avoid unwanted overwrites.
An example is provided below:

```
mysql -h localhost -D confservice -u root -proot -e "drop table db_lock"
java -cp target/confservicedss-x.y.z.jar -Dloader.main=eu.pledgerproject.confservice.InitDB org.springframework.boot.loader.PropertiesLauncher src/main/resources/config/sql/dump_base.sql localhost 3306 root root
```

Please note the lock table ''db_lock' is automatically re-created by InitDB; if necessary, it can also be created with 

```
mysql -h localhost -D confservice -u root -proot -e "create table db_lock(id INT);"
```

### Login

Finally, when the sql configuration is loaded, navigate to [http://localhost:9000](http://localhost:9000) and login. 

The SQL dump_* scripts above have:
- username: root 
- password: test


### Remote debugging
For remote debugging you can attach to remote session after launching ConfServiceDSS with the option

```
java -jar target/*.jar -Pprod -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
```


![EU Flag](https://www.consilium.europa.eu/images/img_flag-eu.gif) This work has received funding by the European Commission under grant agreement No. 871536, Pledger project.