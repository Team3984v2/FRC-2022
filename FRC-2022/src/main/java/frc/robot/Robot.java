/*
Title: Pollock Code 2022
Purpose: Code that runs on the 2022 bot
Date Last Modified: 2022-02-15
*/

// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;


import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.CvSink;
import edu.wpi.first.cscore.CvSource;
import edu.wpi.first.cscore.MjpegServer;
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.cscore.VideoMode.PixelFormat;
import edu.wpi.first.wpilibj.ADXRS450_Gyro; // Gyro may need to be changed
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  public Constants constants = new Constants();
  public Functions functions = new Functions();




  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();



  // initializing Talon motor controllers for the drivetrain
  private final WPI_TalonSRX m_leftRearMotor = new WPI_TalonSRX(constants.kleftRearMotorChannel); 
  private final WPI_TalonSRX m_leftFrontMotor = new WPI_TalonSRX(constants.kleftFrontMotorChannel);
  private final WPI_TalonSRX m_rightRearMotor = new WPI_TalonSRX(constants.krightFrontMotorChannel); 
  private final WPI_TalonSRX m_rightFrontMotor = new WPI_TalonSRX(constants.krightRearMotorChannel);
  
  //Motor Controller Groups
  private final MotorControllerGroup leftGroup = new MotorControllerGroup(m_leftRearMotor, m_leftFrontMotor);
  private final MotorControllerGroup rightGroup = new MotorControllerGroup(m_rightRearMotor, m_rightFrontMotor);
  
  //Differential Drive
  private final DifferentialDrive m_motorGroup = new DifferentialDrive(leftGroup, rightGroup);

  // initializing Spark motor controllers for the drivetrain
  private final Spark m_intakeMotor = new Spark(constants.kPrimaryIntakeChannel);
  private final Spark m_secondaryIntake = new Spark(constants.kSecondaryIntakeChannel);
  private final Spark m_shootMotor = new Spark(constants.kShootingChannel);
  private final Spark m_winch = new Spark(constants.kWinchChannel);
  
  // driver controller(s)
  private final XboxController m_driverController = new XboxController(constants.kControllerChannel); 
  
  // pneumatics Still need to get the ports.
  private final Compressor comp = new Compressor(4, PneumaticsModuleType.CTREPCM);

  private final DoubleSolenoid rightSolenoid = new DoubleSolenoid(4,PneumaticsModuleType.CTREPCM, constants.kRightSolenoidChannel1, constants.kRightSolenoidChannel2);
  //private final DoubleSolenoid leftSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, constants.kLeftSolenoidChannel1, constants.kLeftSolenoidChannel2);
  private final DoubleSolenoid latchSolenoid = new DoubleSolenoid(4,PneumaticsModuleType.CTREPCM, constants.klatchSolenoidChannel1, constants.klatchSolenoidChannel2);

  //Drive Setting up Gyro 
  private ADXRS450_Gyro gyro = new ADXRS450_Gyro();

  private int shoot_index = 0;
  private int rumble_index = 0;

  private double kP = 1;
  private double heading = gyro.getAngle();
  private double error = heading - gyro.getAngle();
  private DigitalInput toplimitSwitch = new DigitalInput(0);
  
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {

    // Creates UsbCamera and MjpegServer [1] and connects them
    UsbCamera usbCamera = new UsbCamera("USB Camera 0", 0);
    MjpegServer mjpegServer1 = new MjpegServer("serve_USB Camera 0", 1181);
    mjpegServer1.setSource(usbCamera);

    // Creates the CvSink and connects it to the UsbCamera
    CvSink cvSink = new CvSink("opencv_USB Camera 0");
    cvSink.setSource(usbCamera);

    // Creates the CvSource and MjpegServer [2] and connects them
    CvSource outputStream = new CvSource("Blur", PixelFormat.kYUYV, 640, 480, 30);
    MjpegServer mjpegServer2 = new MjpegServer("serve_Blur", 1182);
    mjpegServer2.setSource(outputStream);

    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);


    //Setting up motors. Main thing: inverts neccessary motors.
    functions.setInitTalons(m_leftFrontMotor, m_rightFrontMotor, m_leftRearMotor, m_rightRearMotor);
    shoot_index = 0;


  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    //Driving
    m_motorGroup.arcadeDrive(-m_driverController.getLeftY(), m_driverController.getRightX());
    
    //Arm Solenoids
    if (m_driverController.getPOV() == 90){
      // arm goes up if right bumper is presesed
      rightSolenoid.set(Value.kForward);
      //leftSolenoid.set(Value.kForward);
    }
    if (m_driverController.getPOV() == 270){
      // arm goes down if left bumper is pressed.
      rightSolenoid.set(Value.kReverse);
      //leftSolenoid.set(Value.kReverse);
    }

    //Latch Solenoid
    if (m_driverController.getBackButton()){
      latchSolenoid.set(Value.kReverse);
    }
    if (m_driverController.getStartButton()){
      latchSolenoid.set(Value.kForward);
    }

    //Winch Motor
    if (m_driverController.getPOV() == 0){
      m_winch.set(.5);
    }else if (m_driverController.getPOV() == 180){
      m_winch.set(-1);
    }else{
      m_winch.stopMotor();
    }


    //Intake System
    if(m_driverController.getAButton()){
      m_intakeMotor.set(1);
      m_secondaryIntake.set(1);
    }
    if (m_driverController.getBButton()) {
      m_intakeMotor.set(-1);
      m_secondaryIntake.set(-1);
    }
    if(!m_driverController.getAButton() && !m_driverController.getBButton()) {
      m_intakeMotor.set(0);
      m_secondaryIntake.set(0);
    }

    //Shoot Motor

    switch (shoot_index) {
      case 0:
        if(m_driverController.getYButtonPressed()){
          m_shootMotor.set(1);
          shoot_index = 1;
        }
        break;
    
      case 1:
        if(m_driverController.getYButtonPressed()){
          m_shootMotor.set(0);
          shoot_index = 0;
        }
        break;
    }

    if (toplimitSwitch.get()) {
        // We are going up and top limit is tripped so stop
        SmartDashboard.putString("Latch Secure", "True");
          if (rumble_index <= 20){
            m_driverController.setRumble(RumbleType.kRightRumble, 1);
            m_driverController.setRumble(RumbleType.kLeftRumble, 1);
            rumble_index++;

          }
    } else {
        // We are going up but top limit is not tripped so go at commanded speed
        SmartDashboard.putString("Latch Secure", "False");
        rumble_index = 0;
    }


    
    

  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {

    //Test to see orientation each motor NOTE: Robot init still works 
    if (m_driverController.getLeftBumper()){
      m_leftRearMotor.set(1);
    }else{
      m_leftRearMotor.set(0);
    }
    if (m_driverController.getRightBumper()){
      m_rightFrontMotor.set(1);
    }else{
      m_rightFrontMotor.set(0);
    }
    if (m_driverController.getLeftTriggerAxis() > .5){
      m_leftFrontMotor.set(1);
    }else{
      m_leftFrontMotor.set(0);
    }
    if (m_driverController.getRightTriggerAxis() > .5){
      m_rightRearMotor.set(1);
    }else{
      m_rightRearMotor.set(0);
    }
    
    if(m_driverController.getAButton()){
      m_intakeMotor.set(1);
      m_secondaryIntake.set(1);
    }
    else if (m_driverController.getBButton()) {
      m_intakeMotor.set(-1);
      m_secondaryIntake.set(-1);
    }
    else if(!m_driverController.getAButton() && !m_driverController.getBButton()) {
      m_intakeMotor.set(0);
      m_secondaryIntake.set(0);
    }

    switch (shoot_index) {
      case 0:
        if(m_driverController.getYButtonPressed()){
          m_shootMotor.set(1);
          shoot_index = 1;
        }
        break;
    
      case 1:
        if(m_driverController.getYButtonPressed()){
          m_shootMotor.set(0);
          shoot_index = 0;
        }
        break;
    }

    if (m_driverController.getPOV() == 0){
      m_winch.set(.25);
    }else if (m_driverController.getPOV() == 180){
      m_winch.set(-.25);
    }else{
      m_winch.stopMotor();
    }

    if (m_driverController.getPOV() == 90){
      // arm goes up if right bumper is presesed
      rightSolenoid.set(Value.kForward);
      //leftSolenoid.set(Value.kForward);
    }
    else if (m_driverController.getPOV() == 270){
      // arm goes down if left bumper is pressed.
      rightSolenoid.set(Value.kReverse);
      //leftSolenoid.set(Value.kReverse);
    }

    if (m_driverController.getBackButton()){
      latchSolenoid.set(Value.kReverse);
    }else if (m_driverController.getStartButton()){
      latchSolenoid.set(Value.kForward);
    }



  }
}
