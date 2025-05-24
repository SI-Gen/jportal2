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
                    "SELECT * FROM Users"
                    "ORDER BY name"
                ENDCODE
    "#;
    
    let result = parse_database(input);
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