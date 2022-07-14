FROM ubuntu:20.04

ENV TZ=Europe/Rome
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN apt-get update 

RUN apt-get install nodejs npm -y

RUN npm install -g --unsafe-perm node-red

RUN npm install -g --unsafe-perm  node-red-contrib-kafka-client 

EXPOSE 1880


CMD /usr/local/bin/node-red

