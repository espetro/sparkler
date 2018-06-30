package app;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Sparkler {

	public static void startGUI(JFrame window) {
		
		SparkUI layout = new SparkUI();
		SparkController ctrl = new SparkController(layout);
		layout.setController(ctrl);
		ImageIcon image = new ImageIcon("static/favicon.png");
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setContentPane(layout);
		window.pack();
		window.setIconImage(image.getImage());
		window.setVisible(true);
	}
	
	
	
	public static void main(String[] args) {
		final JFrame window = new JFrame("Sparkler");
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				startGUI(window);
			}
		});
	}
	
	@SuppressWarnings("resource")
	public static String getScriptOutput(Process p) throws java.io.IOException {
	    java.util.Scanner s = new java.util.Scanner(p.getInputStream()).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}

}
