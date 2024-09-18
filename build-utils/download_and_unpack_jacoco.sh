#!/usr/bin/env bash
set -euxo pipefail

wget https://repo1.maven.org/maven2/org/jacoco/jacoco/0.8.12/jacoco-0.8.12.zip
unzip -j ./jacoco-0.8.12.zip "lib/jacocoagent.jar" -d /jar/
rm ./jacoco-0.8.12.zip