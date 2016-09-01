package org.gtri.fhir.api.vistaex.rest.service.servlet;

import org.gtri.fhir.api.vistaex.rest.service.util.VistaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * Created by es130 on 9/1/2016.
 */
public class SessionRefreshTimer extends TimerTask {

    private final static Logger logger = LoggerFactory.getLogger(SessionRefreshTimer.class);

    public SessionRefreshTimer(){
    }

    @Override
    public void run() {
        logger.debug("Refreshing session");
        VistaUtil.getVistaExResource().refreshSessionOnVistaEx();
        logger.debug("Finished Refreshing Session");
    }
}
