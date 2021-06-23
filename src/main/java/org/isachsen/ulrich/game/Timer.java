package org.isachsen.ulrich.game;

import java.util.ArrayList;

public class Timer implements Runnable
{
	private boolean runRequested = true;
	private long sleepTime;
	private double deltaTime;
	
	private ArrayList<Tickable> tickers = new ArrayList<Tickable>();
	
	public Timer(long fps)
	{
		sleepTime = 1000 / fps;
	}
	
	public double getDeltaTime()
	{
		return deltaTime;
	}
	
	public void addTicker(Tickable ticker)
	{
		tickers.add(ticker);
	}
	
	public void run()
	{
		while(runRequested)
		{
			long startTime = System.nanoTime();
			
			for(Tickable ticker:tickers)
			{
				ticker.tick();
			}
			
			long endTime = System.nanoTime();
			//The time the frame took in ms
			long frameTime = (endTime - startTime) / 1000000;
			long remainingSleepTime = sleepTime - frameTime;
			
			if(remainingSleepTime > 0)
			{
				try {
					Thread.sleep(remainingSleepTime);
					deltaTime = (double)sleepTime / 1000.0;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else
			{
				deltaTime = (double)frameTime / 1000.0;
			}
		}
	}

	public void stop()
	{
		runRequested = false;
	}
}
