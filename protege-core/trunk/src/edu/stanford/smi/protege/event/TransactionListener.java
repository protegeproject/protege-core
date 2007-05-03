package edu.stanford.smi.protege.event;

import java.util.*;

public interface TransactionListener extends EventListener{
    void transactionBegin(TransactionEvent event);
    void transactionEnded(TransactionEvent event);

}
