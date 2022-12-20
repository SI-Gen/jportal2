Let's add a second table to our database.
Create a file called `sql/si/todo_item.si`.  

Your structure should now look like this:
```
jportal2-demo
└───sql
    └───si
        ├──todolist.si
        └──todo_item.si
```

```sql title="todo_items.si"
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

LINK ToDoList TodoList_ID

//We do a normal Insert and Update without a Returning here, to test the regular generaion
//We do an InsertReturning and UpdateReturning in the ToDoList table to test that generation there
PROC Insert
PROC Update
PROC SelectOne
PROC DeleteOne
```

Most of the file should be familiar to you now, but have a look at **line 17**:
```sql linenums="17"
LINK ToDoList TodoList_ID```
```

The `LINK` keyword creates a foreign key. The syntax is `LINK <parent_table> <my_column_name>`.
The line above instructs JPortal that there is a foreign key from `ToDoItem.TodeList_ID` to the
primary key of `ToDoList`.

