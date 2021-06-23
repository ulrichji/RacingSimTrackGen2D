package org.isachsen.ulrich.game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import javax.imageio.ImageIO;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.lwjgl.opengl.GL11;

public class Track implements Drawable
{
	private ArrayList<Line2> lines;
	private int startX = 0;
	private int startY = 0;
	private double epsilon = 1.5;
	
	public void loadTrackFromImageFile(String path)
	{
		File imageFile = new File(path);
		BufferedImage img = null;
		try {
			img = ImageIO.read(imageFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		int width = img.getWidth();
		int height = img.getHeight();
		boolean[] binaryImg = new boolean[width * height];
		
		//Construct the binary image.
		for(int y=0;y<height;y++)
		{
			for(int x=0;x<width;x++)
			{
				int index = (y*width) + x;
				boolean isWhite = isWhite(img.getRGB(x, y));
				binaryImg[index] = isWhite;
			}
		}
		
		//Extract edges from the image
		boolean[] edge_image = new boolean[width * height];
		for(int y=0; y<height; y++)
		{
			for(int x=0; x<width; x++)
			{
				int index = (y * width) + x;
				if(x - 1 < 0)
					edge_image[index] = true;
				else if(x + 1 >= width)
					edge_image[index] = true;
				else if(y - 1 < 0)
					edge_image[index] = true;
				else if(y + 1 >= height)
					edge_image[index] = true;
				else if(binaryImg[index] == false && 
						(  binaryImg[index - 1] == true
						|| binaryImg[index + 1] == true
						|| binaryImg[index + width] == true
						|| binaryImg[index - width] == true
						|| binaryImg[index - 1 - width] == true
						|| binaryImg[index - 1 + width] == true
						|| binaryImg[index + 1 - width] == true
						|| binaryImg[index + 1 + width] == true))
					edge_image[index] = true;
				else
					edge_image[index] = false;
			}
		}
		
		ArrayList<Line2> lines = new ArrayList<Line2>();
		for(int i=0; i<width*height; i++)
		{
			boolean pixel = edge_image[i];
			if(pixel)
			{
				Stack<Integer> predecessors = new Stack<Integer>();
				Stack<Integer> successors = new Stack<Integer>();
				predecessors.add(i);
				successors.add(i);
				
				while(!successors.isEmpty())
				{
					int predecessor = predecessors.pop();
					int successor = successors.pop();
					
					int pred_x = predecessor % width;
					int pred_y = predecessor / width;
					int succ_x = successor % width;
					int succ_y = successor / width;
					
					lines.add(new Line2(new Position(pred_x + startX, pred_y + startY),
							new Position(succ_x + startX, succ_y + startY)));
					
					edge_image[successor] = false;
					if(succ_x + 1 < width && edge_image[successor + 1])
					{
						successors.add(successor + 1);
						predecessors.add(successor);
					}
					else if(succ_x - 1 >= 0 && edge_image[successor - 1])
					{
						successors.add(successor - 1);
						predecessors.add(successor);
					}
					else if(succ_y + 1 < height && edge_image[successor + width])
					{
						successors.add(successor+width);
						predecessors.add(successor);
					}
					else if(succ_y - 1 >= 0 && edge_image[successor - width])
					{
						successors.add(successor - width);
						predecessors.add(successor);
					}
				}
			}
		}
		
		this.lines = simplifyLines(lines);
		//this.lines = lines;
	}

	private ArrayList<Line2> simplifyLines(ArrayList<Line2> lines)
	{
		ArrayList<Line2> simplified_lines = new ArrayList<Line2>();
		
		ArrayList<ArrayList<Position>> positions = new ArrayList<ArrayList<Position>>();
		
		for(Line2 line : lines)
		{
			if(line.getFrom().equals(line.getTo()))
			{
				positions.add(new ArrayList<Position>());
				positions.get(positions.size() - 1).add(line.getTo());
			}
			else
			{
				positions.get(positions.size() - 1).add(line.getTo());
			}
		}
		
		for(ArrayList<Position> position_list : positions)
		{
			//System.out.println(position_list);
			ArrayList<Line2> new_lines = douglasPeucker(position_list, 0, position_list.size() - 1);
			simplified_lines.addAll(new_lines);
			System.out.println(new_lines);
		}
		
		return simplified_lines;
	}
	
	private ArrayList<Line2> douglasPeucker(ArrayList<Position> position_list, int start, int end)
	{
		ArrayList<Line2> return_list = new ArrayList<Line2>();
		
		//System.out.println(start + "  "+end);
		
		double dmax = 0;
		int index = start;
		
		Line2 new_line = new Line2(position_list.get(start), position_list.get(end));
		
		for(int i = start + 1; i < end - 1; i++)
		{
			Position p = position_list.get(i);
			double d = new_line.perpendicularDistance(p);
			if(d > dmax)
			{
				index = i;
				dmax = d;
			}
		}
		if(dmax > this.epsilon)
		{
			ArrayList<Line2> rec_results_1 = douglasPeucker(position_list, start, index);
			ArrayList<Line2> rec_results_2 = douglasPeucker(position_list, index, end);
			
			return_list.addAll(rec_results_1);
			return_list.addAll(rec_results_2);
		}
		else
		{
			return_list.add(new_line);
		}
		
		return return_list;
	}

	private boolean isWhite(int rgb)
	{
		//Extract the parts of the integer for each color.
		int  red   = (rgb & 0x00ff0000) >> 16;
        int  green = (rgb & 0x0000ff00) >> 8;
        int  blue  =  rgb & 0x000000ff;
        
        //Do a simple grayscale conversion as it is not going to be shown to user.
        int gray = (red + green + blue) / 3;
        
        if(gray > 128)
        	return true;
        return false;
	}

	@Override
	public java.awt.Rectangle getBoundingBox()
	{
		return null;
	}

	float div = 15.0f;
	@Override
	public void glDraw()
	{
		for (Line2 l : lines)
		{
			double line_length = l.length();
			double line_center_x = (l.getFrom().x + l.getTo().x) / 2.0;
			double line_center_y = (l.getFrom().y + l.getTo().y) / 2.0;
			//FIXME this is probably wrong. Equation is also used below
			double line_rotation = Math.atan2(l.getTo().y - l.getFrom().y, l.getTo().x - l.getFrom().x) +
					(Math.PI/2.0);
			
			GL11.glColor3d(1.0, 1.0, 1.0);
			
			GL11.glTranslated(line_center_x, line_center_y, 0);
			GL11.glRotated(line_rotation* 180.0 / Math.PI, 0, 0, 1);
			
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glVertex2d(-0.5, -line_length / 2);
				GL11.glVertex2d(0.5, -line_length / 2);
				GL11.glVertex2d(0.5, line_length / 2);
				GL11.glVertex2d(-0.5, line_length / 2);
			GL11.glEnd();
			
			GL11.glRotated(-line_rotation * 180.0 / Math.PI, 0, 0, 1);
			GL11.glTranslated(-line_center_x, -line_center_y, 0);
		}
	}

	public void addBodiesToWorld(World world)
	{	
		for(Line2 l : lines)
		{
			double line_length = l.length();
			double line_center_x = (l.getFrom().x + l.getTo().x) / 2.0;
			double line_center_y = (l.getFrom().y + l.getTo().y) / 2.0;
			double line_rotation = Math.atan2(l.getTo().y - l.getFrom().y, l.getTo().x - l.getFrom().x)
					+ (Math.PI / 2);
			
			if(line_length > 0)
			{
				Body b = new Body();
				Rectangle shape = new Rectangle(1.0, line_length);
				b.addFixture(shape);
				b.setMass(MassType.INFINITE);
				b.getTransform().setTranslation(line_center_x, line_center_y);
				b.getTransform().setRotation(line_rotation);
				
				world.addBody(b);
			}
		}
	}
	
}
