use pest::Parser;
use pest_derive::Parser;

#[derive(Parser)]
#[grammar = "grammar.pest"]
pub struct TestParser;

fn main() {
    let input = r#"PROC DATA
    "INSERT INTO Users VALUES (1, 'test')"
ENDDATA"#;
    
    match TestParser::parse(Rule::proc_section, input) {
        Ok(pairs) => {
            for pair in pairs {
                println!("Parsed: {:?}", pair);
                for inner in pair.into_inner() {
                    println!("  Inner: {:?} = '{}'", inner.as_rule(), inner.as_str());
                }
            }
        }
        Err(e) => {
            println!("Parse error: {:?}", e);
        }
    }
} 