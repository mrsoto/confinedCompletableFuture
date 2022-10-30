package me.async;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyLogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return
                record.getInstant()+"::"
                +record.getLongThreadID()+"::"+record.getSourceClassName()+"::"
                +record.getSourceMethodName()+"::"
                +record.getMessage()+System.lineSeparator();
    }

}