package com.vulcan.flightlogger;

import android.view.View;
import com.vulcan.flightlogger.AltitudeDatum;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.graphics.RectF;
// import android.graphics.Color;
// import android.util.Log;

public class TransectILSView extends View {
	
	// altitude
	private AltitudeDatum	mCurAltitude;
	private float	mAltitudePixelOffset; // up or down position (center)
	private float	mAltitudeTargetFeet; // e.g. 300 feet
	private float	mAltitudeDeviationFeet; // e.g. +/- 20 feet
	private float	mAltitudeDeltaNormalized;
	private boolean	mAltitudeDeviationClippped;
	
	// path
	private GPSDatum	mCurTansect;
	private float	mTransectPixelOffset; // up or down position (center)
	private float	mTransectTargetFeet; // e.g. 300 feet
	private float	mTransectDeviationFeet; // e.g. +/- 20 feet
	private float	mTransectDeltaNormalized;
	private boolean	mTransectDeviationClippped;
	
	// drawing
	private Paint mPaint;
	private RectF mOvalH;
	private RectF mOvalV;

	// for xml construction
	public TransectILSView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupVars();
	}

	// for code construction
	public TransectILSView(final Context context) {
		super(context);
		setupVars();
	}

	private void setupVars() {
		
		// TODO - externalize
		mAltitudeTargetFeet = 300;
		mAltitudeDeviationFeet = 20;
		
		// TODO - externalize
		mTransectTargetFeet = 0;
		mTransectDeviationFeet = 100;
		
		this.mPaint = new Paint();
		this.mOvalH = new RectF(0, 0, 0, 0);
		this.mOvalV = new RectF(0, 0, 0, 0);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// save w & h here if need be
	}

	@Override
	protected void onDraw(Canvas canvas) {

		float w = getWidth();
		float h = getWidth();
		float centerX = (float) w / 2;
		float centerY = (float) h / 2;
		float centerSize = w / 30;
		float markerStrokeWidth = 18;
		float hashSize = markerStrokeWidth * 2f;
		float hashW = hashSize / 4f;
		float hashFullW = hashW * 2f;
		float markerLen = w * 0.66f;
		float innerStrokeWidth = 3;
		float outerStrokeWidth = 6;
		float strokePad = outerStrokeWidth / 2;
		float adjW = w - strokePad;
		float adjH = h - strokePad;

		// TESTING Log.i("navView", "draw w = " + w + ", getWidth = " + ww +
		// ", mw = " + width + ", pl = " + pl);

		mPaint.setColor(getResources().getColor(R.color.nav_ips_axis));
		mPaint.setStyle(Paint.Style.STROKE);

		// axis
		mPaint.setStrokeWidth(innerStrokeWidth);
		canvas.drawLine(strokePad, centerY, adjW, centerY, mPaint);
		canvas.drawLine(centerX, strokePad, centerX, adjH, mPaint);

		// circles
		mPaint.setColor(getResources().getColor(R.color.nav_ips_circle));

		// inside circle
		mPaint.setStrokeWidth(innerStrokeWidth);
		canvas.drawCircle(centerX, centerY, centerSize, mPaint);

		// outside circle
		mPaint.setStrokeWidth(outerStrokeWidth);
		canvas.drawCircle(centerX, centerY, centerX - (strokePad * 2), mPaint);

		// horizontal notches
		mPaint.setColor(getResources().getColor(R.color.nav_ips_notches));
		mPaint.setStrokeWidth(1);
		mPaint.setStyle(Paint.Style.FILL);

		float xl = centerX;
		float xr = centerX;
		float yy0 = centerY - (hashSize / 2);
		float yy1 = centerY + (hashSize / 2);
		int numHashes = 6;
		float delta = centerX / numHashes;

		mOvalH.left = 0;
		mOvalH.right = hashFullW;
		mOvalH.top = yy0;
		mOvalH.bottom = yy1;

		for (int i = 0; i < numHashes - 1; i++) {
			xl -= delta;
			xr += delta;

			// left
			mOvalH.offsetTo(xl - hashW, yy0);
			canvas.drawOval(mOvalH, mPaint);

			// right
			mOvalH.offsetTo(xr - hashW, yy0);
			canvas.drawOval(mOvalH, mPaint);
		}

		// vertical notches

		mPaint.setColor(getResources().getColor(R.color.nav_ips_notches));
		mPaint.setStrokeWidth(1);
		mPaint.setStyle(Paint.Style.FILL);

		float y1 = centerY;
		float y2 = centerY;
		float vHashWidth = hashSize;
		float vHashHeight = hashSize / 2;
		float vHashHalfHeight = vHashHeight / 2;
		float vHashLeft = centerX - (vHashWidth / 2);

		mOvalV.left = 0;
		mOvalV.right = vHashWidth;
		mOvalV.top = 0;
		mOvalV.bottom = vHashHeight;

		for (int i = 0; i < numHashes - 1; i++) {
			y1 -= delta;
			y2 += delta;

			// top
			mOvalV.offsetTo(vHashLeft, y1 - vHashHalfHeight);
			canvas.drawOval(mOvalV, mPaint);

			// bottom
			mOvalV.offsetTo(vHashLeft, y2 - vHashHalfHeight);
			canvas.drawOval(mOvalV, mPaint);
		}

		mPaint.setStyle(Paint.Style.STROKE);

		// markers
		mPaint.setColor(getResources().getColor(R.color.nav_ips_guides));
		mPaint.setStrokeWidth(markerStrokeWidth);

		// vertical guide marker
		float pixelHDelta = (w/2.0f) * mTransectDeltaNormalized;
		float x = centerX + pixelHDelta;
		float verticalMarkerY1 = centerY - (markerLen / 2);
		float verticalMarkerY2 = centerY + (markerLen / 2);
		canvas.drawLine(x, verticalMarkerY1, x, verticalMarkerY2, mPaint);

		// horizontal guide marker
		float pixelVDelta = (h/2.0f) * mAltitudeDeltaNormalized;
		float yPos = centerY + pixelVDelta;
		float horizMarkerX1 = centerX - (markerLen / 2);
		float horizMarkerX2 = centerX + (markerLen / 2);
		canvas.drawLine(horizMarkerX1, yPos, horizMarkerX2, yPos, mPaint);
	}
	
	protected boolean updateAltitude(AltitudeDatum altitudeData) {
		
		float oldAtitudeDeltaNormalized = mAltitudeDeltaNormalized;
		// TODO - old visible
		
		if (altitudeData != null) {
			
			if (altitudeData.mDataIsValid) {
				
				// physical delta
				float altitudeInFeet = altitudeData.getAltitudeInFeet();
				float physicalDeltaFeet = altitudeInFeet - mAltitudeTargetFeet;
				
				// normalized delta
				mAltitudeDeltaNormalized = physicalDeltaFeet / mAltitudeDeviationFeet;
				
				// DEMO_MODE
				if (altitudeData.mDemoMode)
					mAltitudeDeltaNormalized = -0.3f;
				
				// validate
				if (mAltitudeDeltaNormalized > 1.0f) {
					mAltitudeDeltaNormalized = 1.0f;
					mAltitudeDeviationClippped = true;
				}
				else if (mAltitudeDeltaNormalized < -1.0f) {
					mAltitudeDeltaNormalized = -1.0f;
					mAltitudeDeviationClippped = true;
				}
			}
		}
		
		return mAltitudeDeltaNormalized != oldAtitudeDeltaNormalized;
	}
	
	protected boolean updateGps(GPSDatum gpsData) {
	
		float oldTransectDeltaNormalized = mTransectDeltaNormalized;
		// TODO - old visible
		
		if (gpsData != null) {
			
			if (gpsData.mDataIsValid) {
				
				// physical delta
				float pathDeviationInFeet = gpsData.getTransectDeltaInFeet();
				float physicalDeltaFeet = pathDeviationInFeet - mTransectTargetFeet;
				
				// normalized delta
				mTransectDeltaNormalized = physicalDeltaFeet / mTransectDeviationFeet;
				
				// DEMO_MODE
				if (gpsData.mDemoMode)
					mTransectDeltaNormalized = -0.4f;
				
				// validate
				if (mTransectDeltaNormalized > 1.0f) {
					mTransectDeltaNormalized = 1.0f;
					mTransectDeviationClippped = true;
				}
				else if (mTransectDeltaNormalized < -1.0f) {
					mTransectDeltaNormalized = -1.0f;
					mTransectDeviationClippped = true;
				}
			}
		}
		
		return mTransectDeltaNormalized != oldTransectDeltaNormalized;
	}
	
	public boolean update(AltitudeDatum atitudeData, GPSDatum gpsData) {
		boolean somethingChanged = false;
		
		somethingChanged |= updateAltitude(atitudeData);
		somethingChanged |= updateGps(gpsData);
		
		if (somethingChanged)
			invalidate();
		
		return somethingChanged;
	}
	
	public void reset() {
		// todo
	}
}
