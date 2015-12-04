package io.github.gallyamow.tracking;

import java.util.Date;

public interface TrackingCoordinate {
	int getTracker();

	Date getDatetime();

	double getLon();

	double getLat();

	float getBearing();

	float getSpeed();
}