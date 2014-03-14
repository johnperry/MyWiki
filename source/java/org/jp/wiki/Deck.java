package org.jp.wiki;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.rsna.util.*;
import org.rsna.ui.*;
import org.w3c.dom.*;

public class Deck {
	File file;
	LinkedList<Card> deck;
	String key = "";
	Component parent;
	boolean changed = false;

	public Deck(File file, Component parent) {
		this.file = file;
		this.parent = parent;
		deck = new LinkedList<Card>();
	}

	public boolean hasChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = this.changed || changed;
	}

	public void load() {
		if (!getKey()) System.exit(0);
		if (file.exists()) {
			try {
				String text = FileUtil.getText(file);
				if (!text.equals("")) {
					if (!key.equals("")) {
						text = CipherUtil.decipher(text, key);
					}

					Document doc = null;
					try { doc = XmlUtil.getDocument(text); }
					catch (Exception unable) {
						JOptionPane.showMessageDialog(
										parent,
										"Unable to decipher and parse the deck.",
										"Error",
										JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}

					Element root = doc.getDocumentElement();
					Node child = root.getFirstChild();
					while (child != null) {
						if (child instanceof Element) {
							Element childEl = (Element)child;
							if (childEl.getTagName().equals("Card")) {
								String cardText = childEl.getTextContent();
								Card c = new Card(cardText);
								deck.add(c);
							}
						}
						child = child.getNextSibling();
					}
				}
			}
			catch (Exception ex) {
				JOptionPane.showMessageDialog(
								parent,
								"Unable to construct the deck.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		if (deck.size() == 0) deck.add( new Card("My first card") );
	}

	public void add(Card c) {
		deck.add(c);
		changed = true;
	}

	public void remove(Card c) {
		deck.remove(c);
		changed = true;
	}

	public int size() {
		return deck.size();
	}

	public void save() {
		try {
			Document doc = XmlUtil.getDocument();
			Element root = doc.createElement("Deck");
			doc.appendChild(root);
			for (Card c : deck) {
				String cardText = c.getEditorText();
				Element cardEl = doc.createElement("Card");
				CDATASection cdata = doc.createCDATASection(cardText);
				cardEl.appendChild(cdata);
				root.appendChild(cardEl);
			}
			String text = XmlUtil.toString(doc);
			String encrypted = CipherUtil.encipher(text, key);
			if (!encrypted.equals("")) {
				FileUtil.setText(file, encrypted);
			}
			changed = false;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(
							parent,
							"Unable to store the deck.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean getKeyInClear() {
		String newKey = JOptionPane.showInputDialog(
				parent, "Password: ", "Enter the Encryption Key", JOptionPane.PLAIN_MESSAGE);
		if (newKey != null) {
			key = newKey;
			return true;
		}
		return false;
	}

	public boolean getKey() {
		JPanel panel = new JPanel( new RowLayout() );
		panel.add( new JLabel("Password") );
		panel.add( RowLayout.crlf() );
		final JPasswordField password = new JPasswordField(25);
		panel.add( password );
		panel.add( RowLayout.crlf() );
		JOptionPane pane = new JOptionPane( panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION );
		JDialog dialog = pane.createDialog( parent, "Enter the Encryption Key" );
		dialog.addWindowFocusListener( new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				password.requestFocusInWindow();
			}
		});
		dialog.setVisible( true );
		Object confirm = pane.getValue();
		if (confirm instanceof Integer) {
			int value = ((Integer)confirm).intValue();
			if (value == JOptionPane.OK_OPTION) {
				key = new String( password.getPassword() );
				return true;
			}
		}
		return false;
	}

	public void changeKey() {
		String oldKey = JOptionPane.showInputDialog(
				parent, "Password: ", "Enter the Current Encryption Key", JOptionPane.PLAIN_MESSAGE);
		if ((oldKey != null) && oldKey.equals(key)) {
			String newKey1 = JOptionPane.showInputDialog(
				parent, "Password: ", "Enter the New Encryption Key", JOptionPane.PLAIN_MESSAGE);
			if (newKey1 !=  null) {
				String newKey2 = JOptionPane.showInputDialog(
					parent, "Password: ", "Enter the New Encryption Key Again", JOptionPane.PLAIN_MESSAGE);
				if ((newKey2 != null) && newKey1.equals(newKey2)) {
					key = newKey1;
				}
				else {
					JOptionPane.showMessageDialog(
									parent,
									"The keys did not match.\nThe old key remains in effect.",
									"Error",
									JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	public Card[] find(String s) {
		LinkedList<Card> list = new LinkedList<Card>();
		for (Card c : deck) {
			if (c.contains(s)) list.add(c);
		}
		Card[] cards = new Card[list.size()];
		cards = list.toArray(cards);
		return cards;
	}

	public String getIndex() {
		HashSet<String> titles = new HashSet<String>();
		for (Card c : deck) {
			String[] h1s = c.getTitles();
			for (String h1 : h1s) {
				titles.add(h1);
			}
		}
		String[] x = new String[titles.size()];
		x = titles.toArray(x);
		Arrays.sort(x, new CaselessComparator());
		StringBuffer sb = new StringBuffer("<H1><CENTER>Index</CENTER></h1>\n");
		sb.append("<HR>\n");
		sb.append("<UL>\n");
		for (String t : x) {
			sb.append("<LI><a href=\"xxx\">"+t+"</a>\n");
		}
		sb.append("</UL>\n");
		return sb.toString();
	}

	class CaselessComparator implements Comparator<String> {
		public int compare(String s1, String s2) {
			return s1.toLowerCase().compareTo(s2.toLowerCase());
		}
	}

}

