/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managers;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.Session;

/**
 *
 * @author Luke
 */
@ClientEndpoint
public class TestHarnessWSManager {
    
    @OnClose
    public void close(Session session)
    {
        
    }
    
    @OnError
    public void error(Throwable error)
    {
        error.printStackTrace();
    }
}
