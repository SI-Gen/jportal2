
## Creating our first simple table definition: *ToDoList*

### Create the SI file  
Create a file called todolist.si in the ${rootDir}/sql/si directory created above. SI files are the input files to JPortal2.

Your structure should now look like this:
```
jportal2-demo
└───.vscode
    └── settings.json
└───sql
    └───si
        └── todolist.si
```



The todolist.si file should contain the following code:

**todolist.si**
```sql
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
PROC Insert Returning
PROC Update
PROC SelectOne
PROC DeleteOne

//You can put take-on or test data in this section
SQLDATA
INSERT INTO ToDoList_App.ToDoList(ListName,ListType,Description,LastUpdated) VALUES ('Takeon Test List 1', 1, 'Take on test list description', CURRENT_DATE )
ENDDATA
                                                                               
<Need to leave an empty line at the end>**
```
The above file does quite a lot, in a small amount of code:  

* **Line 1** tells JPortal2 that you want to create a database called ExampleDatabase. Depending on the generator, this line may or may not be used.      
* **Line 2** tells JPortal2 to put the code generated from this file, into a namespace/package called com.example.db  
* **Line 3** tells JPortal2 that this code will be running in a microservice called ExampleServer. For the majority of your use cases, this is not important - this is really used for other private-source generators (not available to the public) that build off of JPortal2  
* **Line 5-11** tells JPortal2 we want to create a database table called TodoList, with 5 fields, as well as the types of the fields. See <TODO> for a full list of field types supported. The only interesting thing to note here, is that we are defining the ListType field as a SHORT, but we are also specifying that we want an Enum for the field, which contains two values, "Private" and "Public". More on this later.  
* **Line 13-14** tells JPortal2 we want to create a key called PKEY. It will be of type `PRIMARY` (in other words a primary key) and consist of a single field, in this case the `ID` field.   
* **Line 17-20** tells JPortal2 we want to generate code for 4 simple CRUD operations:  
    * an `Insert` function that will give us back the inserted primary key `ID` (because we specified the keyword `Returning`)
    * An `Update` function that will take all the fields (except for the Primary Key), and update them.  
    * A `SelectOne` function that selects a record by Primary Key, and returns all of the fields  
    * A `DeleteOne` function that will delete a record by Primary Key
* **Line 22-25** is completely optional, but is a simple way to insert take-on data into our database for testing. It takes the lines that are between the tokens `SQLDATA` and `ENDDATA` and puts them verbatim into the generated files as we'll see below.

Indenting and spacing generally doesn't matter, but try to indent to keep your code readable.

**!!!NOTE!!!:** There is unfortunately one gotcha (bug) in JPortal2, you do need to have an empty line at the end of your SI file. This is a side-effect of how our parser works, and seems to be an elusive bug to fix. So for now, just remember to leave an empty line at the end of your SI file. 
