The built-in Insert, Updates, Deletes, and various selects are cool, and you can do a lot with them.

But what if you want to write a more complex query? JPortal2 makes that really easy too:

Add the following code to the todo_item.si:

```sql title="todo_item.si"
PROC GetItemsWithListNameForID
INPUT
    ID              =
OUTPUT
    ListName        CHAR(255)
    ItemName        =
    ItemDescription =
SQLCODE
    SELECT  
            l.ListName,
            i.ItemName,
            i.ItemDescription
    FROM
            ToDoList_App.ToDoList l 
            INNER JOIN
            ToDoList_App.ToDo_Item i
            ON l.ID = i.TodoList_ID
    WHERE i.ID =  :ID
ENDCODE
```

This time, we need a bit more code, but it's still pretty simple.
In **line 1** we are defining the name for our custom proc. It will be called `GetItemsWithListNameForID`.  
**Line 2-3** we are saying that our proc has a single input, called `ID`.  

### The `=` (equals sign)
You might be wondering what the `=` sign means in the above SI file.  

Essentially, the `=` sign means "use the column type specified in the table definition".   

Because ID is specified in our table as a SEQUENCE, the type for input parameter ID in our proc, will also
be SEQUENCE.  


**Line 4-7** specifies the outputs of our proc.  
Here we are saying our proc will return a `ListName`, `ItemName` and `ItemDescription`.  

`ItemName` and `ItemDescription` are defined in our table defintion, so we can use the `=` sign trick 
again.   

But `ListName` is not defined in this file. Instead it is defined in the `todo_list.si` file.  

JPortal2 only looks at the current SI file, so it doesn't know what column type to use for `ListName`. Therefore we specify it by hand as CHAR(255), which is the same as in the `todo_list.si` file.  

**Line 8-18** specifies our query that we want to run.  
Our code is specified between the keywords `SQLCODE` and `ENDCODE`. This indicates the start and end of our custom
SQL to JPortal2.  

Notice how we pass in the input parameter `ID`, using the syntax `:ID`. If you have more than one input
parameter, you can refer to them by `:<INPUT_PARAMETER_NAME>`.


# Dynamic SQL
For our final trick, we will do something powerful, but dangerous.  For this, we will use dynamic sql:
``` sql
//Dynamic queries can be done with the ampersand below
//REMEMBER!!! Dynamic SQL is open to SQL injection! So use with care and make sure to sanitize inputs!
//Dynamic SQL is a last resort escape hatch, not the first tool to reach for!!
//In the DAL class created for the below query, a property called ItemNamesList will be created
//This will allow you to pass through the remainder of the where clause as a string.
PROC SelectWithDynamicQuery
INPUT
    ID              =
OUTPUT
    ItemName        =
    ItemDescription =
    LastUpdated     =
SQLCODE
    SELECT
            ItemName,
            ItemDescription,
            LastUpdated
    FROM
            ToDoList_App.ToDo_Item i
    WHERE
            i.ID = :ID
    AND
            ItemName IN (&ItemNamesList)
ENDCODE
```

If you run the jportal_generate.sh command again, you will see the following generated code:

```py title="ToDo_Item.py"
@dataclass
class DB_ToDo_ItemSelectWithDynamicQuery:
    #Outputs
    ItemName: str
    ItemDescription: str
    LastUpdated: datetime

    @classmethod
    def get_statement(cls
                     , ID: int
                     , ItemNamesList: str) -> TextAsFrom:
        class _ret:
            sequence = "default," #postgres uses default for sequences
            output = " OUTPUT (ItemName,ItemDescription,LastUpdated)"
            tail = " RETURNING ItemName ItemDescription LastUpdated"
            #session.bind.dialect.name

        statement = sa.text(
                        f"/* PROC ToDoList_App.ToDo_Item.SelectWithDynamicQuery */"
                        f"SELECT "
                        f"ItemName, "
                        f"ItemDescription, "
                        f"LastUpdated "
                        f"FROM "
                        f"ToDoList_App.ToDo_Item i "
                        f"WHERE "
                        f"i.ID = :ID "
                        f"AND "
                        f"ItemName IN ( "
                        f"{ItemNamesList}"
                        f") ")

        text_statement = statement.columns(ItemName=db_types.NonNullableString,
                                      ItemDescription=sa.types.Text,
                                      LastUpdated=sa.types.DateTime,
                                      )
        text_statement = text_statement.bindparams(ID=ID,
                                         )
        return text_statement

    @classmethod
    def execute(cls, session: Session, ID: int
                     , ItemNamesList: str) -> List['DB_ToDo_ItemSelectWithDynamicQuery']:
        params = process_bind_params(session, [sa.types.Integer,
                                        db_types.NonNullableString,], [ID,
                                        ItemNamesList,])
        res = session.execute(cls.get_statement(*params))
        recs = res.fetchall()
        return process_result_recs(DB_ToDo_ItemSelectWithDynamicQuery, session, [db_types.NonNullableString,
                                        sa.types.Text,
                                        sa.types.DateTime,
                                        ], recs)
```

If you look at the generated code, you will see that it expects two input parameters, namely the `ID` we
specified, but also `ItemNamesList`, which we specified on **line 23** of the SI file.  

Effectively, the dynamic sql clause (specified by an ampersand (&) in the SI file), allows us to pass a text
string through to our query. So if we wanted to query all ToDoItems named `A` and `B` related to ToDoList 123,
we could do the following
```python
recs = DB_ToDo_ItemSelectWithDynamicQuery.execute(session, ID=123, "'A','B'")
```

!!! danger
        While Dynamic SQL is very powerful, it is also very dangerous, because it opens you up to 
        [SQL Injection Attacks](https://owasp.org/www-community/attacks/SQL_Injection). You should try to avoid using
        dynamic SQL as far possible. If you are forced to use is, make sure to guard against SQL injection by either
        coding against it, or using a library that helps with that.
