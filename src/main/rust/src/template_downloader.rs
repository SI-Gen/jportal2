use std::fs;
use std::path::{Path, PathBuf};
use log::{info, warn, error, debug};
use crate::arguments::JPortal2Arguments;

/// Template downloader for JPortal2
/// 
/// This is the Rust equivalent of the Java TemplateDownloader class.
/// Handles downloading templates from URLs or copying from local directories.
pub struct TemplateDownloader {
    /// Default template directory
    default_template_dir: PathBuf,
}

impl TemplateDownloader {
    /// Create a new template downloader
    pub fn new() -> Self {
        Self {
            default_template_dir: PathBuf::from("templates"),
        }
    }
    
    /// Download or copy templates based on the arguments
    /// 
    /// Returns 0 on success, positive error code on failure.
    pub fn download_templates(&self, arguments: &JPortal2Arguments) -> Result<i32, Box<dyn std::error::Error>> {
        if let Some(template_source) = arguments.get_template_source() {
            info!("Processing template source: {}", template_source);
            
            if template_source.starts_with("http://") || template_source.starts_with("https://") {
                // Download from URL
                self.download_from_url(template_source, arguments)
            } else {
                // Copy from local directory
                self.copy_from_local(template_source, arguments)
            }
        } else {
            // Check if default template directory exists
            if self.default_template_dir.exists() {
                info!("Using default template directory: {:?}", self.default_template_dir);
                Ok(0)
            } else {
                warn!("No template source specified and default template directory does not exist");
                // This is not necessarily an error - we can proceed without templates
                Ok(0)
            }
        }
    }
    
    /// Download templates from a URL
    fn download_from_url(&self, url: &str, arguments: &JPortal2Arguments) -> Result<i32, Box<dyn std::error::Error>> {
        info!("Downloading templates from URL: {}", url);
        
        // For now, we'll implement a basic HTTP download
        // In a production system, you might want to use a more sophisticated HTTP client
        
        let target_dir = self.get_target_template_dir(arguments);
        
        // Create target directory if it doesn't exist
        if !target_dir.exists() {
            fs::create_dir_all(&target_dir)?;
            debug!("Created template directory: {:?}", target_dir);
        }
        
        // TODO: Implement actual HTTP download
        // For now, we'll just log that we would download
        warn!("HTTP template download not yet implemented. URL: {}", url);
        warn!("Please manually download templates to: {:?}", target_dir);
        
        // Return success for now - this allows the rest of the system to work
        // even without template downloading implemented
        Ok(0)
    }
    
    /// Copy templates from a local directory
    fn copy_from_local(&self, source_path: &str, arguments: &JPortal2Arguments) -> Result<i32, Box<dyn std::error::Error>> {
        let source_dir = Path::new(source_path);
        
        if !source_dir.exists() {
            error!("Template source directory does not exist: {}", source_path);
            return Ok(1);
        }
        
        if !source_dir.is_dir() {
            error!("Template source is not a directory: {}", source_path);
            return Ok(1);
        }
        
        let target_dir = self.get_target_template_dir(arguments);
        
        info!("Copying templates from {} to {:?}", source_path, target_dir);
        
        // Create target directory if it doesn't exist
        if !target_dir.exists() {
            fs::create_dir_all(&target_dir)?;
            debug!("Created template directory: {:?}", target_dir);
        }
        
        // Copy all files and directories from source to target
        self.copy_directory_recursive(source_dir, &target_dir)?;
        
        info!("Template copy completed successfully");
        Ok(0)
    }
    
    /// Get the target directory for templates
    fn get_target_template_dir(&self, arguments: &JPortal2Arguments) -> PathBuf {
        if let Some(working_dir) = arguments.get_working_dir() {
            working_dir.join("templates")
        } else {
            self.default_template_dir.clone()
        }
    }
    
    /// Recursively copy a directory and all its contents
    fn copy_directory_recursive(&self, source: &Path, target: &Path) -> Result<(), Box<dyn std::error::Error>> {
        if !target.exists() {
            fs::create_dir_all(target)?;
        }
        
        for entry in fs::read_dir(source)? {
            let entry = entry?;
            let source_path = entry.path();
            let file_name = entry.file_name();
            let target_path = target.join(file_name);
            
            if source_path.is_dir() {
                // Recursively copy subdirectory
                self.copy_directory_recursive(&source_path, &target_path)?;
            } else {
                // Copy file
                fs::copy(&source_path, &target_path)?;
                debug!("Copied file: {:?} -> {:?}", source_path, target_path);
            }
        }
        
        Ok(())
    }
    
    /// Check if templates are available
    pub fn templates_available(&self, arguments: &JPortal2Arguments) -> bool {
        let template_dir = self.get_target_template_dir(arguments);
        template_dir.exists() && template_dir.is_dir()
    }
    
    /// Get the template directory path
    pub fn get_template_dir(&self, arguments: &JPortal2Arguments) -> PathBuf {
        self.get_target_template_dir(arguments)
    }
}

impl Default for TemplateDownloader {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::fs;
    use tempfile::TempDir;
    
    #[test]
    fn test_template_downloader_creation() {
        let downloader = TemplateDownloader::new();
        assert_eq!(downloader.default_template_dir, PathBuf::from("templates"));
    }
    
    #[test]
    fn test_copy_from_local() -> Result<(), Box<dyn std::error::Error>> {
        // Create temporary directories for testing
        let temp_dir = TempDir::new()?;
        let source_dir = temp_dir.path().join("source");
        let target_dir = temp_dir.path().join("target");
        
        // Create source directory with some files
        fs::create_dir_all(&source_dir)?;
        fs::write(source_dir.join("template1.txt"), "Template 1 content")?;
        fs::write(source_dir.join("template2.txt"), "Template 2 content")?;
        
        // Create subdirectory
        let sub_dir = source_dir.join("subdir");
        fs::create_dir_all(&sub_dir)?;
        fs::write(sub_dir.join("template3.txt"), "Template 3 content")?;
        
        let downloader = TemplateDownloader::new();
        let mut arguments = JPortal2Arguments::new();
        arguments.working_dir = Some(target_dir.parent().unwrap().to_path_buf());
        
        // Test copying
        let result = downloader.copy_from_local(source_dir.to_str().unwrap(), &arguments)?;
        assert_eq!(result, 0);
        
        // Verify files were copied
        let template_dir = target_dir.join("templates");
        assert!(template_dir.exists());
        assert!(template_dir.join("template1.txt").exists());
        assert!(template_dir.join("template2.txt").exists());
        assert!(template_dir.join("subdir").join("template3.txt").exists());
        
        Ok(())
    }
    
    #[test]
    fn test_nonexistent_source_directory() {
        let downloader = TemplateDownloader::new();
        let arguments = JPortal2Arguments::new();
        
        let result = downloader.copy_from_local("/nonexistent/path", &arguments);
        assert!(result.is_ok());
        assert_eq!(result.unwrap(), 1); // Error code for missing directory
    }
} 