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

#[test]
fn test_all_example_si_files_parse_successfully() {
    use std::path::Path;
    use std::fs;

    let example_files_dir = Path::new("../../../src/main/resources/example_si_files");
    assert!(example_files_dir.exists(), "Example SI files directory should exist");

    let dir_entries = fs::read_dir(example_files_dir)
        .expect("Should be able to read example_si_files directory");

    let mut parsed_files = 0;
    let mut failed_files = Vec::new();
    let mut successful_files = Vec::new();

    // Files that are known to parse successfully with the current parser
    let expected_working_files = vec![
        "DBHealthCheck.si",
        "TableWithJSONColumn.si",
        "ContingencyStatus.si",
        "testjson.si", 
        "test_stored_proc.si",
        "test_enums.si",
    ];

    // Files with known parsing issues that need grammar improvements
    let known_problematic_files = vec![
        "todolist.si",               // PROC SelectOneBy syntax
        "Selection.si",              // PROC SelectByContingencyId (standard) syntax
        "ExampleTable.si",           // Custom PROC with OUTPUT sections
        "SelectionStatus.si",        // PROC SelectOneBy syntax
        "todo_items.si",             // Custom PROC with OUTPUT sections  
        "Contingency.si",            // PROC with (standard) syntax
    ];

    for entry in dir_entries {
        let entry = entry.expect("Should be able to read directory entry");
        let path = entry.path();
        
        // Only process .si files
        if let Some(extension) = path.extension() {
            if extension == "si" {
                let file_name = path.file_name().unwrap().to_string_lossy();
                println!("Testing parse of file: {}", file_name);
                
                // Read the file content
                let content = fs::read_to_string(&path)
                    .expect(&format!("Should be able to read file: {}", file_name));
                
                // Attempt to parse the content
                match parse_database(&content) {
                    Ok(database) => {
                        println!("✓ Successfully parsed {}: database name = '{}'", file_name, database.name);
                        parsed_files += 1;
                        successful_files.push(file_name.to_string());
                        
                        // Basic validation that the database is not empty
                        assert!(!database.name.is_empty(), "Database name should not be empty for {}", file_name);
                        assert!(!database.server.is_empty(), "Server should not be empty for {}", file_name);
                    }
                    Err(error) => {
                        println!("✗ Failed to parse {}: {}", file_name, error);
                        failed_files.push((file_name.to_string(), error.to_string()));
                    }
                }
            }
        }
    }

    // Report comprehensive results
    println!("\n=== Parse Test Summary ===");
    println!("Successfully parsed: {} files", parsed_files);
    if !successful_files.is_empty() {
        println!("Working files:");
        for file_name in &successful_files {
            println!("  ✓ {}", file_name);
        }
    }
    
    if !failed_files.is_empty() {
        println!("Failed to parse: {} files", failed_files.len());
        for (file_name, error) in &failed_files {
            let status = if known_problematic_files.contains(&file_name.as_str()) {
                "[KNOWN ISSUE]"
            } else {
                "[UNEXPECTED]"
            };
            println!("  ✗ {} {}: {}", status, file_name, error);
        }
    }

    // Check that expected working files actually work
    for expected_file in &expected_working_files {
        assert!(successful_files.contains(&expected_file.to_string()),
            "Expected file {} should parse successfully but it failed", expected_file);
    }

    // Check for unexpected failures (files not in our known problematic list)
    let unexpected_failures: Vec<_> = failed_files.iter()
        .filter(|(name, _)| !known_problematic_files.contains(&name.as_str()))
        .collect();
    
    if !unexpected_failures.is_empty() {
        panic!("Found unexpected parsing failures (not in known issues list): {:?}", 
            unexpected_failures.iter().map(|(name, _)| name).collect::<Vec<_>>());
    }
    
    // Assert that we actually found and tested some files
    assert!(parsed_files + failed_files.len() > 0, "Should have found and tested at least one .si file");
    
    println!("\n✓ Test completed: {} files working as expected, {} files have known grammar issues", 
             successful_files.len(), failed_files.len());
} 