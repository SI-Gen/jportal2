use std::path::PathBuf;

/// JPortal2 command-line arguments structure
/// 
/// This is the Rust equivalent of the Java JPortal2Arguments class.
#[derive(Debug, Clone)]
pub struct JPortal2Arguments {
    /// Input files to process
    pub input_files: Vec<PathBuf>,
    
    /// Output directory for generated files
    pub output_dir: Option<PathBuf>,
    
    /// Template directory or URL
    pub template_source: Option<String>,
    
    /// Log file name
    pub log_file_name: Option<String>,
    
    /// Debug mode flag
    pub debug: bool,
    
    /// Verbose output flag
    pub verbose: bool,
    
    /// Force overwrite existing files
    pub force: bool,
    
    /// Generator types to use
    pub generators: Vec<String>,
    
    /// Additional flags
    pub flags: Vec<String>,
    
    /// Database connection string
    pub connection_string: Option<String>,
    
    /// Project file path
    pub project_file: Option<PathBuf>,
    
    /// Working directory
    pub working_dir: Option<PathBuf>,
}

impl JPortal2Arguments {
    /// Create new arguments with default values
    pub fn new() -> Self {
        Self {
            input_files: Vec::new(),
            output_dir: None,
            template_source: None,
            log_file_name: None,
            debug: false,
            verbose: false,
            force: false,
            generators: Vec::new(),
            flags: Vec::new(),
            connection_string: None,
            project_file: None,
            working_dir: None,
        }
    }
    
    /// Check if debug mode is enabled
    pub fn must_debug(&self) -> bool {
        self.debug
    }
    
    /// Get log file name
    pub fn get_log_file_name(&self) -> Option<&String> {
        self.log_file_name.as_ref()
    }
    
    /// Check if verbose mode is enabled
    pub fn is_verbose(&self) -> bool {
        self.verbose
    }
    
    /// Check if force mode is enabled
    pub fn is_force(&self) -> bool {
        self.force
    }
    
    /// Get output directory
    pub fn get_output_dir(&self) -> Option<&PathBuf> {
        self.output_dir.as_ref()
    }
    
    /// Get template source
    pub fn get_template_source(&self) -> Option<&String> {
        self.template_source.as_ref()
    }
    
    /// Get input files
    pub fn get_input_files(&self) -> &Vec<PathBuf> {
        &self.input_files
    }
    
    /// Get generators
    pub fn get_generators(&self) -> &Vec<String> {
        &self.generators
    }
    
    /// Get flags
    pub fn get_flags(&self) -> &Vec<String> {
        &self.flags
    }
    
    /// Get connection string
    pub fn get_connection_string(&self) -> Option<&String> {
        self.connection_string.as_ref()
    }
    
    /// Get project file
    pub fn get_project_file(&self) -> Option<&PathBuf> {
        self.project_file.as_ref()
    }
    
    /// Get working directory
    pub fn get_working_dir(&self) -> Option<&PathBuf> {
        self.working_dir.as_ref()
    }
}

impl Default for JPortal2Arguments {
    fn default() -> Self {
        Self::new()
    }
}

/// Command-line argument parser
/// 
/// This is the Rust equivalent of the Java JPortal2ArgumentParser class.
pub struct JPortal2ArgumentParser;

impl JPortal2ArgumentParser {
    /// Parse command-line arguments
    /// 
    /// Returns None if help was requested or parsing failed.
    /// Returns Some(arguments) if parsing was successful.
    pub fn parse(args: &[String]) -> Option<JPortal2Arguments> {
        let mut arguments = JPortal2Arguments::new();
        let mut i = 1; // Skip program name
        
        if args.len() <= 1 {
            Self::print_help();
            return None;
        }
        
        while i < args.len() {
            let arg = &args[i];
            
            match arg.as_str() {
                "-h" | "--help" => {
                    Self::print_help();
                    return None;
                }
                "-d" | "--debug" => {
                    arguments.debug = true;
                }
                "-v" | "--verbose" => {
                    arguments.verbose = true;
                }
                "-f" | "--force" => {
                    arguments.force = true;
                }
                "-o" | "--output" => {
                    if i + 1 < args.len() {
                        i += 1;
                        arguments.output_dir = Some(PathBuf::from(&args[i]));
                    } else {
                        eprintln!("Error: --output requires a directory path");
                        return None;
                    }
                }
                "-t" | "--templates" => {
                    if i + 1 < args.len() {
                        i += 1;
                        arguments.template_source = Some(args[i].clone());
                    } else {
                        eprintln!("Error: --templates requires a path or URL");
                        return None;
                    }
                }
                "-l" | "--log" => {
                    if i + 1 < args.len() {
                        i += 1;
                        arguments.log_file_name = Some(args[i].clone());
                    } else {
                        eprintln!("Error: --log requires a file name");
                        return None;
                    }
                }
                "-g" | "--generator" => {
                    if i + 1 < args.len() {
                        i += 1;
                        arguments.generators.push(args[i].clone());
                    } else {
                        eprintln!("Error: --generator requires a generator name");
                        return None;
                    }
                }
                "-c" | "--connection" => {
                    if i + 1 < args.len() {
                        i += 1;
                        arguments.connection_string = Some(args[i].clone());
                    } else {
                        eprintln!("Error: --connection requires a connection string");
                        return None;
                    }
                }
                "-p" | "--project" => {
                    if i + 1 < args.len() {
                        i += 1;
                        arguments.project_file = Some(PathBuf::from(&args[i]));
                    } else {
                        eprintln!("Error: --project requires a project file path");
                        return None;
                    }
                }
                "-w" | "--workdir" => {
                    if i + 1 < args.len() {
                        i += 1;
                        arguments.working_dir = Some(PathBuf::from(&args[i]));
                    } else {
                        eprintln!("Error: --workdir requires a directory path");
                        return None;
                    }
                }
                "--flag" => {
                    if i + 1 < args.len() {
                        i += 1;
                        arguments.flags.push(args[i].clone());
                    } else {
                        eprintln!("Error: --flag requires a flag value");
                        return None;
                    }
                }
                _ => {
                    if arg.starts_with('-') {
                        eprintln!("Error: Unknown option: {}", arg);
                        return None;
                    } else {
                        // Treat as input file
                        arguments.input_files.push(PathBuf::from(arg));
                    }
                }
            }
            i += 1;
        }
        
        // Validate arguments
        if arguments.input_files.is_empty() && arguments.project_file.is_none() {
            eprintln!("Error: No input files or project file specified");
            Self::print_help();
            return None;
        }
        
        Some(arguments)
    }
    
    /// Print help message
    fn print_help() {
        println!("JPortal2 Rust - Database Definition Language Processor");
        println!();
        println!("USAGE:");
        println!("    jportal2 [OPTIONS] [INPUT_FILES...]");
        println!();
        println!("OPTIONS:");
        println!("    -h, --help              Show this help message");
        println!("    -d, --debug             Enable debug logging");
        println!("    -v, --verbose           Enable verbose output");
        println!("    -f, --force             Force overwrite existing files");
        println!("    -o, --output <DIR>      Output directory for generated files");
        println!("    -t, --templates <PATH>  Template directory or URL");
        println!("    -l, --log <FILE>        Log file name");
        println!("    -g, --generator <GEN>   Generator to use (can be specified multiple times)");
        println!("    -c, --connection <STR>  Database connection string");
        println!("    -p, --project <FILE>    Project file to process");
        println!("    -w, --workdir <DIR>     Working directory");
        println!("    --flag <FLAG>           Additional flag (can be specified multiple times)");
        println!();
        println!("EXAMPLES:");
        println!("    jportal2 database.si");
        println!("    jportal2 -o generated -g rust database.si");
        println!("    jportal2 --debug --verbose -p project.json");
        println!("    jportal2 -t https://templates.example.com database.si");
    }
} 