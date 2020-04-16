|Generator name|Generator Output                     |Output file extension|Freemarker or Java|Builtin|
|:-------------|:------------------------------------|:--------------------|:-----------------|:------|
|MSSqlDDL      |Script SI file as Database Definition|.sql                 |Java              |True   |


# Supported types

All SI types can be appended with ` NULL`, for example `COLNAME <DB TYPE> NULL`, to make the field nullable. If ` NULL` has NOT been specified the field will generate as: `COLNAME <DB TYPE> NOT NULL`

|SI Type                       |Database Type                  |Condition                    |SI Example    |Generated Example    |
|:-----------------------------|:------------------------------|:----------------------------|:-------------|:--------------------|
|CHAR(\<length>)               |VARCHAR(\<length>)             |\<length> <= 8000            |CHAR(500)     |VARCHAR(500)         |
|CHAR(\<length>)               |VARCHAR(MAX)                   |\<length> > 8000             |CHAR(9000)    |VARCHAR(MAX)         |
|ANSICHAR(\<length>)           |CHAR(\<length>)                |                             |ANSICHAR(500) |CHAR(500)            |
|BOOLEAN                       |BIT                            |                             |BOOLEAN       |BIT                  |
|BYTE                          |TINYINT                        |                             |BYTE          |TINYINT              |
|SHORT                         |SMALLINT                       |                             |SHORT         |SMALLINT             |
|INT                           |INT                            |                             |INT           |INT                  |
|LONG                          |BIGINT                         |                             |LONG          |BIGINT               |
|FLOAT                         |FLOAT                          |                             |FLOAT         |FLOAT                |
|FLOAT(\<precision>, \<scale>) |FLOAT                          |\<precision> <= 15           |FLOAT(10, 10) |FLOAT                |
|FLOAT(\<precision>, \<scale>) |DECIMAL(\<precision>, \<scale>)|\<precision> > 15            |FLOAT(20, 10) |DECIMAL(20, 10)      |
|DOUBLE                        |FLOAT                          |                             |DOUBLE        |FLOAT                |
|DOUBLE(\<precision>, \<scale>)|FLOAT                          |\<precision> <= 15           |DOUBLE(10, 10)|FLOAT                |
|DOUBLE(\<precision>, \<scale>)|DECIMAL(\<precision>, \<scale>)|\<precision> > 15            |DOUBLE(20, 10)|                     |
|IDENTITY                      |INTEGER IDENTITY(1,1)          |                             |IDENTITY      |INTEGER IDENTITY(1,1)|
|BIGIDENTITY                   |BIGINT IDENTITY(1,1)           |                             |BIGIDENTITY   |BIGINT IDENTITY(1,1) |
|SEQUENCE                      |INTEGER                        |hasSequenceReturning == False|SEQUENCE      |INTEGER              |
|SEQUENCE                      |INTEGER IDENTITY(1,1)          |hasSequenceReturning == True |SEQUENCE      |INTEGER IDENTITY(1,1)|
|BIGSEQUENCE                   |BIGINT                         |hasSequenceReturning == False|BIGSEQUENCE   |BIGINT               |
|BIGSEQUENCE                   |BIGINT IDENTITY(1,1)           |hasSequenceReturning == True |BIGSEQUENCE   |BIGINT IDENTITY(1, 1)|
|DATE                          |DATETIME                       |                             |DATE          |DATETIME             |
|DATETIME                      |DATETIME                       |                             |DATETIME      |DATETIME             |
|BLOB                          |IMAGE                          |                             |BLOB          |IMAGE                |
|BIGXML                        |XML                            |                             |BIGXML        |XML                  |
|XML                           |XML                            |                             |XML           |XML                  |
|MONEY                         |MONEY                          |                             |MONEY         |MONEY                |
|USERSTAMP                     |VARCHAR(50)                    |                             |USERSTAMP     |VARCHAR(50)          |
|UID                           |UNIQUEIDENTIFIER               |                             |UID           |UNIQUEIDENTIFIER     |
|undefined SI type             |unkown                         |                             |*             |unknown              |

The `hasSequenceReturning` variable is `False` by defualt and is set to `True` if the standard INSERT proc has the RETURNING keyword:
```
PROC INSERT RETURNING
```

# Automatic Adding of Fields
## TmStamp
The TmStamp field can automatically be added to the table by specifying the `--flag "add timestamp"` flag:
```
TABLE Example
    ID                  SEQUENCE
    SOME_DATA           CHAR(100)
    USER_ID             USERSTAMP
```
Jportal Params:
```
--builtin-generator MSSqlDDL:\tmp --flag "add timestamp"
```
Example output:
```
CREATE TABLE ExampleSchema.Example
(
  ID INTEGER IDENTITY(1,1) NOT NULL
, SOME_DATA VARCHAR(100) NOT NULL
, USER_ID VARCHAR(50) NOT NULL
, TIMESTAMP
, CONSTRAINT PK_ExampleSchema_Example_PKEY PRIMARY KEY (
    ID
  )
)
GO
```

## UserId and TmStamp
UserId's and TmStamp fields can automatically be added to the table by specifing `--flag "internal stamps"` to the generator.
```
TABLE Example
    ID                  SEQUENCE
    SOME_DATA           CHAR(100)
```
Jportal Params:
```
--builtin-generator MSSqlDDL:\tmp --flag "internal stamps"
```
Example output:
```
CREATE TABLE ExampleSchema.Example
(
  ID INTEGER IDENTITY(1,1) NOT NULL
, SOME_DATA VARCHAR(100) NOT NULL
, UpdateWhen  DATETIME DEFAULT CURRENT_TIMESTAMP NULL
, UpdateByWho CHAR(8)  DEFAULT USER NULL 
, CONSTRAINT PK_ExampleSchema_Example_PKEY PRIMARY KEY (
    ID
  )
)
GO
```

# Triggers
## Insert trigger
An insert trigger can be used to automatically set `ID`, `TIMESTAMP` and `USERSTAMP` fields. In order for this to generate in the DDL you need to specify the Primary key of type `SEQUENCE` or `BIGSEQUENCE` and pass the `--flag "use insert trigger"` to the generator:

Example si file:
```
TABLE Example
    ID                  SEQUENCE
    SOME_DATA           CHAR(100)
    USER_ID             USERSTAMP
    TMSTAMP             TIMESTAMP
```
Jportal Params:
```
--builtin-generator MSSqlDDL:\tmp --flag "use insert trigger"
```

This will generate the DDL:
```
CREATE TABLE ExampleSchema.Example
(
  ID INTEGER NOT NULL
, SOME_DATA VARCHAR(100) NOT NULL
, USER_ID VARCHAR(50) NOT NULL
, TMSTAMP DATETIME NOT NULL
, CONSTRAINT PK_ExampleSchema_Example_PKEY PRIMARY KEY (
    ID
  )
)
GO

IF OBJECT_ID('ExampleSchema.ExampleInsertTrigger','TR') IS NOT NULL
    DROP TRIGGER ExampleSchema.ExampleInsertTrigger
GO

CREATE TRIGGER ExampleSchema.ExampleInsertTrigger ON ExampleSchema.Example FOR INSERT AS
UPDATE ExampleSchema.Example SET ID=ID+0
WHERE ID=(SELECT MAX(ID) FROM ExampleSchema.Example)
UPDATE ExampleSchema.Example
SET
  ID = (SELECT MAX(ID) FROM ExampleSchema.Example)+1
, USER_ID = USER_NAME()
, TMSTAMP = GETDATE()
WHERE ID = (SELECT ID FROM INSERTED)
GO
```


# Audit Trigger
An Audit trigger can be generated to track `INSERT`, `DELETE` and `UPDATE` queries performed on the table. For this to happed the flag `--flag "audit triggers"` should be passed to the generator. If the flag is passed a secondry table will be generated in the DDL called `<SCHEMA><TABLENAME>Audit`. When an `INSERT`, `UPDATE` or `DELETE` query is executed this table will be inserted into with:
```
ACTION
GETDATE()
<TABLE FIELDS>
```
Example SI file:
```
TABLE Example
    ID                  SEQUENCE
    SOME_DATA           CHAR(100)
    USER_ID             USERSTAMP
    TMSTAMP             TIMESTAMP
```
Jportal Params:
```
--builtin-generator MSSqlDDL:E:\temp --flag "audit triggers"
```
This will generate the DDL:
```
CREATE TABLE ExampleSchema.Example
(
  ID INTEGER NOT NULL
, SOME_DATA VARCHAR(100) NOT NULL
, USER_ID VARCHAR(50) NOT NULL
, TMSTAMP DATETIME NOT NULL
, CONSTRAINT PK_ExampleSchema_Example_PKEY PRIMARY KEY (
    ID
  )
)
GO

IF OBJECT_ID('ExampleSchema.ExampleUpdateTrigger','TR') IS NOT NULL
    DROP TRIGGER ExampleSchema.ExampleUpdateTrigger
GO

CREATE TRIGGER ExampleSchema.ExampleUpdateTrigger ON ExampleSchema.Example FOR UPDATE AS
UPDATE ExampleSchema.Example
SET
  USER_ID = USER_NAME()
, TMSTAMP = GETDATE()
FROM INSERTED I
WHERE ExampleSchema.ExampleID = I.ID 
GO

IF OBJECT_ID('ExampleSchemaExampleAudit','U') IS NOT NULL
    DROP TABLE ExampleSchemaExampleAudit
GO

CREATE TABLE ExampleSchemaExampleAudit
(
  AuditId INTEGER IDENTITY(1,1) NOT NULL PRIMARY KEY
, AuditAction INTEGER NOT NULL -- 1 = INSERT, 2 = DELETE, 3 = UPDATE
, AuditWhen DATETIME NOT NULL
, ID INTEGER NULL
, SOME_DATA VARCHAR(100) NULL
, USER_ID VARCHAR(50) NULL
, TMSTAMP DATETIME NULL
)
GO

IF OBJECT_ID('ExampleSchemaExampleAuditTrigger','TR') IS NOT NULL
    DROP TRIGGER ExampleSchemaExampleAuditTrigger
GO

CREATE TRIGGER ExampleSchemaExampleAuditTrigger ON ExampleSchemaExample
FOR INSERT, DELETE, UPDATE AS
BEGIN
  DECLARE @INSERT INT, @DELETE INT, @ACTION INT;
  SELECT @INSERT = COUNT(*) FROM INSERTED;
  SELECT @DELETE = COUNT(*) FROM DELETED;
  IF @INSERT > 0 SELECT @ACTION = 1 ELSE SELECT @ACTION = 0;
  IF @DELETE > 0 SELECT @ACTION = @ACTION + 2;
  -- 1 = INSERT, 2 = DELETE, 3 = UPDATE
  IF @ACTION = 2 BEGIN
    INSERT INTO ExampleSchemaExampleAudit
    SELECT @ACTION
         , GETDATE()
          , ID
          , SOME_DATA
          , USER_ID
          , TMSTAMP
    FROM DELETED;
  END ELSE
  BEGIN
    INSERT INTO ExampleSchemaExampleAudit
    SELECT @ACTION
         , GETDATE()
         , ID
         , SOME_DATA
         , USER_ID
         , TMSTAMP
    FROM INSERTED;
  END
END
GO
```


## Index Information
Indexes can be created using the `KEY` keyword in the SI file:

Example SI file:
```
TABLE Example
    ID                  SEQUENCE
    SOME_DATA           CHAR(100)
    USER_ID             USERSTAMP
    TMSTAMP             TIMESTAMP

KEY PKEY PRIMARY
    ID

KEY INX1
    SOME_DATA

KEY INX2
    ID
    SOME_DATA
```
Example output:
```
CREATE TABLE ExampleSchema.Example
(
  ID INTEGER IDENTITY(1,1) NOT NULL
, SOME_DATA VARCHAR(100) NOT NULL
, USER_ID VARCHAR(50) NOT NULL
, TMSTAMP DATETIME NOT NULL
, TIMESTAMP
, CONSTRAINT PK_ExampleSchema_Example_PKEY PRIMARY KEY (
    ID
  )
)
GO

CREATE INDEX INX1 ON ExampleSchema.Example
(
  SOME_DATA
)
GO

CREATE INDEX INX2 ON ExampleSchema.Example
(
  ID
, SOME_DATA
)
GO
```
## Unique constraints
Unique constraints can be generated by adding the `UNIQUE` keyword after the name of the index.

Example SI file:
```
TABLE Example
    ID                  SEQUENCE
    SOME_DATA           CHAR(100)
    USER_ID             USERSTAMP
    TMSTAMP             TIMESTAMP



KEY PKEY PRIMARY
    ID

KEY INX1 UNIQUE
    SOME_DATA
```

Example Output:
```
CREATE TABLE ExampleSchema.Example
(
  ID INTEGER IDENTITY(1,1) NOT NULL
, SOME_DATA VARCHAR(100) NOT NULL
, USER_ID VARCHAR(50) NOT NULL
, TMSTAMP DATETIME NOT NULL
, TIMESTAMP
, CONSTRAINT PK_ExampleSchema_Example_PKEY PRIMARY KEY (
    ID
  )
, CONSTRAINT UK_ExampleSchema_Example_INX1 UNIQUE (
    SOME_DATA
  )
)
GO
```
## Foreign Keys
Foreign Keys can be created on fields to other table fields by using the `LINK` keyword:

Example SI File:
```
TABLE Example
    ID                  SEQUENCE
    SOME_DATA           CHAR(100)
    USER_ID             USERSTAMP
    TMSTAMP             TIMESTAMP


LINK Example2.ExampleTable2 SOME_DATA
```

Example Output:
```
CREATE TABLE ExampleSchema.Example
(
  ID INTEGER IDENTITY(1,1) NOT NULL
, SOME_DATA VARCHAR(100) NOT NULL
, USER_ID VARCHAR(50) NOT NULL
, TMSTAMP DATETIME NOT NULL
, TIMESTAMP
, CONSTRAINT FK_ExampleSchema_Example_Example2_ExampleTable2_SOME_DATA FOREIGN KEY (
    SOME_DATA
  )
  REFERENCES Example2.ExampleTable2
)
GO
```

### Cascades
Cascade updates and deletions can be created using the `<operation> CASCADE` keyword on a link.

Example SI File:
```
TABLE Example
    ID                  SEQUENCE
    SOME_DATA           CHAR(100)
    USER_ID             USERSTAMP
    TMSTAMP             TIMESTAMP


LINK Example2.ExampleTable2 DELETE UPDATE CASCADE SOME_DATA
```

Example Output File:
```
CREATE TABLE ExampleSchema.Example
(
  ID INTEGER IDENTITY(1,1) NOT NULL
, SOME_DATA VARCHAR(100) NOT NULL
, USER_ID VARCHAR(50) NOT NULL
, TMSTAMP DATETIME NOT NULL
, TIMESTAMP
, CONSTRAINT FK_ExampleSchema_Example_Example2_ExampleTable2_SOME_DATA FOREIGN KEY (
    SOME_DATA
  )
  REFERENCES Example2.ExampleTable2
    ON DELETE CASCADE
    ON UPDATE CASCADE
)
GO
```

## Grants
Grants can be used to grant actions against the table to username's using the `GRANT` keyword.

Example SI File:
```
TABLE Example
    ID                  SEQUENCE
    SOME_DATA           CHAR(100)
    USER_ID             USERSTAMP
    TMSTAMP             TIMESTAMP

GRANT SELECT INSERT DELETE UPDATE TO FreeTextUserName
```

Example Output:
```
CREATE TABLE ExampleSchema.Example
(
  ID INTEGER IDENTITY(1,1) NOT NULL
, SOME_DATA VARCHAR(100) NOT NULL
, USER_ID VARCHAR(50) NOT NULL
, TMSTAMP DATETIME NOT NULL
, TIMESTAMP
)
GO

GRANT select ON ExampleSchema.Example TO FreeTextUserName
GO

GRANT insert ON ExampleSchema.Example TO FreeTextUserName
GO

GRANT delete ON ExampleSchema.Example TO FreeTextUserName
GO

GRANT update ON ExampleSchema.Example TO FreeTextUserName
GO
```

## Views
A VIEW can be created using the `VIEW` keyword. It can also be constrained to a specific user:

Example SI File:
```
TABLE Example
    ID                  SEQUENCE
    SOME_DATA           CHAR(100)
    USER_ID             USERSTAMP
    TMSTAMP             TIMESTAMP


VIEW SelectSomeData TO FreeTextUser
SQLCODE
    select SOME_DATA from ExampleSchema.ExampleTable
ENDCODE
```

Example output:
```
CREATE TABLE ExampleSchema.Example
(
  ID INTEGER IDENTITY(1,1) NOT NULL
, SOME_DATA VARCHAR(100) NOT NULL
, USER_ID VARCHAR(50) NOT NULL
, TMSTAMP DATETIME NOT NULL
, TIMESTAMP
)
GO

IF OBJECT_ID('ExampleSchema.ExampleGetSomeData','V') IS NOT NULL
    DROP VIEW ExampleSchema.ExampleGetSomeData
GO

CREATE VIEW ExampleSchema.ExampleGetSomeData
(
) AS
(
select SOME_DATA from ExampleSchema.ExampleTable
)
GO

GRANT SELECT ON ExampleSchema.ExampleGetSomeData TO FreeTextUser
GO
```


# General Information
The generator output will delete the table if the table currently exists:
```
IF OBJECT_ID('Schema.ExampleTable','U') IS NOT NULL
    DROP TABLE Schema.ExampleTable
GO
```
