/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.File;

/**
 *
 * @author Luke
 */
public final class serverConfig {

    /**
     * Platform Websocket URL to be used in ClientEndPoint websocket for Test harness.
     */
    public final static String PLATFORM_URL = "ws://localhost:8080/RanjSparks/ws";
            
    /**
     * Folder name of this platform within the server.
     */
    public final static String PLATFORM_NAME = "RanjSparks";
    
    /**
     * Folder name where resources are located (listeners and models) preceded by the platform name.
     */
    public final static String RESOURCE_FOLDER = PLATFORM_NAME+File.separator+"Resources";

    /**
     * Folder name where the listeners are located, preceded by resource folder.
     */
    public final static String LISTENER_FOLDER = RESOURCE_FOLDER+File.separator+"Listeners";

    /**
     * Folder name where the models are located, preceded by resource folder.
     */
    public final static String MODEL_FOLDER = RESOURCE_FOLDER+File.separator+"Models";
}
