#!/bin/bash
mvn -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.0.49" clean package

