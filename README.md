# ConfServiceDSS

This file contains the main instruction to build and run ConfServiceDSS


### Development

ConfServiceDSS is based on [JHipster 6.10.5](https://www.jhipster.tech/documentation-archive/v6.10.5) with [this model file](jhipster-jdl.jdl). Before you can build this project, you must install and configure the following dependencies on your machine with minimal version highlighted:

- [OpenJDK](https://openjdk.java.net/) or [AdoptJDK](https://adoptopenjdk.net/) v1.8: Java Developer Kit is used to run the project services
- [Maven](https://maven.apache.org/) v3.3: Maven is used to build the project
- [MySQL](https://www.mysql.com/downloads/) v5.6: MySQL is used to persist configuration and DSS data
- [Node.js](https://nodejs.org/en/) v12.16: Node is used to run a development web server and build the project.
- [npm](https://docs.npmjs.com/) v6.14: npm is used to install Node dependencies and to launch the development front end service

After installing Node, run the following command to install development tools.
You will also need to run this command when dependencies change in [package.json](package.json).

```
npm install
```

Before you run the project, you need a [Kafka](https://kafka.apache.org/) service running and these topics to be available:
- configuration
- sla_violation
- deployment
- deployment_feedback
- benchmarking
- app_profiler

Environment variables for Kafka need to be set, if needed also Mail, for example:

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


### Packaging and launching as jar

To build the final jar and optimize ConfServiceDSS for production, run:

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

To package your ConfServiceDSS as a Docker image and push to a Docker registry, run:

```
mvn package -Pprod jib:dockerBuild -DskipTests=true -Djib.allowInsecureRegistries=true  -Dimage=myregistry/confservicedss

```

To launch your ConfServiceDSS from a Docker image, use env variables (eg. [this](doc/env/confservicedss.env), to be changed) and run:

```
docker run -v $(pwd):/var/tmp --env-file doc/env/confservicedss.env -p8080:8080 confservicedss
```

Once ConfServiceDSS is started the first time, load the basic configuration data (eg. dump_base.sql or dump_kind.sql):

```
mysql -h localhost -D confservice -u root -proot -e "drop table db_lock"
java -cp target/confservicedss-2.4.4.jar -Dloader.main=eu.pledgerproject.confservice.InitDB org.springframework.boot.loader.PropertiesLauncher src/main/resources/config/sql/dump_base.sql localhost 3306 root root
```

Then, navigate to [http://localhost:8080](http://localhost:8080) and login with root/test


For remote debugging you can attach to remote session after launching ConfServiceDSS with the option

```
java -jar target/*.jar -Pprod -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000
```


###### This project has received funding from the European Union’s Horizon 2020 research and innovation programme under grant agreement No 871536.


