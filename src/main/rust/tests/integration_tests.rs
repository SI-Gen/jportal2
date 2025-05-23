use rust_parser::parse_database;

#[test]
fn test_basic_database() {
    let basic_test = r#"
        DATABASE SimpleDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
    "#;
    
    let result = parse_database(basic_test);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.name, "SimpleDB");
    assert_eq!(db.server, "\"localhost\"");
    assert_eq!(db.tables.len(), 1);
    assert_eq!(db.tables[0].name, "Users");
    assert_eq!(db.tables[0].fields.len(), 2);
}

#[test]
fn test_database_with_field_modifiers() {
    let modifiers_test = r#"
        DATABASE ModifiersDB
        SERVER "localhost"
        TABLE Users
            id int not null
            name char(50) not null
            email char(100)
            active boolean
            created_at timestamp
            updated_at timestamp
    "#;
    
    let result = parse_database(modifiers_test);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.name, "ModifiersDB");
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    assert_eq!(table.fields.len(), 6);
    
    // Check field nullability
    assert!(!table.fields[0].is_null); // id int not null
    assert!(!table.fields[1].is_null); // name char(50) not null
    assert!(table.fields[2].is_null);  // email char(100) - default nullable
    assert!(table.fields[3].is_null);  // active boolean - default nullable
    assert!(table.fields[4].is_null);  // created_at timestamp - default nullable
    assert!(table.fields[5].is_null);  // updated_at timestamp - default nullable
}

#[test]
fn test_multiple_tables_with_various_field_types() {
    let multi_tables_test = r#"
        DATABASE MultiTableDB
        SERVER "localhost"
        TABLE Users
            user_id bigidentity
            username char(50)
            email char(100)
            created_at timestamp
        TABLE Orders
            order_id bigidentity
            user_id long
            order_date timestamp
            total_amount money
        TABLE Products
            product_id bigidentity
            name char(100)
            description tlob
            price money
            in_stock boolean
    "#;
    
    let result = parse_database(multi_tables_test);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.name, "MultiTableDB");
    assert_eq!(db.tables.len(), 3);
    
    // Check table names
    assert_eq!(db.tables[0].name, "Users");
    assert_eq!(db.tables[1].name, "Orders");
    assert_eq!(db.tables[2].name, "Products");
    
    // Check field counts
    assert_eq!(db.tables[0].fields.len(), 4);
    assert_eq!(db.tables[1].fields.len(), 4);
    assert_eq!(db.tables[2].fields.len(), 5);
    
    // Check some field types
    use rust_parser::FieldType;
    assert!(matches!(db.tables[0].fields[0].field_type, FieldType::BigIdentity));
    assert!(matches!(db.tables[1].fields[3].field_type, FieldType::Money));
    assert!(matches!(db.tables[2].fields[2].field_type, FieldType::Tlob));
    assert!(matches!(db.tables[2].fields[4].field_type, FieldType::Boolean));
}

#[test]
fn test_full_featured_database() {
    let full_featured_test = r#"
        DATABASE ProductionDB
        FLAGS "production" "mysql" "optimized"
        PACKAGE com.example.ecommerce
        OUTPUT "generated/sql"
        IMPORT "common_types"
        IMPORT "audit_tables"
        SERVER "prod-db.example.com:3306"
        SCHEMA "ecommerce_schema"
        USERID dbadmin
        PASSWORD dbsecret
        TABLE Users
            user_id bigidentity
            username char(50) not null
            email char(100) not null
            password_hash char(255) not null
            first_name char(50)
            last_name char(50)
            date_of_birth date
            created_at timestamp
            updated_at timestamp
        TABLE Orders
            order_id bigidentity
            user_id long not null
            order_date timestamp
            total_amount money
            status char(20)
            notes tlob
        TABLE OrderItems
            item_id bigidentity
            order_id long not null
            product_id long not null
            quantity int not null
            unit_price money
    "#;
    
    let result = parse_database(full_featured_test);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    assert_eq!(db.name, "ProductionDB");
    
    // Check metadata
    assert_eq!(db.flags.len(), 3);
    assert_eq!(db.flags[0], "production");
    assert_eq!(db.flags[1], "mysql");
    assert_eq!(db.flags[2], "optimized");
    
    assert_eq!(db.package_name, Some("com.example.ecommerce".to_string()));
    assert_eq!(db.output, Some("\"generated/sql\"".to_string()));
    assert_eq!(db.imports.len(), 2);
    assert_eq!(db.imports[0], "\"common_types\"");
    assert_eq!(db.imports[1], "\"audit_tables\"");
    
    // Check connection info
    assert_eq!(db.server, "\"prod-db.example.com:3306\"");
    assert_eq!(db.schema, Some("\"ecommerce_schema\"".to_string()));
    assert_eq!(db.userid, "dbadmin");
    assert_eq!(db.password, "dbsecret");
    
    // Check tables
    assert_eq!(db.tables.len(), 3);
    assert_eq!(db.tables[0].name, "Users");
    assert_eq!(db.tables[1].name, "Orders");
    assert_eq!(db.tables[2].name, "OrderItems");
    
    // Check field counts
    assert_eq!(db.tables[0].fields.len(), 9);
    assert_eq!(db.tables[1].fields.len(), 6);
    assert_eq!(db.tables[2].fields.len(), 5);
    
    // Check some field nullability in Users table
    let users_table = &db.tables[0];
    assert!(users_table.fields[0].is_null);  // user_id bigidentity - default nullable
    assert!(!users_table.fields[1].is_null); // username char(50) not null
    assert!(!users_table.fields[2].is_null); // email char(100) not null
    assert!(!users_table.fields[3].is_null); // password_hash char(255) not null
    assert!(users_table.fields[4].is_null);  // first_name char(50) - default nullable
    
    // Check some field nullability in OrderItems table
    let order_items_table = &db.tables[2];
    assert!(order_items_table.fields[0].is_null);  // item_id bigidentity - default nullable
    assert!(!order_items_table.fields[1].is_null); // order_id long not null
    assert!(!order_items_table.fields[2].is_null); // product_id long not null
    assert!(!order_items_table.fields[3].is_null); // quantity int not null
    assert!(order_items_table.fields[4].is_null);  // unit_price money - default nullable
} 