package edu.stanford.smi.protege.resource;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import edu.stanford.smi.protege.util.ComponentUtilities;

class ReadonlyIconFilter extends RGBImageFilter {
    private static final int TRANSPARENT_COLOR = 0x00ffffff;
    private static final int LEVEL = 128;

    @Override
    public int filterRGB(int x, int y, int rgb) {
        if (rgb != TRANSPARENT_COLOR) {
            int a = LEVEL * 0x01000000;
            rgb = (rgb & 0x00ffffff) | a;
        }
        return rgb;
    }
}

class HiddenIconFilter extends GrayFilter {
    HiddenIconFilter() {
        super(true, 65);
    }
}

/**
 * Utility class for accessing icons stored in the Protege jar.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

public final class Icons {
    private static final Icon UGLY_ICON = new UglyIcon();
    private static Map<ResourceKey, Icon> _icons = new HashMap<ResourceKey, Icon>();
    private static final ImageFilter hiddenIconFilter = new HiddenIconFilter();
    private static final ImageFilter readonlyIconFilter = new ReadonlyIconFilter();

    // need to preload with all icons that don't come from a file.
    static {
        _icons.put(ResourceKey.COMPONENT_MENU, new ConfigureButtonIcon());
        _icons.put(ResourceKey.CLASS_ADD_SUPERCLASS, getAddClsIcon());
        _icons.put(ResourceKey.CLASS_REMOVE_SUPERCLASS, getRemoveClsIcon());
        _icons.put(ResourceKey.SLOT_ADD_SUPERSLOT, getAddSlotIcon());
        _icons.put(ResourceKey.SLOT_REMOVE_SUPERSLOT, getRemoveSlotIcon());
    }

    public static Icon lookupIcon(ResourceKey key) {
        Icon icon = _icons.get(key);
        if (icon == null || icon.getIconWidth() == -1) {
            String fileName = key.toString() + ".gif";
            icon = ComponentUtilities.loadImageIcon(Icons.class, "image/" + fileName);
            _icons.put(key, icon);
        }
        return icon;
    }

    public static Icon lookupActionIcon(ResourceKey key, boolean large, boolean disabled) {
        if (disabled) {
            key = new ResourceKey(key.toString() + ".gray");
        }
        if (large) {
            key = new ResourceKey(key.toString() + ".24");
        }
        return lookupIcon(key);
    }

    public static Icon getIcon(ResourceKey key) {
        Icon icon = lookupIcon(key);
        if (icon == null) {
            icon = UGLY_ICON;
        }
        return icon;
    }

    private static Icon getIcon(String name) {
        return getIcon(new ResourceKey(name));
    }

    private static Icon getActionIcon(ResourceKey name, boolean large, boolean disabled) {
        Icon icon = lookupActionIcon(name, large, disabled);
        if (icon == null) {
            icon = UGLY_ICON;
        }
        return icon;
    }

    private static Icon getObjectIcon(String name, boolean readonly, boolean isHidden) {
        Icon icon;
        if (isHidden) {
            icon = getFilteredIcon(name, "hidden", hiddenIconFilter);
        } else if (readonly) {
            icon = getFilteredIcon(name, "readonly", readonlyIconFilter);
        } else {
            icon = getIcon(name);
        }
        return icon;
    }

    private static Icon getFilteredIcon(String baseName, String extension, ImageFilter filter) {
        ResourceKey key = new ResourceKey(baseName + "." + extension);
        Icon filteredIcon = _icons.get(key);
        if (filteredIcon == null) {
            ImageIcon baseIcon = (ImageIcon) getIcon(baseName);
            ImageProducer source = new FilteredImageSource(baseIcon.getImage().getSource(), filter);
            filteredIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(source));
            _icons.put(key, filteredIcon);
        }
        return filteredIcon;
    }

    public static Icon getClsesIcon() {
        return getClsIcon();
    }

    public static Icon getSlotsIcon() {
        return getSlotIcon();
    }

    public static Icon getInstancesIcon() {
        return getInstanceIcon();
    }

    public static Icon getFacetsIcon() {
        return getFacetIcon();
    }

    public static Image getClsImage() {
        return getImage(getClsIcon());
    }

    public static Image getSlotImage() {
        return getImage(getSlotIcon());
    }

    public static Image getFacetImage() {
        return getImage(getFacetIcon());
    }

    public static Image getInstanceImage() {
        return getImage(getInstanceIcon());
    }

    private static Image getImage(Icon icon) {
        return ((ImageIcon) icon).getImage();
    }

    public static Icon getClsesAndInstancesIcon() {
        return getClsAndInstanceIcon();
    }

    public static Icon getHierarchyExpandedIcon() {
        return getIcon("hierarchy.expanded");
    }

    public static Icon getHierarchyCollapsedIcon() {
        return getIcon("hierarchy.collapsed");
    }

    public static Icon getConfigureIcon() {
        return new ConfigureButtonIcon();
    }

    public static Icon getLogo() {
        return getIcon("logo.banner");
    }

    public static Icon getAddClsIcon() {
        return getIcon(ResourceKey.CLASS_ADD);
    }

    public static Icon getAddInstanceIcon() {
        return getIcon(ResourceKey.INSTANCE_ADD);
    }

    public static Icon getAddSlotIcon() {
        return getIcon(ResourceKey.SLOT_ADD);
    }

    public static Icon getAddIcon() {
        return getIcon(ResourceKey.VALUE_ADD);
    }

    public static Icon getArchiveProjectIcon() {
        return getArchiveProjectIcon(false, false);
    }

    public static Icon getArchiveProjectIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.PROJECT_ARCHIVE, large, disabled);
    }

    public static Icon getBackIcon() {
        return getIcon("back");
    }

    public static Icon getBlankIcon() {
        return new BlankIcon();
    }

    public static Icon getCancelIcon() {
        return getIcon(ResourceKey.CANCEL_BUTTON_LABEL);
    }

    public static Icon getCascadeWindowsIcon() {
        return getCascadeWindowsIcon(false, false);
    }

    public static Icon getCascadeWindowsIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.CASCADE_WINDOWS, large, disabled);
    }

    public static Icon getCloseAllWindowsIcon() {
        return getCloseAllWindowsIcon(false, false);
    }

    public static Icon getCloseAllWindowsIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.CLOSE_ALL_WINDOWS, large, disabled);
    }

    public static Icon getCloseIcon() {
        return getOkIcon();
    }

    public static Icon getClsAndInstanceIcon() {
        return getIcon("class.instance");
    }

    public static Icon getClsIcon() {
        return getClsIcon(false, false, false, false);
    }

    public static Icon getClsIcon(boolean metaclass, boolean isAbstract, boolean readonly, boolean isHidden) {
        String name = "class";
        if (metaclass) {
            name += ".metaclass";
        }
        if (isAbstract) {
            name += ".abstract";
        }
        return getObjectIcon(name, readonly, isHidden);
    }

    public static Icon getInstanceCopyIcon() {
        return getIcon(ResourceKey.INSTANCE_COPY);
    }

    public static Icon getSlotHideNoteIcon() {
        return getIcon(ResourceKey.SLOT_NOTE_HIDE);
    }

    public static Icon getInstanceHideNoteIcon() {
        return getIcon(ResourceKey.INSTANCE_NOTE_HIDE);
    }

    public static Icon getClsHideNoteIcon() {
        return getIcon(ResourceKey.CLASS_NOTE_HIDE_ALL);
    }

    public static Icon getInstanceNoteIcon() {
        return getIcon(ResourceKey.INSTANCE_NOTE);
    }

    public static Icon getClsNoteIcon() {
        return getIcon(ResourceKey.CLASS_NOTE);
    }

    public static Icon getSlotNoteIcon() {
        return getIcon(ResourceKey.SLOT_NOTE);
    }

    public static Icon getCreateIcon() {
        return getIcon(ResourceKey.VALUE_CREATE);
    }

    public static Icon getCreateClsIcon() {
        return getIcon(ResourceKey.CLASS_CREATE);
    }

    public static Icon getCreateInstanceIcon() {
        return getIcon(ResourceKey.INSTANCE_CREATE);
    }

    public static Icon getCreateSlotIcon() {
        return getIcon(ResourceKey.SLOT_CREATE);
    }

    public static Icon getCreateClsNoteIcon() {
        return getIcon(ResourceKey.CLASS_NOTE_CREATE);
    }

    public static Icon getCreateInstanceNoteIcon() {
        return getIcon(ResourceKey.INSTANCE_NOTE_CREATE);
    }

    public static Icon getDeleteIcon() {
        return getIcon(ResourceKey.VALUE_DELETE);
    }

    public static Icon getDeleteClsIcon() {
        return getIcon(ResourceKey.CLASS_DELETE);
    }

    public static Icon getDeleteInstanceIcon() {
        return getIcon(ResourceKey.INSTANCE_DELETE);
    }

    public static Icon getDeleteSlotIcon() {
        return getIcon(ResourceKey.SLOT_DELETE);
    }

    public static Icon getDeleteClsNoteIcon() {
        return getIcon(ResourceKey.CLASS_NOTE_DELETE);
    }

    public static Icon getCreateSlotNoteIcon() {
        return getIcon(ResourceKey.SLOT_NOTE_CREATE);
    }

    public static Icon getDeleteSlotNoteIcon() {
        return getIcon(ResourceKey.SLOT_NOTE_DELETE);
    }

    public static Icon getDeleteInstanceNoteIcon() {
        return getIcon(ResourceKey.INSTANCE_NOTE_DELETE);
    }

    public static Icon getDownIcon() {
        return getIcon(ResourceKey.VALUE_MOVE_DOWN);
    }

    public static Icon getFacetIcon() {
        return getFacetIcon(false, false);
    }

    public static Icon getFacetIcon(boolean readonly, boolean hidden) {
        return getObjectIcon("facet", readonly, hidden);
    }

    public static Icon getFindIcon() {
        // return getIcon(ResourceKey.VALUE_SEARCH_FOR);
        return getIcon(ResourceKey.INSTANCE_SEARCH_FOR);
    }

    public static Icon getFindClsIcon() {
        return getIcon(ResourceKey.CLASS_SEARCH_FOR);
    }

    public static Icon getFindInstanceIcon() {
        return getIcon(ResourceKey.INSTANCE_SEARCH_FOR);
    }

    public static Icon getFindFormIcon() {
        return getIcon(ResourceKey.FORM_SEARCH_FOR);
    }

    public static Icon getFindSlotIcon() {
        return getIcon(ResourceKey.SLOT_SEARCH_FOR);
    }

    public static Icon getFormIcon() {
        return getFormIcon(false);
    }

    public static Icon getFormIcon(boolean customized) {
        String name = "form";
        if (customized) {
            name += ".customized";
        }
        return getIcon(name);
    }

    public static Icon getForwardIcon() {
        return getIcon("forward");
    }

    public static Icon getHomeIcon() {
        return getIcon("home");
    }

    public static Icon getInstanceIcon() {
        return getInstanceIcon(false, false);
    }

    public static Icon getInstanceIcon(boolean readonly, boolean isHidden) {
        return getObjectIcon("instance", readonly, isHidden);
    }

    public static Icon getLayoutLikeOtherFormIcon() {
        return getIcon(ResourceKey.FORM_LAYOUT_LIKE);
    }

    public static Icon getLogoIcon() {
        return getIcon("logo16");
    }

    public static Icon getNewProjectIcon() {
        return getNewProjectIcon(false, false);
    }

    public static Icon getNewProjectIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.PROJECT_NEW, large, disabled);
    }

    public static Icon getCutIcon() {
        return getCutIcon(false, false);
    }

    public static Icon getCutIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.CUT_ACTION, large, disabled);
    }

    public static Icon getCopyIcon() {
        return getCopyIcon(false, false);
    }

    public static Icon getCopyIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.COPY_ACTION, large, disabled);
    }

    public static Icon getPasteIcon() {
        return getPasteIcon(false, false);
    }

    public static Icon getPasteIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.PASTE_ACTION, large, disabled);
    }

    public static Icon getClearIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.CLEAR_ACTION, large, disabled);
    }

    public static Icon getNoIcon() {
        return getIcon(ResourceKey.NO_BUTTON_LABEL);
    }

    public static Icon getOkIcon() {
        return getIcon(ResourceKey.OK_BUTTON_LABEL);
    }

    public static Icon getOpenProjectIcon() {
        return getOpenProjectIcon(false, false);
    }

    public static Icon getOpenProjectIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.PROJECT_OPEN, large, disabled);
    }

    public static Icon getProjectIcon() {
        return getIcon("project");
    }

    public static Icon getSelectProjectIcon() {
        return getIcon("project_select");
    }

    public static Icon getProjectIcon(boolean readonly, boolean hidden) {
        return getObjectIcon("project", readonly, hidden);
    }

    public static Icon getQueryIcon() {
        return getIcon("query");
    }

    public static Icon getCopyQueryIcon() {
        return getIcon("query.copy");
    }

    public static Icon getDeleteQueryIcon() {
        return getIcon("query.delete");
    }

    public static Icon getViewQueryIcon() {
        return getIcon("query.view");
    }

    public static Icon getAddQueryLibraryIcon() {
        return getIcon("query.library.add");
    }

    public static Icon getRetrieveQueryLibraryIcon() {
        return getIcon("query.library.retrieve");
    }

    public static Icon getRedoIcon() {
        return getRedoIcon(false, false);
    }

    public static Icon getRedoIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.REDO_ACTION, large, disabled);
    }

    public static Icon getRelayoutIcon() {
        return getIcon(ResourceKey.FORM_RELAYOUT);
    }

    public static Icon getRemoveCustomizationsIcon() {
        return getIcon(ResourceKey.FORM_REMOVE_CUSTOMIZATIONS);
    }

    public static Icon getRemoveIcon() {
        return getIcon(ResourceKey.VALUE_REMOVE);
    }

    public static Icon getRemoveClsIcon() {
        return getIcon(ResourceKey.CLASS_REMOVE);
    }

    public static Icon getRemoveInstanceIcon() {
        return getIcon(ResourceKey.INSTANCE_REMOVE);
    }

    public static Icon getRemoveSlotIcon() {
        return getIcon(ResourceKey.SLOT_REMOVE);
    }

    public static Icon getRemoveSlotOverrideIcon() {
        return getIcon(ResourceKey.SLOT_REMOVE_FACET_OVERRIDES);
    }

    public static Icon getRevertProjectIcon() {
        return getRevertProjectIcon(false, false);
    }

    public static Icon getRevertProjectIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.PROJECT_REVERT, large, disabled);
    }

    public static Icon getSaveProjectIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.PROJECT_SAVE, large, disabled);
    }

    public static Icon getSaveProjectIcon() {
        return getSaveProjectIcon(false, false);
    }

    public static Icon getSlotIcon() {
        return getSlotIcon(false, false, false, false);
    }

    public static Icon getSlotIcon(boolean inherited, boolean overridden, boolean readonly, boolean isHidden) {
        String name = "slot";
        if (inherited) {
            name += ".inherited";
        }
        if (overridden) {
            name += ".overridden";
        }
        return getObjectIcon(name, readonly, isHidden);
    }

    public static Icon getLogoSplashIcon() {
        return getIcon("logo.splash");
    }

    public static Icon getLogoBannerIcon() {
        return getIcon("logo.banner");
    }

    public static Icon getUglyIcon() {
        return UGLY_ICON;
    }

    public static Icon getUndoIcon(boolean large, boolean disabled) {
        return getActionIcon(ResourceKey.UNDO_ACTION, large, disabled);
    }

    public static Icon getUndoIcon() {
        return getUndoIcon(false, false);
    }

    public static Icon getUpIcon() {
        return getIcon(ResourceKey.VALUE_MOVE_UP);
    }

    public static Icon getViewIcon() {
        return getIcon(ResourceKey.VALUE_VIEW);
    }

    public static Icon getViewClsIcon() {
        return getIcon(ResourceKey.CLASS_VIEW);
    }

    public static Icon getViewInstanceIcon() {
        return getIcon(ResourceKey.INSTANCE_VIEW);
    }

    public static Icon getViewClsReferencersIcon() {
        return getIcon(ResourceKey.CLASS_VIEW_REFERENCES);
    }

    public static Icon getViewInstanceReferencersIcon() {
        return getIcon(ResourceKey.INSTANCE_VIEW_REFERENCES);
    }

    public static Icon getViewSlotAtClassIcon() {
        return getIcon(ResourceKey.SLOT_VIEW_FACET_OVERRIDES);
    }

    public static Icon getViewSlotIcon() {
        return getIcon(ResourceKey.SLOT_VIEW);
    }

    public static Icon getViewFormIcon() {
        return getIcon(ResourceKey.FORM_VIEW_CUSTOMIZATIONS);
    }

    public static Icon getYesIcon() {
        return getIcon(ResourceKey.YES_BUTTON_LABEL);
    }

    public static Icon getQueryExportIcon() {
        return getIcon("export");
    }

    public static Icon getExportIcon() {
        return getIcon(ResourceKey.PROJECT_EXPORT);
    }
}