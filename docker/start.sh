if   [  $SERVER_PORT  ];
then
   /jre-9.0.4/bin/java -Xms20480m -Xmn5120m -jar /challenge.jar $SERVER_PORT &
else
   /jre-9.0.4/bin/java -Xms20480m -Xmn5120m -jar /challenge.jar 8000 &
fi
tail -f /start.sh
