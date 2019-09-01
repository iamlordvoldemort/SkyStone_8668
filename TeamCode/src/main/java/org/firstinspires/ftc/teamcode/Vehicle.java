package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Credit:
 *
 * @author Andrew, SBF Robotics
 */
public class Vehicle
{
    public PVector location;
    public PVector velocity;
    public PVector acceleration;
    public PVector desiredVelocity;

    double endZone;
    double maxSpeed;
    double gain;

    Telemetry telemetry;

    Vehicle(float x, float y, Telemetry telem)
    {
        telemetry = telem;
        acceleration = new PVector(0, 0);
        velocity = new PVector(0,0);
        location = new PVector(x,y);
        desiredVelocity = new PVector(0,0);
        endZone = 6; //inch
        gain = 35;

        // 30 in/sec correlates to maximum unloaded motor speed //
        maxSpeed = 15; //inches/second
        //30.615 in/sec = 15.7 rad/sec * 1.3 gearing * 1.5 in wheel radius


    }

    public void arrive(PVector target)
    {
        //find the needed velocity to move to target and call it desiredVelocity
        desiredVelocity = PVector.sub(target, location);

        telemetry.addData("v.desired velocity: ", desiredVelocity);

        //speed is the magnitude of desiredVelocity
        float speed = desiredVelocity.mag();

        if(speed < endZone)
        {
            float m = scaleVector(speed, 0, (float)endZone, 0, (float)maxSpeed);
            desiredVelocity.setMag(m);
        }
        else
        {
            //set speed to maximum allowed speed
            desiredVelocity.setMag(maxSpeed);
        }

        telemetry.addData("v.desired velocity set maxSpd: ", desiredVelocity);


        // steerAcceleration is the amount of needed change in velocity
        PVector steerAcceleration = PVector.sub(desiredVelocity, velocity);
        telemetry.addData("v.steerAcceleration: ", steerAcceleration);

        // limit rate of change to robot velocity
        steerAcceleration.limit(gain);

        telemetry.addData("v.limit steerAcceleration: ", steerAcceleration);


        //corrects robot velocity by adding error (steerAcceleration)
        desiredVelocity = PVector.add(velocity, steerAcceleration);

        //make sure final velocity isn't too fast
        desiredVelocity.limit(maxSpeed);

        telemetry.addData("v.desired velocity: ", desiredVelocity);

    }

    /**
     * Given a start waypoint and end waypoint, take the robot's current location and move the robot
     * first to the start point and then to the end point.
     */
    public void follow()
    {
        double radius = 5;
        PVector target;

        PVector start = new PVector(20,0);
        PVector end = new PVector(0,30);

        PVector velocityCopy = velocity.copy();
        velocityCopy.setMag(2);

        PVector projectedLoc = PVector.add(location, velocityCopy);

        if(projectedLoc.dist(end) < radius)
        {
            target = end.copy();
        }
        else
        {
            PVector normalPoint = getNormalPoint(projectedLoc, start, end);

            PVector pathDirection = PVector.sub(end, start);
            pathDirection.setMag(5.0);

            target = normalPoint.copy();
            target.add(pathDirection);
        }

        arrive(target);
    }


    /**
     * A function to get the normal point from a point (p) to a line segment (a-b)
     * This function could be optimized to make fewer new Vector objects
     *
     * @param projectedLoc  The projected location of the robot. Calculated by subtracting the
     *                      projected location from the starting position and subtracting the target
     *                      position from the start position -- then find the dot product of the
     *                      two values.
     * @param segmentStart  The first waypoint. Generally the point where the robot starts from,
     *                      although this is not necessarily the case.
     * @param segmentEnd  The target waypoint to which the robot is attempting to drive.
     * @return The target direction the robot will drive at to move towards the end point.
     * */
    public PVector getNormalPoint(PVector projectedLoc, PVector segmentStart, PVector segmentEnd) {
        // Vector from a to p
        PVector ap = PVector.sub(projectedLoc, segmentStart);
        // Vector from a to b
        PVector ab = PVector.sub(segmentEnd, segmentStart);
        ab.normalize(); // Normalize the line
        // Project vector "diff" onto line by using the dot product
        ab.mult(ap.dot(ab));
        PVector normalPoint = PVector.add(segmentStart, ab);
        return normalPoint;
    }


    public float scaleVector(float value, float start1, float stop1, float start2, float stop2)
    {
        float outgoing = start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));

        return outgoing;
    }


}
