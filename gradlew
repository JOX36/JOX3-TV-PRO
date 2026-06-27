#!/bin/sh
# Gradle wrapper script
# This is a minimal wrapper that downloads Gradle if needed

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Determine the Gradle distribution URL
GRADLE_VERSION="8.5"
GRADLE_DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

# Gradle user home
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

# Check if Gradle is already downloaded
GRADLE_HOME="$GRADLE_USER_HOME/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
if [ ! -d "$GRADLE_HOME" ] ; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    mkdir -p "$GRADLE_HOME"
    curl -L -o "$GRADLE_HOME/gradle-${GRADLE_VERSION}-bin.zip" "$GRADLE_DIST_URL"
    unzip -q -o "$GRADLE_HOME/gradle-${GRADLE_VERSION}-bin.zip" -d "$GRADLE_HOME"
fi

# Find the gradle binary
GRADLE_BIN=$(find "$GRADLE_HOME" -name "gradle" -type f | head -1)

# Run Gradle
exec "$GRADLE_BIN" "$@"
