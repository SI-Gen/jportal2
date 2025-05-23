use rust_parser::parse_database;

fn main() {
    println!("JPortal Database Parser - Enhanced JavaCC Port");
    println!("==============================================");
    
    // Comprehensive example demonstrating all enhanced JavaCC features
    let input = r#"
        DATABASE L'Production Database'
        FLAGS "production" "mysql" "optimized"
        PACKAGE com.example.ecommerce
        OUTPUT "generated/sql"
        IMPORT "common_types"
        SERVER "prod-db.example.com:3306"
        SCHEMA "ecommerce_schema"
        USERID dbadmin
        PASSWORD dbsecret

        TABLE Users ALIAS "UserTable" CHECK "id > 0"
            "User management table"
            "Stores customer information"
            OPTIONS "cache" "index"
            id bigidentity
            username char(50) not null
            email char(100) not null
            com.example.audit.created_at timestamp DEFAULTV "NOW()"
            com.example.user.status char(20) ALIAS user_status DEFAULTV "pending" NOT NULL CHECK "status IN ('active', 'pending', 'disabled')" "User status field"
            KEY primary_key (id)
            LINK email_link (email)
            GRANT select_grant (id, username)
            PROC GetUserByEmail {
                email_param char(100)
                "SELECT * FROM Users WHERE email = ?"
            }
            SPROC CreateUser {
                new_username char(50)
                new_email char(100)
                "INSERT INTO Users (username, email) VALUES (?, ?)"
            }
            PARM {
                title "User Management"
                cache_size "1000"
                "Additional configuration"
            }

        IMPORT "external_audit_log" [user_id action timestamp] ALIAS audit_table

        TABLE Orders
            id bigidentity
            user_id long not null
            total_amount money
            com.example.audit.created_at timestamp DEFAULTV "NOW()"
            com.example.order.status char(20) CALC "Computed order status"
            KEY primary_key (id)
            LINK user_link (user_id)
    "#;

    match parse_database(input) {
        Ok(database) => {
            println!("✅ Successfully parsed enhanced database definition!");
            println!();
            
            println!("📊 Database Information:");
            println!("  Name: {}", database.name);
            println!("  Package: {}", database.package_name.unwrap_or("None".to_string()));
            println!("  Server: {}", database.server);
            println!("  Schema: {}", database.schema.unwrap_or("None".to_string()));
            println!("  Flags: {:?}", database.flags);
            println!("  Imports: {:?}", database.imports);
            println!();

            println!("📋 Tables ({}):", database.tables.len());
            for (i, table) in database.tables.iter().enumerate() {
                println!("  {}. Table: {}", i + 1, table.name);
                if table.is_import {
                    println!("     Type: Import Table");
                    if !table.import_fields.is_empty() {
                        println!("     Import Fields: {:?}", table.import_fields);
                    }
                } else {
                    println!("     Type: Regular Table");
                }
                
                if let Some(alias) = &table.alias {
                    println!("     Alias: {}", alias);
                }
                
                if let Some(check) = &table.check {
                    println!("     Check: {}", check);
                }
                
                if !table.comments.is_empty() {
                    println!("     Comments: {:?}", table.comments);
                }
                
                if !table.options.is_empty() {
                    println!("     Options: {:?}", table.options);
                }

                println!("     Fields ({}):", table.fields.len());
                for (j, field) in table.fields.iter().enumerate() {
                    print!("       {}. {} ({:?})", j + 1, field.name, field.field_type);
                    
                    if field.is_package_field {
                        print!(" [Package Field]");
                    }
                    
                    if let Some(alias) = &field.alias {
                        print!(" ALIAS {}", alias);
                    }
                    
                    if !field.is_null {
                        print!(" NOT NULL");
                    }
                    
                    if field.is_calc {
                        print!(" CALC");
                    }
                    
                    if let Some(default) = &field.default_value {
                        print!(" DEFAULT {}", default);
                    }
                    
                    if let Some(check) = &field.check_value {
                        print!(" CHECK {}", check);
                    }
                    
                    println!();
                    
                    if !field.comments.is_empty() {
                        println!("          Comments: {:?}", field.comments);
                    }
                }

                if !table.keys.is_empty() {
                    println!("     Keys: {:?}", table.keys.iter().map(|k| &k.name).collect::<Vec<_>>());
                }
                
                if !table.links.is_empty() {
                    println!("     Links: {:?}", table.links.iter().map(|l| &l.name).collect::<Vec<_>>());
                }
                
                if !table.grants.is_empty() {
                    println!("     Grants: {:?}", table.grants.iter().map(|g| &g.perms).collect::<Vec<_>>());
                }
                
                if !table.procs.is_empty() {
                    println!("     Procedures: {:?}", table.procs.iter().map(|p| &p.name).collect::<Vec<_>>());
                }
                
                if !table.parameters.is_empty() {
                    println!("     Parameters: {} sections", table.parameters.len());
                }
                
                println!();
            }

            println!("🎉 Enhanced JavaCC Features Demonstrated:");
            println!("  ✅ jPackageIdent() - Package identifiers with dots");
            println!("  ✅ jIdentOrString() - Flexible identifier/string parsing");
            println!("  ✅ jIdent() - Enhanced identifiers including literal identifiers");
            println!("  ✅ jTableImport() - Table imports with field lists and aliases");
            println!("  ✅ jTable() - Enhanced table structure with aliases, checks, comments, options");
            println!("  ✅ jPackageField() - Package field definitions with all modifiers");
            println!("  ✅ Complete procedure and parameter support");
            println!("  ✅ All field types and modifiers");
            println!("  ✅ Keys, links, and grants");
        }
        Err(e) => {
            println!("❌ Failed to parse database: {}", e);
        }
    }
}
