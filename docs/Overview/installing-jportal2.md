## Overview
JPortal2 is distributed as a jar hosted on Maven Central as well as a command-line docker tool, for easy cross-platform usage.

We recommend using the docker image for most projects.

### Docker

#### Installing docker
Install docker using the instructions [here](https://docs.docker.com/get-docker/)

#### Installing JPortal2 docker image
First pull the docker image for the version of JPortal you want to use.
You can browse to [https://github.com/si-gen/jportal2](https://github.com/si-gen/jportal2) to look at the available versions.

```shell
docker pull ghcr.io/si-gen/jportal2:1.8.14
```

Next run JPortal using `docker run`
```shell
echo Running JPortal2 from ${PWD}...

docker run --rm -v ${PWD}:/local ghcr.io/si-gen/jportal2:1.8.14 \
                      --inputdir=/local/src/sql/si_files \
                      --template-generator \
                        SQLAlchemy:/local/src/python/bbdcontent/sqlalchemy \
                      --builtin-generator \
                      PostgresDDL:/local/database/generated_sql \
```

### Java

The Java JAR is hosted at https://ossindex.sonatype.org/component/pkg:maven/za.co.bbd/jportal2

To use JPortal2 in your Maven based Java project, simply add the following to your POM:
properties:
```
<properties>    
    <jportal2.version>1.3.0</jportal2.version>
    <jportal2maven.version>1.2.0</jportal2maven.version>
</properties>
```
dependencies:
```
        <dependency>
            <groupId>za.co.bbd</groupId>
            <artifactId>jportal2</artifactId>
            <version>1.3.0</version>
            <scope>compile</scope>
        </dependency>
``` 
and plugins:
```
            <plugin>
            <groupId>za.co.bbd</groupId>
            <artifactId>jportal2-maven-plugin</artifactId>
            <version>${jportal2maven.version}</version>
            <configuration>
                <sourcePath>${basedir}/src/main/sql/</sourcePath>
                <generators>
                    <generator>JavaJCCode:${basedir}/target/generated-sources/java/com/example/db</generator>
                    <generator>PostgresDDL:${basedir}/target/generated-sources/scripts/sql</generator>
                </generators>
                <compilerFlags>
                    <compilerFlag>utilizeEnums</compilerFlag>
                </compilerFlags>
<!--                    <additionalArguments>&#45;&#45;template-generator JdbiSqlObjects:${basedir}/target/generated-sources/java/</additionalArguments>-->
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>za.co.bbd</groupId>
                    <artifactId>jportal2</artifactId>
                    <version>${jportal2.version}</version>
                </dependency>
            </dependencies>
            <executions>
               <execution>
                 <phase>generate-sources</phase>
                     <goals>
                        <goal>jportal</goal>
                    </goals>
            </execution>
            </executions>
        </plugin>

```

JPortal2 is the actual Data Access Layer (DAL) generator. jportal2-maven-plugin is a plugin for maven that will run the generator at build time.
