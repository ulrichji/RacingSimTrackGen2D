package org.isachsen.ulrich.game;

import java.util.ArrayList;

import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Vector2;

class CarGameMaven implements Tickable
{
	private Display display;
	private World world;
	private Timer timer;
	private ArrayList<Car> cars = new ArrayList<Car>();	
	private Track track;
	
	public CarGameMaven()
	{
		world = new World();
		display = new Display();
		timer = new Timer(60);
		timer.addTicker(this);
		
		track = new Track();
		world.setGravity(new Vector2(0,0));
	}
	
	public void init()
	{
		cars.clear();
		display.removeAllDrawables();
		display.removeAllInputKeyListeners();
		world.removeAllBodies();
		display.run();
		
		track.loadTrackFromImageFile("C:/Users/ulric/Documents/workspace/cargamemaven/track.png");
		track.addBodiesToWorld(world);
		
		//Create some cars
		cars.add(new Car(0 + 115.0, 0 + 115.0, 1));
		cars.add(new Car(5.6 + 115.0, 7 + 115.0, Math.PI + 1));
		
		for(Car car : cars)
		{
			world.addBody(car);
			display.addDrawable(car);
		}
		display.addDrawable(track);
		
		KeyCarController key_car_controller = new KeyCarController();
		display.addInputKeyListener(key_car_controller);
		cars.get(0).setCarController(key_car_controller);
	}
	
	public void run()
	{
		timer.run();
	}
	
	@Override
	public void tick()
	{
		if(!display.closeRequested())
		{
			for(Car car : cars)
				car.phyicsTick(timer.getDeltaTime());
			world.update(timer.getDeltaTime());
			display.setWindowCenter(cars.get(0).getTransform().getTranslationX(),
					cars.get(0).getTransform().getTranslationY());
			display.setScale(50.0);
			display.draw();
		}
		else
		{
			stopGame();
		}
	}

	private void stopGame()
	{
		timer.stop();
		display.close();
	}
	
	public static void main(String[] args)
	{
		CarGameMaven game = new CarGameMaven();
		game.init();
		game.run();
	}
}