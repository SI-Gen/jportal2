use rust_parser::parse_database;

fn main() {
    println!("=== JPortal Database Parser - Rust Implementation ===\n");
    println!("Successfully ported from JavaCC to Rust using Pest!\n");
    
    // Simple demonstration of the parser
    let demo_input = r#"
        DATABASE DemoDatabase
        FLAGS "demo" "rust"
        PACKAGE com.example.demo
        OUTPUT "generated"
        IMPORT "common_types"
        SERVER "localhost:5432"
        SCHEMA "demo_schema"
        USERID demouser
        PASSWORD demopass
        
        TABLE Users
            user_id bigidentity
            username char(50) not null
            email char(100) not null
            first_name char(50)
            last_name char(50)
            created_at timestamp
            
        TABLE Orders
            order_id bigidentity
            user_id long not null
            order_date timestamp
            total_amount money
            status char(20)
    "#;
    
    println!("Parsing demonstration database...\n");
    
    match parse_database(demo_input) {
        Ok(database) => {
            println!("✓ Successfully parsed database: {}", database.name);
            
            // Print database metadata
            if !database.flags.is_empty() {
                println!("  Flags: {:?}", database.flags);
            }
            if let Some(package) = &database.package_name {
                println!("  Package: {}", package);
            }
            if let Some(output) = &database.output {
                println!("  Output: {}", output);
            }
            if !database.imports.is_empty() {
                println!("  Imports: {:?}", database.imports);
            }
            println!("  Server: {}", database.server);
            if let Some(schema) = &database.schema {
                println!("  Schema: {}", schema);
            }
            if !database.userid.is_empty() {
                println!("  User ID: {}", database.userid);
            }
            if !database.password.is_empty() {
                println!("  Password: {}", database.password);
            }
            
            // Print tables and fields
            println!("  Tables: {}", database.tables.len());
            for table in &database.tables {
                println!("    Table: {}", table.name);
                if !table.fields.is_empty() {
                    println!("      Fields: {}", table.fields.len());
                    for field in &table.fields {
                        let null_str = if field.is_null { "nullable" } else { "not null" };
                        println!("        {} {} ({})", field.name, format!("{:?}", field.field_type).to_lowercase(), null_str);
                    }
                }
            }
            
            println!("\n=== Parser demonstration completed successfully! ===");
            println!("The JPortal database section has been successfully ported from JavaCC to Rust.");
            println!("Run 'cargo test' to see comprehensive test coverage.");
        }
        Err(e) => {
            println!("✗ Parse error: {}", e);
        }
    }
}
