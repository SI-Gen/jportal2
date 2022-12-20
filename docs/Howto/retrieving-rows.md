## Built-In Procs
JPortal ships with a number of built-in procs to allow easy querying of a database.

Let assume the following table structure in our SI, with various procs defined to retrieve records:

<pre>
<code>
DATABASE ExampleDatabase
PACKAGE com.example.db
SERVER ExampleServer
SCHEMA BooksAndAuthors

TABLE Authors
   ID               SEQUENCE
   Bio              CHAR
   BirthDate        DATE
   LastUpdated      TIMESTAMP

KEY PKEY PRIMARY
    ID


//Simple CRUD queries are available out of the box with JPortal2
<a href="#selectone">PROC SelectOne</a>
<a href="#selectall">PROC SelectAll</a>
<a href="#selectoneby">PROC SelectOneBy</a> Bio
<a href="#selectby">PROC SelectBy</a> BirthDate Returning Bio
<a href="#selectby">PROC SelectBy</a> BirthDate As FancySelectByBirthDate
OUTPUT
    ID          =
    Bio         =
    BirthDate   =
</code>
</pre>

Let's work through the above.  

### **SelectOne**
Selects a record by its primary key.

``` linenums="0"
SelectOne  
[ FOR <UPDATE | READONLY> ]
```

SelectOne will select all the fields of a table, by primary key.
Adding the FOR UPDATE clause will cause JPortal2 to add the text `FOR UPDATE` to the query. Use this to lock a record
for when you want to select it, change on or more fields, and then update it.
Adding the FOR READONLY clause will cause JPortal2 to add the text "FOR READONLY" to the query. Use this to 
put a READONLY lock on a record.

=== "--template-generator SQLAlchemy"
    === "SelectOne" 
        ```python
        @dataclass
        class DB_AuthorsSelectOne:
            #Outputs
            Bio: str
            BirthDate: datetime
            LastUpdated: datetime
        
            @classmethod
            def get_statement(cls
                             , ID: int
                             ) -> TextAsFrom:
        
                statement = sa.text(
                                f"/* PROC BooksAndAuthors.Authors.SelectOne */"
                                f"select"
                                f"  Bio"
                                f", BirthDate"
                                f", LastUpdated"
                                f" from BooksAndAuthors.Authors"
                                f" where ID = :ID")
        
                text_statement = statement.columns(Bio=db_types.NonNullableString,
                                              BirthDate=sa.types.DateTime,
                                              LastUpdated=sa.types.DateTime,
                                              )
                text_statement = text_statement.bindparams(ID=ID,
                                                 )
                return text_statement
        
            @classmethod
            def execute(cls, session: Session, ID: int
                             ) -> Optional['DB_AuthorsSelectOne']:
                params = process_bind_params(session, [sa.types.Integer,
                                                ], [ID,
                                                ])
                res = session.execute(cls.get_statement(*params))
                rec = res.fetchone()
                if rec:
                    res.close()
                    return process_result_rec(DB_AuthorsSelectOne, session, [db_types.NonNullableString,
                                                sa.types.DateTime,
                                                sa.types.DateTime,
                                                ], rec)
        
                return None
        ```
    === "SelectOne FOR UPDATE"
    
        ```python
        @dataclass
        class DB_AuthorsSelectOneUpd:
            #Outputs
            Bio: str
            BirthDate: datetime
            LastUpdated: datetime
        
            @classmethod
            def get_statement(cls
                             , ID: int
                             ) -> TextAsFrom:
        
                statement = sa.text(
                                f"/* PROC BooksAndAuthors.Authors.SelectOneUpd */"
                                f"select"
                                f"  Bio"
                                f", BirthDate"
                                f", LastUpdated"
                                f" from BooksAndAuthors.Authors"
                                f" where ID = :ID"
                                f" for update")
        
                text_statement = statement.columns(Bio=db_types.NonNullableString,
                                              BirthDate=sa.types.DateTime,
                                              LastUpdated=sa.types.DateTime,
                                              )
                text_statement = text_statement.bindparams(ID=ID,
                                                 )
                return text_statement
        
            @classmethod
            def execute(cls, session: Session, ID: int
                             ) -> Optional['DB_AuthorsSelectOneUpd']:
                params = process_bind_params(session, [sa.types.Integer,
                                                ], [ID,
                                                ])
                res = session.execute(cls.get_statement(*params))
                rec = res.fetchone()
                if rec:
                    res.close()
                    return process_result_rec(DB_AuthorsSelectOneUpd, session, [db_types.NonNullableString,
                                                sa.types.DateTime,
                                                sa.types.DateTime,
                                                ], rec)
        
                return None
        ```
    === "SelectOne FOR READONLY"
    
        ```python
        @dataclass
        class DB_AuthorsSelectOneReadOnly:
            #Outputs
            Bio: str
            BirthDate: datetime
            LastUpdated: datetime
        
            @classmethod
            def get_statement(cls
                             , ID: int
                             ) -> TextAsFrom:
        
                statement = sa.text(
                                f"/* PROC BooksAndAuthors.Authors.SelectOneUpd */"
                                f"select"
                                f"  Bio"
                                f", BirthDate"
                                f", LastUpdated"
                                f" from BooksAndAuthors.Authors"
                                f" where ID = :ID"
                                f" for read only")
        
                text_statement = statement.columns(Bio=db_types.NonNullableString,
                                              BirthDate=sa.types.DateTime,
                                              LastUpdated=sa.types.DateTime,
                                              )
                text_statement = text_statement.bindparams(ID=ID,
                                                 )
                return text_statement
        
            @classmethod
            def execute(cls, session: Session, ID: int
                             ) -> Optional['DB_AuthorsSelectOneUpd']:
                params = process_bind_params(session, [sa.types.Integer,
                                                ], [ID,
                                                ])
                res = session.execute(cls.get_statement(*params))
                rec = res.fetchone()
                if rec:
                    res.close()
                    return process_result_rec(DB_AuthorsSelectOneUpd, session, [db_types.NonNullableString,
                                                sa.types.DateTime,
                                                sa.types.DateTime,
                                                ], rec)
        
                return None
        ```
=== "--builtin-generator JavaJCCode"
    === "SelectOne"
        ```java
        public boolean selectOne() throws SQLException
        {
          String statement =
            "/* PROC ToDoList_App.Authors.SelectOne */"
          + "select"
          + "  Bio"
          + ", BirthDate"
          + ", LastUpdated"
          + " from ToDoList_App.Authors"
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
          bio =  result.getString(1);
          birthDate =  result.getDate(2);
          lastUpdated =  result.getTimestamp(3);
          result.close();
          prep.close();
          return true;
        }
        ```
    
    === "SelectOne FOR UPDATE"
        ```java
        public boolean selectOneUpd() throws SQLException
        {
          String statement =
            "/* PROC ToDoList_App.Authors.SelectOneUpd */"
          + "select"
          + "  Bio"
          + ", BirthDate"
          + ", LastUpdated"
          + " from ToDoList_App.Authors"
          + " where ID = ?"
          + " for update"
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
          bio =  result.getString(1);
          birthDate =  result.getDate(2);
          lastUpdated =  result.getTimestamp(3);
          result.close();
          prep.close();
          return true;
        }
        ```
    === "SelectOne FOR READONLY"
        ```java
        public boolean selectOneReadOnly() throws SQLException
        {
          String statement =
            "/* PROC ToDoList_App.Authors.SelectOneReadOnly */"
          + "select"
          + "  Bio"
          + ", BirthDate"
          + ", LastUpdated"
          + " from ToDoList_App.Authors"
          + " where ID = ?"
          + " for read only"
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
          bio =  result.getString(1);
          birthDate =  result.getDate(2);
          lastUpdated =  result.getTimestamp(3);
          result.close();
          prep.close();
          return true;
        }
        ```

### **SelectAll**
Selects all the records in a table.

``` sql linenums="0"
SelectAll   
[ [IN] ORDER <OrderColumnName>* [DESC]]   
[ FOR <UPDATE | READONLY> ]
```

SelectAll will select all the records of a table.

Adding the IN ORDER clause will add `ORDER BY <specified columns>` to the query. Specify 

Adding the FOR UPDATE clause will cause JPortal2 to add the text `FOR UPDATE` to the query. Use this to lock a record
for when you want to select it, change on or more fields, and then update it.
Adding the FOR READONLY clause will cause JPortal2 to add the text "FOR READONLY" to the query. Use this to
put a READONLY lock on a record. See [SelectOne](#selectone) for an example of `FOR <UPDATE | READONLY>`.

=== "--template-generator SQLAlchemy"
    === "SelectAll"

        ```python
        @dataclass
        class DB_AuthorsSelectAll:
            #Outputs
            ID: int
            Bio: str
            BirthDate: datetime
            LastUpdated: datetime

            @classmethod
            def get_statement(cls
                             ) -> TextAsFrom:
        
                statement = sa.text(
                                f"/* PROC BooksAndAuthors.Authors.SelectAll */"
                                f"select"
                                f"  ID"
                                f", Bio"
                                f", BirthDate"
                                f", LastUpdated"
                                f" from BooksAndAuthors.Authors")
        
                text_statement = statement.columns(ID=sa.types.Integer,
                                              Bio=db_types.NonNullableString,
                                              BirthDate=sa.types.DateTime,
                                              LastUpdated=sa.types.DateTime,
                                              )
                return text_statement
        
            @classmethod
            def execute(cls, session: Session) -> List['DB_AuthorsSelectAll']:
                res = session.execute(cls.get_statement())
                recs = res.fetchall()
                return process_result_recs(DB_AuthorsSelectAll, session, [sa.types.Integer,
                                                db_types.NonNullableString,
                                                sa.types.DateTime,
                                                sa.types.DateTime,
                                                ], recs)
    
        ```
    === "SelectAll IN ORDER"
    
        ```python
        @dataclass
        class DB_AuthorsSelectAllSorted:
            #Outputs
            ID: int
            Bio: str
            BirthDate: datetime
            LastUpdated: datetime
    
            @classmethod
            def get_statement(cls
                             ) -> TextAsFrom:
        
                statement = sa.text(
                                f"/* PROC BooksAndAuthors.Authors.SelectAllSorted */"
                                f"select"
                                f"  ID"
                                f", Bio"
                                f", BirthDate"
                                f", LastUpdated"
                                f" from BooksAndAuthors.Authors"
                                f" order by Bio"
                                f", ID desc")
        
                text_statement = statement.columns(ID=sa.types.Integer,
                                              Bio=db_types.NonNullableString,
                                              BirthDate=sa.types.DateTime,
                                              LastUpdated=sa.types.DateTime,
                                              )
                return text_statement
        
            @classmethod
            def execute(cls, session: Session) -> List['DB_AuthorsSelectAllSorted']:
                res = session.execute(cls.get_statement())
                recs = res.fetchall()
                return process_result_recs(DB_AuthorsSelectAllSorted, session, [sa.types.Integer,
                                                db_types.NonNullableString,
                                                sa.types.DateTime,
                                                sa.types.DateTime,
                                                ], recs)
        ```

=== "--builtin-generator JavaJCCode"
    === "SelectAll"
        ```java
        /**
        * Returns any number of records.
        * @return result set of records found
        * @exception SQLException is passed through
        */
        public Query selectAll() throws SQLException
        {
        String statement =
          "/* PROC ToDoList_App.Authors.SelectAllSorted */"
        + "select"
        + "  ID"
        + ", Bio"
        + ", BirthDate"
        + ", LastUpdated"
        + " from ToDoList_App.Authors"
        ;
        PreparedStatement prep = connector.prepareStatement(statement);
        ResultSet result = prep.executeQuery();
        Query query = new Query(prep, result);
        return query;
        } 
        ```

    === "SelectAll IN ORDER"
        ```java
        /**
        * Returns any number of records.
        * @return result set of records found
        * @exception SQLException is passed through
        */
        public Query selectAllSorted() throws SQLException
        {
        String statement =
          "/* PROC ToDoList_App.Authors.SelectAllSorted */"
        + "select"
        + "  ID"
        + ", Bio"
        + ", BirthDate"
        + ", LastUpdated"
        + " from ToDoList_App.Authors"
        + " order by Bio"
        + ", ID desc"
        ;
        PreparedStatement prep = connector.prepareStatement(statement);
        ResultSet result = prep.executeQuery();
        Query query = new Query(prep, result);
        return query;
        } 
        ```
### **SelectOneBy**
Selects a record by user specified columns.

```
SelectOneBy <SelectColumns>+
[ FOR <UPDATE | READONLY> ]
```

SelectOneBy will select all the fields of a table, by the keys specified by `SelectColumns`.  
Adding the FOR UPDATE clause will cause JPortal2 to add the text `FOR UPDATE` to the query. Use this to lock a record
for when you want to select it, change on or more fields, and then update it.  
Adding the FOR READONLY clause will cause JPortal2 to add the text "FOR READONLY" to the query. Use this to 
put a READONLY lock on a record. See [SelectOne](#selectone) for an example of `FOR <UPDATE | READONLY>`.  

=== "--template-generator SQLAlchemy"
    === "SelectOneBy" 
        ```python
        @dataclass
        class DB_AuthorsSelectOneByBio:
            #Outputs
            ID: int
            Bio: str
            BirthDate: datetime
            LastUpdated: datetime
        
            @classmethod
            def get_statement(cls
                             , Bio: str
                             ) -> TextAsFrom:
                class _ret:
                    sequence = "default," #postgres uses default for sequences
                    output = " OUTPUT (ID,Bio,BirthDate,LastUpdated)"
                    tail = " RETURNING ID Bio BirthDate LastUpdated"
                    #session.bind.dialect.name
        
                statement = sa.text(
                                f"/* PROC BooksAndAuthors.Authors.SelectOneByBio */"
                                f"select"
                                f"  ID"
                                f", Bio"
                                f", BirthDate"
                                f", LastUpdated"
                                f" from BooksAndAuthors.Authors"
                                f" for update"
                                f" where Bio = :Bio")
        
                text_statement = statement.columns(ID=sa.types.Integer,
                                              Bio=db_types.NonNullableString,
                                              BirthDate=sa.types.DateTime,
                                              LastUpdated=sa.types.DateTime,
                                              )
                text_statement = text_statement.bindparams(Bio=Bio,
                                                 )
                return text_statement
        
            @classmethod
            def execute(cls, session: Session, Bio: str
                             ) -> Optional['DB_AuthorsSelectOneByBio']:
                params = process_bind_params(session, [db_types.NonNullableString,
                                                ], [Bio,
                                                ])
                res = session.execute(cls.get_statement(*params))
                rec = res.fetchone()
                if rec:
                    res.close()
                    return process_result_rec(DB_AuthorsSelectOneByBio, session, [sa.types.Integer,
                                                db_types.NonNullableString,
                                                sa.types.DateTime,
                                                sa.types.DateTime,
                                                ], rec)
        
                return None
        ```

=== "--builtin-generator JavaJCCode"
    === "SelectOneBy"
        ```java
        /**
        * Returns at most one record.
        * @return true if a record is found
        * @exception SQLException is passed through
        */
        public boolean selectOneByBio() throws SQLException
        { 
            String statement = 
              "/* PROC ToDoList_App.Authors.SelectOneByBio */"
            + "select"
            + "  ID"
            + ", Bio"
            + ", BirthDate"
            + ", LastUpdated"
            + " from ToDoList_App.Authors"
            + " for update"
            + " where Bio = ?"
            ;
             PreparedStatement prep = connector.prepareStatement(statement);
             prep.setString(1, bio);
             ResultSet result = prep.executeQuery();
             if (!result.next())
             {
               result.close();
               prep.close();
               return false;
             }
             id =  result.getInt(1);
             bio =  result.getString(2);
             birthDate =  result.getDate(3);
             lastUpdated =  result.getTimestamp(4);
             result.close();
             prep.close();
             return true;
        }
        ```

### **SelectBy**
Selects record by user specified keys,columns returning columns specified by user

```
SelectBy <SelectColumns>+
[ [IN] ORDER <OrderColumnName>* [DESC]]   
[ FOR UPDATE | READONLY ]
[AS <alias>]
[RETURNING <columns to return>]
```

SelectBy will select the fields of a table that are specified by the `RETURNING` clause, by the keys specified 
by `SelectColumns`.  
Adding the FOR UPDATE clause will cause JPortal2 to add the text `FOR UPDATE` to the query. Use this to lock a record
for when you want to select it, change on or more fields, and then update it.  
Adding the FOR READONLY clause will cause JPortal2 to add the text "FOR READONLY" to the query. Use this to
put a READONLY lock on a record. See [SelectOne](#selectone) for an example of `FOR <UPDATE | READONLY>`.  
the `AS` clause specifies a custom name for the proc. If `AS` is omitted, the name will default to
`SelectBy<SelectColumns>`.  
The `RETURNING` clause specifies the fields that must be returned, If it is omitted, all the fields in the table
will be returned.  

=== "--template-generator SQLAlchemy"
    === "SelectBy"
        ```python 
                @dataclass
                class DB_AuthorsSelectByBirthDate:
                #Outputs
                Bio: str
        
                @classmethod
                def get_statement(cls
                                 , BirthDate: datetime
                                 ) -> TextAsFrom:
                    class _ret:
                        sequence = "default," #postgres uses default for sequences
                        output = " OUTPUT (Bio)"
                        tail = " RETURNING Bio"
                        #session.bind.dialect.name
            
                    statement = sa.text(
                                    f"/* PROC BooksAndAuthors.Authors.SelectByBirthDate */"
                                    f"select"
                                    f"  Bio"
                                    f" from BooksAndAuthors.Authors"
                                    f" where BirthDate = :BirthDate")
            
                    text_statement = statement.columns(Bio=db_types.NonNullableString,
                                                  )
                    text_statement = text_statement.bindparams(BirthDate=BirthDate,
                                                     )
                    return text_statement
            
                @classmethod
                def execute(cls, session: Session, BirthDate: datetime
                                 ) -> List['DB_AuthorsSelectByBirthDate']:
                    params = process_bind_params(session, [sa.types.DateTime,
                                                    ], [BirthDate,
                                                    ])
                    res = session.execute(cls.get_statement(*params))
                    recs = res.fetchall()
                    return process_result_recs(DB_AuthorsSelectByBirthDate, session, [db_types.NonNullableString,
                                                    ], recs)
        ```
=== "--builtin-generator JavaJCCode"
    === "SelectOneBy"
        ```java
            /**
            * Returns any number of records.
            * @return result set of records found
            * @exception SQLException is passed through
            */
            public Query selectByBirthDate() throws SQLException
            {
                String statement =
                "/* PROC ToDoList_App.Authors.SelectByBirthDate */"
              + "select"
              + "  Bio"
              + " from ToDoList_App.Authors"
              + " where BirthDate = ?"
              ;
              PreparedStatement prep = connector.prepareStatement(statement);
              prep.setDate(1, birthDate);
              ResultSet result = prep.executeQuery();
              Query query = new Query(prep, result);
              return query;
           }
        ```

## Custom Procs
If the built-in select functions don't give you the power and flexibility you need, you can write your own 
select function using a [Custom Proc](custom-procs.md), which is covered in the next section.


