/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managers;

import java.util.HashMap;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;
import models.ranjChallenge;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.Query;

/**
 *
 * @author Luke
 */
@Named
@Stateless
public class ChallengeManager {

    @EJB
    MongoManager mongoClientProvider;

    AdvancedDatastore challengeData;

    public ranjChallenge IssueChallenge(String username) {
        CheckDBCon();
        ranjChallenge chal = null;
        Query<ranjChallenge> query = challengeData.createQuery(ranjChallenge.class).field("status").equalIgnoreCase("Issued");
        List<ranjChallenge> challenges = query.asList();

        if (!challenges.isEmpty())
        {
            chal = challenges.get(0);
            if (chal.GetChallengerID().equals(username))
            {
                chal = new ranjChallenge(username);
            }
            else
            {
                chal.AddChallengedPlayer(username);
                chal.StartChallenge();
            }
        }
        else
        {
            chal = new ranjChallenge(username);
        }
        challengeData.save(chal);


        return chal;
    }

    public ranjChallenge FindChallengeByID(String challengeID) {
        CheckDBCon();
        return challengeData.createQuery(ranjChallenge.class).field("challengeID").equalIgnoreCase(challengeID).get();
    }
    
    public List<ranjChallenge> FindChallengesByUser(String username)
    {
        CheckDBCon();
        return challengeData.createQuery(ranjChallenge.class).field("playerIDs").hasThisOne(username).asList();
    }
    
    public AdvancedDatastore GetDataStore()
    {
        return challengeData;
    }

    /**
     * Returns the first challenge that fits the filters. Filters are built up
     * of the parameter + operand and the value, e.g: {"ELO <=", 100}
     * @param filters
     * @return the first ranjChallenge containing the specified filters
     */
    public List<ranjChallenge> FindChallengeWithFilter(HashMap<String, Object> filters) {
        CheckDBCon();
        Query q = challengeData.createQuery(ranjChallenge.class);
        Set<Entry<String, Object>> entries = filters.entrySet();
        for (Entry<String, Object> e : entries)
        {
            q.filter(e.getKey(), e.getValue());
        }

        return (List<ranjChallenge>) q.asList();
    }

    private void CheckDBCon() {
        if (challengeData == null)
        {
            challengeData = mongoClientProvider.GetOrCreateDatastore("Challenges");
        }
    }

    public void GracefulShutdown() {
        if (mongoClientProvider.getMongoClient() != null)
        {
            mongoClientProvider.getMongoClient().close();
        }
    }
}
