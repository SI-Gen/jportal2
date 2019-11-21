# JPortal2

[<img src="https://dieterrosch.visualstudio.com/_apis/public/build/definitions/62fc2ca3-38b4-432f-8341-af383b7ba8e4/3/badge"/>](https://dieterrosch.visualstudio.com/_apis/public/build/definitions/62fc2ca3-38b4-432f-8341-af383b7ba8e4/3/badge)

JPortal2 is the newest version of the JPortal SI-file based generator.


## What is JPortal2?
Simply put, JPortal2 (and JPortal before it) is a data-definition first code generator.  
To use it, you define database tables using a small, easy-to-understand Domain-Specific Language (DSL). You define the queries you want to expose for the table. Standard CRUD queries like SELECT, INSERT, UPDATE, DELETE and MERGE can be created for you. More complex queries can easily be defined using standard SQL. Once you have defined your tables, you run the JPortal2 generator, and specify the code you want generated. JPortal2 can generate:
    * DDL for all the popular RDBMS systems (Postgres, MySQL, Oracle, SQLServer, DB/2 and others)
    * DAL (Data Access Layers) for most languages (C/C++, Java, C#, Python and others)
    * Code stubs that can be used in other generators to create Client/Server and other Applications
    * Anything else you want to generate off your database structure

JPortal2 has a number of built-in code generators written in Java, but also has built-in Freemarker support to allow you to easily add you own generators.

## History
### JPortal
JPortal was originally developed in the early 1990's by Vincent Risi (https://github.com/VincentRisi) as a standardised way to generate a Data Access Layer (DAL) for multiple databases and target languages. SI Files allow the definition of a SQL table with columns, as well as SQL Queries to be run against the table. It then allows the generation of a DAL in a number of languages. Being developed in the 90's, the first languages that were supported, were Java, C++, C and Visual Basic. Original database support was for Oracle.

Over the years the tool has grown into a suite of tools to include generators DAL's, RPC client and server generators and for a number of languages including C# and Python, and database support for Oracle, SQL Server, MySQL, PostgreSQL, DB2 and SQLite.


### JPortal2

The original JPortal grew organically, and while over the years the capabilities have expanded, lots of it was not envisioned originally. As a result, some of the code has become difficult to maintain. JPortal2 is an attempt to clean up the code bit-by-bit, and also start adding unittests and other features that are required in modern libraries.

### Original Code
The original JPortal code can be found at https://github.com/VincentRisi. An updated version with some bug-fixes can be found at https://github.com/dieterrosch. However, this repo (https://github.com/SI-Gen) is the only actively maintained repository. 


## Quickstart
### Java
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
                <additionalArguments>--template-generator JdbiSqlObjects:${basedir}/target/generated-sources/java/</additionalArguments>
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

SCHEMA ToDoList_App

TABLE ToDoList
   ID               UUID
   ListName         CHAR(255)
   ListType         SHORT (Private=1, Public=2)
   Description      CHAR
   LastUpdated      TIMESTAMP

KEY PKEY PRIMARY
    ID

PROC Insert Returning
PROC Update
PROC SelectOne
PROC Delete

PROC SelectListNameAndListTypeAsString
INPUT
    ID          =
OUTPUT
    ListName    =
    ListType    CHAR
SQLCODE
SELECT
    ListName,
    ListType
FROM 
    TodoList
WHERE
    ID = :ID
ENDCODE
```

Now create a file called todo_items.si in the same directory (${basedir}/src/main/sql/)
todo_items.si
```sql
DATABASE ExampleDatabase
PACKAGE com.example.db

SCHEMA ToDoList_App

TABLE ToDo_Item
   ID               SEQUENCE
   TodoList_ID      BIGINT
   ItemName         CHAR(255)
   ItemDescription  CLOB
   LastUpdated      TIMESTAMP

KEY PKEY PRIMARY
    ID

PROC Insert Returning
PROC Update
PROC SelectOne
PROC Delete

PROC SelectBy TodoList_ID
OUTPUT
    ID                  =
    ItemName            =
    ItemDescription     =
    LastUpdated
```

Now compile your maven project. If all went well, you should see 2 files inside
the directory ${basedir}/target/generated-sources/java/com/example/db.
