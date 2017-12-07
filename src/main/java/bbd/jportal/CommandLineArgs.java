package bbd.jportal;

import com.beust.jcommander.Parameter;
import java.util.List;

public class CommandLineArgs {
  //@Parameter
  //private List<String> parameters = new ArrayList<>();

  @Parameter(names = { "--log", "-l"}, description = "Logfile name")
  private String logFileName = "jportal2.log";

  @Parameter(names = { "--nubdir", "-n"}, description = "Nubdir")
  private String nubDir = "";

  @Parameter(names = { "--inputdir", "-d"}, description = "Input dir")
  private String inputDir = "";

  @Parameter(names = { "--inputfile", "-f"}, description = "Input file")
  private String inputFile = "";
  
  //arity=2 allows us to do --generator CSNetCode cs/
  @Parameter(names = { "--generator", "-d"}, description = "Generator to run", arity=2)
  private List<String> generators;

//   @Parameter(names = "-groups", description = "Comma-separated list of group names to be run")
//   private String groups;

//   @Parameter(names = "-debug", description = "Debug mode")
//   private boolean debug = false;
}