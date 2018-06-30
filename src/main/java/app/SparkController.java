package app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.apache.commons.lang3.SystemUtils;

public class SparkController implements ActionListener {

	/**
	 ** Kill Master(s) from CMD
	 *
	 * for /f "tokens=1" %i in ('jcmd -l ^| find "master.Master"') do (taskkill /F /PID %i)
	 *
	 ** Find all processes running on Java
	 *
	 * jcmd -l
	 *
	 */
	
	private SparkUI view;
	private Process running;
	private MasterDaemon wm = null;
	private WorkerDaemon ws = null;
	private static String execPath;
	private static String killMaster;
	private static String killWorker;
	private static Random r = new Random();
	
	/**
	 * Instantiates the Controller and writes the path where the 'spark class' bin is located.
	 * Also writes the command to kill remaining daemons. Depends on the OS.
	 * @param view
	 */
	public SparkController(SparkUI view) {
		
		this.view = view;
		
		File path = new File(System.getProperty("user.dir"));
		if(path.getAbsolutePath().endsWith("spark-app")) {
			// While on DEVELOPMENT
			execPath = path.getParent();
		}
		else {
			// While on PRODUCTION (.jar/.exe/.sh)
			execPath = path.getAbsolutePath();
		}
		
		execPath += File.separator + "bin" + File.separator;
		
		if(SystemUtils.IS_OS_WINDOWS) {
			//! Kill it from CMD
			killMaster = "cmd /c for /f \"tokens=1\" %i in ('jcmd -l ^| find \"master.Master\"') do (taskkill /F /PID %i)";
			killWorker = "cmd /c for /f \"tokens=1\" %i in ('jcmd -l ^| find \"worker.Worker\"') do (taskkill /F /PID %i)";
			
			execPath += "spark-class.cmd";
		}
		else {
			//! Kill it from BASH
			killMaster = "kill $(jcmd -l | awk │ \'/master.Master/{print $1}\')";
			killWorker = "kill $(jcmd -l | awk │ \'/worker.Worker/{print $1}\')";
			
			execPath += "spark-class";
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		try {
		
			switch(e.getActionCommand()) {
					
			case "SLAVE_RESOURCES":
				
				if(view.getSlaveEnableRes()) {
					view.enableSlaveResources();
				} else {
					view.disableSlaveResources();
				}
				
				break;
				
			case "SLAVE_START":
									
				System.out.println("[C] Slaver node started");
				
				ProcessBuilder ps;
				String masterURL = view.getSlaveNet();
				
				if(view.getSlaveEnableRes()) {
					
					// Translates the RAM slider (ints) to chunks of 500MB
					String masterRAM = Integer.toString(view.getSlaveResRAM() * 500) + "M";

					// startPort should be between 1024 and 65535 (inclusive), or 0 for a random free port
					ps = new ProcessBuilder(execPath,
							"org.apache.spark.deploy.worker.Worker",
							"--cores", Integer.toString(view.getSlaveResCPU()),
							"--memory", masterRAM,
							masterURL);
				} else {
					ps = new ProcessBuilder(execPath,
							"org.apache.spark.deploy.worker.Worker",
							masterURL);
				}
				
				
				ps.redirectErrorStream(true);
				view.clearSlaveConsole();
				view.appendSlaveConsole("Connect to http://localhost:8081");
				
				running = ps.start();
				ws = new WorkerDaemon(running, killWorker, (str) -> view.appendSlaveConsole(str));
				
				view.disableWorkerUI();
				ws.execute();
				
				break;
				
			case "SLAVE_FINISH":
				
				System.out.println("[C] I just killed the slave");
				ws.cancel(true);
				view.enableWorkerUI();
				
				break;
				
			case "MASTER_START":
				
				System.out.println("[C] Master node started");
				
				//! 65535 - 1024 = 64511 (+1 for exclusive boundary)
				int port = r.nextInt(64512) + 1024; //32470 // (alternative port)
				
				// startPort should be between 1024 and 65535 (inclusive), or 0 for a random free port
				ProcessBuilder pm = new ProcessBuilder(execPath,
						"org.apache.spark.deploy.master.Master",
						"--port",
						Integer.toString(port));
				
				pm.redirectErrorStream(true);
				view.clearMasterConsole();
				view.appendMasterConsole("Connect to http://localhost:8080");
				
				// Sets the Master SparkIP
				view.setMasterNet("spark://" + this.getLocalIP() + ":" +  port);
								
				running = pm.start();
				wm = new MasterDaemon(running, killMaster, (str) -> view.appendMasterConsole(str));
				
				view.disableMasterUI();
				wm.execute();
					
				break;
				
			case "MASTER_FINISH":
				
				System.out.println("[C] I just killed the master");
				wm.cancel(true);
				view.enableMasterUI();
				// Clears the spark path
				view.setMasterNet(null);
				
				break;
			}
			
		} catch (/*IOException*/ Exception exception) {
			exception.printStackTrace();
		}
	}
		
	@SuppressWarnings("resource")
	public String getScriptOutput(Process p) throws java.io.IOException {
	    java.util.Scanner s = new java.util.Scanner(p.getInputStream()).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	public String getLocalIP() {
		String ip = null;
		
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.err.println("ERROR! Cannot determine host address");
			e.printStackTrace();
		}
		return ip;
	}
}
