package edu.stanford.smi.protege.storage.clips;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

/**
 * Panel to display the error messages resulting from parsing a clips file.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ParseErrorPanel extends JComponent {

    public ParseErrorPanel(Collection errors) {
        setLayout(new BorderLayout());
        JTextArea area = new JTextArea();
        add(new JScrollPane(area));
        area.setText(getText(errors));
        setPreferredSize(new Dimension(700, 400));
    }

    private static String getText(Collection errors) {
        StringBuffer buffer = new StringBuffer();
        Iterator i = errors.iterator();
        while (i.hasNext()) {
            String text;
            Object o = i.next();
            if (o instanceof Exception) {
                Exception e = (Exception) o;
                StringWriter s = new StringWriter();
                e.printStackTrace(new PrintWriter(s));
                text = s.toString();
            } else {
                text = (o == null) ? "missing message" : o.toString();
            }
            buffer.append(text);
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
