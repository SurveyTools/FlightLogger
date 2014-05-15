package com.vulcan.flightlogger;

import android.view.View;

import com.vulcan.flightlogger.AltitudeDatum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.graphics.RectF;
import android.graphics.Path;
import android.graphics.Path.Direction;
// import android.graphics.Color;
// import android.util.Log;

public class TransectILSView extends View {

	// altitude
	private AltitudeDatum mCurAltitude;
	private float mAltitudeTargetFeet; // e.g. 300 feet
	private float mAltitudeDialRadiusFeet; // e.g. +/- 20 feet
	private float mAltitudeDeltaNormalized;

	// path
	private GPSDatum mCurTansect;
	private float mTransectTargetFeet; // e.g. 300 feet
	private float mTransectDialRadiusFeet; // e.g. +/- 20 feet
	private float mTransectDeltaNormalized;

	// colors
	private int mMarkerColorNormal;
	private int mMarkerColorWarning;
	private int mMarkerColorError;
	
	// drawing
	private Paint mPaint;
	private RectF mOvalH;
	private RectF mOvalV;
	private Path mCircleClip;
	private float mCircleClipRadius;
	private float mCircleClipX;
	private float mCircleClipY;

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
		mAltitudeDialRadiusFeet = 40; // superdevo

		// TODO - externalize
		mTransectTargetFeet = 0;
		mTransectDialRadiusFeet = 100;

		mCircleClipRadius = 0;
		mCircleClipX = 0;
		mCircleClipY = 0;
		this.mCircleClip = new Path();

		this.mPaint = new Paint();
		this.mOvalH = new RectF(0, 0, 0, 0);
		this.mOvalV = new RectF(0, 0, 0, 0);

		mMarkerColorNormal = getResources().getColor(R.color.nav_ips_guides);
		mMarkerColorWarning = getResources().getColor(R.color.nav_ips_guides_warning);
		mMarkerColorError = getResources().getColor(R.color.nav_ips_guides_warning);
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
		float pixelRadius = w / 2.0f;
		float contentPixelRadius = pixelRadius - outerStrokeWidth;
		float errorPixelRadius = contentPixelRadius - markerStrokeWidth; // 15 is perfect, 18 gives you a little gap which is good
		float warningPixelRadius = errorPixelRadius * .85f; // 15 is perfect, 18 gives you a little gap which is good

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

		// set up the circular clipping
		// note that this is not supported in hardware acceleraiton

		// set up the clip (if need be)
		float clipRadius = contentPixelRadius - 4;
		if ((centerX != mCircleClipX) || (centerY != mCircleClipY) || (mCircleClipRadius != clipRadius)) {
			// update the clip
			mCircleClipRadius = clipRadius;
			mCircleClipX = centerX;
			mCircleClipY = centerY;
			mCircleClip.reset();
			mCircleClip.addCircle(mCircleClipX, mCircleClipY, mCircleClipRadius, Direction.CW);
		}

		// clip
		int canvasStateRef = canvas.save();
		canvas.clipPath(mCircleClip);

		// vertical guide marker |
		float pixelHDelta = pixelRadius * mTransectDeltaNormalized;

		int markerColor = mMarkerColorNormal;
		
		// validate
		if (pixelHDelta < -errorPixelRadius) {
			// pegged
			pixelHDelta = -errorPixelRadius;
			markerColor = mMarkerColorError;
		} else if (pixelHDelta < -warningPixelRadius) {
			// warning
			markerColor = mMarkerColorWarning;
		} else if (pixelHDelta > errorPixelRadius) {
			// pegged
			pixelHDelta = errorPixelRadius;
			markerColor = mMarkerColorError;
		} else if (pixelHDelta > warningPixelRadius) {
			// warning
			markerColor = mMarkerColorWarning;
		}

		float x = centerX + pixelHDelta;
		float verticalMarkerY1 = centerY - (markerLen / 2);
		float verticalMarkerY2 = centerY + (markerLen / 2);

		mPaint.setColor(markerColor);
		canvas.drawLine(x, verticalMarkerY1, x, verticalMarkerY2, mPaint);

		// horizontal guide marker --
		float pixelVDelta = pixelRadius * mAltitudeDeltaNormalized;
		
		markerColor = mMarkerColorNormal;
		
		// validate
		if (pixelVDelta < -errorPixelRadius) {
			// pegged
			pixelVDelta = -errorPixelRadius;
			markerColor = mMarkerColorError;
		} else if (pixelVDelta < -warningPixelRadius) {
			// warning
			markerColor = mMarkerColorWarning;
		} else if (pixelVDelta > errorPixelRadius) {
			// pegged
			pixelVDelta = errorPixelRadius;
			markerColor = mMarkerColorError;
		} else if (pixelVDelta > warningPixelRadius) {
			// warning
			markerColor = mMarkerColorWarning;
		}

		float yPos = centerY + pixelVDelta;
		float horizMarkerX1 = centerX - (markerLen / 2);
		float horizMarkerX2 = centerX + (markerLen / 2);

		mPaint.setColor(markerColor);
		canvas.drawLine(horizMarkerX1, yPos, horizMarkerX2, yPos, mPaint);

		/*
		 * TESTING the clip: RectF bigRect = new RectF(-10, -10, w + 20, h +
		 * 20); mPaint.setStyle(Paint.Style.FILL); canvas.drawRect(bigRect,
		 * mPaint);
		 */

		// restore
		canvas.restoreToCount(canvasStateRef);
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
				mAltitudeDeltaNormalized = physicalDeltaFeet / mAltitudeDialRadiusFeet;

				// DEMO_MODE
				if (altitudeData.mDemoMode)
					mAltitudeDeltaNormalized = -0.3f;
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
				mTransectDeltaNormalized = physicalDeltaFeet / mTransectDialRadiusFeet;

				// DEMO_MODE
				if (gpsData.mDemoMode)
					mTransectDeltaNormalized = -0.4f;
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
