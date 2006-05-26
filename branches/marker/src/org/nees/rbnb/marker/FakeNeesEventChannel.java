package org.nees.rbnb.marker;

import java.io.IOException;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Vector;

import javax.xml.transform.TransformerException;

/**
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author Terry E. Weymouth <weymouth@umich.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * @since 050916
 * Perforce RCS info:
 * $Id$
 * $Header: $
 * $Date$
 * $Change: $
 * $File: $
 * $Revision$
 * $Author$
 *
 * Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * All rights reserved. See full notice in the source, at the end of the file.
 * 
 */
public class FakeNeesEventChannel {

    private String server;
    private boolean connected;
    Vector listeners = new Vector();
    Vector eventQueue = new Vector();
    Thread postThread = null;
    
    FakeNeesEventChannel(String server)
    {
        this.server = server;
        System.out.println("FakeChannel: initialize with server = " + server);
    }
    
    public void reconnect()
    {
        if (connected) return;
        postThread = new Thread()
        {
            public void run(){
                exec();
                postThread = null;
            }
        };
        connected = true;
        postThread.start();
        System.out.println("FakeChannel: (re)connected with server = " + server);
    }
    
    public void disconnect()
    {
        if (!connected) return;
        connected = false;
        postThread.interrupt(); // just in case
        System.out.println("FakeChannel: disconnected with server = " + server);
    }
    
    public void postEvent(NeesEvent e)
    {
        String xml;
        try {
            xml = e.toEventXmlString();
            eventQueue.addElement(xml);
            System.out.println("FakeChannel: event added to queue");
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (TransformerException e1) {
            e1.printStackTrace();
        }
    }
    
    private void exec() // uesd by thread
    {
        while(connected)
        {
            if (connected && (eventQueue.size() > 0))
            {
                postToAllListeners((String)eventQueue.firstElement());
                eventQueue.removeElementAt(0);
            }
            try {Thread.sleep(2000);} catch (Exception Ignore){}
        }
    }

    private void postToAllListeners(String xml) {
        Enumeration e = listeners.elements();
        NeesEvent event = new NeesEvent();
        try {
            event.setFromEventXml(xml);
            while (e.hasMoreElements())
            {
                NeesEventListener l = (NeesEventListener)e.nextElement();
                l.performEventAction(event);
                System.out.println("FakeChannel: event posted to listener");
            }
        } catch (InvalidPropertiesFormatException e1) {
            e1.printStackTrace();
        } catch (TransformerException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
    public void addListener(NeesEventListener l)
    {
        listeners.addElement(l);
        System.out.println("FakeChannel: add listener");
    }
    
    public void removeListener(NeesEventListener l)
    {
        listeners.removeElement(l);
        System.out.println("FakeChannel: remove listener");
    }
    
    public void removeAllListeners()
    {
        listeners.removeAllElements();
        System.out.println("FakeChannel: ");
    }
}

/* Copyright Notice:
*
* Copyright (c) 2005, NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
*    * Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*    * Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimer in the 
* documentation and/or other materials provided with the distribution.
*    * Neither the name of the San Diego Supercomputer Center nor the names of
* its contributors may be used to endorse or promote products derived from this
* software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
