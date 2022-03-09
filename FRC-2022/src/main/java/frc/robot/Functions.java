package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;



/**
 * 
 * Class for functions.
 * <p> NOTE: Functions are reuseable code. Methods are object oriented programing
 * <p>
 */
public class Functions {


    /**
     * Will set the motors initial conditions
     * <p>Inverts Motors
     * 
     * @param frontLefTalonSRX talon motor
     * @param frontRightTalonSRX talon motor
     * @param rearLeftTalonSRX talon motor
     * @param rearRightTalonSRX talon motor
     */
    public void setInitTalons(WPI_TalonSRX frontLefTalonSRX, WPI_TalonSRX frontRightTalonSRX, WPI_TalonSRX rearLeftTalonSRX, WPI_TalonSRX rearRightTalonSRX ){
    
        //frontLefTalonSRX.setInverted(true);
        frontRightTalonSRX.setInverted(true);
        //rearLeftTalonSRX.setInverted(true);
        rearRightTalonSRX.setInverted(true);
    
    
    }


    /**
     * This takes the double value of the input and 
     * <p> outputs 0 if |input| < range.
     * <p> this is a deadband and it will prevent the
     * <p> motors from running if there is too little of an input.
     * 
     * @param input raw value
     * @param range positive double value of the "deadzone" range
     * @return The input value (if greater than range), or zero if less than.
     */
    public double doubleDeadBand(double input, double range){

        if (Math.abs(input) < range){
            return 0;
        }
        else{
            return input;
        }
        
    }


    


}
