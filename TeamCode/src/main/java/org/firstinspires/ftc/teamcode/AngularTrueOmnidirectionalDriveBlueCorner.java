/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuMarkInstanceId;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import static com.sun.tools.javac.util.Constants.format;


@Autonomous(name="AngularTrueOmnidirectionalDriveBlueCorner", group="Linear Opmode")  // @Autonomous(...) is the other common choice
@Disabled
public class AngularTrueOmnidirectionalDriveBlueCorner extends LinearOpMode {

    //Auto init variables
    String sideColor = "blue";
    String startPosition = "corner";
    //=============================================================================================
    //=============================================================================================


    //vuforia shenanagins
    VuforiaLocalizer vuforia;
    OpenGLMatrix lastLocation = null;


    /* Declare OpMode members. */
    private ElapsedTime runtime = new ElapsedTime();
    DcMotor leftFront = null;
    DcMotor rightFront = null;
    DcMotor leftBack = null;
    DcMotor rightBack = null;
    Servo claw = null;
    DcMotor verticalLift = null;
    Servo arm = null;
    ColorSensor color = null;


    //driving variables
    double angle = 0;
    double driveAngle = 0;
    double deadZone = 0.25;
    double speed = 0.5;
    double pi = 3.14159265358979323846264338327950288419716939937510;

    //lift variables
    double clawClose = 0.3;
    double clawOpen = 0.7;
    double liftSpeed = 0.5;
    boolean clawIsOpen = true;


    //color sensing arm
    double armLowPos = 0;
    double armRaisePos = 1;
    String measuredColor = "none";

    //auto angle variables
    int FORWARDS = 0;
    int RIGHT_FORWARDS = 45;
    int RIGHT = 90;
    int RIGHT_BACKWARDS = 135;
    int BACKWARDS = 180;
    int LEFT_BACKWARDS = 225;
    int LEFT = 270;
    int LEFT_FORWARDS = 315;
    int SPIN_LEFT = -1000;
    int SPIN_RIGHT = 1000;
    int STOP = 1337;

    //auto constants
    double timer = 0;
    int loweringTime = 1000;
    int jewlKnockDistance = 1000;
    int vumarkSearchTime = 5000;
    int moveForwardDistance = 1000;

    //auto control variables
    String target = "center";
    boolean armLowered = false;
    boolean jewlRecovered = false;
    boolean lookingForVumark = true;
    boolean reCentered = false;
    boolean turned = false;
    boolean firstMove = false;
    boolean movedForward = false;


    @Override
    public void runOpMode() {

        //vuforia things ============================================================================================
        //int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        //VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        // OR...  Do Not Activate the Camera Monitor View, to save power
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        /*
         * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
         * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
         * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
         * web site at https://developer.vuforia.com/license-manager.
         *
         * Vuforia license keys are always 380 characters long, and look as if they contain mostly
         * random data. As an example, here is a example of a fragment of a valid key:
         *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
         * Once you've obtained a license key, copy the string from the Vuforia web site
         * and paste it in to your code onthe next line, between the double quotes.
         */
        parameters.vuforiaLicenseKey = "ARRYVKz/////AAAAGZxuAEFNDkCrkYt707UsjihZs15F76lsvH7AU/mlPnRZ3yAdhedSbovCnzPrTc4U6nQU0BbKTmXyYv+6l4YQzmIMIos9kWdCc9mFhExHofogzzGejNg38CogHWqIUFqwvbTFIzTwvsTDFTEJuJAduMh1nl4ui9YHjRWv5I3vrBJ96kzkIO1aC23JBA9w+JsMAXKk0PyBitnXq8hTY2x4SM8IVwmRJontBEvr3BUIHi2P8E1sMznS2bEshTvwmg2nOnD6IA9ChrKIP/YVbsO1HHGm9fmqTfoN/VBOiUskbzNBcmylv0jPZOhq+X2LnMRZinss3ZWn8KQE1VLPeVSIJdEAwx8rqyX+wvkqriFVwae/";

        /*
         * We also indicate which camera on the RC that we wish to use.
         * Here we chose the back (HiRes) camera (for greater range), but
         * for a competition robot, the front camera might be more convenient.
         */
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        /**
         * Load the data set containing the VuMarks for Relic Recovery. There's only one trackable
         * in this data set: all three of the VuMarks in the game were created from this one template,
         * but differ in their instance id information.
         * @see VuMarkInstanceId
         */
        VuforiaTrackables relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        VuforiaTrackable relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate"); // can help in debugging; otherwise not necessary
        //end of vuforia things =================================================================================================




        //hardware config
        leftFront = hardwareMap.dcMotor.get("leftFront");
        rightFront = hardwareMap.dcMotor.get("rightFront");
        leftBack = hardwareMap.dcMotor.get("leftBack");
        rightBack = hardwareMap.dcMotor.get("rightBack");

        claw = hardwareMap.servo.get("Lclaw");
        verticalLift = hardwareMap.dcMotor.get("verticalLift");
        arm = hardwareMap.servo.get("arm");

        color = hardwareMap.colorSensor.get("color");
        //color.setI2cAddress(I2cAddr.create8bit(0x4c));

        leftFront.setDirection(DcMotor.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.REVERSE);
        //end of hardware config

        telemetry.addData("Status", "Ready to begin");
        telemetry.update();

        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("Left Front encoder:", " " + leftFront.getCurrentPosition());
            telemetry.addData("Direction:", " " + driveAngle);
            telemetry.addData("Speed:"," " + speed);
            telemetry.addData("Raw angle:", " " + angle);
            telemetry.addData("Color: ", color.red() + " " + color.green() + " " + color.blue());
            telemetry.update();

            if (!armLowered){
                arm.setPosition(armLowPos);
                clawIsOpen = false;
                verticalLift.setPower(liftSpeed);
                if (timer == 0){
                    timer = runtime.milliseconds();
                } else if (runtime.milliseconds() >= timer + loweringTime){
                    armLowered = true;
                    timer = 0;
                    verticalLift.setPower(0);
                }
            } else if (!jewlRecovered){
                if(measuredColor == sideColor){
                    driveAngle = SPIN_RIGHT;
                    speed = 0.3;
                } else {
                    driveAngle = SPIN_LEFT;
                    speed = 0.3;
                }
                if(Math.abs(leftFront.getCurrentPosition()) > jewlKnockDistance){
                    jewlRecovered = true;
                    driveAngle = STOP;
                    speed = 0;
                }
            } else if(lookingForVumark) {
                driveAngle = SPIN_LEFT;
                speed = 0.15;

                RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
                if (vuMark != RelicRecoveryVuMark.UNKNOWN) {
                lookingForVumark = false;
                /* Found an instance of the template. In the actual game, you will probably
                 * loop until this condition occurs, then move on to act accordingly depending
                 * on which VuMark was visible. */
                    telemetry.addData("VuMark", "%s visible", vuMark);

                /* For fun, we also exhibit the navigational pose. In the Relic Recovery game,
                 * it is perhaps unlikely that you will actually need to act on this pose information, but
                 * we illustrate it nevertheless, for completeness. */
                    OpenGLMatrix pose = ((VuforiaTrackableDefaultListener) relicTemplate.getListener()).getPose();
                    telemetry.addData("Pose", format(pose));

                /* We further illustrate how to decompose the pose into useful rotational and
                 * translational components */
                    if (pose != null) {
                        VectorF trans = pose.getTranslation();
                        Orientation rot = Orientation.getOrientation(pose, AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES);

                        // Extract the X, Y, and Z components of the offset of the target relative to the robot
                        double tX = trans.get(0);
                        double tY = trans.get(1);
                        double tZ = trans.get(2);

                        // Extract the rotational components of the target relative to the robot
                        double rX = rot.firstAngle;
                        double rY = rot.secondAngle;
                        double rZ = rot.thirdAngle;
                    }
                } else {
                    telemetry.addData("VuMark", "not visible");
                }
                if (vuMark == RelicRecoveryVuMark.LEFT){
                    target = "left";
                } else if (vuMark == RelicRecoveryVuMark.RIGHT){
                    target = "right";
                } else {
                    target = "center";
                }
                if(timer == 0){
                    timer = runtime.milliseconds();
                } else if(runtime.milliseconds() >= timer + vumarkSearchTime){
                    lookingForVumark = false;
                    timer = 0;
                }
            } else if (!reCentered){
                if (leftFront.getCurrentPosition() > 100){
                    driveAngle = SPIN_RIGHT;
                    speed = 0.15;
                } else if (leftFront.getCurrentPosition() < -100){
                    driveAngle = SPIN_LEFT;
                    speed = 0.15;
                } else {
                    driveAngle = STOP;
                    speed = 0;
                    reCentered = true;
                }
            } else if (!turned){
                  if(sideColor == "blue"){
                      if(startPosition == "corner"){

                      } else {

                      }
                  } else {
                      if(startPosition == "corner"){

                      } else {

                      }
                  }
            } else if (!firstMove){
                if (startPosition == "corner") {
                    if (target == "left") {
                        if(sideColor == "blue"){
                            //left corner blue
                        } else {
                            //left corner red
                        }
                    } else if (target == "right"){
                        if(sideColor == "blue"){
                            //right corner blue
                        } else {
                            //right corner red
                        }
                    } else {
                        if(sideColor == "blue"){
                            //center corner blue
                        } else {
                            //center corner red
                        }
                    }
                } else {
                    if (target == "left") {
                        if(sideColor == "blue"){
                            //left side blue
                        } else {
                            //left side red
                        }
                    } else if (target == "right"){
                        if(sideColor == "blue"){
                            //right side blue
                        } else {
                            //right side red
                        }
                    } else {
                        if(sideColor == "blue"){
                            //center side blue
                        } else {
                            //center side red
                        }
                    }
                }
            } else if (!movedForward){
                angle = FORWARDS;
                speed = 0.2;
                if(timer == 0){
                    timer = leftFront.getCurrentPosition();
                } else if(leftFront.getCurrentPosition() >= timer + moveForwardDistance){
                    movedForward = true;
                    timer = 0;
                    clawIsOpen = true;
                    angle = STOP;
                    speed = 0;
                }
            }

            //claw
            if(clawIsOpen){
                claw.setPosition(clawOpen);
            } else{
                claw.setPosition(clawClose);
            }





            if (color.blue() > color.red() && color.blue() > 1){
                measuredColor = "blue";
            } else if(color.red() > color.blue() && color.red() > 1){
                measuredColor = "red";
            } else {
                measuredColor = "none";
            }


            if (driveAngle != STOP) {
                if (driveAngle == -SPIN_LEFT || driveAngle == SPIN_RIGHT) {
                    leftFront.setPower(speed * driveAngle / 1000);
                    rightFront.setPower(-speed * driveAngle / 1000);
                    leftBack.setPower(speed * driveAngle / 1000);
                    rightBack.setPower(-speed * driveAngle / 1000);
                } else {
                    leftFront.setPower(speed * Math.cos(pi * (driveAngle - 45) / 180));
                    rightFront.setPower(speed * Math.sin(pi * (driveAngle - 45) / 180));
                    leftBack.setPower(speed * Math.sin(pi * (driveAngle - 45) / 180));
                    rightBack.setPower(speed * Math.cos(pi * (driveAngle - 45) / 180));
                }
            }
        }
    }
}
