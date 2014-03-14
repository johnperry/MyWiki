package org.jp.wiki;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.rsna.util.*;
import org.rsna.ui.*;
import org.w3c.dom.*;

public class Card {

	String editorText = "";

	public Card() {
		super();
	}

	public Card(String s) {
		this();
		setEditorText(s);
	}

	public boolean contains(String s) {
		return editorText.toLowerCase().contains(s.toLowerCase());
	}

	public String getEditorText() {
		return editorText.replaceAll("\\r", "");
	}

	public boolean setEditorText(String s) {
		s = s.replaceAll("\\r", "");
		boolean changed = !s.equals(editorText);
		editorText = s;
		return changed;
	}

	public String[] getTitles() {
		String text = getDisplayText();
		Pattern pattern = Pattern.compile("<[hH]1>(.+?)</[hH]1>");
		LinkedList<String> titles = new LinkedList<String>();
		try {
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				String group = matcher.group(1);
				titles.add(group.trim());
			}
		}
		catch (Exception ex) { }
		String[] x = new String[titles.size()];
		x = titles.toArray(x);
		return x;
	}

	public String getDisplayText() {
		String ss = getEditorText();
		StringBuffer sb = new StringBuffer();
		int k;
		while ( (k=ss.indexOf("<pre>")) != -1) {
			String s = ss.substring(0, k);
			sb.append(filter(s));
			int kk = ss.indexOf("</pre>", k);
			if (kk != -1) {
				sb.append(ss.substring(k, kk+6));
				ss = ss.substring(kk+6);
			}
		}
		sb.append(filter(ss));
		return sb.toString();
	}

	public URL getFirstURL() {
		Pattern pattern = Pattern.compile("\\[\\[(http[s]?://[^\\]]*)\\]\\]");
		Matcher matcher = pattern.matcher(editorText);
		if (matcher.find()) {
			String url = matcher.group(1).trim();
			try { return new URL(url); }
			catch (Exception returnNull) { }
		}
		return null;
	}

	private String filter(String s) {
		s = doList(s, '*', "UL");
		s = doList(s, '#', "OL");
		s = doH(s);
		s = doB(s);
		s = doA(s);
		s = doU(s);
		s = doEM(s);
		s = doHR(s);
		s = doP(s);
		return s;
	}

	private String doP(String s) {
		s = s.replaceAll("(\\n){2,}::", "\n<P class=\"indent2\">");
		s = s.replaceAll("(\\n){2,}:", "\n<P class=\"indent1\">");
		s = s.replaceAll("(\\n){2,}", "\n<P>");
		return s;
	}

	private String doH(String s) {
		s = doH(s, 3);
		s = doH(s, 2);
		s = doH(s, 1);
		return s;
	}

	private String doH(String s, int n) {
		try {
			String tag = "H" + n;
			Pattern pattern = Pattern.compile("^={"+n+"}([^\\n]+)", Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(s);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				String group = matcher.group(1);
				StringBuffer buf = new StringBuffer();
				if (n > 1) buf.append("<"+tag+">"+group+"</"+tag+">");
				else buf.append("<CENTER><"+tag+">"+group+"</"+tag+"></CENTER>");
				matcher.appendReplacement(sb, buf.toString());
			}
			matcher.appendTail(sb);
			return sb.toString();
		}
		catch (Exception ex) { return s; }
	}

	private String doHR(String s) {
		try {
			Pattern pattern = Pattern.compile("^-{4,}$", Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(s);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				matcher.appendReplacement(sb, "<HR/>");
			}
			matcher.appendTail(sb);
			return sb.toString();
		}
		catch (Exception ex) { return s; }
	}

	private String doB(String s) {
		return replace(s, '\'', 2, "B");
	}

	private String doU(String s) {
		return replace(s, '_', 2, "U");
	}

	private String doEM(String s) {
		return replace(s, '~', 2, "EM");
	}

	private String doList(String s, char c, String tag) {
		try {
			Pattern pattern = Pattern.compile("((^\\"+c+"[^\\n]*\\n)+)", Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(s);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				String group = matcher.group(1);
				StringBuffer buf = new StringBuffer();
				buf.append("<"+tag+">");
				String[] lines = group.split("\n");
				for (String line : lines) {
					buf.append("\n<LI>"+line.substring(1));
				}
				buf.append("\n</"+tag+">\n");
				matcher.appendReplacement(sb, buf.toString());
			}
			matcher.appendTail(sb);
			return sb.toString();
		}
		catch (Exception ex) { return s; }
	}

	private static String doA(String s) {
		try {
			Pattern pattern = Pattern.compile("\\[\\[(.+?)\\]\\]");
			Matcher matcher = pattern.matcher(s);

			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				String group = matcher.group(1);
				int k = group.lastIndexOf("|");
				String name = group;
				if (k != -1) {
					name = group.substring(k+1).trim();
					group = group.substring(0,k).trim();
					if (name.equals("")) name = group;
				}
				String repl = "<A HREF=\""+group+"\">"+name+"</A>";
				matcher.appendReplacement(sb, repl);
			}
			matcher.appendTail(sb);
			return sb.toString();
		}
		catch (Exception ex) { return s; }
	}

	private static String replace(String s, char c, int n, String tag) {
		try {
			Pattern pattern = Pattern.compile("[\\"+c+"]{"+n+"}?(.+?)[\\"+c+"]{"+n+"}?");
			Matcher matcher = pattern.matcher(s);

			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				String group = matcher.group(1);
				String repl = "<"+tag+">" + group + "</"+tag+">";
				if ((c == '=') && (n == 2)) repl = "<CENTER>" + repl + "</CENTER>";
				matcher.appendReplacement(sb, repl);
			}
			matcher.appendTail(sb);
			return sb.toString();
		}
		catch (Exception ex) { return s; }
	}

}
