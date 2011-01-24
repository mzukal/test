# In order to start SC with remote JMX add the following parameters in the command line
#		 -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
#
# you can pass parameters on the command line like
#     -DlogDirectory=c:/temp
# and use them in the sc.properties files as ${sys:logDirectory} and in log4j.properties as ${logDirectory}
#
# start sc
java -Dlog4j.configuration=file:../config/log4j-sc.properties -jar ../bin/sc.jar -config ../config/sc.properties