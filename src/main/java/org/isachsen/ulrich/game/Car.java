package org.isachsen.ulrich.game;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3d;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertex2d;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

public class Car extends Body implements Drawable
{
	private CarController controller = null;
	
	private Wheel frontWheels;
	private Wheel backWheels;
	private double density = 10;
	private Rectangle shape;
	private double frontWheelBearing = 0.5;
	private double frontWheelDistanceToCenter = 1.0;
	private double backWheelDistanceToCenter = 1.0;
	private double brake = 0.0;
	private double ebrake = 0.0;
	private double brakeForce = 100.0;
	private double ebrakeForce = 50.0;
	private double throttle = 1.0;
	private double engineForce = 150.0;
	private double rollResistance = 0.1;
	private double airResistance = 0.1;
	
	public Car(double posX, double posY, double direction)
	{
		super();
		shape = new Rectangle(2.0, 1.0);
		frontWheels = new Wheel();
		backWheels = new Wheel();
		
		
		this.addFixture(shape);
		this.setMass(shape.createMass(density));
		this.setLinearDamping(0.5);
		Transform transform = this.getTransform();
		transform.setTranslation(posX, posY);
		transform.setRotation(direction);
		this.setTransform(transform);
	}

	private void handleControllerInput()
	{
		if(controller == null)
			return;
		
		brake = controller.getBrake(this);
		ebrake = controller.getEBrake(this);
		frontWheelBearing = controller.getSteeringAngle(this);
		throttle = controller.getThrottle(this);
	}
	
	public void phyicsTick(double deltaTime)
	{	
		handleControllerInput();
		
		//Calculate the swing component of the vehicle
		Vector2 relative_velocity = this.getLinearVelocity().copy();
		relative_velocity.rotate(-this.getTransform().getRotation());
		
		//TODO improve weight displacement
		double axle_weight_front = this.getMass().getMass() / 2.0;
		double axle_weight_rear = this.getMass().getMass() / 2.0;
		
		double yaw_speed_front = frontWheelDistanceToCenter * this.getAngularVelocity();
		double yaw_speed_rear = -backWheelDistanceToCenter * this.getAngularVelocity();
		
		double slip_angle_front = Math.atan2(relative_velocity.y + yaw_speed_front,
				Math.abs(relative_velocity.x)) - (Math.signum(relative_velocity.x) * this.frontWheelBearing);
		double slip_angle_back = Math.atan2(relative_velocity.y + yaw_speed_rear,
				Math.abs(relative_velocity.x));
		
		double tire_grip_front = frontWheels.dynamicFriction;
		double tire_grip_rear = backWheels.dynamicFriction;
		
		double friction_force_front = clamp(-frontWheels.cornerStiffness * slip_angle_front,
				-tire_grip_front, tire_grip_front) * axle_weight_front;
		double friction_force_rear = clamp(-backWheels.cornerStiffness * slip_angle_back,
				-tire_grip_rear, tire_grip_rear) * axle_weight_rear;
		
		double brake = Math.min((this.brake * this.brakeForce) + (this.ebrake * this.ebrakeForce),
				this.brakeForce);
		double throttle = this.throttle * this.engineForce;
		
		double traction_force_x = throttle - brake * Math.signum(relative_velocity.x);
		double traction_force_y = 0;
		
		double drag_force_x = (this.rollResistance * relative_velocity.x)
				- (this.airResistance * relative_velocity.x * Math.abs(relative_velocity.x));
		//Is there roll resistance in sideways direction?
		double drag_force_y = (this.rollResistance * relative_velocity.y)
				- (this.airResistance * relative_velocity.y * Math.abs(relative_velocity.y));
		
		double total_force_x = drag_force_x + traction_force_x;
		double total_force_y = drag_force_y + traction_force_y 
				+ (Math.cos(this.frontWheelBearing) * friction_force_front)
				+ friction_force_rear;
		
		double angular_torque = ((friction_force_front + traction_force_y) * frontWheelDistanceToCenter)
				- (friction_force_rear * backWheelDistanceToCenter);
		
		Vector2 linear_force = new Vector2(total_force_x, total_force_y);
		linear_force.rotate(this.getTransform().getRotation());
		
		this.applyForce(linear_force);
		this.applyTorque(angular_torque);
	}
	
	private double clamp(double a, double min, double max)
	{
		if(a < min)
			return min;
		else if(a > max)
			return max;
		return a;
	}

	@Override
	public java.awt.Rectangle getBoundingBox()
	{
		java.awt.Rectangle rect = new java.awt.Rectangle(-1000, -1000, 2000, 2000);
		return rect;
	}

	@Override
	public void glDraw()
	{
		double x = this.getTransform().getTranslationX();
		double y = this.getTransform().getTranslationY();
		double rotation = this.getTransform().getRotation() * (180.0/Math.PI);
		double width = shape.getWidth();
		double height = shape.getHeight();
		
		//Draw vehicle body
		glTranslated(x,y,0);
		glRotated(rotation,0,0,1);
		glBegin(GL_QUADS);
			glVertex2d(-width / 2, -height/2);
			glVertex2d(width / 2, -height/2);
			glVertex2d(width / 2, height/2);
			glVertex2d(-width / 2, height/2);
		glEnd();
		glRotated(-rotation,0,0,1);
		glTranslated(-x,-y,0);
		
		double wheel_width = width / 4;
		double wheel_height = height / 4;
		double sn = Math.sin(rotation * (Math.PI / 180.0));
		double co = Math.cos(rotation * (Math.PI / 180.0));
		glColor3d(0.0, 0.0, 0.0);
		
		//Draw front left wheel
		double wheel_center_x = x + ((width/2) * co) - ((height/2) * sn);
		double wheel_center_y = y + ((width/2) * sn) + ((height/2) * co);
		glTranslated(wheel_center_x, wheel_center_y, 0);
		glRotated(rotation + (frontWheelBearing*180.0/Math.PI),0,0,1);
		glBegin(GL_QUADS);
			glVertex2d(-wheel_width / 2, -wheel_height / 2);
			glVertex2d(wheel_width / 2, -wheel_height / 2);
			glVertex2d(wheel_width / 2, wheel_height / 2);
			glVertex2d(-wheel_width / 2, wheel_height / 2);
		glEnd();
		glRotated(-rotation - (frontWheelBearing*180.0/Math.PI),0,0,1);
		glTranslated(-wheel_center_x, -wheel_center_y, 0);
		
		//Draw front right wheel
		wheel_center_x = x + ((width/2) * co) - ((-height/2) * sn);
		wheel_center_y = y + ((width/2) * sn) + ((-height/2) * co);
		glTranslated(wheel_center_x, wheel_center_y, 0);
		glRotated(rotation + (frontWheelBearing*180.0/Math.PI),0,0,1);
		glBegin(GL_QUADS);
			glVertex2d(-wheel_width / 2, -wheel_height / 2);
			glVertex2d(wheel_width / 2, -wheel_height / 2);
			glVertex2d(wheel_width / 2, wheel_height / 2);
			glVertex2d(-wheel_width / 2, wheel_height / 2);
		glEnd();
		glRotated(-rotation - (frontWheelBearing*180.0/Math.PI),0,0,1);
		glTranslated(-wheel_center_x, -wheel_center_y, 0);
		
		//Draw rear left wheel
		wheel_center_x = x + ((-width/2) * co) - ((height/2) * sn);
		wheel_center_y = y + ((-width/2) * sn) + ((height/2) * co);
		glTranslated(wheel_center_x, wheel_center_y, 0);
		glRotated(rotation,0,0,1);
		glBegin(GL_QUADS);
			glVertex2d(-wheel_width / 2, -wheel_height / 2);
			glVertex2d(wheel_width / 2, -wheel_height / 2);
			glVertex2d(wheel_width / 2, wheel_height / 2);
			glVertex2d(-wheel_width / 2, wheel_height / 2);
		glEnd();
		glRotated(-rotation,0,0,1);
		glTranslated(-wheel_center_x, -wheel_center_y, 0);
		
		//Rear right wheel
		wheel_center_x = x + ((-width/2) * co) - ((-height/2) * sn);
		wheel_center_y = y + ((-width/2) * sn) + ((-height/2) * co);
		glTranslated(wheel_center_x, wheel_center_y, 0);
		glRotated(rotation,0,0,1);
		glBegin(GL_QUADS);
			glVertex2d(-wheel_width / 2, -wheel_height / 2);
			glVertex2d(wheel_width / 2, -wheel_height / 2);
			glVertex2d(wheel_width / 2, wheel_height / 2);
			glVertex2d(-wheel_width / 2, wheel_height / 2);
		glEnd();
		glRotated(-rotation,0,0,1);
		glTranslated(-wheel_center_x, -wheel_center_y, 0);
		
		glColor3d(1.0, 1.0, 1.0);
	}

	public void setCarController(CarController key_car_controller)
	{
		controller = key_car_controller;
	}
}
