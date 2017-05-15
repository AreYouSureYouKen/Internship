/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import com.serverConfig;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Luke
 */
@WebListener
@Named
public class CustomCodeBean implements ServletContextListener, Serializable {

    private HashMap<String, String> listeners = new HashMap<>();

    private static ServletContext sc;

    private boolean checkedListenerFolder = false;

    /**
     * Loads all the JavaScript files in the resources/listeners
     *
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        try
        {
            File listenerDir = new File(serverConfig.LISTENER_FOLDER);
            if (!listenerDir.mkdirs())
            {
                for (File f : listenerDir.listFiles())
                {
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    StringBuilder sb = new StringBuilder();
                    String curLine;
                    while ((curLine = br.readLine()) != null)
                    {
                        sb.append(curLine);
                        sb.append(System.lineSeparator());
                    }

                    String data = sb.toString();

                    String filename = f.getName();
                    listeners.put(filename.substring(0, filename.length() - 3), data);
                    sc.setAttribute("listeners", listeners);
                    br.close();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
      Gets all the listeners that have been loaded and startup and added afterwards.
     */
    public HashMap<String, String> getListeners() {
        if (listeners == null || listeners.isEmpty())
        {
            listeners = (HashMap<String, String>) sc.getAttribute("listeners");
        }
        return listeners;
    }
    
    public boolean ContainsListener(String name)
    {
        if (listeners == null || listeners.isEmpty())
        {
            listeners = (HashMap<String, String>) sc.getAttribute("listeners");
        }
        if(listeners == null) return false;
        return listeners.containsKey(name);
    }

    public String GetListener(String listener) {
        if (listeners == null || listeners.isEmpty())
        {
            listeners = (HashMap<String, String>) sc.getAttribute("listeners");
        }
        return listeners.get(listener);
    }

    public Set<String> GetKeySet() {
        if (listeners == null || listeners.isEmpty())
        {
            listeners = (HashMap<String, String>) sc.getAttribute("listeners");
        }
        return listeners.keySet();
    }

    public Set<Entry<String, String>> GetEntrySet() {
        if (listeners.isEmpty())
        {
            listeners = (HashMap<String, String>) sc.getAttribute("listeners");
        }
        return listeners.entrySet();
    }

    public void SaveListener(String name) {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String newname = request.getParameter(name + "newname");
        String content = getListeners().get(name);
        if (!name.equals(newname))
        {
            System.out.println("Name of listener has been changed, edit this to reflect.");
            listeners.remove(name);
            try
            {
                File f = new File(serverConfig.LISTENER_FOLDER + File.separator + name + ".js");
                System.out.println(f.getAbsolutePath());
                f.delete();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            name = newname;
            listeners.put(name, content);

        }

        if (!checkedListenerFolder)
        {
            File directory = new File(serverConfig.LISTENER_FOLDER);
            if (!directory.exists())
            {
                directory.mkdirs();
            }
            checkedListenerFolder = true;
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(serverConfig.LISTENER_FOLDER + "/" + name + ".js"), "utf-8")))
        {
            String[] lines = content.split("\r\n|\r|\n");
            for (String line : lines)
            {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Saving file on " + writer.toString());
        }
        catch (IOException ex)
        {
            Logger.getLogger(CustomCodeBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void AddListener(String name) {
        if (!name.equals(""))
        {
            if (getListeners() != null)
            {
                if (getListeners().get(name) == null)
                {
                    listeners.put(name, "");
                }
            }
            else
            {
                listeners = new HashMap<>();
                sc.setAttribute("listeners", listeners);
                listeners.put(name, "");
            }
        }

    }

    public void RemoveListener(String Name) {
        if (getListeners().get(Name) != null)
        {
            listeners.remove(Name);

            try
            {
                File f = new File(serverConfig.LISTENER_FOLDER + File.separator + Name + ".js");
                System.out.println(f.getAbsolutePath());
                f.delete();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Ignore this, it's just needed to be overriden because of weblistener.
    }

}
