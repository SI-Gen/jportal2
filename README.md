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

### Freemarker
To use a Freemarker Template. Create a folder anywhere: \$HOME/templates/Example
Inside that folder create the freemarker template file: ExampleDB.py.ftl

JPortal2 supports freemarker file name substitution. For example ExampleDB${table.name}.py.ftl will generate a ExampleDB{tablename}.py file for all the SI files you gen for. 

Inside your template file:
```
<#list database.tables as table>
table name: ${table.name}:
    table Fields:
    <#list table.fields as field>${field.name}
    </#list>
</#list>
```

JPortal2 takes 2 arguments to run freemarker. The template location where all the templates folders are, and what templates you want to run.

Run jportal with the argument:
```
--template-location $HOME/templates/
--template-generator ExampleDB:$HOME/output
```

When JPortal2 is done. You will have the generated files in the output directory and it will look like this:
```
table name: ExampleTable
    table fields:
    id
    name
    surname
```

### Freemarker Variables
There are a few variables available to you in the freemarker template language for generation:
- __Database__
- __Table__
- __Proc__

The above variables match the hierarchy that is built into the SI structure. A database is a collection of SI files, and each SI file represents a table. Furthermore, each SI table has a collection of procs attached to it.

The above variables are also available in the name component of the free-marker template file. That way one can create a generated file down to the level of each individual proc.


## Using Literals
Occasionally you will want to use a word that is a reserved word, as the name of a table or a column. For example, consider the following SI file:

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
   L'Desc'          CHAR   //DESC is a reserved keyword!
   LastUpdated      TIMESTAMP

KEY PKEY PRIMARY
    ID

//Simple CRUD queries are available out of the box with JPortal2
PROC Insert Returning
PROC Update
PROC SelectOne
PROC DeleteOne
```

Notice that the Description column is named 'Desc', which is a reserved word (both in JPortal, as well as in SQL). This will cause a JPortal compilation issue. To work around this, we turn the column name into a *literal* by writing it as L'Desc'. This will allow the SI file to compile.
