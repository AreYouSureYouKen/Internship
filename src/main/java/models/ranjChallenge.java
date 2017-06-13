/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.persistence.PrePersist;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import utility.Exclude;

/**
 *
 * @author Luke
 */
@Entity
public class ranjChallenge implements Serializable {

    @Id
    private final long id;
    private String status;
    private final String shortCode;
    private List<String> playerIDs;
    private String challengerID;
    private String playerTurn;
    private String winnerID;
    private HashMap<String, Object> scriptData;
    @Exclude
    private HashMap<String, Object> privateData;
    private Date lastUpdated;

    public ranjChallenge() {
        this.id = new Random().nextInt(999999999);
        this.shortCode = "" + id;
        this.status = "Issued";
        this.lastUpdated = new Date();
    }

    public ranjChallenge(String username) {
        this.id = new Random().nextInt(999999999);
        this.shortCode = "" + id;
        this.status = "Issued";
        this.playerIDs = new ArrayList<>();
        this.playerIDs.add(username);
        this.challengerID = username;
        this.playerTurn = username;
        this.lastUpdated = new Date();
    }

    public String GetRunState() {
        return status;
    }

    public long GetId() {
        return id;
    }

    public String GetShortCode() {
        return shortCode;
    }

    public void WinChallenge(String playerID) {
        this.winnerID = playerID;
        this.status = "Completed";
    }
    
    public String GetWinner()
    {
        return winnerID;
    }

    public void DrawChallenge() {
        this.status = "Draw";
    }

    public void StartChallenge() {
        this.status = "Running";
    }

    public List<String> GetPlayerIDs() {
        return playerIDs;
    }

    public void AddChallengedPlayer(String playerID) {
        playerIDs.add(playerID);
    }

    public String GetChallengerID() {
        return challengerID;
    }

    public boolean TakeTurn(String playerID) {
        if(playerTurn.equals(playerID))
        {
            int index = playerIDs.indexOf(playerID);
            if(index >= playerIDs.size()) index = 0; else index++;
            playerTurn = playerIDs.get(index);
            return true;
        }
        return false;
    }

    public Object GetPrivateData(String name) {
        return privateData.get(name);
    }

    public void SetPrivateData(String name, Object value) {
        privateData.put(name, value);
    }

    public void RemovePrivateData(String name) {
        privateData.remove(name);
    }

    public Object GetScriptData(String name) {
        return scriptData.get(name);
    }

    public void SetScriptData(String name, String value) {
        scriptData.put(name, value);
    }

    public void RemoveScriptData(String name) {
        privateData.remove(name);
    }

    @PrePersist
    private void prePersist() {
        this.lastUpdated = new Date();
    }
}
