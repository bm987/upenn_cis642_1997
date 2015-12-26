// $Id: simApplet.java,v 1.5 1997/05/01 05:40:55 berrym Exp $
//
// simApplet.java
//
//
// A real-time, priority-driven, preempting graphical scheduling simulator
//
//    This sourcecode drives the applet.
//
// Mike Berry
// CIS 642
// 24 April 1997


import java.applet.*;
import java.awt.*;
import java.util.Vector;

// ---------------------------------------------------------------------- //
// simHelp - 
//   Pops up a window with a message; goes away when the
//   user clicks OK
// ---------------------------------------------------------------------- //

class simHelp extends Frame {

    Button buttonOK;
    Label labelMsg;

    public simHelp(String msg) {

        super("simHelp window");       // add a title
        GridBagLayout subLayout = new GridBagLayout();
        GridBagConstraints subc = new GridBagConstraints();
        setLayout(subLayout);

        // the panel we're making is going to have two
        // widgets, one on each line.  thus, set gridwidth to
        // REMAINDER so that we jump to the next line after
        // each widget.

        subc.gridwidth = GridBagConstraints.REMAINDER;

        addNotify();
        resize(insets().left + insets().right + 400, insets().top + insets().bottom + 123);

        labelMsg = new Label (msg, Label.CENTER);
        buttonOK = new Button("OK");

        subLayout.setConstraints(labelMsg, subc);
        add(labelMsg);

        subLayout.setConstraints(buttonOK, subc);
        add(buttonOK);
    }


    public boolean handleEvent(Event event) {
    	if (event.id == Event.ACTION_EVENT && event.target == buttonOK) {
    	    	ok();
    	    	return true;
    	}
    	else if (event.id == Event.WINDOW_DESTROY) {
    	    hide();
    	    return true;
    	}
    	return super.handleEvent(event);
    }

    public void ok() {
        // the user has clicked ok -- we can leave now
        this.dispose();
    }
}

// ---------------------------------------------------------------------- //
// simWindow -
//   The window for each simulation.  This class holds all the
//   grphical objects for the simulation and handles all user
//   interaction with the simulation.
//   
// ---------------------------------------------------------------------- //

class simWindow extends Frame {
  
  Menu menu1;
  Button sButton, rButton;
  List lstQueue, lstMissed;
  simSet mySet;
  scheduler mySched;
  Vector tasks;		// we need to remember this in case of reset

  private Panel labelledComponent (String str, Component c0) {
    
    //
    // returns a new Panel with a label (str) and a pulldown
    // menu (or any other component) directly below the label
    //
    
    Panel p = new Panel();
    GridBagLayout subLayout = new GridBagLayout();
    GridBagConstraints subc = new GridBagConstraints();
    p.setLayout(subLayout);
    
    // the panel we are making is going to have two
    // widgets, one on each line.  thus, set gridwidth to
    // REMAINDER so that we jump to the next line after
    // each widget.
    
    subc.gridwidth = GridBagConstraints.REMAINDER;
    
    // left justify
    
    subc.anchor = GridBagConstraints.WEST;
    
    // now add the widgets
    
    Label tmpLabel = new Label(str);
    subLayout.setConstraints(tmpLabel, subc);
    p.add(tmpLabel);
    subLayout.setConstraints(c0, subc);
    
    p.add(c0);
    return p;
    
  }

  public void populateJobList (List gui, Vector job) {
    
    Job jobPtr;
    gui.clear();

    for (int x = 0; x < job.size(); x++) {
      jobPtr = (Job) job.elementAt(x);
      gui.addItem("Task " + jobPtr.myTask.id + ", Job " + jobPtr.id + ", D " + jobPtr.deadline);
    }

  }

  public void populateTaskList (List gui, Vector tsk) {
    
    Task tskPtr;
    gui.clear();

    for (int x = 0; x < tsk.size(); x++) {
      tskPtr = (Task) tsk.elementAt(x);
      gui.addItem(" " + tskPtr.id + ": " + tskPtr.phase + ", " + tskPtr.period + ", " + tskPtr.execute + ", " + tskPtr.deadline);
    }
    
  }

  public simWindow (String Title, Vector in_tasks, scheduler in_mySched, int width) {
    
    super(Title);       // add a title

    tasks = in_tasks;
    mySched = in_mySched;

    GridBagLayout subLayout = new GridBagLayout();
    GridBagConstraints subc = new GridBagConstraints();
    setLayout(subLayout);
    
    // the panel we arere making is going to have two
    // widgets, one on each line.  thus, set gridwidth to
    // REMAINDER so that we jump to the next line after
    // each widget.
    
    // subc.gridwidth = GridBagConstraints.REMAINDER;
    
    
    addNotify();
    resize(insets().left + insets().right + width, insets().top + insets().bottom + 300);
    
    
    //INIT_MENUS
    MenuBar mb = new MenuBar();
    menu1 = new Menu("File");
    menu1.add(new MenuItem("Close"));
    mb.add(menu1);
    setMenuBar(mb);
    //


    mySet = new simSet(this, tasks, mySched);
    new simThread("test_thread1",mySet).start();

    rButton = new Button ("Reset");
    subLayout.setConstraints(rButton, subc);
    add(rButton);

    List lstTask = new List(5,false);
    populateTaskList (lstTask, tasks);
    Panel p0 = labelledComponent("Tasks", lstTask);
    subLayout.setConstraints(p0, subc);
    add(p0);

    lstQueue = new List(5,false);
    Panel p1 = labelledComponent("Run Queue", lstQueue);
    subLayout.setConstraints(p1, subc);
    add(p1);

    lstMissed = new List(5,false);
    Panel p2 = labelledComponent("Missed Deadlines", lstMissed);
    subLayout.setConstraints(p2, subc);
    add(p2);

    sButton = new Button ("Start/Stop");
    subLayout.setConstraints(sButton, subc);
    add(sButton);

  }

  public boolean handleEvent(Event e) {


    if ((e.target == sButton) && (e.id == e.ACTION_EVENT)) {
      if (mySet.run)
	mySet.run = false;
      else {
	mySet.run = true;
	new simThread("test_thread1",mySet).start();
      }
    } 
    else if ((e.target == rButton) && (e.id == e.ACTION_EVENT)) {
      mySet.run = false;
      Task tskPtr;
      for (int x = 0; x < tasks.size(); x++) {
	tskPtr = (Task) tasks.elementAt(x);
	tskPtr.reset();
      }
      mySet = new simSet(this, tasks, mySched);
      repaint();
      populateJobList(lstQueue,mySet.sys1.runQueue.myList); 
      populateJobList(lstMissed,mySet.sys1.runQueue.dlist); 
    }
    return super.handleEvent(e);
  }

  public void paint(Graphics g) {

    g.drawRect(0, 0, size().width - 1, size().height - 1);
    
    Color bg = getBackground();
    
    //Draw a fancy frame around the applet.
    g.setColor(bg);
    
    int top = 50;
    int maxTime = 30;
    int widthTime = 20;
    int height = 30;
    int lineHeight = 10;

    g.draw3DRect(0, top, maxTime * widthTime, height+top, true);

    g.setColor(java.awt.Color.black);
    for (int x = 0; x < maxTime; x++)
      g.drawLine(x*widthTime, top + height, x*widthTime, top + height-lineHeight);

    mySet.redraw(g, 0, top + height, widthTime, maxTime);
  }
  
  public boolean action(Event event, Object arg) {
    if (event.target instanceof MenuItem) {
      String label = (String) arg;
      if (label.equalsIgnoreCase("Close")) {
	this.dispose();
	return true;
      }
    }
    return super.action(event, arg);
  }
}

// ---------------------------------------------------------------------- //
// simThread -
//   Thread of control for a simulation.  Held by simSet.
//  
//   
// ---------------------------------------------------------------------- //


class simThread extends Thread {

  simSet mySet;

  public simThread(String str, simSet in_simSet) {
    super(str);
    mySet = in_simSet;
  }


  private void sleep (int sec)
    {
      
      try {
	java.lang.Thread.currentThread().sleep(sec * 100); // sleep one second
      } catch (Exception sleepexc) {}
    }

  public void run() {

    Job currentJob;
    
    for ( ;  ; mySet.x = mySet.x + 1) {
      if (mySet.run) {
	currentJob = mySet.sys1.run();
	mySet.completedJob(currentJob);
	sleep(1);
      }
      else
	return;
    }
  }
}


// ---------------------------------------------------------------------- //
// simSet -
//   Each simulation window creates and holds a single simSet.  simSet
//   holds the simulation data structures (see sim.java) and accesses
//   both the simulation classes and the window (simWindow class)
//   as the simulation thread runs.
//   
// ---------------------------------------------------------------------- //

class simSet {

    // each simulation is represented by an instance of
    // the simSet

  boolean run;			// flag to indicate if we should be running or not
  Simulation sys1;		// the simulation object
  int x;			// simulation time
  Job[] recentJobs;		// hold a circular bounded buffer of recently completed jobs
  int recentNdx;		// ""
  int jobsCompleted;		// ""
  simWindow myWindow;
  scheduler mySched;

  public simSet (simWindow in_myWindow, Vector tasks, scheduler in_Sched) {
    
    myWindow = in_myWindow;
    mySched = in_Sched;
    sys1 = new Simulation(tasks, new Queue(mySched));
    x = 0;
    run = false;
    recentNdx = 0;
    jobsCompleted = 0;
    recentJobs = new Job[30];	// this is WRONG!!!
  }

  public void completedJob (Job j1) {
      // record the last 30 completed jobs in a circular buffer
    jobsCompleted++;

    recentJobs[recentNdx] = j1;
    if (recentNdx < 30 - 1)
      recentNdx++;
    else
      recentNdx = 0;
    myWindow.repaint();
    myWindow.populateJobList(myWindow.lstQueue,sys1.runQueue.myList); 
    myWindow.populateJobList(myWindow.lstMissed,sys1.runQueue.dlist); 
  }

  public void redraw (Graphics g, int x, int y, int width, int max) {

    int start, pos;

    // System.out.println ("Sim: redraw, completed "+ jobsCompleted + " ndx " + recentNdx);
    if (jobsCompleted < 31)
      start = 0;
    else if (recentNdx < 30 -1)
      start = recentNdx + 1;
    else
      start = 0;
    pos = 0;
    while ((pos < max) && (pos < jobsCompleted)) {
	// fill the rectangle for recentJobs[start]
      Job thisJob = recentJobs[start];
	if (thisJob != null) {
	  int taskNumber = thisJob.myTask.id;
	  int jobNumber = thisJob.id;



	  if (taskNumber - ((taskNumber / 4) * 4) == 0)
	    g.setColor(java.awt.Color.green);
	  else if (taskNumber - ((taskNumber / 4) * 4) == 1)
	    g.setColor(java.awt.Color.yellow);
	  else if (taskNumber - ((taskNumber / 4) * 4) == 2)
	    g.setColor(java.awt.Color.blue);
	  else if (taskNumber - ((taskNumber / 4) * 4) == 3)
	    g.setColor(java.awt.Color.red);
	  g.fillRect(x + (width * pos), y, width,  20);

	  g.setColor(java.awt.Color.black);
	  g.drawString("" + recentJobs[start].id, x + (width*pos) + 2, y + 10);

	}
	else {
	  g.setColor(myWindow.getBackground());
      	  g.fillRect(x + (width * pos), y, width,  20);
	}
      
      if (start < 30 - 1)
	start++;
      else
	start = 0;
      pos++;
    }
    g.setColor(java.awt.Color.black);
    g.drawString("t" + sys1.time, (width * max) - 35, y + 45);
    g.drawString("Sched: " + mySched.id(), 10, y + 45);
  }
  
}


// ---------------------------------------------------------------------- //
// simApplet -
//   The main applet window which appears in the browser.  This class
//   manages the main GUI and creates simulation windows as the
//   user asks for them.
//   
// ---------------------------------------------------------------------- //


public class simApplet extends Applet {
  
  StringBuffer buffer;
  Vector tasks;

  TextField tfAddPhase, tfAddPeriod, tfAddExec, tfAddDeadline;
  Button btAddTask, btForkSim;
  Choice chSched;
  List chTasks;
  Label lbWidth;
  Button btWidthLess, btWidthMore;
  Checkbox cbStrict;
  scheduler curSched;
  int taskNum;


  public void populateTaskList (List gui, Vector tsk) {
    
    Task tskPtr;
    gui.clear();

    for (int x = 0; x < tsk.size(); x++) {
      tskPtr = (Task) tsk.elementAt(x);
      gui.addItem(" " + tskPtr.phase + ", " + tskPtr.period + ", " + tskPtr.execute + ", " + tskPtr.deadline);
    }
    
  }

  private Panel labelledComponent (String str, Component c0) {
    
    //
    // returns a new Panel with a label (str) and a pulldown
    // menu (or any other component) directly below the label
    //
    
    Panel p = new Panel();
    GridBagLayout subLayout = new GridBagLayout();
    GridBagConstraints subc = new GridBagConstraints();
    p.setLayout(subLayout);
    
    // the panel we are making is going to have two
    // widgets, one on each line.  thus, set gridwidth to
    // REMAINDER so that we jump to the next line after
    // each widget.
    
    subc.gridwidth = GridBagConstraints.REMAINDER;
    
    // left justify
    
    subc.anchor = GridBagConstraints.WEST;
    
    // now add the widgets
    
    Label tmpLabel = new Label(str);
    subLayout.setConstraints(tmpLabel, subc);
    p.add(tmpLabel);
    subLayout.setConstraints(c0, subc);
    
    p.add(c0);
    return p;
    
  }
  
  private void myAdd (Component gui, GridBagLayout l, GridBagConstraints c) {
    l.setConstraints(gui, c);
    add(gui);
  }

  public void init() {
    buffer = new StringBuffer();
    addItem("initializing... ");
    tasks = new Vector();

    taskNum = 1;

    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(lay);

    // setLayout(new FlowLayout());

    Label l1=new Label("Priority-driven RT Scheduler", Label.CENTER);
    l1.setFont(new Font("Helvetica",Font.BOLD,18));
    c.gridwidth = GridBagConstraints.REMAINDER;
    myAdd(l1, lay, c);

    Label l2=new Label("CIS 642: Spring 1997", Label.CENTER);
    l2.setFont(new Font("Helvetica",Font.ITALIC,12));
    c.gridwidth = GridBagConstraints.REMAINDER;
    myAdd(l2, lay, c);

    tfAddPhase = new TextField(3); 
    Panel p1 = labelledComponent("Phase", tfAddPhase);
    c.gridwidth = 1;
    myAdd(p1, lay, c);

    tfAddPeriod = new TextField(3); 
    Panel p2 = labelledComponent("Period", tfAddPeriod);
    myAdd(p2, lay, c);

    tfAddExec = new TextField(3); 
    Panel p3 = labelledComponent("Exec", tfAddExec);
    myAdd(p3, lay, c);

    tfAddDeadline = new TextField(3); 
    Panel p4 = labelledComponent("Deadline", tfAddDeadline);
    c.gridwidth = GridBagConstraints.REMAINDER;
    myAdd(p4, lay, c);

    btAddTask = new Button ("Add Task"); 

    myAdd (btAddTask, lay, c);

    cbStrict = new Checkbox("Strict");
    myAdd(cbStrict, lay, c);

    chSched = new Choice();
    chSched.addItem ("EDF");
    chSched.addItem ("LST");
    chSched.addItem ("RM");
    chSched.addItem ("DM");
    myAdd(chSched, lay, c);

    chTasks = new List();
    chTasks.addItem (" ");
    Panel p5 = labelledComponent("Tasks", chTasks);
    myAdd(p5, lay, c);


    btForkSim = new Button ("Launch new simulation");
    myAdd(btForkSim, lay, c);

  }
  
  public void start() {
    //    simHelp tmpsimHelp = new simHelp("Welcome to the simulator");
    //    tmpsimHelp.show();

    addItem("Starting...");
    

 
  }

  public void stop() {
    addItem("stopping... ");
  }
  
  public void destroy() {
    addItem("preparing for unloading...");
  }
  
  void addItem(String newWord) {
    // System.out.println(newWord);
    buffer.append(newWord);
    repaint();
  }


  public boolean handleEvent(Event e) {
    if ((e.target == btForkSim) && (e.id == e.ACTION_EVENT)) {

      boolean strict = cbStrict.getState();

      if (tasks.size() > 0) {
	if (chSched.getSelectedItem() == "EDF")
	  curSched = new schedEDF(strict);
	else if (chSched.getSelectedItem() == "LST")
	  curSched = new schedLST(strict);
	else if (chSched.getSelectedItem() == "DM")
	  curSched = new schedDM(strict);
	else if (chSched.getSelectedItem() == "RM")
	  curSched = new schedRM(strict);

	simWindow simWin1;
	simWin1 = new simWindow ("Simulation Window", tasks, curSched, 600);
	simWin1.show();
	tasks = new Vector();	// i love garbage collection 
	populateTaskList (chTasks, tasks);
	taskNum = 1;		// re-initialize taskNum
      }
      else {
	simHelp tmpsimHelp = new simHelp("You must add a task, first.");
	tmpsimHelp.show();
      }

      


    }
    else if ((e.target == btAddTask) && (e.id == e.ACTION_EVENT))
      {
	int userDeadLine = 0;
	int userExec = 0;
	int userPeriod = 0;
	int userPhase = 0;
	boolean ok = true;
	try {
	  userDeadLine = java.lang.Integer.parseInt(tfAddDeadline.getText());
	  userExec = java.lang.Integer.parseInt(tfAddExec.getText());
	  userPeriod = java.lang.Integer.parseInt(tfAddPeriod.getText());
	  userPhase = java.lang.Integer.parseInt(tfAddPhase.getText());
	} catch (NumberFormatException exc) {
	  simHelp tmpsimHelp = new simHelp("You must fill in all the fields.");
	  tmpsimHelp.show();
	  ok = false;
	}
	if (ok) {
	  tasks.addElement(new Task(taskNum++, userPhase, userPeriod, userExec, userDeadLine));
	  populateTaskList (chTasks, tasks);
	}
      }

    return super.handleEvent(e);
  }
  
  public void paint(Graphics g) {

    Color bg = getBackground();

    //Draw a fancy frame around the applet.
    // g.setColor(bg);

    g.drawRect(0, 0, size().width - 1, size().height - 1);
    // g.drawString(buffer.toString(), 60, 25);

  }

  
}


