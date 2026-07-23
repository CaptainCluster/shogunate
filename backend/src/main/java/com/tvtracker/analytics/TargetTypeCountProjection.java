package com.tvtracker.analytics;

import com.tvtracker.common.TargetType;

public interface TargetTypeCountProjection {

    TargetType getTargetType();

    long getCount();
}
