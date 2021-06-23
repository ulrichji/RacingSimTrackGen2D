package org.isachsen.ulrich.game;

public class Position
{
	public final int x,y;
	public Position(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if(other instanceof Position)
			return x == ((Position)other).x && y == ((Position)other).y;
		return false;
	}
	
	public String toString()
	{
		return "[Position x="+this.x+" y="+this.y+"]";
	}
}
