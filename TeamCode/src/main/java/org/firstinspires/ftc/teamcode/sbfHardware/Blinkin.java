/*
 * Copyright (c) 2018 Craig MacFarlane
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * (subject to the limitations in the disclaimer below) provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of Craig MacFarlane nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS LICENSE. THIS
 * SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firstinspires.ftc.teamcode.sbfHardware;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;

import java.util.concurrent.TimeUnit;


/**
 * Contains the hardware and usage methods for the REV Blinkin LED Driver
 * and any attached LED light strips.
 *
 * @author Andrew, 8668 Should Be Fine!
 * */
public class Blinkin
{
    /** A telemetry object passed down from the opmode -- used to send data to the driver station screen. */
    private Telemetry telemetry;
    /** A hardware map object passed down from the opmode -- used to access the hardware libraries. */
    private HardwareMap hardwareMap;
    /** A boolean that marks whether the class is being used in an autonomous opmode or a teleop opmode. */
    private boolean isTeleop;

    /**
     * Change the pattern every 10 seconds in AUTO mode.
     */
    private final static int LED_PERIOD = 10;

    /**
     * Rate limit gamepad button presses to every 500ms.
     */
    private final static int GAMEPAD_LOCKOUT = 500;

    /** The actual driver that controls the LEDs */
    RevBlinkinLedDriver blinkinLedDriver;
    /** An enum that lists all the preprogrammed LED colors and patterns. */
    RevBlinkinLedDriver.BlinkinPattern pattern;


    /** Tracks when there is 30 seconds left. */
    private Deadline isEndgame;
    /** Tracks when there is 15 seconds left. */
    private Deadline is15Seconds;
    /** Tracks when there is 5 seconds left. */
    private Deadline is5Seconds;

    private Deadline isGameOver;


    /** An enum that stores the display mode of the LEDs */
    protected enum DisplayKind {
        MANUAL,
        AUTO
    }

    /**
     * Runs once when the init button is pressed on the driver station. Initializes all the hardware
     * used by the class, initiates the telemetry and hardware map objects, and sets any needed
     * variables to their correct starting values.
     * @param telem  A telemetry object passed down from the opmode.
     * @param hwMap  A hardware object passed down from the opmode.
     * @param theIsTeleop  Tracks whether the class is being used by a teleop or an autonomous opmode.
     */
    public void init(Telemetry telem, HardwareMap hwMap, boolean theIsTeleop)
    {
        hardwareMap =hwMap;
        telemetry = telem;
        isTeleop = theIsTeleop;

        try
        {
            blinkinLedDriver = hardwareMap.get(RevBlinkinLedDriver.class, "lights");
            pattern = RevBlinkinLedDriver.BlinkinPattern.BREATH_BLUE;
//            setPattern(pattern);
            blinkinLedDriver.setPattern(pattern);
            telemetry.addData("success", " blinkin configured");
        }
        catch(Exception p_exception)
        {
            blinkinLedDriver = null;
            telemetry.addData("blinkin driver not found in config file", "...");
        }

        isEndgame = new Deadline(90, TimeUnit.SECONDS);
        is15Seconds = new Deadline(105, TimeUnit.SECONDS);
        is5Seconds = new Deadline(115, TimeUnit.SECONDS);
        isGameOver = new Deadline( 120, TimeUnit.SECONDS);
    }

    public void loop()
    {

    }

    /**
     * Is called when the START button is pressed on the drivers station.
     * Resets all the internal timers that control the light patterns (different light patterns activate
     * based on how much time is left in the match).
     * */
    public void start()
    {
        isEndgame.reset();
        is15Seconds.reset();
        is5Seconds.reset();
        isGameOver.reset();
        setPattern(RevBlinkinLedDriver.BlinkinPattern.ORANGE);
    }

    /** A control method that sets the lights on the robot to equal one of four possible states:
     * 1) If there is 30 seconds left in the match, set the lights to solid red.
     * 2) If there is 15 seconds left in the match, set the lights to slow blink red.
     * 3) If there es 5 seconds left in the match, set the lights to fast blink red.
     * 4) If none of the above are met, set lights to whatever the user specifies.
     *
     * @param thePattern  Accesses an enum type that is all the pre-programmed light patterns/colors*/
    public void setPattern(RevBlinkinLedDriver.BlinkinPattern thePattern)
    {
        if(blinkinLedDriver!=null)
        {
            if(isTeleop)
            {
                if (isGameOver.hasExpired() )
                {
                    blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.SHOT_WHITE);
                }
                else if(is5Seconds.hasExpired())
                {
                    blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.STROBE_RED);
                }
                else if(is15Seconds.hasExpired())
                {
                    blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_RED);
                }
                else if(isEndgame.hasExpired())
                {
                    blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED);
                }
                else
                {
                    blinkinLedDriver.setPattern(thePattern);
                }
            }
            else
            {
                blinkinLedDriver.setPattern(thePattern);
            }
        }
    }


}
