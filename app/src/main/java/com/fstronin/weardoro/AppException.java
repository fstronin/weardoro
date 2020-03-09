package com.fstronin.weardoro;

public class AppException extends Exception
{
    public AppException(String format, Object... args)
    {
        String msg = String.format(
                App.getLocale(),
                format,
                args
        );
    }
}
