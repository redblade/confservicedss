#!/usr/bin/env groovy

node {
    APP_NAME = "confservicedss"
    BRANCH_NAME = "master"
    DOCKER_IMAGE_TAG = "$APP_NAME:2.4.4b${env.BUILD_ID}"
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
        sh "mvn -ntp clean -P-webpack"
    }
    stage('nohttp') {
        sh "mvn -ntp checkstyle:check"
    }

    stage('install tools') {
        sh "mvn -ntp com.github.eirslett:frontend-maven-plugin:install-node-and-npm -DnodeVersion=v12.16.1 -DnpmVersion=6.14.5"
    }

    stage('npm install') {
        sh "mvn -ntp com.github.eirslett:frontend-maven-plugin:npm"
    }

    stage('package image') {
        sh "mvn -ntp verify -P-webpack -Pprod -DskipTests"
        archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
    }

    stage('publish  image') {
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'Artifacts', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
            sh "docker login --password=${PASSWORD} --username=${USERNAME} ${ARTIFACTORY_SERVER}"
            sh "mvn -ntp jib:build -Djib.allowInsecureRegistries=true -Dimage=$ARTIFACTORY_DOCKER_REGISTRY$DOCKER_IMAGE_TAG"
            sh "docker images"
        }
    }
    
    
    
}