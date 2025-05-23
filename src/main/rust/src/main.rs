use rust_parser::parse_database;

fn main() {
    let input = r#"
        DATABASE L'Enhanced JPortal Database'
        FLAGS "production" "mysql" "enhanced"
        PACKAGE com.example.enhanced
        OUTPUT "generated/enhanced"
        IMPORT "common_types"
        SERVER "enhanced-db.example.com:3306"
        SCHEMA "enhanced_schema"
        USERID dbadmin
        PASSWORD dbsecret

        TABLE Users ALIAS "UserTable" CHECK "id > 0"
            "User management table with enhanced features"
            OPTIONS "cache" "index" "optimized"
            id bigidentity "Primary key" "Auto-generated user ID"
            username (login) char(50) NOT NULL "User login name"
            email char(100) DEFAULTV "user@example.com" CHECK "email LIKE '%@%'" "Email address"
            status byte(ACTIVE=1, INACTIVE=0, PENDING=2) DEFAULTV "2" NOT NULL "User status"
            age short(MIN=0, MAX=150) "User age"
            balance money(10,2) DEFAULTV "0.00" "Account balance"
            created_at timestamp "Creation timestamp"
            profile_data json(2000) "User profile JSON"
            avatar blob(1048576) "User avatar image"
            preferences ansichar("light", "dark", "auto") DEFAULTV "auto" "UI preferences"
            com.example.audit.created_by char(50) ALIAS created_by DEFAULTV "system" "Audit field"
            com.example.audit.modified_at timestamp ALIAS modified_at CALC "Computed modification time"
            related_user = (id) "Self-referencing lookup"
            manager_id = "Manager lookup without parentheses"
            KEY primary_key (id)
            LINK email_unique (email)
            GRANT user_access (id, username, email)
            PROC GetUserByEmail {
                email_param char(100) "Email parameter"
                "SELECT * FROM Users WHERE email = ?"
            }
            SPROC UpdateUserStatus {
                user_id bigidentity "User ID parameter"
                new_status byte "New status value"
                "UPDATE Users SET status = ? WHERE id = ?"
            }
            PARM {
                title "Enhanced User Management"
                cache_size "1000"
                index_type "btree"
            }

        IMPORT "external_audit_log" [user_id action timestamp] ALIAS audit_table

        TABLE Orders
            order_id bigidentity "Order primary key"
            user_id (uid) long = (id) "Foreign key to Users table"
            product_name char(200) ("Electronics", "Books", "Clothing") "Product category"
            quantity int(MIN=1, MAX=1000) DEFAULTV "1" NOT NULL "Order quantity"
            unit_price double(8,2) NOT NULL "Price per unit"
            total_amount money(12,2) CALC "Calculated total amount"
            order_date datetime "Order creation date"
            notes tlob(4096) "Order notes and comments"
    "#;

    match parse_database(input) {
        Ok(database) => {
            println!("✅ Successfully parsed enhanced JPortal database!");
            println!("📊 Database: {}", database.name);
            println!("🏷️  Package: {}", database.package_name.as_ref().unwrap_or(&"None".to_string()));
            println!("🔗 Server: {}", database.server);
            println!("📁 Schema: {}", database.schema.as_ref().unwrap_or(&"None".to_string()));
            println!("🚩 Flags: {:?}", database.flags);
            println!("📤 Output: {}", database.output.as_ref().unwrap_or(&"None".to_string()));
            println!("📥 Imports: {:?}", database.imports);
            println!("👤 User: {} / Password: {}", database.userid, database.password);
            println!();

            for (i, table) in database.tables.iter().enumerate() {
                println!("📋 Table {}: {}", i + 1, table.name);
                if let Some(alias) = &table.alias {
                    println!("   🏷️  Alias: {}", alias);
                }
                if let Some(check) = &table.check {
                    println!("   ✅ Check: {}", check);
                }
                if !table.comments.is_empty() {
                    println!("   💬 Comments: {:?}", table.comments);
                }
                if !table.options.is_empty() {
                    println!("   ⚙️  Options: {:?}", table.options);
                }
                if table.is_import {
                    println!("   📥 Import table with fields: {:?}", table.import_fields);
                }
                println!("   📊 Fields ({}):", table.fields.len());

                for (j, field) in table.fields.iter().enumerate() {
                    print!("      {}. {} ({:?}", j + 1, field.name, field.field_type);
                    if let Some(length) = field.length {
                        print!(", len: {}", length);
                    }
                    if let Some(precision) = field.precision {
                        print!(", prec: {}", precision);
                        if let Some(scale) = field.scale {
                            print!(", scale: {}", scale);
                        }
                    }
                    print!(")");
                    
                    if let Some(alias) = &field.alias {
                        print!(" ALIAS {}", alias);
                    }
                    if !field.is_null {
                        print!(" NOT NULL");
                    }
                    if let Some(default) = &field.default_value {
                        print!(" DEFAULT {}", default);
                    }
                    if field.is_calc {
                        print!(" CALC");
                    }
                    if let Some(check) = &field.check_value {
                        print!(" CHECK {}", check);
                    }
                    if field.is_sequence {
                        print!(" SEQUENCE");
                    }
                    if field.is_package_field {
                        print!(" PACKAGE");
                    }
                    if let Some(lookup) = &field.lookup_name {
                        print!(" LOOKUP({})", lookup);
                    }
                    if !field.enums.is_empty() {
                        print!(" ENUMS: {:?}", field.enums);
                    }
                    if !field.value_list.is_empty() {
                        print!(" VALUES: {:?}", field.value_list);
                    }
                    if !field.comments.is_empty() {
                        print!(" COMMENTS: {:?}", field.comments);
                    }
                    println!();
                }

                if !table.keys.is_empty() {
                    println!("   🔑 Keys: {:?}", table.keys);
                }
                if !table.links.is_empty() {
                    println!("   🔗 Links: {:?}", table.links);
                }
                if !table.grants.is_empty() {
                    println!("   🛡️  Grants: {:?}", table.grants);
                }
                if !table.procs.is_empty() {
                    println!("   ⚙️  Procedures: {:?}", table.procs);
                }
                if !table.parameters.is_empty() {
                    println!("   📋 Parameters: {:?}", table.parameters);
                }
                println!();
            }

            println!("🎉 Enhanced JPortal parsing completed successfully!");
            println!("✨ Features demonstrated:");
            println!("   • Literal identifiers (L'name')");
            println!("   • Package identifiers with dots");
            println!("   • Enhanced field types with sizes");
            println!("   • Field aliases with parentheses");
            println!("   • Enum values and char lists");
            println!("   • Lookup types and references");
            println!("   • Field modifiers (DEFAULTV, CALC, CHECK)");
            println!("   • Table extras (keys, links, grants)");
            println!("   • Procedures and parameters");
            println!("   • Package fields with namespaces");
            println!("   • Table imports with field lists");
            println!("   • Comments and options");
        }
        Err(e) => {
            eprintln!("❌ Error parsing database: {}", e);
        }
    }
}
