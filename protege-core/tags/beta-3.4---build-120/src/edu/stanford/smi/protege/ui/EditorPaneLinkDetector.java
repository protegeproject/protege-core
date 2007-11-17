package edu.stanford.smi.protege.ui;

import java.awt.event.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.accessibility.AccessibleHypertext;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.parser.ParserDelegator;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.SystemUtilities;

//  @author Vivek Tripathi (vivekyt@stanford.edu) 

public class EditorPaneLinkDetector extends JEditorPane implements Disposable {
	/**
	 * Creates a <code>EditorPaneLinkDetector</code>.
	 */
	
	private static String linkActive;
	
	public static String getLinkActive() {
		return linkActive;
	}
	
	public void dispose() {
		removeMouseListener(getEditorPaneMouseListener());
		removeMouseMotionListener(getEditorPaneMouseMotionListener());
		
	}

	private MouseListener ml = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {

			if(linkActive != null){
			//  this means that the editor pane
			// has a hyperlink at the point
			// where mouse pointer is clicked currently.
								
			String str;
			
			//  the user might or might not have entered
			// http:// in the hyperlink text. We add "http://"
			// in the hyperlink if needed.
			
			if(linkActive.contains("http://"))
				str = linkActive;
			else
				str = "http://"+linkActive;
			
			//  here we call the explorer with the clicked
			// link as an argument. These are present in str
			// Runtime.getRuntime().exec(str);
			SystemUtilities.showHTML(str);
			setEditable(true);
			}
		}
	};
	
	private MouseMotionListener mml = new MouseMotionAdapter() {
		public void mouseMoved(MouseEvent e) {

			AccessibleJTextComponent context = (AccessibleJTextComponent) getAccessibleContext()
					.getAccessibleEditableText();

			AccessibleHypertext accText = (AccessibleHypertext) context
					.getAccessibleText();

			//  accText.getIndexAtPoint() returns 0 based index of character 
			//	at the mouse tip 
			int index = accText.getIndexAtPoint(e.getPoint());

			//  Returns the index into an array of hyperlinks that is 
			// associated with this character index, or -1 if there 
			// is no hyperlink associated with this index.
			int linkIndex = accText.getLinkIndex(index);
			
			if (linkIndex == -1) { //  this means that the editor pane
								   // does not have a hyperlink at the point
								   // where mouse pointer is currently
				setToolTipText(null);
				linkActive = null;
				if (!isEditable())
					setEditable(true);
				return;
				
			}

			//  getAccessibleActionDescription() Returns a String 
			// description of this particular link action.
			// We save it in linkDesc.
			
			String linkDesc = accText.getLink(linkIndex)
					.getAccessibleActionDescription(0);

			String toolTipText = "<html><body style='margin: 3'>"
					+ linkDesc
					+ "<br><b>Click to follow link</b></body></html>";
			
			//  here we make linkActive as the link on which mouse pointer
			// is moved. This linkActive is then used in mouseclick listener
			
			linkActive = linkDesc;
			
			//  We display the link in the ToolTipText of the pointer
			// and also Change mouse pointer to 'hand' 
			// when it is moved over a hyperlink
			
			setToolTipText(toolTipText);
			if (isEditable())
				setEditable(false);
		}
	};
	
	public MouseListener getEditorPaneMouseListener() {
		return ml;
	}
	
	public MouseMotionListener getEditorPaneMouseMotionListener() {
		return mml;
	}
	
	public EditorPaneLinkDetector() {

		linkActive = null;
		HTMLEditorKit htmlkit = new HTMLEditorKit();

		StyleSheet styles = htmlkit.getStyleSheet();
		StyleSheet ss = new StyleSheet();

		ss.addStyleSheet(styles);

		ss.addRule("body {font-family:arial;font-size:12pt}");
		ss.addRule("p {font-family:arial;margin:2}");

		// HTMLDocLinkDetector is class made which extends HTMLDocument
		HTMLDocument doc = new HTMLDocLinkDetector(ss);

		setEditorKit(htmlkit);

		setDocument(doc);

		addMouseListener(ml);
		addMouseMotionListener(mml);
		
			
	}

	protected class HTMLDocLinkDetector extends HTMLDocument {

		public HTMLDocLinkDetector(StyleSheet ss) {
			super(ss);
			// p - the new asynchronous loading priority; 
			// a value less than zero indicates that the document 
			// should not be loaded asynchronously
			setAsynchronousLoadPriority(4);
			// Sets the number of tokens to buffer before trying to 
			// update the documents element structure. 
			setTokenThreshold(100);
			// Sets the parser that is used by the methods that insert 
			// html into the existing document, such as setInnerHTML, 
			// and setOuterHTML. 
			// HTMLEditorKit.createDefaultDocument can also set the default parser. 
			setParser(new ParserDelegator());
		}

		/**
		 * Returns true if the Element contains a HTML.Tag.A attribute, false
		 * otherwise.
		 * 
		 * @param e
		 *            the Element to be checked
		 * @return
		 */
		protected boolean isLink(Element e) {
			
			return (e.getAttributes().getAttribute(HTML.Tag.A) != null);

		}

		/**
		 * This method corrects or creates a url contained in an Element as an
		 * hyperlink.
		 * 
		 * @param e
		 *            the Element to be computed
		 * @throws BadLocationException
		 */
		protected void computeLinks(Element e) throws BadLocationException {
			
			int caretPos = getCaretPosition(); // gets the position of 
											   // insert text caret
			try {
				if (isLink(e)) // if there is already a link in e which
							   // has been edited, correct the link
					correctLink(e);
				else			// if this is a new link being created in e
								// call createLink(e)
					createLink(e);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			setCaretPosition(Math.min(caretPos, getLength()));
		}

		/**
		 * The method corrects the url inside an Element, that is supposed to be
		 * an element containing a link only. This function is typically called
		 * when the url is beeing edited. What the function does is to remove
		 * the html tags, so the url is actually edited in plain text and not as
		 * an hyperlink.
		 * 
		 * @param e
		 *            the Element that contains the url
		 * @throws BadLocationException
		 * @throws IOException
		 */
		protected void correctLink(Element e) throws BadLocationException,
				IOException {
			
			int length = e.getEndOffset() - e.getStartOffset();
			
			boolean endOfDoc = e.getEndOffset() == getLength() + 1;

			// to avoid catching the final '\n' of the document.
			if (endOfDoc)
				length--;

			String text = getText(e.getStartOffset(), length);

			setOuterHTML(e, text);

			// insert final spaces ignored by the html
			Matcher spaceMatcher = Pattern.compile("(\\s+)$").matcher(text);

			
			if (spaceMatcher.find()) {// Returns true if, and only if, a subsequence of the input 
				// sequence matches this matcher's pattern
				String endingSpaces = spaceMatcher.group(1);
				insertString(Math.min(getLength(), e.getEndOffset()),
						endingSpaces, null);
			}
		}

		/**
		 * The method check if the element contains a url in plain text, and if
		 * so, it creates the html tag HTML.Tag.A to have the url displayed as
		 * an hyperlink.
		 * 
		 * @param e
		 *            element that contains the url
		 * @throws BadLocationException
		 * @throws IOException
		 */
		protected void createLink(Element e) throws BadLocationException,
				IOException {
			
			// This function gets called as we type each character. It 
			// continuously parses the text starting from last known hyperlink
			// position till the current caret position and tries to find out
			// whether a link has been entered. If it finds a link, it enables
			// the link and then changes the startOffset to the position where 
			// this newly found link has ended.
			
			int caretPos = getCaretPosition();
			int startOffset = e.getStartOffset();
			int length = e.getEndOffset() - e.getStartOffset();
			
			boolean endOfDoc = e.getEndOffset() == getLength() + 1;
			// to avoid catching the final '\n' of the document.
			if (endOfDoc)
				length--;

			// get the entire string starting from previous hyperlink or newline to caret position
			String text = getText(startOffset, length);
	
			// here we specify to the parser for the stings to parse in given text
			Matcher matcher = Pattern.compile(
					"(?i)(\\b(http://|https://|www.|ftp://|file:/|mailto:)\\S+)(\\s+)")
					.matcher(text);

			if (matcher.find()) {
				// if we find a hyperlink in given text
				String url = matcher.group(1);
				String prefix = matcher.group(2);
				String endingSpaces = matcher.group(3);
				// Example: if user types "this is a hyperlink www.google.com"
				// then following would be the conents of above String variables
				// url: www.google.com prefix: www. endingSpaces:
				
				// to ignore characters after the caret
				int validPos = startOffset + matcher.start(3) + 1;
				if (validPos > caretPos)
					return;

				Matcher dotEndMatcher = Pattern.compile("([\\W&&[^/]]+)$")
						.matcher(url);

				//Ending non alpha characters like [.,?%] shouldn't be included
				// in the url.
				String endingDots = "";
				if (dotEndMatcher.find()) {
					endingDots = dotEndMatcher.group(1);
					url = dotEndMatcher.replaceFirst("");
				}

				// Example: if user types "this is a hyperlink www.google.com"
				// then matcher.replaceFirst would the text as
				// text: this is a hyperlink <a href='www.google.com'>www.google.com</a>
				text = matcher.replaceFirst("<a href='" + url + "'>" + url
						+ "</a>" + endingDots + endingSpaces);
				
				// e - the branch element whose children will be replaced
				// text - the string to be parsed and assigned to e 
				setOuterHTML(e, text);

				// insert initial spaces in normal text which were ignored by the html
				Matcher spaceMatcher = Pattern.compile("^(\\s+)").matcher(text);

				if (spaceMatcher.find()) {
					String initialSpaces = spaceMatcher.group(1);
					insertString(startOffset, initialSpaces, null);
				}

				// insert final spaces in normal text ignored by the html
				spaceMatcher = Pattern.compile("(\\s+)$").matcher(text);

				if (spaceMatcher.find()) {
					String extraSpaces = spaceMatcher.group(1);
					int endoffset = e.getEndOffset();
					if (extraSpaces.charAt(extraSpaces.length() - 1) == '\n') {
						extraSpaces = extraSpaces.substring(0, extraSpaces
								.length() - 1);
						endoffset--;
					}
					insertString(Math.min(getLength(), endoffset), extraSpaces,
							null);
				}
				
			}
		}

		public void remove(int offs, int len) throws BadLocationException {
			// offs - the starting offset >= 0
			// len - the number of characters to remove >= 0
			super.remove(offs, len);
			Element e = getCharacterElement(offs - len);
			computeLinks(e);
		}

		
		public void insertString(int offs, String str, AttributeSet a)
				throws BadLocationException {
		 
			super.insertString(offs, str, a);
			Element e = getCharacterElement(offs);
			computeLinks(e);
		}
	}

}
