The Python3 scripts in this folders allows to send msg on Kafka (dev/prod)
They require kafka-python to be installed (pip3 install kafka-python)


HOWTO get ca, cert and key from JKS
keytool (and openssl) are needed
sudo apt install openjdk-11-jre-headless -y


1) extract ca.pem
keytool -exportcert -alias caroot -keystore kafka.client.truststore.jks -rfc -file ca.pem

2) extract cert.pem
keytool -exportcert -alias cert -keystore kafka.client.keystore.jks -rfc -file cert.pem

3) extract key.pem
keytool -importkeystore -srckeystore kafka.client.keystore.jks -destkeystore new-store.p12 -deststoretype PKCS12
openssl pkcs12 -in new-store.p12 -nodes -nocerts -out key.pem
rm new-store.p12

