/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managers;

import beans.CustomCodeBean;
import beans.ListenerBean;
import com.google.gson.Gson;
import models.ranj;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import models.RanjUser;
import models.ranjChallenge;
import models.ranjMessage;

/**
 *
 * @author Luke
 */
@WebListener
@ServerEndpoint(value = "/ws")
public class SocketManager implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(SocketManager.class.getName());
    private static final Gson GSON = new Gson();

    @Inject
    ListenerBean lb;
    @Inject
    ChallengeManager chalM;

    @OnOpen
    public void OnOpen(Session session) {
        LOG.log(Level.INFO, "New connection with client: {0}", session.getId());
        sendOnOpen(session);
    }

    private void sendOnOpen(Session session) {
        try
        {
            session.getBasicRemote().sendText(GSON.toJson(new ranjMessage("ConnectedMessage", "You are now connected to the server")));
        }
        catch (IOException ex)
        {
            Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @OnMessage
    public String OnMessage(String message, Session session) {
        ranjMessage returnMessage = new ranjMessage();
        LOG.log(Level.INFO, "New message from Client [{0}]: {1}",
                new Object[]
                {
                    session.getId(), message
                });

        returnMessage = lb.relayIncoming(returnMessage, message, session);

        return GSON.toJson(returnMessage);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        chalM.GracefulShutdown();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // File loading is being done in CustomCodeBean so contextInitialized is being ignored here. Only need one central place to gracefully shut down the connections and save data.
    }

}
