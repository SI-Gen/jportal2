use rust_parser::parse_database;

#[test]
fn test_package_ident_with_dots() {
    let input = r#"
        DATABASE TestDB
        PACKAGE com.example.test.package
        SERVER "localhost"
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.package_name, Some("com.example.test.package".to_string()));
}

#[test]
fn test_literal_identifier_database_name() {
    let input = r#"
        DATABASE L'Special Database Name'
        SERVER "localhost"
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.name, "Special Database Name");
}

#[test]
fn test_literal_identifier_lowercase() {
    let input = r#"
        DATABASE l'lowercase literal'
        SERVER "localhost"
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.name, "lowercase literal");
}

#[test]
fn test_ident_or_string_variations() {
    let input = r#"
        DATABASE TestDB
        SERVER localhost
        SCHEMA "string_schema"
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.server, "localhost");
    assert_eq!(db.schema, Some("\"string_schema\"".to_string()));
}

#[test]
fn test_table_with_key() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            KEY primary_key (id)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    assert_eq!(table.fields.len(), 2);
    assert_eq!(table.keys.len(), 1);
    assert_eq!(table.keys[0].name, "primary_key");
    assert_eq!(table.keys[0].fields, vec!["id"]);
}

#[test]
fn test_table_with_link() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Orders
            id int
            user_id int
            LINK user_link (user_id)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.links.len(), 1);
    assert_eq!(table.links[0].name, "user_link");
    assert_eq!(table.links[0].fields, vec!["user_id"]);
}

#[test]
fn test_table_with_grant() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            GRANT select_grant (id, name)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.grants.len(), 1);
    assert_eq!(table.grants[0].perms, vec!["select_grant"]);
    assert_eq!(table.grants[0].users, vec!["id", "name"]);
}

#[test]
fn test_table_with_proc() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            PROC GetUser {
                input_id int
                "SELECT * FROM Users WHERE id = ?"
            }
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 1);
    assert_eq!(table.procs[0].name, "GetUser");
    assert!(table.procs[0].is_proc);
    assert!(!table.procs[0].is_sproc);
}

#[test]
fn test_table_with_sproc() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            SPROC UpdateUser {
                new_name char(50)
                "UPDATE Users SET name = ? WHERE id = ?"
            }
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 1);
    assert_eq!(table.procs[0].name, "UpdateUser");
    assert!(!table.procs[0].is_proc);
    assert!(table.procs[0].is_sproc);
}

#[test]
fn test_table_with_parm() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            PARM {
                "User Management"
                "Additional parameters"
            }
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    println!("Parameters: {:?}", table.parameters);
    assert_eq!(table.parameters.len(), 1);
    assert_eq!(table.parameters[0].title, Some("User Management".to_string()));
}

#[test]
fn test_table_import() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        IMPORT ExternalUsers
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "ExternalUsers");
    assert!(table.is_import);
    assert_eq!(table.fields.len(), 0); // Import tables don't have field definitions
}

#[test]
fn test_complex_table_with_all_features() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id bigidentity
            username char(50) not null
            email char(100) not null
            created_at timestamp
            KEY primary_key (id)
            KEY unique_username (username)
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
                title "User Table Parameters"
                cache_size "1000"
                "Additional configuration"
            }
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Check basic table info
    assert_eq!(table.name, "Users");
    assert_eq!(table.fields.len(), 4);
    assert!(!table.is_import);
    
    // Check keys
    assert_eq!(table.keys.len(), 2);
    assert_eq!(table.keys[0].name, "primary_key");
    assert_eq!(table.keys[1].name, "unique_username");
    
    // Check links
    assert_eq!(table.links.len(), 1);
    assert_eq!(table.links[0].name, "email_link");
    
    // Check grants
    assert_eq!(table.grants.len(), 1);
    assert_eq!(table.grants[0].perms, vec!["select_grant"]);
    
    // Check procedures
    assert_eq!(table.procs.len(), 2);
    assert_eq!(table.procs[0].name, "GetUserByEmail");
    assert!(table.procs[0].is_proc);
    assert_eq!(table.procs[1].name, "CreateUser");
    assert!(table.procs[1].is_sproc);
    
    // Check parameters
    assert_eq!(table.parameters.len(), 1);
} 