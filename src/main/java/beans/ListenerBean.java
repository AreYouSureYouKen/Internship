/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.websocket.Session;
import managers.ChallengeManager;
import managers.SocketManager;
import models.RanjUser;
import models.ranj;
import models.ranjChallenge;
import models.ranjMessage;
import utility.AnnotationExclusionStrategy;


/**
 *
 * @author Luke
 */
@ApplicationScoped
public class ListenerBean {
private static final Logger LOG = Logger.getLogger(SocketManager.class.getName());
    private static final Gson GSON = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy()).create();
    private final HashMap<String, String> connections = new HashMap<>();
    private final HashMap<String, Session> userSessions = new HashMap<>();
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine;

    @Inject
    ChallengeManager chalM;
    @Inject
    CustomCodeBean ccb;

    public void SendToUser(String username, ranjMessage message) throws IOException {
        if (userSessions.containsKey(username))
        {
            userSessions.get(username).getBasicRemote().sendText(GSON.toJson(message));
        }
        //TODO: Find way to implement searching user to find session and send message to this session.
    }
    
    public ranjMessage relayIncoming(ranjMessage returnMessage, String message, Session session) {
        try
        {
            ranjMessage rm = GSON.fromJson(message, ranjMessage.class);
            ranj r = new ranj(chalM, session.getId(), rm);
            switch (rm.getMessageClass())
            {
                // When a LoginMessage is fired, has little to no functionality other than adding the username and session ID to the connection map.
                case "LoginMessage":
                    RanjUser ru = GSON.fromJson(rm.getMessageBody().toString(), RanjUser.class);
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
                    userSessions.put(ru.getUsername(), session);
                    System.out.println(userSessions.toString());
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
                case "ListChallenges":
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
                            List<ranjChallenge> rcs = r.GetChallengesFromuser(connections.get(session.getId()));
                            returnMessage.setMessageClass(rm.getMessageClass());
                            returnMessage.setMessageBody(rcs);
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
            returnMessage.setMessageBody(r.GetAllScriptData());
        }
        else
        {
            returnMessage.setMessageError("No data to be sent is found");
        }

        if (!r.GetScriptError().isEmpty())
        {
            returnMessage.setMessageError(r.GetScriptError());
        }

        if (!r.GetMessages().isEmpty())
        {
            System.out.println("found messages to send");
            for (Map.Entry<String, ranjMessage> entry : r.GetMessages().entrySet())
            {
                SendToUser(entry.getKey(), entry.getValue());
            }
        }
        return returnMessage;
    }

}
