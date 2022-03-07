
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

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
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

  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  // initializing constants for use throughout the program
  private static final int kLeftMotorChannel = 0;
  private static final int kRightMotorChannel = 1;
  private static final int kControllerChannel = 0;
  //TODO - set channels for solenoids
  private static final int kRightSolenoidChannel1 = 1;
  private static final int kRightSolenoidChannel2 = 0;
  private static final int kLeftSolenoidChannel1 = 1;
  private static final int kLeftSolenoidChannel2  = 0;

  private static final int kintakeMotorChannel

  // initializing Spark motor controllers for the drivetrain
  private final WPI_TalonSRX m_leftMotor = new WPI_TalonSRX(kLeftMotorChannel); 
  private final WPI_TalonSRX m_rightMotor = new WPI_TalonSRX(kRightMotorChannel);
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_leftMotor, m_rightMotor);

  private final Spark m_intakeMotor = new Spark(kintakeMotorChannel);

  // driver controller(s)
  private final XboxController m_driverController = new XboxController(kControllerChannel); 
  // pneumatics Still need to get the ports.
  private final DoubleSolenoid rightSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, kRightSolenoidChannel1, kRightSolenoidChannel2);
  private final DoubleSolenoid leftSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, kLeftSolenoidChannel1, kLeftSolenoidChannel2);
  private final Compressor comp= new Compressor(9, PneumaticsModuleType.CTREPCM);
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    // when robot is started, everything should be retracted.
    rightSolenoid.set(Value.kReverse);
    leftSolenoid.set(Value.kReverse);

    // need to invert one side of the drivetrain, uncomment and edit as needed
    // m_rightMotor.setInverted(true);
  }
  //solenoids:::}}}

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {

  }

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
    // In the begining og autonomus, the intake system is deployed
    rightSolenoid.set(Value.kForward);
    leftSolenoid.set(Value.kForward);
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
  public void teleopInit() {
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    m_robotDrive.arcadeDrive(-m_driverController.getLeftY(), m_driverController.getRightX());
    
    if (m_driverController.getRightBumperPressed()){
      // arm goes up if right bumper is presesed
      rightSolenoid.set(Value.kForward);
      leftSolenoid.set(Value.kForward);
    }
    else if (m_driverController.getLeftBumper()){
      // arm goes down if left bumper is pressed.
      rightSolenoid.set(Value.kReverse);
      leftSolenoid.set(Value.kReverse);
    }
    else{
      // Stops the solenoids if the button is not pressed?
      rightSolenoid.set(Value.kOff);
      leftSolenoid.set(Value.kOff);
    }
    
    if(m_driverController.getLeftBumper()){
      m_intakeMotor.set(1);
    }
    else if (m_driverController.getRightBumper()) {
      m_intakeMotor.set(-1);
    }
    else if(!m_driverController.getRightBumper() && !m_driverController.getLeftBumper()) {
      m_intakeMotor.set(0);
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
  public void testPeriodic() {}
}
