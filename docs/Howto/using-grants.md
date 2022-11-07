JPortal2 allows you to specify database permission grants.

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

GRANT all TO db_user

```

### **GRANT**
Grants permissions to a db user.

``` linenums="0"
GRANT  
ALL | DELETE | INSERT | SELECT | UPDATE | EXECUTE
TO 
<user>+ 
```

`Grant` will grant permissions to database user.  
`ALL | DELETE | INSERT | SELECT | UPDATE | EXECUTE` specifies the permissions to grant.  
`user` is/are the database user(s) to grant permissions to.
