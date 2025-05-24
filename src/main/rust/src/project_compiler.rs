use std::fs;
use std::path::{Path, PathBuf};
use log::{info, warn, error, debug};
use crate::arguments::JPortal2Arguments;
use crate::lib::{Database, parse_database};

/// Project compiler for JPortal2
/// 
/// This is the Rust equivalent of the Java ProjectCompiler class.
/// Handles compiling JPortal database definitions and generating output.
pub struct ProjectCompiler {
    /// Arguments for compilation
    arguments: JPortal2Arguments,
    
    /// Working directory
    working_dir: PathBuf,
    
    /// Output directory
    output_dir: PathBuf,
    
    /// Template directory
    template_dir: Option<PathBuf>,
}

impl ProjectCompiler {
    /// Create a new project compiler
    pub fn new(arguments: JPortal2Arguments) -> Self {
        let working_dir = arguments.get_working_dir()
            .cloned()
            .unwrap_or_else(|| std::env::current_dir().unwrap_or_else(|_| PathBuf::from(".")));
        
        let output_dir = arguments.get_output_dir()
            .cloned()
            .unwrap_or_else(|| working_dir.join("generated"));
        
        Self {
            arguments,
            working_dir,
            output_dir,
            template_dir: None,
        }
    }
    
    /// Set the template directory
    pub fn set_template_dir(&mut self, template_dir: PathBuf) {
        self.template_dir = Some(template_dir);
    }
    
    /// Compile all input files
    /// 
    /// Returns 0 on success, positive error code on failure.
    pub fn compile_all(&self) -> Result<i32, Box<dyn std::error::Error>> {
        info!("Starting compilation process");
        
        // Create output directory if it doesn't exist
        if !self.output_dir.exists() {
            fs::create_dir_all(&self.output_dir)?;
            info!("Created output directory: {:?}", self.output_dir);
        }
        
        let mut total_errors = 0;
        
        // Process project file if specified
        if let Some(project_file) = self.arguments.get_project_file() {
            match self.compile_project_file(project_file) {
                Ok(errors) => total_errors += errors,
                Err(e) => {
                    error!("Failed to compile project file {:?}: {}", project_file, e);
                    total_errors += 1;
                }
            }
        }
        
        // Process individual input files
        for input_file in self.arguments.get_input_files() {
            match self.compile_input_file(input_file) {
                Ok(errors) => total_errors += errors,
                Err(e) => {
                    error!("Failed to compile input file {:?}: {}", input_file, e);
                    total_errors += 1;
                }
            }
        }
        
        if total_errors == 0 {
            info!("Compilation completed successfully");
            Ok(0)
        } else {
            error!("Compilation completed with {} errors", total_errors);
            Ok(total_errors)
        }
    }
    
    /// Compile a project file
    fn compile_project_file(&self, project_file: &Path) -> Result<i32, Box<dyn std::error::Error>> {
        info!("Compiling project file: {:?}", project_file);
        
        if !project_file.exists() {
            error!("Project file does not exist: {:?}", project_file);
            return Ok(1);
        }
        
        // Read and parse project file (JSON format expected)
        let project_content = fs::read_to_string(project_file)?;
        
        // TODO: Implement JSON project file parsing
        // For now, we'll just log that we would process it
        warn!("Project file processing not yet fully implemented");
        debug!("Project file content length: {} bytes", project_content.len());
        
        Ok(0)
    }
    
    /// Compile a single input file
    fn compile_input_file(&self, input_file: &Path) -> Result<i32, Box<dyn std::error::Error>> {
        info!("Compiling input file: {:?}", input_file);
        
        if !input_file.exists() {
            error!("Input file does not exist: {:?}", input_file);
            return Ok(1);
        }
        
        // Read the input file
        let input_content = fs::read_to_string(input_file)?;
        
        // Parse the database definition
        match parse_database(&input_content) {
            Ok(database) => {
                info!("Successfully parsed database: {}", database.name);
                
                // Generate output files
                self.generate_output(&database, input_file)?;
                
                Ok(0)
            }
            Err(e) => {
                error!("Failed to parse database from {:?}: {}", input_file, e);
                Ok(1)
            }
        }
    }
    
    /// Generate output files for a database
    fn generate_output(&self, database: &Database, source_file: &Path) -> Result<(), Box<dyn std::error::Error>> {
        info!("Generating output for database: {}", database.name);
        
        // Create database-specific output directory
        let db_output_dir = self.output_dir.join(&database.name);
        if !db_output_dir.exists() {
            fs::create_dir_all(&db_output_dir)?;
        }
        
        // Generate summary file
        self.generate_summary_file(database, &db_output_dir, source_file)?;
        
        // Generate files for each requested generator
        for generator in self.arguments.get_generators() {
            match self.generate_for_generator(database, generator, &db_output_dir) {
                Ok(_) => {
                    info!("Generated output for generator: {}", generator);
                }
                Err(e) => {
                    error!("Failed to generate output for generator {}: {}", generator, e);
                }
            }
        }
        
        // If no generators specified, generate a default summary
        if self.arguments.get_generators().is_empty() {
            info!("No generators specified, generating summary only");
        }
        
        Ok(())
    }
    
    /// Generate a summary file for the database
    fn generate_summary_file(&self, database: &Database, output_dir: &Path, source_file: &Path) -> Result<(), Box<dyn std::error::Error>> {
        let summary_file = output_dir.join("database_summary.txt");
        
        let mut summary = String::new();
        summary.push_str(&format!("JPortal2 Database Summary\n"));
        summary.push_str(&format!("========================\n\n"));
        summary.push_str(&format!("Source File: {:?}\n", source_file));
        summary.push_str(&format!("Database Name: {}\n", database.name));
        summary.push_str(&format!("Server: {}\n", database.server));
        
        if let Some(schema) = &database.schema {
            summary.push_str(&format!("Schema: {}\n", schema));
        }
        
        if let Some(package) = &database.package_name {
            summary.push_str(&format!("Package: {}\n", package));
        }
        
        summary.push_str(&format!("Tables: {}\n", database.tables.len()));
        summary.push_str(&format!("Views: {}\n", database.views.len()));
        
        if !database.flags.is_empty() {
            summary.push_str(&format!("Flags: {:?}\n", database.flags));
        }
        
        summary.push_str("\nTables:\n");
        for (i, table) in database.tables.iter().enumerate() {
            summary.push_str(&format!("  {}. {} ({} fields, {} procedures)\n", 
                i + 1, table.name, table.fields.len(), table.procs.len()));
        }
        
        if !database.views.is_empty() {
            summary.push_str("\nViews:\n");
            for (i, view) in database.views.iter().enumerate() {
                summary.push_str(&format!("  {}. {}\n", i + 1, view.name));
            }
        }
        
        fs::write(summary_file, summary)?;
        info!("Generated database summary");
        
        Ok(())
    }
    
    /// Generate output for a specific generator
    fn generate_for_generator(&self, database: &Database, generator: &str, output_dir: &Path) -> Result<(), Box<dyn std::error::Error>> {
        debug!("Generating output for generator: {}", generator);
        
        // Create generator-specific output directory
        let gen_output_dir = output_dir.join(generator);
        if !gen_output_dir.exists() {
            fs::create_dir_all(&gen_output_dir)?;
        }
        
        // TODO: Implement actual generators
        // For now, we'll just create a placeholder file
        let placeholder_file = gen_output_dir.join("generator_placeholder.txt");
        let placeholder_content = format!(
            "Generator: {}\nDatabase: {}\nTables: {}\n\nThis is a placeholder for the {} generator output.\n",
            generator, database.name, database.tables.len(), generator
        );
        
        fs::write(placeholder_file, placeholder_content)?;
        warn!("Generator '{}' not yet implemented - created placeholder", generator);
        
        Ok(())
    }
    
    /// Get the working directory
    pub fn get_working_dir(&self) -> &Path {
        &self.working_dir
    }
    
    /// Get the output directory
    pub fn get_output_dir(&self) -> &Path {
        &self.output_dir
    }
    
    /// Get the template directory
    pub fn get_template_dir(&self) -> Option<&Path> {
        self.template_dir.as_deref()
    }
}

/// Project compiler builder
/// 
/// This is the Rust equivalent of the Java ProjectCompilerBuilder class.
pub struct ProjectCompilerBuilder;

impl ProjectCompilerBuilder {
    /// Build a project compiler from arguments
    /// 
    /// Returns Some(compiler) on success, None if the compiler cannot be built.
    pub fn build(arguments: &JPortal2Arguments) -> Result<Option<ProjectCompiler>, Box<dyn std::error::Error>> {
        info!("Building project compiler");
        
        // Validate arguments
        if arguments.get_input_files().is_empty() && arguments.get_project_file().is_none() {
            error!("No input files or project file specified");
            return Ok(None);
        }
        
        // Create the compiler
        let mut compiler = ProjectCompiler::new(arguments.clone());
        
        // Set template directory if available
        if let Some(template_source) = arguments.get_template_source() {
            if !template_source.starts_with("http") {
                // Local template directory
                let template_dir = PathBuf::from(template_source);
                if template_dir.exists() {
                    compiler.set_template_dir(template_dir);
                }
            }
        }
        
        info!("Project compiler built successfully");
        Ok(Some(compiler))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use tempfile::TempDir;
    
    #[test]
    fn test_project_compiler_creation() {
        let arguments = JPortal2Arguments::new();
        let compiler = ProjectCompiler::new(arguments);
        
        assert!(compiler.get_working_dir().exists());
        assert_eq!(compiler.get_output_dir().file_name().unwrap(), "generated");
    }
    
    #[test]
    fn test_project_compiler_builder() -> Result<(), Box<dyn std::error::Error>> {
        let mut arguments = JPortal2Arguments::new();
        arguments.input_files.push(PathBuf::from("test.si"));
        
        let result = ProjectCompilerBuilder::build(&arguments)?;
        assert!(result.is_some());
        
        Ok(())
    }
    
    #[test]
    fn test_project_compiler_builder_no_inputs() -> Result<(), Box<dyn std::error::Error>> {
        let arguments = JPortal2Arguments::new();
        
        let result = ProjectCompilerBuilder::build(&arguments)?;
        assert!(result.is_none());
        
        Ok(())
    }
    
    #[test]
    fn test_compile_simple_database() -> Result<(), Box<dyn std::error::Error>> {
        // Create temporary directory and file
        let temp_dir = TempDir::new()?;
        let input_file = temp_dir.path().join("test.si");
        
        let database_content = r#"
            DATABASE TestDB
            SERVER "localhost"
            TABLE Users
                id int
        "#;
        
        fs::write(&input_file, database_content)?;
        
        let mut arguments = JPortal2Arguments::new();
        arguments.input_files.push(input_file);
        arguments.output_dir = Some(temp_dir.path().join("output"));
        
        let compiler = ProjectCompiler::new(arguments);
        let result = compiler.compile_all()?;
        
        assert_eq!(result, 0);
        assert!(temp_dir.path().join("output").exists());
        
        Ok(())
    }
} 