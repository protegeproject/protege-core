package edu.stanford.smi.protege.ui;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Renderer for frames.  This class has logic to render all type of frames: classes, slots, facet, simple instances.  
 * There isn't a separate renderer for each frame type because Protege's metaclass architecture is such that any list
 * of "instances" could contain any of the above types.  Since all elements of a list use the same renderer is just
 * makes sense to stuff the code for a frame types into a single place.  This class is big an clumsy though.  It should
 * probably instead delegate the rendering of each of the frame types to another instance.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameRenderer extends DefaultRenderer implements Cloneable {
    protected final static int NONE = 0;
    protected final static int DIRECT = 1;
    protected final static int ALL = 2;

    protected int _instanceCountType = NONE;
    protected boolean _hasLoadedIconFlags = false;
    protected boolean _displayFrameTypeIcon = true;
    protected boolean _displayAbstractIcon = true;
    protected boolean _displayMultipleParentsIcon = true;
    protected boolean _displayDefaultMetaclassIcon = true;
    protected boolean _displayHiddenIcon = true;

    protected boolean _displayType = false;

    protected static FrameRenderer _frameRendererPrototype = new FrameRenderer();

    public FrameRenderer() {
    }

    public static FrameRenderer createInstance() {
        FrameRenderer renderer;
        try {
            renderer = (FrameRenderer) _frameRendererPrototype.clone();
        } catch (CloneNotSupportedException e) {
            renderer = null;
        }
        return renderer;
    }

    protected void ensureIconFlagsLoaded() {
        if (!_hasLoadedIconFlags) {
            Project p = ProjectManager.getProjectManager().getCurrentProject();
            if (p != null) {
                _displayAbstractIcon = p.getDisplayAbstractClassIcon();
                _displayMultipleParentsIcon = p.getDisplayMultiParentClassIcon();
                _hasLoadedIconFlags = true;
            }
        }
    }

    protected String getInstanceCountString(Cls cls) {
        int count;
        switch (_instanceCountType) {
            case NONE :
                count = 0;
                break;
            case DIRECT :
                count = cls.getDirectInstanceCount();
                break;
            case ALL :
                count = cls.getInstanceCount();
                break;
            default :
                Assert.fail("bad type: " + _instanceCountType);
                count = 0;
        }
        String s;
        if (count > 0) {
            s = "  (" + count + ")";
        } else {
            s = "";
        }
        return s;
    }

    public void setDisplayType(boolean displayType) {
        _displayType = displayType;
    }

    public void load(Object value) {
        ensureIconFlagsLoaded();

        if (value instanceof Frame) {
            Frame frameValue = (Frame) value;
            if (!(frameValue.isEditable())) {
                setGrayedText(true);
            }
            if (frameValue.isDeleted()) {
                setMainIcon(null);
                setMainText("<deleted frame>");
            } else if (frameValue instanceof Cls) {
                loadCls((Cls) value);
            } else if (frameValue instanceof Slot) {
                loadSlot((Slot) value);
            } else if (frameValue instanceof Facet) {
                loadFacet((Facet) value);
            } else if (frameValue instanceof SimpleInstance) {
                loadInstance((Instance) value);
            }
        } else {
            setMainIcon(null);
            setMainText(value.toString());
        }
    }
    
    protected Icon getIcon(Cls cls) {
        return cls.getIcon();
    }

    protected void loadCls(Cls cls) {
        setMainIcon(getIcon(cls));
        setMainText(cls.getBrowserText());
        appendText(getInstanceCountString(cls));
        appendType(cls);
        setBackgroundSelectionColor(Colors.getClsSelectionColor());
    }

    protected void loadFacet(Facet facet) {
        setMainIcon(facet.getIcon());
        setMainText(facet.getBrowserText());
        appendType(facet);
    }

    protected void loadInstance(Instance instance) {
        String s = instance.getBrowserText();
        setMainText(s);
        setMainIcon(instance.getIcon());
        appendType(instance);
        setBackgroundSelectionColor(Colors.getInstanceSelectionColor());
    }

    public void loadNull() {
    }

    protected void loadSlot(Slot slot) {
        setMainIcon(slot.getIcon());
        setMainText(slot.getBrowserText());
        appendType(slot);
        setBackgroundSelectionColor(Colors.getSlotSelectionColor());
    }

    private void appendType(Instance instance) {
        if (_displayType) {
            String type = instance.getDirectType().getBrowserText();
            appendText("  (" + type + ")");
        }
    }

    public void setDisplayDirectInstanceCount(boolean b) {
        _instanceCountType = b ? DIRECT : NONE;
    }

    public void setDisplayFrameTypeIcon(boolean b) {
        _displayFrameTypeIcon = b;
    }

    public void setDisplayHiddenIcon(boolean b) {
        _displayHiddenIcon = b;
    }

    public void setDisplayInstanceCount(boolean b) {
        _instanceCountType = b ? ALL : NONE;
    }

    public void setDisplayTrailingIcons(boolean b) {
        _displayAbstractIcon = b;
        _displayMultipleParentsIcon = b;
        _displayDefaultMetaclassIcon = b;
        _hasLoadedIconFlags = true;
    }

    public void setMainIcon(Icon icon) {
        if (_displayFrameTypeIcon) {
            super.setMainIcon(icon);
        }
    }

    public static void setPrototypeInstance(FrameRenderer renderer) {
        _frameRendererPrototype = renderer;
    }
}
