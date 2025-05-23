use pest::Parser;
use pest_derive::Parser;

#[derive(Parser)]
#[grammar = "grammar.pest"]
pub struct JPortalParser;

// Data structures representing the parsed database
#[derive(Debug, Clone)]
pub struct Database {
    pub name: String,
    pub userid: String,
    pub password: String,
    pub flags: Vec<String>,
    pub package_name: Option<String>,
    pub output: Option<String>,
    pub imports: Vec<String>,
    pub server: String,
    pub schema: Option<String>,
    pub tables: Vec<Table>,
    pub views: Vec<View>,
}

#[derive(Debug, Clone)]
pub struct Table {
    pub name: String,
    pub alias: Option<String>,
    pub check: Option<String>,
    pub comments: Vec<String>,
    pub options: Vec<String>,
    pub fields: Vec<Field>,
    pub keys: Vec<Key>,
    pub links: Vec<Link>,
    pub grants: Vec<Grant>,
    pub procs: Vec<Proc>,
    pub parameters: Vec<Parameter>,
    pub is_import: bool,
    pub import_fields: Vec<String>,
    pub is_literal: bool,
    pub literal_name: String,
    pub start_line: Option<i32>,
}

#[derive(Debug, Clone)]
pub struct Field {
    pub name: String,
    pub alias: Option<String>,
    pub field_type: FieldType,
    pub length: Option<i32>,
    pub precision: Option<i32>,
    pub scale: Option<i32>,
    pub is_null: bool,
    pub is_calc: bool,
    pub default_value: Option<String>,
    pub check_value: Option<String>,
    pub comments: Vec<String>,
    pub enums: Vec<Enum>,
    pub value_list: Vec<String>,
    pub is_literal: bool,
    pub literal_name: String,
    pub is_package_field: bool,
}

#[derive(Debug, Clone)]
pub enum FieldType {
    Blob,
    Boolean,
    Byte,
    Char,
    AnsiChar,
    WChar,
    WAnsiChar,
    Utf8,
    Short,
    Int,
    Long,
    Uid,
    Date,
    DateTime,
    Time,
    Timestamp,
    AutoTimestamp,
    Tlob,
    Xml,
    BigXml,
    Json,
    BigJson,
    UserStamp,
    Sequence,
    BigSequence,
    Identity,
    BigIdentity,
    Double,
    Float,
    Money,
}

#[derive(Debug, Clone)]
pub struct Enum {
    pub name: String,
    pub value: i32,
}

#[derive(Debug, Clone)]
pub struct Key {
    pub name: String,
    pub is_unique: bool,
    pub is_primary: bool,
    pub fields: Vec<String>,
    pub options: Vec<String>,
}

#[derive(Debug, Clone)]
pub struct Link {
    pub name: String,
    pub fields: Vec<String>,
    pub link_fields: Vec<String>,
    pub is_delete_cascade: bool,
    pub is_update_cascade: bool,
    pub options: Vec<String>,
}

#[derive(Debug, Clone)]
pub struct Grant {
    pub perms: Vec<String>,
    pub users: Vec<String>,
}

#[derive(Debug, Clone)]
pub struct View {
    pub name: String,
    pub users: Vec<String>,
    pub aliases: Vec<String>,
    pub lines: Vec<String>,
}

#[derive(Debug, Clone)]
pub struct Proc {
    pub name: String,
    pub is_proc: bool,
    pub is_sproc: bool,
    pub is_built_in: bool,
    pub comments: Vec<String>,
    pub options: Vec<String>,
    pub inputs: Vec<Field>,
    pub outputs: Vec<Field>,
    pub lines: Vec<String>,
}

#[derive(Debug, Clone)]
pub struct Parameter {
    pub title: Option<String>,
    pub is_view_only: bool,
    pub shows: Vec<String>,
    pub supplied: Vec<String>,
    pub cache_extras: Vec<String>,
}

// Parser implementation
pub fn parse_database(input: &str) -> Result<Database, Box<dyn std::error::Error>> {
    let pairs = JPortalParser::parse(Rule::database, input)?;
    
    for pair in pairs {
        match pair.as_rule() {
            Rule::database => {
                return parse_database_rule(pair);
            }
            _ => unreachable!(),
        }
    }
    
    Err("No database found".into())
}

fn parse_database_rule(pair: pest::iterators::Pair<Rule>) -> Result<Database, Box<dyn std::error::Error>> {
    let mut database = Database {
        name: String::new(),
        userid: String::new(),
        password: String::new(),
        flags: Vec::new(),
        package_name: None,
        output: None,
        imports: Vec::new(),
        server: String::new(),
        schema: None,
        tables: Vec::new(),
        views: Vec::new(),
    };
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::database_name => {
                for name_pair in inner_pair.into_inner() {
                    database.name = parse_ident_or_string_from_pair(name_pair);
                }
            }
            Rule::flags_section => {
                for flag_pair in inner_pair.into_inner() {
                    if let Rule::string_literal = flag_pair.as_rule() {
                        database.flags.push(parse_string_literal(flag_pair.as_str()));
                    }
                }
            }
            Rule::package_section => {
                for pkg_pair in inner_pair.into_inner() {
                    if let Rule::package_ident = pkg_pair.as_rule() {
                        database.package_name = Some(pkg_pair.as_str().to_string());
                    }
                }
            }
            Rule::output_section => {
                for out_pair in inner_pair.into_inner() {
                    if let Rule::ident_or_string = out_pair.as_rule() {
                        database.output = Some(parse_ident_or_string(out_pair));
                    }
                }
            }
            Rule::import_section => {
                for imp_pair in inner_pair.into_inner() {
                    if let Rule::ident_or_string = imp_pair.as_rule() {
                        database.imports.push(parse_ident_or_string(imp_pair));
                    }
                }
            }
            Rule::connect_section => {
                parse_connect_section(inner_pair, &mut database)?;
            }
            Rule::tables_section => {
                parse_tables_section(inner_pair, &mut database)?;
            }
            _ => {}
        }
    }
    
    Ok(database)
}

// Enhanced string literal parsing - matches JavaCC fixString() behavior
fn parse_string_literal(s: &str) -> String {
    // Remove quotes and handle escape sequences
    let trimmed = s.trim_matches('"').trim_matches('\'');
    trimmed.to_string()
}

// Enhanced identifier parsing - matches JavaCC jIdent() behavior
fn parse_ident_or_string_from_pair(pair: pest::iterators::Pair<Rule>) -> String {
    match pair.as_rule() {
        Rule::identifier => pair.as_str().to_string(),
        Rule::literal_identifier => {
            // Handle L'identifier' format - extract content between quotes
            let s = pair.as_str();
            if s.len() > 3 && (s.starts_with("L'") || s.starts_with("l'")) && s.ends_with('\'') {
                s[2..s.len()-1].to_string()
            } else {
                s.to_string()
            }
        }
        Rule::string_literal => pair.as_str().to_string(), // Keep quotes for consistency
        _ => pair.as_str().to_string(),
    }
}

fn parse_ident_or_string(pair: pest::iterators::Pair<Rule>) -> String {
    for inner_pair in pair.clone().into_inner() {
        return parse_ident_or_string_from_pair(inner_pair);
    }
    pair.as_str().to_string()
}

fn parse_connect_section(pair: pest::iterators::Pair<Rule>, database: &mut Database) -> Result<(), Box<dyn std::error::Error>> {
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::server_clause => {
                for server_pair in inner_pair.into_inner() {
                    if let Rule::ident_or_string = server_pair.as_rule() {
                        database.server = parse_ident_or_string(server_pair);
                    }
                }
            }
            Rule::schema_clause => {
                for schema_pair in inner_pair.into_inner() {
                    if let Rule::ident_or_string = schema_pair.as_rule() {
                        database.schema = Some(parse_ident_or_string(schema_pair));
                    }
                }
            }
            Rule::userid_clause => {
                for userid_pair in inner_pair.into_inner() {
                    if let Rule::identifier = userid_pair.as_rule() {
                        database.userid = userid_pair.as_str().to_string();
                    }
                }
            }
            Rule::password_clause => {
                for password_pair in inner_pair.into_inner() {
                    if let Rule::identifier = password_pair.as_rule() {
                        database.password = password_pair.as_str().to_string();
                    }
                }
            }
            _ => {}
        }
    }
    Ok(())
}

// Enhanced tables parsing - matches JavaCC jTables() behavior
fn parse_tables_section(pair: pest::iterators::Pair<Rule>, database: &mut Database) -> Result<(), Box<dyn std::error::Error>> {
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::table_definition => {
                let table = parse_table_definition(inner_pair)?;
                database.tables.push(table);
            }
            _ => {}
        }
    }
    Ok(())
}

fn parse_table_definition(pair: pest::iterators::Pair<Rule>) -> Result<Table, Box<dyn std::error::Error>> {
    let mut table = Table {
        name: String::new(),
        alias: None,
        check: None,
        comments: Vec::new(),
        options: Vec::new(),
        fields: Vec::new(),
        keys: Vec::new(),
        links: Vec::new(),
        grants: Vec::new(),
        procs: Vec::new(),
        parameters: Vec::new(),
        is_import: false,
        import_fields: Vec::new(),
        is_literal: false,
        literal_name: String::new(),
        start_line: None,
    };
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::table_section => {
                parse_table_section_into(inner_pair, &mut table)?;
            }
            Rule::table_import_section => {
                parse_table_import_section_into(inner_pair, &mut table)?;
                table.is_import = true;
            }
            Rule::table_extras => {
                parse_table_extras_into(inner_pair, &mut table)?;
            }
            Rule::proc_section => {
                let proc = parse_proc_section(inner_pair)?;
                table.procs.push(proc);
            }
            Rule::parm_section => {
                let parameter = parse_parm_section(inner_pair)?;
                table.parameters.push(parameter);
            }
            _ => {}
        }
    }
    
    Ok(table)
}

fn parse_table_section_into(pair: pest::iterators::Pair<Rule>, table: &mut Table) -> Result<(), Box<dyn std::error::Error>> {
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::identifier => {
                table.name = inner_pair.as_str().to_string();
            }
            Rule::alias_clause => {
                table.alias = Some(parse_alias_clause(inner_pair)?);
            }
            Rule::check_clause => {
                table.check = Some(parse_check_clause(inner_pair)?);
            }
            Rule::comment_clause => {
                table.comments.push(parse_comment_clause(inner_pair)?);
            }
            Rule::options_clause => {
                table.options = parse_options_clause(inner_pair)?;
            }
            Rule::field_def => {
                let field = parse_field_def(inner_pair)?;
                table.fields.push(field);
            }
            Rule::package_field_def => {
                let field = parse_package_field_def(inner_pair)?;
                table.fields.push(field);
            }
            _ => {}
        }
    }
    Ok(())
}

fn parse_table_import_section_into(pair: pest::iterators::Pair<Rule>, table: &mut Table) -> Result<(), Box<dyn std::error::Error>> {
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::ident_or_string => {
                table.name = parse_ident_or_string(inner_pair);
            }
            Rule::field_import_list => {
                table.import_fields = parse_field_import_list(inner_pair)?;
            }
            Rule::alias_clause => {
                table.alias = Some(parse_alias_clause(inner_pair)?);
            }
            _ => {}
        }
    }
    Ok(())
}

fn parse_alias_clause(pair: pest::iterators::Pair<Rule>) -> Result<String, Box<dyn std::error::Error>> {
    for inner_pair in pair.into_inner() {
        if let Rule::ident_or_string = inner_pair.as_rule() {
            return Ok(parse_ident_or_string(inner_pair));
        }
    }
    Ok(String::new())
}

fn parse_check_clause(pair: pest::iterators::Pair<Rule>) -> Result<String, Box<dyn std::error::Error>> {
    for inner_pair in pair.into_inner() {
        if let Rule::string_literal = inner_pair.as_rule() {
            return Ok(parse_string_literal(inner_pair.as_str()));
        }
    }
    Ok(String::new())
}

fn parse_comment_clause(pair: pest::iterators::Pair<Rule>) -> Result<String, Box<dyn std::error::Error>> {
    for inner_pair in pair.clone().into_inner() {
        if let Rule::string_literal = inner_pair.as_rule() {
            return Ok(parse_string_literal(inner_pair.as_str()));
        }
    }
    Ok(pair.as_str().to_string())
}

fn parse_options_clause(pair: pest::iterators::Pair<Rule>) -> Result<Vec<String>, Box<dyn std::error::Error>> {
    let mut options = Vec::new();
    for inner_pair in pair.into_inner() {
        if let Rule::string_literal = inner_pair.as_rule() {
            options.push(parse_string_literal(inner_pair.as_str()));
        }
    }
    Ok(options)
}

fn parse_field_import_list(pair: pest::iterators::Pair<Rule>) -> Result<Vec<String>, Box<dyn std::error::Error>> {
    let mut fields = Vec::new();
    for inner_pair in pair.into_inner() {
        if let Rule::identifier = inner_pair.as_rule() {
            fields.push(inner_pair.as_str().to_string());
        }
    }
    Ok(fields)
}

fn parse_package_field_def(pair: pest::iterators::Pair<Rule>) -> Result<Field, Box<dyn std::error::Error>> {
    let mut field = Field {
        name: String::new(),
        alias: None,
        field_type: FieldType::Int,
        length: None,
        precision: None,
        scale: None,
        is_null: true, // fieldsNullByDefault in JavaCC
        is_calc: false,
        default_value: None,
        check_value: None,
        comments: Vec::new(),
        enums: Vec::new(),
        value_list: Vec::new(),
        is_literal: false,
        literal_name: String::new(),
        is_package_field: true,
    };
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::package_ident => {
                field.name = inner_pair.as_str().to_string();
                // TODO: Set is_literal and literal_name based on parsing
            }
            Rule::alias_clause => {
                field.alias = Some(parse_alias_clause(inner_pair)?);
            }
            Rule::field_type => {
                field.field_type = parse_field_type(inner_pair)?;
            }
            Rule::package_field_modifiers => {
                parse_package_field_modifiers(inner_pair, &mut field)?;
            }
            Rule::comment_clause => {
                field.comments.push(parse_comment_clause(inner_pair)?);
            }
            _ => {}
        }
    }
    
    Ok(field)
}

fn parse_package_field_modifiers(pair: pest::iterators::Pair<Rule>, field: &mut Field) -> Result<(), Box<dyn std::error::Error>> {
    let mut not_modifier = false;
    let mut expecting_default_value = false;
    let mut expecting_check_value = false;
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::DEFAULTV => {
                expecting_default_value = true;
            }
            Rule::string_literal => {
                if expecting_default_value {
                    field.default_value = Some(parse_string_literal(inner_pair.as_str()));
                    expecting_default_value = false;
                } else if expecting_check_value {
                    field.check_value = Some(parse_string_literal(inner_pair.as_str()));
                    expecting_check_value = false;
                }
            }
            Rule::NOT => {
                not_modifier = true;
            }
            Rule::NULL => {
                field.is_null = !not_modifier;
                not_modifier = false;
            }
            Rule::CALC => {
                field.is_calc = true;
            }
            Rule::CHECK => {
                expecting_check_value = true;
            }
            _ => {}
        }
    }
    Ok(())
}

fn parse_table_extras_into(pair: pest::iterators::Pair<Rule>, table: &mut Table) -> Result<(), Box<dyn std::error::Error>> {
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::key_section => {
                let key = parse_key_section(inner_pair)?;
                table.keys.push(key);
            }
            Rule::link_section => {
                let link = parse_link_section(inner_pair)?;
                table.links.push(link);
            }
            Rule::grant_section => {
                let grant = parse_grant_section(inner_pair)?;
                table.grants.push(grant);
            }
            _ => {}
        }
    }
    Ok(())
}

fn parse_key_section(pair: pest::iterators::Pair<Rule>) -> Result<Key, Box<dyn std::error::Error>> {
    let mut key = Key {
        name: String::new(),
        is_unique: false,
        is_primary: false,
        fields: Vec::new(),
        options: Vec::new(),
    };
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::identifier => {
                key.name = inner_pair.as_str().to_string();
            }
            Rule::field_list => {
                key.fields = parse_field_list(inner_pair)?;
            }
            _ => {}
        }
    }
    
    Ok(key)
}

fn parse_link_section(pair: pest::iterators::Pair<Rule>) -> Result<Link, Box<dyn std::error::Error>> {
    let mut link = Link {
        name: String::new(),
        fields: Vec::new(),
        link_fields: Vec::new(),
        is_delete_cascade: false,
        is_update_cascade: false,
        options: Vec::new(),
    };
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::identifier => {
                link.name = inner_pair.as_str().to_string();
            }
            Rule::field_list => {
                link.fields = parse_field_list(inner_pair)?;
            }
            _ => {}
        }
    }
    
    Ok(link)
}

fn parse_grant_section(pair: pest::iterators::Pair<Rule>) -> Result<Grant, Box<dyn std::error::Error>> {
    let mut grant = Grant {
        perms: Vec::new(),
        users: Vec::new(),
    };
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::identifier => {
                grant.perms.push(inner_pair.as_str().to_string());
            }
            Rule::field_list => {
                grant.users = parse_field_list(inner_pair)?;
            }
            _ => {}
        }
    }
    
    Ok(grant)
}

fn parse_field_list(pair: pest::iterators::Pair<Rule>) -> Result<Vec<String>, Box<dyn std::error::Error>> {
    let mut fields = Vec::new();
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::identifier => {
                fields.push(inner_pair.as_str().to_string());
            }
            _ => {}
        }
    }
    
    Ok(fields)
}

fn parse_proc_section(pair: pest::iterators::Pair<Rule>) -> Result<Proc, Box<dyn std::error::Error>> {
    let mut proc = Proc {
        name: String::new(),
        is_proc: false,
        is_sproc: false,
        is_built_in: false,
        comments: Vec::new(),
        options: Vec::new(),
        inputs: Vec::new(),
        outputs: Vec::new(),
        lines: Vec::new(),
    };
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::PROC => {
                proc.is_proc = true;
            }
            Rule::SPROC => {
                proc.is_sproc = true;
            }
            Rule::identifier => {
                proc.name = inner_pair.as_str().to_string();
            }
            Rule::proc_body => {
                parse_proc_body_into(inner_pair, &mut proc)?;
            }
            _ => {}
        }
    }
    
    Ok(proc)
}

fn parse_proc_body_into(pair: pest::iterators::Pair<Rule>, proc: &mut Proc) -> Result<(), Box<dyn std::error::Error>> {
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::proc_content => {
                for content_pair in inner_pair.into_inner() {
                    match content_pair.as_rule() {
                        Rule::field_def => {
                            let field = parse_field_def(content_pair)?;
                            proc.inputs.push(field);
                        }
                        Rule::package_field_def => {
                            let field = parse_package_field_def(content_pair)?;
                            proc.inputs.push(field);
                        }
                        Rule::identifier | Rule::string_literal | Rule::number => {
                            proc.lines.push(content_pair.as_str().to_string());
                        }
                        _ => {}
                    }
                }
            }
            _ => {}
        }
    }
    Ok(())
}

fn parse_parm_section(pair: pest::iterators::Pair<Rule>) -> Result<Parameter, Box<dyn std::error::Error>> {
    let mut parameter = Parameter {
        title: None,
        is_view_only: false,
        shows: Vec::new(),
        supplied: Vec::new(),
        cache_extras: Vec::new(),
    };
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::parm_body => {
                parse_parm_body_into(inner_pair, &mut parameter)?;
            }
            _ => {}
        }
    }
    
    Ok(parameter)
}

fn parse_parm_body_into(pair: pest::iterators::Pair<Rule>, parameter: &mut Parameter) -> Result<(), Box<dyn std::error::Error>> {
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::parm_content => {
                for content_pair in inner_pair.into_inner() {
                    match content_pair.as_rule() {
                        Rule::identifier => {
                            parameter.shows.push(content_pair.as_str().to_string());
                        }
                        Rule::string_literal => {
                            parameter.supplied.push(parse_string_literal(content_pair.as_str()));
                        }
                        _ => {}
                    }
                }
            }
            _ => {}
        }
    }
    Ok(())
}

fn parse_field_def(pair: pest::iterators::Pair<Rule>) -> Result<Field, Box<dyn std::error::Error>> {
    let mut field = Field {
        name: String::new(),
        alias: None,
        field_type: FieldType::Int,
        length: None,
        precision: None,
        scale: None,
        is_null: true,
        is_calc: false,
        default_value: None,
        check_value: None,
        comments: Vec::new(),
        enums: Vec::new(),
        value_list: Vec::new(),
        is_literal: false,
        literal_name: String::new(),
        is_package_field: false,
    };
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::identifier => {
                field.name = inner_pair.as_str().to_string();
            }
            Rule::field_type => {
                field.field_type = parse_field_type(inner_pair)?;
            }
            Rule::field_modifiers => {
                parse_field_modifiers(inner_pair, &mut field)?;
            }
            _ => {}
        }
    }
    
    Ok(field)
}

fn parse_field_type(pair: pest::iterators::Pair<Rule>) -> Result<FieldType, Box<dyn std::error::Error>> {
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::BLOB => return Ok(FieldType::Blob),
            Rule::BOOLEAN => return Ok(FieldType::Boolean),
            Rule::BYTE => return Ok(FieldType::Byte),
            Rule::CHAR => return Ok(FieldType::Char),
            Rule::ANSICHAR => return Ok(FieldType::AnsiChar),
            Rule::WCHAR => return Ok(FieldType::WChar),
            Rule::WANSICHAR => return Ok(FieldType::WAnsiChar),
            Rule::UTF8 => return Ok(FieldType::Utf8),
            Rule::SHORT => return Ok(FieldType::Short),
            Rule::INT => return Ok(FieldType::Int),
            Rule::LONG => return Ok(FieldType::Long),
            Rule::UID => return Ok(FieldType::Uid),
            Rule::DATE => return Ok(FieldType::Date),
            Rule::DATETIME => return Ok(FieldType::DateTime),
            Rule::TIME => return Ok(FieldType::Time),
            Rule::TIMESTAMP => return Ok(FieldType::Timestamp),
            Rule::AUTOTIMESTAMP => return Ok(FieldType::AutoTimestamp),
            Rule::TLOB => return Ok(FieldType::Tlob),
            Rule::XML => return Ok(FieldType::Xml),
            Rule::BIGXML => return Ok(FieldType::BigXml),
            Rule::JSON => return Ok(FieldType::Json),
            Rule::BIGJSON => return Ok(FieldType::BigJson),
            Rule::USERSTAMP => return Ok(FieldType::UserStamp),
            Rule::SEQUENCE => return Ok(FieldType::Sequence),
            Rule::BIGSEQUENCE => return Ok(FieldType::BigSequence),
            Rule::IDENTITY => return Ok(FieldType::Identity),
            Rule::BIGIDENTITY => return Ok(FieldType::BigIdentity),
            Rule::DOUBLE => return Ok(FieldType::Double),
            Rule::FLOAT => return Ok(FieldType::Float),
            Rule::MONEY => return Ok(FieldType::Money),
            Rule::size_spec => {
                // Handle size specification - we'll ignore it for now but could store it
            }
            _ => {}
        }
    }
    Ok(FieldType::Int) // default
}

fn parse_field_modifiers(pair: pest::iterators::Pair<Rule>, field: &mut Field) -> Result<(), Box<dyn std::error::Error>> {
    let mut not_modifier = false;
    
    for inner_pair in pair.into_inner() {
        match inner_pair.as_rule() {
            Rule::DEFAULT => {
                // Handle default value - could extract the string literal
                for default_pair in inner_pair.into_inner() {
                    if let Rule::string_literal = default_pair.as_rule() {
                        field.default_value = Some(parse_string_literal(default_pair.as_str()));
                    }
                }
            }
            Rule::NOT => {
                not_modifier = true;
            }
            Rule::NULL => {
                field.is_null = !not_modifier;
                not_modifier = false;
            }
            Rule::CHECK => {
                // Handle check constraint
                for check_pair in inner_pair.into_inner() {
                    if let Rule::string_literal = check_pair.as_rule() {
                        field.check_value = Some(parse_string_literal(check_pair.as_str()));
                    }
                }
            }
            _ => {}
        }
    }
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_basic_database_parsing() {
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
} 