package org.jp.wiki;

import java.awt.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 * A JTextPane that supports text color and includes thread-safe methods.
 */
public class DisplayPane extends JEditorPane {

	boolean trackWidth = true;

	/**
	 * Create a DisplayPane.
	 */
	public DisplayPane() {
		super();
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setContentType("text/html");
		setEditable(false);

		EditorKit kit = getEditorKit();
		if (kit instanceof HTMLEditorKit) {
			HTMLEditorKit htmlKit = (HTMLEditorKit)kit;
			StyleSheet sheet = htmlKit.getStyleSheet();
			sheet.addRule("body {font-family: arial; font-size:16;}");
			sheet.addRule("pre  {border:thin dashed blue; color:blue;}");
			sheet.addRule(".indent1 {margin-left:30;}");
			sheet.addRule(".indent2 {margin-left:60;}");
			htmlKit.setStyleSheet(sheet);
		}
	}

	/**
	 * Get the flag that indicates whether the pane is to track the width
	 * of its container.
	 */
	public boolean getScrollableTracksViewportWidth() {
		return trackWidth;
	}

	/**
	 * Set the flag that indicates whether the pane is to track the width
	 * of its container.
	 */
	public void setScrollableTracksViewportWidth(boolean trackWidth) {
		this.trackWidth = trackWidth;
	}

}
