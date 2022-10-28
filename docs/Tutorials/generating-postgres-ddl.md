### Generate PostgreSQL DDL from our SI file
Now, run the JPortal2 docker command, to generate a PostgreSQL DDL file:

**If you are running the tutorial inside a dev container as we recommend, make sure you open a terminal window in 
VSCode, and run the below command in the built-in terminal. Opening a terminal in VSCode running a dev container, will 
open the terminal inside the container, rather than on your local filesystem and OS.**

```shell
docker run --rm -v ${PWD}:/local ghcr.io/si-gen/jportal2:latest \
                      --inputdir=/local/sql/si \
                      --builtin-generator PostgresDDL:/local/generated_sources/generated_sql
```

**PRO-TIP: For ease-of-use, we usually create a file named generate_jportal.sh in the root directory, which contains 
the above command-line.**

You will notice that we are passing 2 command-line parameters to JPortal2:  
The `--inputdir` parameter tells JPortal2 where the SI files are located.  
The `--builtin-generator` parameter tells JPortal2 that we want to run the built-in generator named PostgresDDL, and 
place the generated output files in the directory `generated_sources/generated_sql`

After running the above command, you should see some console output, and then a freshly-generated file in the 
`${rootDir}/generated_sources/generate_sql/` directory. The file will be named **ExampleDatabase.sql** (because we 
specified the database name as `ExampleDatabase` in line one of the above SI file).  
The file should look as per below:

**ExampleDatabase.sql**
```postgresql
DROP TABLE IF EXISTS ToDoList_App.ToDoList CASCADE;

CREATE TABLE ToDoList_App.ToDoList
( ID serial
  , ListName varchar(255)
  , ListType smallint
  , Description varchar(255)
  , LastUpdated timestamp
);

ALTER TABLE ToDoList_App.ToDoList ALTER ID SET NOT NULL;
ALTER TABLE ToDoList_App.ToDoList ALTER ListName SET NOT NULL;
ALTER TABLE ToDoList_App.ToDoList ALTER ListType SET NOT NULL;
ALTER TABLE ToDoList_App.ToDoList ALTER Description SET NOT NULL;
ALTER TABLE ToDoList_App.ToDoList ALTER LastUpdated SET NOT NULL;

ALTER TABLE ToDoList_App.ToDoList
  ADD CONSTRAINT TODOLIST_PKEY PRIMARY KEY
    ( ID
      )
;

INSERT INTO ToDoList_App.ToDoList(ListName,ListType,Description,LastUpdated) VALUES ('Takeon Test List 1', 1, 'Take on test list description', CURRENT_DATE )
```

The above file should be completely self-explanatory, however we will add a few comments here:
1. The above DDL file is meant to facilitate easy creation of a local test database for developers. You will most likely use it to create an initial test database to play with, but once you start getting into the proper SDLC, and start creating DEV, QA and PROD database, you will use the file as an example or helper to write your own Flyway or Liquibase scripts. It isn't meant to just use as-is in one of these tools.
2. Notice **line 23**, which contains the take-on data that we specified at the bottom of the **todolist.si** file above. As mentioned previously, this can be a quick and easy way to get test data into your database.

### Create our tables in the database
To run our DDL, we will use the VSCode SQLTools extension we installed [here](setting-up-the-project-directory-and-sqltools-extension.md).  
Open the `generated_sources/generated_sql/ExampleDatabase.sql` file by double-clicking on it.

Now press `Ctrl+Shift+P` to open the command palette, and type `SQLTools Run`, choose the `SQLTools Connection: Run this file` option:
![Run the DDL](../img/run-query.gif)

Finally, to check that our table was created, go to the SQLTools extension on the left, open the postgres connection,
and navigate to the right schema and table:

![Query the table](../img/query-table.gif)

Works like magic, doesn't it? :)


*"OK, so we can generate DDL using the above mechanism,"* I hear you say, *"But what about the type-safe code you promised me? Where is all my Python, C#, or Java goodness? What is the point of this?".*

Your point is completely valid - DDL is not hard to write, and you do it once. But you interact with the database from code every day. Fear not, we will get to that next.
