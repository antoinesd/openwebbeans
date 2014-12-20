/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.se.sample;

import org.apache.webbeans.se.sample.gui.LoginWindow;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.swing.*;

public class Boot
{
    
    private static JFrame frame = null;
    
    
    
    public static void main(String[] args) throws Exception
    {
     
        frame = new JFrame();
        
        BeanManager beanManager = CDI.current().getBeanManager();
        Bean<?> bean = beanManager.getBeans("loginWindow").iterator().next();

        LoginWindow loginWindow = (LoginWindow) beanManager.getReference(bean, LoginWindow.class, beanManager
                .createCreationalContext(bean));
        
        frame.setTitle("OWB @ Java-SE");
        frame.add(loginWindow,BorderLayout.CENTER);        
        frame.setLocation(400, 300);        
        frame.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosed(WindowEvent e)
            {
                try
                {
                    //Boot.shutdown(e);
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }            
            
        });
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        
    }
    
    public static JFrame getFrame()
    {
        return frame;
    }
}
