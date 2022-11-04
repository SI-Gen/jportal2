## Built-In Procs
JPortal ships with a number of built-in procs to allow easy inserting of data into a database.

Let assume the following table structure in our SI, with various procs defined to insert records:

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

<a href="#insert">PROC Insert</a>
<a href="#insert">PROC Insert Returning</a>
<a href="#bulkinsert">PROC BulkInsert</a>
</code>
</pre>

Let's work through the above.

### **Insert**
Insert a new record.

``` linenums="0"
Insert  
[ RETURNING ]
```

Insert will insert a record into a table.
Adding the RETURNING clause will cause JPortal2 to return the inserted primary key. This is useful when using
database sequences.

=== "--template-generator SQLAlchemy"
    === "Insert"
        ```python 
        @dataclass
        class DB_ToDoListInsert:
            # Enum for ListType field
            class ListTypeEnum(enum.Enum):
                Private = 1
                Public = 2
        
                @classmethod
                def process_result_value_cls(cls, value, dialect):
                    return DB_ToDoListInsert.ListTypeEnum(value)
        
        
            
        
            @classmethod
            def get_statement(cls
                             , ListName: str
                             , ListType: ListTypeEnum
                             , Description: str
                             , LastUpdated: datetime
                             ) -> TextAsFrom:
                class _ret:
                    sequence = "default," #postgres uses default for sequences
                    output = ""
                    tail = ""
                    #session.bind.dialect.name
        
                statement = sa.text(
                                f"/* PROC ToDoList_App.ToDoList.Insert */"
                                f"insert into ToDoList_App.ToDoList ("
                                f"  ListName,"
                                f"  ListType,"
                                f"  Description,"
                                f"  LastUpdated"
                                f" ) "
                                f" values ("
                                f"  :ListName,"
                                f"  :ListType,"
                                f"  :Description,"
                                f"  :LastUpdated"
                                f" )")
        
                text_statement = statement.columns()
                text_statement = text_statement.bindparams(ListName=ListName,
                                                 ListType=ListType,
                                                 Description=Description,
                                                 LastUpdated=LastUpdated,
                                                 )
                return text_statement
        
            @classmethod
            def execute(cls, session: Session, ListName: str
                             , ListType: ListTypeEnum
                             , Description: str
                             , LastUpdated: datetime
                             ) -> None:
                params = process_bind_params(session, [db_types.NonNullableString,
                                                sa.types.SmallInteger,
                                                db_types.NonNullableString,
                                                sa.types.DateTime,
                                                ], [ListName,
                                                ListType.value if isinstance(ListType, enum.Enum) else ListType,
                                                Description,
                                                LastUpdated,
                                                ])
                res = session.execute(cls.get_statement(*params))
                res.close()
        
        ```
    === "Insert Returning"
        ```python hl_lines="26 46"
        @dataclass
        class DB_ToDoListInsertReturning:
            # Enum for ListType field
            class ListTypeEnum(enum.Enum):
                Private = 1
                Public = 2
        
                @classmethod
                def process_result_value_cls(cls, value, dialect):
                    return DB_ToDoListInsertReturning.ListTypeEnum(value)
        
        
            #Outputs
            ID: int
        
            @classmethod
            def get_statement(cls
                             , ListName: str
                             , ListType: ListTypeEnum
                             , Description: str
                             , LastUpdated: datetime
                             ) -> TextAsFrom:
                class _ret:
                    sequence = "default," #postgres uses default for sequences
                    output = ""
                    tail = " RETURNING ID" # (1)!
                    #session.bind.dialect.name
        
                statement = sa.text(
                                f"/* PROC ToDoList_App.ToDoList.Insert */"
                                f"insert into ToDoList_App.ToDoList ("
                                f"  ID,"
                                f"  ListName,"
                                f"  ListType,"
                                f"  Description,"
                                f"  LastUpdated"
                                f" ) "
                                f"{_ret.output}"
                                f" values ("
                                f"{_ret.sequence}"
                                f"  :ListName,"
                                f"  :ListType,"
                                f"  :Description,"
                                f"  :LastUpdated"
                                f" )"
                                f"{_ret.tail}") # (2)!
        
                text_statement = statement.columns(ID=sa.types.Integer,
                                              )
                text_statement = text_statement.bindparams(ListName=ListName,
                                                 ListType=ListType,
                                                 Description=Description,
                                                 LastUpdated=LastUpdated,
                                                 )
                return text_statement
        
            @classmethod
            def execute(cls, session: Session, ListName: str
                             , ListType: ListTypeEnum
                             , Description: str
                             , LastUpdated: datetime
                             ) -> Optional['DB_ToDoListInsertReturning']:
                params = process_bind_params(session, [db_types.NonNullableString,
                                                sa.types.SmallInteger,
                                                db_types.NonNullableString,
                                                sa.types.DateTime,
                                                ], [ListName,
                                                ListType.value if isinstance(ListType, enum.Enum) else ListType,
                                                Description,
                                                LastUpdated,
                                                ])
                res = session.execute(cls.get_statement(*params))
                rec = res.fetchone()
                if rec:
                    res.close()
                    return process_result_rec(DB_ToDoListInsertReturning, session, [sa.types.Integer,
                                                ], rec)
        
                return None
        
        ```
        
        1. Different database engine have different ways of returning records. Postgres requires a postfix or "tail"
           which specifies which colunns to return. Here we are setting up the postgres returning section.
        2. Here we inject the Posrgres "tail" we set up above.

=== "--builtin-generator JavaJCCode"
    === "Insert"
        ```java
        public void insert() throws SQLException
        {
          String statement = 
            "/* PROC BooksAndAuthors.ToDoList.Insert */"
          + "insert into BooksAndAuthors.ToDoList ("
          + "  ListName,"
          + "  ListType,"
          + "  Description,"
          + "  LastUpdated"
          + " ) "
          + " values ("
          + "  ?,"
          + "  ?,"
          + "  ?,"
          + "  ?"
          + " )"
          ;
          PreparedStatement prep = connector.prepareStatement(statement);
          lastUpdated = connector.getTimestamp();
          prep.setString(1, listName);
          prep.setShort(2, listType);
          prep.setString(3, description);
          prep.setTimestamp(4, lastUpdated);
          prep.executeUpdate();
          prep.close();
        }
        ```
    === "Insert Returning"
        ```java hl_lines="3 21"
        public boolean insert() throws SQLException
        {
          Connector.Returning _ret = connector.getReturning("ToDoList","ID"); // (1)!
          String statement = 
            "/* PROC BooksAndAuthors.ToDoList.Insert */"
          + "insert into BooksAndAuthors.ToDoList ("
          + "  ID,"
          + "  ListName,"
          + "  ListType,"
          + "  Description,"
          + "  LastUpdated"
          + " ) "
          +  {_ret.output} 
          + " values ("
          +  {_ret.sequence} 
          + "  ?,"
          + "  ?,"
          + "  ?,"
          + "  ?"
          + " )"
          +  {_ret.tail} // (2)!
          ;
          PreparedStatement prep = connector.prepareStatement(statement);
          lastUpdated = connector.getTimestamp();
          prep.setString(1, listName);
          prep.setShort(2, listType);
          prep.setString(3, description);
          prep.setTimestamp(4, lastUpdated);
          ResultSet result = prep.executeQuery();
          if (!result.next())
          {
            result.close();
            prep.close();
            return false;
          }
          id =  result.getInt(1);
          result.close();
          prep.close();
          return true;
        }
        ```

        1. Different database engine have different ways of returning records. Postgres requires a postfix or "tail"
           which specifies which colunns to return. Here we are setting up the postgres returning section.
        2. Here we inject the Posrgres "tail" we set up above.



### **BulkInsert**
Insert new records in bulk.

``` linenums="0"
BulkInsert [<rowcount>] 
```

BulkInsert will insert records into a table in bulk.  
Adding the `<rowcount>` clause will cause JPortal2 to batch the inserts into batches of size <rowcount>.  
If `<rouwcount>` is omitted, JPortal2 will default to batches of size 1 000.

=== "--template-generator SQLAlchemy"
    !!! warning
        **NOT CURRENTLY SUPPORTED IN SQLALCHEMY GENERATOR**
=== "--builtin-generator JavaJCCode"
    ```java
      public void bulkInsert(List<ToDoList> records) throws SQLException
      {
        String statement = 
          "/* PROC BooksAndAuthors.ToDoList.BulkInsert */"
        + "insert into BooksAndAuthors.ToDoList ("
        + "  ListName,"
        + "  ListType,"
        + "  Description,"
        + "  LastUpdated"
        + " ) "
        + " values ("
        + "  ?,"
        + "  ?,"
        + "  ?,"
        + "  ?"
        + " )"
        ;
        for (int batchSize=0; batchSize <= Math.ceil(records.size()/1000); batchSize++ ) {
            PreparedStatement prep = connector.prepareStatement(statement);
            for (int recCount=(batchSize*1000); recCount < (batchSize+1)*1000 && recCount < records.size(); recCount++) {
                ToDoList record = records.get(recCount);
                lastUpdated = connector.getTimestamp();
                prep.setString(1, record.listName);
                prep.setShort(2, record.listType);
                prep.setString(3, record.description);
                prep.setTimestamp(4, lastUpdated);
                prep.addBatch();
            }
            prep.executeBatch();
            prep.close();
        }
      }
    
    ```
