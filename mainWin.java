import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.print.*;
import javax.print.event.*;
import javax.print.attribute.*;
import javax.mail.*;
import javax.mail.internet.*;

/*Compile with javac main.java mainWin.java PrintJobWatcher.java -cp javax.mail.jar*/

/*Run with java -cp javax.mail.jar:. main*/

//for jars
//Class-Path: javax.mail.jar could be added to manifest for jar maybe
/*JAR with jar -cmf Manifest.txt Word++.jar *.class *.png*/

public class mainWin extends JFrame implements KeyListener{
    JPanel pan = new JPanel();
    final JTextArea ta = new JTextArea();
    JScrollPane span = new JScrollPane(ta);
    MenuBar mb = new MenuBar();
    MenuItem mi = new MenuItem();
    Menu file = new Menu("File");
    Menu edit = new Menu("Edit");
    Menu options = new Menu("Options");
    Menu help = new Menu("Help");
    JFileChooser fileOpener = new JFileChooser();

    MenuItem font = new MenuItem("Hack");
    MenuItem textSize = new MenuItem("Text Size");
    MenuItem compile = new MenuItem("Compile");
    MenuItem lines = new MenuItem("Lines");
    MenuItem tts = new MenuItem("TTS");
    MenuItem serverI = new MenuItem("Serve");

    MenuItem copy = new MenuItem("Copy");
    MenuItem cut = new MenuItem("Cut");
    MenuItem paste = new MenuItem("Paste");
    MenuItem find = new MenuItem("Find");
    MenuItem replace = new MenuItem("Replace");
    MenuItem regexr = new MenuItem("Regex Replace");

    MenuItem newf = new MenuItem("New");
    MenuItem save = new MenuItem("Save");
    MenuItem open = new MenuItem("Open");
    MenuItem email = new MenuItem("Email");
    MenuItem print = new MenuItem("Print");
    MenuItem closer = new MenuItem("Exit");

    MenuItem aboutClip = new MenuItem("About Clipboard");
    MenuItem aboutServe = new MenuItem("About Server");
    MenuItem about = new MenuItem("About Word++");

    int saved = 0, hacked = 0, compiled = 0, emailed = 0, currentTextSize = 12;
    String clip = "", cmd = "", addr, passwrd;

    mainWin(String args[]){
	super("Word++");
	makeWindow(args);
    }

    public void showError(String s){
	JOptionPane.showMessageDialog(null, s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void makeWindow(String args[]){
	setIcon();
	makeWindowHeader();
	addFileItems();
	addEditItems();
	addOptionsItems();
	addHelpItems();
	makeActionListeners();
	setBounds(100, 100, 500, 500);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	Container con = this.getContentPane();
	con.add(pan);
	span.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	con.add(span);
	ta.setFont(new Font("Serif", Font.PLAIN, currentTextSize));
	prettyGUI();
	setVisible(true);
	openArgs(args);
    }

    public void prettyGUI(){
	try{
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    SwingUtilities.updateComponentTreeUI(span);
	    SwingUtilities.updateComponentTreeUI(fileOpener);
	}catch(Exception e){
	    showError("Error setting look and feel.");
	}
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

    public void makeWindowHeader(){
	setMenuBar(mb);
	mb.add(file);
	mb.add(edit);
	mb.add(options);
	mb.add(help);
    }

    public void addFileItems(){
        file.add(newf);
	file.add(save);
	file.add(open);
	file.add(email);
	file.add(print);
	file.add(closer);
    }

    public void addEditItems(){
        edit.add(copy);
	edit.add(cut);
	edit.add(paste);
	edit.add(find);
	edit.add(replace);
	edit.add(regexr);
    }

    public void addOptionsItems(){
	options.add(font);
	options.add(textSize);
	options.add(compile);
	options.add(lines);
	if(getOS().contains("win")){
	    options.add(tts);
	}
        options.add(serverI);
    }

    public void addHelpItems(){
	help.add(aboutClip);
	help.add(aboutServe);
	help.add(about);
    }

    public void openFile(){
	int option = fileOpener.showOpenDialog(this);
	String data = "", line = null;
	BufferedReader br = null;
	try{
	    br = new BufferedReader(new FileReader(fileOpener.getSelectedFile().getPath()));
	    ta.setText("");
	    while((line = br.readLine()) != null){
		ta.append(line);
		ta.append("\n");
	    }
	    br.close();
	    setTitle(fileOpener.getSelectedFile().getName());
	    saved = 1;
	}catch(Exception e){
	    showError("Error Reading File.");
	}
    }

    public void openArgs(String args[]){
	try{
	    String file = args[0], data = "", line = null;
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    ta.setText("");
	    while((line = br.readLine()) != null){
		ta.append(line);
		ta.append("\n");
	    }
	    br.close();
	    File files = new File(file);
	    fileOpener.setSelectedFile(files);
	    setTitle(fileOpener.getSelectedFile().getName());
	    saved = 1;
	}catch(Exception e){
	    //probably no args
	}
    }

    public void saveFileAs(){
	int option = fileOpener.showSaveDialog(this);
	try{
	    BufferedWriter bw = new BufferedWriter(new FileWriter(fileOpener.getSelectedFile().getPath()));
	    bw.write(ta.getText());
	    bw.close();
	    saved = 1;
	}catch(Exception e){
	    //user may click cancle so we should't do anything for this error
	}
    }

    public void saveFile(){
	try{
	    BufferedWriter bw = new BufferedWriter(new FileWriter(fileOpener.getSelectedFile().getPath()));
	    bw.write(ta.getText());
	    bw.close();
	}catch(Exception e){
	    showError("Error writing to file.");
	}
    }

    public void copym(){
	clip = ta.getSelectedText();
    }

    public void cutm(){
	clip = ta.getSelectedText();
	ta.replaceSelection("");
    }

    public void pastem(){
	ta.replaceSelection(clip);
    }

    public void printm(){
	if(saved == 0){
	    showError("Can't print file that does not exist!");
	}else{
	    try{
		DocFlavor df = DocFlavor.INPUT_STREAM.AUTOSENSE;
		PrintRequestAttributeSet attrib = new HashPrintRequestAttributeSet();
		PrintService[] ps = PrintServiceLookup.lookupPrintServices(df, attrib); 
		DocPrintJob pj;
		Doc d;
		PrintJobWatcher pjw;
		FileInputStream fis = new FileInputStream(fileOpener.getSelectedFile());
		
		/*
		 * lol. Print from all printers found XD
		 */
		
		for(PrintService p : ps){
		    pj = p.createPrintJob();
		    d = new SimpleDoc(fis, df, null);
		    pjw = new PrintJobWatcher(pj);
		    pj.print(d, attrib);
		}
	    }catch(Exception e){
		showError("Error printing.");
	    }
	}
    }

    public void setLeFontSize(){
	int fon = currentTextSize;
        try{
	    fon = Integer.parseInt(JOptionPane.showInputDialog("Enter font size: "));
	    ta.setFont(new Font("Serif", Font.PLAIN, fon));
	}catch(Exception e){
	    showError("Error setting font.");
        }
    }

    public String getOS(){
	return System.getProperty("os.name").toLowerCase();
    }

    public void compileFile(){
	if(saved == 0){
	    JOptionPane.showMessageDialog(null, "Save file first.", "Error", JOptionPane.INFORMATION_MESSAGE);
	}else{
	    if(compiled != 1){
		String ex = JOptionPane.showInputDialog("Enter compiler and flags: ");
		cmd = ex + " " + fileOpener.getSelectedFile();
		try{
		    if(!getOS().contains("win")){
			System.out.println("Executing command...");
			Process p = Runtime.getRuntime().exec(cmd);
		    }else{
			Process p = Runtime.getRuntime().exec("cmd /c " + cmd);
		    }
		    compiled = 1;
		}catch(Exception e){
		    JOptionPane.showMessageDialog(null, "Command execution failed!");
		}
	    }else{
		try{
		    if(!getOS().contains("win")){
			Process p = Runtime.getRuntime().exec(cmd);
		    }else{
			Process p = Runtime.getRuntime().exec("cmd /c " + cmd);
		    }
		}catch(Exception ex){
		    showError("Recompilation failed!");
		}
	    }
	}
    }

    public String regexReplace(String str, String ex, String rpStr){
	String le = ta.getText();
	try{
	    le = str.replaceAll(ex, rpStr);
	}catch(Exception e){
	    showError("Error evaluating regex.");
	}
	return le;
    }

    public void normReplace(){
	String replace = JOptionPane.showInputDialog("Replace:");
	String replacer = JOptionPane.showInputDialog("With:");
	try{
	    ta.setText(ta.getText().replaceAll(replace, replacer));
	}catch(Exception e){
	    //user could've pressed cancle
	}
    }

    public void findm(){
	try{
	    Highlighter h = ta.getHighlighter();
	    Highlighter.HighlightPainter hp = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
	    String finds = JOptionPane.showInputDialog("Search:");
	    if(!finds.equals(null) && !finds.equals("")){
		h.removeAllHighlights();
		int x1 = ta.getText().indexOf(finds);
		int x2 = x1 + finds.length();
		h.addHighlight(x1, x2, hp);
	    }else{
		h.removeAllHighlights();
	    }
	}catch(Exception e){
	    //user couldv'e pressed cancel
	}
    }

    public void hacklefuck(){
	if(hacked == 0){
	    ta.setBackground(Color.BLACK);
	    ta.setForeground(Color.GREEN);
	    hacked = 1;
	}else{
	    ta.setBackground(Color.WHITE);
	    ta.setForeground(Color.BLACK);
	    hacked = 0;
	}
    }

    public void displayLinesOfCode(){
	JOptionPane.showMessageDialog(null, "Lines of code: " + ta.getLineCount());
    }
    
    public void emailm(){
	final String address, pass;
	if(emailed == 0){
	    address = JOptionPane.showInputDialog("Enter your email address:");  
	    pass = JOptionPane.showInputDialog("Enter your email password:");
	    addr = address;
	    passwrd = pass;
	}else{
	    address = addr;
	    pass = passwrd;
	}
       
	Properties props = new Properties();

	props.put("mail.smtp.auth", "true");
	props.put("mail.smtp.starttls.enable", "true");
	props.put("mail.smtp.host", "smtp.gmail.com");
	props.put("mail.smtp.port", "587");

	Session ses = Session.getInstance(props,
					  new javax.mail.Authenticator(){
					      protected PasswordAuthentication getPasswordAuthentication(){
						  return new PasswordAuthentication(address, pass);
					      }
					  });
	
	String toAddress = JOptionPane.showInputDialog("Enter who you would like to email:");

	try{
	    Message m = new MimeMessage(ses);
	    m.setFrom(new InternetAddress(address));
	    m.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
	    m.setSubject("Word++ Email");
	    m.setText(ta.getText()); //user may not save since just email
	    Transport.send(m);
	    emailed = 1;
	}catch(Exception e){
	    showError("Error sending email.");
	}
    }
    

    public void ttsm(){
	try{
	    File file = new File("tts27328.vbs");
	    FileWriter fw = new FileWriter(file);
	    BufferedWriter bw = new BufferedWriter(fw);
	    String contents = "Set voice = CreateObject(\"SAPI.SpVoice\")\nvoice.Speak \"" + ta.getText() + "\"";
	    bw.write(contents);
	    bw.close();
	    Process p = Runtime.getRuntime().exec("cmd.exe /c tts27328.vbs");
	    file.delete();
	}catch(Exception e){
	    showError("Error speaking.");
	}
    }

    public void displayAboutClip(){
	JOptionPane.showMessageDialog(null, "The copy and paste in this\nprogram are an additional clipboard.", "About Clipboard", JOptionPane.INFORMATION_MESSAGE);
    }

    public void displayAboutServem(){
	JOptionPane.showMessageDialog(null, "My gift to any HTML/PHP/Javascript/anything but Ruby programmers.\nJust write your script and you can serve it via HTTP.\nYou may need to it run as root.", "About Server", JOptionPane.INFORMATION_MESSAGE);
    }

    public void displayAbout(){
	JOptionPane.showMessageDialog(null, "Word++ is an IDE for forgetful\n\theckers that use regex.", "About Word++", JOptionPane.INFORMATION_MESSAGE);
    }

    public ActionListener newwer = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		ta.setText("");
		saved = 0;
	    }
	};

    public ActionListener openner = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		openFile();
	    }
	};

    public ActionListener saver = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		saverm();
	    }
	};
    
    public void saverm(){
	if(saved == 0){
	    saveFileAs();
	}else{
	    saveFile();
	}
    }

    public ActionListener emailr = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		emailm();
	    }
	};

    public ActionListener printer = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		printm();
	    }
	};

    public ActionListener closerAction = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		System.exit(0);
	    }
	};

    public ActionListener copier = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		copym();
	    }
	};

    public ActionListener cutter = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		cutm();
	    }
	};

    public ActionListener paster = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		pastem();
	    }
	};

    public ActionListener findr = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		findm();
	    }
	};

    public ActionListener replacer = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		normReplace();
	    }
	};

    public ActionListener regexReplacer = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		String ex = JOptionPane.showInputDialog("Enter regex: ");
		String rpStr = JOptionPane.showInputDialog("Enter replace text: ");
		ta.setText(regexReplace(ta.getText(), ex, rpStr));
	    }
	};

    public ActionListener hecker = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		hacklefuck();
	    }
	};
 
    public ActionListener setTextSize = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		setLeFontSize();
	    }
	};

    public ActionListener comp = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		compileFile();
	    }
	};

    public ActionListener liner = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		displayLinesOfCode();
	    }
	};

    public ActionListener ttsr = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		ttsm();
	    }
	};

    public ActionListener serverr = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		Thread t = new Thread(new Runnable(){
			public void run(){
			    server s = new server();
			}
		    });
		t.start();
	    }
	};

    public ActionListener aboutCliper = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		displayAboutClip();
	    }
	};

    public ActionListener aboutServerr = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		displayAboutServem();
	    }
	};

    public ActionListener aboutr = new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		displayAbout();
	    }
	};

    public void makeActionListeners(){
	ta.addKeyListener(this);
	newf.addActionListener(newwer);
	open.addActionListener(openner);
	save.addActionListener(saver);
	email.addActionListener(emailr);
	print.addActionListener(printer);
	closer.addActionListener(closerAction);

	copy.addActionListener(copier);
	cut.addActionListener(cutter);
	paste.addActionListener(paster);
	find.addActionListener(findr);
	replace.addActionListener(replacer);
	regexr.addActionListener(regexReplacer);

	font.addActionListener(hecker);
	textSize.addActionListener(setTextSize);
	compile.addActionListener(comp);
	lines.addActionListener(liner);
	tts.addActionListener(ttsr);
	serverI.addActionListener(serverr);

	aboutClip.addActionListener(aboutCliper);
	aboutServe.addActionListener(aboutServerr);
	about.addActionListener(aboutr);
    }

    public void keyPressed(KeyEvent ke){
	switch(ke.getKeyCode()){
	case KeyEvent.VK_ESCAPE:
	    System.exit(0);
	    break;
	case KeyEvent.VK_ALT:
	    saverm();
	    break;
	}
    }
    public void keyReleased(KeyEvent ke){}
    public void keyTyped(KeyEvent ke){}
}
