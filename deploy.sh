#!/bin/bash
mvn clean compile assembly:single
scp target/test-app-1.0-SNAPSHOT-jar-with-dependencies.jar 192.168.0.101:/opt/ &
scp target/test-app-1.0-SNAPSHOT-jar-with-dependencies.jar 192.168.0.104:/opt/
