Counting records in a table is easy.

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

<a href="#count">PROC Count</a>
</code>
</pre>


### **Count**
Counts the number of records in a table.

``` linenums="0"
Count
```

Count will count the number of records in a table.

=== "--template-generator SQLAlchemy"
    ```python 
    @dataclass
    class DB_ToDoListCount:
        #Outputs
        noOf: int
    
        @classmethod
        def get_statement(cls
                         ) -> TextAsFrom:
            class _ret:
                sequence = "default," #postgres uses default for sequences
                output = " OUTPUT (noOf)"
                tail = " RETURNING noOf"
                #session.bind.dialect.name
    
            statement = sa.text(
                            f"/* PROC ToDoList_App.ToDoList.Count */"
                            f"select count(*) noOf from ToDoList_App.ToDoList")
    
            text_statement = statement.columns(noOf=sa.types.Integer,
                                          )
            return text_statement
    
        @classmethod
        def execute(cls, session: Session) -> Optional['DB_ToDoListCount']:
            res = session.execute(cls.get_statement())
            rec = res.fetchone()
            if rec:
                res.close()
                return process_result_rec(DB_ToDoListCount, session, [sa.types.Integer,
                                            ], rec)
    
            return None
    
    ```
=== "--builtin-generator JavaJCCode"
    ```java
      public boolean count() throws SQLException
      {
        String statement = 
          "/* PROC BooksAndAuthors.ToDoList.Count */"
        + "select count(*) noOf from BooksAndAuthors.ToDoList"
        ;
        PreparedStatement prep = connector.prepareStatement(statement);
        ResultSet result = prep.executeQuery();
        if (!result.next())
        {
          result.close();
          prep.close();
          return false;
        }
        noOf =  result.getInt(1);
        result.close();
        prep.close();
        return true;
      }
    ```
