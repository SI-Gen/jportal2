# <b>Overview</b>
## ** Compile SQL to type-safe code, in any language **

[![GitHub release](https://img.shields.io/github/release/SI-Gen/jportal2.svg)](https://github.com/SI-Gen/jportal2/releases)
[![Documentation Site](https://img.shields.io/badge/DOCS_SITE-YES-GREEN.svg)](https://si-gen.github.io/jportal2/)
[![GitHub forks](https://img.shields.io/github/forks/SI-Gen/jportal2.svg?style=social&label=Fork&maxAge=2592000)](https://github.com/SI-Gen/jportal2/network/)

JPortal2 is the newest version of the JPortal SI-file based generator.

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
3. More complex queries can easily be defined using standard SQL. Once you have defined your tables, you run the JPortal2 generator, and specify the code you want generated. JPortal2 can generate:
   * DDL for all the popular RDBMS systems (Postgres, MySQL, Oracle, SQLServer, DB/2 and others)
   * DAL (Data Access Layers) for most languages (C/C++, Java, C#, Python and others)
   * Code stubs that can be used in other generators to create Client/Server and other Applications
   * Anything else you want to generate off your database structure

JPortal2 has a number of built-in code generators written in Java, but also has built-in Freemarker support to allow you to easily add your own generators.


## Documentation  
For full Documentation see [here](https://si-gen.github.io/jportal2/).  

## Quickstart

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


### Workflow
A detailed description of the workflow process is mentioned in the [Contribution.md](/docs/Contributing/Contribution.md)
Feel free to add any suggestions or any help in the 

# Documentation
For comprehensive documentation. Refer to the [docs](docs/index.md)
