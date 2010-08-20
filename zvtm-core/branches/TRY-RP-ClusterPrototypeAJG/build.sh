#!/bin/bash
mvn -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="127.0.0.1" clean package

