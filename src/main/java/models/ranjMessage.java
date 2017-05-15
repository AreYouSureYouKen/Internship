/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.io.Serializable;

/**
 *
 * @author Luke
 */
public class ranjMessage implements Serializable{
    private String messageClass;
    private String messageBody;
    private String messageError;
    
    public ranjMessage()
    {
        
    }
    
    public ranjMessage(String messageClass, String messageBody)
    {
        this.messageClass = messageClass;
        this.messageBody = messageBody;
        this.messageError = "";
    }
    
    public ranjMessage(String messageClass, String messageBody, String messageError)
    {
        this.messageClass = messageClass;
        this.messageBody = messageBody;
        this.messageError = messageError;
    }

    /**
     * @return the messageClass
     */
    public String getMessageClass() {
        return messageClass;
    }

    /**
     * @param messageClass the messageClass to set
     */
    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }

    /**
     * @return the messageBody
     */
    public String getMessageBody() {
        return messageBody;
    }

    /**
     * @param messageBody the messageBody to set
     */
    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    /**
     * @return the messageError
     */
    public String getMessageError() {
        return messageError;
    }

    /**
     * @param messageError the messageError to set
     */
    public void setMessageError(String messageError) {
        this.messageError = messageError;
    }
}
