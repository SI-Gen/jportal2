To create indexes in JPortal2 is easy:

``` hl_lines="13-14"
DATABASE ExampleDatabase
PACKAGE com.example.db
SERVER ExampleServer
SCHEMA BooksAndAuthors

TABLE Books
   ID               SEQUENCE
   AuthorID         INT
   Title            CHAR(255)
   PublishDate      DATE
   LastUpdated      TIMESTAMP

KEY PKEY PRIMARY
    ID
    
LINK Authors DELETE CASCADE AuthorID 
```

## Primary Keys and Indexes

### **KEY**
Creates a primary key or index on a table.

``` linenums="0"
KEY <key_name> 
[OPTIONS ( <options_name>+ ]
[PRIMARY | UNIQUE ] 
<key_columns>+
```

`Key` will create a primary key or index on a table.  
`<key_name>` is the name of the index to be created.  
`OPTIONS` specifies the options for the index. These options get passed through to the underlying generator, so the
available options depend on the generator itself, but one example is `OPTIONS tablespace ABC` will place the index in
the ABC tablespace when using the Db2DDL generator.  
`PRIMARY` indicates that you want to create a primary key, as opposed to an index.  
`UNIQUE` indicates that the index must be a unique index.  
`<key_columns` contains the names of the columns to be included in the index

## Foreign Keys

### **LINK**
Create a foreign key to a parent table.

``` linenums="0"
LINK <parent_table> 
[DELETE [CASCADE]]
[UPDATE [CASCADE]]
[OPTIONS ( <options_name>+ ] 
<key_columns>+
```

`Link` will create a a foreign key to a parent table.  
`<parent_table>` is the name of the parent table to create the foreign key to.  
`DELETE CASCADE` and `UPDATE CASCADE` will specify cascading deletes or updates to the parent table.  
`OPTIONS` specifies the options for the index. These options get passed through to the underlying generator, so the
available options depend on the generator itself.  
`<key_columns` contains the names of the columns in this SI to be used for the foreign key link. The foreign key will
always be created to the Primary Key of the parent table.  
