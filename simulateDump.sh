#! /bin/bash

curl "http://localhost:8082/jolokia/exec/ryebrye:name=mockHardware/broadcastHighSwitchStatus/true"
curl "http://localhost:8082/jolokia/exec/ryebrye:name=mockHardware/broadcastHighSwitchStatus/false"
sleep 2
curl "http://localhost:8082/jolokia/exec/ryebrye:name=mockHardware/broadcastLowSwitchStatus/false"
curl "http://localhost:8082/jolokia/exec/ryebrye:name=mockHardware/broadcastLowSwitchStatus/true"
