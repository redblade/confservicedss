#!/usr/bin/env groovy

node {
    APP_NAME = "confservicedss"
    BRANCH_NAME = "master"
    DOCKER_IMAGE_TAG = "$APP_NAME:2.5.4b${env.BUILD_ID}"
    MYBUILD_ID = "${env.BUILD_ID}"
    ARTIFACTORY_SERVER = "https://116.203.2.204:443/artifactory/plgregistry/"
    ARTIFACTORY_DOCKER_REGISTRY = "116.203.2.204:443/plgregistry/"
    
    stage('source checkouts') {
        checkout scm
    }

    stage('check java') {
        sh "java -version"
    }

    stage('clean') {
        sh "chmod +x mvnw"
        sh "./mvnw -ntp clean -P-webpack"
    }
    stage('nohttp') {
        sh "./mvnw -ntp checkstyle:check"
    }

    stage('install tools') {
        sh "./mvnw -ntp com.github.eirslett:frontend-maven-plugin:install-node-and-npm -DnodeVersion=v12.16.1 -DnpmVersion=6.14.5"
    }

    stage('npm install') {
        sh "./mvnw -ntp com.github.eirslett:frontend-maven-plugin:npm"
    }

    stage('package image') {
        sh "./mvnw -ntp verify -P-webpack -Pprod -DskipTests"
        archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
    }

    stage('publish  image') {
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'Artifacts', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
            sh "docker login --password=${PASSWORD} --username=${USERNAME} ${ARTIFACTORY_SERVER}"
            sh "./mvnw -ntp jib:build -Djib.allowInsecureRegistries=true -Dimage=$ARTIFACTORY_DOCKER_REGISTRY$DOCKER_IMAGE_TAG"
            sh "docker images"
        }
    }
    
    
    stage('apply Kubernetes deployment') {
        withKubeConfig([credentialsId: 'Jenkins_ServiceAccount' , serverUrl: 'https://192.168.70.5:6443/', namespace:'core']) {
            sh "cat ./custom_configuration/confservice-deployment.yml | sed 's/JENKINS_BUILD/$MYBUILD_ID/g' | kubectl apply -f -"
        }
    }
    
}
