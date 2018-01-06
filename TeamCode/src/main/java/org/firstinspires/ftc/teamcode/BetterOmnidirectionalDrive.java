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


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import android.media.MediaPlayer;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuMarkInstanceId;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;


@TeleOp(name="BetterOmnidirectionalDrive", group="Linear Opmode")  // @Autonomous(...) is the other common choice
//@Disabled
public class BetterOmnidirectionalDrive extends LinearOpMode {

    /* Declare OpMode members. */
    private ElapsedTime runtime = new ElapsedTime();
    DcMotor leftFront = null;
    DcMotor rightFront = null;
    DcMotor leftBack = null;
    DcMotor rightBack = null;
    DcMotor verticalLift = null;
    Servo relicArmClaw = null;
    Servo Lclaw = null;
    Servo Rclaw = null;
    Servo arm = null;
    DcMotor relicArmVertical = null;
    DcMotor relicArmHorizontal = null;

    MediaPlayer grab;
    MediaPlayer start_sound;
    MediaPlayer ult;
    MediaPlayer power_fist;
    //driving variables
    
    double deadZone = 0.15;
    double verticalLiftSpeed = 1;
    int slowFactor = 3;
    double g1Ly;
    double g1Lx;
    double g1Rx;
    int FL_motor_position;
    int FR_motor_position;
    boolean toggleA = false;
    boolean flag = true;
    boolean flag2 = true;
    boolean flag3 = false;
    boolean flag4 = true;
    boolean toggleLB = false;
    boolean toggleRT = false;







    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    VuforiaLocalizer vuforia;


    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        grab = MediaPlayer.create(hardwareMap.appContext, R.raw.rocket_grab);
        start_sound = MediaPlayer.create(hardwareMap.appContext, R.raw.blitzcrank_startup);
        ult = MediaPlayer.create(hardwareMap.appContext, R.raw.ult_sound);
        power_fist = MediaPlayer.create(hardwareMap.appContext, R.raw.power_fist);


        leftFront = hardwareMap.dcMotor.get("leftFront");
        rightFront = hardwareMap.dcMotor.get("rightFront");
        leftBack = hardwareMap.dcMotor.get("leftBack");
        rightBack = hardwareMap.dcMotor.get("rightBack");
        verticalLift = hardwareMap.dcMotor.get("verticalLift");
        Lclaw = hardwareMap.servo.get("Lclaw");
        Rclaw = hardwareMap.servo.get("Rclaw");
        arm = hardwareMap.servo.get("arm");
        relicArmVertical = hardwareMap.dcMotor.get("relicArmVertical");
        relicArmHorizontal = hardwareMap.dcMotor.get("relicArmHorizontal");
        relicArmClaw = hardwareMap.servo.get("relicArmClaw");
        //color = hardwareMap.colorSensor.get("color");
        //color.setI2cAddress(I2cAddr.create8bit(0x4c));


        leftFront.setDirection(DcMotor.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.REVERSE);
        verticalLift.setDirection(DcMotor.Direction.REVERSE);
        Lclaw.setDirection(Servo.Direction.FORWARD);
        Rclaw.setDirection(Servo.Direction.REVERSE);





        /**
         * Load the data set containing the VuMarks for Relic Recovery. There's only one trackable
         * in this data set: all three of the VuMarks in the game were created from this one template,
         * but differ in their instance id information.
         * @see VuMarkInstanceId
         */
        arm.setPosition(.6);

        telemetry.addData("Status", "Ready to begin");
        telemetry.update();


        start_sound.start();
        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            FL_motor_position = leftFront.getCurrentPosition();
            FR_motor_position = rightFront.getCurrentPosition();
            g1Ly = -gamepad1.left_stick_y;
            g1Lx = gamepad1.left_stick_x;
            g1Rx = gamepad1.right_stick_x;

            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("left trigger", " " + gamepad1.left_trigger);
            telemetry.addData("encoder position LF", " " + FL_motor_position);
            telemetry.addData("encoder position RF", " " + FR_motor_position);
            telemetry.addData("left bumper:", " " + gamepad1.left_bumper);


            //telemetry.addData("Color: ", color.red() + " " + color.green() + " " + color.blue());
            telemetry.update();

            //for rotation
            if (g1Rx > deadZone || g1Rx < -deadZone) {
                //for precision movement
                if (toggleLB) {
                    leftBack.setPower(g1Rx / slowFactor);
                    rightFront.setPower(-g1Rx / slowFactor);
                    leftFront.setPower(g1Rx / slowFactor);
                    rightBack.setPower(-g1Rx / slowFactor);
                    telemetry.addData("rotation speed", " " + g1Rx / slowFactor);
                } else {//for fast movement
                    leftBack.setPower(g1Rx);
                    rightFront.setPower(-g1Rx);
                    leftFront.setPower(g1Rx);
                    rightBack.setPower(-g1Rx);
                    telemetry.addData("rotation speed", " " + g1Rx);
                }
            }

            //for left and right
            else if (Math.abs(g1Lx) > deadZone && Math.abs(g1Ly) < deadZone) {
                if (toggleLB) {
                    leftBack.setPower(-g1Lx / slowFactor);
                    rightFront.setPower(-g1Lx / slowFactor);
                    leftFront.setPower(g1Lx / slowFactor);
                    rightBack.setPower(g1Lx / slowFactor);
                } else {
                    leftBack.setPower(-g1Lx);
                    rightFront.setPower(-g1Lx);
                    leftFront.setPower(g1Lx);
                    rightBack.setPower(g1Lx);
                }
            }

            //for up and down
            else if (Math.abs(g1Ly) > deadZone && Math.abs(g1Lx) < deadZone) {
                if (toggleLB) {
                    leftBack.setPower(g1Ly / slowFactor);
                    rightFront.setPower(g1Ly / slowFactor);
                    leftFront.setPower(g1Ly / slowFactor);
                    rightBack.setPower(g1Ly / slowFactor);
                } else {
                    leftBack.setPower(g1Ly);
                    rightFront.setPower(g1Ly);
                    leftFront.setPower(g1Ly);
                    rightBack.setPower(g1Ly);
                }
            }

            //for top left and bottem right
            else if (g1Ly > deadZone && g1Lx < -deadZone || g1Ly < -deadZone && g1Lx > deadZone) {
                if (toggleLB){
                leftBack.setPower((((g1Ly - g1Lx) / 2) * 1.4)/slowFactor);
                rightFront.setPower(((g1Ly - g1Lx) / 2) * 1.4);
                leftFront.setPower(0);
                rightBack.setPower(0);
            }else {

                    leftBack.setPower(((g1Ly - g1Lx) / 2) * 1.4);
                    rightFront.setPower(((g1Ly - g1Lx) / 2) * 1.4);
                    leftFront.setPower(0);
                    rightBack.setPower(0);
                }
            }

            //for top right and bottem left
            else if (g1Ly > deadZone && g1Lx > deadZone || g1Ly < -deadZone && g1Lx < -deadZone) {
                if(toggleLB){
                    leftBack.setPower(0);
                    rightFront.setPower(0);
                    leftFront.setPower((((g1Ly + g1Lx) / 2) * 1.4)/slowFactor);
                    rightBack.setPower((((g1Ly + g1Lx) / 2) * 1.4)/slowFactor);
                }else {
                    leftBack.setPower(0);
                    rightFront.setPower(0);
                    leftFront.setPower(((g1Ly + g1Lx) / 2) * 1.4);
                    rightBack.setPower(((g1Ly + g1Lx) / 2) * 1.4);
                }
                }

            //to stop
            else {
                leftBack.setPower(0);
                rightFront.setPower(0);
                leftFront.setPower(0);
                rightBack.setPower(0);
            }

            if(gamepad1.y || gamepad2.y)
            {
                ult.start();
            }

            //for claw movement
            if ((gamepad1.a && flag) || (gamepad2.a && flag)){
                toggleA = !toggleA;
                flag = false;

            }
            else if(!gamepad1.a && !gamepad2.a) {
                flag = true;
            }
            //starting position
            if(!toggleA) {
                Lclaw.setPosition(0.3);
                Rclaw.setPosition(0.4);

            }
            //closed position
            else{
                Lclaw.setPosition(0.02);
                Rclaw.setPosition(0.15);

                grab.start();
            }

            //for lift movement
            if(gamepad1.dpad_up || gamepad2.dpad_up)
            {
                verticalLift.setPower(verticalLiftSpeed);
                if(flag4){
                    flag4 = false;

                }
            }
            else if(gamepad1.dpad_down || gamepad2.dpad_down)
            {
                verticalLift.setPower(-verticalLiftSpeed);
                flag4 = true;
            }
            else
            {
                verticalLift.setPower(0);
                flag4 = true;
            }

            //VUFORIA




            if((gamepad1.a || gamepad2.a) && flag )
            {
                toggleA = !toggleA;
                flag = false;
            }
            else if (!gamepad1.a && !gamepad2.a)
            {
                flag = true;
            }

            if((gamepad1.left_bumper || gamepad2.left_bumper) && flag2 )
            {
                toggleLB = !toggleLB;
                flag2 = false;
            }
            else if (!gamepad1.left_bumper && !gamepad2.left_bumper)
            {
                flag2 = true;
            }
            if(Math.abs(gamepad2.right_stick_y) > deadZone){

                relicArmVertical.setPower(gamepad2.right_stick_y);

            }
            else{
                relicArmVertical.setPower(0);
            }
            if(Math.abs(gamepad2.right_stick_x) > deadZone){
                relicArmHorizontal.setPower(gamepad2.right_stick_x);
            }
            else{
                relicArmHorizontal.setPower(0);
            }
            if (gamepad2.right_trigger > .3 && flag3){
                toggleRT = !toggleRT;
                flag3 = false;

            }
            else if(gamepad2.right_trigger <= 0.3) {
                flag3 = true;
            }
            if(toggleRT){
                relicArmClaw.setPosition(.5);
            }
            else{
                relicArmClaw.setPosition(.2);
            }
        }
    }
}