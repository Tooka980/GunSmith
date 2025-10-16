#!/usr/bin/env sh
DIR="$(cd "$(dirname "$0")"; pwd)"
APP_HOME="$DIR"
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "WARNING: gradle-wrapper.jar missing. Generate it locally with: gradle wrapper --gradle-version 8.10.2"
  exit 1
fi
exec java -cp "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
