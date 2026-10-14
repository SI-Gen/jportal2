FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

# JDK 11 matches the CI verify job. GraalVM 25 (with native-image) and Maven
# are installed by the Java dev container feature. gcc and zlib are required
# by native-image. MkDocs matches the documentation workflow.
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get install -y --no-install-recommends \
        openjdk-11-jdk \
        build-essential \
        zlib1g-dev \
        python3 \
        python3-pip \
        python3-venv \
    && jdk11="$(find /usr/lib/jvm -maxdepth 1 -type d -name 'java-11-openjdk-*' | head -n 1)" \
    && test -n "${jdk11}" \
    && ln -sfn "${jdk11}" /usr/lib/jvm/java-11 \
    && python3 -m venv /opt/mkdocs \
    && /opt/mkdocs/bin/pip install --no-cache-dir mkdocs-material mkdocs-glightbox \
    && ln -sfn /opt/mkdocs/bin/mkdocs /usr/local/bin/mkdocs \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY jdk-switch.sh /etc/profile.d/jportal-jdks.sh
RUN chmod 644 /etc/profile.d/jportal-jdks.sh \
    && echo '. /etc/profile.d/jportal-jdks.sh' >> /etc/bash.bashrc
