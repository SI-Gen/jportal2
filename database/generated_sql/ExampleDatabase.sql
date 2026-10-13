DROP TABLE IF EXISTS ToDoList_App.ToDo_Item CASCADE;

CREATE TABLE ToDoList_App.ToDo_Item
( ID serial
, TodoList_ID integer
, ItemName varchar(255)
, ItemDescription text
, LastUpdated timestamp
);

ALTER TABLE ToDoList_App.ToDo_Item ALTER ID SET NOT NULL;
ALTER TABLE ToDoList_App.ToDo_Item ALTER TodoList_ID SET NOT NULL;
ALTER TABLE ToDoList_App.ToDo_Item ALTER ItemName SET NOT NULL;
ALTER TABLE ToDoList_App.ToDo_Item ALTER ItemDescription SET NOT NULL;
ALTER TABLE ToDoList_App.ToDo_Item ALTER LastUpdated SET NOT NULL;

ALTER TABLE ToDoList_App.ToDo_Item
 ADD CONSTRAINT TODO_ITEM_PKEY PRIMARY KEY
  ( ID
  )
;

DROP TABLE IF EXISTS ToDoList_App.ToDoList CASCADE;

CREATE TABLE ToDoList_App.ToDoList
( ID serial
, ListName varchar(255)
, ListType smallint
, Description varchar(1)
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

