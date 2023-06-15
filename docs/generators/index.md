Documentation for each generator:

### DDL Generators
Generation has changed to now generate a single file for each table that contains that table only and then generate the sequences.sql, views.sql and foreign_keys.sql files separately.

In order to utilize single file generation for DDL generators, pass the flag SingleFileDDLGeneration to the run of the program. An example is as follows:

```shell
docker run --rm -v ${PWD}:/local ghcr.io/si-gen/jportal2:latest \
                      --inputdir=/local/sql/si \
                      --builtin-generator PostgresDDL:/local/generated_sources/generated_sql \
                      --flag SingleFileDDLGeneration                    
```

[MSSqlDDL](MSSqlDDL.md) - DDL Generator for Microsoft SQL Server