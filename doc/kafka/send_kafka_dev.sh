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
      print ('send_kafka_dev.py -t <topic> -f <jsonfile>')
      sys.exit(2)
   if len(opts) == 0:
      print ('send_kafka_dev.py -t <topic> -f <jsonfile>')
      sys.exit()

   for opt, arg in opts:
      if opt == '-h':
         print ('send_kafka_dev.py -t <topic> -f <jsonfile>')
         sys.exit()
      elif opt in ("-t", "--topic"):
         topic = arg
      elif opt in ("-f", "--jsonfile"):
         jsonfile = arg
         with open(jsonfile, 'r') as file:
            msg_json = json.load(file)

         producer = KafkaProducer(bootstrap_servers='localhost:9092',
                         value_serializer=lambda x: 
                         dumps(x).encode('utf-8'))
         producer.send(topic, msg_json)


if __name__ == "__main__":
   main(sys.argv[1:])
