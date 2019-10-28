package org.firstinspires.ftc.teamcode.sbfUtil;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * In this sample, we demonstrate how to use the {@link OpenCvPipeline#onViewportTapped()}
 * callback to switch which stage of a pipeline is rendered to the viewport for debugging
 * purposes. We also show how to get data from the pipeline to your OpMode.
 */
@TeleOp
public class PipelineStageSwitchingExample extends LinearOpMode
{
    OpenCvCamera phoneCam;
    StageSwitchingPipeline stageSwitchingPipeline;


    @Override
    public void runOpMode()
    {
        /**
         * NOTE: Many comments have been omitted from this sample for the
         * sake of conciseness. If you're just starting out with EasyOpenCv,
         * you should take a look at {@link InternalCameraExample} or its
         * webcam counterpart, {@link WebcamExample} first.
         */

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        phoneCam = new OpenCvWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        phoneCam.openCameraDevice();
        stageSwitchingPipeline = new StageSwitchingPipeline();
        phoneCam.setPipeline(stageSwitchingPipeline);
        phoneCam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);

        waitForStart();

        while (opModeIsActive())
        {
            telemetry.addData("position: ",stageSwitchingPipeline.SSposition);
            telemetry.update();
            sleep(100);
        }
    }

    /*
     * With this pipeline, we demonstrate how to change which stage of
     * is rendered to the viewport when the viewport is tapped. This is
     * particularly useful during pipeline development. We also show how
     * to get data from the pipeline to your OpMode.
     */
    static class StageSwitchingPipeline extends OpenCvPipeline
    {
        Mat yCbCrChan2Mat = new Mat();
        Mat thresholdMat = new Mat();

        double leftSum = 0;
        double centerSum = 0;
        double rightSum = 0;

        String SSposition;

        enum Stage
        {
            YCbCr_CHAN2,
            THRESHOLD,
            RAW_IMAGE,
        }

        private Stage stageToRenderToViewport = Stage.YCbCr_CHAN2;
        private Stage[] stages = Stage.values();

        @Override
        public void onViewportTapped()
        {
            /*
             * Note that this method is invoked from the UI thread
             * so whatever we do here, we must do quickly.
             */

            int currentStageNum = stageToRenderToViewport.ordinal();

            int nextStageNum = currentStageNum + 1;

            if(nextStageNum >= stages.length)
            {
                nextStageNum = 0;
            }

            stageToRenderToViewport = stages[nextStageNum];
        }

        @Override
        public Mat processFrame(Mat input)
        {
             leftSum = 0;
             centerSum = 0;
             rightSum = 0;

             Scalar red = new Scalar(255,0,0);
             Scalar green = new Scalar(0,250,0);

             Scalar left = red;
             Scalar center = red;
             Scalar right = red;

            /*
             * This pipeline finds the contours of yellow blobs such as the Gold Mineral
             * from the Rover Ruckus game.
             */
            Imgproc.cvtColor(input, yCbCrChan2Mat, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(yCbCrChan2Mat, yCbCrChan2Mat, 2);
            Imgproc.threshold(yCbCrChan2Mat, thresholdMat, 102, 255, Imgproc.THRESH_BINARY_INV);

            for(int c = (int)(thresholdMat.cols()*.001); c < (int)(thresholdMat.cols()*.333); c++)
            {
                for(int r = (int)(thresholdMat.rows()*.4); r <= (thresholdMat.rows()*.6); r++)
                {
                    leftSum = leftSum + (thresholdMat.get(r,c))[0];
                }
            }
            for(int c = (int)(thresholdMat.cols()*.340); c < (int)(thresholdMat.cols()*.666); c++)
            {
                for(int r = (int)(thresholdMat.rows()*.4); r <= (thresholdMat.rows()*.6); r++)
                {
                    centerSum = centerSum + (thresholdMat.get(r,c))[0];
                }
            }
            for(int c = (int)(thresholdMat.cols()*.670); c < (int)(thresholdMat.cols()); c++)
            {
                for(int r = (int)(thresholdMat.rows()*.4); r <= (thresholdMat.rows()*.6); r++)
                {
                    rightSum = rightSum + (thresholdMat.get(r,c))[0];
                }
            }

            if(leftSum < rightSum && leftSum < centerSum)
            {
                SSposition = "LEFT";
                left = green;
            }
            else if(rightSum < leftSum && rightSum < centerSum)
            {
                SSposition = "RIGHT";
                right = green;
            }
            else
            {
                SSposition = "CENTER";
                center = green;
            }

            Imgproc.rectangle(
                    input,
                    new Point(
                            input.cols()*.001,
                            input.rows()*.4),
                    new Point(
                            input.cols()*.333,
                            input.rows()*.6),
                    left, 4);
            Imgproc.rectangle(
                    input,
                    new Point(
                            input.cols()*.340,
                            input.rows()*.4),
                    new Point(
                            input.cols()*.666,
                            input.rows()*.6),
                    center, 4);
            Imgproc.rectangle(
                    input,
                    new Point(
                            input.cols()*.670,
                            input.rows()*.4),
                    new Point(
                            input.cols()*1.0,
                            input.rows()*.6),
                    right, 4);



            switch (stageToRenderToViewport)
            {
                case YCbCr_CHAN2:
                {
                    return yCbCrChan2Mat;
                }

                case THRESHOLD:
                {
                    return thresholdMat;
                }

                case RAW_IMAGE:
                {
                    return input;
                }

                default:
                {
                    return input;
                }
            }
        }
    }
}