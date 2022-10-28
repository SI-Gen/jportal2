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

**todo_items.si**
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

//We do a normal Insert and Update without a Returning here, to test the regular generaion
//We do an InsertReturning and UpdateReturning in the ToDoList table to test that generation there
PROC Insert
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


PROC UpdateBy ItemName SET ItemName ItemDescription
```

Most of the file should be familiar to you now, but have a look at **lines 24-32**:
```sql
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

This might look very weird, but it is actually really simple to understand. This simply creates a function that
selects all the records from the Todo_Item table, for a specified TodoList_ID. In other words, this function will 
generate SQL similar to  
```sql
SELECT 
        ID,
        ItenName,
        ItemDescription,
        LastUpdated
FROM
        TodoList_Item
WHERE
        TodoList_ID = <SOME ID>
```

run `./generate_jportal.sh` again. Remember to run it from the terminal inside VSCode!