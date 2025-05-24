use jportal2_lib::parse_database;

#[test]
fn test_basic_database_parsing() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.name, "TestDB");
    assert_eq!(db.server, "\"localhost\"");
    assert_eq!(db.tables.len(), 1);
    assert_eq!(db.tables[0].name, "Users");
    assert_eq!(db.tables[0].fields.len(), 2);
}

#[test]
fn test_database_with_metadata() {
    let input = r#"
        DATABASE TestDB
        FLAGS "flag1" "flag2"
        PACKAGE com.example.test
        OUTPUT "output_dir"
        IMPORT "import1"
        SERVER "localhost"
        SCHEMA "test_schema"
        USERID testuser
        PASSWORD testpass
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.name, "TestDB");
    assert_eq!(db.flags.len(), 2);
    assert_eq!(db.package_name, Some("com.example.test".to_string()));
    assert_eq!(db.server, "\"localhost\"");
    assert_eq!(db.schema, Some("\"test_schema\"".to_string()));
    assert_eq!(db.userid, "testuser");
    assert_eq!(db.password, "testpass");
}

#[test]
fn test_field_modifiers() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int not null
            name char(50) not null
            email char(100)
            active boolean
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.fields.len(), 4);
    
    // Check field nullability
    assert!(!table.fields[0].is_null); // id int not null
    assert!(!table.fields[1].is_null); // name char(50) not null
    assert!(table.fields[2].is_null);  // email char(100) - default nullable
    assert!(table.fields[3].is_null);  // active boolean - default nullable
}

#[test]
fn test_multiple_tables() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
        TABLE Orders
            order_id int
            user_id int
            amount money
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 2);
    assert_eq!(db.tables[0].name, "Users");
    assert_eq!(db.tables[1].name, "Orders");
    assert_eq!(db.tables[0].fields.len(), 2);
    assert_eq!(db.tables[1].fields.len(), 3);
}

#[test]
fn test_various_field_types() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE TestTable
            blob_field blob
            bool_field boolean
            byte_field byte
            char_field char(50)
            int_field int
            long_field long
            date_field date
            timestamp_field timestamp
            money_field money
            bigidentity_field bigidentity
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.fields.len(), 10);
    
    // Check field types
    use jportal2_lib::FieldType;
    assert!(matches!(table.fields[0].field_type, FieldType::Blob));
    assert!(matches!(table.fields[1].field_type, FieldType::Boolean));
    assert!(matches!(table.fields[2].field_type, FieldType::Byte));
    assert!(matches!(table.fields[3].field_type, FieldType::Char));
    assert!(matches!(table.fields[4].field_type, FieldType::Int));
    assert!(matches!(table.fields[5].field_type, FieldType::Long));
    assert!(matches!(table.fields[6].field_type, FieldType::Date));
    assert!(matches!(table.fields[7].field_type, FieldType::Timestamp));
    assert!(matches!(table.fields[8].field_type, FieldType::Money));
    assert!(matches!(table.fields[9].field_type, FieldType::BigIdentity));
}

#[test]
fn test_invalid_database() {
    let input = r#"
        INVALID_KEYWORD TestDB
        SERVER "localhost"
    "#;
    
    let result = parse_database(input);
    assert!(result.is_err());
}

#[test]
fn test_missing_server() {
    let input = r#"
        DATABASE TestDB
        TABLE Users
            id int
    "#;
    
    let result = parse_database(input);
    assert!(result.is_err());
} 