use rust_parser::parse_database;

#[test]
fn test_enhanced_enum_value_with_link() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            status byte(LINK status_table ACTIVE=1, INACTIVE=0, PENDING=2)
            priority short(HIGH=10, MEDIUM=5, LOW=1)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let status_field = &table.fields[0];
    assert_eq!(status_field.enum_link, Some("status_table".to_string()));
    assert_eq!(status_field.enums.len(), 3);
    assert_eq!(status_field.enums[0].name, "ACTIVE");
    assert_eq!(status_field.enums[0].value, 1);
    
    let priority_field = &table.fields[1];
    assert_eq!(priority_field.enum_link, None);
    assert_eq!(priority_field.enums.len(), 3);
    assert_eq!(priority_field.enums[0].name, "HIGH");
    assert_eq!(priority_field.enums[0].value, 10);
}

#[test]
fn test_enhanced_char_list_with_braces() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            category char{electronics, books, clothing}
            status byte{active, inactive, pending}
    "#;
    
    let result = parse_database(input);
    if let Err(e) = &result {
        println!("Error: {}", e);
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let category_field = &table.fields[0];
    assert_eq!(category_field.value_list.len(), 3);
    assert_eq!(category_field.value_list, vec!["electronics", "books", "clothing"]);
    
    let status_field = &table.fields[1];
    assert_eq!(status_field.value_list.len(), 3);
    assert_eq!(status_field.value_list, vec!["active", "inactive", "pending"]);
}

#[test]
fn test_enhanced_char_size_formats() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            name1 char[100]
            name2 char(200)
            name3 char 300
            blob1 blob[1024]
            xml1 xml(2048)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    assert_eq!(table.fields[0].length, Some(100)); // char[100]
    assert_eq!(table.fields[1].length, Some(200)); // char(200)
    assert_eq!(table.fields[2].length, Some(300)); // char 300
    assert_eq!(table.fields[3].length, Some(1024)); // blob[1024]
    assert_eq!(table.fields[4].length, Some(2048)); // xml(2048)
}

#[test]
fn test_enhanced_float_size_formats() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            price1 double[10,2]
            price2 money(12,4)
            rate float[8,3]
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let price1_field = &table.fields[0];
    assert_eq!(price1_field.precision, Some(10));
    assert_eq!(price1_field.scale, Some(2));
    
    let price2_field = &table.fields[1];
    assert_eq!(price2_field.precision, Some(12));
    assert_eq!(price2_field.scale, Some(4));
    
    let rate_field = &table.fields[2];
    assert_eq!(rate_field.precision, Some(8));
    assert_eq!(rate_field.scale, Some(3));
}

#[test]
fn test_enhanced_enum_char_formats() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            status1 ansichar[50]
            status2 ansichar(LINK status_ref ACTIVE=65, INACTIVE=73)
            status3 ansichar 100
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let status1_field = &table.fields[0];
    assert_eq!(status1_field.length, Some(50));
    
    let status2_field = &table.fields[1];
    assert_eq!(status2_field.enum_link, Some("status_ref".to_string()));
    assert_eq!(status2_field.enums.len(), 2);
    assert_eq!(status2_field.enums[0].name, "ACTIVE");
    assert_eq!(status2_field.enums[0].value, 65); // ASCII 'A'
    
    let status3_field = &table.fields[2];
    assert_eq!(status3_field.length, Some(100));
}

#[test]
fn test_enhanced_parameter_directives() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            name char(50)
            PARM {
                "User Management Parameters"
                PARMSHOWS id name
                PARMVIEWONLY
                PARMSUPPLIED name
                PARMCACHE GetAllUsers extra1 extra2
                PARMREADER SELECTALL
                PARMINSERT INSERT
                PARMUPDATE UPDATE
                PARMDELETE DELETEONE
            }
    "#;
    
    let result = parse_database(input);
    if let Err(e) = &result {
        println!("Error: {}", e);
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let parameter = &table.parameters[0];
    
    println!("Parameter: {:?}", parameter);
    
    assert_eq!(parameter.title, Some("User Management Parameters".to_string()));
    assert!(parameter.is_view_only);
    assert!(parameter.shows.contains(&"id".to_string()));
    assert!(parameter.shows.contains(&"name".to_string()));
    assert!(parameter.supplied.contains(&"name".to_string()));
    assert_eq!(parameter.cache, Some("GetAllUsers".to_string()));
    assert!(parameter.cache_extras.contains(&"extra1".to_string()));
    assert!(parameter.cache_extras.contains(&"extra2".to_string()));
    assert_eq!(parameter.reader, Some("SelectAll".to_string()));
    assert_eq!(parameter.insert, Some("Insert".to_string()));
    assert_eq!(parameter.update, Some("Update".to_string()));
    assert_eq!(parameter.delete, Some("DeleteOne".to_string()));
}

#[test]
fn test_mixed_enhanced_features() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Orders
            id bigidentity
            status byte(LINK order_status PENDING=1, PROCESSING=2, COMPLETED=3)
            category char{electronics, books, clothing, other}
            amount money[12,2]
            notes tlob(4096)
            metadata ansichar(LINK meta_ref ACTIVE=65, INACTIVE=73)
            PARM {
                "Order Management"
                PARMSHOWS id status category
                PARMCACHE GetOrdersByStatus
            }
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Check enhanced enum with link
    let status_field = &table.fields[1];
    assert_eq!(status_field.enum_link, Some("order_status".to_string()));
    assert_eq!(status_field.enums.len(), 3);
    
    // Check enhanced char list
    let category_field = &table.fields[2];
    assert_eq!(category_field.value_list.len(), 4);
    assert_eq!(category_field.value_list[3], "other");
    
    // Check enhanced float size
    let amount_field = &table.fields[3];
    assert_eq!(amount_field.precision, Some(12));
    assert_eq!(amount_field.scale, Some(2));
    
    // Check enhanced char size
    let notes_field = &table.fields[4];
    assert_eq!(notes_field.length, Some(4096));
    
    // Check enhanced enum char
    let metadata_field = &table.fields[5];
    assert_eq!(metadata_field.enum_link, Some("meta_ref".to_string()));
    assert_eq!(metadata_field.enums.len(), 2);
    
    // Check enhanced parameters
    let parameter = &table.parameters[0];
    assert_eq!(parameter.title, Some("Order Management".to_string()));
    assert!(parameter.shows.len() >= 3);
}

#[test]
fn test_backward_compatibility() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            status byte(ACTIVE=1, INACTIVE=0)
            gender char("M", "F", "O")
            price money(10,2)
    "#;
    
    let result = parse_database(input);
    if let Err(e) = &result {
        println!("Error: {}", e);
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Original enum format should still work
    let status_field = &table.fields[0];
    assert_eq!(status_field.enums.len(), 2);
    assert_eq!(status_field.enums[0].name, "ACTIVE");
    assert_eq!(status_field.enums[0].value, 1);
    
    // Original char list format should still work
    let gender_field = &table.fields[1];
    assert_eq!(gender_field.value_list.len(), 3);
    assert_eq!(gender_field.value_list, vec!["M", "F", "O"]);
    
    // Original float size format should still work
    let price_field = &table.fields[2];
    assert_eq!(price_field.precision, Some(10));
    assert_eq!(price_field.scale, Some(2));
}

#[test]
fn test_field_with_parentheses_alias() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            user_id (id) int
            name char(50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let field = &table.fields[0];
    assert_eq!(field.name, "user_id");
    assert_eq!(field.alias, Some("id".to_string()));
}

#[test]
fn test_field_with_comments() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int "Primary key field" "Auto-generated identifier"
            name char(50) "User name field"
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let id_field = &table.fields[0];
    assert_eq!(id_field.name, "id");
    assert_eq!(id_field.comments.len(), 2);
    assert_eq!(id_field.comments[0], "Primary key field");
    assert_eq!(id_field.comments[1], "Auto-generated identifier");
    
    let name_field = &table.fields[1];
    assert_eq!(name_field.name, "name");
    assert_eq!(name_field.comments.len(), 1);
    assert_eq!(name_field.comments[0], "User name field");
}

#[test]
fn test_field_with_char_size() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            name char(100)
            description blob(2048)
            xml_data xml(5000)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let name_field = &table.fields[0];
    assert_eq!(name_field.length, Some(100));
    
    let desc_field = &table.fields[1];
    assert_eq!(desc_field.length, Some(2048));
    
    let xml_field = &table.fields[2];
    assert_eq!(xml_field.length, Some(5000));
}

#[test]
fn test_field_with_float_size() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            price double(10,2)
            rate float(8,4)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let price_field = &table.fields[0];
    assert_eq!(price_field.precision, Some(10));
    assert_eq!(price_field.scale, Some(2));
    
    let rate_field = &table.fields[1];
    assert_eq!(rate_field.precision, Some(8));
    assert_eq!(rate_field.scale, Some(4));
}

#[test]
fn test_field_with_enum_values() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            status byte(ACTIVE=1, INACTIVE=0, PENDING=2)
            priority short(LOW=1, MEDIUM=5, HIGH=10)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let status_field = &table.fields[0];
    assert_eq!(status_field.enums.len(), 3);
    assert_eq!(status_field.enums[0].name, "ACTIVE");
    assert_eq!(status_field.enums[0].value, 1);
    assert_eq!(status_field.enums[1].name, "INACTIVE");
    assert_eq!(status_field.enums[1].value, 0);
    
    let priority_field = &table.fields[1];
    assert_eq!(priority_field.enums.len(), 3);
    assert_eq!(priority_field.enums[2].name, "HIGH");
    assert_eq!(priority_field.enums[2].value, 10);
}

#[test]
fn test_field_with_char_list() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            gender char("M", "F", "O")
            status ansichar("active", "inactive", "pending")
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let gender_field = &table.fields[0];
    assert_eq!(gender_field.value_list.len(), 3);
    assert_eq!(gender_field.value_list, vec!["M", "F", "O"]);
    
    let status_field = &table.fields[1];
    assert_eq!(status_field.value_list.len(), 3);
    assert_eq!(status_field.value_list, vec!["active", "inactive", "pending"]);
}

#[test]
fn test_field_with_lookup_type() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            related_id = (id)
            other_field = 
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let related_field = &table.fields[1];
    assert_eq!(related_field.field_type, rust_parser::FieldType::Lookup);
    assert_eq!(related_field.lookup_name, Some("id".to_string()));
    
    let other_field = &table.fields[2];
    assert_eq!(other_field.field_type, rust_parser::FieldType::Lookup);
    assert_eq!(other_field.lookup_name, None);
}

#[test]
fn test_field_sequence_types() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id identity
            big_id bigidentity
            seq_num sequence
            big_seq bigsequence
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    for field in &table.fields {
        assert!(field.is_sequence);
    }
    
    assert_eq!(table.fields[0].length, Some(4)); // identity
    assert_eq!(table.fields[1].length, Some(8)); // bigidentity
    assert_eq!(table.fields[2].length, Some(4)); // sequence
    assert_eq!(table.fields[3].length, Some(8)); // bigsequence
}

#[test]
fn test_field_default_lengths() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            blob_field blob
            bool_field boolean
            byte_field byte
            char_field char
            short_field short
            int_field int
            long_field long
            uid_field uid
            date_field date
            datetime_field datetime
            time_field time
            timestamp_field timestamp
            autotimestamp_field autotimestamp
            tlob_field tlob
            xml_field xml
            bigxml_field bigxml
            json_field json
            bigjson_field bigjson
            userstamp_field userstamp
            double_field double
            float_field float
            money_field money
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Check default lengths match JavaCC implementation
    assert_eq!(table.fields[0].length, Some(0));    // blob
    assert_eq!(table.fields[1].length, Some(1));    // boolean
    assert_eq!(table.fields[2].length, Some(1));    // byte
    assert_eq!(table.fields[3].length, Some(1));    // char
    assert_eq!(table.fields[4].length, Some(2));    // short
    assert_eq!(table.fields[5].length, Some(4));    // int
    assert_eq!(table.fields[6].length, Some(8));    // long
    assert_eq!(table.fields[7].length, Some(16));   // uid
    assert_eq!(table.fields[8].length, Some(8));    // date
    assert_eq!(table.fields[9].length, Some(14));   // datetime
    assert_eq!(table.fields[10].length, Some(6));   // time
    assert_eq!(table.fields[11].length, Some(14));  // timestamp
    assert_eq!(table.fields[12].length, Some(14));  // autotimestamp
    assert_eq!(table.fields[13].length, Some(0));   // tlob
    assert_eq!(table.fields[14].length, Some(1000)); // xml
    assert_eq!(table.fields[15].length, Some(10000)); // bigxml
    assert_eq!(table.fields[16].length, Some(1000)); // json
    assert_eq!(table.fields[17].length, Some(10000)); // bigjson
    assert_eq!(table.fields[18].length, Some(50));  // userstamp
    assert_eq!(table.fields[19].length, Some(8));   // double
    assert_eq!(table.fields[20].length, Some(8));   // float
    assert_eq!(table.fields[21].length, Some(8));   // money
}

#[test]
fn test_field_with_all_enhanced_features() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            status (user_status) byte(ACTIVE=1, INACTIVE=0) DEFAULTV "1" NOT NULL CHECK "status IN (0,1)" "User status field" "Required field"
            name char(100) "User name"
            price double(10,2) CALC "Computed price"
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    let status_field = &table.fields[0];
    assert_eq!(status_field.name, "status");
    assert_eq!(status_field.alias, Some("user_status".to_string()));
    assert_eq!(status_field.enums.len(), 2);
    assert_eq!(status_field.default_value, Some("1".to_string()));
    assert!(!status_field.is_null);
    assert_eq!(status_field.check_value, Some("status IN (0,1)".to_string()));
    assert_eq!(status_field.comments.len(), 2);
    
    let name_field = &table.fields[1];
    assert_eq!(name_field.length, Some(100));
    assert_eq!(name_field.comments.len(), 1);
    
    let price_field = &table.fields[2];
    assert_eq!(price_field.precision, Some(10));
    assert_eq!(price_field.scale, Some(2));
    assert!(price_field.is_calc);
}

#[test]
fn test_mixed_field_types_and_features() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Orders
            id bigidentity "Primary key"
            user_id (uid) long = (id) "Foreign key reference"
            status char(20) ("active", "pending", "completed") DEFAULTV "pending"
            amount money(10,2) NOT NULL
            created_at timestamp
            notes tlob(4096) "Order notes"
    "#;
    
    let result = parse_database(input);
    if let Err(e) = &result {
        println!("Error: {}", e);
    }
    assert!(result.is_ok());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.fields.len(), 6);
    
    // Check identity field
    let id_field = &table.fields[0];
    assert!(id_field.is_sequence);
    assert_eq!(id_field.comments.len(), 1);
    
    // Check lookup field with alias
    let user_id_field = &table.fields[1];
    assert_eq!(user_id_field.alias, Some("uid".to_string()));
    assert_eq!(user_id_field.field_type, rust_parser::FieldType::Lookup);
    assert_eq!(user_id_field.lookup_name, Some("id".to_string()));
    
    // Check char field with value list
    let status_field = &table.fields[2];
    assert_eq!(status_field.value_list.len(), 3);
    assert_eq!(status_field.default_value, Some("pending".to_string()));
    
    // Check money field with precision
    let amount_field = &table.fields[3];
    assert_eq!(amount_field.precision, Some(10));
    assert_eq!(amount_field.scale, Some(2));
    assert!(!amount_field.is_null);
    
    // Check tlob with size
    let notes_field = &table.fields[5];
    assert_eq!(notes_field.length, Some(4096));
} 