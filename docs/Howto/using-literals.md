## Using Literals
Occasionally you will want to use a word that is a reserved word, as the name of a table or a column. For example, consider the following SI file:

todolist.si
```sql hl_lines="10"
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




Notice that the Description column is named 'Desc', which is a reserved word (both in JPortal, as well as in SQL). 
This will cause a JPortal compilation issue. To work around this, we turn the column name into a *literal* by 
writing it as `L'Desc'`. This will allow the SI file to compile.
