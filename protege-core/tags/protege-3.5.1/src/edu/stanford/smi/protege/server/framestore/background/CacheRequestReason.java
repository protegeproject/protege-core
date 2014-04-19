package edu.stanford.smi.protege.server.framestore.background;

import java.util.EnumSet;

public enum CacheRequestReason {
  USER_REQUESTED_FRAME_VALUES,  USER_NAME_REQUEST, USER_CLOSURE_REQUEST, USER_SPECIFIC_FRAMES,
  NEW_FRAME, IMMEDIATE_PRELOAD, PRELOAD, SUBCLASS, DIRECT_INSTANCES, STATE_MACHINE;

  private static int MIN_PRIORITY;
  static {
    int min = STATE_MACHINE.priority();
    for (CacheRequestReason reason : CacheRequestReason.values()) {
      if (reason.priority() < min) {
        min = reason.priority();
      }
      MIN_PRIORITY = min;
    }
  }

  public int priority() {
    switch (this) {
    case USER_REQUESTED_FRAME_VALUES:
      return 8;
    case USER_CLOSURE_REQUEST:
      return 7;
    case NEW_FRAME:
      return 6;
    case STATE_MACHINE:
      return 3;
    case IMMEDIATE_PRELOAD:
      return 3;
    case SUBCLASS:
      return -1;
    case PRELOAD:
      return -1;
    default:
      return 0;
    }
  }

  public static int priority(EnumSet<CacheRequestReason> reasons) {
    int priority = MIN_PRIORITY;
    for (CacheRequestReason reason : reasons) {
      if (reason.priority() > priority) {
        priority = reason.priority();
      }
    }
    return priority;
  }

}
