package org.firstinspires.ftc.teamcode.sbfActions;


import org.firstinspires.ftc.teamcode.sbfHardware.Lift;
import org.firstinspires.ftc.teamcode.sbfHardware.Robot;
import org.firstinspires.ftc.robotcore.external.Telemetry;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * ActionMaster is responsible for managing the run list of actions and moving each action to the
 * run map and then removing it in sequence.
 *
 * @author Andrew, 8668 Should Be Fine!
 * */
public class ActionMaster
{
    /** The dictionary of all possible actions loaded from the CSV file. */
    Map<String, RobotAction> actionMap = new Hashtable<String, RobotAction>();
    /** The list of actions to be executed for a given step. */
    Map<String, RobotAction> runMap = new Hashtable<String, RobotAction>();
    /** The list of actions to be executed next. */
    List<String>  nextList = new Vector<String>();
    /** A telemetry object that is used to display information. */
    Telemetry telemetry;
    /** A truth value is is whether or not a method has run before or not. */
    boolean firstRun = true;
    String firstAction = "One";

    /** Initializes the telemetry, csv file, and robot.
     * **/
    public void init(Telemetry telem, File autoFile, Robot robot)
    {
        telemetry = telem;
        BufferedReader br = null;
        String line = "null";
        try
        {
            RobotAction myAction;

            FileReader inputStreamReader = new FileReader(autoFile);

            br = new BufferedReader(inputStreamReader);

            br.readLine();  // throw away first line by reading it and not looking at it

            while((line = br.readLine()) != null)
            {
                // Get a line from the CSV file and split it on the commas
                String[] theItems = line.split(",");

                // Grab the first one... this will be the type of RobotAction to create
                if(theItems.length != 0)
                {
                    String type = theItems[0].trim().toUpperCase();

                    if (!type.isEmpty()) {


                        // Grab the reset of the items after the first one... these are the parameters to use
                        // with the constructors for each of the RobotAction subclasses.
                        String[] params = (Arrays.copyOfRange(theItems, 1, theItems.length));

                        // Based on the type, make the specific kind of RobotAction
                        if (type.equalsIgnoreCase("WAITACTION")) {
                            myAction = new WaitAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("DRIVEACTION")) {
                            myAction = new DriveAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("CAMERAACTION")) {
                            myAction = new CameraAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("TURNACTION")) {
                            myAction = new TurnAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("GYROACTION")) {
                            myAction = new GyroAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("FOUNDATIONACTION")) {
                            myAction = new FoundationAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("CHANGEOUTACTION")) {
                            myAction = new ChangeOutAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("INTAKEACTION")) {
                            myAction = new IntakeAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("LIFTACTION")) {
                            myAction = new LiftAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("LIFTHOMEACTION")) {
                            myAction = new LiftHomeAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("DEPLOYSTONEACTION")) {
                            myAction = new DeployStoneAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("INTAKESTONEACTION")) {
                            myAction = new IntakeStoneAction(params);
                            myAction.init(telemetry, robot);
                            this.addAction(myAction);
                        }
                        else if (type.equalsIgnoreCase("PURSUITACTION")) {
                            if (!actionMap.containsKey(params[0].toUpperCase())) {
                                myAction = new PursuitAction(params);
                                myAction.init(telemetry, robot);
                                this.addAction(myAction);
                            }
                            else {
                                myAction = actionMap.get(params[0].toUpperCase());
                                if (myAction != null) {
                                    ((PursuitAction) myAction).addPoint(params);
                                    telemetry.addData("Adding pursuit point to ", params[0]);

                                }
                                else {
                                    telemetry.addData("addPoint() FAILED ", "--myAction equals null--");
                                }
                            }
                        }
                        else {
                            myAction = null;
                        }

                        telemetry.addData("TheAction: ", myAction);
                    }
                }

            }
//            this.addRunAction("One");
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            telemetry.addData("file not found e: ", e);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            telemetry.addData("io exception: ", e);
        }
        finally
        {
            if(br != null)
            {
                try
                {
                    br.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setFirstAction( String id )
    {
        this.addRunAction(id);
    }

    /** The body of the code to execute: Creates a run list of actions to do, executes the current
     * action, and then deletes each action from the run list as it is completed. */
    public void execute()
    {
        if(firstRun)
        {
            for(RobotAction action : runMap.values())
            {
                action.entry();
            }
            firstRun = false;
        }

        telemetry.addData("RunMap: ", keyList());
        Iterator<Map.Entry<String, RobotAction>> itr = runMap.entrySet().iterator();

        while(itr.hasNext())
        {

            Map.Entry<String, RobotAction> entry = itr.next();
            Boolean actionDone = entry.getValue().execute();
            if(!(entry.getValue().getAuxAction().toUpperCase().equals("NULL")))
            {
                nextList.add(entry.getValue().getAuxAction().toUpperCase());
//                telemetry.addData("aux action: ", action.getAuxAction());
            }
            if(actionDone)
            {
                if(entry.getValue().theNextAction != null)
                {
                    nextList.add(entry.getValue().theNextAction.toUpperCase());
                }
                entry.getValue().exit();
                itr.remove();
            }
        }
//        for(RobotAction action : runMap.values())
//        {
//            Boolean actionDone = action.execute();
//            if(!(action.getAuxAction().toUpperCase().equals("NULL")))
//            {
//                nextList.add(action.getAuxAction().toUpperCase());
////                telemetry.addData("aux action: ", action.getAuxAction());
//            }
//            if(actionDone)
//            {
//                if(action.theNextAction != null)
//                {
//                    nextList.add(action.theNextAction.toUpperCase());
//                }
//                action.exit();
//                runMap.remove(action.theId.toUpperCase());
//            }
//            telemetry.addData("action done: ", actionDone);
//        }

        telemetry.addData("next list: ", nextList.toString());
        for(String next : nextList)
        {
//            telemetry.addData("Size of dictionary: ", actionMap.size());
//            telemetry.addData("Keys:", actionMap.keySet());
//            telemetry.addData("Contains Camera Key?:", actionMap.containsKey(next));
//            telemetry.addData("string id to get from Map:", next );
            RobotAction nextAction = actionMap.get(next);
            telemetry.addData("Name of action got: ", nextAction.theId);
//            telemetry.addData("next action: ", nextAction);

            nextAction.entry();
            addRunAction(nextAction.theId);
        }
        nextList.clear();
    }

    /** Adds an action to the run map. */
    public void addRunAction(String action)
    {
        runMap.put(action.toUpperCase(), actionMap.get(action.toUpperCase()));
    }

    /** Clears the action map, run map, and next list of actions.*/
    public void buildActionMap()
    {
        actionMap.clear();
        runMap.clear();
        nextList.clear();
    }

    /** Adds the action listed in the parameter to the action map.*/
    public void addAction(RobotAction action)
    {
        actionMap.put(action.theId.toUpperCase(), action);
        telemetry.addData("Adding Action Named: ", action.theId);
    }

    /** Lists which actions currently running.  */
    public Set<String> keyList()
    {
        return runMap.keySet();
    }

    /**
     * Returns the number of objects in the run map.
     * @return  the number of objects in the run map.
     * */
    public int getRunListSize()
    {
        return runMap.size();
    }

}
