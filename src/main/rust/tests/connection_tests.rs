use rust_parser::parse_database;

#[test]
fn test_server_only() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.server, "\"localhost\"");
    assert_eq!(db.schema, None);
    assert_eq!(db.userid, "");
    assert_eq!(db.password, "");
}

#[test]
fn test_server_with_schema() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost:5432"
        SCHEMA "test_schema"
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.server, "\"localhost:5432\"");
    assert_eq!(db.schema, Some("\"test_schema\"".to_string()));
    assert_eq!(db.userid, "");
    assert_eq!(db.password, "");
}

#[test]
fn test_server_with_userid_password() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        USERID testuser
        PASSWORD testpass
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.server, "\"localhost\"");
    assert_eq!(db.schema, None);
    assert_eq!(db.userid, "testuser");
    assert_eq!(db.password, "testpass");
}

#[test]
fn test_full_connection_info() {
    let input = r#"
        DATABASE TestDB
        SERVER "prod-db.example.com:3306"
        SCHEMA "production_schema"
        USERID dbadmin
        PASSWORD dbsecret
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.server, "\"prod-db.example.com:3306\"");
    assert_eq!(db.schema, Some("\"production_schema\"".to_string()));
    assert_eq!(db.userid, "dbadmin");
    assert_eq!(db.password, "dbsecret");
}

#[test]
fn test_server_with_identifier() {
    let input = r#"
        DATABASE TestDB
        SERVER localhost
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.server, "localhost");
}

#[test]
fn test_schema_with_identifier() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        SCHEMA test_schema
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.schema, Some("test_schema".to_string()));
}

#[test]
fn test_missing_password_should_fail() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        USERID testuser
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    // This should fail because if USERID is present, PASSWORD must also be present
    assert!(result.is_err());
}

#[test]
fn test_missing_userid_should_fail() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        PASSWORD testpass
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    // This should fail because if PASSWORD is present, USERID must also be present
    assert!(result.is_err());
} 