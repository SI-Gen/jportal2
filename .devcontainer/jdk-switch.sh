# Sourced from login shells and bashrc. Keep this POSIX: /bin/sh loads
# /etc/profile.d during image setup.
#
# GraalVM 25 is the default JDK so Maven and native-image share one JAVA_HOME.
# Maven must run on JDK 17 or newer. native-maven-plugin is Java 17 bytecode
# and cannot load on JDK 11.

use_jdk_11() {
    export JAVA_HOME=/usr/lib/jvm/java-11
    export PATH="${JAVA_HOME}/bin:${PATH}"
}

use_graalvm() {
    export JAVA_HOME=/usr/local/sdkman/candidates/java/current
    export PATH="${JAVA_HOME}/bin:${PATH}"
}
