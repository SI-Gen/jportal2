########################################################################################################################
################## Generated Code. DO NOT CHANGE THIS CODE. Change it in the generator and regenerate ##################
########################################################################################################################

from dataclasses import dataclass, field
from datetime import datetime
from typing import List, Any, Optional

import sqlalchemy as sa
from sqlalchemy.orm import Session
from sqlalchemy.sql.expression import TextAsFrom

from bbdcommon.database.db_common import DBMixin, Base, DBColumn
from bbdcommon.database import db_types
from bbdcommon.database.processing import process_result_recs, process_result_rec, process_bind_params



TODO_ITEM_SCHEMA = "todolist_app"
class DB_ToDo_Item(Base, DBMixin):
    ID: int = DBColumn("id", sa.Integer(), sa.Sequence("todo_item_id_seq", metadata=Base.metadata, schema=TODO_ITEM_SCHEMA), primary_key=True, autoincrement=False)
    TodoList_ID: int = DBColumn("todolist_id", sa.Integer())
    ItemName: str = DBColumn("itemname", db_types.NonNullableString(length=255))
    ItemDescription: str = DBColumn("itemdescription", sa.Text())
    LastUpdated: datetime = DBColumn("lastupdated", sa.DateTime(), default=datetime.now, onupdate=datetime.now)

    __schema__ = TODO_ITEM_SCHEMA

    def __init__(self, TodoList_ID: int, ItemName: str, ItemDescription: str, LastUpdated: datetime):
        super(DB_ToDo_Item, self).__init__(
            TodoList_ID=TodoList_ID,
            ItemName=ItemName,
            ItemDescription=ItemDescription,
            LastUpdated=LastUpdated)
    # Insert yes

@dataclass
class DB_ToDo_ItemInsertReturning:
    #Outputs
    ID: int

    @classmethod
    def get_statement(cls
                     , TodoList_ID: int
                     , ItemName: str
                     , ItemDescription: str
                     , LastUpdated: datetime
                     ) -> TextAsFrom:
        class _ret:
            sequence = "default," #postgres uses default for sequences
            output = ""
            tail = " RETURNING ID"
            #session.bind.dialect.name

        statement = sa.text(
                        f"/* PROC ToDoList_App.ToDo_Item.Insert */"
                        f"insert into ToDoList_App.ToDo_Item ("
                        f"  ID,"
                        f"  TodoList_ID,"
                        f"  ItemName,"
                        f"  ItemDescription,"
                        f"  LastUpdated"
                        f" ) "
                        f"{_ret.output}"
                        f" values ("
                        f"{_ret.sequence}"
                        f"  :TodoList_ID,"
                        f"  :ItemName,"
                        f"  :ItemDescription,"
                        f"  :LastUpdated"
                        f" )"
                        f"{_ret.tail}")

        text_statement = statement.columns(ID=sa.types.Integer,
                                      )
        text_statement = text_statement.bindparams(TodoList_ID=TodoList_ID,
                                         ItemName=ItemName,
                                         ItemDescription=ItemDescription,
                                         LastUpdated=LastUpdated,
                                         )
        return text_statement

    @classmethod
    def execute(cls, session: Session, TodoList_ID: int
                     , ItemName: str
                     , ItemDescription: str
                     , LastUpdated: datetime
                     ) -> Optional['DB_ToDo_ItemInsertReturning']:
        params = process_bind_params(session, [sa.types.Integer,
                                        db_types.NonNullableString,
                                        sa.types.Binary,
                                        sa.types.DateTime,
                                        ], [TodoList_ID,
                                        ItemName,
                                        ItemDescription,
                                        LastUpdated,
                                        ])
        res = session.execute(cls.get_statement(*params))
        rec = res.fetchone()
        if rec:
            res.close()
            return process_result_rec(DB_ToDo_ItemInsertReturning, session, [sa.types.Integer,
                                        ], rec)

        return None
    # Update yes

@dataclass
class DB_ToDo_ItemUpdate:
    

    @classmethod
    def get_statement(cls
                     , TodoList_ID: int
                     , ItemName: str
                     , ItemDescription: str
                     , LastUpdated: datetime
                     , ID: int
                     ) -> TextAsFrom:
        class _ret:
            sequence = "default," #postgres uses default for sequences
            output = ""
            tail = ""
            #session.bind.dialect.name

        statement = sa.text(
                        f"update ToDoList_App.ToDo_Item"
                        f" set"
                        f"  TodoList_ID = :TodoList_ID"
                        f", ItemName = :ItemName"
                        f", ItemDescription = :ItemDescription"
                        f", LastUpdated = :LastUpdated"
                        f" where ID = :ID")

        text_statement = statement.columns()
        text_statement = text_statement.bindparams(TodoList_ID=TodoList_ID,
                                         ItemName=ItemName,
                                         ItemDescription=ItemDescription,
                                         LastUpdated=LastUpdated,
                                         ID=ID,
                                         )
        return text_statement

    @classmethod
    def execute(cls, session: Session, TodoList_ID: int
                     , ItemName: str
                     , ItemDescription: str
                     , LastUpdated: datetime
                     , ID: int
                     ) -> None:
        params = process_bind_params(session, [sa.types.Integer,
                                        db_types.NonNullableString,
                                        sa.types.Binary,
                                        sa.types.DateTime,
                                        sa.types.Integer,
                                        ], [TodoList_ID,
                                        ItemName,
                                        ItemDescription,
                                        LastUpdated,
                                        ID,
                                        ])
        res = session.execute(cls.get_statement(*params))
        res.close()
    # SelectOne yes

@dataclass
class DB_ToDo_ItemSelectOne:
    #Outputs
    TodoList_ID: int
    ItemName: str
    ItemDescription: str
    LastUpdated: datetime

    @classmethod
    def get_statement(cls
                     , ID: int
                     ) -> TextAsFrom:
        class _ret:
            sequence = "default," #postgres uses default for sequences
            output = " OUTPUT (TodoList_ID,ItemName,ItemDescription,LastUpdated)"
            tail = " RETURNING TodoList_ID ItemName ItemDescription LastUpdated"
            #session.bind.dialect.name

        statement = sa.text(
                        f"/* PROC ToDoList_App.ToDo_Item.SelectOne */"
                        f"select"
                        f"  TodoList_ID"
                        f", ItemName"
                        f", ItemDescription"
                        f", LastUpdated"
                        f" from ToDoList_App.ToDo_Item"
                        f" where ID = :ID")

        text_statement = statement.columns(TodoList_ID=sa.types.Integer,
                                      ItemName=db_types.NonNullableString,
                                      ItemDescription=sa.types.Binary,
                                      LastUpdated=sa.types.DateTime,
                                      )
        text_statement = text_statement.bindparams(ID=ID,
                                         )
        return text_statement

    @classmethod
    def execute(cls, session: Session, ID: int
                     ) -> Optional['DB_ToDo_ItemSelectOne']:
        params = process_bind_params(session, [sa.types.Integer,
                                        ], [ID,
                                        ])
        res = session.execute(cls.get_statement(*params))
        rec = res.fetchone()
        if rec:
            res.close()
            return process_result_rec(DB_ToDo_ItemSelectOne, session, [sa.types.Integer,
                                        db_types.NonNullableString,
                                        sa.types.Binary,
                                        sa.types.DateTime,
                                        ], rec)

        return None
    # DeleteOne yes

@dataclass
class DB_ToDo_ItemDeleteOne:
    

    @classmethod
    def get_statement(cls
                     , ID: int
                     ) -> TextAsFrom:
        class _ret:
            sequence = "default," #postgres uses default for sequences
            output = ""
            tail = ""
            #session.bind.dialect.name

        statement = sa.text(
                        f"/* PROC ToDoList_App.ToDo_Item.DeleteOne */"
                        f"delete from ToDoList_App.ToDo_Item"
                        f" where ID = :ID")

        text_statement = statement.columns()
        text_statement = text_statement.bindparams(ID=ID,
                                         )
        return text_statement

    @classmethod
    def execute(cls, session: Session, ID: int
                     ) -> None:
        params = process_bind_params(session, [sa.types.Integer,
                                        ], [ID,
                                        ])
        res = session.execute(cls.get_statement(*params))
        res.close()
    # SelectByTodoList_ID no

@dataclass
class DB_ToDo_ItemSelectByTodoList_ID:
    #Outputs
    ID: int
    ItemName: str
    ItemDescription: str
    LastUpdated: datetime

    @classmethod
    def get_statement(cls
                     , TodoList_ID: int
                     ) -> TextAsFrom:
        class _ret:
            sequence = "default," #postgres uses default for sequences
            output = " OUTPUT (ID,ItemName,ItemDescription,LastUpdated)"
            tail = " RETURNING ID ItemName ItemDescription LastUpdated"
            #session.bind.dialect.name

        statement = sa.text(
                        f"/* PROC ToDoList_App.ToDo_Item.SelectByTodoList_ID */"
                        f"select"
                        f"  ID"
                        f", ItemName"
                        f", ItemDescription"
                        f", LastUpdated"
                        f" from ToDoList_App.ToDo_Item"
                        f" where TodoList_ID = :TodoList_ID")

        text_statement = statement.columns(ID=sa.types.Integer,
                                      ItemName=db_types.NonNullableString,
                                      ItemDescription=sa.types.Binary,
                                      LastUpdated=sa.types.DateTime,
                                      )
        text_statement = text_statement.bindparams(TodoList_ID=TodoList_ID,
                                         )
        return text_statement

    @classmethod
    def execute(cls, session: Session, TodoList_ID: int
                     ) -> List['DB_ToDo_ItemSelectByTodoList_ID']:
        params = process_bind_params(session, [sa.types.Integer,
                                        ], [TodoList_ID,
                                        ])
        res = session.execute(cls.get_statement(*params))
        recs = res.fetchall()
        return process_result_recs(DB_ToDo_ItemSelectByTodoList_ID, session, [sa.types.Integer,
                                        db_types.NonNullableString,
                                        sa.types.Binary,
                                        sa.types.DateTime,
                                        ], recs)
