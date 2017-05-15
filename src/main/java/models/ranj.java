/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import managers.ChallengeManager;
import java.util.HashMap;

/**
 *
 * @author Luke
 */
public class ranj {

    private String senderID;

    ChallengeManager chalManager;
    ranjMessage message;
    private HashMap<String, Object> scriptData;
    private HashMap<String, ranjMessage> messages;
    private String scriptError ="";

    public ranj(ChallengeManager chalManager) {
        this.chalManager = chalManager;
        scriptData = new HashMap<>();
        messages = new HashMap<>();
    }

    public ranj(ChallengeManager chalManager, String senderID, ranjMessage rm) {
        this.chalManager = chalManager;
        this.senderID = senderID;
        this.message = rm;
        scriptData = new HashMap<>();
        messages = new HashMap<>();
    }
    

    public RanjUser getUser() {
        return null;
    }

    public RanjUser loadUser(String playerID) {
        return null;
    }

    public ranjMessage GetData() {
        return message;

    }

    public String GetSender() {
        return senderID;
    }

    public ranjChallenge GetChallenge(String challengeId) {
        return chalManager.findChallengeByID(challengeId);
    }

    public ranjChallenge IssueChallenge(String playerID) {
        return chalManager.IssueChallenge(playerID);
    }

    public void SetScriptData(String name, Object value) {
        this.scriptData.put(name, value);
    }

    public void RemoveScriptData(String name) {
        this.scriptData.remove(name);
    }
    
    public Object GetScriptData(String name)
    {
        return this.scriptData.get(name);
    }
    
    public HashMap<String,Object> GetAllScriptData()
    {
        return this.scriptData;
    }

    /**
     * @return the scriptError
     */
    public String getScriptError() {
        return scriptError;
    }

    /**
     * @param scriptError the scriptError to set
     */
    public void setScriptError(String scriptError) {
        this.scriptError = scriptError;
    }

    /**
     * @return the messages
     */
    public HashMap<String, ranjMessage> getMessages() {
        return messages;
    }

    /**
     * @param username the username this will be sent to
     * @param message the message that will be sent to user
     */
    public void sendMessage(String username, ranjMessage message) {
        this.messages.put(username, message);
    }
}
