package app;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.SystemUtils;

public class SparkUI extends JTabbedPane {
	private static final long serialVersionUID = -5072079924861147311L;

	private final int CPU = getAvailableCPU();
	private final int RAM = (int) getFreeRAM() * 2; // in chunks of 1/2GB
	
	private JLabel slave_net_label = new JLabel("Connect to master:");
	private JTextField slave_net = new JTextField(15);
	private JLabel slave_res_label = new JLabel("Resources");
	private JTextArea slave_console = new JTextArea(10,30);
	private JCheckBox slave_res_enable = new JCheckBox("Modify");
	private JLabel slave_res_cpu1 = new JLabel("CPU");
	private JSlider slave_res_cpu2 = new JSlider(1, CPU, 2);
	private JLabel slave_res_ram1 = new JLabel("RAM");
	private JSlider slave_res_ram2 = new JSlider(1, RAM, 1);
	private JButton slave_start = new JButton("Start");
	private JButton slave_finish = new JButton("Finish");
	
	private JLabel master_net_label = new JLabel("Master IP: ");
	private JTextField master_net = new JTextField(15);
	private JLabel master_res_label = new JLabel("Resources");
	private JTextArea master_console = new JTextArea(10,30);
	private JButton master_start = new JButton("Start");
	private JButton master_finish = new JButton("Finish");
	
	private JScrollPane master_scroll = new JScrollPane(master_console,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private JScrollPane slave_scroll = new JScrollPane(slave_console,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
	private JLabel os_info = new JLabel("");
	
	public SparkUI() {
		this.setTabPlacement(JTabbedPane.TOP);
		os_info.setText(SystemUtils.OS_NAME);
		
		master_net.setEditable(false);
		master_console.setEditable(false);
		master_console.setLineWrap(true);
		master_console.setText("> Console ...");
		slave_console.setEditable(false);
		slave_console.setText("> Console ...");
		slave_console.setLineWrap(true);
		slave_res_enable.setSelected(false);
		slave_res_cpu2.setMajorTickSpacing(1);
		slave_res_cpu2.setPaintLabels(true);
		slave_res_cpu2.setEnabled(false);
		slave_res_ram2.setMajorTickSpacing(1);
		slave_res_ram2.setPaintLabels(true);
		slave_res_ram2.setLabelTable(getRAMTable());
		slave_res_ram2.setEnabled(false);
		
		JPanel slave = setPane(slave_net_label, slave_net,
							   slave_res_label, slave_scroll,
							   slave_res_enable, slave_res_cpu1, slave_res_cpu2,
							   slave_res_ram1, slave_res_ram2, slave_start, slave_finish,
							   os_info);
		
		this.addTab("Slave", slave);
		
		JPanel master = setPane(master_net_label, master_net,
								master_res_label, master_scroll,
								null, null, null, null, null,
								master_start, master_finish,
								os_info);
		
		this.addTab("Master", master);
	}
	
	public void setController(SparkController ctlr) {
		slave_start.addActionListener(ctlr);
		slave_finish.addActionListener(ctlr);
		slave_start.setActionCommand("SLAVE_START");
		slave_finish.setActionCommand("SLAVE_FINISH");
		
		slave_res_enable.addActionListener(ctlr);
		slave_res_enable.setActionCommand("SLAVE_RESOURCES");
		
		master_start.addActionListener(ctlr);
		master_finish.addActionListener(ctlr);
		master_start.setActionCommand("MASTER_START");
		master_finish.setActionCommand("MASTER_FINISH");
	}
	
	/****** Helper Methods ******/
	
	private JPanel setResource(JLabel name, JComponent input) {
		JPanel res = new JPanel(new BorderLayout());
		res.add(input, BorderLayout.PAGE_START);
		res.add(name, BorderLayout.PAGE_END);
		return res;
	}
		
	private JPanel setPane(JLabel net_label, JTextField net_text, JLabel res_label, JScrollPane console,
			JCheckBox enable_res, JLabel cpu_label, JSlider cpu_input, JLabel ram_label, JSlider ram_input,
			JButton start_button, JButton finish_button, JLabel os_info) {
		
		JPanel layout = new JPanel(new BorderLayout());
		
		JPanel net = new JPanel(new FlowLayout());
		net.add(net_label);
		net.add(net_text);

		layout.add(net, BorderLayout.NORTH);
		
		JPanel res = new JPanel(new BorderLayout());
		res.add(console, BorderLayout.PAGE_START);
		
		// Distincts between Master and Worker panel
		if(cpu_label != null) {
			
			res.add(res_label, BorderLayout.CENTER);
			JPanel res_set = new JPanel(new BorderLayout());
			res_set.add(enable_res, BorderLayout.WEST);
			res_set.add(setResource(cpu_label, cpu_input), BorderLayout.CENTER);
			res_set.add(setResource(ram_label, ram_input), BorderLayout.EAST);
			res.add(res_set, BorderLayout.PAGE_END);
		}
		
		layout.add(res, BorderLayout.CENTER);

		JPanel start = new JPanel(new FlowLayout());
		start.add(start_button);
		start.add(finish_button);
		start.add(os_info);
		
		layout.add(start, BorderLayout.SOUTH);
		
		return layout;
	}
	
	private int getAvailableCPU() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	/**
	 * Splits the RAM slider into chunks of 1/2 GB
	 * It takes the Math.floor of available RAM, subtracts 1GB
	 * and chunks the rest in halfs
	 * The computation is done on the controller side
	 * @return
	 */
	private Hashtable<Integer, JLabel> getRAMTable() {
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		
		// maps each step 'i' to a value 'i' times 0.5
		for(double i = 0.5, j = 1 ; i <= (double) RAM; i += 0.5, j++) {
			// 1 * 0.5 = 0.5; 2 * 0.5 = 1 [... later on the controller]
			labelTable.put((int) j, new JLabel(Double.toString(i)));
		}
		
		return labelTable;
	}
	
	/**
	 * Checks both free physical and virtual memory
	 * Minimum working RAM is 500MB (For working on RPIs)
	 * @return
	 */
	private double getFreeRAM() {
		double total = 0.5;
		String mem1 = null, mem2 = null;
		
		try {
			
			if(SystemUtils.IS_OS_WINDOWS) {
				
				mem1 = runScript("wmic OS get FreePhysicalMemory");
				mem2 = runScript("wmic OS get FreeVirtualMemory");
				
			} else {
				
				mem1 = runScript("free | awk '/^Mem:/{print $4}'");
				mem2 = runScript("free | awk '/^Swap:/{print $4}'");
			}
			
			// Ensure that total RAM is at least 0.5
			mem1 = mem1.replaceAll("\\D+", "");
			mem2 = mem2.replaceAll("\\D+", "");
			total = ((Integer.parseInt(mem1) + Integer.parseInt(mem2)) / Math.pow(1024, 2));
			
		} catch(IOException e) {	
			e.printStackTrace();
		}
		
		return total;
	}
	
	/**
	 * 
	 * @param cmd
	 * @return
	 * @throws java.io.IOException
	 */
	@SuppressWarnings("resource")
	public static String runScript(String cmd) throws java.io.IOException {
	    java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	/****** Disablers / Enablers ******/
	
	public void disableMasterUI() {
		master_start.setEnabled(false);
	}
	
	public void enableMasterUI() {
		master_start.setEnabled(true);
	}
	
	public void disableWorkerUI() {
		slave_start.setEnabled(false);
	}
	
	public void enableWorkerUI() {
		slave_start.setEnabled(true);
	}
	
	public void enableSlaveResources() {
		slave_res_cpu2.setEnabled(true);
		slave_res_ram2.setEnabled(true);
	}
	
	public void disableSlaveResources() {
		slave_res_cpu2.setEnabled(false);
		slave_res_ram2.setEnabled(false);
	}
	
	/***** Getters ******/
	
	public String getSlaveNet() {
		return slave_net.getText();
	}
	
	public int getSlaveResCPU() {
		return slave_res_cpu2.getValue();
	}
	
	public int getSlaveResRAM() {
		return slave_res_ram2.getValue();
	}
	
	public String getMasterNet() {
		return master_net.getText();
	}
		
	public boolean getSlaveEnableRes() {
		return slave_res_enable.isSelected();
	}
	
	/****** Setters ******/
	
	public void setMasterNet(String ip) {
		master_net.setText(ip);
	}
	
	public Void clearSlaveConsole() {
		slave_console.setText(null);
		return null;
	}
	
	public Void appendSlaveConsole(String info) {
		slave_console.append(info + "\n");
		return null;
	}
	
	public Void clearMasterConsole() {
		master_console.setText(null);
		return null;
	}
	
	public Void appendMasterConsole(String info) {
		master_console.append(info + "\n");
		return null;
	}
}
