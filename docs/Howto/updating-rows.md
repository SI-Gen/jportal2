## Built-In Procs
JPortal ships with a number of built-in procs to allow easy updating of data into a database.

Let assume the following table structure in our SI, with various procs defined to update records:

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
<a href="#update">PROC Update</a>
<a href="#updateby">PROC UpdateBy ListName</a>
<a href="#updateby">PROC UpdateBy ListName SET ListType Description AS UpdateListTypeDescriptionByListName</a>

<</code>
</pre>

Let's work through the above.

### **Update**
Updates an existing record by primary key.

``` linenums="0"
Update
```

Update will update an existing record in a table, by primary key. If no primary key is specified on the table,
JPortal2 will issue a warning at compile time.

!!! note
    Note that Update will update ALL the columns of the record. If you want to only update certain columns, 
    consider using [UpdateBy](#updateby) or a [custom proc](./custom-procs.md) to achieve what you want.

=== "--template-generator SQLAlchemy"
    ```python 
    @dataclass
    class DB_ToDoListUpdate:
    # Enum for ListType field
    class ListTypeEnum(enum.Enum):
        Private = 1
        Public = 2

        @classmethod
        def process_result_value_cls(cls, value, dialect):
            return DB_ToDoListUpdate.ListTypeEnum(value)


    

        @classmethod
        def get_statement(cls
                         , ListName: str
                         , ListType: ListTypeEnum
                         , Description: str
                         , LastUpdated: datetime
                         , ID: int
                         ) -> TextAsFrom:
            class _ret:
                sequence = "default," #postgres uses default for sequences
                output = ""
                tail = ""
                #session.bind.dialect.name
    
            statement = sa.text(
                            f"update ToDoList_App.ToDoList"
                            f" set"
                            f"  ListName = :ListName"
                            f", ListType = :ListType"
                            f", Description = :Description"
                            f", LastUpdated = :LastUpdated"
                            f" where ID = :ID")
    
            text_statement = statement.columns()
            text_statement = text_statement.bindparams(ListName=ListName,
                                             ListType=ListType,
                                             Description=Description,
                                             LastUpdated=LastUpdated,
                                             ID=ID,
                                             )
            return text_statement
    
        @classmethod
        def execute(cls, session: Session, ListName: str
                         , ListType: ListTypeEnum
                         , Description: str
                         , LastUpdated: datetime
                         , ID: int
                         ) -> None:
            params = process_bind_params(session, [db_types.NonNullableString,
                                            sa.types.SmallInteger,
                                            db_types.NonNullableString,
                                            sa.types.DateTime,
                                            sa.types.Integer,
                                            ], [ListName,
                                            ListType.value if isinstance(ListType, enum.Enum) else ListType,
                                            Description,
                                            LastUpdated,
                                            ID,
                                            ])
            res = session.execute(cls.get_statement(*params))
            res.close()

    ```

=== "--builtin-generator JavaJCCode"
    ```java
      public void update() throws SQLException
      {
        String statement = 
          "update BooksAndAuthors.ToDoList"
        + " set"
        + "  ListName = ?"
        + ", ListType = ?"
        + ", Description = ?"
        + ", LastUpdated = ?"
        + " where ID = ?"
        ;
        PreparedStatement prep = connector.prepareStatement(statement);
        lastUpdated = connector.getTimestamp();
        prep.setString(1, listName);
        prep.setShort(2, listType);
        prep.setString(3, description);
        prep.setTimestamp(4, lastUpdated);
        prep.setInt(5, id);
        prep.executeUpdate();
        prep.close();
      }

    ```

### **UpdateBy**
Updates an existing record by user specified keys.

``` linenums="0"
UpdateBy <UpdateKeys>+ 
[SET <ColumnToUpdate>+]
[AS <alias>] 
```

UpdateBy will update the columns of a record that are specified by the `SET` clause, by the keys specified
by `UpdateKeys`.  
The `AS` clause specifies a custom name for the proc. If `AS` is omitted, the name will default to
`UpdateBy<SelectKeys>`.  

=== "--template-generator SQLAlchemy"
    ```python
    @dataclass
    class DB_ToDoListUpdateBy:
        # Enum for ListType field
        class ListTypeEnum(enum.Enum):
            Private = 1
            Public = 2
    
        @classmethod
        def process_result_value_cls(cls, value, dialect):
            return DB_ToDoListUpdateBy.ListTypeEnum(value)

    
        
    
        @classmethod
        def get_statement(cls
                         , ListType: ListTypeEnum
                         , Description: str
                         , LastUpdated: datetime
                         , ListName: str
                         ) -> TextAsFrom:
            class _ret:
                sequence = "default," #postgres uses default for sequences
                output = ""
                tail = ""
                #session.bind.dialect.name
    
            statement = sa.text(
                            f"/* PROC ToDoList_App.ToDoList.UpdateBy */"
                            f"update ToDoList_App.ToDoList"
                            f" set"
                            f"  ListType = :ListType"
                            f", Description = :Description"
                            f", LastUpdated = :LastUpdated"
                            f" where ListName = :ListName")
    
            text_statement = statement.columns()
            text_statement = text_statement.bindparams(ListType=ListType,
                                             Description=Description,
                                             LastUpdated=LastUpdated,
                                             ListName=ListName,
                                             )
            return text_statement
    
        @classmethod
        def execute(cls, session: Session, ListType: ListTypeEnum
                         , Description: str
                         , LastUpdated: datetime
                         , ListName: str
                         ) -> None:
            params = process_bind_params(session, [sa.types.SmallInteger,
                                            db_types.NonNullableString,
                                            sa.types.DateTime,
                                            db_types.NonNullableString,
                                            ], [ListType.value if isinstance(ListType, enum.Enum) else ListType,
                                            Description,
                                            LastUpdated,
                                            ListName,
                                            ])
            res = session.execute(cls.get_statement(*params))
            res.close() 
    ```

=== "--builtin-generator JavaJCCode"
    ```java
      public void updateBy() throws SQLException
      {
        String statement = 
          "/* PROC BooksAndAuthors.ToDoList.UpdateBy */"
        + "update BooksAndAuthors.ToDoList"
        + " set"
        + "  ListType = ?"
        + ", Description = ?"
        + ", LastUpdated = ?"
        + " where ListName = ?"
        ;
        PreparedStatement prep = connector.prepareStatement(statement);
        lastUpdated = connector.getTimestamp();
        prep.setShort(1, listType);
        prep.setString(2, description);
        prep.setTimestamp(3, lastUpdated);
        prep.setString(4, listName);
        prep.executeUpdate();
        prep.close();
      }
    ```


### **BulkUpdate**
Updates existing records in bulk.

``` linenums="0"
BulkUpdate [<rowcount>] 
```

BulkUpdate will update existing records in a table in bulk.  
Adding the `<rowcount>` clause will cause JPortal2 to batch the updates into batches of size <rowcount>.  
If `<rouwcount>` is omitted, JPortal2 will default to batches of size 1 000.

=== "--template-generator SQLAlchemy"
    !!! warning
        **NOT CURRENTLY SUPPORTED IN SQLALCHEMY GENERATOR**
=== "--builtin-generator JavaJCCode"
    ```java
    public void bulkUpdate(List<ToDoList> records) throws SQLException
      {
        String statement = 
          "update BooksAndAuthors.ToDoList"
        + " set"
        + "  ListName = ?"
        + ", ListType = ?"
        + ", Description = ?"
        + ", LastUpdated = ?"
        + " where ID = ?"
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
                prep.setInt(5, record.id);
                prep.addBatch();
            }
            prep.executeBatch();
            prep.close();
        }
      }
    ```
