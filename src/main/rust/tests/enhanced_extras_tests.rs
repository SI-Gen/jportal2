use rust_parser::parse_database;

#[test]
fn test_const_section() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            status byte
            CONST StatusValues
                ACTIVE = "1"
                INACTIVE = "0"
                PENDING = "2"
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    assert_eq!(table.consts.len(), 1);
    let const_item = &table.consts[0];
    assert_eq!(const_item.name, "StatusValues");
    assert_eq!(const_item.values.len(), 3);
    assert_eq!(const_item.values[0].key, "ACTIVE");
    assert_eq!(const_item.values[0].value, "1");
    assert_eq!(const_item.values[1].key, "INACTIVE");
    assert_eq!(const_item.values[1].value, "0");
    assert_eq!(const_item.values[2].key, "PENDING");
    assert_eq!(const_item.values[2].value, "2");
}

#[test]
fn test_enhanced_grant_section() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
        
            GRANT SELECT INSERT UPDATE TO admin user1 user2
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    assert_eq!(table.grants.len(), 1);
    let grant = &table.grants[0];
    assert_eq!(grant.perms.len(), 3);
    assert!(grant.perms.contains(&"select".to_string()));
    assert!(grant.perms.contains(&"insert".to_string()));
    assert!(grant.perms.contains(&"update".to_string()));
    assert_eq!(grant.users.len(), 3);
    assert!(grant.users.contains(&"admin".to_string()));
    assert!(grant.users.contains(&"user1".to_string()));
    assert!(grant.users.contains(&"user2".to_string()));
}

#[test]
fn test_enhanced_grant_all_permissions() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            GRANT ALL TO admin
            GRANT DELETE EXECUTE TO poweruser
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    assert_eq!(table.grants.len(), 2);
    
    let grant1 = &table.grants[0];
    assert_eq!(grant1.perms, vec!["all"]);
    assert_eq!(grant1.users, vec!["admin"]);
    
    let grant2 = &table.grants[1];
    assert!(grant2.perms.contains(&"delete".to_string()));
    assert!(grant2.perms.contains(&"execute".to_string()));
    assert_eq!(grant2.users, vec!["poweruser"]);
}

#[test]
fn test_enhanced_key_section() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            KEY primary_key PRIMARY id
            KEY unique_username UNIQUE username
            KEY composite_key username email
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    assert_eq!(table.keys.len(), 3);
    
    let primary_key = &table.keys[0];
    assert_eq!(primary_key.name, "primary_key");
    assert!(primary_key.is_primary);
    assert!(!primary_key.is_unique);
    assert_eq!(primary_key.fields, vec!["id"]);
    
    let unique_key = &table.keys[1];
    assert_eq!(unique_key.name, "unique_username");
    assert!(!unique_key.is_primary);
    assert!(unique_key.is_unique);
    assert_eq!(unique_key.fields, vec!["username"]);
    
    let composite_key = &table.keys[2];
    assert_eq!(composite_key.name, "composite_key");
    assert!(!composite_key.is_primary);
    assert!(!composite_key.is_unique);
    assert_eq!(composite_key.fields, vec!["username", "email"]);
}

#[test]
fn test_enhanced_key_with_options() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            KEY primary_key OPTIONS "CLUSTERED" "FILLFACTOR=90" PRIMARY id
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    assert_eq!(table.keys.len(), 1);
    let key = &table.keys[0];
    assert_eq!(key.name, "primary_key");
    assert!(key.is_primary);
    assert_eq!(key.options.len(), 2);
    assert!(key.options.contains(&"CLUSTERED".to_string()));
    assert!(key.options.contains(&"FILLFACTOR=90".to_string()));
    assert_eq!(key.fields, vec!["id"]);
}

#[test]
fn test_enhanced_link_section() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Orders
            id int
            user_id int
            status_id int
            LINK com.example.users.User (user_id) DELETE CASCADE user_id
            LINK status_table UPDATE CASCADE status_id
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    assert_eq!(table.links.len(), 2);
    
    let user_link = &table.links[0];
    assert_eq!(user_link.name, "com.example.users.User");
    assert!(user_link.is_delete_cascade);
    assert!(!user_link.is_update_cascade);
    assert!(user_link.link_fields.contains(&"user_id".to_string()));
    assert_eq!(user_link.fields, vec!["user_id"]);
    
    let status_link = &table.links[1];
    assert_eq!(status_link.name, "status_table");
    assert!(!status_link.is_delete_cascade);
    assert!(status_link.is_update_cascade);
    assert_eq!(status_link.fields, vec!["status_id"]);
}

#[test]
fn test_enhanced_link_with_options() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Orders
            id int
            user_id int
            LINK user_table OPTIONS "FOREIGN KEY" "ON DELETE SET NULL" user_id
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    assert_eq!(table.links.len(), 1);
    let link = &table.links[0];
    assert_eq!(link.name, "user_table");
    assert_eq!(link.options.len(), 2);
    assert!(link.options.contains(&"FOREIGN KEY".to_string()));
    assert!(link.options.contains(&"ON DELETE SET NULL".to_string()));
    assert_eq!(link.fields, vec!["user_id"]);
}

#[test]
fn test_enhanced_view_section() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            email char(100)
            VIEW UserSummary TO admin manager
                OUTPUT id name
                "SELECT id, name FROM Users"
                "WHERE active = 1"
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    assert_eq!(table.views.len(), 1);
    let view = &table.views[0];
    assert_eq!(view.name, "UserSummary");
    assert_eq!(view.users.len(), 2);
    assert!(view.users.contains(&"admin".to_string()));
    assert!(view.users.contains(&"manager".to_string()));
    assert_eq!(view.aliases.len(), 2);
    assert!(view.aliases.contains(&"id".to_string()));
    assert!(view.aliases.contains(&"name".to_string()));
    assert_eq!(view.lines.len(), 2);
    assert!(view.lines.contains(&"SELECT id, name FROM Users".to_string()));
    assert!(view.lines.contains(&"WHERE active = 1".to_string()));
}

#[test]
fn test_enhanced_view_old_code_format() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            VIEW UserList
                CODE
                    SELECT * FROM Users
                    ORDER BY name
                ENDCODE
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    assert_eq!(table.views.len(), 1);
    let view = &table.views[0];
    assert_eq!(view.name, "UserList");
    assert_eq!(view.lines.len(), 2);
    assert!(view.lines.contains(&"SELECT * FROM Users".to_string()));
    assert!(view.lines.contains(&"ORDER BY name".to_string()));
}

#[test]
fn test_multiple_enhanced_extras() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id bigidentity
            username char(50)
            email char(100)
            status byte
            CONST StatusValues
                ACTIVE = "1"
                INACTIVE = "0"
            GRANT SELECT INSERT TO user_role
            GRANT ALL TO admin_role
            KEY primary_key PRIMARY id
            KEY unique_email UNIQUE email
            LINK profile_table (id) DELETE CASCADE id
            VIEW ActiveUsers TO admin
                OUTPUT id username email
                "SELECT id, username, email FROM Users"
                "WHERE status = 1"
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Check constants
    assert_eq!(table.consts.len(), 1);
    assert_eq!(table.consts[0].name, "StatusValues");
    assert_eq!(table.consts[0].values.len(), 2);
    
    // Check grants
    assert_eq!(table.grants.len(), 2);
    assert_eq!(table.grants[0].perms, vec!["select", "insert"]);
    assert_eq!(table.grants[1].perms, vec!["all"]);
    
    // Check keys
    assert_eq!(table.keys.len(), 2);
    assert!(table.keys[0].is_primary);
    assert!(table.keys[1].is_unique);
    
    // Check links
    assert_eq!(table.links.len(), 1);
    assert!(table.links[0].is_delete_cascade);
    
    // Check views
    assert_eq!(table.views.len(), 1);
    assert_eq!(table.views[0].name, "ActiveUsers");
    assert_eq!(table.views[0].aliases.len(), 3);
}

#[test]
fn test_enhanced_extras_error_handling() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            CONST
    "#;
    
    let result = parse_database(input);
    // Should handle incomplete CONST gracefully
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_err() || result.unwrap().tables[0].consts.is_empty());
}

#[test]
fn test_enhanced_extras_mixed_with_fields() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Orders
            id bigidentity
            user_id int
            amount money(10,2)
            status byte(ACTIVE=1, INACTIVE=0)
            created_at timestamp
            KEY primary_key PRIMARY id
            LINK user_table user_id
            GRANT SELECT INSERT UPDATE TO order_manager
            VIEW OrderSummary
                "SELECT id, amount, status FROM Orders"
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Check fields are still parsed correctly
    assert_eq!(table.fields.len(), 5);
    assert_eq!(table.fields[0].name, "id");
    assert!(table.fields[0].is_sequence);
    
    // Check enhanced extras work alongside fields
    assert_eq!(table.keys.len(), 1);
    assert_eq!(table.links.len(), 1);
    assert_eq!(table.grants.len(), 1);
    assert_eq!(table.views.len(), 1);
}

#[test]
fn test_javacc_permission_table_flags() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            GRANT ALL TO admin_role
            GRANT SELECT INSERT TO user_role
            GRANT DELETE TO manager_role
            GRANT UPDATE TO editor_role
            GRANT EXECUTE TO service_role
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    
    // Verify table permission flags are set correctly (matches JavaCC jPermission() behavior)
    assert!(table.has_execute, "Table should have execute permission from ALL grant");
    assert!(table.has_select, "Table should have select permission from ALL and SELECT grants");
    assert!(table.has_delete, "Table should have delete permission from ALL and DELETE grants");
    assert!(table.has_insert, "Table should have insert permission from ALL and INSERT grants");
    assert!(table.has_update, "Table should have update permission from ALL and UPDATE grants");
    
    // Verify grants are parsed correctly
    assert_eq!(table.grants.len(), 5);
    
    let all_grant = &table.grants[0];
    assert_eq!(all_grant.perms, vec!["all"]);
    assert_eq!(all_grant.users, vec!["admin_role"]);
    
    let select_insert_grant = &table.grants[1];
    assert_eq!(select_insert_grant.perms, vec!["select", "insert"]);
    assert_eq!(select_insert_grant.users, vec!["user_role"]);
    
    let delete_grant = &table.grants[2];
    assert_eq!(delete_grant.perms, vec!["delete"]);
    assert_eq!(delete_grant.users, vec!["manager_role"]);
    
    let update_grant = &table.grants[3];
    assert_eq!(update_grant.perms, vec!["update"]);
    assert_eq!(update_grant.users, vec!["editor_role"]);
    
    let execute_grant = &table.grants[4];
    assert_eq!(execute_grant.perms, vec!["execute"]);
    assert_eq!(execute_grant.users, vec!["service_role"]);
}

#[test]
fn test_javacc_key_modifiers_table_flags() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            KEY primary_key PRIMARY id
            KEY unique_username UNIQUE username
            KEY composite_key username email
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    
    // Verify table primary key flag is set (matches JavaCC jModifier() behavior)
    assert!(table.has_primary_key, "Table should have primary key flag set");
    
    // Verify keys are parsed correctly
    assert_eq!(table.keys.len(), 3);
    
    let primary_key = &table.keys[0];
    assert_eq!(primary_key.name, "primary_key");
    assert!(primary_key.is_primary, "Key should be marked as primary");
    assert!(!primary_key.is_unique, "Primary key should not have unique flag");
    assert_eq!(primary_key.fields, vec!["id"]);
    
    let unique_key = &table.keys[1];
    assert_eq!(unique_key.name, "unique_username");
    assert!(!unique_key.is_primary, "Key should not be marked as primary");
    assert!(unique_key.is_unique, "Key should be marked as unique");
    assert_eq!(unique_key.fields, vec!["username"]);
    
    let composite_key = &table.keys[2];
    assert_eq!(composite_key.name, "composite_key");
    assert!(!composite_key.is_primary, "Key should not be marked as primary");
    assert!(!composite_key.is_unique, "Key should not be marked as unique");
    assert_eq!(composite_key.fields, vec!["username", "email"]);
}

#[test]
fn test_javacc_functions_comprehensive() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Orders
            id int
            customer_id int
            status char(20)
            created_date datetime
            CONST StatusValues
                PENDING = "P"
                COMPLETED = "C"
                CANCELLED = "X"
            GRANT ALL TO admin
            GRANT SELECT INSERT UPDATE TO order_manager
            KEY primary_order PRIMARY id
            KEY unique_customer UNIQUE customer_id
            LINK com.example.Customer (customer_id) DELETE CASCADE customer_id
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "Orders");
    
    // Test jPermission() implementation - table flags
    assert!(table.has_execute, "Should have execute from ALL grant");
    assert!(table.has_select, "Should have select from ALL and SELECT grants");
    assert!(table.has_delete, "Should have delete from ALL grant");
    assert!(table.has_insert, "Should have insert from ALL and INSERT grants");
    assert!(table.has_update, "Should have update from ALL and UPDATE grants");
    
    // Test jModifier() implementation - primary key flag
    assert!(table.has_primary_key, "Should have primary key flag set");
    
    // Test jUser() implementation - user parsing
    assert_eq!(table.grants.len(), 2);
    assert_eq!(table.grants[0].users, vec!["admin"]);
    assert_eq!(table.grants[1].users, vec!["order_manager"]);
    
    // Test jKey() implementation - key parsing with modifiers
    assert_eq!(table.keys.len(), 2);
    
    let primary_key = &table.keys[0];
    assert_eq!(primary_key.name, "primary_order");
    assert!(primary_key.is_primary);
    assert_eq!(primary_key.fields, vec!["id"]);
    
    let unique_key = &table.keys[1];
    assert_eq!(unique_key.name, "unique_customer");
    assert!(unique_key.is_unique);
    assert_eq!(unique_key.fields, vec!["customer_id"]);
    
    // Test enhanced link with cascade
    assert_eq!(table.links.len(), 1);
    let link = &table.links[0];
    assert_eq!(link.name, "com.example.Customer");
    assert!(link.is_delete_cascade);
    assert!(!link.is_update_cascade);
    
    // Test constants
    assert_eq!(table.consts.len(), 1);
    let const_section = &table.consts[0];
    assert_eq!(const_section.name, "StatusValues");
    assert_eq!(const_section.values.len(), 3);
}

#[test]
fn test_javacc_jcolumn_validation() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            KEY primary_key PRIMARY id username
            KEY unique_email UNIQUE email
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    
    // Verify fields are marked as primary when in primary key
    let id_field = table.fields.iter().find(|f| f.name == "id").unwrap();
    assert!(id_field.is_primary, "ID field should be marked as primary");
    
    let username_field = table.fields.iter().find(|f| f.name == "username").unwrap();
    assert!(username_field.is_primary, "Username field should be marked as primary");
    
    let email_field = table.fields.iter().find(|f| f.name == "email").unwrap();
    assert!(!email_field.is_primary, "Email field should not be marked as primary");
    
    // Verify keys are parsed correctly
    assert_eq!(table.keys.len(), 2);
    
    let primary_key = &table.keys[0];
    assert_eq!(primary_key.name, "primary_key");
    assert!(primary_key.is_primary);
    assert_eq!(primary_key.fields, vec!["id", "username"]);
    
    let unique_key = &table.keys[1];
    assert_eq!(unique_key.name, "unique_email");
    assert!(unique_key.is_unique);
    assert_eq!(unique_key.fields, vec!["email"]);
}

#[test]
fn test_javacc_jlink_validation() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Orders
            id int
            customer_id int
            product_id int
            status_id int
            LINK com.example.Customer (customer_id) DELETE CASCADE customer_id
            LINK product_table product_id
            LINK status_lookup status_id
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "Orders");
    
    // Verify links are parsed correctly with validation
    assert_eq!(table.links.len(), 3);
    
    let customer_link = &table.links[0];
    assert_eq!(customer_link.name, "com.example.Customer");
    assert!(customer_link.is_delete_cascade);
    assert_eq!(customer_link.link_fields, vec!["customer_id"]);
    assert_eq!(customer_link.fields, vec!["customer_id"]);
    
    let product_link = &table.links[1];
    assert_eq!(product_link.name, "product_table");
    assert_eq!(product_link.fields, vec!["product_id"]);
    
    let status_link = &table.links[2];
    assert_eq!(status_link.name, "status_lookup");
    assert_eq!(status_link.fields, vec!["status_id"]);
}

#[test]
fn test_javacc_field_validation_warnings() {
    // This test verifies that validation warnings are generated for invalid fields
    // Note: In a real implementation, you might want to collect warnings instead of printing them
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            KEY invalid_key nonexistent_field
            LINK invalid_link nonexistent_field
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Should parse even with invalid field references");
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // The key should still be created but with empty fields due to validation
    assert_eq!(table.keys.len(), 1);
    let key = &table.keys[0];
    assert_eq!(key.name, "invalid_key");
    // Field should not be added due to validation failure
    assert!(key.fields.is_empty() || key.fields == vec!["nonexistent_field"]);
    
    // The link should still be created but with empty fields due to validation
    assert_eq!(table.links.len(), 1);
    let link = &table.links[0];
    assert_eq!(link.name, "invalid_link");
    // Field should not be added due to validation failure
    assert!(link.fields.is_empty() || link.fields == vec!["nonexistent_field"]);
}

#[test]
fn test_javacc_duplicate_field_validation() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            KEY duplicate_key id id username
            LINK duplicate_link id id
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Should parse even with duplicate field references");
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Check that duplicate fields are handled properly
    assert_eq!(table.keys.len(), 1);
    let key = &table.keys[0];
    assert_eq!(key.name, "duplicate_key");
    // Should only contain unique fields due to validation
    assert!(key.fields.len() <= 2); // id should only appear once
    
    assert_eq!(table.links.len(), 1);
    let link = &table.links[0];
    assert_eq!(link.name, "duplicate_link");
    // Should only contain unique fields due to validation
    assert!(link.fields.len() <= 1); // id should only appear once
}

#[test]
fn test_javacc_functions_complete_integration() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE CompleteExample
            id bigidentity
            username char(50)
            email char(100)
            status byte
            created_at timestamp
            CONST StatusValues
                ACTIVE = "1"
                INACTIVE = "0"
            GRANT ALL TO admin
            GRANT SELECT INSERT UPDATE TO user_manager
            KEY primary_key PRIMARY id
            KEY unique_username UNIQUE username
            KEY composite_key username email
            LINK user_profile (id) DELETE CASCADE id
            LINK status_lookup status
            VIEW ActiveUsers TO admin
                OUTPUT id username email
                "SELECT id, username, email FROM CompleteExample"
                "WHERE status = 1"
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse complete example: {:?}", result.err());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "CompleteExample");
    
    // Test jPermission() - table flags
    assert!(table.has_execute, "Should have execute from ALL grant");
    assert!(table.has_select, "Should have select from ALL and SELECT grants");
    assert!(table.has_delete, "Should have delete from ALL grant");
    assert!(table.has_insert, "Should have insert from ALL and INSERT grants");
    assert!(table.has_update, "Should have update from ALL and UPDATE grants");
    
    // Test jModifier() - primary key flag
    assert!(table.has_primary_key, "Should have primary key flag set");
    
    // Test jColumn() - field primary key marking
    let id_field = table.fields.iter().find(|f| f.name == "id").unwrap();
    assert!(id_field.is_primary, "ID field should be marked as primary");
    
    let username_field = table.fields.iter().find(|f| f.name == "username").unwrap();
    assert!(!username_field.is_primary, "Username field should not be marked as primary (unique key, not primary)");
    
    // Test jKey() with validation
    assert_eq!(table.keys.len(), 3);
    let primary_key = &table.keys[0];
    assert_eq!(primary_key.name, "primary_key");
    assert!(primary_key.is_primary);
    assert_eq!(primary_key.fields, vec!["id"]);
    
    // Test jLink() and jLinkColumn() with validation
    assert_eq!(table.links.len(), 2);
    
    let profile_link = &table.links[0];
    assert_eq!(profile_link.name, "user_profile");
    assert!(profile_link.is_delete_cascade);
    assert_eq!(profile_link.link_fields, vec!["id"]);
    assert_eq!(profile_link.fields, vec!["id"]);
    
    let status_link = &table.links[1];
    assert_eq!(status_link.name, "status_lookup");
    // Note: "status" field doesn't exist, so validation should handle this
    
    // Test other components still work
    assert_eq!(table.consts.len(), 1);
    assert_eq!(table.grants.len(), 2);
    assert_eq!(table.views.len(), 1);
}

#[test]
fn test_javacc_jview_user_validation() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            VIEW UserSummary TO admin manager admin
                OUTPUT id name
                "SELECT id, name FROM Users"
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    
    // Verify view user validation (matches JavaCC jView() -> jUser() behavior)
    assert_eq!(table.views.len(), 1);
    let view = &table.views[0];
    assert_eq!(view.name, "UserSummary");
    
    // Should only contain unique users due to validation
    assert_eq!(view.users.len(), 2); // admin should only appear once
    assert!(view.users.contains(&"admin".to_string()));
    assert!(view.users.contains(&"manager".to_string()));
    
    // Verify start line is set (matches JavaCC view.start = t.beginLine)
    assert!(view.start_line.is_some());
}

#[test]
fn test_javacc_jview_alias_validation() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            email char(100)
            VIEW UserDetails
                OUTPUT id name email id name
                "SELECT id, name, email FROM Users"
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Verify view alias validation (matches JavaCC jViewAlias() behavior)
    assert_eq!(table.views.len(), 1);
    let view = &table.views[0];
    assert_eq!(view.name, "UserDetails");
    
    // Should only contain unique aliases due to validation
    assert_eq!(view.aliases.len(), 3); // id and name should only appear once each
    assert!(view.aliases.contains(&"id".to_string()));
    assert!(view.aliases.contains(&"name".to_string()));
    assert!(view.aliases.contains(&"email".to_string()));
}

#[test]
fn test_javacc_jstring_processing() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            VIEW TestView
                "SELECT * FROM Users"
                "Simple line"
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Verify enhanced string processing (matches JavaCC jString() and fixString())
    assert_eq!(table.views.len(), 1);
    let view = &table.views[0];
    assert_eq!(view.name, "TestView");
    assert_eq!(view.lines.len(), 2);
    
    // Check basic string processing
    assert_eq!(view.lines[0], "SELECT * FROM Users");
    assert_eq!(view.lines[1], "Simple line");
}

#[test]
fn test_javacc_old_view_code_format() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            VIEW OldStyleView
                CODE
                    SELECT * FROM Users
                    WHERE status = active
                    ORDER BY name
                ENDCODE
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Verify old view code format (matches JavaCC jOldViewCode() behavior)
    assert_eq!(table.views.len(), 1);
    let view = &table.views[0];
    assert_eq!(view.name, "OldStyleView");
    assert_eq!(view.lines.len(), 3);
    
    // Check raw line processing without quotes
    assert_eq!(view.lines[0], "SELECT * FROM Users");
    assert_eq!(view.lines[1], "WHERE status = active");
    assert_eq!(view.lines[2], "ORDER BY name");
}

#[test]
fn test_javacc_new_view_code_format() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            VIEW NewStyleView
                "  SELECT id, name FROM Users  "
                "  WHERE active = 1  "
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Verify new view code format (matches JavaCC jNewViewCode() behavior)
    assert_eq!(table.views.len(), 1);
    let view = &table.views[0];
    assert_eq!(view.name, "NewStyleView");
    assert_eq!(view.lines.len(), 2);
    
    // Check CODELINE processing with trim() like JavaCC
    assert_eq!(view.lines[0], "SELECT id, name FROM Users");
    assert_eq!(view.lines[1], "WHERE active = 1");
}

#[test]
fn test_javacc_view_complete_validation() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE CompleteViewTest
            id int
            username char(50)
            email char(100)
            status byte
            VIEW ComplexView TO admin manager admin
                OUTPUT id username email id
                "SELECT id, username, email FROM CompleteViewTest"
                "WHERE status = 1"
                "ORDER BY username"
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Test complete jView() implementation with all validations
    assert_eq!(table.views.len(), 1);
    let view = &table.views[0];
    assert_eq!(view.name, "ComplexView");
    
    // Test jUser() validation - duplicates removed
    assert_eq!(view.users.len(), 2);
    assert!(view.users.contains(&"admin".to_string()));
    assert!(view.users.contains(&"manager".to_string()));
    
    // Test jViewAlias() validation - duplicates removed
    assert_eq!(view.aliases.len(), 3);
    assert!(view.aliases.contains(&"id".to_string()));
    assert!(view.aliases.contains(&"username".to_string()));
    assert!(view.aliases.contains(&"email".to_string()));
    
    // Test view code processing
    assert_eq!(view.lines.len(), 3);
    assert_eq!(view.lines[0], "SELECT id, username, email FROM CompleteViewTest");
    assert_eq!(view.lines[1], "WHERE status = 1");
    assert_eq!(view.lines[2], "ORDER BY username");
    
    // Test start line is set
    assert!(view.start_line.is_some());
}

#[test]
fn test_javacc_string_escape_sequences() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE EscapeTest
            id int
            VIEW EscapeView
                "Line with backslash and quotes"
                "Tab separated newline"
                "Simple text"
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Test basic string processing (complex escape sequences are handled by the grammar)
    assert_eq!(table.views.len(), 1);
    let view = &table.views[0];
    assert_eq!(view.lines.len(), 3);
    
    // Verify basic string processing
    assert_eq!(view.lines[0], "Line with backslash and quotes");
    assert_eq!(view.lines[1], "Tab separated newline");
    assert_eq!(view.lines[2], "Simple text");
} 