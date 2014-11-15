package org.jp.wiki;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import org.rsna.util.*;
import org.rsna.ui.*;

public class MyWiki extends JFrame {

	static Color 	background = Color.getHSBColor(0.58f, 0.17f, 0.95f);
    CardPanel		cardPanel;
    FooterPanel		footerPanel;
    String			windowTitle = "MyWiki - v3";
    int 			width = 580;
    int 			height = 700;
    JMenu 			menu = null;
    PropertiesFile  props = null;
    Deck			deck;
    File			deckFile = new File("MyWiki.deck");
    AttachedFrame	helpFrame;

    public static void main(String[] args) {
        new MyWiki();
    }

    public MyWiki() {
		ProcessListener listener = new ProcessListener(17999, this);
		if (listener.check()) System.exit(0);
		props = new PropertiesFile(new File("MyWiki.properties"));
		deck = new Deck(deckFile, this);
    	initComponents();
    	deck.load();
    	cardPanel.displayIndex();
    	listener.listen();
    }

    private void initComponents() {
		setTitle(windowTitle);
		setJMenuBar(getAppMenuBar());
		JPanel main = new JPanel();
		main.setLayout(new BorderLayout());
		cardPanel = new CardPanel();
		footerPanel = new FooterPanel();
		main.add(cardPanel,BorderLayout.CENTER);
		main.add(footerPanel,BorderLayout.SOUTH);
		getContentPane().add(main, BorderLayout.CENTER);
		pack();
		positionFrame();
		setVisible(true);
		addWindowListener( new WindowCloser(this) );
		helpFrame = new AttachedFrame(this, "Help", 550, Color.white);
		helpFrame.setText( FileUtil.getText( FileUtil.getStream("/Help.html") ) );
    }

    class WindowCloser extends WindowAdapter {
		public WindowCloser(JFrame parent) {
			parent.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}
		public void windowClosing(WindowEvent evt) {
			save();
			System.exit(0);
		}
    }

    private void save() {
		cardPanel.display();
		if ((deck.size() > 0) && deck.hasChanged()) {
			backup(deckFile);
			deck.save();
		}
		saveProps();
	}

	private void saveProps() {
		Point p = this.getLocation();
		props.setProperty("x", Integer.toString(p.x));
		props.setProperty("y", Integer.toString(p.y));
		Toolkit t = getToolkit();
		Dimension d = this.getSize ();
		props.setProperty("w", Integer.toString(d.width));
		props.setProperty("h", Integer.toString(d.height));
		props.store();
	}

    private JMenuBar getAppMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu deckMenu = new JMenu();
		deckMenu.setText("Deck");

		JMenuItem changeKeyItem = new JMenuItem("Change encryption key");
		changeKeyItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				deck.changeKey();
			}
		});
		deckMenu.add(changeKeyItem);

		JMenuItem indexItem = new JMenuItem("Display index");
		indexItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,InputEvent.CTRL_MASK));
		indexItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				footerPanel.clearSearchField();
				cardPanel.displayIndex();
			}
		});
		deckMenu.add(indexItem);

		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_MASK));
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				save();
			}
		});
		deckMenu.add(saveItem);

		JMenuItem deleteBackupsItem = new JMenuItem("Delete backups");
		deleteBackupsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					File parent = deckFile.getCanonicalFile().getParentFile();
					File[] files = parent.listFiles(new DeckFileFilter());
					if ((files.length > 0)
							&& confirm("Are you sure you want to delete\nthe backup files?\n")) {
						for (File file : files) file.delete();
					}
				}
				catch (Exception skip) { }
			}
		});
		deckMenu.add(deleteBackupsItem);

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,InputEvent.CTRL_MASK));
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				save();
				System.exit(0);
			}
		});
		deckMenu.add(exitItem);

		menuBar.add(deckMenu);

		JMenu cardMenu = new JMenu();
		cardMenu.setText("Card");

		JMenuItem newCardItem = new JMenuItem("New card");
		newCardItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,InputEvent.CTRL_MASK));
		newCardItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				footerPanel.clearSearchField();
				Card c = new Card("=Title\n----\n");
				deck.add(c);
				cardPanel.display(new Card[] { c });
				cardPanel.edit();
				cardPanel.select(1, 6); //Select the word "Title"
				cardPanel.requestFocus();
			}
		});
		cardMenu.add(newCardItem);

		JMenuItem displayCardItem = new JMenuItem("Display current card");
		displayCardItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,InputEvent.CTRL_MASK));
		displayCardItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cardPanel.display();
			}
		});
		cardMenu.add(displayCardItem);

		JMenuItem editCardItem = new JMenuItem("Edit current card");
		editCardItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,InputEvent.CTRL_MASK));
		editCardItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cardPanel.edit();
			}
		});
		cardMenu.add(editCardItem);

		JMenuItem printCardItem = new JMenuItem("Print current card");
		printCardItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,InputEvent.CTRL_MASK));
		printCardItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				printCard();
			}
		});
		cardMenu.add(printCardItem);

		JMenuItem removeCardItem = new JMenuItem("Remove current card");
		removeCardItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_MASK));
		removeCardItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (cardPanel.deckCardIsActive()
						&& confirm("Are you sure you want\nto delete this card?")) {
					cardPanel.removeCurrentCard();
				}
			}
		});
		cardMenu.add(removeCardItem);

		menuBar.add(cardMenu);

		JMenu helpMenu = new JMenu();
		helpMenu.setText("Help");

		JMenuItem helpItem = new JMenuItem("Display Help Frame");
		helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_MASK));
		helpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				helpFrame.setVisible(true);
				helpFrame.attach();
			}
		});
		helpMenu.add(helpItem);

		JMenuItem aboutItem = new JMenuItem("About MyWiki");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				aboutPopup();
			}
		});
		helpMenu.add(aboutItem);

		menuBar.add(helpMenu);

		return menuBar;
	}

	private void printCard() {
		PrintUtil.printComponent(this);
	}

	private void aboutPopup() {
		String text = FileUtil.getText( FileUtil.getStream( "/About.txt" ) );
		JOptionPane.showMessageDialog( this, text );
	}

	private void backup(File targetFile) {
		targetFile = targetFile.getAbsoluteFile();
		File parent = targetFile.getParentFile();
		if (targetFile.exists()) {
			String name = targetFile.getName();
			int k = name.lastIndexOf(".");
			String target = name.substring(0,k) + "[";
			int tlen = target.length();
			String ext = name.substring(k);

			int n = 0;
			File[] files = parent.listFiles();
			if (files != null) {
				for (File file : files) {
					String fname = file.getName();
					if (fname.startsWith(target)) {
						int kk = fname.indexOf("]", tlen);
						if (kk > tlen) {
							int nn = StringUtil.getInt(fname.substring(tlen, kk), 0);
							if (nn > n) n = nn;
						}
					}
				}
			}
			n++;
			File backup = new File(parent, target + n + "]" + ext);
			backup.delete(); //shouldn't be there, but just in case.
			FileUtil.copy(targetFile, backup);
		}
	}

	class DeckFileFilter implements FileFilter {
		public boolean accept(File file) {
			return file.getName().matches("MyWiki\\[\\d+\\]\\.deck");
		}
	}

	private boolean confirm(String message) {
		int yesno = JOptionPane.showConfirmDialog(
							this,
							message,
							"Are you sure?",
							JOptionPane.YES_NO_OPTION);
		return (yesno == JOptionPane.YES_OPTION);
	}

	private void positionFrame() {
		int x = StringUtil.getInt( props.getProperty("x"), 0 );
		int y = StringUtil.getInt( props.getProperty("y"), 0 );
		int w = StringUtil.getInt( props.getProperty("w"), 0 );
		int h = StringUtil.getInt( props.getProperty("h"), 0 );
		boolean noProps = ((w == 0) || (h == 0));
		int wmin = 500;
		int hmin = 600;
		if ((w < wmin) || (h < hmin)) {
			w = wmin;
			h = hmin;
		}
		if ( noProps || !screensCanShow(x, y) || !screensCanShow(x+w-1, y+h-1) ) {
			Toolkit t = getToolkit();
			Dimension scr = t.getScreenSize ();
			w = wmin;
			h = hmin;
			x = (scr.width - w)/2;
			y = (scr.height - h)/2;
		}
		setSize( w, h );
		setLocation( x, y );
	}

	private boolean screensCanShow(int x, int y) {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = env.getScreenDevices();
		for (GraphicsDevice screen : screens) {
			GraphicsConfiguration[] configs = screen.getConfigurations();
			for (GraphicsConfiguration gc : configs) {
				if (gc.getBounds().contains(x, y)) return true;
			}
		}
		return false;
	}

	class FooterPanel extends JPanel implements DocumentListener, ActionListener {
		public JTextField searchText;
		public JLabel currentCard;
		public JButton next;
		public JButton prev;
		public FooterPanel() {
			super();
			this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			this.setLayout(new BoxLayout( this, BoxLayout.X_AXIS ));
			this.setBackground(background);

			searchText = new JTextField(25);
			searchText.addActionListener(this);
			currentCard = new JLabel("");
			next = new JButton(">");
			next.addActionListener(this);
			prev = new JButton("<");
			prev.addActionListener(this);

			this.add(searchText);
			this.add(Box.createHorizontalStrut(10));
			this.add(currentCard);
			this.add(Box.createHorizontalStrut(10));
			this.add(Box.createHorizontalGlue());
			this.add(prev);
			this.add(Box.createHorizontalStrut(4));
			this.add(next);
			startListening();
		}
		public void stopListening() {
			searchText.getDocument().removeDocumentListener(this);
		}
		public void startListening() {
			searchText.getDocument().addDocumentListener(this);
		}
		public void setSearchText(String text){
			searchText.setText(text);
		}
		public void clearSearchField() {
			stopListening();
			setSearchText("");
			startListening();
		}
		public void setCurrentCard(int current, int total) {
			if (current <= 0) {
				currentCard.setText("");
			}
			else if (total >= current) {
				currentCard.setText("Showing card "+current+" of "+total);
			}
			else {
				currentCard.setText("Showing card "+current);
			}
		}
		public void insertUpdate(DocumentEvent e) { update(); }
		public void removeUpdate(DocumentEvent e) { update(); }
		public void changedUpdate(DocumentEvent e) { update(); }
		private void update() {
			Card[] cards = deck.find(searchText.getText());
			cardPanel.display(cards);
		}
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source.equals(next)) {
				cardPanel.displayNext();
			}
			else if (source.equals(prev)) {
				cardPanel.displayPrev();
			}
			else if (source.equals(searchText)) {
				cardPanel.browseFirstURL();
			}
		}
	}

	class CardPanel extends JPanel implements HyperlinkListener {

		JScrollPane jsp;
		Card[] cards = null;
		Card currentCard = null;
		int currentCardIndex = -1;
		DisplayPane displayPane;
		ColorPane editorPane;
		boolean displayMode = true;

		public CardPanel() {
			super();
			setLayout(new BorderLayout());
			setBackground(Color.white);
			jsp = new JScrollPane();
			add(jsp, BorderLayout.CENTER);
			displayPane = new DisplayPane();
			editorPane = new ColorPane();
			editorPane.setFont(new Font("Monospaced", Font.BOLD, 16));
			displayPane.addHyperlinkListener(this);
		}

		public void browseFirstURL() {
			if (currentCard != null) {
				try {
					URL url = currentCard.getFirstURL();
					if (url != null) {
						Desktop.getDesktop().browse( url.toURI() );
					}
				}
				catch (Exception ignore) { }
			}
		}

		public void select(int start, int end) {
			editorPane.select(start, end);
		}

		public void hyperlinkUpdate(HyperlinkEvent r) {
			try{
				if (r.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					URL url = r.getURL();
					if (url != null) {
						Desktop.getDesktop().browse( r.getURL().toURI() );
					}
					else {
						Element el = r.getSourceElement();
						int k1 = el.getStartOffset();
						int k2 = el.getEndOffset();
						footerPanel.setSearchText(displayPane.getDocument().getText(k1, k2-k1));
					}
				}
			}
			catch(Exception e) { e.printStackTrace(); }
		}

		public void requestFocus() {
			Component c = jsp.getViewport().getView();
			if (c != null) c.requestFocus();
		}

		public boolean deckCardIsActive() {
			return (currentCard != null);
		}

		public void displayIndex() {
			Card indexCard = new Card(deck.getIndex());
			display( new Card[] { indexCard } );
		}

		public void display(Card[] cards) {
			this.cards = cards;
			if ((cards != null) && (cards.length > 0)) {
				display(0);
			}
			else {
				cards = null;
				currentCardIndex = -1;
				currentCard = null;
				jsp.setViewportView(null);
				footerPanel.setCurrentCard(-1,-1);
			}
		}

		public void display() {
			display(currentCardIndex);
		}

		public void display(int c) {
			if (!displayMode && (currentCard != null)) {
				deck.setChanged(
						currentCard.setEditorText(
								editorPane.getText()));
			}
			if ((cards != null) && (c >= 0) && (c < cards.length)) {
				displayPane.setText(cards[c].getDisplayText());
				jsp.setViewportView(displayPane);
				currentCardIndex = c;
				currentCard = cards[c];
				displayMode = true;
				footerPanel.setCurrentCard(c+1, cards.length);
				revalidate();
			}
		}

		public void displayNext() {
			display(currentCardIndex + 1);
		}

		public void displayPrev() {
			display(currentCardIndex - 1);
		}

		public void edit() {
			if ((cards != null) && (currentCardIndex >= 0) && (currentCardIndex < cards.length)) {
				editorPane.setText(cards[currentCardIndex].getEditorText());
				jsp.setViewportView(editorPane);
				displayMode = false;
				revalidate();
				editorPane.requestFocus();
			}
		}

		public Card getCurrentCard() {
			return currentCard;
		}

		public void removeCurrentCard() {
			if (currentCard != null)  {
				deck.remove(currentCard);
				displayIndex();
			}
		}
	}
}
