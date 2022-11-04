Enums are a very useful programming construct. JPortal has some powerful mechanisms for working with enums.

``` hl_lines="9 26"
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

PROC EnumExampleReturnAllPublicLists
OUTPUT
    ID          =
    ListName    =
    ListType    =
SQLCODE
    SELECT 
            ID,
            ListName,
            ListType
    FROM    
            ToDoList
    WHERE   
            ListType = %ListType.Public%
ENDCODE

```

In the above table definition, line 9 defines `ListType` as an enum with 2 values:  

| Key | Value    |
| --- | -----    |
| 1   | Private  |
| 2   | Public   |

## Using enums in your code
In the generated code this will allow you to use enums instead of magic numbers in your code. For example:
=== "--template-generator SQLAlchemy"
    ```python
    @dataclass
    class DB_ToDoListBulkInsert:
        # Enum for ListType field
        class ListTypeEnum(enum.Enum):
            Private = 1
            Public = 2
    ```
=== "--builtin-generator JavaJCCode"
    ```java hl_lines="4-25"
    public class ToDoListStruct implements Serializable
    {
      public static final long serialVersionUID = 1L;
      public enum ListType
      {
        PRIVATE(1, "Private"),
        PUBLIC(2, "Public");
        public int key;
        public String value;
        ListType(int key, String value)
        {
          this.key = key;
          this.value = value;
        }
        public static ListType get(int key)
        {
          for (ListType op : values())
            if (op.key == key) return op;
          return null;
        }
        public String toString()
        {
          return value;
        }
      }
      protected Integer id;
      public Integer getID(){ return id; } 
      public void setID(Integer  id){ this.id = id; }
    
      protected String listName;
      public String getListName(){ return listName; } 
      public void setListName(String  listName){ this.listName = listName; }
    
      protected Short listType;
      public Short getListType(){ return listType; } 
      public void setListType(Short  listType){ this.listType = listType; }
    
      protected String description;
      public String getDescription(){ return description; } 
      public void setDescription(String  description){ this.description = description; }
    
      protected Timestamp lastUpdated;
      public Timestamp getLastUpdated(){ return lastUpdated; } 
      public void setLastUpdated(Timestamp  lastUpdated){ this.lastUpdated = lastUpdated; }
    
      public ToDoListStruct()
      {
        id = null;
        listName = null;
        listType = null;
        description = null;
        lastUpdated = new Timestamp(0);
      }
      public String toString()
      {
        String CRLF = System.lineSeparator();
        return "  id          : " + id + CRLF
             + "  listName    : " + listName + CRLF
             + "  listType    : " + listType + CRLF
             + "  description : " + description + CRLF
             + "  lastUpdated : " + lastUpdated + CRLF
        ;
      }
    }

    ```

## Using enums in SQL
To use the above enums in your SQL code, you can use the syntax
`%<ColumnName>.<EnumValue>`.

So for example if you wanted to select all records where ListType is Public (i.e. 2), you could write
```python linenums="0"
    WHERE   
            ListType = %ListType.Public%

```
which would generate
=== "--template-generator SQLAlchemy"
    ```python

    ```
=== "--builtin-generator JavaJCCode"
    ```java
    public Query enumExampleReturnAllPublicLists() throws SQLException
    {
        String statement =
        "/* PROC BooksAndAuthors.ToDoList.EnumExampleReturnAllPublicLists */"
        + "SELECT "
        + "ID, "
        + "ListName, "
        + "ListType "
        + "FROM "
        + "ToDoList "
        + "WHERE "
        + "ListType = 2 "
        ;
        PreparedStatement prep = connector.prepareStatement(statement);
        ResultSet result = prep.executeQuery();
        Query query = new Query(prep, result);
        return query;
    }
    ```
