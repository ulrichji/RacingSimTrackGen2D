package org.isachsen.ulrich.game;

public class Line2
{
	private Position startPos;
	private Position endPos;

	public Line2(Position toPos)
	{
		startPos = new Position(0,0);
		endPos = toPos;
	}
	public Line2(Position fromPos, Position toPos)
	{
		startPos = fromPos;
		endPos = toPos;
	}
	
	public String toString()
	{
		return "[Line2 from="+startPos.toString()+" to="+endPos.toString()+"]";
	}
	
	public double length()
	{
		double xDiff = endPos.x - startPos.x;
		double yDiff = endPos.y - startPos.y;
		return Math.sqrt((xDiff*xDiff)+(yDiff*yDiff));
	}
	
	public Position getFrom()
	{
		return this.startPos;
	}
	
	public Position getTo()
	{
		return this.endPos;
	}
	
	public double perpendicularDistance(Position p)
	{
		double x0 = p.x;
		double y0 = p.y;
		double x1 = startPos.x;
		double y1 = startPos.y;
		double x2 = endPos.x;
		double y2 = endPos.y;
		
		return Math.abs(((y2 - y1) * x0) - ((x2 - x1) * y0) + (x2 * y1) - (y2 * x1)) /
				Math.sqrt(((y2 - y1) * (y2 - y1)) + ((x2 - x1) * (x2 - x1)));
	}
}

