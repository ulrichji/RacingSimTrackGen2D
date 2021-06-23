package org.isachsen.ulrich.game;

import java.awt.Rectangle;

public interface Drawable
{
	public Rectangle getBoundingBox();
	public void glDraw();
}
