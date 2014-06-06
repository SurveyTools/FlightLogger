package com.vulcan.flightlogger;

import java.util.List;

import android.view.View;

import com.vulcan.flightlogger.geo.data.Transect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;

public class TransectGraphView extends View {

	// colors
	private int mNormalTransectColor;
	private int mActiveTransectColor;

	// drawing
	private Paint mPaint;
	private Path mArrowPath;
	
	// data
	private List<Transect> mTransectList;
	private Transect mActiveTransect;
	private double mMinLat = 0;
	private double mMinLong = 0;
	private double mMaxLat = 0;
	private double mMaxLong = 0;
	

	// for xml construction
	public TransectGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupVars();
	}

	// for code construction
	public TransectGraphView(final Context context) {
		super(context);
		setupVars();
	}

	private void setupVars() {

		this.mPaint = new Paint();
		this.mArrowPath = new Path();

		mNormalTransectColor = getResources().getColor(R.color.transect_graph_normal);
		mActiveTransectColor = getResources().getColor(R.color.transect_graph_active);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// save w & h here if need be
	}
	
	public void setTransectList(List<Transect> transectList, Transect activeTransect) {
		mTransectList = transectList;
		mActiveTransect = activeTransect;
		updateGpsBounds();
		invalidate();
	}

	protected void updateGpsBounds() {

		if (mTransectList == null) {
			mMinLat = mMaxLat = mMinLong = mMaxLong = 0;
		} else {
			for(int i=0;i<mTransectList.size();i++) {
				Transect trans = mTransectList.get(i);
				
				if (i==0) {
					// first one
					mMinLat = Math.min(trans.mStartWaypt.getLatitude(), trans.mEndWaypt.getLatitude());
					mMaxLat = Math.max(trans.mStartWaypt.getLatitude(), trans.mEndWaypt.getLatitude());
					mMinLong = Math.min(trans.mStartWaypt.getLongitude(), trans.mEndWaypt.getLongitude());
					mMaxLong = Math.max(trans.mStartWaypt.getLongitude(), trans.mEndWaypt.getLongitude());
				} else {
					// check to see if the bounds are expanded
					mMinLat = Math.min(trans.mStartWaypt.getLatitude(), mMinLat);
					mMinLat = Math.min(trans.mEndWaypt.getLatitude(), mMinLat);
					mMaxLat = Math.max(trans.mStartWaypt.getLatitude(), mMaxLat);
					mMaxLat = Math.max(trans.mEndWaypt.getLatitude(), mMaxLat);

					mMinLong = Math.min(trans.mStartWaypt.getLongitude(), mMinLong);
					mMinLong = Math.min(trans.mEndWaypt.getLongitude(), mMinLong);
					mMaxLong = Math.max(trans.mStartWaypt.getLongitude(), mMaxLong);
					mMaxLong = Math.max(trans.mEndWaypt.getLongitude(), mMaxLong);
				}
			}
		}
	}
	
	protected float calcPixelForLongitude(double gpsValue, double minGpsValue, double gpsToPixels, double centeringOffset) {
		double relativeGpsValue = gpsValue - minGpsValue;
		double relativePixelValue = relativeGpsValue * gpsToPixels;
		return (float)(centeringOffset + relativePixelValue);
	}
	
	protected float calcPixelForLatitude(double gpsValue, double minGpsValue, double gpsToPixels, double centeringOffset, double pixelRange) {
		double relativeGpsValue = gpsValue - minGpsValue;
		double relativePixelValue = relativeGpsValue * gpsToPixels;
		double reflectedPixelValue = (pixelRange > 0) ? pixelRange - relativePixelValue : relativePixelValue;
		return (float)(centeringOffset + reflectedPixelValue);
	}
	
	protected double calcAngleForPoints(float x1, float y1, float x2, float y2) {
		double opp = y2 - y1;
		double adj = x2 - x1;
		
		// atan is only good for -pi/2..pi/2 (right half of circle), so we reflect it and spin it back as necessary
		
		if (adj < 0) {
			// reflect and spin back into place
			return Math.atan(-opp/-adj) + Math.PI;
		} else {
			// normal
			return  Math.atan(opp/adj);
		}
	}
	
	protected void drawThinArrowOnLine(float x1, float y1, float x2, float y2, float tailLen, float arrowAngleInDegrees, float stroke, float loc, Canvas canvas) {

		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(stroke);

		// compute the arrow
		double transAngle = calcAngleForPoints(x1, y1, x2, y2);
		double fortyFive = Math.toRadians(arrowAngleInDegrees);
		double endAngle1 = transAngle + fortyFive;
		double endAngle2 = transAngle - fortyFive;
		
		// TESTING Log.d("TransectGraphView", "arrow " + i + ", line " + Math.toDegrees(transAngle) + ", end1 " + Math.toDegrees(endAngle1) + ", end2 " + Math.toDegrees(endAngle2)); 

		// arrow point location (0 = beginning, 1 = end)
		float x = x1 + ((x2 - x1) * loc);
		float y = y1 + ((y2 - y1) * loc);
		
		float tail1y = (float)(y - tailLen * Math.sin(endAngle1));
		float tail2y = (float)(y - tailLen * Math.sin(endAngle2));
		float tail1x = (float)(x - tailLen * Math.cos(endAngle1));
		float tail2x = (float)(x - tailLen * Math.cos(endAngle2));
		
		// draw the arrow
		canvas.drawLine(tail1x, tail1y, x, y, mPaint);
		canvas.drawLine(tail2x, tail2y, x, y, mPaint);
	}

	protected void drawFatArrowOnLine(float x1, float y1, float x2, float y2, float tailLen, float arrowAngleInDegrees, float stroke, float loc, Canvas canvas) {

		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setStrokeWidth(stroke);

		// compute the arrow
		double transAngle = calcAngleForPoints(x1, y1, x2, y2);
		double fortyFive = Math.toRadians(arrowAngleInDegrees);
		double endAngle1 = transAngle + fortyFive;
		double endAngle2 = transAngle - fortyFive;
		
		// TESTING Log.d("TransectGraphView", "arrow " + i + ", line " + Math.toDegrees(transAngle) + ", end1 " + Math.toDegrees(endAngle1) + ", end2 " + Math.toDegrees(endAngle2)); 
		// arrow point location (0 = beginning, 1 = end)
		// NOTE: the point of the arrow is on x,y... not really what you'd want for loc = 0
		float x = x1 + ((x2 - x1) * loc);
		float y = y1 + ((y2 - y1) * loc);

		float tail1y = (float)(y - tailLen * Math.sin(endAngle1));
		float tail2y = (float)(y - tailLen * Math.sin(endAngle2));
		float tail1x = (float)(x - tailLen * Math.cos(endAngle1));
		float tail2x = (float)(x - tailLen * Math.cos(endAngle2));
		
		// construct the arrow

		mArrowPath.reset();
		mArrowPath.moveTo(x, y);
		mArrowPath.lineTo(tail1x, tail1y);
		mArrowPath.lineTo(tail2x, tail2y);
		mArrowPath.lineTo(x, y);
		mArrowPath.close();

		// draw the arrow
		canvas.drawLine(tail1x, tail1y, x, y, mPaint);
		canvas.drawLine(tail2x, tail2y, x, y, mPaint);
		canvas.drawPath(mArrowPath, mPaint);
	}

	protected void drawCircleOnLine(float x1, float y1, float x2, float y2, float diameter, float stroke, float loc, Canvas canvas) {

		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setStrokeWidth(stroke);

		// compute the loc
		float x = x1 + ((x2 - x1) * loc);
		float y = y1 + ((y2 - y1) * loc);
		
		canvas.drawCircle(x, y, diameter, mPaint);
	}

	protected void testArrowQuadrant(float fromX, float fromY, float toX1, float toY1, float toX2, float toY2, float tailLen, float arrowAngleInDegrees, float arrowLoc, int n, int color, Canvas canvas) {

		float dx = (toX2 - toX1) / n;
		float dy = (toY2 - toY1) / n;
		
		float x = toX1;
		float y = toY1;
				
		mPaint.setColor(color);

		for(int i=0;i<n;i++) {
			
			// draw the line
			mPaint.setStyle(Paint.Style.STROKE);
			canvas.drawLine(fromX, fromY, x, y, mPaint);
			
			//drawThinArrowOnLine(fromX, fromY, x, y, tailLen, arrowAngleInDegrees, 4, arrowLoc, canvas);
			drawFatArrowOnLine(fromX, fromY, x, y, tailLen, arrowAngleInDegrees, 4, arrowLoc, canvas);
			
			x  += dx;
			y += dy;
		}
	}

	protected void testArrows(Canvas canvas) {

		int n = 10;
		int inset = 8;
		float tailLen = 24;
		float arrowLoc = .8f; // 0 = start, 1 = end
		float arrowAngleInDegrees = 35;
		float w = getWidth();
		float h = getHeight();

		float centerX = w/2;
		float centerY = h/2;
		float left = inset;
		float right = w - inset;
		float top = inset;
		float bottom = h -inset;
		
		// right testArrowSet(n, centerX, centerY, w-8, h, dx, dy, canvas);
		testArrowQuadrant(centerX, centerY, right, bottom, right, top, tailLen, arrowAngleInDegrees, arrowLoc, n, getResources().getColor(R.color.debug0), canvas);
		testArrowQuadrant(centerX, centerY, right, top, left, top, tailLen, arrowAngleInDegrees, arrowLoc, n, getResources().getColor(R.color.debug1), canvas);
		testArrowQuadrant(centerX, centerY, left, top, left, bottom, tailLen, arrowAngleInDegrees, arrowLoc, n, getResources().getColor(R.color.debug2), canvas);
		testArrowQuadrant(centerX, centerY, left, bottom, right, bottom, tailLen, arrowAngleInDegrees, arrowLoc, n, getResources().getColor(R.color.debug3), canvas);
	}

@Override
protected void onDraw(Canvas canvas)
{
	// TESTING testArrows(canvas); return;

	float inset = 16;
	float w = getWidth()-(inset*2);
	float h = getHeight()-(inset*2);

	/*
	 	TESTING
	 	
		mPaint.setColor( getResources().getColor(R.color.debug2));
		mPaint.setStyle(Paint.Style.FILL);
		RectF rrrr = new RectF(0, 0, w, h);
		canvas.drawRect(rrrr, mPaint);

		mPaint.setColor( getResources().getColor(R.color.debug1));
		mPaint.setStyle(Paint.Style.FILL);
		RectF rrr = new RectF(1, 1, w-2, h-2);
		canvas.drawRect(rrr, mPaint);
	*/
	
	if ((mTransectList != null) && (mTransectList.size() > 1))
	{
		double latRange = Math.abs(mMaxLat - mMinLat);
		double lonRange = Math.abs(mMaxLong - mMinLong);
		double gpsRange;
		double pixelRange;
		double gpsToPixels;
		double xCenteringOffset;
		double yCenteringOffset;
		
		if (latRange > lonRange) {
			// tall
			gpsRange = latRange;
			// height is our limiting factor
			pixelRange = h;
			// computer scaler
			gpsToPixels = pixelRange / gpsRange; // e.g. 300px / 10 degrees = 30px/degree 
			// centering
			xCenteringOffset = inset + ((w - (lonRange * gpsToPixels)) / 2);
			yCenteringOffset = inset;
		} 
		else 
		{
			// wide
			gpsRange = lonRange;
			// width is our limiting factor...
			pixelRange = w;
			// computer scaler
			gpsToPixels = pixelRange / gpsRange; // e.g. 300px / 10 degrees = 30px/degree 
			// centering
			xCenteringOffset = inset;
			yCenteringOffset = inset + ((h - (latRange * gpsToPixels)) / 2);
		}
					
		// go from back to front so our start-marker overlays the other lines if need be
		
		// ALT (move the arrows around)
		// float range = .2f; // don't go end to end.
		// the max is so things are more tightly grouped when the number of transects is low
		// float arrowPositionDelta = range / (float) Math.max(mTransectList.size(), 8);
		// float arrowPosition = (1.0f - range) / 2.0f;
		
		boolean activeTransectFound = false;
		float activeTransectFromX = 0;
		float activeTransectFromY = 0;
		float activeTransectToX = 0;
		float activeTransectToY = 0;
		
		for(int i=0;i<mTransectList.size();i++) 
		{
			Transect trans = mTransectList.get(i);
			boolean isActiveTransect = (trans == mActiveTransect);
			
			float fromX = calcPixelForLongitude(trans.mStartWaypt.getLongitude(), mMinLong, gpsToPixels, xCenteringOffset);
			float fromY = calcPixelForLatitude(trans.mStartWaypt.getLatitude(), mMinLat, gpsToPixels, yCenteringOffset, h);
			float toX = calcPixelForLongitude(trans.mEndWaypt.getLongitude(), mMinLong, gpsToPixels, xCenteringOffset);
			float toY = calcPixelForLatitude(trans.mEndWaypt.getLatitude(), mMinLat, gpsToPixels, yCenteringOffset, h);
			
			if (isActiveTransect) {
				// defer 'til the end
				activeTransectFound = true;
				activeTransectFromX = fromX;
				activeTransectFromY = fromY;
				activeTransectToX = toX;
				activeTransectToY = toY;
			} else {
				// color
				mPaint.setColor(mNormalTransectColor);

				// start
				drawCircleOnLine(fromX, fromY, toX, toY, 5, 4, 0.0f, canvas);
				
				// end
				drawFatArrowOnLine(fromX, fromY, toX, toY, 14, 20, 4, 1f, canvas);
				
				//line
				mPaint.setStrokeWidth(4);
				canvas.drawLine(fromX, fromY, toX, toY, mPaint);

				// kinda fancy, but tight parallel lines are actually a problem
				// ALT arrowPosition += arrowPositionDelta;
			}
		}
		
		// draw the active one last (so it overlaps)
		if (activeTransectFound) {
			mPaint.setColor(mActiveTransectColor);
			drawCircleOnLine(activeTransectFromX, activeTransectFromY, activeTransectToX, activeTransectToY, 9, 4, 0.0f, canvas);
			drawFatArrowOnLine(activeTransectFromX, activeTransectFromY, activeTransectToX, activeTransectToY, 30, 20, 4, 1f, canvas);
			mPaint.setStrokeWidth(8);
			canvas.drawLine(activeTransectFromX, activeTransectFromY, activeTransectToX, activeTransectToY, mPaint);
		}
	}
}
}
