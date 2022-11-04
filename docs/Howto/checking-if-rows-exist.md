To check if a row with a specific primary key exists, we use the Exists proc.

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

<a href="#exists">PROC Exists</a>
</code>
</pre>


### **Exist**
Checks if a record with a specific primary key exists.

``` linenums="0"
Exists
```

Exists will check if a record with a specific primary key exists.

=== "--template-generator SQLAlchemy"
    ```python 
    @dataclass
    class DB_ToDoListExists:
        #Outputs
        noOf: int
    
        @classmethod
        def get_statement(cls
                         , ID: int
                         ) -> TextAsFrom:
            class _ret:
                sequence = "default," #postgres uses default for sequences
                output = " OUTPUT (noOf)"
                tail = " RETURNING noOf"
                #session.bind.dialect.name
    
            statement = sa.text(
                            f"/* PROC ToDoList_App.ToDoList.Exists */"
                            f"select count(*) noOf from ToDoList_App.ToDoList"
                            f" where ID = :ID")
    
            text_statement = statement.columns(noOf=sa.types.Integer,
                                          )
            text_statement = text_statement.bindparams(ID=ID,
                                             )
            return text_statement
    
        @classmethod
        def execute(cls, session: Session, ID: int
                         ) -> Optional['DB_ToDoListExists']:
            params = process_bind_params(session, [sa.types.Integer,
                                            ], [ID,
                                            ])
            res = session.execute(cls.get_statement(*params))
            rec = res.fetchone()
            if rec:
                res.close()
                return process_result_rec(DB_ToDoListExists, session, [sa.types.Integer,
                                            ], rec)
    
            return None
    
    ```
=== "--builtin-generator JavaJCCode"
    ```java
    public boolean exists() throws SQLException
    {
      String statement = 
        "/* PROC BooksAndAuthors.ToDoList.Exists */"
      + "select count(*) noOf from BooksAndAuthors.ToDoList"
      + " where ID = ?"
      ;
      PreparedStatement prep = connector.prepareStatement(statement);
      prep.setInt(1, id);
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
