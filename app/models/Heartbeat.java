package models;

import constants.Config;
import controllers.APIHelpers;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

/**
 * Heartbeat
 * <p>Used to indicate availability and uptime. Configured to run every 5 minutes. A heartbeat is a simple
 * mechanism to register that a service is up and running and ready to receive calls. If there is no heartbeat,
 * it can be assumed that the service is offline.</p>
 */
@Every("5mn")
public class Heartbeat extends Job {
    private static final String KEY_NAME = "heartbeat:" + APIHelpers.getPublicIP() + ":" + Config.APPLICATION_NAME;

    // heartbeat counter - gets incremented every 5 minutes
    private static long heartbeat = 0;

    static {

    }

    /**
     * Create heartbeat in cache and update it every 5 minutes.
     * Stores the application url and current datetime in cache.
     */
    @Override
    public void doJob() {
        Logger.trace("Heartbeat #%d", ++heartbeat);
    }

    /**
     * Shutdown heartbeat - usually called when service is gracefully shutting down
     */
    public static void shutdown() {
        Logger.info("Stopping Heartbeat@%s", StringUtils.replace(KEY_NAME, "_", ":"));
        Logger.info("Stopped Heartbeat@%s", StringUtils.replace(KEY_NAME, "_", ":"));
    }
}
