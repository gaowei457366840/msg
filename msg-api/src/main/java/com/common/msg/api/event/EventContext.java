package com.common.msg.api.event;


public class EventContext {
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof EventContext)) return false;
        EventContext other = (EventContext) o;
        return !!other.canEqual(this);
    }

    protected boolean canEqual(Object other) {
        return other instanceof EventContext;
    }

    public int hashCode() {
        return 1;
    }

    public String toString() {
        return "EventContext()";
    }

}

