#cat /proc/cpuinfo &
if   [  $SERVER_PORT == 8002  ];
then
   /jre-9.0.4/bin/java  -Xms2048m  -Xmn1024m -XX:CompileThreshold=2000 -jar /challenge.jar $SERVER_PORT &
else
   /jre-9.0.4/bin/java  -Xms512m  -Xmn1024m -XX:CompileThreshold=2000 -jar /challenge.jar $SERVER_PORT &
fi
tail -f /start.sh
