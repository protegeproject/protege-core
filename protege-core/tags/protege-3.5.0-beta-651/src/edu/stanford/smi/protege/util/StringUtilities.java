package edu.stanford.smi.protege.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import edu.stanford.smi.protege.model.Frame;

/**
 * Some utilities for working with Strings.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class StringUtilities {
	private static Logger log = Log.getLogger(StringUtilities.class);
	private static Random random = new Random();

	public static String capitalize(String words) {
		StringBuffer buffer = new StringBuffer();
		boolean isNewWord = true;
		int length = words.length();
		for (int i = 0; i < length; ++i) {
			char c = words.charAt(i);
			if (Character.isWhitespace(c)) {
				isNewWord = true;
			} else if (isNewWord) {
				c = Character.toUpperCase(c);
				isNewWord = false;
			}
			buffer.append(c);
		}
		return buffer.toString();
	}

	public static String replace(String text, String macro, String value) {
		StringBuffer buffer = new StringBuffer();
		int macroLen = macro.length();
		int start = 0;
		int end;
		while ((end = text.indexOf(macro, start)) != -1) {
			buffer.append(text.substring(start, end));
			buffer.append(value);
			start = end + macroLen;
		}
		buffer.append(text.substring(start));
		return buffer.toString();
	}

	public static String symbolToLabel(String symbol) {
		String words = symbol.replace('_', ' ');
		words = capitalize(words);
		return words;
	}

	public static String commaSeparatedList(Collection c) {
		StringBuffer text = new StringBuffer();
		if (c == null) { return "(empty)"; }
		Iterator i = c.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (text.length() > 0) {
				text.append(", ");
			}			
			text.append(o.toString());			
		}
		return text.toString();
	}
	
	public static String getShortClassName(String classname) {
		int index = classname.lastIndexOf('.') + 1;
		return classname.substring(index);
	}

	public static String getClassName(Class clas) {
		String s = clas.getName();
		return getShortClassName(s);
	}

	public static String getClassName(Object o) {
		return getClassName(o.getClass());
	}

	public static String stripHtmlTags(String html) {
		String text = "";
		if (html == null) { return text; }

		StringReader in = new StringReader(html);
		Html2Text parser = new Html2Text();
		try {
			parser.parse(in);
			text = parser.getText();
		} catch (IOException e) {
			Log.getLogger().log(Level.WARNING, "Error at converting html to text", e);
		}
		in.close();
		return text;
	}

	//a very careful method of getting the user friendly name of a frame
	public static String getFriendlyName(Frame frame) {
		String name = frame.getName();
		try {
			name = frame.getBrowserText();
		} catch (Throwable t) {
			Log.emptyCatchBlock(t);
		}
		return name;
	}
	
	private static final char SINGLE_QUOTE = '\'';
	
	public static String quote(String text) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(SINGLE_QUOTE);
		buffer.append(text);
		buffer.append(SINGLE_QUOTE);
		return buffer.toString();
	}
	
	public static String unquote(String text) {
		if ( text.length() > 0 &&
			 text.charAt(0) == SINGLE_QUOTE &&
			 text.charAt(text.length() - 1) == SINGLE_QUOTE) {
				return text.substring(1, text.length() - 1);
		}
		return text;
	}
	
	 public static String removeAllQuotes(String text) {
	        if (text != null && text.length() > 0 && text.charAt(0) == SINGLE_QUOTE
	                && text.charAt(text.length() - 1) == SINGLE_QUOTE) {
	            return text.replaceAll("'", "");
	        }
	        return text;
	}
	
    public static DigestAndSalt makeDigest(String password) {
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        String encodedSalt = encodeBytes(salt);
        return makeDigest(password, encodedSalt);
    }
    
    public static DigestAndSalt makeDigest(String password, String salt) {
    	MessageDigest messageDigest;
    	try {
    		messageDigest = MessageDigest.getInstance("MD5");
    	} catch (NoSuchAlgorithmException e) {
    		log.severe("Did not have MD5 algorithm");
    		throw new RuntimeException("Did not have MD5 algorithm");
    	}

    	messageDigest.update(salt.getBytes());
    	// ToDo Normalization should be done here -- Java 6
    	messageDigest.update(password.getBytes());
    	String digest = encodeBytes(messageDigest.digest());
    	return new DigestAndSalt(digest, salt);
    }
    
    private static String encodeBytes(byte[] bytes) {
        int stringLength = 2 * bytes.length;
        BigInteger bi = new BigInteger(1,  bytes);
        String encoded  = bi.toString(16);
        while (encoded.length() < stringLength) {
            encoded = "0" + encoded;
        }
        return encoded;
    }

	
}


class Html2Text extends HTMLEditorKit.ParserCallback {
	StringBuffer s;

	public void parse(Reader in) throws IOException {
		s = new StringBuffer();
		ParserDelegator delegator = new ParserDelegator();
		// the third parameter is TRUE to ignore charset directive
		delegator.parse(in, this, Boolean.TRUE);
	}

	@Override
	public void handleText(char[] text, int pos) {
		s.append(text);
	}

	public String getText() {
		return s.toString();
	}
}

