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
You will only need to run this command when dependencies change in [package.json](package.json).

```
npm install
```

Then, before you run the project, you need a [Kafka](https://kafka.apache.org/) service running. These topics need to be present:
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
export SPRING_MAIL_HOST=mymail
export SPRING_MAIL_PORT=myport
export SPRING_MAIL_USERNAME=myuser
export SPRING_MAIL_PASSWORD=mypass

```

After setting up the environment variables, you can run the following commands in two separate terminals for development, to have browser auto-refresh when files change on the hard drive (backend will listen on port 8080, frontend on port 9000)

```

mvn -Dspring.profiles.active=prod -DskipTests=true

npm start
```

The default MySQL configuration expects MySQL to be running on localhost:3306 with user/pass root/root with a schema "confservice" already created. MySQL url, user and pass can be overridden with the following additional environment variables:

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

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files. 

To ensure everything worked, run (after adding the environment variables above):

```

java -jar target/*.jar -Pprod


```

### Packaging and launching as a container

To package your ConfServiceDSS as a local Docker image, run:

```

mvn package -Pprod jib:dockerBuild -DskipTests=true -Dimage=confservicedss 

```

To package your ConfServiceDSS as a Docker image and push to a Docker registry, run:

```

mvn package -Pprod jib:dockerBuild -DskipTests=true -Djib.allowInsecureRegistries=true  -Dimage=myregistry/confservicedss


```

To run your ConfServiceDSS from a Docker image, use env variables (eg. in a file like [this](doc/jhipster/confservicedss.env)) and run

```
docker run --env-file doc/jhipster/confservicedss.env -p8080:8080 confservicedss
```

Once ConfServiceDSS is first started, load the basic configuration data (like [this](src/main/resources/config/sql/dump_base.sql)), then navigate to [http://localhost:8080](http://localhost:8080) and login with root/test

###### This project has received funding from the European Union’s Horizon 2020 research and innovation programme under grant agreement No 871536.


