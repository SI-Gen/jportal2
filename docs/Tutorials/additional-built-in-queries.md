Now let's add 2 more queries to our table. This time we will do something a little more advanced than just
a basic `SELECT` or `INSERT`.  

Add the lines highlighted below:

```sql title="todo_items.si" hl_lines="26-32"
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

//The SelectBy function automatically creates
//a SELECT query using the given fields as the
//WHERE clause
PROC SelectBy TodoList_ID RETURNING ID ItemName ItemDescription LastUpdated

PROC UpdateBy ItemName SET ItemDescription

```


We'll start with **line 29**:
```sql linenums="26"
//The SelectBy function automatically creates
//a SELECT query using the given fields as the
//WHERE clause
PROC SelectBy TodoList_ID RETURNING ID ItemName ItemDescription LastUpdated
```

This might look very a little weird, but it is actually really simple to understand. This simply creates a function that
selects all the records from the Todo_Item table, for a specified TodoList_ID. In other words, this function will
generate SQL similar to
```sql
SELECT 
        ID,
        ItemName,
        ItemDescription,
        LastUpdated
FROM
        TodoList_Item
WHERE
        TodoList_ID = <SOME ID>
```

Run `./generate_jportal.sh` again. Remember to run it from the terminal inside VSCode!  

Search for a class named `DB_ToDo_ItemSelectByTodoList_ID`. You should see the following:

```python title="db_ToDo_Item.py" linenums="0"
@dataclass
class DB_ToDo_ItemSelectByTodoList_ID:
    #Outputs
    ID: int
    ItemName: str
    ItemDescription: str
    LastUpdated: datetime

    @classmethod
    def get_statement(cls
                     , TodoList_ID: int
                     ) -> TextAsFrom:
        class _ret:
            sequence = "default," #postgres uses default for sequences
            output = " OUTPUT (ID,ItemName,ItemDescription,LastUpdated)"
            tail = " RETURNING ID ItemName ItemDescription LastUpdated"
            #session.bind.dialect.name

        statement = sa.text(
                        f"/* PROC ToDoList_App.ToDo_Item.SelectByTodoList_ID */"
                        f"select"
                        f"  ID"
                        f", ItemName"
                        f", ItemDescription"
                        f", LastUpdated"
                        f" from ToDoList_App.ToDo_Item"
                        f" where TodoList_ID = :TodoList_ID")

        text_statement = statement.columns(ID=sa.types.Integer,
                                      ItemName=db_types.NonNullableString,
                                      ItemDescription=sa.types.Text,
                                      LastUpdated=sa.types.DateTime,
                                      )
        text_statement = text_statement.bindparams(TodoList_ID=TodoList_ID,
                                         )
        return text_statement

    @classmethod
    def execute(cls, session: Session, TodoList_ID: int
                     ) -> List['DB_ToDo_ItemSelectByTodoList_ID']:
        params = process_bind_params(session, [sa.types.Integer,
                                        ], [TodoList_ID,
                                        ])
      PROC UpdateBy ItemName SET ItemDescription  res = session.execute(cls.get_statement(*params))
        recs = res.fetchall()
        return process_result_recs(DB_ToDo_ItemSelectByTodoList_ID, session, [sa.types.Integer,
                                        db_types.NonNullableString,
                                        sa.types.Text,
                                        sa.types.DateTime,
                                        ], recs)

```

Now look at **line 31**:
```sql linenums="31"
PROC UpdateBy ItemName SET ItemDescription
```

You should start to see the pattern by now. We are creating an update statement that will update the ItemDescription
for a given ItemName.

Run `./generate_jportal.sh` and open the `db_ToDo_Item.py` file again. 

Now search for a class named `DB_ToDo_ItemUpdateByItemDescription`.   
Look closely at the line highlighted below.

```python title="db_ToDo_Item.py" linenums="0" hl_lines="22"
@dataclass
class DB_ToDo_ItemUpdateByItemDescription:
    

    @classmethod
    def get_statement(cls
                     , ItemDescription: str
                     , LastUpdated: datetime
                     , ItemName: str
                     ) -> TextAsFrom:
        class _ret:
            sequence = "default," #postgres uses default for sequences
            output = ""
            tail = ""
            #session.bind.dialect.name

        statement = sa.text(
                        f"/* PROC ToDoList_App.ToDo_Item.UpdateByItemDescription */"
                        f"update ToDoList_App.ToDo_Item"
                        f" set"
                        f"  ItemDescription = :ItemDescription"
                        f", LastUpdated = :LastUpdated"
                        f" where ItemName = :ItemName")

        text_statement = statement.columns()
        text_statement = text_statement.bindparams(ItemDescription=ItemDescription,
                                         LastUpdated=LastUpdated,
                                         ItemName=ItemName,
                                         )
        return text_statement

    @classmethod
    def execute(cls, session: Session, ItemDescription: str
                     , LastUpdated: datetime
                     , ItemName: str
                     ) -> None:
        params = process_bind_params(session, [sa.types.Text,
                                        sa.types.DateTime,
                                        db_types.NonNullableString,
                                        ], [ItemDescription,
                                        LastUpdated,
                                        ItemName,
                                        ])
        res = session.execute(cls.get_statement(*params))
        res.close()
```

What's this? We specified that we wanted to update the ItemDescription. But the generated code is also updating 
the `LastUpdated` field! Why is it doing that? Is the generation wrong?  

No, actually this is a feature built into JPortal2. Look again at the table definition, specifically the column
definition for LastUpdated. You will notice the columntype is specified as `TIMESTAMP`.  

TIMESTAMP is a special type. When you specify a TIMESTAMP column, you are telling JPortal that you want this column
to be updated every time you update the record. JPortal's generated INSERT statements will automatically put the
current time into this column, as will the normal UPDATE. In this case, because we are doing a custom UPDATE,
JPortal generates an input field that we must set when we call the function.

