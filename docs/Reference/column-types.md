JPortal2 SI files support the following column types for table fields, or proc parameters:

| Type          | Description                                                                                                                                                                |
|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| BLOB          | Binary Large Object                                                                                                                                                        |
| BOOLEAN       | Boolean                                                                                                                                                                    |
| BYTE<length>  | Byte or bytestring<br/>of length <length>                                                                                                                                  |
| CHAR<length>  | Character or string<br/>of length <length>                                                                                                                                 | 
| DATE          | Date                                                                                                                                                                       |
| DATETIME      | DateTime                                                                                                                                                                   |
| DOUBLE        | Double                                                                                                                                                                     |
| DYNAMIC       | DynamicSQL - this type is an internal type<br/>and not used by a user.                                                                                                     |
| FLOAT         | Floating point number                                                                                                                                                      |
| IDENTITY      | <TODO>                                                                                                                                                                     |
| INT           | Integer                                                                                                                                                                    |
| LONG          | Long Integer                                                                                                                                                               |
| MONEY         | Money class, usually maps to the Decimal type                                                                                                                              |
| SEQUENCE      | Auto-incrementing database sequence type, of size INT                                                                                                                      |
| SHORT         | Short integer                                                                                                                                                              |
| STATUS        | ?                                                                                                                                                                          |
| TIME          | Time                                                                                                                                                                       |
| TIMESTAMP     | Auto-inserted date-time. If you mark a column as TIMESTAMP, JPortal2<br/>will automatically insert the current date and time when you use a built-in INSERT or UPDATE PROC |
| TLOB          | Text Large Object                                                                                                                                                          |
| USERSTAMP     | Generally generator-specific, this maps in C# to the `cursor.GetUserStamp` function, <br/>and in most of the other generators simply maps to `CHAR`                        |
| ANSICHAR      | Ansichar types                                                                                                                                                             |
| UID           | GUID column-type                                                                                                                                                           |
| XML           | XML column-type of max length 4,096 bytes                                                                                                                                  |
| BIGSEQUENCE   | Auto-incrementing database sequence type, of size BIGINT                                                                                                                   |
| BIGIDENTITY   | <TODO>                                                                                                                                                                     |
| AUTOTIMESTAMP | Same as TIMESTAMP                                                                                                                                                          |
| WCHAR         | Not used                                                                                                                                                                   |
| WANSICHAR     | Not used                                                                                                                                                                   |
| UTF8          | Not used                                                                                                                                                                   |
| BIGXML        | XML column-type of max length 4,194,304 bytes                                                                                                                              |
| JSON          | JSON column-type of max length 4,096 bytes                                                                                                                                 |
| BIGJSON       | JSON column-type of max length 4,194,304 bytes                                                                                                                             |
