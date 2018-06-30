package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;

import javax.swing.SwingWorker;

/* SLAVE
spark-class org.apache.spark.deploy.worker.Worker spark://192.168.1.111:7077

Usage: Worker [options] <master>

Master must be a URL of the form spark://hostname:port

Options:
-c CORES, --cores CORES  Number of cores to use
-m MEM, --memory MEM     Amount of memory to use (e.g. 1000M, 2G)
-d DIR, --work-dir DIR   Directory to run apps in (default: SPARK_HOME/work)
-i HOST, --ip IP         Hostname to listen on (deprecated, please use --host or -h)
-h HOST, --host HOST     Hostname to listen on
-p PORT, --port PORT     Port to listen on (default: random)
--webui-port PORT        Port for web UI (default: 8081)
--properties-file FILE   Path to a custom Spark properties file.
                         Default is conf/spark-defaults.conf.
*/

public class WorkerDaemon extends SwingWorker<Void, Void> {

	private Process process;
	private Function<String, Void> console;
	private String killCommand;
	
	public WorkerDaemon(Process process, String killCommand, Function<String, Void> console) {
		this.process = process;
		this.killCommand = killCommand;
		this.console = console;
	}
	
	@Override
	protected void done() {
		try {
//			System.out.println("[W] done started");
			
			Runtime.getRuntime().exec(killCommand, null, null);
//			Thread.sleep(1000);
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

		// Launch master web UI 
		// Desktop.getDesktop().browse(URI.create("http://localhost:8081"));

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
