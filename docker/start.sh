if   [  $SERVER_PORT == 8002  ];
then
   /jre-9.0.4/bin/java -Xms20480m -Xmn5120m -XX:+DisableExplicitGC -jar /challenge.jar $SERVER_PORT &
else
   /jre-9.0.4/bin/java -Xms30720m -Xmn5120m -XX:+DisableExplicitGC -jar /challenge.jar $SERVER_PORT &
fi
tail -f /start.sh
