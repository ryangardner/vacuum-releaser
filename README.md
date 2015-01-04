vacuum-releaser
===============

the brains to a raspberry-pi vacuum releaser

Build this project using maven, then run on raspberry pi with

    sudo java -XX:MetaspaceSize=32m -Xmx368m -Xms96m -jar -Dspring.profiles.active=raspberryPi,default raspberry-pi-test-0.0.1-SNAPSHOT.jar
  
More instructions to come later.

Local Development
-----------------
Local development is a much faster way to develop then pushing the file to a raspberry pi and waiting for it to restart. Some simple mbeans
are available that you can use to send messages into the same message bus that the float switches do when running on the actual raspberry pi.

To run locally, you can run the ```ReleaserApplication``` from inside your ide or by executing: ```mvn spring-boot:run``` from the command line.

Once it is started, you can navigate to:

[http://localhost:8080/index.html](http://localhost:8080/index.html) to see the home page.

# Simulating the hardware
To simulate the low float switch becoming active, you can send the following request to your local server:

[http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastLowSwitchStatus/true]([http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastLowSwitchStatus/true])

Similarly the high switch can be set high by doing:
[http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastHighSwitchStatus/true]([http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastHighSwitchStatus/true])

To simulate an entire filling / emptying cycle you can run:
[http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastLowSwitchStatus/true]([http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastLowSwitchStatus/true])
[http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastHighSwitchStatus/true]([http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastHighSwitchStatus/true])
[http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastHighSwitchStatus/false]([http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastHighSwitchStatus/false])
[http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastLowSwitchStatus/false]([http://localhost:8080/jolokia/exec/ryebrye:name=mockHardware/broadcastLowSwitchStatus/false])

After running a few simulated dumps of the releaser, if you refresh the index page, you should see the values change there.
