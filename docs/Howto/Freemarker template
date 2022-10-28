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
