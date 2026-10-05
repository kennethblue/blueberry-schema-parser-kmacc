package com.bluerobotics.blueberry.schema.parser.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import com.bluerobotics.blueberry.schema.parser.parsing.BlueberrySchemaParser;
import com.bluerobotics.blueberry.schema.parser.parsing.Constants;
import com.bluerobotics.blueberry.schema.parser.parsing.ParserIssue;
import com.bluerobotics.blueberry.schema.parser.parsing.ParserIssue.Type;
import com.bluerobotics.blueberry.schema.parser.parsing.ParserIssueLogger;
import com.bluerobotics.blueberry.schema.parser.parsing.SchemaParserException;
import com.bluerobotics.blueberry.schema.parser.tokens.Coord;
import com.bluerobotics.blueberry.schema.parser.writers.CWriter;
import com.bluerobotics.blueberry.schema.parser.writers.JavaWriter;
import com.bluerobotics.blueberry.schema.parser.writers.PrettyWriter;
import com.starfishmedical.settings.Settings;
import com.starfishmedical.settings.SettingsDialog;
import com.starfishmedical.settings.SettingsTableCellEditor;
import com.starfishmedical.settings.SettingsTableCellRenderer;
import com.starfishmedical.settings.SettingsTableModel;
import com.starfishmedical.sfdq.actions.ActionManager;
import com.starfishmedical.sfdq.gui.FrameResizer;
import com.starfishmedical.sfdq.gui.ToolBar;
import com.starfishmedical.utils.ResourceTools;
import com.starfishmedical.utils.UtilMethods;

/**
 * This is a main GUI class. It starts up a simple dialog for running the parser
 */
public class BlueberrySchemaParserGui implements Constants {
	private final Settings m_settings;
	private final JFrame m_frame;
	
//	private MenuBar m_menuBar;
	
	public interface TextOutput {
		void add(String s, ParserIssue.Type t);
	}
	private final ActionManager m_actions = new ActionManager(RESOURCE_PATH);
	private final JTextPane m_text = new JTextPane();
	private final Style STYLE_RED = addStyle("red", Color.RED);
	private final Style STYLE_BLUE = addStyle("blue", Color.BLUE);
	private final Style STYLE_ORANGE = addStyle("orange", new Color(220, 100, 0));
	private final Style STYLE_GREEN = addStyle("green", Color.GREEN);
	private final Style STYLE_BLACK = addStyle("black", Color.BLACK);
	private final Style STYLE_MAGENTA = addStyle("magenta", Color.MAGENTA);
	private final ParserIssueLogger m_log = new ParserIssueLogger((s,t) -> {
		
		append(s,t);
	});

	
	private BlueberrySchemaParser m_parser = new BlueberrySchemaParser(m_log);
	private void append(String s) {
		append(s, null);
	}
	private void append(String s, ParserIssue.Type t) {
		SwingUtilities.invokeLater(() -> {
			String s2 = s;
			if(s2.charAt(s2.length() - 1) != '\n') {
				s2 += "\n";
			}
			Document d = m_text.getDocument();
			Style style = STYLE_BLACK;
			if(t != null) {
				switch(t) {
				case ERROR:
					style = STYLE_RED;
					break;
				case NOTE:
					style = STYLE_BLUE;
					break;
				case WARNING:
					style = STYLE_ORANGE;
					break;
				case SKIPPED:
					style = STYLE_MAGENTA;
					break;
				default:
					style = STYLE_BLACK;
					break;
				}
			}
			
			try {
				d.insertString(d.getLength(), s2, style);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			m_text.append("\n");	
//			m_text.repaint();
//			System.out.println(s2);
		});
	}
	
	
	private Style addStyle(String string, Color c) {
		Style result = m_text.addStyle("Style red", null);
		StyleConstants.setForeground(result, c);
		return result;
	}


	/**
	 * 
	 * Constructs the GUI, maps actions
	 * @param s - a settings object for persistence
	 */
	public BlueberrySchemaParserGui(Settings s) {
		m_settings = s;
		JFrame f = new JFrame();
		f.setIconImage(BLUEBERRY_LOGO);
		m_frame = f;
		f.setUndecorated(true);

		Container cp = f.getContentPane();
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createLineBorder(COLOR_LOGO_LILAC, 3));
//		p.setBorder(BorderFactory.createRaisedBevelBorder());
		FrameResizer resizer = new FrameResizer(p, m_frame);
		cp.add(p);
		cp = p;

		cp.setLayout(new BorderLayout());
		
		

		int x = s.getInt(Key.APP_POS_X);
		int y = s.getInt(Key.APP_POS_Y);
		int w = s.getInt(Key.APP_WIDTH);
		int h = s.getInt(Key.APP_HEIGHT);
		f.setLocation(x, y);
		f.setSize(w, h);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		f.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				exit();
			}
		});
		f.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int w = e.getComponent().getWidth();
				int h = e.getComponent().getHeight();
				w = w < 200 ? 200 : w;
				h  = h < 200 ? 200 : h;
				s.set(Key.APP_WIDTH, w);
				s.set(Key.APP_HEIGHT, h);
			}
			@Override
			public void componentMoved(ComponentEvent e) {
				int x = e.getComponent().getX();
				int y = e.getComponent().getY();
				s.set(Key.APP_POS_X, x);
				s.set(Key.APP_POS_Y, y);
			}
		});
		registerActions();


		ToolBar toolbar = new ToolBar() {
			private static final long serialVersionUID = 1L;

			//add initializer to abstract class
			{
				JButton comp = new JButton("<html><p>"+ APP_NAME + "</p>"+VERSION+"</p></html>");
//				comp.setIcon(new ImageIcon(BLUEBERRY_LOGO.getScaledInstance(-1, 49, Image.SCALE_SMOOTH)));
				comp.setIcon(UtilMethods.makeIcon(BLUEBERRY_LOGO, -1, 48));
				comp.setAlignmentY(0.5f);
				comp.setVerticalAlignment(SwingConstants.CENTER);
//				comp.setIcon(BLUEBERRY_ICON);
//				comp.setIcon(UtilMethods.makeIcon(BLUEBERRY_LOGO, 48));
				comp.setRequestFocusEnabled(false);//this stops buttons from taking focus
				comp.setBackground(Color.BLUE);//don't know why this works, but it keeps the rollover from showing a weird gradient
				comp.setBorder(new EmptyBorder(5,5,5,5));
				comp.setOpaque(false);
				Dimension d = new Dimension(300,50);
//				comp.setPreferredSize(d);
				comp.setMaximumSize(d);
				comp.setFocusable(false);
				add(comp);
				setFloatable(false);

				addItem(m_actions, ActionInfos.PARSE_SCHEMA);
				addItem(m_actions, ActionInfos.GENERATE_C);
				addItem(m_actions, ActionInfos.GENERATE_JAVA);
				addItem(m_actions, ActionInfos.CLEAN_SCHEMA);
				addItem(m_actions, ActionInfos.COPY_ISSUES);
				addItem(m_actions, ActionInfos.TOOLS_CLEAR_OUTPUT);
				addItem(m_actions, ActionInfos.TOOLS_RESET_KEYS);

				addSeparator(new Dimension(20,20));
//				addItem( m_actions, ActionInfos.HELP);

				addGlue();
				addItem(m_actions, ActionInfos.MINIMIZE);
				addItem(m_actions, ActionInfos.NORMALIZE);
				addItem(m_actions, ActionInfos.MAXIMIZE);
				addItem(m_actions, ActionInfos.EXIT);

			}
		};

		resizer.addMoveComponent(toolbar);
		cp.add(toolbar, BorderLayout.NORTH);
		Key[] keys = new Key[] {
				Key.VERSION_4_NOT_3,
				Key.SCHEMA_DIRECTORY,
				Key.JAVA_DIRECTORY,
				Key.JAVA_PACKAGE_NAME,
				Key.C_DIRECTORY,
				Key.CODE_HEADER_FILE_PATH,
				Key.IDL_HEADER_FILE_PATH,
		};

		JTable setTable = new JTable(new SettingsTableModel(m_settings, keys));

		setTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		setTable.setRowHeight((int)(setTable.getFont().getSize()*1.5));
		setTable.getColumnModel().getColumn(1).setCellEditor(new SettingsTableCellEditor(m_settings));
		setTable.getColumnModel().getColumn(1).setCellRenderer(new SettingsTableCellRenderer(m_settings));
//		settings.setDefaultEditor(File.class, new SettingsTableFileCellEditor());
//		settings.setDefaultEditor(SerialPort.class, new SettingsTableSerialPortCellEditor());
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		Box b = Box.createVerticalBox();
		JScrollPane jsp = new JScrollPane(setTable);
		jsp.setPreferredSize(new Dimension(500,500));
		split.add(jsp);
		
		m_settings.addSettingsListener(e -> {
			int sp = m_settings.getInt(Key.SPLIT_PANE_POS);
			
			split.setDividerLocation(sp);
		}, Key.SPLIT_PANE_POS, true);
		split.addPropertyChangeListener(e -> {
			int sp = split.getDividerLocation();
			
			m_settings.set(Key.SPLIT_PANE_POS, sp);
			
		});
//		m_text.setPreferredSize(new Dimension(500,500));
//		m_text.setLineWrap(false);
		m_text.setFont(new Font("Monospaced", Font.PLAIN, 12));
		
		m_text.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				showPanePopup(e);
			}

			
			
			
			
		});
//		m_text.setWrapStyleWord(true);
		split.add(new JScrollPane(m_text));
		cp.add(split);

		f.setVisible(true);

	}
	
	private void showPanePopup(MouseEvent e) {
		if(e.isPopupTrigger()) {
			JPopupMenu jpm = new JPopupMenu();
			jpm.add(m_actions.getAction(ActionInfos.COPY_ISSUES));
			jpm.show(e.getComponent(), e.getX(), e.getY());		
		}
	}
	private Settings getSettings() {
		return m_settings;
	}
	public JFrame getFrame(){
		return m_frame;
	}

	private void registerActions() {
		m_actions.setIconColor(COLOR_LOGO_BLUE);
		m_actions.registerActions(ActionInfos.values());
		m_actions.addKeyBindings(m_frame.getRootPane());

		m_actions.addListener(ActionInfos.HELP, e -> {});
		m_actions.addListener(ActionInfos.EXIT, e -> exit());
		m_actions.addListener(ActionInfos.MINIMIZE, e -> m_frame.setExtendedState(Frame.ICONIFIED));
		m_actions.addListener(ActionInfos.NORMALIZE, e -> m_frame.setExtendedState(Frame.NORMAL));
		m_actions.addListener(ActionInfos.MAXIMIZE, e -> m_frame.setExtendedState(Frame.MAXIMIZED_BOTH));

		m_actions.addListener(ActionInfos.SETTINGS_LOAD, e -> getSettings().loadSettings(getFrame(), Key.DEFAULT_FILE_PATH));
		m_actions.addListener(ActionInfos.SETTINGS_SAVE, e -> getSettings().saveSettings(getFrame(), Key.DEFAULT_FILE_PATH));
		m_actions.addListener(ActionInfos.SETTINGS_SHOW, e -> SettingsDialog.showSettingsDialog(getFrame(), getSettings(), Key.values()));
		m_actions.addListener(ActionInfos.PARSE_SCHEMA, e -> parse());
		m_actions.addListener(ActionInfos.GENERATE_C, e -> generateC());
		m_actions.addListener(ActionInfos.GENERATE_JAVA, e -> generateJava());
		m_actions.addListener(ActionInfos.CLEAN_SCHEMA, e -> generatePretty());

		m_actions.addListener(ActionInfos.COPY_ISSUES, e -> copyIssues());
		m_actions.addListener(ActionInfos.TOOLS_RESET_KEYS, e -> resetKeys());
		m_actions.addListener(ActionInfos.TOOLS_CLEAR_OUTPUT, e -> clearText());

		
	}

	private void clearText() {
		m_text.setText("");
	}
	private void copyIssues() {
		int s = m_text.getSelectionStart();
		int e = m_text.getSelectionEnd();
		m_text.selectAll();
		m_text.copy();
		if(s >= 0) {
			m_text.setSelectionStart(s);
			m_text.setSelectionEnd(e);
		}
		
	}
	private void execute(Runnable r) {
		Thread t = new Thread(() -> {
			r.run();
		});
		t.start();
	}
	private void parse() {
		m_text.setText("");
		execute(() -> noThreadParse());
		
	}
	
	
	
	/**
	 * after parsing, this resets all module and message keys and recomputes 
	 */
	private void resetKeys() {
		execute(() -> {
			if(m_parser.getMessages().size() == 0) {
				noThreadParse();
			}
			if(m_log.isError()) {
				append("Errors occured during parsing. Will not generate proceed with key reset.", ParserIssue.Type.ERROR);
			} else {
				append("Reseting module and message keys \n");
				
				m_parser.resetKeys();
			}
			append("Done");
		});		
	}
	private void generateJava() {
		execute(() -> {
			File dir = m_settings.getFile(Key.JAVA_DIRECTORY);
			String p = m_settings.getString(Key.JAVA_PACKAGE_NAME);
			
			
	
			if(m_parser.getMessages().size() == 0) {
				noThreadParse();
			}
			if(m_log.isError()) {
				append("Errors occured during parsing. Will not generate Java code.", ParserIssue.Type.ERROR);
			} else {
				append("Generating Java code in \"" + dir+"\"\n");
				String h = readHeader(m_settings.getUri(Key.CODE_HEADER_FILE_PATH));
				JavaWriter w = new JavaWriter(dir, m_parser, h, p, m_log);
				w.write();
			}
			appendDone();
		});
	}

	private void generateC() {
		execute(() -> {
			File dir = m_settings.getFile(Key.C_DIRECTORY);
			
			
			
	
			if(m_parser.getMessages().size() == 0) {
				noThreadParse();
			}
			
			if(m_log.isError()) {
				append("Errors occured during parsing. Will not generate C code.", ParserIssue.Type.ERROR);
			} else {
				append("Generating C code in \"" + dir+"\"\n");
		
				String h = readHeader(m_settings.getUri(Key.CODE_HEADER_FILE_PATH));
				CWriter w = new CWriter(dir, m_parser, h, m_log);
				w.write();
			}
			appendDone();
		});
	}
	private String readHeader(URI uri) {
		String header = "";
		
		Path p = Path.of(uri);
		if(!Files.isRegularFile(p)) {
			System.out.println("Code header is not a file: "+p);
			
		} else {
			try {
				header = Files.readString(p);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return header;
			
	}
	private void generatePretty() {
		File dir = m_settings.getFile(Key.SCHEMA_DIRECTORY);
		append("Generating Beautified Schema in \"" + dir+"\"\n");

//		if(m_parser.getTopLevelField() == null) {
//			parse();
//		}

		String h = readHeader(m_settings.getUri(Key.IDL_HEADER_FILE_PATH));
		PrettyWriter pw = new PrettyWriter(dir, m_parser, h, m_log);
//		pw.write(m_parser.getTopLevelField(), m_parser.getHeader());
		appendDone();
	}
	private void noThreadParse() {

		File dir = m_settings.getFile(Key.SCHEMA_DIRECTORY);
		boolean v4 = m_settings.getBoolean(Key.VERSION_4_NOT_3);
		String ext = v4 ? ".msg" : ".idl";
		
		try {
			
			m_parser.clear();
			m_parser.setVersion(v4 ? 4 : 3);
			
			
			loadFiles(dir, dir, f -> f.isDirectory() || f.getName().toLowerCase().endsWith(ext));
			append("Starting Parser");
			m_parser.parse();
			appendDone();
		} catch(SchemaParserException e) {
			append(e.toString());
		}
	}
	private void appendDone() {
		int s = m_log.getIssueCountOfType(Type.SKIPPED);
		int w = m_log.getIssueCountOfType(Type.WARNING);
		int e = m_log.getIssueCountOfType(Type.ERROR);
		int n = m_log.getIssueCountOfType(Type.NOTE);
		append("Parsing Done: "+e+" Errors, "+w+" Warnings, "+s+" Skipped, "+n+" Notes.");
	}
	private void loadFiles(File root, File f, FileFilter ff) throws SchemaParserException {
		append("Loading \""+f+"\"\n");
		if(f.isDirectory()) {
			
			File[] fs = f.listFiles(ff);
			for(File cf : fs) {
				loadFiles(root, cf, ff);
			}
		} else if(f.isFile() && ff.accept(f)) {
			BufferedReader br;
			String[] ss = null;
			try {
				br = new BufferedReader(new FileReader(f));
				Stream<String> lines = br.lines();
				ss = lines.toArray(String[]::new);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				append(e.toString());
			}

			if(ss != null) {
				
				Path p = root.toPath().relativize(f.toPath());
				m_parser.append(p.toString(), ss);
				
			}
		}
	}

	public static void main(String...args){
		Settings settings = new Settings(BlueberrySchemaParserGui.class);
		ResourceTools.setFonts();

		BlueberrySchemaParserGui gui = new BlueberrySchemaParserGui(settings);


	}
	public void exit() {

		System.exit(0);


	}
}
