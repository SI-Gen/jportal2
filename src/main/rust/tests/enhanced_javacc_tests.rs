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

#[cfg(test)]
mod enhanced_javacc_tests {
    use super::*;

    #[test]
    fn test_new_data_block_parsing() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                PROC SQLDATA
                INSERT INTO Users VALUES (1, 'test')
                INSERT INTO Users VALUES (2, 'test2')
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse new data block: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        assert_eq!(table.procs.len(), 1);
        
        let proc = &table.procs[0];
        assert!(proc.is_data);
        assert!(!proc.is_idl_code);
        assert_eq!(proc.lines.len(), 2);
    }

    #[test]
    fn test_idl_code_block_parsing() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                PROC IDLCODE
                interface UserService {
                    void createUser(in User user);
                }
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse IDL code block: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        assert_eq!(table.procs.len(), 1);
        
        let proc = &table.procs[0];
        assert!(proc.is_idl_code);
        assert!(!proc.is_data);
        assert_eq!(proc.lines.len(), 3);
    }

    #[test]
    fn test_enhanced_input_type_parsing() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT (MULTIPLE)
                        search_term char(100)
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users WHERE username LIKE ?
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse enhanced input type: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(proc.is_multiple_input);
        assert_eq!(proc.inputs.len(), 1);
        assert_eq!(proc.inputs[0].name, "search_term");
    }

    #[test]
    fn test_enhanced_output_type_parsing() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT
                        search_term char(100)
                    OUTPUT (SINGLE)
                        id int
                        username char(50)
                    CODE
                    SELECT id, username FROM Users WHERE username = ?
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse enhanced output type: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(proc.is_single);
        assert_eq!(proc.outputs.len(), 2);
    }

    #[test]
    fn test_dynamic_sql_parsing() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT
                        search_term char(100)
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users WHERE username LIKE &search_term
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse dynamic SQL: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert_eq!(proc.dynamics.len(), 1);
        assert_eq!(proc.dynamics[0], "search_term");
        assert_eq!(proc.dynamic_sizes[0], 256); // Default size
        assert_eq!(proc.dynamic_strung[0], false);
    }

    #[test]
    fn test_dynamic_sql_with_size() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT
                        search_term char(100)
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users WHERE username LIKE &search_term(100)
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse dynamic SQL with size: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert_eq!(proc.dynamics.len(), 1);
        assert_eq!(proc.dynamics[0], "search_term");
        assert_eq!(proc.dynamic_sizes[0], 100);
    }

    #[test]
    fn test_dynamic_sql_strung() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT
                        search_term char(100)
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users WHERE username LIKE &'search_term'
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse strung dynamic SQL: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert_eq!(proc.dynamics.len(), 1);
        assert_eq!(proc.dynamics[0], "search_term");
        assert_eq!(proc.dynamic_strung[0], true);
    }

    #[test]
    fn test_old_code_with_dynamic_identifiers() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc
                    SQL
                    CODE
                        SELECT * FROM Users WHERE id = &search_term(100)
                        ORDER BY id
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse old code with dynamic identifiers: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(proc.is_sql);
        assert_eq!(proc.dynamics.len(), 1);
        assert_eq!(proc.dynamics[0], "search_term");
        assert_eq!(proc.dynamic_sizes[0], 100);
    }

    #[test]
    fn test_user_proc_with_inout() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc
                    INOUT (SINGLE)
                        user_id int
                        username char(50)
                    CODE
                        UPDATE Users SET username = ? WHERE id = ?
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse user proc with INOUT: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(!proc.is_built_in);
        assert!(proc.is_single);
        assert_eq!(proc.inputs.len(), 2);
        assert_eq!(proc.inputs[0].name, "user_id");
        assert_eq!(proc.inputs[1].name, "username");
        assert!(proc.inputs[0].is_in);
        assert!(proc.inputs[1].is_in);
    }

    #[test]
    fn test_proc_with_standard_extension() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc (STANDARD)
                    INPUT
                        search_term char(100)
                    CODE
                        SELECT * FROM Users WHERE username LIKE ?
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse proc with STANDARD extension: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(proc.extends_std);
        assert!(proc.use_std);
        assert!(proc.is_std);
        assert!(!proc.is_built_in);
    }

    #[test]
    fn test_multiple_dynamic_variables() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                email char(100)
                PROC TestProc SELECT
                    INPUT
                        search_term char(100)
                        email_filter char(100)
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users WHERE username LIKE &search_term AND email LIKE &email_filter
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse multiple dynamic variables: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert_eq!(proc.dynamics.len(), 2);
        assert!(proc.dynamics.contains(&"search_term".to_string()));
        assert!(proc.dynamics.contains(&"email_filter".to_string()));
    }

    #[test]
    fn test_proc_helper_methods() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT
                        search_term char(100)
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users WHERE username LIKE &search_term
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse for helper method tests: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        // Test helper methods
        assert!(proc.has_input("search_term"));
        assert!(!proc.has_input("nonexistent"));
        assert!(proc.has_output("id"));
        assert!(!proc.has_output("nonexistent"));
        assert!(proc.has_dynamic("search_term"));
        assert!(!proc.has_dynamic("nonexistent"));
    }

    #[test]
    fn test_complex_data_and_code_blocks() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC SQLDATA
                INSERT INTO Users (id, username) VALUES (1, 'admin')
                INSERT INTO Users (id, username) VALUES (2, 'user')
                PROC IDLCODE
                interface UserService {
                    User getUser(in long id);
                    void updateUser(in User user);
                }
                PROC TestSelect SELECT
                    INPUT
                        user_id int
                    OUTPUT
                        username char(50)
                    CODE
                    SELECT username FROM Users WHERE id = &user_id
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse complex data and code blocks: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        assert_eq!(table.procs.len(), 3);
        
        // Check data proc
        let data_proc = &table.procs[0];
        assert!(data_proc.is_data);
        assert_eq!(data_proc.lines.len(), 2);
        
        // Check IDL proc
        let idl_proc = &table.procs[1];
        assert!(idl_proc.is_idl_code);
        assert!(idl_proc.lines.len() > 0);
        
        // Check SELECT proc
        let select_proc = &table.procs[2];
        assert!(!select_proc.is_built_in);
        assert_eq!(select_proc.inputs.len(), 1);
        assert_eq!(select_proc.outputs.len(), 1);
        assert_eq!(select_proc.dynamics.len(), 1);
    }

    #[test]
    fn test_debug_simple_proc_field() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                PROC TestProc SELECT
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        if result.is_err() {
            println!("Parse error: {:?}", result.as_ref().err());
        }
        assert!(result.is_ok(), "Failed to parse simple proc field: {:?}", result.err());
    }

    #[test]
    fn test_debug_simple_proc_no_output() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                PROC TestProc SELECT
                    CODE
                    SELECT id FROM Users
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        if result.is_err() {
            println!("Parse error: {:?}", result.as_ref().err());
        }
        assert!(result.is_ok(), "Failed to parse simple proc without output: {:?}", result.err());
    }

    #[test]
    fn test_javacc_input_type_standard() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                email char(100)
                PROC TestProc SELECT
                    INPUT (STANDARD)
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse INPUT (STANDARD): {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        // With STANDARD, all table fields should be added as inputs
        assert!(proc.extends_std);
        assert!(proc.use_std);
        // Note: The actual field addition happens in the JavaCC-style functions
        // which need table context that we don't have in the current parsing structure
    }

    #[test]
    fn test_javacc_input_type_multiple() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT (MULTIPLE)
                        search_term char(100)
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users WHERE username LIKE ?
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse INPUT (MULTIPLE): {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(proc.is_multiple_input);
        assert_eq!(proc.inputs.len(), 1);
        assert_eq!(proc.inputs[0].name, "search_term");
    }

    #[test]
    fn test_javacc_input_type_with_number() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT (5)
                        search_term char(100)
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users WHERE username LIKE ?
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse INPUT (5): {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(proc.is_multiple_input);
        assert_eq!(proc.no_rows, Some(5));
        assert_eq!(proc.inputs.len(), 1);
    }

    #[test]
    fn test_javacc_output_type_single() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT
                        search_term char(100)
                    OUTPUT (SINGLE)
                        id int
                        username char(50)
                    CODE
                    SELECT id, username FROM Users WHERE username = ?
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse OUTPUT (SINGLE): {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(proc.is_single);
        assert_eq!(proc.outputs.len(), 2);
    }

    #[test]
    fn test_javacc_output_type_single_standard() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                email char(100)
                PROC TestProc SELECT
                    INPUT
                        search_term char(100)
                    OUTPUT (SINGLE STANDARD)
                    CODE
                    SELECT * FROM Users WHERE username = ?
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse OUTPUT (SINGLE STANDARD): {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(proc.is_single);
        assert!(proc.extends_std);
        assert!(proc.use_std);
        // Note: The actual field addition happens in the JavaCC-style functions
    }

    #[test]
    fn test_javacc_output_type_single_update() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT
                        user_id int
                    OUTPUT (SINGLE UPDATE)
                        username char(50)
                    CODE
                    SELECT username FROM Users WHERE id = ? FOR UPDATE
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse OUTPUT (SINGLE UPDATE): {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(proc.is_single);
        assert!(proc.has_updates);
        assert_eq!(proc.outputs.len(), 1);
    }

    #[test]
    fn test_javacc_output_type_with_number() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT
                        search_term char(100)
                    OUTPUT (10)
                        id int
                        username char(50)
                    CODE
                    SELECT id, username FROM Users WHERE username LIKE ?
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse OUTPUT (10): {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert_eq!(proc.no_rows, Some(10));
        assert_eq!(proc.outputs.len(), 2);
    }

    #[test]
    fn test_javacc_output_type_standard_single() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                email char(100)
                PROC TestProc SELECT
                    INPUT
                        search_term char(100)
                    OUTPUT (STANDARD SINGLE)
                    CODE
                    SELECT * FROM Users WHERE username = ?
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse OUTPUT (STANDARD SINGLE): {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert!(proc.is_single);
        assert!(proc.extends_std);
        assert!(proc.use_std);
        // Note: The actual field addition happens in the JavaCC-style functions
    }

    #[test]
    fn test_simple_old_code_debug() {
        let input = r#"DATABASE TestDB
SERVER "localhost"
TABLE Users
id int
PROC TestProc
CODE
SELECT * FROM Users WHERE id = &search_term(100)
ENDCODE"#;
        
        let result = parse_database(input);
        assert!(result.is_ok(), "Failed to parse simple old code: {:?}", result.err());
        
        let db = result.unwrap();
        let table = &db.tables[0];
        let proc = &table.procs[0];
        
        assert_eq!(proc.name, "TestProc");
        assert!(!proc.lines.is_empty());
        assert!(!proc.dynamics.is_empty());
    }

    #[test]
    fn test_debug_minimal_output_field() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                PROC TestProc SELECT
                    OUTPUT
                        id int
                    CODE
                    SELECT id FROM Users
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        if result.is_err() {
            println!("Minimal output field parse error: {:?}", result.as_ref().err());
        }
        assert!(result.is_ok(), "Failed to parse minimal output field: {:?}", result.err());
    }

    #[test]
    fn test_debug_input_standard_only() {
        let input = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
                username char(50)
                PROC TestProc SELECT
                    INPUT (STANDARD)
                    CODE
                    SELECT * FROM Users
                    ENDCODE
        "#;
        
        let result = parse_database(input);
        if result.is_err() {
            println!("INPUT (STANDARD) only parse error: {:?}", result.as_ref().err());
        }
        assert!(result.is_ok(), "Failed to parse INPUT (STANDARD) only: {:?}", result.err());
    }
} 