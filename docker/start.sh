cat /proc/cpuinfo &
if   [  $SERVER_PORT == 8002  ];
then
   /jdk-11.0.7/bin/java -Xms2048m  -Xmn1024m -XX:CompileThreshold=1000 -jar /challenge.jar $SERVER_PORT &
else
   /jdk-11.0.7/bin/java -Xms512m  -Xmn1024m -XX:CompileThreshold=1000 -jar /challenge.jar $SERVER_PORT &
fi
tail -f /start.sh
