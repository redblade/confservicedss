#!/bin/bash

if [ $# -lt 2 ]
  then
    echo "No arguments"
    exit
fi

echo changing version from $1 to $2

gsed -i -e "s#\"version\": \"$1\"#\"version\": \"$2\"#" ../package.json
gsed -i -e "s#<version>$1</version>#<version>$2</version>#" ../pom.xml
gsed -i -e "s#$APP_NAME:$1#$APP_NAME:$2#" ../Jenkinsfile
gsed -i -e "s#confservice:$1#confservice:$2#g" ../custom_configuration/confservice-deployment.yml
gsed -i -e "s#version: $1#version: $2#g" ../src/main/resources/config/application.yml


