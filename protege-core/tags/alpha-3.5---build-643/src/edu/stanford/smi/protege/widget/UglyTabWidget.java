package edu.stanford.smi.protege.widget;

import edu.stanford.smi.protege.resource.*;

/**
 * A tab widget to display when a specified tab widget fails to load (the constructor or the initialize method throw
 * an exception).  Having the system display a "dead" tab widget leads to UI hangs that are difficult to diagnose.  It
 * is unclear which tab is causing the problem.  Thus we replace the bad tab with an Ugly one that "works".  This has
 * the advantage of making it clear which tab is broken.
 * 
 * @author  Ray Fergerson   <fergerson@smi.stanford.edu>
 */
public class UglyTabWidget extends AbstractTabWidget {

    private static final long serialVersionUID = -175426276108203092L;

    public void initialize() {
        setIcon(Icons.getUglyIcon());
    }
}
