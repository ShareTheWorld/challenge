if   [  $SERVER_PORT == 8002  ];
then
   /jre-9.0.4/bin/java -Xms2500m  -Xmn1024m -XX:CompileThreshold=1000 -jar /challenge.jar $SERVER_PORT &
else
   /jre-9.0.4/bin/java -Xms2500m  -Xmn1024m -XX:CompileThreshold=1000 -jar /challenge.jar $SERVER_PORT &
fi
tail -f /start.sh
