package edu.stanford.smi.protege.server.dbsynch;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;

public class Guard {
	private static Logger logger = Log.getLogger(Guard.class);
	private KnowledgeBase kb;
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	private Set<Thread> threadsAccessingDatabase = new HashSet<Thread>();
	private boolean executionInProgress = false;
	
	public Guard(KnowledgeBase kb) {
		this.kb = kb;
	}
	
	public void waitForDatabaseFree() {
		while (!executionInProgress && !threadsAccessingDatabase.isEmpty()) {
			try {
				kb.wait();
			}
			catch (InterruptedException ie) {
				logger.log(Level.WARNING, "shouldn't", ie);		
			}
		}
	}
	
	public <X> X submitDatabaseJob(final Callable<X> callable) {
		final Thread me = Thread.currentThread();
		threadsAccessingDatabase.add(me);
		FutureTask<X> task = new FutureTask<X>(new Callable<X>() {
			@Override
			public X call() throws Exception {
				try {
					return callable.call();
				}
				finally {
					synchronized (kb) {
						threadsAccessingDatabase.remove(kb);
						if (threadsAccessingDatabase.isEmpty()) {
							kb.notifyAll();
						}
					}
				}
			}
		});
		exec.submit(task);
		while (threadsAccessingDatabase.contains(me)) {
			try {
				kb.wait();
			}
			catch (InterruptedException ie) {
				logger.log(Level.WARNING, "shouldn't", ie);
			}
		}
		executionInProgress = false;
		kb.notifyAll();
		try {
			return task.get();
		}
		catch (InterruptedException ie) {
			throw new RuntimeException(ie);
		}
		catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		}
	}

}
