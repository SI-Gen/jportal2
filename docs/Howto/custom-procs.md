Write a custom proc if the built-in procs don't give you the power or flexibility you need.

```
PROC <proc_name>
[ (standard) ]
[ ** <comment> ]*
[ OPTIONS <option_string>+ ]
[INPUT
    (<InputField> <InputFieldType>)+
]
[INOUT
    (<InoutField> <InoutFieldType>)+
]
[OUTPUT
    (<OutputField> <OutputFieldType>)+
]
[[SQL]CODE]
<Lines of SQL code>+
[ENDCODE]
```

`<proc_name>` specifies the name of the proc.  
`(standard)` is a keyword which indicates to JPortal2 that the fields returned by the query, will include all the
fields of the table. The `(standard)` clause is mostly used with some older built-in generators for typed languages like
C#, Java or C++. Its use is largely supersedes in most of the newer generators and the template generators, since
one can get exactly the same functionality by using [SelectBy](../Howto/retrieving-rows.md#selectby) and not specifying 
`OUTPUT` clause.
`<comment>` is a comment that will get added to the generated code. Not all generators support comments.  
`<option_string>` is a list of string options that can be passed to the generators. The options are generator
specified. If you want to use the a specific generator's options, the generator's instructions will tell you what
to pass into `<option_string>`.  
The `INPUT` section defines the input fields into the proc.  
The `INOUT` section defines fields that are input and output fields into the proc. You can also put the fieldname
into both the INPUT and OUTPUT section for the same effect.  
The `OUTPUT` section defines the output fields into the proc.  
The `SQLCODE` and `ENDCODE` keywords specify the beginning and end of your custom SQL.

```sql title="books.si"
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

PROC SelectBookAndAuthorDetails 
** This proc selects books and their authors for titles like :TitleLike. 
** You can pass in a wildcard into TitleLike eg TitleLike = 'ABC%'
INPUT
    TitleLike   CHAR(255)
OUTPUT
    Bio             CHAR(255)
    Title           =
    PublishDate     =
    LastUpdated     =
SQLCODE
    SELECT
        a.Bio,
        b.Title,
        b.PublishDate,
        b.LastUpdated
    FROM
        Authors a
            INNER JOIN
        Books b
            ON a.ID = b.AuthorID
    WHERE
        b.Title LIKE :TitleLike
ENDCODE
```


The generated code will look like below:  

=== "--template-generator SQLAlchemy"
    ```python
        @dataclass
        class DB_BooksSelectBookAndAuthorDetails:
        #Outputs
        Bio: str
        Title: str
        PublishDate: datetime
        LastUpdated: datetime
    
        @classmethod
        def get_statement(cls
                         , TitleLike: str
                         ) -> TextAsFrom:
            class _ret:
                sequence = "default," #postgres uses default for sequences
                output = " OUTPUT (Bio,Title,PublishDate,LastUpdated)"
                tail = " RETURNING Bio Title PublishDate LastUpdated"
                #session.bind.dialect.name
    
            statement = sa.text(
                            f"/* PROC BooksAndAuthors.Books.SelectBookAndAuthorDetails */"
                            f"SELECT "
                            f"a.Bio, "
                            f"b.Title, "
                            f"b.PublishDate, "
                            f"b.LastUpdated "
                            f"FROM "
                            f"Authors a "
                            f"INNER JOIN "
                            f"Books b "
                            f"ON a.ID = b.AuthorID "
                            f"WHERE "
                            f"b.Title LIKE :TitleLike ")
    
            text_statement = statement.columns(Bio=db_types.NonNullableString,
                                          Title=db_types.NonNullableString,
                                          PublishDate=sa.types.DateTime,
                                          LastUpdated=sa.types.DateTime,
                                          )
            text_statement = text_statement.bindparams(TitleLike=TitleLike,
                                             )
            return text_statement
    
        @classmethod
        def execute(cls, session: Session, TitleLike: str
                         ) -> List['DB_BooksSelectBookAndAuthorDetails']:
            params = process_bind_params(session, [db_types.NonNullableString,
                                            ], [TitleLike,
                                            ])
            res = session.execute(cls.get_statement(*params))
            recs = res.fetchall()
            return process_result_recs(DB_BooksSelectBookAndAuthorDetails, session, [db_types.NonNullableString,
                                            db_types.NonNullableString,
                                            sa.types.DateTime,
                                            sa.types.DateTime,
                                            ], recs)
    
    ```
=== "--builtin-generator JavaJCCode"
    ```java title="BooksSelectBookAndAuthorDetails.java"
    package com.example.db;
    
    import bbd.jportal2.util.*;
    import java.sql.*;
    import java.util.*;
    import java.math.*;
    
    /**
     * This proc selects books and their authors for titles like :TitleLike. 
     * You can pass in a wildcard into TitleLike eg TitleLike = 'ABC%'
     */
    public class BooksSelectBookAndAuthorDetails extends BooksSelectBookAndAuthorDetailsStruct
    {
        private static final long serialVersionUID = 1L;
        Connector connector;
        Connection connection;
        public BooksSelectBookAndAuthorDetails()
        {
            super();
        }
        public void setConnector(Connector conn)
        {
            this.connector = conn;
            connection = connector.connection;
        }
        public BooksSelectBookAndAuthorDetails(Connector connector)
        {
            super();
            this.connector = connector;
            connection = connector.connection;
        }
        /**
         * This proc selects books and their authors for titles like :TitleLike. 
         * You can pass in a wildcard into TitleLike eg TitleLike = 'ABC%'
         * Returns any number of records.
         * @return result set of records found
         * @exception SQLException is passed through
         */
        public Query selectBookAndAuthorDetails() throws SQLException
        {
            String statement =
                    "/* PROC BooksAndAuthors.Books.SelectBookAndAuthorDetails */"
                            + "SELECT "
                            + "a.Bio, "
                            + "b.Title, "
                            + "b.PublishDate, "
                            + "b.LastUpdated "
                            + "FROM "
                            + "Authors a "
                            + "INNER JOIN "
                            + "Books b "
                            + "ON a.ID = b.AuthorID "
                            + "WHERE "
                            + "b.Title LIKE ? "
                    ;
            PreparedStatement prep = connector.prepareStatement(statement);
            prep.setString(1, titleLike);
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
        public boolean selectBookAndAuthorDetails(Query query) throws SQLException
        {
            if (!query.result.next())
            {
                query.close();
                return false;
            }
            ResultSet result = query.result;
            bio =  result.getString(1);
            title =  result.getString(2);
            publishDate =  result.getDate(3);
            lastUpdated =  result.getTimestamp(4);
            return true;
        }
        /**
         * Returns all the records in a result set as array of BooksSelectBookAndAuthorDetails.
         * @return array of BooksSelectBookAndAuthorDetails.
         * @exception SQLException is passed through
         */
        public BooksSelectBookAndAuthorDetails[] selectBookAndAuthorDetailsLoad() throws SQLException
        {
            Vector<BooksSelectBookAndAuthorDetails> recs = new Vector<>();
            Query query = selectBookAndAuthorDetails();
            while (selectBookAndAuthorDetails(query) == true)
            {
                BooksSelectBookAndAuthorDetails rec = new BooksSelectBookAndAuthorDetails();
                rec.bio = bio;
                rec.title = title;
                rec.publishDate = publishDate;
                rec.lastUpdated = lastUpdated;
                recs.addElement(rec);
            }
            BooksSelectBookAndAuthorDetails[] result = new BooksSelectBookAndAuthorDetails[recs.size()];
            for (int i=0; i<recs.size();i++)
                result[i] = recs.elementAt(i);
            return result;
        }
        /**
         * Returns any number of records.
         * @return result set of records found
         * @param titleLike input.
         * @exception SQLException is passed through
         */
        public Query selectBookAndAuthorDetails(
                String titleLike
        ) throws SQLException
        {
            this.titleLike = titleLike;
            return selectBookAndAuthorDetails();
        }
    }
    
    ```
    
