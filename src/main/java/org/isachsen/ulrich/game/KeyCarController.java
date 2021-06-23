package org.isachsen.ulrich.game;

import org.dyn4j.geometry.Vector2;

public class KeyCarController implements InputKeyListener, CarController
{
	private double brake = 0;
	private double ebrake = 0;
	private double steeringAngle = 0;
	private double throttle = 0;
	
	@Override
	public void handleKey(long window, int key, int scancode, int action, int mods)
	{
		if(key == 263 && action == 1)
			steeringAngle = 0.5;
		else if(key == 263 && action == 0 && steeringAngle >= 0)
			steeringAngle = 0.0;
		else if(key == 262 && action == 1)
			steeringAngle = -0.5;
		else if(key == 262 && action == 0 && steeringAngle <= 0)
			steeringAngle = 0.0;
		else if(key == 265 && action == 1)
			throttle = 1.0;
		else if(key == 265 && action == 0)
			throttle = 0;
		else if(key == 264 && action == 1)
			brake = 1.0;
		else if(key == 264 && action == 0)
			brake = 0.0;
	}

	@Override
	public double getBrake(Car car)
	{
		Vector2 relative_velocity = car.getLinearVelocity().copy();
		relative_velocity.rotate(-car.getTransform().getRotation());
		double velocity = relative_velocity.x;
		
		if(velocity <= 0.1)
			return 0;
		
		return brake;
	}

	@Override
	public double getEBrake(Car car)
	{
		return ebrake;
	}

	@Override
	public double getSteeringAngle(Car car)
	{
		return steeringAngle;
	}

	@Override
	public double getThrottle(Car car)
	{
		Vector2 relative_velocity = car.getLinearVelocity().copy();
		relative_velocity.rotate(-car.getTransform().getRotation());
		double velocity = relative_velocity.x;
		
		if(velocity <= 0.1 && brake > 0)
		{
			System.out.println(-brake);
			return -brake;
		}
		System.out.println(throttle);
		
		return throttle;
	}	
}
