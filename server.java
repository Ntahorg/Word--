import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

public class server extends JFrame{
    JPanel pan = new JPanel();
    JFileChooser chooser = new JFileChooser();
    final JTextArea ta = new JTextArea();
    JScrollPane span = new JScrollPane(ta);
    boolean attacked = false;
    Vector atk = new Vector(1, 1);
    String conAddr = "", lastCon = "";
    int cons = 0;
    long nTime, oTime;

    public server(){
	makeGUI();
	startServe();
    }

    public void showError(String s){
	JOptionPane.showMessageDialog(null, s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public String getOS(){
	return System.getProperty("os.name").toLowerCase();
    }

    public void makeGUI(){
	setTitle("Server");
	setIcon();
	setBounds(200, 200, 600, 600);
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	Container con = this.getContentPane();
	con.add(pan);
	span.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	con.add(span);
	prettyGUI();
	ta.setEditable(false);
	setVisible(true);
    }

    public void setIcon(){
       	URL iconURL = null;
	if(getOS().contains("win")){
	    iconURL = getClass().getResource("icon.png");
	}else{
	    iconURL = getClass().getResource("tiles.png");
	}
	ImageIcon icon = new ImageIcon(iconURL);
	setIconImage(icon.getImage());
    }

    public void prettyGUI(){
	try{
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    SwingUtilities.updateComponentTreeUI(span);
	    SwingUtilities.updateComponentTreeUI(chooser);
	}catch(Exception e){
	    showError("Error setting look and feel.");
	}
    }

    public void startServe(){
	try{
	    String data = null;
	    if((data = readLeFile()) != null){
		ServerSocket ss = makeSocket();
		while(true){
		    Socket s = ss.accept();
		    OutputStream os = s.getOutputStream();
		    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		    PrintWriter pw = new PrintWriter(os, true);

		    /*
		     *thread this
		     */

		    if((nTime - oTime) >= 60000){
			oTime = nTime;
			cons = 0;
		    }
		    
		    if(serveData(s, br, pw, data) == 1){
			break;
		    }
		    
		    s.close();
		    nTime = Calendar.getInstance().getTimeInMillis();
		}
	    }
	}catch(Exception e){
	    showError("Error making server.");
	}
    }

    public ServerSocket makeSocket(){
	ServerSocket ss = null;
	try{
	    ss = new ServerSocket(80);
	    ss.setReuseAddress(true);
	}catch(Exception e){
	    showError("Couldn't make socket.");
	}
	return ss;
    }

    public String readLeFile(){
	String data = "", line = null;
	try{
	    int le = chooser.showOpenDialog(this);
	    File file = new File(chooser.getSelectedFile().getAbsolutePath());
	    FileReader fr = new FileReader(file);
	    BufferedReader br = new BufferedReader(fr);
	    while((line = br.readLine()) != null){
		data += line;
	    }
	}catch(Exception e){
	    showError("Error reading file.");
	}
	return data;
    }

    public int serveData(Socket s, BufferedReader br, PrintWriter pw, String data){
	int win = 0;
	char[] cbuff = new char[1024];
	int offset = 0;
	int len = cbuff.length;
	try{
	    if((attacked == true) && atk.contains(s.getRemoteSocketAddress().toString())){
		while(true){
		    br.read(); //slow read counter attack.
		}
	    }else{
		br.read(cbuff, offset, len);
		conAddr = s.getRemoteSocketAddress().toString();
		ta.append("Data from " + conAddr + ":\n");
		if(conAddr.equals(lastCon)){
		    cons += 1;
		}
		if(cons >= 100){
		    attacked = true;
		    atk.addElement(conAddr);
		}
		String readData = new String(cbuff);
		ta.append(readData);
		pw.println(data);
		pw.flush();
	    }
	}catch(Exception e){
	    win = 1;
	}
	return win;
    }
}