/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import com.google.gson.Gson;
import com.serverConfig;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.websocket.ContainerProvider;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import managers.TestHarnessWSManager;
import models.ranjMessage;
import org.omnifaces.cdi.ViewScoped;

/**
 *
 * @author Luke
 */
@Named
@ViewScoped
public class TestHarnessBean implements Serializable {

    private Session session = null;
    
    private ranjMessage returnMessage;
    private long milis;
    private long startTime;
    private Gson gson = new Gson();
    
    
    @PostConstruct
    public void init() 
    {
        WebSocketContainer wsc = null;
        try
        {
            wsc = ContainerProvider.getWebSocketContainer();
            startTime = System.currentTimeMillis();
            session = wsc.connectToServer(TestHarnessWSManager.class, new URI(serverConfig.PLATFORM_URL));
            
            session.addMessageHandler(new MessageHandler.Whole<String>() {
            
                @Override
                public void onMessage(String msg) 
                {
                    messageReceived(msg);
                }
            
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @PreDestroy()
    public void destroy()
    {
        close();
    }

    
    private void messageReceived(String msg)
    {
        milis = System.currentTimeMillis() - startTime;
        setReturnMessage(msg);
        System.out.println(msg);
        // TODO: Update view bean here somehow to reflect the message received.
    }
    
    

    private void close()
    {
        try {
            session.close();
            System.out.println("CLOSED Connection to WS endpoint " + serverConfig.PLATFORM_URL);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void SendMessage(String classname, String message)
    {
        ranjMessage rm = new ranjMessage(classname, message);
        
        try
        {
            startTime = System.currentTimeMillis();
            session.getBasicRemote().sendText(gson.toJson(rm));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")

    /**
     * @return the returnMessage
     */
    public ranjMessage getReturnMessage() {
        return returnMessage;
    }

    /**
     * @param returnMessage the returnMessage to set
     */
    public void setReturnMessage(String rm) {
        this.returnMessage = gson.fromJson(rm, ranjMessage.class);
    }
    
    public long getMilis()
    {
        return this.milis;
    }
    
    public void SetMilis(long m)
    {
        milis = m;
    }
}
