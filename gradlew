#!/bin/sh
# Gradle wrapper script for Unix systems

APP_HOME=$( cd "${0%/*}" && pwd )
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk}

exec "$JAVA_HOME/bin/java" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
