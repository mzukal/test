#!/bin/bash
# In order to start SC with remote JMX add the following parameters in the command line
#		 -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
#
# you can pass parameters on the command line like
#     -DlogDirectory=c:/temp
# and use them in the sc.properties files as ${sys:logDirectory} and in logback.xml as ${logDirectory}
#
# Adapt this script to optimize JVM parameters for SC
#  -Xmx1024M  allow max 1GB heap size
#  -server    enables server JVM
#
# start sc
java -Xmx1024M -Dlogback.configurationFile=file:../conf/logback-sc.xml -jar sc-${sc.version}.jar -config ../conf/sc.properties
