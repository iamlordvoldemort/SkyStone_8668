package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

public class Robot
{

    private Telemetry telemetry;
    private HardwareMap hardwareMap;

    private DcMotorEx RF = null;
    private DcMotorEx RR = null;
    private DcMotorEx LF = null;
    private DcMotorEx LR = null;

    private DcMotorEx xEncoder = null;
    private DcMotorEx yEncoder = null;

    private BNO055IMU gyro;

    private double xTicksPerRad;
    private double yTicksPerRad;
    private double xTicksPerSecond;
    private double yTicksPerSecond;
    private double xInPerSec;
    private double yInPerSec;
    private final double encoderWheelRadius = 1.5; //in inches
    private final double tickPerRotation = 2400;
    private final double distanceConstant = 195.5/192; //calibrated over 16' & 12' on foam tiles -- 9/13/19
    private final double inchesPerRotation = 3 * Math.PI * distanceConstant; // this is the encoder wheel distancd
    private final double gearRatio = 1.3;
    private final double ticksPerInch = tickPerRotation * 2.0 * Math.PI * encoderWheelRadius;
    private double xPrev = 0;
    private double yPrev = 0;

    private int currentXEncoder = 0;
    private int currentYEncoder = 0;
    private double currentHeading = 0;




    public void init(Telemetry telem, HardwareMap hwmap)
    {
        telemetry = telem;
        hardwareMap = hwmap;

        RF = hardwareMap.get(DcMotorEx.class, "rf");
        RF.setDirection(DcMotorEx.Direction.FORWARD);

        RR = hardwareMap.get(DcMotorEx.class, "rr");
        RR.setDirection(DcMotorEx.Direction.FORWARD);

        LF = hardwareMap.get(DcMotorEx.class, "lf");
        LF.setDirection(DcMotorEx.Direction.FORWARD);

        LR = hardwareMap.get(DcMotorEx.class, "lr");
        LR.setDirection(DcMotorEx.Direction.FORWARD);

        xEncoder = hardwareMap.get(DcMotorEx.class, "xEncoder");
        xEncoder.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        xEncoder.setDirection((DcMotorEx.Direction.FORWARD));

        yEncoder = hardwareMap.get(DcMotorEx.class, "yEncoder");
        yEncoder.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        yEncoder.setDirection((DcMotorEx.Direction.REVERSE));

        xTicksPerRad = xEncoder.getMotorType().getTicksPerRev() / 2.0 / Math.PI;
        yTicksPerRad = yEncoder.getMotorType().getTicksPerRev() / 2.0 / Math.PI;

        try
        {
            // Set up the parameters with which we will use our IMU. Note that integration
            // algorithm here just reports accelerations to the logcat log; it doesn't actually
            // provide positional information.
            BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
            parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
            parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
            parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
            parameters.loggingEnabled = true;
            parameters.loggingTag = "IMU";
            parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

            // Retrieve and initialize the IMU.
            gyro = hardwareMap.get(BNO055IMU.class, "imu");
            gyro.initialize(parameters);
        }
        catch (Exception p_exeception)
        {
            telemetry.addData("imu not found in config file", 0);
            gyro = null;
        }

    }

    public void update()
    {
        currentHeading = updateHeading();

        xTicksPerSecond = xEncoder.getVelocity(AngleUnit.RADIANS) * xTicksPerRad / gearRatio;
        yTicksPerSecond = yEncoder.getVelocity(AngleUnit.RADIANS) * yTicksPerRad / gearRatio;
        xInPerSec = xTicksPerSecond / ticksPerInch;
        yInPerSec = yTicksPerSecond / ticksPerInch;

        currentXEncoder = xEncoder.getCurrentPosition();
        currentYEncoder = yEncoder.getCurrentPosition();
    }

    /**
     * Used to get the robot's heading.
     *
     * @return  the robot's heading as an double
     */
    public double updateHeading()
    {
        Orientation angles = gyro.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        return -AngleUnit.DEGREES.normalize(AngleUnit.DEGREES.fromUnit(angles.angleUnit, angles.firstAngle));
    }
    public double getHeading()
    {
        return currentHeading;
    }

    public void getEncoderTelem()
    {
        getXInchesMoved();
        getYInchesMoved();
        getXLinearVelocity();
        getYLinearVelocity();
    }


    public float getXInchesMoved()
    {
        double inchesX = (((currentXEncoder - xPrev) / tickPerRotation) * inchesPerRotation) * Math.cos(Math.toRadians(currentHeading)) +
                (((currentYEncoder - yPrev) / tickPerRotation) * inchesPerRotation) * Math.cos(Math.toRadians(90-currentHeading));

//        telemetry.addData("mp.x inches moved: ", inchesX);

        return (float)inchesX;
    }

    public float getYInchesMoved()
    {
        double inchesY = ((-(currentXEncoder - xPrev) / tickPerRotation) * inchesPerRotation) * Math.sin(Math.toRadians(currentHeading)) +
                (((currentYEncoder - yPrev) / tickPerRotation) * inchesPerRotation) * Math.sin(Math.toRadians(90-currentHeading));
        return (float)inchesY;
    }

    public float getX()
    {
        double inchesX = (((currentXEncoder) / tickPerRotation) * inchesPerRotation) * Math.cos(Math.toRadians(currentHeading)) +
                (((currentYEncoder) / tickPerRotation) * inchesPerRotation) * Math.cos(Math.toRadians(90-currentHeading));
        return (float)inchesX;
    }

    public float getY()
    {
        double inchesY = ((-(currentXEncoder) / tickPerRotation) * inchesPerRotation) * Math.sin(Math.toRadians(currentHeading)) +
                (((currentYEncoder) / tickPerRotation) * inchesPerRotation) * Math.sin(Math.toRadians(90-currentHeading));
        return (float)inchesY;
    }

    public PVector getLocation()
    {
        PVector location = new PVector(getX(), getY());
        return location;
    }



    public void updateMotors(PVector neededVelocity, double spin)
    {
//        PVector neededVelocity = bot.desiredVelocity.copy();

//        telemetry.addData("mp.needed Velocity: ", neededVelocity);
//        PVector headingVector = PVector.fromAngle((float)Math.toRadians(currentHeading+90));
//        float rotation = PVector.angleBetween(headingVector, neededVelocity);
//        float rotation = (float)(90+currentHeading) - (float)Math.toDegrees(neededVelocity.heading());
//        telemetry.addData("mp.heading of neededVelocity: ", rotation);


//        telemetry.addData("mp.world velocity heading: ", Math.toDegrees(neededVelocity.heading()));

        neededVelocity.rotate((float)Math.toRadians(currentHeading));

//        telemetry.addData("mp.needed Velocity post rotate: ", neededVelocity);
//        telemetry.addData("mp.world velocity heading post rotate: ", Math.toDegrees(neededVelocity.heading()));


//        double x = neededVelocity.x / 31.4; //bot.maxSpeed; //max speed is 31.4 in/sec
//        double y = neededVelocity.y / 31.4; // bot.maxSpeed;
        double x = neededVelocity.x / 40.0; //bot.maxSpeed; //max speed is 31.4 in/sec
        double y = neededVelocity.y / 40.0; // bot.maxSpeed;


//        telemetry.addData("mp.right stick angular velocity: ", bot.joystickAngularVelocity);

        double turn = -spin / 343;
//        turn = -gamepad1.right_stick_x;

//        telemetry.addData("mp.desired velocity x: ", x);
//        telemetry.addData("mp.desired velocity y: ", y);

        joystickDrive(-x, -y, turn, 0, 1);
    }

    public void joystickDrive(double leftStickX, double leftStickY, double rightStickX, double rightStickY, double powerLimit)
    {
        /*
            These are the calculations need to make a simple mecaccnum drive.
              - The left joystick controls moving straight forward/backward and straight sideways.
              - The right joystick control turning.
        */
        double rightFront = (-leftStickY+rightStickX+leftStickX);
        double leftFront = (leftStickY+rightStickX+leftStickX);
        double rightRear=  (-leftStickY+rightStickX-leftStickX);
        double leftRear = (leftStickY+rightStickX-leftStickX);


        //Find the largest command value given and assign it to max.
        double max = 0.0;
        if (Math.abs(leftFront) > max)  { max = Math.abs(leftFront); }
        if (Math.abs(rightFront) > max) { max = Math.abs(rightFront); }
        if (Math.abs(leftRear) > max)   { max = Math.abs(leftRear); }
        if (Math.abs(rightRear) > max)  { max = Math.abs(rightRear); }

        //Set the minimum and maximum power allowed for drive moves and compare it to the parameter powerLimit.
        powerLimit = Range.clip(powerLimit, .05, 1);
        //If max still equals zero after checking all four motors, then set the max to 1
        if (max == 0.0)
        {
            max = 1;
        }

        // If max is greater than the power limit, divide all command values by max to ensure that all command
        // values stay below the magnitude of the power limit.
        if (max > powerLimit)
        {
            leftFront = leftFront / max * powerLimit;
            rightFront = rightFront / max * powerLimit;
            leftRear = leftRear / max * powerLimit;
            rightRear = rightRear / max *powerLimit;
        }

        RF.setVelocity(rightFront * 15.7, AngleUnit.RADIANS);
        RR.setVelocity(rightRear * 15.7, AngleUnit.RADIANS);
        LF.setVelocity(leftFront * 15.7, AngleUnit.RADIANS);
        LR.setVelocity(leftRear * 15.7, AngleUnit.RADIANS);


//////////////////////////////////////////////////////////////////////////
//    ////////if the robot is not moving, instruct the motors to hold their current position.///////
//        if(rightFront == 0 && leftFront == 0)
//        {
//            setMode(DcMotor.RunMode.RUN_TO_POSITION);
//            double rf = rFrontMotor.getCurrentPosition();
//            double lf = lFrontMotor.getCurrentPosition();
//            double rr = rRearMotor.getCurrentPosition();
//            double lr = lRearMotor.getCurrentPosition();
//            if (rFrontMotor != null)
//            {
//                rFrontMotor.setTargetPosition( (int) rf);
//            }
//
//            if (lFrontMotor != null)
//            {
//                lFrontMotor.setTargetPosition( (int) lf);
//            }
//
//            if (rRearMotor != null)
//            {
//                rRearMotor.setTargetPosition( (int) rr);
//            }
//
//            if (lRearMotor != null)
//            {
//                lRearMotor.setTargetPosition( (int) lr);
//            }
//        }
//        else
//        {
//            setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        }
//////////////////////////////////////////////////////////////////////////////////

    }



    public double getAngularVelocity()
    {
        AngularVelocity gyroReading;
        gyroReading = gyro.getAngularVelocity();
        telemetry.addData("mp.rot rate: ", -gyroReading.xRotationRate);
        return -gyroReading.xRotationRate;
    }

    public float getXLinearVelocity()
    {
        double linearX = xInPerSec * Math.cos(Math.toRadians(currentHeading)) +
                yInPerSec * Math.cos(Math.toRadians(90-currentHeading));
        return (float)linearX;
    }

    public float getYLinearVelocity()
    {
        double linearY = -yInPerSec * Math.sin(Math.toRadians(currentHeading)) +
                ( yInPerSec ) * Math.sin(Math.toRadians(90-currentHeading));
        return (float)linearY;
    }

    public PVector getVelocity()
    {
        PVector velocity = new PVector(getXLinearVelocity(), getYLinearVelocity());
        return velocity;
    }

    public void stop()
    {
        RF.setPower(0.0);
        RR.setPower(0.0);
        LF.setPower(0.0);
        LR.setPower(0.0);

    }




}