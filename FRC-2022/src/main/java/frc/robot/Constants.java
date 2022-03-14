package frc.robot;





/**
 * Class for Constants
 * <p>
 * 
 */
public class Constants{

  public final int kControllerChannel = 0;
  
  //Drivetrain constants
  public final int kleftRearMotorChannel = 0;
  public final int krightRearMotorChannel = 1;
  public final int kleftFrontMotorChannel = 2;
  public final int krightFrontMotorChannel = 3;

  //channels for solenoids
  public final int kRightSolenoidChannel2 = 4;
  public final int kRightSolenoidChannel1 = 5;
  //public final int kLeftSolenoidChannel1 = 2;
  //public final int kLeftSolenoidChannel2  = 3;
  public final int klatchSolenoidChannel2 = 0;    //Solenoid to latch onto the lift
  public final int klatchSolenoidChannel1 = 1;

  //Spark channels
  public final int kPrimaryIntakeChannel = 8;    //intake motors on the arm
  public final int kSecondaryIntakeChannel = 9;  // Lower Large Wheel intake motor attached to robot body
  public final int kShootingChannel = 6;         // Top Large wheel for shooting attached to the robot body
  public final int kWinchChannel = 7;
  
}
