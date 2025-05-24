use std::env;
use std::process;
use log::{error, debug, info};
use env_logger;

// Import the library module
use jportal2_lib as lib;

// Local modules
mod arguments;
mod template_downloader;
mod project_compiler;

use arguments::{JPortal2Arguments, JPortal2ArgumentParser};
use template_downloader::TemplateDownloader;
use project_compiler::{ProjectCompiler, ProjectCompilerBuilder};

/// JPortal2 Main Entry Point
/// 
/// Reads input from stored repository and processes JPortal database definitions.
/// This is the Rust port of the Java Main.java functionality.
fn main() {
    // Parse command line arguments
    let args: Vec<String> = env::args().collect();
    let arguments = match JPortal2ArgumentParser::parse(&args) {
        Some(args) => args,
        None => {
            process::exit(0);
        }
    };

    // Set up logging before the logger starts
    setup_logging(&arguments);

    if arguments.must_debug() {
        debug!("Debug logging enabled");
    }

    // Main processing with error handling
    match run_main_process(arguments) {
        Ok(exit_code) => {
            process::exit(exit_code);
        }
        Err(e) => {
            error!("General Exception caught: {}", e);
            process::exit(3);
        }
    }
}

/// Main processing logic separated for better error handling
fn run_main_process(arguments: JPortal2Arguments) -> Result<i32, Box<dyn std::error::Error>> {
    // Download templates if needed
    let template_downloader = TemplateDownloader::new();
    let download_rc = template_downloader.download_templates(&arguments)?;
    
    if download_rc > 0 {
        return Ok(download_rc);
    }

    // Build and run project compiler
    let project_compiler = ProjectCompilerBuilder::build(&arguments)?;
    
    match project_compiler {
        Some(compiler) => {
            let rc = compiler.compile_all()?;
            Ok(rc)
        }
        None => {
            Ok(1)
        }
    }
}

/// Set up logging configuration based on arguments
fn setup_logging(arguments: &JPortal2Arguments) {
    let mut builder = env_logger::Builder::new();
    
    // Set log level based on debug flag
    if arguments.must_debug() {
        builder.filter_level(log::LevelFilter::Debug);
    } else {
        builder.filter_level(log::LevelFilter::Info);
    }
    
    // Configure log file if specified
    if let Some(log_file_name) = arguments.get_log_file_name() {
        // In Rust, we'll use env_logger with file output
        // This is a simplified version - in production you might want to use a more sophisticated logging framework
        env::set_var("RUST_LOG_FILE", log_file_name);
    }
    
    builder.init();
    
    info!("JPortal2 Rust version starting");
}
