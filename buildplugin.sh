#!/bin/bash

# Change to the directory of the script
cd "$(dirname "opentelemetry-plugin-dm")"

# Run the commands sequentially
brew services stop jenkins-lts
mvn package -Dmaven.test.skip=true
cp -v target/harnessmigration.hpi ~/.jenkins/plugins
brew services start jenkins-lts