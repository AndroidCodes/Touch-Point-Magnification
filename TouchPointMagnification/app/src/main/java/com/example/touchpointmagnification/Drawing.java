package com.example.touchpointmagnification;

import android.graphics.Paint;
import android.graphics.Path;

public class Drawing {
	private int color;
	private Paint paint;
	private Path path;
	private int strokeWidth;

	public Drawing(Path p, int strokeWidth, int color, Paint paint) {
		this.path = p;
		this.strokeWidth = strokeWidth;
		this.paint = paint;
		this.color = color;
	}

	public Path getPath() {
		return this.path;
	}

	public void setPath(Path p) {
		this.path = p;
	}

	public int getStrokeWidth() {
		return this.strokeWidth;
	}

	public void setStrokeWidth(int strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	public int getColor() {
		return this.color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public Paint getPaint() {
		return this.paint;
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}
}
