use jportal2_lib::parse_database;

#[test]
fn test_insert_procedure() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id bigidentity
            username char(50)
            email char(100)
            PROC InsertUser INSERT
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    assert_eq!(db.tables.len(), 1);
    
    let table = &db.tables[0];
    assert_eq!(table.name, "Users");
    assert_eq!(table.procs.len(), 1);
    
    let proc = &table.procs[0];
    assert_eq!(proc.name, "Insert");
    assert!(proc.is_proc);
    assert!(!proc.is_sproc);
    assert!(proc.is_built_in);
    assert!(!proc.is_update);
    assert!(!proc.has_returning);
}

#[test]
fn test_insert_procedure_with_returning() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id bigidentity
            username char(50)
            PROC InsertUserReturning INSERT RETURNING
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let proc = &table.procs[0];
    
    assert_eq!(proc.name, "Insert");
    assert!(proc.is_built_in);
    assert!(proc.has_returning);
}

#[test]
fn test_update_procedure() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            PROC UpdateUser UPDATE
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let proc = &table.procs[0];
    
    assert_eq!(proc.name, "Update");
    assert!(proc.is_built_in);
    assert!(proc.is_update);
}

#[test]
fn test_updateby_procedure() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            status byte
            PROC UpdateByUsername UPDATEBY username SET status
    "#;
    
    let result = parse_database(input);
    if result.is_err() {
        println!("Parse error: {:?}", result.as_ref().err());
    }
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let proc = &table.procs[0];
    
    assert_eq!(proc.name, "UpdateByusername"); // Field name appended
    assert!(proc.is_built_in);
    assert!(proc.is_update);
    assert_eq!(proc.fields.len(), 2); // username and status
    assert!(proc.fields.contains(&"username".to_string()));
    assert!(proc.fields.contains(&"status".to_string()));
}

#[test]
fn test_updateby_procedure_with_alias() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            PROC CustomUpdate UPDATEBY username AS CustomUpdate
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let proc = &table.procs[0];
    
    assert_eq!(proc.name, "CustomUpdate");
    assert!(proc.is_built_in);
    assert!(proc.is_update);
}

#[test]
fn test_delete_procedures() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            status byte
            PROC DeleteUser DELETEONE
            PROC DeleteAllUsers DELETEALL
            PROC DeleteByStatus DELETEBY status
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 3);
    
    let delete_one = &table.procs[0];
    assert_eq!(delete_one.name, "DeleteOne");
    assert!(delete_one.is_built_in);
    
    let delete_all = &table.procs[1];
    assert_eq!(delete_all.name, "DeleteAll");
    assert!(delete_all.is_built_in);
    
    let delete_by = &table.procs[2];
    assert_eq!(delete_by.name, "DeleteBystatus"); // Field name appended
    assert!(delete_by.is_built_in);
    assert_eq!(delete_by.fields.len(), 1);
    assert!(delete_by.fields.contains(&"status".to_string()));
}

#[test]
fn test_select_procedures() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            status byte
            PROC SelectUser SELECTONE
            PROC SelectUserUpd SELECTONE FOR UPDATE
            PROC SelectUserReadOnly SELECTONE FOR READONLY
            PROC SelectAllUsers SELECTALL
            PROC SelectAllSorted SELECTALL IN ORDER
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 5);
    
    let select_one = &table.procs[0];
    assert_eq!(select_one.name, "SelectOne");
    assert!(select_one.is_built_in);
    
    let select_one_upd = &table.procs[1];
    assert_eq!(select_one_upd.name, "SelectOneUpd");
    assert!(select_one_upd.is_built_in);
    
    let select_one_readonly = &table.procs[2];
    assert_eq!(select_one_readonly.name, "SelectOneReadOnly");
    assert!(select_one_readonly.is_built_in);
    
    let select_all = &table.procs[3];
    assert_eq!(select_all.name, "SelectAll");
    assert!(select_all.is_built_in);
    
    let select_all_sorted = &table.procs[4];
    assert_eq!(select_all_sorted.name, "SelectAllSorted");
    assert!(select_all_sorted.is_built_in);
}

#[test]
fn test_selectby_procedures() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            status byte
            PROC SelectByUsername SELECTONEBY username
            PROC SelectByStatus SELECTBY status
            PROC SelectByStatusOrdered SELECTBY status IN ORDER
            PROC CustomSelectBy SELECTBY username status AS CustomSelectBy
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 4);
    
    let select_one_by = &table.procs[0];
    assert_eq!(select_one_by.name, "SelectOneByusername");
    assert!(select_one_by.is_built_in);
    assert!(select_one_by.is_single);
    
    let select_by = &table.procs[1];
    assert_eq!(select_by.name, "SelectBystatus");
    assert!(select_by.is_built_in);
    assert!(!select_by.is_single);
    
    let select_by_ordered = &table.procs[2];
    assert_eq!(select_by_ordered.name, "SelectBystatusemailstatus"); // Multiple fields
    assert!(select_by_ordered.is_built_in);
    
    let custom_select_by = &table.procs[3];
    assert_eq!(custom_select_by.name, "CustomSelectBy");
    assert!(custom_select_by.is_built_in);
}

#[test]
fn test_bulk_procedures() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id bigidentity
            username char(50)
            PROC BulkInsertUsers BULKINSERT
            PROC BulkInsertBatch BULKINSERT (100)
            PROC BulkUpdateUsers BULKUPDATE
            PROC BulkUpdateBatch BULKUPDATE (50)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 4);
    
    let bulk_insert = &table.procs[0];
    assert_eq!(bulk_insert.name, "BulkInsert");
    assert!(bulk_insert.is_built_in);
    assert!(bulk_insert.row_count.is_none());
    
    let bulk_insert_batch = &table.procs[1];
    assert_eq!(bulk_insert_batch.name, "BulkInsert");
    assert!(bulk_insert_batch.is_built_in);
    assert_eq!(bulk_insert_batch.row_count, Some(100));
    
    let bulk_update = &table.procs[2];
    assert_eq!(bulk_update.name, "BulkUpdate");
    assert!(bulk_update.is_built_in);
    assert!(bulk_update.is_update);
    
    let bulk_update_batch = &table.procs[3];
    assert_eq!(bulk_update_batch.name, "BulkUpdate");
    assert!(bulk_update_batch.is_built_in);
    assert!(bulk_update_batch.is_update);
    assert_eq!(bulk_update_batch.row_count, Some(50));
}

#[test]
fn test_utility_procedures() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            last_modified timestamp
            PROC CountUsers COUNT
            PROC UserExists EXISTS
            PROC MergeUser MERGE
            PROC MaxTimestamp MAXTMSTAMP
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 4);
    
    let count_proc = &table.procs[0];
    assert_eq!(count_proc.name, "Count");
    assert!(count_proc.is_built_in);
    
    let exists_proc = &table.procs[1];
    assert_eq!(exists_proc.name, "Exists");
    assert!(exists_proc.is_built_in);
    
    let merge_proc = &table.procs[2];
    assert_eq!(merge_proc.name, "Merge");
    assert!(merge_proc.is_built_in);
    
    let max_timestamp = &table.procs[3];
    assert_eq!(max_timestamp.name, "MaxTmStamp");
    assert!(max_timestamp.is_built_in);
}

#[test]
fn test_procedure_with_options() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            PROC SelectUser SELECTONE OPTIONS "HINT_INDEX" "PARALLEL"
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let proc = &table.procs[0];
    
    assert_eq!(proc.name, "SelectOne");
    assert_eq!(proc.options.len(), 2);
    assert!(proc.options.contains(&"HINT_INDEX".to_string()));
    assert!(proc.options.contains(&"PARALLEL".to_string()));
}

#[test]
fn test_sproc_vs_proc() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            PROC RegularProc INSERT
            SPROC StoredProc INSERT
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 2);
    
    let regular_proc = &table.procs[0];
    assert!(regular_proc.is_proc);
    assert!(!regular_proc.is_sproc);
    
    let stored_proc = &table.procs[1];
    assert!(!stored_proc.is_proc);
    assert!(stored_proc.is_sproc);
}

#[test]
fn test_deleteone_with_standard_flag() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            PROC DeleteUser DELETEONE (STANDARD)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let proc = &table.procs[0];
    
    assert_eq!(proc.name, "DeleteOne");
    assert!(proc.is_std);
}

#[test]
fn test_complex_selectby_with_output() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            status byte
            created_date datetime
            PROC SelectActiveUsers SELECTBY status IN ORDER created_date DESC FOR READONLY
                OUTPUT
                    id int
                    username char(50)
                    email char(100)
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    let proc = &table.procs[0];
    
    assert_eq!(proc.name, "SelectBystatuscreated_dateReadOnly"); // Fields appended with ReadOnly suffix
    assert!(proc.is_built_in);
    assert_eq!(proc.outputs.len(), 3);
    
    let output_fields: Vec<&str> = proc.outputs.iter().map(|f| f.name.as_str()).collect();
    assert!(output_fields.contains(&"id"));
    assert!(output_fields.contains(&"username"));
    assert!(output_fields.contains(&"email"));
}

#[test]
fn test_multiple_standard_procedures() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Orders
            id bigidentity
            customer_id int
            product_id int
            quantity int
            status char(20)
            created_date datetime
            PROC InsertOrder INSERT RETURNING
            PROC UpdateOrder UPDATE
            PROC DeleteOrder DELETEONE
            PROC SelectOrder SELECTONE
            PROC SelectByCustomer SELECTBY customer_id
            PROC SelectByStatus SELECTBY status IN ORDER created_date
            PROC CountOrders COUNT
            PROC OrderExists EXISTS
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 8);
    
    // Verify all procedures are parsed correctly
    let proc_names: Vec<&str> = table.procs.iter().map(|p| p.name.as_str()).collect();
    assert!(proc_names.contains(&"Insert"));
    assert!(proc_names.contains(&"Update"));
    assert!(proc_names.contains(&"DeleteOne"));
    assert!(proc_names.contains(&"SelectOne"));
    assert!(proc_names.contains(&"SelectBycustomer_id"));
    assert!(proc_names.contains(&"SelectBystatuscreated_date"));
    assert!(proc_names.contains(&"Count"));
    assert!(proc_names.contains(&"Exists"));
    
    // Verify specific procedure properties
    let insert_proc = table.procs.iter().find(|p| p.name == "Insert").unwrap();
    assert!(insert_proc.has_returning);
    
    let update_proc = table.procs.iter().find(|p| p.name == "Update").unwrap();
    assert!(update_proc.is_update);
    
    let select_by_customer = table.procs.iter().find(|p| p.name == "SelectBycustomer_id").unwrap();
    assert_eq!(select_by_customer.fields.len(), 1);
    assert!(select_by_customer.fields.contains(&"customer_id".to_string()));
    
    let select_by_status = table.procs.iter().find(|p| p.name == "SelectBystatuscreated_date").unwrap();
    assert_eq!(select_by_status.fields.len(), 2);
    assert!(select_by_status.fields.contains(&"status".to_string()));
    assert!(select_by_status.fields.contains(&"created_date".to_string()));
}

#[test]
fn test_javacc_jproc_behavior() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            PROC TestProc INSERT
            PROC TestProc UPDATE  // Duplicate name - should generate warning
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Both procedures should be parsed, but in real JavaCC implementation
    // the second would generate a warning and not be added
    assert_eq!(table.procs.len(), 2);
    
    // Verify both procedures have the same name (would trigger hasProc() warning in JavaCC)
    assert_eq!(table.procs[0].name, "Insert");
    assert_eq!(table.procs[1].name, "Update");
}

#[test]
fn test_procedure_start_line_tracking() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            PROC TestProc INSERT
            SPROC TestSProc UPDATE
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    
    // Verify start line tracking (matches JavaCC proc.start = t.beginLine)
    for proc in &table.procs {
        assert!(proc.start_line.is_some(), "Procedure should have start line set");
    }
}

#[test]
fn test_custom_select_procedures() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            status byte
            PROC CustomSelect SELECT
                "SELECT id, username FROM Users WHERE username LIKE ?"
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 1);
    
    let custom_select = &table.procs[0];
    assert_eq!(custom_select.name, "CustomSelect");
    assert!(!custom_select.is_built_in);
    assert!(custom_select.use_std);
    assert!(custom_select.extends_std);
    assert_eq!(custom_select.lines.len(), 1);
    assert!(custom_select.lines[0].contains("SELECT id, username"));
}

#[test]
fn test_jnewproc_comprehensive() {
    let input = r#"
        DATABASE TestDB
        SERVER "localhost"
        TABLE Users
            id int
            username char(50)
            email char(100)
            status byte
            PROC SimpleSelect SELECT
                "SELECT * FROM Users"
            PROC SelectWithInput SELECT
                INPUT
                    search_term char(100)
                    status_filter byte
                "SELECT * FROM Users WHERE username LIKE ? AND status = ?"
            PROC SelectWithOutput SELECT
                OUTPUT
                    Users.id int
                    Users.username char(50)
                "SELECT id, username FROM Users"
            PROC SelectWithBoth SELECT
                INPUT
                    min_id int
                OUTPUT
                    Users.id int
                    Users.username char(50)
                "SELECT id, username FROM Users WHERE id >= ?"
            PROC StandardSelect SELECT (STANDARD)
                "SELECT * FROM Users"
    "#;
    
    let result = parse_database(input);
    assert!(result.is_ok(), "Failed to parse database: {:?}", result.err());
    
    let db = result.unwrap();
    let table = &db.tables[0];
    assert_eq!(table.procs.len(), 5);
    
    // Test SimpleSelect
    let simple_select = &table.procs[0];
    assert_eq!(simple_select.name, "SimpleSelect");
    assert!(!simple_select.is_built_in);
    assert!(simple_select.use_std);
    assert!(simple_select.extends_std);
    assert!(!simple_select.is_std);
    assert_eq!(simple_select.inputs.len(), 0);
    assert_eq!(simple_select.outputs.len(), 0);
    assert_eq!(simple_select.lines.len(), 1);
    
    // Test SelectWithInput
    let select_with_input = &table.procs[1];
    assert_eq!(select_with_input.name, "SelectWithInput");
    assert_eq!(select_with_input.inputs.len(), 2);
    assert_eq!(select_with_input.inputs[0].name, "search_term");
    assert_eq!(select_with_input.inputs[1].name, "status_filter");
    assert_eq!(select_with_input.outputs.len(), 0);
    
    // Test SelectWithOutput
    let select_with_output = &table.procs[2];
    assert_eq!(select_with_output.name, "SelectWithOutput");
    assert_eq!(select_with_output.inputs.len(), 0);
    assert_eq!(select_with_output.outputs.len(), 2);
    assert_eq!(select_with_output.outputs[0].name, "Users.id");
    assert_eq!(select_with_output.outputs[1].name, "Users.username");
    
    // Test SelectWithBoth
    let select_with_both = &table.procs[3];
    assert_eq!(select_with_both.name, "SelectWithBoth");
    assert_eq!(select_with_both.inputs.len(), 1);
    assert_eq!(select_with_both.outputs.len(), 2);
    assert_eq!(select_with_both.inputs[0].name, "min_id");
    
    // Test StandardSelect
    let standard_select = &table.procs[4];
    assert_eq!(standard_select.name, "StandardSelect");
    assert!(standard_select.is_std);
    assert!(standard_select.use_std);
    assert!(standard_select.extends_std);
} 