package de.tum.bgu.msm.util.js;

import org.apache.log4j.Logger;

import javax.script.SimpleBindings;
import java.util.concurrent.ConcurrentHashMap;

public class LoggableBindings extends SimpleBindings {

    public static final Logger logger = Logger.getLogger(LoggableBindings.class);

    public LoggableBindings() {
        super(new ConcurrentHashMap<>());
    }

    public void logValues() {
        StringBuilder builder = new StringBuilder("Bound values: \n");
        for(Entry<String, Object> entry: this.entrySet()) {
            builder.append(entry.getKey() + " = " + entry.getValue() + "\n");
        }
        logger.debug(builder.toString());
    }
}
