# Sourced from login shells and bashrc. Keep this POSIX: /bin/sh loads
# /etc/profile.d during image setup.
#
# GraalVM 25 is the default JDK so Maven and native-image share one JAVA_HOME.
# JDK 11 is the runtime used by the CI verify job.

use_jdk_11() {
    export JAVA_HOME=/usr/lib/jvm/java-11
    export PATH="${JAVA_HOME}/bin:${PATH}"
}

use_graalvm() {
    export JAVA_HOME=/usr/local/sdkman/candidates/java/current
    export PATH="${JAVA_HOME}/bin:${PATH}"
}
