/* Generated By:JavaCC: Do not edit this line. ParserConstants.java */
package edu.stanford.smi.protege.storage.clips;


/** 
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface ParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int COMMENT_LINE = 5;
  /** RegularExpression Id. */
  int EOL = 6;
  /** RegularExpression Id. */
  int BLANK_COMMENT = 7;
  /** RegularExpression Id. */
  int TEXT_COMMENT = 8;
  /** RegularExpression Id. */
  int QUESTION_NONE = 9;
  /** RegularExpression Id. */
  int QUESTION_DEFAULT = 10;
  /** RegularExpression Id. */
  int LPAREN = 11;
  /** RegularExpression Id. */
  int RPAREN = 12;
  /** RegularExpression Id. */
  int BRA = 13;
  /** RegularExpression Id. */
  int KET = 14;
  /** RegularExpression Id. */
  int EXTENSION = 15;
  /** RegularExpression Id. */
  int ABSTRACT = 16;
  /** RegularExpression Id. */
  int ACCESS = 17;
  /** RegularExpression Id. */
  int ALLOWED_CLASSES = 18;
  /** RegularExpression Id. */
  int ALLOWED_FLOATS = 19;
  /** RegularExpression Id. */
  int ALLOWED_GRAMMAR = 20;
  /** RegularExpression Id. */
  int ALLOWED_INTEGERS = 21;
  /** RegularExpression Id. */
  int ALLOWED_PARENTS = 22;
  /** RegularExpression Id. */
  int ALLOWED_STRINGS = 23;
  /** RegularExpression Id. */
  int ALLOWED_SYMBOLS = 24;
  /** RegularExpression Id. */
  int ALLOWED_VALUES = 25;
  /** RegularExpression Id. */
  int ANY = 26;
  /** RegularExpression Id. */
  int ASSOCIATED_FACET = 27;
  /** RegularExpression Id. */
  int BUILD = 28;
  /** RegularExpression Id. */
  int CARDINALITY = 29;
  /** RegularExpression Id. */
  int COMMENT = 30;
  /** RegularExpression Id. */
  int COMPOSITE = 31;
  /** RegularExpression Id. */
  int CONCRETE = 32;
  /** RegularExpression Id. */
  int CREATE_ACCESSOR = 33;
  /** RegularExpression Id. */
  int DEFAULT_ = 34;
  /** RegularExpression Id. */
  int DEFCLASS = 35;
  /** RegularExpression Id. */
  int EXCLUSIVE = 36;
  /** RegularExpression Id. */
  int FLOAT = 37;
  /** RegularExpression Id. */
  int INCLUDE = 38;
  /** RegularExpression Id. */
  int INHERIT = 39;
  /** RegularExpression Id. */
  int INITIALIZER_ONLY = 40;
  /** RegularExpression Id. */
  int INSTANCE = 41;
  /** RegularExpression Id. */
  int INTEGER = 42;
  /** RegularExpression Id. */
  int INVERSE_SLOT = 43;
  /** RegularExpression Id. */
  int IS_A = 44;
  /** RegularExpression Id. */
  int LOCAL = 45;
  /** RegularExpression Id. */
  int MULTISLOT = 46;
  /** RegularExpression Id. */
  int NO_INHERIT = 47;
  /** RegularExpression Id. */
  int NON_REACTIVE = 48;
  /** RegularExpression Id. */
  int NOTE = 49;
  /** RegularExpression Id. */
  int OF = 50;
  /** RegularExpression Id. */
  int OVERRIDE_MESSAGE = 51;
  /** RegularExpression Id. */
  int PATTERN_MATCH = 52;
  /** RegularExpression Id. */
  int PRIVATE = 53;
  /** RegularExpression Id. */
  int PROPAGATION = 54;
  /** RegularExpression Id. */
  int PUBLIC = 55;
  /** RegularExpression Id. */
  int RANGE = 56;
  /** RegularExpression Id. */
  int REACTIVE = 57;
  /** RegularExpression Id. */
  int READ = 58;
  /** RegularExpression Id. */
  int READ_ONLY = 59;
  /** RegularExpression Id. */
  int READ_WRITE = 60;
  /** RegularExpression Id. */
  int ROLE = 61;
  /** RegularExpression Id. */
  int SINGLE_SLOT = 62;
  /** RegularExpression Id. */
  int SHARED = 63;
  /** RegularExpression Id. */
  int SLOT = 64;
  /** RegularExpression Id. */
  int SOURCE = 65;
  /** RegularExpression Id. */
  int STORAGE = 66;
  /** RegularExpression Id. */
  int STRING = 67;
  /** RegularExpression Id. */
  int SUBSLOT_OF = 68;
  /** RegularExpression Id. */
  int SYMBOL = 69;
  /** RegularExpression Id. */
  int TYPE = 70;
  /** RegularExpression Id. */
  int USER_FACET = 71;
  /** RegularExpression Id. */
  int VALUE = 72;
  /** RegularExpression Id. */
  int VISIBILITY = 73;
  /** RegularExpression Id. */
  int VERSION = 74;
  /** RegularExpression Id. */
  int WRITE = 75;
  /** RegularExpression Id. */
  int INSTANCE_NAME_LITERAL = 76;
  /** RegularExpression Id. */
  int STRING_LITERAL = 77;
  /** RegularExpression Id. */
  int SYMBOL_LITERAL = 78;
  /** RegularExpression Id. */
  int SYMBOL_CHAR_START = 79;
  /** RegularExpression Id. */
  int SYMBOL_CHAR_MIDDLE = 80;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "<COMMENT_LINE>",
    "<EOL>",
    "<BLANK_COMMENT>",
    "<TEXT_COMMENT>",
    "\"?NONE\"",
    "\"?DEFAULT\"",
    "\"(\"",
    "\")\"",
    "\"[\"",
    "\"]\"",
    "\";+\"",
    "\"abstract\"",
    "\"access\"",
    "\"allowed-classes\"",
    "\"allowed-floats\"",
    "\"allowed-grammar\"",
    "\"allowed-integers\"",
    "\"allowed-parents\"",
    "\"allowed-strings\"",
    "\"allowed-symbols\"",
    "\"allowed-values\"",
    "\"ANY\"",
    "\"associated-facet\"",
    "\"build\"",
    "\"cardinality\"",
    "\"comment\"",
    "\"composite\"",
    "\"concrete\"",
    "\"create-accessor\"",
    "\"default\"",
    "\"defclass\"",
    "\"exclusive\"",
    "\"FLOAT\"",
    "\"include\"",
    "\"inherit\"",
    "\"initializer-only\"",
    "\"INSTANCE\"",
    "\"INTEGER\"",
    "\"inverse-slot\"",
    "\"is-a\"",
    "\"local\"",
    "\"multislot\"",
    "\"no-inherit\"",
    "\"non-reactive\"",
    "\"note\"",
    "\"of\"",
    "\"override-message\"",
    "\"pattern-match\"",
    "\"PRIVATE\"",
    "\"propagation\"",
    "\"PUBLIC\"",
    "\"range\"",
    "\"reactive\"",
    "\"read\"",
    "\"read-only\"",
    "\"read-write\"",
    "\"role\"",
    "\"single-slot\"",
    "\"shared\"",
    "\"slot\"",
    "\"source\"",
    "\"storage\"",
    "\"STRING\"",
    "\"subslot-of\"",
    "\"SYMBOL\"",
    "\"type\"",
    "\"user-facet\"",
    "\"value\"",
    "\"VISIBILITY\"",
    "\"version\"",
    "\"write\"",
    "<INSTANCE_NAME_LITERAL>",
    "<STRING_LITERAL>",
    "<SYMBOL_LITERAL>",
    "<SYMBOL_CHAR_START>",
    "<SYMBOL_CHAR_MIDDLE>",
  };

}
