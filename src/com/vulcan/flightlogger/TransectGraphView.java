package com.vulcan.flightlogger;

import java.util.List;

import android.view.View;

import com.vulcan.flightlogger.geo.data.Transect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class TransectGraphView extends View {

	// colors
	private int mNormalTransectColor;
	private int mActiveTransectColor;

	// drawing
	private Paint mPaint;
	
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
	
	@Override
	protected void onDraw(Canvas canvas) {

		float w = getWidth();
		float h = getHeight();

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
		
		if (mTransectList != null) {
			
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
				xCenteringOffset = (w - (lonRange * gpsToPixels)) / 2;
				yCenteringOffset = 0;
			} else {
				// wide
				gpsRange = lonRange;
				// width is our limiting factor...
				pixelRange = w;
				// computer scaler
				gpsToPixels = pixelRange / gpsRange; // e.g. 300px / 10 degrees = 30px/degree 
				// centering
				xCenteringOffset = 0;
				yCenteringOffset = (h - (latRange * gpsToPixels)) / 2;
			}
						
			for(int i=0;i<mTransectList.size();i++) {
				Transect trans = mTransectList.get(i);
				
				float fromX = calcPixelForLongitude(trans.mStartWaypt.getLongitude(), mMinLong, gpsToPixels, xCenteringOffset);
				float fromY = calcPixelForLatitude(trans.mStartWaypt.getLatitude(), mMinLat, gpsToPixels, yCenteringOffset, h);
				float toX = calcPixelForLongitude(trans.mEndWaypt.getLongitude(), mMinLong, gpsToPixels, xCenteringOffset);
				float toY = calcPixelForLatitude(trans.mEndWaypt.getLatitude(), mMinLat, gpsToPixels, yCenteringOffset, h);
				
				if (trans == mActiveTransect) {
					mPaint.setColor(mActiveTransectColor);
					mPaint.setStrokeWidth(8);
				} else {
					mPaint.setColor(mNormalTransectColor);
					mPaint.setStrokeWidth(4);
				}
				
				canvas.drawLine(fromX, fromY, toX, toY, mPaint);
			}
		}
	}
}
