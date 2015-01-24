package org.usfirst.frc.team2823.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	static final double R = 2;
	static final double LEVEL_HEIGHT = 14;
	static final double ENCODER_RESOLUTION = 2048;
	static final double ELEVATOR_SPEED = 0.05;
	static final int MAXIMUM_LEVEL = 4;
	
	//RobotDrive myRobot;
	Joystick stick;
	int autoLoopCounter;
	Talon talon1;
	Talon talon2;
	Talon talon3;
	Talon talon4;
	Talon talon5;
	Encoder encoder;
	DigitalInput di;
	AnalogInput ai;
	Gyro myGyro;
	int newlevel;
	int oldlevel;
	double motorScale = 0.98;
	double currentAngle = 0;

	boolean BAMUpPressed = false;
	boolean BAMDownPressed = false;
	boolean StraightMode = false;
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    	//myRobot = new RobotDrive(0,1);
    	stick = new Joystick(0);
    	talon1 = new Talon(0);
    	talon2 = new Talon(1);
    	talon3 = new Talon(2);
    	talon4 = new Talon(3);
    	talon5 = new Talon(4);
    	encoder = new Encoder(0, 1, true, EncodingType.k4X);
    	di = new DigitalInput(2);
    	ai = new AnalogInput(1);
    	myGyro = new Gyro(0);
    	
    }
    
    /**
     * This function is run once each time the robot enters autonomous mode
     */
    public void autonomousInit() {
    	autoLoopCounter = 0;
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	if(autoLoopCounter < 100) //Check if we've completed 100 loops (approximately 2 seconds)
		{
			//myRobot.drive(-0.5, 0.0); 	// drive forwards half speed
			autoLoopCounter++;
			} else {
			//myRobot.drive(0.0, 0.0); 	// stop robot
		}
    }
    
    /**
     * This function is called once each time the robot enters tele-operated mode
     */
    public void teleopInit(){
    	encoder.reset();
    	myGyro.reset();
    
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
    	double axis1 = stick.getRawAxis(1);
    	double axis3 = stick.getRawAxis(3);
    	driveRobot(axis1, axis3);
    	if (stick.getRawButton(6))
    	{
    		if(!(newlevel >= MAXIMUM_LEVEL) && !BAMUpPressed)
    		{
        		newlevel += 1;	
    		}
    		BAMUpPressed = true;
    	}
    	else
    	{
    		BAMUpPressed = false;
    	}
    	
    	if (stick.getRawButton(8))
    	{
    		if(!(newlevel <= 1) && !BAMDownPressed)
    		{
        		newlevel -= 1;
    		}
    	}
    	else
    	{
    		BAMDownPressed = false;
    	}
    	
    	updateElevator();
    	
    	//DEBUG
    	//System.out.printf("%f \t %f \n", axis1, axis3);
    	System.out.printf("%b %s \n", di.get(), ai.getVoltage());
    	System.out.println("Gyro: " + myGyro.getAngle() + " Encoder: " + encoder.get());
        //myRobot.arcadeDrive(stick);
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    	LiveWindow.run();
    }
    
    public void driveRobot(double left, double right)
    {
    	if(left > 0)
    	{
    		left = Math.floor(4*left)/4;
    	}
    	else
    	{
    		left = Math.ceil(4*left)/4;
    	}
    	if(right > 0)
    	{
    		right = Math.floor(4*right)/4;
    	}
    	else
    	{
    		right = Math.ceil(4*right)/4;
    	}

    	if(Math.abs(left-right) < 0.0001)
    	{ //we're going straight and we're going to check if this is the beginning of our straight section
    		if(StraightMode == false)
    		{
    			myGyro.reset();
    			currentAngle = 0;
    			StraightMode = true;
    		}
    		if(myGyro.getAngle() > 0.25) // The robot is veering to the right
    		{
    			if(myGyro.getAngle() > currentAngle)
    			{
    				currentAngle = myGyro.getAngle();
    				motorScale -= 0.03; //reduce speed of 
    			}
    			if(right < -0.01)
    			{
    				left *= motorScale;
    			}
    			else
    			{
    				right *= motorScale;
    			}
    		}
    		else if(myGyro.getAngle() < -0.25) // The robot is veering to the left
    		{
    			if(myGyro.getAngle() < currentAngle)
    			{
    				currentAngle = myGyro.getAngle();
    				motorScale -= 0.03;
    			}
    			if(right < -0.01)
    			{
    				right *= motorScale;
    			}
    			else
    			{
    				left *= motorScale;
    			}
    		}
    		else
    		{
    			motorScale = 1;
    		}
    	}
    	else
    	{
    		StraightMode = false;
    	}

    	talon1.set(left);
    	talon2.set(left);
    	// Values are multiplied by -1 to ensure that the motors on the right spin opposite the motors on the left.
    	talon3.set(-1*right);
    	talon4.set(-1*right);
    }
    
    public void updateElevator()
    {
    	int difference = newlevel - oldlevel;
    	if (difference == 0)
    	{
    		talon5.set(0);
    	}
    	else 
    	{
    		if (Math.abs(encoder.get()) >= Math.abs((difference*ENCODER_RESOLUTION*LEVEL_HEIGHT)/(2*Math.PI*R)))
    		{
    			talon5.set(0);
    			oldlevel = newlevel;
    			encoder.reset();
    		}
    		else 
    		{
    			talon5.set(ELEVATOR_SPEED*(Math.signum(difference)));
    		}
    		   
    	}
    }
}
  