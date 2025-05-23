use rust_parser::parse_database;

#[test]
fn test_enhanced_table_import_with_field_list() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        IMPORT "external_module" [field1 field2 field3]
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "\"external_module\"");
    assert!(table.is_import);
    assert_eq!(table.import_fields.len(), 3);
    assert_eq!(table.import_fields, vec!["field1", "field2", "field3"]);
}

#[test]
fn test_enhanced_table_import_with_alias() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        IMPORT ExternalUsers ALIAS "UserAlias"
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.name, "ExternalUsers");
    assert!(table.is_import);
    assert_eq!(table.alias, Some("\"UserAlias\"".to_string()));
}

#[test]
fn test_enhanced_table_import_with_field_list_and_alias() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        IMPORT "external_db" [id name email] ALIAS ext_table
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.name, "\"external_db\"");
    assert!(table.is_import);
    assert_eq!(table.import_fields, vec!["id", "name", "email"]);
    assert_eq!(table.alias, Some("ext_table".to_string()));
}

#[test]
fn test_enhanced_table_with_alias() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users ALIAS "UserTable"
            id int
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    assert!(!table.is_import);
    assert_eq!(table.alias, Some("\"UserTable\"".to_string()));
    assert_eq!(table.fields.len(), 2);
}

#[test]
fn test_enhanced_table_with_check() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users CHECK "id > 0"
            id int
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    assert_eq!(table.check, Some("id > 0".to_string()));
}

#[test]
fn test_enhanced_table_with_comments() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            "This is a user table"
            "Contains user information"
            id int
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    assert_eq!(table.comments.len(), 2);
    assert_eq!(table.comments[0], "This is a user table");
    assert_eq!(table.comments[1], "Contains user information");
}

#[test]
fn test_enhanced_table_with_options() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users OPTIONS "option1" "option2" "option3"
            id int
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    assert_eq!(table.options.len(), 3);
    assert_eq!(table.options, vec!["option1", "option2", "option3"]);
}

#[test]
fn test_enhanced_table_with_all_features() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users ALIAS "UserTable" CHECK "id > 0"
            "User management table"
            "Stores user data"
            OPTIONS "cache" "index"
            id int
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    assert_eq!(table.alias, Some("\"UserTable\"".to_string()));
    assert_eq!(table.check, Some("id > 0".to_string()));
    assert_eq!(table.comments.len(), 2);
    assert_eq!(table.options.len(), 2);
    assert_eq!(table.fields.len(), 2);
}

#[test]
fn test_package_field_basic() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            com.example.user.id int
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.fields.len(), 2);
    
    let package_field = &table.fields[0];
    assert_eq!(package_field.name, "com.example.user.id");
    assert!(package_field.is_package_field);
}

#[test]
fn test_package_field_with_alias() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            com.example.user.id int ALIAS user_id
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let package_field = &table.fields[0];
    assert_eq!(package_field.name, "com.example.user.id");
    assert_eq!(package_field.alias, Some("user_id".to_string()));
    assert!(package_field.is_package_field);
}

#[test]
fn test_package_field_with_defaultv() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            com.example.user.status char(10) DEFAULTV "active"
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let package_field = &table.fields[0];
    assert_eq!(package_field.name, "com.example.user.status");
    assert_eq!(package_field.default_value, Some("active".to_string()));
    assert!(package_field.is_package_field);
}

#[test]
fn test_package_field_with_calc() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            com.example.user.computed_field int CALC
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let package_field = &table.fields[0];
    assert_eq!(package_field.name, "com.example.user.computed_field");
    assert!(package_field.is_calc);
    assert!(package_field.is_package_field);
}

#[test]
fn test_package_field_with_null_modifiers() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            com.example.user.required_field int NOT NULL
            com.example.user.optional_field int NULL
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let required_field = &table.fields[0];
    assert_eq!(required_field.name, "com.example.user.required_field");
    assert!(!required_field.is_null);
    assert!(required_field.is_package_field);
    
    let optional_field = &table.fields[1];
    assert_eq!(optional_field.name, "com.example.user.optional_field");
    assert!(optional_field.is_null);
    assert!(optional_field.is_package_field);
}

#[test]
fn test_package_field_with_comments() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            com.example.user.id int "Primary key field" "Auto-generated"
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let package_field = &table.fields[0];
    assert_eq!(package_field.name, "com.example.user.id");
    assert_eq!(package_field.comments.len(), 2);
    assert_eq!(package_field.comments[0], "Primary key field");
    assert_eq!(package_field.comments[1], "Auto-generated");
    assert!(package_field.is_package_field);
}

#[test]
fn test_package_field_with_all_features() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            com.example.user.status char(20) ALIAS user_status DEFAULTV "pending" NOT NULL CHECK "status IN ('active', 'pending', 'disabled')" "User status field" "Required field"
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let package_field = &table.fields[0];
    
    assert_eq!(package_field.name, "com.example.user.status");
    assert_eq!(package_field.alias, Some("user_status".to_string()));
    assert_eq!(package_field.default_value, Some("pending".to_string()));
    assert!(!package_field.is_null);
    assert_eq!(package_field.check_value, Some("status IN ('active', 'pending', 'disabled')".to_string()));
    assert_eq!(package_field.comments.len(), 2);
    assert!(package_field.is_package_field);
}

#[test]
fn test_mixed_regular_and_package_fields() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int not null
            com.example.user.external_id long ALIAS ext_id
            name char(50)
            com.example.audit.created_at timestamp DEFAULTV "NOW()"
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.fields.len(), 4);
    
    // Regular field
    assert_eq!(table.fields[0].name, "id");
    assert!(!table.fields[0].is_package_field);
    
    // Package field
    assert_eq!(table.fields[1].name, "com.example.user.external_id");
    assert!(table.fields[1].is_package_field);
    assert_eq!(table.fields[1].alias, Some("ext_id".to_string()));
    
    // Regular field
    assert_eq!(table.fields[2].name, "name");
    assert!(!table.fields[2].is_package_field);
    
    // Package field with default
    assert_eq!(table.fields[3].name, "com.example.audit.created_at");
    assert!(table.fields[3].is_package_field);
    assert_eq!(table.fields[3].default_value, Some("NOW()".to_string()));
} 