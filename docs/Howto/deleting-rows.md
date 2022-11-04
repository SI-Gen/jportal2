## Built-In Procs
JPortal ships with a number of built-in procs to allow easy deleting of records in a table.

<pre>
<code>
DATABASE ExampleDatabase
PACKAGE com.example.db
SERVER ExampleServer
SCHEMA ToDoList_App

TABLE ToDoList
   ID               SEQUENCE
   ListName         CHAR(255)
   ListType         SHORT (Private=1, Public=2)
   Description      CHAR(255)
   LastUpdated      TIMESTAMP

KEY PKEY PRIMARY
    ID

//Simple CRUD queries are available out of the box with JPortal2
<a href="#deleteone">PROC DeleteOne</a>
<a href="#deleteby">PROC DeleteBy ListName ListType</a>
<a href="#deleteall">PROC DeleteAll</a>

</code>
</pre>

### **DeleteOne**
Deletes an existing record by primary key.

``` linenums="0"
DeleteOne
[(standard)]
```

DeleteOne will delete an existing record in a table, by primary key. If no primary key is specified on the table,
JPortal2 will issue a warning at compile time.

=== "--template-generator SQLAlchemy"
    ```python 
    @dataclass
    class DB_ToDoListDeleteOne:

        @classmethod
        def get_statement(cls
                         , ID: int
                         ) -> TextAsFrom:
            class _ret:
                sequence = "default," #postgres uses default for sequences
                output = ""
                tail = ""
                #session.bind.dialect.name
    
            statement = sa.text(
                            f"/* PROC ToDoList_App.ToDoList.DeleteOne */"
                            f"delete from ToDoList_App.ToDoList"
                            f" where ID = :ID")
    
            text_statement = statement.columns()
            text_statement = text_statement.bindparams(ID=ID,
                                             )
            return text_statement
    
        @classmethod
        def execute(cls, session: Session, ID: int
                         ) -> None:
            params = process_bind_params(session, [sa.types.Integer,
                                            ], [ID,
                                            ])
            res = session.execute(cls.get_statement(*params))
            res.close()
    ```

=== "--builtin-generator JavaJCCode"
    ```java
    public void deleteOne() throws SQLException
    {
      String statement = 
        "/* PROC BooksAndAuthors.ToDoList.DeleteOne */"
      + "delete from BooksAndAuthors.ToDoList"
      + " where ID = ?"
      ;
      PreparedStatement prep = connector.prepareStatement(statement);
      prep.setInt(1, id);
      prep.executeUpdate();
      prep.close();
    }
    ```

### **DeleteBy**
Deletes record by user specified keys

```
DeleteBy <DeleteKeyColumn>+
```

DeleteBy will delete the records of a table that are specified by the keys specified
by `DeleteKeyColumn`'s.  

=== "--template-generator SQLAlchemy"
    ```python
    ```

### **DeleteAll**
Deletes all existing records in table.

``` linenums="0"
DeleteAll 
```

DeleteAll will delete all existing records in a table.  

=== "--template-generator SQLAlchemy"
    ```python
    @dataclass
    class DB_ToDoListDeleteAll:
        @classmethod
        def get_statement(cls
                         ) -> TextAsFrom:
            class _ret:
                sequence = "default," #postgres uses default for sequences
                output = ""
                tail = ""
                #session.bind.dialect.name
    
            statement = sa.text(
                            f"/* PROC ToDoList_App.ToDoList.DeleteAll */"
                            f"delete from ToDoList_App.ToDoList")
    
            text_statement = statement.columns()
            return text_statement
    
        @classmethod
        def execute(cls, session: Session) -> None:
            res = session.execute(cls.get_statement())
            res.close()
    
    ```
=== "--builtin-generator JavaJCCode"
    ```java
    public static void deleteAll(Connector connector) throws SQLException
    {
        String statement =
        "/* PROC BooksAndAuthors.ToDoList.DeleteAll */"
        +"delete from BooksAndAuthors.ToDoList"
        ;
        PreparedStatement prep = connector.prepareStatement(statement);
        prep.executeUpdate();
        prep.close();
    }
    ```
