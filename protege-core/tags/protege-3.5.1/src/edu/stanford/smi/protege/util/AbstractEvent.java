package edu.stanford.smi.protege.util;

import java.io.Serializable;
import java.util.Collection;

import edu.stanford.smi.protege.event.Event;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;

/**
 * Base class for all events.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractEvent extends SessionEvent implements Localizable, Event {
    private static final long serialVersionUID = -8345363395859512986L;
    private int _eventType;
    private Object _source;
    private Object _argument1;
    private Object _argument2;
    private Object _argument3;
    private long _timeStamp;
    private String _userName;
    private boolean replacementEvent = false;

    protected AbstractEvent(Object source, int type) {
        this(source, type, null, null, null);
    }

    protected AbstractEvent(Object source, int type, Object arg1) {
        this(source, type, arg1, null, null);
    }

    protected AbstractEvent(Object source, int type, Object arg1, Object arg2) {
        this(source, type, arg1, arg2, null);
    }

    protected AbstractEvent(Object source, int type, Object arg1, Object arg2, Object arg3) {
        super(source);
        if (source instanceof Serializable) {
            _source = source;
        }
        _eventType = type;
        _argument1 = arg1;
        _argument2 = arg2;
        _argument3 = arg3;
        
        _timeStamp = System.currentTimeMillis();
        _userName = getUserName();
    }

    public void localize(KnowledgeBase kb) {
        LocalizeUtils.localize(_source, kb);
        LocalizeUtils.localize(_argument1, kb);
        LocalizeUtils.localize(_argument2, kb);
        LocalizeUtils.localize(_argument3, kb);
    }

    protected void setSource(Object o) {
        _source = o;
        source = o;
    }

    public Object getArgument() {
        return getArgument1();
    }

    public Object getArgument1() {
        return _argument1;
    }

    public Object getArgument2() {
        return _argument2;
    }

    public Object getArgument3() {
        return _argument3;
    }

    public int getEventType() {
        return _eventType;
    }

    public Object getSource() {
        return (_source == null) ? super.getSource() : _source;
    }

    public String toString() {
        String text = StringUtilities.getClassName(this);
        return text + "(" + getSource() + ", " + _eventType + ", " + getArgument1() + 
        	", " + getArgument2() + ", " + getUserName() + ", " + getTimeStamp() + ")";
    }

    public int hashCode() {
        return HashUtils.getHash(source, _argument1, _argument2);
    }

    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof AbstractEvent) {
            AbstractEvent rhs = (AbstractEvent) o;
            equals = (_timeStamp == rhs._timeStamp) &&  (_userName.equals(rhs._userName)) && 
            	equals(source, rhs.source) && equals(_argument1, rhs._argument1) && equals(_argument2, rhs._argument2) && equals(_argument3, rhs._argument3);
        }
        return equals;
    }

    public static boolean equals(Object o1, Object o2) {
        boolean equals;
        if (o1 instanceof Collection && o2 instanceof Collection) {
            equals = CollectionUtilities.equalsSet((Collection) o1, (Collection) o2);
        } else {
            equals = SystemUtilities.equals(o1, o2);
        }
        return equals;
    }
    
    public void setTimeStamp(long timeStamp) {
      _timeStamp = timeStamp;
    }

    
	/**
	 * @return The current system time in milliseconds as a long. 
	 * This can be used to generate a java.util.Date object. 
	 */
	public long getTimeStamp() {
		return _timeStamp;
	}

	/**
	 * @return The user name of the Protege client that generated the event for any knowledge base event.
	 * (For example, create class, slot, instance, change values of slots etc.). 
	 * For the GUI events (e.g. SelectionEvent and WidgetEvent)
	 * it will return the local user name, corresponding to the user.name property,
	 * either from the protege.properties, or Protege.lax, or the system properties (in this order).
	 * If Protege is in standalone mode, it will return the local user name. If Protege is run as a client in
	 * multi-user mode, it will return the user name used to login to the Protege server. (The user names 
	 * are configured in the metaproject ontology).
	 * If none of these properties are found, it will return null.
	 */
	public String getUserName() {
		if (_userName != null)
			return _userName;
				
		RemoteSession session = ServerFrameStore.getCurrentSession();    	
    	_userName = (session != null ? session.getUserName() : ApplicationProperties.getUserName()); 
		
		return _userName;
	}

	/**
	 * This call tells whether this event was generated as a result of a replaceFrame operation.
	 * 
	 * The replaceFrame events are a special case.  For the smoothest user interaction
	 * widgets will want to simply replace the frame that they are referencing with the new frame.
	 * But we need to properly support legacy widgets that only know about frames changing name we need to 
	 * generate the full set of events.  Widgets that work by simply replacing the frame can then filter
	 * out these replace frame events because they  have already done the work on the first call.
	 * 
	 * @return true if and only if the event was generated during a replaceFrame operation.
	 */
	public boolean isReplacementEvent() {
		return replacementEvent;
	}

	/**
	 * This call allows the caller to indicate that this event was generated during a replaceFrame operation.
	 * This method should only be called in the EventGeneratorFrameStore.
	 * 
	 * The replaceFrame events are a special case.  For the smoothest user interaction
	 * widgets will want to simply replace the frame that they are referencing with the new frame.
	 * But we need to properly support legacy widgets that only know about frames changing name we need to 
	 * generate the full set of events.  Widgets that work by simply replacing the frame can then filter
	 * out these replace frame events because they  have already done the work on the first call.
	 * 
	 */
	public void setReplacementEvent(boolean replacementEvent) {
		this.replacementEvent = replacementEvent;
	}
	

}