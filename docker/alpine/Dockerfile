FROM eclipse-temurin:11-jre-alpine

RUN apk --no-cache add --update bash openssl

ARG JPORTAL2_VERSION

COPY ./target/jportal2-${JPORTAL2_VERSION}.jar /opt/jportal2/jportal2.jar

WORKDIR /local

ENTRYPOINT ["java", "-jar", "/opt/jportal2/jportal2.jar"]
CMD ["-?"]