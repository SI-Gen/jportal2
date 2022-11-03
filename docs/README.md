# <b>JPortal2</b>

[**And lo, the Great One looked down upon the people and proclaimed:**  
*“SQL is actually pretty great”*](https://docs.sqlc.dev/en/latest/index.html)


## **Compile SQL to type-safe code, in any language**

[![GitHub release](https://img.shields.io/github/release/SI-Gen/jportal2.svg)](https://github.com/SI-Gen/jportal2/releases)
[![Documentation Site](https://img.shields.io/badge/DOCS_SITE-YES-GREEN.svg)](https://si-gen.github.io/jportal2/)
[![GitHub forks](https://img.shields.io/github/forks/SI-Gen/jportal2.svg?style=social&label=Fork&maxAge=2592000)](https://github.com/SI-Gen/jportal2/network/)

JPortal2 is the newest version of the JPortal SI-file based generator.

## Full Documentation
For full Documentation see [here](https://si-gen.github.io/jportal2/).

## What is JPortal2?
JPortal2 generates fully type-safe idiomatic code from SQL.

* You write table definitions and SQL queries
* You run JPortal2 to generate code that presents type-safe interfaces to those queries
* You write application code that calls the methods JPortal2 generated.

It's that easy. You don't have to write any boilerplate SQL querying code ever again.  
See the current list of supported programming languages and databases here: [Generator Docs](generators/index.md).

## How it Works
Simply put, JPortal2 (and JPortal before it) is a data-definition first code generator. To use it, you:

1. Define database tables using a small, easy-to-understand Domain-Specific Language (DSL).
2. You define the queries you want to expose for the table. Standard CRUD queries like SELECT, INSERT, UPDATE, DELETE and MERGE can be created for you.
3. More complex queries can easily be defined using standard SQL. 

Once you have defined your tables, you run the JPortal2 generator, and specify the code you want generated.  

JPortal2 can generate:  

1. DDL for all the popular RDBMS systems (Postgres, MySQL, Oracle, SQLServer, DB/2 and others)  
2. DAL (Data Access Layers) for most languages (C/C++, Java, C#, Python and others)  
3. Code stubs that can be used in other generators to create Client/Server and other Applications  
4. Anything else you want to generate off your database structure  

JPortal2 has a number of built-in code generators written in Java, but also has built-in Freemarker support to allow you to easily add your own generators.



## Quickstart

### Docker
First pull the docker image for the version of JPortal you want to use.
You can browse to [https://github.com/si-gen/jportal2](https://github.com/si-gen/jportal2) to look at the available versions.

```shell
echo Running JPortal2 from ${PWD}...

docker run --rm -v ${PWD}:/local ghcr.io/si-gen/jportal2:latest \
                      --inputdir=/local/sql/si \
                      --builtin-generator PostgresDDL:/local/generated_sources/generated_sql \
                      --template-generator SQLAlchemy:/local/generated_sources/python/jportal \
                      --download-template "SQLAlchemy:https://github.com/SI-Gen/jportal2-generator-vanguard-sqlalchemy/archive/refs/tags/1.8.zip|stripBaseDir"                      
```

### Java

The Java JAR is hosted at https://ossindex.sonatype.org/component/pkg:maven/za.co.bbd/jportal2

To use JPortal2 in your Maven based Java project, simply add the following to your POM:
properties:
``` xml
<properties>    
    <jportal2.version>1.3.0</jportal2.version>
    <jportal2maven.version>1.2.0</jportal2maven.version>
</properties>
```
dependencies:
``` xml
        <dependency>
            <groupId>za.co.bbd</groupId>
            <artifactId>jportal2</artifactId>
            <version>1.3.0</version>
            <scope>compile</scope>
        </dependency>
``` 
and plugins:
``` xml
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


Create a file called todolist.si in the ${basedir}/src/main/sql/ directory (this is defined above in sourcePath tag)

todolist.si
```sql
DATABASE ExampleDatabase
PACKAGE com.example.db
SERVER ExampleServer
SCHEMA ToDoList_App

TABLE ToDoList
   ID               SEQUENCE
   ListName         CHAR(255)
   ListType         SHORT (Private=1, Public=2)
   Description      CHAR
   LastUpdated      TIMESTAMP

KEY PKEY PRIMARY
    ID

//Simple CRUD queries are available out of the box with JPortal2
PROC Insert Returning
PROC Update
PROC SelectOne
PROC DeleteOne

//More complex custom queries can be defined using standard SQL
PROC SelectListNameAndListTypeAsString
INPUT
    ID          =
OUTPUT
    ListName    =
    ListType    CHAR
SQLCODE
SELECT
    ListName,
    CASE
        WHEN ListType = 1 THEN 'Private'
        WHEN ListType = 2 THEN 'Public'
END
FROM
    TodoList
WHERE
    ID = :ID
ENDCODE

//Dynamic queries can be done with the ampersand below
//REMEMBER!!! Dynamic SQL is open to SQL injection! So use with care and make sure to sanitize inputs!
//Dynamic SQL is a last resort escape hatch, not the first tool to reach for!!
//In the DAL class created for the below query, a property called MyDynamicWhereClause will be created
//This will allow you to pass through the remainder of the where clause as a string.
PROC SelectWithDynamicQuery
INPUT
    ListName    =
OUTPUT
   ID               SEQUENCE
   ListName         CHAR(255)
   ListType         SHORT (Private=1, Public=2)
   Description      CHAR
   LastUpdated      TIMESTAMP
SQLCODE
SELECT
   ID
   ,ListName
   ,ListType
   ,Description
   ,LastUpdated
FROM
    ToDoList
WHERE
    ListName = :ListName
    AND &MyDynamicWhereClause
ENDCODE

```

Now create a file called todo_items.si in the same directory (${basedir}/src/main/sql/)
todo_items.si
```sql
DATABASE ExampleDatabase
PACKAGE com.example.db
SERVER ExampleServer
SCHEMA ToDoList_App

TABLE ToDo_Item
   ID               SEQUENCE
   TodoList_ID      INT     //This is a foreign key to the ToDoList table
   ItemName         CHAR(255)
   ItemDescription  CLOB
   LastUpdated      TIMESTAMP

//This define ID as the Primary Key
KEY PKEY PRIMARY
    ID

PROC Insert Returning
PROC Update
PROC SelectOne
PROC DeleteOne

//The SelectBy function automatically creates
//a SELECT query using the given fields as the
//WHERE clause
PROC SelectBy TodoList_ID
OUTPUT
    ID                  =
    ItemName            =
    ItemDescription     =
    LastUpdated         =



```

Now compile your maven project. If all went well, you should see 2 files inside
the directory ${basedir}/target/generated-sources/java/com/example/db.
