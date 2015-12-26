//
// $Id: sim.java,v 1.5 1997/05/01 05:40:55 berrym Exp $
//
// sim.java
//
// A real-time, priority-driven, preempting graphical scheduling simulator
//
//      This code drives the simulator; with a "sim" class and a main function,
//      it can run as a standalone text-based application.
//
// Mike Berry
// CIS 642
// 24 April 1997


import java.lang.Object;
import java.util.Date;
import java.util.Vector;
import java.net.*;
import java.io.*;

// ---------------------------------------------------------------------- //
// scheduler -
//    The scheduler interface and its associated classes hold various
//    RT scheduling algorithms which are used to sort a run queue
// 
// 
// ---------------------------------------------------------------------- //

interface scheduler {

  boolean sorted(Job j1, Job j2, int time, boolean always);
  String id ();

}

class schedEDF implements scheduler {

  boolean strict;

  public schedEDF (boolean in_strict) {
    strict = in_strict;
  }

  public boolean sorted (Job j1, Job j2, int time, boolean always)
    {
	if ((always) || (strict)) {
	  if (j1.deadline <= j2.deadline)
	    return true;
	  else
	    return false;
	}
	else 
	  return true;
    }

  public String id () {
    if (strict)
      return ("strict EDF");
    else
      return ("non-strict EDF");
  }
}


class schedLST implements scheduler {

  boolean strict;

  public schedLST (boolean in_strict) {
    strict = in_strict;
  }

  public boolean sorted (Job j1, Job j2, int time, boolean always)
    {
	if ((always) || (strict)) {
	  if ((j1.deadline - time - j1.left) <= (j2.deadline - time - j2.left))
	    return true;
	  else
	    return false;
	}
	else 
	  return true;
    }

  public String id () {
    if (strict)
      return ("strict LST");
    else
      return ("non-strict LST");

  }
}

class schedRM implements scheduler {

  boolean strict;

  public schedRM (boolean in_strict) {
    strict = in_strict;
  }

  public boolean sorted (Job j1, Job j2, int time, boolean always)
    {
	if ((always) || (strict)) {
	  if (j1.myTask.period <= j2.myTask.period)
	    return true;
	  else
	    return false;
	}
	else 
	  return true;
    }

  public String id () {
    if (strict)
      return ("strict RM");
    else
      return ("non-strict RM");
  }
}

class schedDM implements scheduler {

  boolean strict;

  public schedDM (boolean in_strict) {
    strict = in_strict;
  }

  public boolean sorted (Job j1, Job j2, int time, boolean always)
    {
	if ((always) || (strict)) {
	  if (j1.myTask.deadline <= j2.myTask.deadline)
	    return true;
	  else
	    return false;
	}
	else 
	  return true;
    }

  public String id () {
    if (strict)
      return ("strict DM");
    else
      return ("non-strict DM");

  }
}

// ---------------------------------------------------------------------- //
// Queue
//    Manages a run queue of jobs; performs deadline checks to see
//    which jobs missed their deadlines and maintains a list of thos
//    jobs.  Sort according to a given scheduling algorithm when asked to.
//    
// 
// 
// ---------------------------------------------------------------------- //



class Queue {

  //
  // this class is pretty loose; we are going
  // to allow the main components of this class be
  // public
  //

  public Vector myList;
  public scheduler mySched;
  public Vector dlist;

  public Queue (scheduler schedType) {
    myList = new Vector();
    dlist = new Vector();
    mySched = schedType;
  }

  public void deadlineCheck (int time) {
      // if any jobs missed their deadlines, 
      // add them to our list
    for (int x = 0; x < myList.size(); x++) {
      Job j1 = (Job) myList.elementAt(x);
	if ((j1.deadline < time) && (!j1.missedDeadLine)) {
	  dlist.addElement(j1);
	  j1.missedDeadLine = true;
	}
    }
  }
  
  public void prioritize (int time, boolean always) {

      // this is the worst available sorting algorithm,
      // which is o.k. because we are trying to make
      // the simulation slow, not fast, so that it
      // is easier to watch.

    boolean swapped = true;
    while (swapped) {
      swapped = false;
      for (int x = 0; x < myList.size() - 1; x++) {
	Job j1 = (Job) myList.elementAt(x);
	Job j2 = (Job) myList.elementAt(x+1);
	
	if (!mySched.sorted(j1, j2, time, always)) {
	  swapped = true;
	  myList.setElementAt(j1, x+1);
	  myList.setElementAt(j2, x);
	}
      }
    }
  }
}


// ---------------------------------------------------------------------- //
// Task -
//    Holds a definition for a task and maintains state of its
//    released jobs in a simulation
// 
// 
// ---------------------------------------------------------------------- //

class Task {

  public int phase;
  public int period;
  public int deadline;		// relative deadline
  public int execute;
  public Vector jobList;
  public int curseq;
  public int id;

  public Task (int in_id, int in_ph, int in_p, int in_e, int in_d) {
    phase = in_ph;
    period = in_p;
    deadline = in_d;
    execute = in_e;
    id = in_id;
    curseq = -1;
    jobList = new Vector();

  }

  public void reset () {
    curseq = -1;
    jobList = new Vector();
  }

  public Job release_job (int t)
    {
	// if there is a job to be released, make it so
      int seq = t / period;
      if (seq > curseq)
	return (new Job(t, ++curseq, this));
      else
	return null;
    }
}

// ---------------------------------------------------------------------- //
// Job -
//    Contains the state for a single job which was released by
//    a task.
// 
// ---------------------------------------------------------------------- //

class Job {

  public int deadline;		// absolute deadline
  public int left;		// how much runtime left 
  public int id;
  public boolean missedDeadLine;
  public Task myTask;			// what task am i a part of

  public Job (int t, int seq, Task mt) {
    missedDeadLine = false;
    id = seq;
    myTask = mt;
    deadline = t + mt.deadline;
    left = mt.execute;
  }
  
  public int run () {

      // run this job for one timeslice
    
    left--;
    if (left == 0) {
      myTask.jobList.removeElement(this); // remove me from the joblist
      return 1;
    }
    else
      return 0;
    
  }
}

// ---------------------------------------------------------------------- //
// Simulation
//    The main holding class for the data structures of a simulation.
//    
// 
// ---------------------------------------------------------------------- //

class Simulation {
  
  
  public Queue runQueue;
  private Vector tasks;
  public int time;
  
  
  public Simulation (Vector in_tasks, Queue in_runQueue)
    {
      time = 0;
      runQueue = in_runQueue;
      tasks = in_tasks;
    }
  
  public Job run () {
    // run the simulation for a single timeslice and return the
    // Job that ran.
    
    // first we release all jobs that need to be released,
    // we do this by asking each task to release a job if
    // it needs to do so
    
    Vector taskList = tasks;
    Job jobPtr;
    Task taskPtr;
    boolean released = false;
    
    for (int x = 0; x < taskList.size(); x++) {
      taskPtr = (Task) taskList.elementAt(x);
      jobPtr = taskPtr.release_job(time); // do you need to release a job?
      
      if (jobPtr != null) {		  // a job was released
	released = true;		  
	runQueue.myList.addElement(jobPtr); 
      }
    }

    // 
    // re order the queue if necessary, depending
    // on whether a job was released and if we are
    // running a strict/non-strict schedule
    //

    if (released) {
      runQueue.prioritize(time, true); 
      released = false;
    }
    else {
      // only re-prioritize if we are in a strict schedule
      runQueue.prioritize(time, false); 
    }

    //
    // now run the job that is at the top of the 
    // run queue, if any
    //

    Job currentJob;
    try {
      currentJob = (Job) runQueue.myList.firstElement();
    } catch (java.util.NoSuchElementException exp) {
      currentJob = null;
    }
    
    if (currentJob != null) {
      if (currentJob.run() == 1) {
	runQueue.myList.removeElement(currentJob);
	runQueue.prioritize(time, true);  // re order on job-finish
      }
    }

    time++;				  // increment time
    runQueue.deadlineCheck(time);	  // check deadlines
    return currentJob;
  }
}
