package org.jp.wiki;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.rsna.util.FileUtil;

public class AttachedFrame extends JFrame implements ComponentListener {

	Component target;
	int width;
	JEditorPane editorPane;

	public AttachedFrame(Component target, String title, int width, Color bgColor) {
		super(title);
		this.target = target;
		this.width = width;
		setBackground(bgColor);
		JScrollPane jsp = new JScrollPane();
		getContentPane().add(jsp,BorderLayout.CENTER);
		editorPane = new JEditorPane();
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		editorPane.setBackground(bgColor);
		jsp.setViewportView(editorPane);
		target.addComponentListener(this);
	}

	public void setText(String text) {
		editorPane.setText(text);
		editorPane.setCaretPosition(0);
	}

	public void attach() {
		Dimension componentSize = target.getSize();
		setSize(width, componentSize.height);
		Point componentLocation = target.getLocation();
		int x = componentLocation.x + componentSize.width;
		setLocation(new Point(x, componentLocation.y));
		validate();
		target.requestFocus();
	}

	//Implement the ComponentListener interface
	public void componentHidden(ComponentEvent e) { }
	public void componentMoved(ComponentEvent e) { setFramePosition(); }
	public void componentResized(ComponentEvent e) { setFramePosition(); }
	public void componentShown(ComponentEvent e) { }
	private void setFramePosition() {
		if (this.isVisible()) {
			this.attach();
		}
	}
}
