/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managers;

import beans.CustomCodeBean;
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
    private ServletContext sc;
    private HashMap<String, String> connections = new HashMap<>();
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine;

    @Inject
    ChallengeManager chalM;
    @Inject
    CustomCodeBean ccb;

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

    public void SendToUser(String username, ranjMessage message) throws IOException {
        if (getSessionByName(username) != null)
        {
            getSessionByName(username).getBasicRemote().sendText(GSON.toJson(message));
        }
        //TODO: Find way to implement searching user to find session and send message to this session.
    }

    @OnMessage
    public String OnMessage(String message, Session session) {
        ranjMessage returnMessage = new ranjMessage();
        LOG.log(Level.INFO, "New message from Client [{0}]: {1}",
                new Object[]
                {
                    session.getId(), message
                });

        returnMessage = relayIncoming(returnMessage, message, session);

        return GSON.toJson(returnMessage);
    }

    /**
     * Relays the message class to the correct listeners and code.
     *
     * @param returnMessage
     * @param message
     * @param session
     * @return A ranjMessage that can be sent back to the incoming request.
     */
    private ranjMessage relayIncoming(ranjMessage returnMessage, String message, Session session) {
        try
        {
            ranjMessage rm = GSON.fromJson(message, ranjMessage.class);
            ranj r = new ranj(chalM, session.getId(), rm);
            switch (rm.getMessageClass())
            {
                // When a LoginMessage is fired, has little to no functionality other than adding the username and session ID to the connection map.
                case "LoginMessage":
                    RanjUser ru = GSON.fromJson(rm.getMessageBody(), RanjUser.class);
                    LOG.log(Level.INFO, "Login request found with user {0}", ru.getUsername());

                    // For when custom code is found for this message, data put in scriptdata and scripterror on the Ranj class gets put in the return message.
                    if (ccb.ContainsListener(rm.getMessageClass()))
                    {
                        engine = manager.getEngineByName("nashorn");
                        engine.put("Ranj", r);
                        engine.eval(ccb.getListeners().get(rm.getMessageClass()));
                        returnMessage.setMessageClass(rm.getMessageClass());
                        // Wrap up the message, send added messages and fill the return message.
                        returnMessage = wrapUp(returnMessage, r);
                    }
                    else
                    {
                        returnMessage.setMessageClass(rm.getMessageClass());
                        returnMessage.setMessageBody("Logged in.");
                    }

                    connections.put(session.getId(), ru.getUsername());
                    addSessionByName(ru.getUsername(), session);
                    System.out.println(getUserSessions().toString());
                    break;
                case "IssueChallenge":
                    // For when custom code is found for this message, data put in scriptdata and scripterror on the Ranj class gets put in the return message.
                    if (ccb.ContainsListener(rm.getMessageClass()))
                    {
                        engine = manager.getEngineByName("nashorn");
                        engine.put("Ranj", r);
                        engine.eval(ccb.getListeners().get(rm.getMessageClass()));

                        returnMessage.setMessageClass(rm.getMessageClass());
                        // Wrap up the message, send added messages and fill the return message.
                        returnMessage = wrapUp(returnMessage, r);

                    }
                    else
                    {
                        // No custom code found for issuing challenge so defaulting to automatic accepting of challenge on logged in player.
                        if (connections.get(session.getId()) != null)
                        {
                            ranjChallenge rc = r.IssueChallenge(connections.get(session.getId()));
                            returnMessage.setMessageClass(rm.getMessageClass());
                            returnMessage.setMessageBody(GSON.toJson(rc));
                        }
                        else
                        {
                            returnMessage.setMessageClass("NotLoggedIn");
                            returnMessage.setMessageBody("Not-logged in user tried to issue a challenge");
                            returnMessage.setMessageError("Unauthenticated connection");
                        }

                    }

                    break;
                default:
                    returnMessage = checkListeners(rm, r, returnMessage);
                    break;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            returnMessage.setMessageClass("ServerError");
            returnMessage.setMessageBody("Unknown error caught when handling [" + message + "]");
            returnMessage.setMessageError("Error caught when handling message");
        }
        finally
        {
            return returnMessage;
        }
    }

    /**
     * Checks the listeners for if there is any that are not a known message
     * class.
     *
     * @param rm
     * @param r
     * @param returnMessage
     * @param message
     * @param session
     * @return A ranjMessage that can be returned to the incoming request.
     */
    private ranjMessage checkListeners(ranjMessage rm, ranj r, ranjMessage returnMessage) throws IOException, ScriptException {
        if (ccb.ContainsListener(rm.getMessageClass()))
        {
            engine = manager.getEngineByName("nashorn");
            engine.put("Ranj", r);
            engine.eval(ccb.getListeners().get(rm.getMessageClass()));
            returnMessage.setMessageClass(rm.getMessageClass());
            // Wrap up the message, send added messages and fill the return message.
            returnMessage = wrapUp(returnMessage, r);
        }
        else
        {
            returnMessage.setMessageClass("NoCustomListener");
            returnMessage.setMessageBody("No custom event found for this message");
            returnMessage.setMessageError("No custom event found for this message");
        }

        return returnMessage;
    }

    @OnClose
    public void OnClose(Session session) {
        LOG.log(Level.INFO, "Close connection for client: {0}",
                session.getId());
        connections.remove(session.getId());
    }

    @OnError
    public void OnError(Throwable exception, Session session) {
        LOG.log(Level.INFO, "Error for client: {0}", session.getId());
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        chalM.GracefulShutdown();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        sc.setAttribute("userSessions", new HashMap<String, Session>());
        // File loading is being done in CustomCodeBean so contextInitialized is being ignored here. Only need one central place to gracefully shut down the connections and save data.
    }

    /**
     * *
     *
     * @param returnMessage the semi-filled ranjMessage to be returned
     * @param r the ranj object connected to the current request being handled
     * @return the filled in ranjMessage to be returned to the request
     */
    private ranjMessage wrapUp(ranjMessage returnMessage, ranj r) throws IOException {
        if (!r.GetAllScriptData().isEmpty())
        {
            returnMessage.setMessageBody(GSON.toJson(r.GetAllScriptData()));
        }
        else
        {
            returnMessage.setMessageError("No data for challenge found. ");
        }

        if (!r.getScriptError().isEmpty())
        {
            returnMessage.setMessageError(r.getScriptError());
        }

        if (!r.getMessages().isEmpty())
        {
            System.out.println("found messages to send");
            for (Map.Entry<String, ranjMessage> entry : r.getMessages().entrySet())
            {
                SendToUser(entry.getKey(), entry.getValue());
            }
        }
        return returnMessage;
    }

    private Session getSessionByName(String name) {
        return ((HashMap<String, Session>) sc.getAttribute("userSessions")).get(name);
    }

    private HashMap<String, Session> getUserSessions() {
        return (HashMap<String, Session>) sc.getAttribute("userSessions");
    }

    private void addSessionByName(String name, Session session) {
        if ((HashMap<String, Session>) sc.getAttribute("userSessions") == null)
        {
            HashMap<String, Session> userS = new HashMap<>();
            userS.put(name, session);
            sc.setAttribute("userSessions", userS);
        }
        else
        {
            ((HashMap<String, Session>) sc.getAttribute("userSessions")).put(name, session);
        }

    }

}
