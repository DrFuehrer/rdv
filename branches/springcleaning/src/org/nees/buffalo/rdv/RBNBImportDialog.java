/*
 * Created on Mar 25, 2005
 */
package org.nees.buffalo.rdv;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;

/**
 * @author Jason P. Hanley
 */
public class RBNBImportDialog extends JDialog implements KeyEventDispatcher {
	static Log log = LogFactory.getLog(RBNBImportDialog.class.getName());
	
	RBNBImportDialog dialog;
	
	JTextField sourceNameTextField;
	
	JTextField dataFileTextField;
	JButton dataFileButton;
	JFileChooser dataFileChooser;
	File dataFile;
	
	JButton importButton;
	JButton cancelButton;
	
	JProgressBar importProgressBar;
	
	public RBNBImportDialog(JFrame owner) {
		super(owner);
		
		dialog = this;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				bindKeys();
			}
			public void windowDeactivated(WindowEvent e) {
				unbindKeys();
			}
		});

		setTitle("Data file import");
		
		initComponents();
	}
	
	private void initComponents() {
		setLayout(new GridLayout(4,1));
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new FlowLayout());
		panel.add(new JLabel("Source name: "));
		sourceNameTextField = new JTextField(15);
		panel.add(sourceNameTextField);
		getContentPane().add(panel);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.add(new JLabel("Data file: "));
		dataFileTextField = new JTextField(25);
		dataFileTextField.setEditable(false);
		panel.add(dataFileTextField);
		dataFileChooser = new JFileChooser();
		dataFileButton = new JButton("Choose data file");
		dataFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int status = dataFileChooser.showSaveDialog(dialog);
				if (status == JFileChooser.APPROVE_OPTION) {
					dataFile = dataFileChooser.getSelectedFile();
					dataFileTextField.setText(dataFile.getAbsolutePath());
				}
			}
		});
		panel.add(dataFileButton);
		getContentPane().add(panel);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout());
		importButton = new JButton("Import");
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String rbnbHostName = DataViewer.getRBNBHostName();
				final int rbnbPortNumber = DataViewer.getRBNBPort();
				final String sourceName = sourceNameTextField.getText();
				importProgressBar.setVisible(true);
				
				new Thread() {
					public void run() {
						importData(rbnbHostName, rbnbPortNumber, sourceName, dataFile, dialog);
					}
				}.start();
				
			}
		});	
		panel.add(importButton);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});		
		panel.add(cancelButton);
		getContentPane().add(panel);
		
		importProgressBar = new JProgressBar(0, 100000);
		importProgressBar.setStringPainted(true);
		importProgressBar.setValue(0);
		importProgressBar.setVisible(false);
		getContentPane().add(importProgressBar);
		
		pack();
		setVisible(true);
	}
	
	private void bindKeys() {
 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		focusManager.addKeyEventDispatcher(this);		
	}
	
	private void unbindKeys() {
 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		focusManager.removeKeyEventDispatcher(this);
	}

 	public boolean dispatchKeyEvent(KeyEvent keyEvent) {
 		int keyCode = keyEvent.getKeyCode();
 		
 		if (keyCode == KeyEvent.VK_ENTER) {
 			
 			return true;
 		} else if (keyCode == KeyEvent.VK_ESCAPE) {
 			dispose();
 			return true;
 		} else {
 			return false;
 		}
 	}
	
 	public void postStatus(double statusRatio) {
 		importProgressBar.setValue((int)(statusRatio*100000));
 	}
	
	public static boolean importData(String rbnbHostName, int rbnbPortNumber, String sourceName, File dataFile, RBNBImportDialog rbnbImportDialog) {
		String delimiters = "\t";
		int[] channels;
		int numberOfChannels;
		
		int bufferCapacity = 256;
		int bufferSize = 0;
		
		int archiveSize = bufferCapacity * 1000;
		int flushes = 0;
		
		long fileLength = dataFile.length();
		long bytesRead = 0;
		
		try {
			Source source = new Source(1, "create", archiveSize);
			source.OpenRBNBConnection(rbnbHostName + ":" + rbnbPortNumber, sourceName);
			ChannelMap cmap = new ChannelMap();
			
			BufferedReader fileReader = new BufferedReader(new FileReader(dataFile));
			String line = fileReader.readLine();
			if (line != null) {
				bytesRead += line.length() + 2;
				line = line.trim();
				String[] tokens = line.split(delimiters);
				numberOfChannels = tokens.length-1;
				channels = new int[numberOfChannels];
				for (int i=0; i<numberOfChannels; i++) {
					String channelName = tokens[i+1].trim();
					channels[i] = cmap.Add(channelName);
					cmap.PutMime(channels[i], "application/octet-stream");
				}
			} else {
				log.error("Error reading data file header");
				return false;
			}
						
			while ((line = fileReader.readLine()) != null) {
				bytesRead += line.length() + 2;
				line = line.trim();
				String[] tokens = line.split(delimiters);
				if (tokens.length != numberOfChannels+1) {
					log.error("Skipping this line of data: " + line);
					continue;
				}
				
				try {
					double time = Double.parseDouble(tokens[0].trim());
					cmap.PutTime(time, 0);
					String dataString = "";
					for (int i=0; i<numberOfChannels; i++) {
						double[] data = {Double.parseDouble(tokens[i+1].trim()) };
						dataString += data[0] + ", ";
						cmap.PutDataAsFloat64(channels[i], data);
					}
					
					if (++bufferSize == bufferCapacity) {
						source.Flush(cmap, true);
						bufferSize = 0;
						if (++flushes == archiveSize) {
							log.error("The file is too large for the archive size.");
							return false;
						}
					}
				} catch (NumberFormatException nfe) {
					log.error("Skipping this line of data: " + line);
					continue;
				}
				
				double statusRatio = ((double)bytesRead)/((double)fileLength);
				rbnbImportDialog.postStatus(statusRatio);
			}
			
			if (bufferSize > 0) {
				source.Flush(cmap, true);
			}
			
			log.info("Final status: " + ((double)bytesRead)/((double)fileLength)*100 + "%");
			
			rbnbImportDialog.postStatus(1);
			
			fileReader.close();
			
			source.Detach();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
