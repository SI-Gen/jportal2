# JPortal2

[<img src="https://dieterrosch.visualstudio.com/_apis/public/build/definitions/62fc2ca3-38b4-432f-8341-af383b7ba8e4/3/badge"/>](https://dieterrosch.visualstudio.com/_apis/public/build/definitions/62fc2ca3-38b4-432f-8341-af383b7ba8e4/3/badge)


JPortal2 is the newest version of the JPortal SI-file based generator.

## History
### JPortal
JPortal was originally developed in the early 1990's by Vincent Risi (https://github.com/VincentRisi) as a standardised way to generate a Data Access Layer (DAL) for multiple databases and target languages. SI Files allow the definition of a SQL table with columns, as well as SQL Queries to be run against the table. It then allows the generation of a DAL in a number of languages. Being developed in the 90's, the first languages that were supported, were Java, C++, C and Visual Basic. Original database support was for Oracle.

Over the years the tool has grown into a suite of tools to include generators DAL's, RPC client and server generators and for a number of languages including C# and Python, and database support for Oracle, SQL Server, MySQL, PostgreSQL, DB2 and SQLite.


### JPortal2

The original JPortal grew organically, and while over the years the capabilities have expanded, lots of it was not envisioned originally. As a result, some of the code has become difficult to maintain. JPortal2 is an attempt to clean up the code bit-by-bit, and also start adding unittests and other features that are required in modern libraries.

### Original Code
The original JPortal code can be found at https://github.com/VincentRisi. An updated version with some bug-fixes can be found at https://github.com/dieterrosch.



