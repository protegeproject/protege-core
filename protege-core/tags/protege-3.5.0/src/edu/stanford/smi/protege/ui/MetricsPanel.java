package edu.stanford.smi.protege.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Panel to display a set of metrics that describe a project.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MetricsPanel extends JComponent {
    private static final long serialVersionUID = 2991659380230833251L;
    private FrameCounts _frameCounts;
    private DefaultTableModel _summaryModel;

    public MetricsPanel(Project p) {
        _frameCounts = p.getKnowledgeBase().getFrameCounts();
        setLayout(new BorderLayout());
        add(createSummaryPanel());

    }

    private Object[] createClsSummary() {
        return new Object[] { "Classes", new Integer(_frameCounts.getSystemClsCount()),
                new Integer(_frameCounts.getIncludedClsCount()),
                new Integer(_frameCounts.getDirectClsCount()),
                new Integer(_frameCounts.getTotalClsCount()) };
    }

    private Object[] createFacetSummary() {
        return new Object[] { "Facets", new Integer(_frameCounts.getSystemFacetCount()),
                new Integer(_frameCounts.getIncludedFacetCount()),
                new Integer(_frameCounts.getDirectFacetCount()),
                new Integer(_frameCounts.getTotalFacetCount()) };
    }

    private Object[] createFrameSummary() {
        return new Object[] { "Frames", new Integer(_frameCounts.getSystemFrameCount()),
                new Integer(_frameCounts.getIncludedFrameCount()),
                new Integer(_frameCounts.getDirectFrameCount()),
                new Integer(_frameCounts.getTotalFrameCount()) };
    }

    private Object[] createInstanceSummary() {
        return new Object[] { "Instances",
                new Integer(_frameCounts.getSystemSimpleInstanceCount()),
                new Integer(_frameCounts.getIncludedSimpleInstanceCount()),
                new Integer(_frameCounts.getDirectSimpleInstanceCount()),
                new Integer(_frameCounts.getTotalSimpleInstanceCount()) };
    }

    private Object[] createSlotSummary() {
        return new Object[] { "Slots", new Integer(_frameCounts.getSystemSlotCount()),
                new Integer(_frameCounts.getIncludedSlotCount()),
                new Integer(_frameCounts.getDirectSlotCount()),
                new Integer(_frameCounts.getTotalSlotCount()) };
    }

    private JComponent createSummaryPanel() {
        _summaryModel = new DefaultTableModel();
        _summaryModel.addColumn("");
        _summaryModel.addColumn("System");
        _summaryModel.addColumn("Included");
        _summaryModel.addColumn("Direct");
        _summaryModel.addColumn("Total");
        return createTable(_summaryModel, "Summary");
    }

    private void updateSummaryModel() {
        _summaryModel.addRow(createClsSummary());
        _summaryModel.addRow(createSlotSummary());
        _summaryModel.addRow(createFacetSummary());
        _summaryModel.addRow(createInstanceSummary());
        _summaryModel.addRow(createFrameSummary());
    }

    private JComponent createTable(DefaultTableModel model, String headerString) {
        JTable table = ComponentFactory.createTable(null);
        table.setModel(model);
        updateSummaryModel();
        table.createDefaultColumnsFromModel();
        ((JLabel) table.getDefaultRenderer(Object.class))
                .setHorizontalAlignment(SwingConstants.RIGHT);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(95);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        column.setCellRenderer(renderer);

        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        table.setDefaultRenderer(Double.class, r);

        table.setEnabled(false);

        JComponent c = new JPanel(new BorderLayout());
        JTableHeader header = table.getTableHeader();
        c.add(table, BorderLayout.CENTER);
        c.add(header, BorderLayout.NORTH);
        headerSizeFix(header);
        return new LabeledComponent(headerString, c);
    }

    private static void headerSizeFix(JTableHeader header) {
        // workaround for JDK 1.4.2 bug
        int height = header.getPreferredSize().height;
        if (height < 16) {
            header.setPreferredSize(new Dimension(0, 16));
        }
    }
}