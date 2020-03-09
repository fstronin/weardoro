package com.fstronin.weardoro.interval;

import com.fstronin.weardoro.AppException;

public class IntervalException extends AppException
{
    public IntervalException(String format, Object... args) {
        super(format, args);
    }
}
