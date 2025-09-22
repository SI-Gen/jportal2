use std::collections::HashMap;
use std::fs;
use std::path::{Path, PathBuf};
use tera::{Tera, Context, Value};
use serde_json::json;
use log::{info, warn};

use crate::{Database, Table, Proc};

pub struct TeraTemplateGenerator {
    tera: Tera,
    template_base_dir: PathBuf,
    output_dir: PathBuf,
}

impl TeraTemplateGenerator {
    pub fn new(template_base_dir: PathBuf, output_dir: PathBuf) -> Result<Self, Box<dyn std::error::Error>> {
        // Create Tera instance with glob pattern for all .tera files
        let template_pattern = format!("{}/**/*.tera", template_base_dir.display());
        let mut tera = match Tera::new(&template_pattern) {
            Ok(t) => t,
            Err(e) => {
                warn!("Failed to load templates from {}: {}", template_pattern, e);
                // Create empty Tera instance if no templates found
                Tera::new("templates/**/*.tera").unwrap_or_else(|_| Tera::new("").unwrap())
            }
        };

        // Register custom filters that match FreeMarker functionality
        tera.register_filter("camelCase", camel_case_filter);
        tera.register_filter("pascalCase", pascal_case_filter);
        tera.register_filter("snake_case", snake_case_filter);
        tera.register_filter("upper", upper_filter);
        tera.register_filter("lower", lower_filter);
        tera.register_filter("sqlType", sql_type_filter);
        tera.register_filter("rust_type", rust_type_filter);
        tera.register_filter("rust_default", rust_default_filter);
        tera.register_filter("postgres_type", postgres_type_filter);
        tera.register_filter("regex_search", regex_search_filter);

        Ok(TeraTemplateGenerator {
            tera,
            template_base_dir,
            output_dir,
        })
    }

    /// Generate templates for a specific generator (matches FreeMarker.generateTemplate)
    pub fn generate_template(
        &mut self,
        database: &Database,
        table: &Table,
        generator_name: &str,
    ) -> Result<Vec<String>, Box<dyn std::error::Error>> {
        let mut generated_files = Vec::new();
        
        // Find all .tera files for this generator
        let generator_path = self.template_base_dir.join(generator_name);
        if !generator_path.exists() {
            warn!("Generator directory does not exist: {}", generator_path.display());
            return Ok(generated_files);
        }

        // Check if table has procedures (matches FreeMarker logic)
        if table.procs.is_empty() {
            warn!("[{}]: Table: [{}] Has no procs defined. Skipping...", generator_name, table.name);
            return Ok(generated_files);
        }

        // Walk through all .tera files in the generator directory
        let template_files = self.find_template_files(&generator_path)?;
        
        for template_file in template_files {
            let relative_path = template_file.strip_prefix(&generator_path)?;
            let template_name = format!("{}/{}", generator_name, relative_path.display());
            
            // Generate file for each procedure (matches FreeMarker logic)
            let mut done_files = std::collections::HashSet::new();
            
            for proc in &table.procs {
                let generated_file = self.generate_single_template(
                    database,
                    table,
                    proc,
                    &template_name,
                    generator_name,
                    &relative_path,
                    &mut done_files,
                )?;
                
                if let Some(file) = generated_file {
                    generated_files.push(file);
                }
            }
        }

        Ok(generated_files)
    }

    /// Generate a single template file (matches FreeMarker.GenerateSingleFTLFile)
    fn generate_single_template(
        &mut self,
        database: &Database,
        table: &Table,
        proc: &Proc,
        template_name: &str,
        generator_name: &str,
        relative_path: &Path,
        done_files: &mut std::collections::HashSet<String>,
    ) -> Result<Option<String>, Box<dyn std::error::Error>> {
        // Create context with all the data (matches FreeMarker root map)
        let mut context = Context::new();
        context.insert("database", database);
        context.insert("table", table);
        context.insert("proc", proc);
        
        // Add static constants and enums (matches FreeMarker STATICS and ENUMS)
        context.insert("STATICS", &create_statics_map());
        context.insert("ENUMS", &create_enums_map());

        // Determine output file name (remove .tera extension)
        let mut dest_file_name = relative_path.to_string_lossy().to_string();
        if dest_file_name.ends_with(".tera") {
            dest_file_name = dest_file_name[..dest_file_name.len() - 5].to_string();
        }

        // Process filename as template (matches FreeMarker filename template processing)
        let processed_filename = self.tera.render_str(&dest_file_name, &context)?;
        
        // Skip if already generated
        if done_files.contains(&processed_filename) {
            return Ok(None);
        }
        done_files.insert(processed_filename.clone());

        // Create full destination path
        let full_dest_path = self.output_dir.join(&processed_filename);
        
        // Create parent directories
        if let Some(parent) = full_dest_path.parent() {
            fs::create_dir_all(parent)?;
        }

        // Render template
        let rendered_content = self.tera.render(template_name, &context)?;
        
        // Write to file
        fs::write(&full_dest_path, rendered_content)?;
        
        info!("[{}]: Generating [{}]", generator_name, full_dest_path.display());
        
        Ok(Some(full_dest_path.to_string_lossy().to_string()))
    }

    /// Find all .tera template files in a directory
    fn find_template_files(&self, dir: &Path) -> Result<Vec<PathBuf>, Box<dyn std::error::Error>> {
        let mut template_files = Vec::new();
        
        if dir.is_dir() {
            for entry in fs::read_dir(dir)? {
                let entry = entry?;
                let path = entry.path();
                
                if path.is_dir() {
                    // Recursively search subdirectories
                    template_files.extend(self.find_template_files(&path)?);
                } else if path.extension().and_then(|s| s.to_str()) == Some("tera") {
                    template_files.push(path);
                }
            }
        }
        
        Ok(template_files)
    }
}

/// Create static constants map (matches FreeMarker STATICS)
fn create_statics_map() -> HashMap<String, Value> {
    let mut statics = HashMap::new();
    
    // Add Field type constants
    let mut field_statics = HashMap::new();
    field_statics.insert("BLOB", json!("BLOB"));
    field_statics.insert("BOOLEAN", json!("BOOLEAN"));
    field_statics.insert("BYTE", json!("BYTE"));
    field_statics.insert("CHAR", json!("CHAR"));
    field_statics.insert("INT", json!("INT"));
    field_statics.insert("LONG", json!("LONG"));
    field_statics.insert("DATE", json!("DATE"));
    field_statics.insert("DATETIME", json!("DATETIME"));
    field_statics.insert("TIMESTAMP", json!("TIMESTAMP"));
    
    statics.insert("Field".to_string(), json!(field_statics));
    statics
}

/// Create enums map (matches FreeMarker ENUMS)
fn create_enums_map() -> HashMap<String, Value> {
    let mut enums = HashMap::new();
    
    // Add FieldType enum values
    let mut field_types = HashMap::new();
    field_types.insert("Blob", json!("Blob"));
    field_types.insert("Boolean", json!("Boolean"));
    field_types.insert("Byte", json!("Byte"));
    field_types.insert("Char", json!("Char"));
    field_types.insert("Int", json!("Int"));
    field_types.insert("Long", json!("Long"));
    field_types.insert("Date", json!("Date"));
    field_types.insert("DateTime", json!("DateTime"));
    field_types.insert("Timestamp", json!("Timestamp"));
    
    enums.insert("Field".to_string(), json!(field_types));
    enums
}

// Custom filters that match common FreeMarker functionality

fn camel_case_filter(value: &Value, _: &HashMap<String, Value>) -> tera::Result<Value> {
    let s = value.as_str().unwrap_or("");
    let camel_case = to_camel_case(s);
    Ok(Value::String(camel_case))
}

fn pascal_case_filter(value: &Value, _: &HashMap<String, Value>) -> tera::Result<Value> {
    let s = value.as_str().unwrap_or("");
    let pascal_case = to_pascal_case(s);
    Ok(Value::String(pascal_case))
}

fn snake_case_filter(value: &Value, _: &HashMap<String, Value>) -> tera::Result<Value> {
    let s = value.as_str().unwrap_or("");
    let snake_case = to_snake_case(s);
    Ok(Value::String(snake_case))
}

fn upper_filter(value: &Value, _: &HashMap<String, Value>) -> tera::Result<Value> {
    let s = value.as_str().unwrap_or("");
    Ok(Value::String(s.to_uppercase()))
}

fn lower_filter(value: &Value, _: &HashMap<String, Value>) -> tera::Result<Value> {
    let s = value.as_str().unwrap_or("");
    Ok(Value::String(s.to_lowercase()))
}

fn sql_type_filter(value: &Value, _: &HashMap<String, Value>) -> tera::Result<Value> {
    // Convert FieldType to SQL type string
    let field_type_str = value.as_str().unwrap_or("");
    let sql_type = match field_type_str {
        "Blob" => "BLOB",
        "Boolean" => "BOOLEAN",
        "Byte" => "TINYINT",
        "Char" => "VARCHAR",
        "Int" => "INTEGER",
        "Long" => "BIGINT",
        "Date" => "DATE",
        "DateTime" => "DATETIME",
        "Timestamp" => "TIMESTAMP",
        _ => "VARCHAR",
    };
    Ok(Value::String(sql_type.to_string()))
}

fn rust_type_filter(value: &Value, _: &HashMap<String, Value>) -> tera::Result<Value> {
    // Convert FieldType to Rust type string
    let field_type_str = value.as_str().unwrap_or("");
    let rust_type = match field_type_str {
        "Blob" => "Vec<u8>",
        "Boolean" => "bool",
        "Byte" => "u8",
        "Char" => "String",
        "Int" => "i32",
        "Long" => "i64",
        "Date" => "NaiveDate",
        "DateTime" => "DateTime<Utc>",
        "Timestamp" => "DateTime<Utc>",
        _ => "String",
    };
    Ok(Value::String(rust_type.to_string()))
}

fn rust_default_filter(value: &Value, _: &HashMap<String, Value>) -> tera::Result<Value> {
    // Convert FieldType to Rust default value
    let field_type_str = value.as_str().unwrap_or("");
    let rust_default = match field_type_str {
        "Blob" => "Vec::new()",
        "Boolean" => "false",
        "Byte" => "0",
        "Char" => "String::new()",
        "Int" => "0",
        "Long" => "0",
        "Date" => "NaiveDate::from_ymd_opt(1970, 1, 1).unwrap()",
        "DateTime" => "DateTime::from_timestamp(0, 0).unwrap()",
        "Timestamp" => "DateTime::from_timestamp(0, 0).unwrap()",
        _ => "String::new()",
    };
    Ok(Value::String(rust_default.to_string()))
}

// Helper functions for case conversion

fn to_camel_case(s: &str) -> String {
    let mut result = String::new();
    let mut capitalize_next = false;
    
    for (i, c) in s.chars().enumerate() {
        if c == '_' || c == '-' || c == ' ' {
            capitalize_next = true;
        } else if i == 0 {
            result.push(c.to_lowercase().next().unwrap_or(c));
        } else if capitalize_next {
            result.push(c.to_uppercase().next().unwrap_or(c));
            capitalize_next = false;
        } else {
            result.push(c);
        }
    }
    
    result
}

fn to_pascal_case(s: &str) -> String {
    let mut camel = to_camel_case(s);
    if let Some(first_char) = camel.chars().next() {
        camel = first_char.to_uppercase().collect::<String>() + &camel[first_char.len_utf8()..];
    }
    camel
}

fn to_snake_case(s: &str) -> String {
    let mut result = String::new();
    
    for (i, c) in s.chars().enumerate() {
        if c.is_uppercase() && i > 0 {
            result.push('_');
        }
        result.push(c.to_lowercase().next().unwrap_or(c));
    }
    
    result
}

fn postgres_type_filter(value: &Value, _: &HashMap<String, Value>) -> tera::Result<Value> {
    // Convert Field object to PostgreSQL type string
    if let Some(field_obj) = value.as_object() {
        let field_type = field_obj.get("field_type")
            .and_then(|v| v.as_str())
            .unwrap_or("Char");
        
        let length = field_obj.get("length")
            .and_then(|v| v.as_i64())
            .unwrap_or(0) as i32;
            
        let precision = field_obj.get("precision")
            .and_then(|v| v.as_i64())
            .unwrap_or(0) as i32;
            
        let scale = field_obj.get("scale")
            .and_then(|v| v.as_i64())
            .unwrap_or(0) as i32;
        
        let postgres_type = match field_type {
            "Byte" => "smallint".to_string(),
            "Short" => "smallint".to_string(),
            "Int" => "integer".to_string(),
            "BigSequence" => "bigserial".to_string(),
            "Sequence" => "serial".to_string(),
            "Long" => "bigint".to_string(),
            "Char" => format!("varchar({})", length),
            "AnsiChar" => format!("character({}) -- beware char to varchar morph", length),
            "Date" => "date".to_string(),
            "DateTime" | "Timestamp" => "timestamp".to_string(),
            "Time" => "time".to_string(),
            "Float" | "Double" => {
                if precision == 0 && scale == 0 {
                    "float8".to_string()
                } else {
                    format!("numeric({}, {})", precision, scale)
                }
            },
            "Blob" => "bytea".to_string(),
            "TLob" => "text".to_string(),
            "Money" => "numeric(24,6)".to_string(),
            "UserStamp" => "VARCHAR(16)".to_string(),
            "Boolean" => "boolean".to_string(),
            "Identity" => "int generated by default as identity".to_string(),
            "BigIdentity" => "bigint generated by default as identity".to_string(),
            "Json" | "BigJson" => "jsonb".to_string(), // Could check for UseJSONInsteadOfJsonB flag
            "Xml" | "BigXml" => "xml".to_string(),
            _ => "unknown".to_string(),
        };
        
        Ok(Value::String(postgres_type))
    } else {
        Ok(Value::String("unknown".to_string()))
    }
}

fn regex_search_filter(value: &Value, args: &HashMap<String, Value>) -> tera::Result<Value> {
    let text = value.as_str().unwrap_or("");
    let pattern = args.get("pattern")
        .and_then(|v| v.as_str())
        .unwrap_or("");
    
    // Simple regex search - returns true if pattern is found in text
    let found = text.contains(pattern);
    Ok(Value::Bool(found))
} 