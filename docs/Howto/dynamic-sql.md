Dynamic SQL allows you to do things that simply aren't possible with normal parameterized SQL, but it comes
with risk.

!!! danger
    While Dynamic SQL is very powerful, it is also very dangerous, because it opens you up to
    [SQL Injection Attacks](https://owasp.org/www-community/attacks/SQL_Injection). You should try to avoid using
    dynamic SQL as far possible. If you are forced to use is, make sure to guard against SQL injection by either
    coding against it, or using a library that helps with that.

To write a dynamic SQL query, write a proc like you would normally do, and at the point where you want to inject
the dynamic portion, use `&parameter_name(<length>)`. See below for an example.

``` sql title="todo_item.si" hl_lines="23"
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
            ItemName IN (&ItemNamesList(1024))
ENDCODE
```

The generated code will look like below:

=== "--template-generator SQLAlchemy"
    ```py title="ToDo_Item.py" hl_lines="30"
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
                            f"{ItemNamesList}" # (1)
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
    
    1. We use python string interpolation to inject the string.
=== "--builtin-generator JavaJCCode"
    ```java title="BooksSelectBookAndAuthorDetails.java" hl_lines="49"
    
    package com.example.db;

    import bbd.jportal2.util.*;
    import java.sql.*;
    import java.util.*;
    import java.math.*;
    
    /**
     */
        public class ToDo_ItemSelectWithDynamicQuery extends ToDo_ItemSelectWithDynamicQueryStruct
        {
          private static final long serialVersionUID = 1L;
          Connector connector;
          Connection connection;
          public ToDo_ItemSelectWithDynamicQuery()
          {
            super();
          }
          public void setConnector(Connector conn)
          {
            this.connector = conn;
            connection = connector.connection;
          }
          public ToDo_ItemSelectWithDynamicQuery(Connector connector)
          {
            super();
            this.connector = connector;
            connection = connector.connection;
          }
          /**
           * Returns any number of records.
           * @return result set of records found
           * @exception SQLException is passed through
           */
          public Query selectWithDynamicQuery() throws SQLException
          {
            String statement = 
              "/* PROC BooksAndAuthors.ToDo_Item.SelectWithDynamicQuery */"
                + "SELECT "
                + "ItemName, "
                + "ItemDescription, "
                + "LastUpdated "
                + "FROM "
                + "ToDoList_App.ToDo_Item i "
                + "WHERE "
                + "i.ID = ? "
                + "AND "
                + "ItemName IN ( "
                +  {ItemNamesList} // (1)
                + " "
                ;
            PreparedStatement prep = connector.prepareStatement(statement);
            prep.setInt(1, id);
            ResultSet result = prep.executeQuery();
            Query query = new Query(prep, result);
            return query;
          }
          /**
           * Returns the next record in a result set.
           * @param query The result set for the query.
           * @return true while records are found.
           * @exception SQLException is passed through
           */
          public boolean selectWithDynamicQuery(Query query) throws SQLException
          {
            if (!query.result.next())
            {
              query.close();
              return false;
            }
            ResultSet result = query.result;
            itemName =  result.getString(1);
            itemDescription =  result.getString(2);
            lastUpdated =  result.getTimestamp(3);
            return true;
          }
          /**
           * Returns all the records in a result set as array of ToDo_ItemSelectWithDynamicQuery.
           * @return array of ToDo_ItemSelectWithDynamicQuery.
           * @exception SQLException is passed through
           */
          public ToDo_ItemSelectWithDynamicQuery[] selectWithDynamicQueryLoad() throws SQLException
          {
            Vector<ToDo_ItemSelectWithDynamicQuery> recs = new Vector<>();
            Query query = selectWithDynamicQuery();
            while (selectWithDynamicQuery(query) == true)
            {
              ToDo_ItemSelectWithDynamicQuery rec = new ToDo_ItemSelectWithDynamicQuery();
              rec.itemName = itemName;
              rec.itemDescription = itemDescription;
              rec.lastUpdated = lastUpdated;
              recs.addElement(rec);
            }
            ToDo_ItemSelectWithDynamicQuery[] result = new ToDo_ItemSelectWithDynamicQuery[recs.size()];
            for (int i=0; i<recs.size();i++)
              result[i] = recs.elementAt(i); 
            return result;
          }
          /**
           * Returns any number of records.
           * @return result set of records found
           * @param id input.
           * @param SelectWithDynamicQuery dynamic input.
           * @exception SQLException is passed through
           */
          public Query selectWithDynamicQuery(
            Integer id
          , String ItemNamesList
          ) throws SQLException
          {
            this.id = id;
            this.ItemNamesList = ItemNamesList;
            return selectWithDynamicQuery();
          }
        }
    ```

    1. We simply append the string to inject the query.