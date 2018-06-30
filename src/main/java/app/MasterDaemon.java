package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;

import javax.swing.SwingWorker;

/* MASTER
  spark-class org.apache.spark.deploy.master.Master -p 31920 -c CORES -m RAM
  
  Usage: Master [options]

Options:
  -i HOST, --ip HOST     Hostname to listen on (deprecated, please use --host or -h)
  -h HOST, --host HOST   Hostname to listen on
  -p PORT, --port PORT   Port to listen on (default: 7077)
  --webui-port PORT      Port for web UI (default: 8080)
  --properties-file FILE Path to a custom Spark properties file.
                         Default is conf/spark-defaults.conf.
  
  startPort should be between 1024 and 65535 (inclusive), or 0 for a random free port
 */

public class MasterDaemon extends SwingWorker<Void, Void> {

	private Process process;
	private Function<String, Void> console;
	private String killCommand;
	
	public MasterDaemon(Process process, String killCommand, Function<String, Void> console) {
		this.process = process;
		this.killCommand = killCommand;
		this.console = console;
	}
	
	@Override
	protected void done() {
		try {
//			System.out.println("[W] done started");
					
			//! Runs on runtime as it's a one-liner script
			//! If command, better run as ProcessBuilder
			Runtime.getRuntime().exec(killCommand, null, null);
//			System.out.println("Check out bash!\n" + getScriptOutput("jcmd -l") +
//					"\n" + process.isAlive());
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
//		System.out.println("[W] done finished");
	}
	
	@Override
	protected void process(List<Void> chunks) {
		// TODO Auto-generated method stub
		super.process(chunks);
	}
	
	@Override
	protected Void doInBackground() throws Exception {

		//! Reading process
		BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		while((line = out.readLine()) != null) {
			console.apply(line);
		}
		
		return null;
	}
	
	@SuppressWarnings("resource")
	public String getScriptOutput(String command) throws java.io.IOException {
	    java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(command).getInputStream()).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}

