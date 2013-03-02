package edu.stanford.smi.protege.resource;

/**
 * TODO Class Comment
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ResourceKey {
    private String key;

    public ResourceKey(String key) {
        this.key = key;
    }

    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof ResourceKey) {
            ResourceKey rhs = (ResourceKey) o;
            equals = key.equals(rhs.key);
        }
        return equals;
    }

    public int hashCode() {
        return key.hashCode();
    }

    public String toString() {
        return key;
    }

    private static ResourceKey key(String s) {
        return new ResourceKey(s);
    }

    public static final ResourceKey MENUBAR_FILE = key("menubar.file");
    public static final ResourceKey MENUBAR_PROJECT = key("menubar.project");
    public static final ResourceKey MENUBAR_EDIT = key("menubar.edit");
    public static final ResourceKey MENUBAR_HELP = key("menubar.help");
    public static final ResourceKey MENUBAR_WINDOW = key("menubar.window");

    public static final ResourceKey PROJECT_NEW = key("project.new");
    public static final ResourceKey PROJECT_OPEN = key("project.open");
    public static final ResourceKey PROJECT_OPEN_RECENT = key("project.open_recent");
    public static final ResourceKey PROJECT_OPEN_REMOTE = key("project.open_remote");
    public static final ResourceKey PROJECT_CLOSE = key("project.close");
    public static final ResourceKey PROJECT_SAVE = key("project.save");
    public static final ResourceKey PROJECT_SAVE_AS = key("project.save_as");
    public static final ResourceKey PROJECT_SAVE_TO_FORMAT = key("project.save_to_format");
    public static final ResourceKey PROJECT_BUILD = key("project.build");
    public static final ResourceKey PROJECT_IMPORT = key("project.import");
    public static final ResourceKey PROJECT_EXPORT = key("project.export");
    public static final ResourceKey PROJECT_CONVERT = key("project.convert");
    public static final ResourceKey PROJECT_IMPORT_TO_STANDARD = key("project.import_to_standard");
    public static final ResourceKey PROJECT_EXPORT_TO_FORMAT = key("project.export_to_format");
    public static final ResourceKey PROJECT_GENERATE_HTML = key("project.generate_html");
    public static final ResourceKey PROJECT_ARCHIVE = key("project.archive");
    public static final ResourceKey PROJECT_CHANGE_INCLUDED = key("project.change_included");
    public static final ResourceKey PROJECT_CONFIGURE_ARCHIVE = key("project.configure_archive");
    public static final ResourceKey PROJECT_CONFIGURE = key("project.configure");
    public static final ResourceKey PROJECT_INCLUDE = key("project.include");
    public static final ResourceKey PROJECT_MANAGE_INCLUDED = key("project.manage_included");
    public static final ResourceKey PROJECT_MANAGE_INCLUDED_DIALOG = key("project.manage_included_dialog");
    public static final ResourceKey PROJECT_MERGE_INCLUDED = key("project.merge_included");
    public static final ResourceKey PROJECT_REVERT = key("project.revert");
    public static final ResourceKey PROJECT_FILE_ENCODINGS = key("project.file_encodings");
    public static final ResourceKey PROJECT_SHOW_INCLUDED = key("project.show_included");
    public static final ResourceKey PROJECT_METRICS = key("project.metrics");

    public static final ResourceKey APPLICATION_PREFERENCES = key("application.preferences");
    public static final ResourceKey APPLICATION_EXIT = key("application.exit");

    public static final ResourceKey CUT_ACTION = key("cut");
    public static final ResourceKey COPY_ACTION = key("copy");
    public static final ResourceKey PASTE_ACTION = key("paste");
    public static final ResourceKey UNDO_ACTION = key("undo");
    public static final ResourceKey REDO_ACTION = key("redo");
    public static final ResourceKey CLEAR_ACTION = key("clear");
    public static final ResourceKey COMMAND_HISTORY_ACTION = key("command_history");
    public static final ResourceKey INSERT_UNICODE_ACTION = key("insert_unicode");
    public static final ResourceKey CONVERT_UNICODE_SEQUENCE_ACTION = key("convert_unicode_sequence");

    public static final ResourceKey SYNCHRONIZE_CLASS_TREE = key("synchronize_class_trees");
    public static final ResourceKey AUTOSYNCHRONIZE_CLASS_TREES = key("autosynchronize_class_trees");
    public static final ResourceKey DETACH_VIEW = key("detach_view");
    public static final ResourceKey CLOSE_VIEW = key("close_view");
    public static final ResourceKey CASCADE_WINDOWS = key("cascade_windows");
    public static final ResourceKey CLOSE_ALL_WINDOWS = key("close_all_windows");
    public static final ResourceKey INCREASE_FONT_SIZE = key("increase_font_size");
    public static final ResourceKey DECREASE_FONT_SIZE = key("decrease_font_size");
    public static final ResourceKey LOOK_AND_FEEL = key("look_and_feel");

    public static final ResourceKey HELP_MENU_ICONS = key("help_menu.icons");
    public static final ResourceKey HELP_MENU_GETTING_STARTED = key("help_menu.getting_started");
    public static final ResourceKey HELP_MENU_FAQ = key("help_menu.faq");
    public static final ResourceKey HELP_MENU_USERS_GUIDE = key("help_menu.users_guide");
    public static final ResourceKey HELP_MENU_ONTOLOGIES_101 = key("help_menu.ontologies_101");
    public static final ResourceKey HELP_MENU_PLUGINS = key("help_menu.plugins");
    public static final ResourceKey HELP_MENU_ABOUT = key("help_menu.about");
    public static final ResourceKey HELP_MENU_ABOUT_PLUGINS = key("help_menu.about_plugins");
    public static final ResourceKey HELP_MENU_CITE_PROTEGE = key("help_menu.cite_protege");

    public static final ResourceKey CLASS_NOTE = key("class_note");
    public static final ResourceKey CLASS_NOTE_CREATE = key("class_note.create");
    public static final ResourceKey CLASS_NOTE_DELETE = key("class_note.delete");
    public static final ResourceKey CLASS_NOTE_HIDE_ALL = key("class_note.hide");

    public static final ResourceKey CLASS_VIEW = key("class.view");
    public static final ResourceKey CLASS_VIEW_REFERENCES = key("class.view_references");
    public static final ResourceKey CLASS_SEARCH_FOR = key("class.search");
    public static final ResourceKey CLASS_CREATE = key("class.create");
    public static final ResourceKey CLASS_CREATE_SUBCLASS = key("class.create_subclass");
    public static final ResourceKey CLASS_DELETE = key("class.delete");
    public static final ResourceKey CLASS_ADD = key("class.add");
    public static final ResourceKey CLASS_REMOVE = key("class.remove");
    public static final ResourceKey CLASS_ADD_SUPERCLASS = key("class.add_superclass");
    public static final ResourceKey CLASS_REMOVE_SUPERCLASS = key("class.remove_superclass");
    public static final ResourceKey CLASS_COPY = key("class.copy");
    public static final ResourceKey CLASS_MOVE_UP = key("class.move_up");
    public static final ResourceKey CLASS_MOVE_DOWN = key("class.move_down");

    public static final ResourceKey INSTANCE_NOTE = key("instance_note");
    public static final ResourceKey INSTANCE_NOTE_CREATE = key("instance_note.create");
    public static final ResourceKey INSTANCE_NOTE_DELETE = key("instance_note.delete");
    public static final ResourceKey INSTANCE_NOTE_HIDE = key("instance_note.hide");

    public static final ResourceKey INSTANCE_VIEW = key("instance.view");
    public static final ResourceKey INSTANCE_VIEW_REFERENCES = key("instance.view_references");
    public static final ResourceKey INSTANCE_SEARCH_FOR = key("instance.search");
    public static final ResourceKey INSTANCE_CREATE = key("instance.create");
    public static final ResourceKey INSTANCE_DELETE = key("instance.delete");
    public static final ResourceKey INSTANCE_ADD = key("instance.add");
    public static final ResourceKey INSTANCE_REMOVE = key("instance.remove");
    public static final ResourceKey INSTANCE_COPY = key("instance.copy");
    public static final ResourceKey INSTANCE_MOVE_UP = key("instance.move_up");
    public static final ResourceKey INSTANCE_MOVE_DOWN = key("instance.move_down");

    public static final ResourceKey SLOT_NOTE = key("slot_note");
    public static final ResourceKey SLOT_NOTE_CREATE = key("slot_note.create");
    public static final ResourceKey SLOT_NOTE_DELETE = key("slot_note.delete");
    public static final ResourceKey SLOT_NOTE_HIDE = key("slot_note.hide");

    public static final ResourceKey SLOT_VIEW = key("slot.view");
    public static final ResourceKey SLOT_VIEW_TOP_LEVEL = key("slot.view_top_level");
    public static final ResourceKey SLOT_VIEW_FACET_OVERRIDES = key("slot.view_overrides");
    public static final ResourceKey SLOT_VIEW_REFERENCES = key("slot.view_references");
    public static final ResourceKey SLOT_SEARCH_FOR = key("slot.search");
    public static final ResourceKey SLOT_CREATE = key("slot.create");
    public static final ResourceKey SLOT_CREATE_SUBSLOT = key("slot.create_subslot");
    public static final ResourceKey SLOT_DELETE = key("slot.delete");
    public static final ResourceKey SLOT_ADD = key("slot.add");
    public static final ResourceKey SLOT_REMOVE = key("slot.remove");
    public static final ResourceKey SLOT_REMOVE_FACET_OVERRIDES = key("slot.remove_overrides");
    public static final ResourceKey SLOT_ADD_SUPERSLOT = key("slot.add_superslot");
    public static final ResourceKey SLOT_REMOVE_SUPERSLOT = key("slot.remove_superslot");
    public static final ResourceKey SLOT_COPY = key("slot.copy");
    public static final ResourceKey SLOT_MOVE_UP = key("slot.move_up");
    public static final ResourceKey SLOT_MOVE_DOWN = key("slot.move_down");

    public static final ResourceKey FACET_VIEW = key("facet.view");
    public static final ResourceKey FACET_VIEW_REFERENCES = key("facet.view_references");
    public static final ResourceKey FACET_CREATE = key("facet.create");
    public static final ResourceKey FACET_DELETE = key("facet.delete");

    public static final ResourceKey VALUE_VIEW = key("object.view");
    public static final ResourceKey VALUE_VIEW_REFERENCES = key("object.view_references");
    public static final ResourceKey VALUE_CREATE = key("object.create");
    public static final ResourceKey VALUE_DELETE = key("object.delete");
    public static final ResourceKey VALUE_ADD = key("object.add");
    public static final ResourceKey VALUE_REMOVE = key("object.remove");
    public static final ResourceKey VALUE_COPY = key("object.copy");
    public static final ResourceKey VALUE_MOVE_UP = key("object.move_up");
    public static final ResourceKey VALUE_MOVE_DOWN = key("object.move_down");

    public static final ResourceKey FORM_SEARCH_FOR = key("form.search");
    public static final ResourceKey FORM_VIEW_CUSTOMIZATIONS = key("form.view_customizations");
    public static final ResourceKey FORM_REMOVE_CUSTOMIZATIONS = key("form.remove_customizations");
    public static final ResourceKey FORM_LAYOUT_LIKE = key("form.layout_like");
    public static final ResourceKey FORM_RELAYOUT = key("form.relayout");

    public static final ResourceKey PROJECT_ADD = key("project.add");
    public static final ResourceKey PROJECT_REMOVE = key("project.remove");

    public static final ResourceKey COMPONENT_MENU = key("component.configure");

    public static final ResourceKey URL_VIEW_IN_BROWSER = key("object.view_in_browser");

    public static final ResourceKey CLASS_BROWSER_TITLE = key("class_browser.title");
    public static final ResourceKey CLASS_BROWSER_FOR_PROJECT_LABEL = key("class_browser.for_project");
    public static final ResourceKey CLASS_BROWSER_HIERARCHY_LABEL = key("class_browser.class_hierarchy");
    public static final ResourceKey CLASS_BROWSER_ALL_RELATIONS_LABEL = key("class_browser.all_relations");
    public static final ResourceKey CLASS_BROWSER_SUPERCLASSES_LABEL = key("class_browser.superclasses");
    public static final ResourceKey CLASS_BROWSER_SHOW_CLASS_HIERARCHY_MENU_ITEM = key("class_browser.show_class_hierarchy");
    public static final ResourceKey CLASS_BROWSER_SHOW_ALL_RELATIONS_MENU_ITEM = key("class_browser.show_all_relations");
    public static final ResourceKey CLASS_BROWSER_SHOW_RELATION_MENU_ITEM = key("class_browser.show_relation");
    public static final ResourceKey CLASS_BROWSER_COLLAPSE_TREE_MENU_ITEM = key("class_browser.collapse_tree");
    public static final ResourceKey CLASS_BROWSER_EXPAND_TREE_MENU_ITEM = key("class_browser.expand_tree");
    public static final ResourceKey CLASS_BROWSER_SET_AS_DEFAULT_METACLASS_MENU_ITEM = key("class_browser.set_as_default_metaclass");
    public static final ResourceKey CLASS_BROWSER_UNSET_AS_DEFAULT_METACLASS_MENU_ITEM = key("class_browser.unset_as_default_metaclass");
    public static final ResourceKey CLASS_BROWSER_SET_AS_DEFAULT_METASLOT_MENU_ITEM = key("class_browser.set_as_default_metaslot");
    public static final ResourceKey CLASS_BROWSER_UNSET_AS_DEFAULT_METASLOT_MENU_ITEM = key("class_browser.unset_as_default_metaslot");
    public static final ResourceKey CLASS_BROWSER_CHANGE_METACLASS_MENU_ITEM = key("class_browser.change_metaclass");
    public static final ResourceKey CLASS_BROWSER_CHANGE_METACLASS_OF_SUBCLASSES_MENU_ITEM = key("class_browser.change_metaclass_of_subclasses");
    public static final ResourceKey CLASS_BROWSER_CREATE_SUBCLASS_USING_METACLASS_MENU_ITEM = key("class_browser.create_subclass_using_metaclass");
    public static final ResourceKey CLASS_BROWSER_HIDE_CLASS_MENU_ITEM = key("class_browser.hide_class");
    public static final ResourceKey CLASS_BROWSER_UNHIDE_CLASS_MENU_ITEM = key("class_browser.unhide_class");

    public static final ResourceKey CLASS_EDITOR_TITLE = key("class_editor.title");
    public static final ResourceKey CLASS_EDITOR_FOR_CLASS_LABEL = key("class_editor.for_class");

    public static final ResourceKey SLOT_BROWSER_TITLE = key("slot_browser.title");
    public static final ResourceKey SLOT_BROWSER_HIERARCHY_LABEL = key("slot_browser.slot_hierarchy");
    public static final ResourceKey SLOT_BROWSER_SUPERSLOTS_LABEL = key("slot_browser.superslots");

    public static final ResourceKey SLOT_EDITOR_TITLE = key("slot_editor.title");
    public static final ResourceKey SLOT_EDITOR_FOR_SLOT_LABEL = key("slot_editor.for_slot");

    public static final ResourceKey INSTANCE_BROWSER_TITLE = key("instance_browser.title");

    public static final ResourceKey INSTANCE_EDITOR_TITLE = key("instance_editor.title");
    public static final ResourceKey INSTANCE_EDITOR_FOR_INSTANCE_LABEL = key("instance_editor.for_instance");

    public static final ResourceKey FRAME_EDITOR_FRAME_NAME = key("frame_editor.frame_name");
    public static final ResourceKey FRAME_EDITOR_FRAME_TYPE = key("frame_editor.frame_type");
    public static final ResourceKey FRAME_EDITOR_FRAME_TYPE_AND_NAME = key("frame_editor.frame_type_and_name");

    public static final ResourceKey FORM_BROWSER_TITLE = key("form_browser.title");
    public static final ResourceKey FORM_BROWSER_FORMS_LABEL = key("form_browser.forms");

    public static final ResourceKey FORM_EDITOR_TITLE = key("form_editor.title");
    public static final ResourceKey FORM_EDITOR_SELECTED_WIDGET_TYPE_LABEL = key("form_editor.selected_widget_type");
    public static final ResourceKey FORM_EDITOR_SELECT_A_WIDGET_PROMPT = key("form_editor.select_a_widget");
    public static final ResourceKey FORM_EDITOR_DISPLAY_SLOT_LABEL = key("form_editor.display_slot");
    public static final ResourceKey FORM_EDITOR_SELECT_NO_WIDGET = key("form_editor.none");

    public static final ResourceKey CLASSES_VIEW_TITLE = key("classes_view.title");
    public static final ResourceKey SLOTS_VIEW_TITLE = key("slots_view.title");
    public static final ResourceKey FORMS_VIEW_TITLE = key("forms_view.title");
    public static final ResourceKey INSTANCES_VIEW_TITLE = key("instances_view.title");

    public static final ResourceKey OK_BUTTON_LABEL = key("ok");
    public static final ResourceKey CANCEL_BUTTON_LABEL = key("cancel");
    public static final ResourceKey YES_BUTTON_LABEL = key("yes");
    public static final ResourceKey NO_BUTTON_LABEL = key("no");
    public static final ResourceKey CLOSE_BUTTON_LABEL = key("close");
    public static final ResourceKey DIALOG_SAVE_CHANGES_TEXT = key("save_changes_text");
    public static final ResourceKey DIALOG_CONFIRM_REMOVE_TEXT = key("confirm_remove_text");
    public static final ResourceKey DIALOG_CONFIRM_DELETE_TEXT = key("confirm_delete_text");

    public static final ResourceKey DATABASE_CONFIGURATION_DIALOG_TITLE = key("database_project_configuration_dialog.title");
    public static final ResourceKey SAVE_PROJECT_FAILED_DIALOG_TITLE = key("save_project_failed_dialog.title");
    public static final ResourceKey RELOAD_PROJECT_FAILED_DIALOG_TITLE = key("reload_project_failed_dialog.title");
    public static final ResourceKey ERRORS_DIALOG_TITLE = key("errors_dialog.title");
    public static final ResourceKey REMOTE_HOST_CONNECT_DIALOG_TITLE = key("remote_host_connect_dialog.title");
    public static final ResourceKey REMOTE_PROJECT_SELECT_DIALOG_TITLE = key("remote_project_select_dialog.title");
    public static final ResourceKey REMOTE_CONNECT_FAILED_DIALOG_TEXT = key("remote_connect_failed_dialog.text");
    public static final ResourceKey REMOTE_SESSION_CREATE_FAILED_DIALOG_TEXT = key("remote_session_create_failed_dialog.text");
    public static final ResourceKey REMOTE_SESSION_CREATE_FAILED_DIALOG_TITLE = key("remote_session_create_failed_dialog.title");
    public static final ResourceKey ARCHIVE_FAILED_DIALOG_TEXT = key("achive_failed_dialog.text");
    public static final ResourceKey DIRECTLY_INCLUDED_PROJECTS_DIALOG_TITLE = key("directly_included_projects_dialog.title");
    public static final ResourceKey CONFIGURE_ARCHIVE_DIALOG_TITLE = key("configure_archive_dialog.title");
    public static final ResourceKey DELETE_CLASS_FAILED_DIALOG_TEXT = key("delete_class_failed_dialog.text");
    public static final ResourceKey EXPORT_DIALOG_TITLE = key("export_dialog.title");
    public static final ResourceKey GENERATE_HTML_OPTIONS_DIALOG_TITLE = key("generate_html_options_dialog.title");
    public static final ResourceKey IMPORT_DIALOG_TITLE = key("import_dialog.title");
    public static final ResourceKey COMMAND_HISTORY_DIALOG_TITLE = key("command_history_dialog.title");
    public static final ResourceKey ENCODINGS_DIALOG_TITLE = key("encodings_dialog.title");
    public static final ResourceKey INCLUDED_PROJECTS_DIALOG_TITLE = key("included_projects_dialog.title");
    public static final ResourceKey METRICS_DIALOG_TITLE = key("metrics_dialog.title");
    public static final ResourceKey INCLUDE_PROJECT_FAILED_DIALOG_RECURSIVE_INCLUDE_TEXT = key("include_project_failed_dialog.recursive_include_text");
    public static final ResourceKey INCLUDE_PROJECT_FILED_DIALOG_ALREADY_INCLUDED_TEXT = key("include_project_failed_dialog.already_included_text");
    public static final ResourceKey COPY_DIALOG_TITLE = key("copy_dialog.title");
    public static final ResourceKey ABOUT_APPLICATION_DIALOG_TITLE = key("about_application_dialog.title");
    public static final ResourceKey ABOUT_PLUGINS_DIALOG_TITLE = key("about_plugins_dialog.title");
    public static final ResourceKey CLIPS_FILES_TO_EXPORT_DIALOG_TITLE = key("clips_files_to_export_dialog.title");
    public static final ResourceKey CLIPS_FILES_TO_IMPORT_DIALOG_TITLE = key("clips_files_to_import_dialog.title");
    public static final ResourceKey DATABASE_CONFIGURE_FAILED_DIALOG_DRIVER_NOT_FOUND_TEXT = key("database_configure_failed_dialog.driver_not_found_text");
    public static final ResourceKey DATABASE_CONFIGURE_FAILED_DIALOG_CANNOT_CREATE_CONNECTION_TEXT = key("database_configure_failed_dialog.cannot_create_connection_text");
    public static final ResourceKey ERROR_DIALOG_TITLE = key("error_dialog.title");
    public static final ResourceKey PREFERENCES_DIALOG_TITLE = key("preferences_dialog.title");
    public static final ResourceKey OPEN_PROJECT_DIALOG_TITLE = key("open_project_dialog.title");

    public static final ResourceKey WELCOME_DIALOG_HELP_TITLE = key("welcome_dialog.help_title");
    public static final ResourceKey WELCOME_DIALOG_GETTING_STARTED = key("welcome_dialog.getting_started");
    public static final ResourceKey WELCOME_DIALOG_ALL_TOPICS = key("welcome_dialog.all_topics");
    public static final ResourceKey WELCOME_DIALOG_USERS_GUIDE = key("welcome_dialog.users_guide");
    public static final ResourceKey WELCOME_DIALOG_FAQ = key("welcome_dialog.faq");
    public static final ResourceKey WELCOME_DIALOG_NEW = key("welcome_dialog.new");
    public static final ResourceKey WELCOME_DIALOG_NEW_TOOLTIP = key("welcome_dialog.new_tooltip");
    public static final ResourceKey WELCOME_DIALOG_OPEN = key("welcome_dialog.open");
    public static final ResourceKey WELCOME_DIALOG_OPEN_TOOLTIP = key("welcome_dialog.open_tooltip");
    public static final ResourceKey WELCOME_DIALOG_OPEN_OTHER = key("welcome_dialog.open_other");
    public static final ResourceKey WELCOME_DIALOG_OPEN_OTHER_TOOLTIP = key("welcome_dialog.open_existing_tooltip");
    public static final ResourceKey WELCOME_DIALOG_OPEN_RECENT_PROJECT_TITLE = key("welcome_dialog.open_recent_project_title");

    // These really should be part of the knowledge-base localization scheme
    public static final ResourceKey NAME_SLOT_WIDGET_LABEL = key("slot_widget_label.name");
    public static final ResourceKey DOCUMENTATION_SLOT_WIDGET_LABEL = key("slot_widget_label.documentation");
    public static final ResourceKey TEMPLATE_SLOTS_SLOT_WIDGET_LABEL = key("slot_widget_label.template_slots");
    public static final ResourceKey TEMPLATE_SLOTS_SLOT_WIDGET_NAME = key("slot_widget_label.template_slots.name");
    public static final ResourceKey TEMPLATE_SLOTS_SLOT_WIDGET_CARDINALITY = key("slot_widget_label.template_slots.cardinality");
    public static final ResourceKey TEMPLATE_SLOTS_SLOT_WIDGET_TYPE = key("slot_widget_label.template_slots.type");
    public static final ResourceKey TEMPLATE_SLOTS_SLOT_WIDGET_OTHER_FACETS = key("slot_widget_label.template_slots.other_facets");

    public static final ResourceKey ROLE_SLOT_WIDGET_LABEL = key("slot_widget_label.role");
    public static final ResourceKey CONSTRAINTS_SLOT_WIDGET_LABEL = key("slot_widget_label.constraints");
    public static final ResourceKey INVERSE_SLOT_WIDGET_LABEL = key("slot_widget_label.inverse");
    public static final ResourceKey MINIMUM_CARDINALITY_SLOT_WIDGET_LABEL = key("slot_widget_label.minimum_cardinality");
    public static final ResourceKey NUMERIC_MINIMUM_SLOT_WIDGET_LABEL = key("slot_widget_label.numeric_minimum");
    public static final ResourceKey NUMERIC_MAXIMUM_SLOT_WIDGET_LABEL = key("slot_widget_label.numeric_maximum");
    public static final ResourceKey VALUE_TYPE_SLOT_WIDGET_LABEL = key("slot_widget_label.value_type");
    public static final ResourceKey DEFAULT_SLOT_WIDGET_LABEL = key("slot_widget_label.default");
    public static final ResourceKey TEMPLATE_VALUES_SLOT_WIDGET_LABEL = key("slot_widget_label.template_values");
    public static final ResourceKey SUPERCLASSES_SLOT_WIDGET_LABEL = key("slot_widget_label.superclasses");
    public static final ResourceKey SUBCLASSES_SLOT_WIDGET_LABEL = key("slot_widget_label.subclasses");
    public static final ResourceKey SUPERSLOTS_SLOT_WIDGET_LABEL = key("slot_widget_label.superslots");
    public static final ResourceKey SUBSLOTS_SLOT_WIDGET_LABEL = key("slot_widget_label.subslots");
    public static final ResourceKey DOMAIN_SLOT_WIDGET_LABEL = key("slot_widget_label.domain");
    public static final ResourceKey DIRECT_TYPES_SLOT_WIDGET_LABEL = key("slot_widget_label.direct_types");
    public static final ResourceKey DIRECT_INSTANCES_SLOT_WIDGET_LABEL = key("slot_widget_label.direct_instances");
    public static final ResourceKey AT_MOST_LABEL = key("slot_widget_label.maximum_cardinality.at_most");
    public static final ResourceKey AT_LEAST_LABEL = key("slot_widget_label.minimum_cardinality.at_least");
    public static final ResourceKey REQUIRED_LABEL = key("slot_widget_label.minimum_cardinality.required");
    public static final ResourceKey MULTIPLE_LABEL = key("slot_widget_label.maximum_cardinality.multiple");

    public static final ResourceKey ALLOWED_CLASSES = key("slot_widget_label.value_type.allowed_classes");
    public static final ResourceKey ALLOWED_SUPERCLASSES = key("slot_widget_label.value_type.allowed_superclasses");
    public static final ResourceKey ALLOWED_VALUES = key("slot_widget_label.value_type.allowed_values");
    public static final ResourceKey TYPE_INSTANCE = key("slot_widget_label.value_type.type_instance");
    public static final ResourceKey TYPE_CLASS = key("slot_widget_label.value_type.type_class");
    public static final ResourceKey TYPE_FLOAT = key("slot_widget_label.value_type.type_float");
    public static final ResourceKey TYPE_BOOLEAN = key("slot_widget_label.value_type.type_boolean");
    public static final ResourceKey TYPE_INTEGER = key("slot_widget_label.value_type.type_integer");
    public static final ResourceKey TYPE_SYMBOL = key("slot_widget_label.value_type.type_symbol");
    public static final ResourceKey TYPE_STRING = key("slot_widget_label.value_type.type_string");
    public static final ResourceKey TYPE_ANY = key("slot_widget_label.value_type.type_any");
}
