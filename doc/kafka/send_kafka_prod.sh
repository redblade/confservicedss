#!/usr/bin/env python3

from pathlib import Path
from kafka import KafkaProducer
from json import dumps

import json
import sys, getopt

def main(argv):
   topic = ''
   datafile = ''
   try:
      opts, args = getopt.getopt(argv,"ht:f:",["topic=","jsonfile="])
   except getopt.GetoptError:
      print ('send_kafka_prod.py -t <topic> -f <jsonfile>')
      sys.exit(2)
   if len(opts) == 0:
      print ('send_kafka_prod.py -t <topic> -f <jsonfile>')
      sys.exit()

   for opt, arg in opts:
      if opt == '-h':
         print ('send_kafka_prod.py -t <topic> -f <jsonfile>')
         sys.exit()
      elif opt in ("-t", "--topic"):
         topic = arg
      elif opt in ("-f", "--jsonfile"):
         jsonfile = arg
         with open(jsonfile, 'r') as file:
            msg_json = json.load(file)
            
         kafkaBrokers='static.180.8.203.116.clients.your-server.de:9093,static.194.8.203.116.clients.your-server.de:9093,static.240.8.203.116.clients.your-server.de:9093'
         caRootLocation='../../../confservicedss_private/kafka/private/ca.pem'
         certLocation='../../../confservicedss_private/kafka/private/cert.pem'
         keyLocation='../../../confservicedss_private/kafka/private/key.pem'
         password='De!@673412)$('

         producer = KafkaProducer(
                          security_protocol='SSL',
                          bootstrap_servers=kafkaBrokers,
                          ssl_cafile=caRootLocation,
                          ssl_certfile=certLocation,
                          ssl_keyfile=keyLocation,
                          ssl_password=password,
                          value_serializer=lambda x: dumps(x).encode('utf-8')
         )
         producer.send(topic, msg_json)


if __name__ == "__main__":
   main(sys.argv[1:])
