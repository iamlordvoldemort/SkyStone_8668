package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;

import org.firstinspires.ftc.teamcode.sbfActions.ActionMaster;

import java.io.File;

@Autonomous(name="pursuit run", group="pure")
@Config
public class AutoPursuit extends OpMode
{
    ActionMaster theMaster = new ActionMaster();

    Pursuit pursuit = new Pursuit((float)0.0, (float)0.0, telemetry);
    Robot robot = new Robot();
    Path drivePath = new Path();


    public void init()
    {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        robot.init(telemetry, hardwareMap, true);

        File autoFile = new File("/auto.csv");
//        theMaster.init(telemetry, autoFile, robot);

        robot.getEncoderTelem();

        // Set up path
        drivePath.addPoint(0,0,15,0);
        drivePath.addPoint(0,15,15,0);
        drivePath.addPoint(15,15,15,0);
        drivePath.addPoint(15,0,15,0);
        drivePath.addPoint(0,0,15,0);
//        drivePath.addPoint(0,0,30,0);
//        drivePath.addPoint(0, 48, 30, 0);
//        drivePath.addPoint(48, 48, 30, 0);
//        drivePath.addPoint(48, 96, 30, 0);
//        drivePath.addPoint(96, 96, 30, 0);

    }

    public void start()
    {
        resetStartTime();
        pursuit.elapsedTime = 0;
        robot.start();
    }

    public void loop()
    {

//        telemetry.addData("mp.heading: ", currentHeading);
        robot.update();

        pursuit.updatePosition(robot.getLocation());

//        telemetry.addData("mp.global location: ", bot.location);

        pursuit.updateVelocity(robot.getVelocity());


//        telemetry.addData("mp.global velocity: ", bot.velocity);
        pursuit.currentHeading = robot.getHeading();
        pursuit.currentAngularVelocity = robot.getAngularVelocity();

//        telemetry.addData("mp.currentAngularVelocity: ", bot.currentAngularVelocity);

        pursuit.elapsedTime = getRuntime();
        pursuit.follow(drivePath);

        robot.updateMotors(pursuit.desiredVelocity.copy(), pursuit.joystickAngularVelocity);

    }

    @Override
    public void stop()
    {
        robot.stop();
        super.stop();
    }
}
