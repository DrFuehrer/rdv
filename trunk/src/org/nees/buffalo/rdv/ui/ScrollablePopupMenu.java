/*
 * RDV
 * Real-time Data Viewer
 * http://nees.buffalo.edu/software/RDV/
 * 
 * Copyright (c) 2005 University at Buffalo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.nees.buffalo.rdv.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.*;
 
import javax.swing.*;
import javax.swing.Timer;

import org.nees.buffalo.rdv.DataViewer;
 
 
/**
 * JMenu with the scrolling feature.
 * 
 * This is a modified version of the ScrollableMenu class found on the
 * <a href="http://forum.java.sun.com/thread.jspa?forumID=57&threadID=454021">
 * Java Technology Forums</a> in a post by colada.
 */
public class ScrollablePopupMenu extends JPopupMenu
{
    /** How fast the scrolling will happen. */
    private int scrollSpeed = 100;
    /** Handles the scrolling upwards. */
    private Timer timerUp;
    /** Handles the scrolling downwards. */
    private Timer timerDown;
    /** How many items are visible. */
    private int visibleItems;
    /** Menuitem's index which is used to control if
     *  up and downbutton are visible or not.  */
    private int indexVisible = 0;
    /** Button to scroll menu upwards. */
    private JMenuItem upButton;
    /** Button to scroll menu downwards. */
    private JMenuItem downButton;
    /** Container to hold submenus. */
    private Vector subMenus = new Vector(  );
    /** Height of the screen. */
    private double screenHeight;
    /** Height of the menu. */
    private double menuHeight;
 
    /**
     * Creates a new ScrollableMenu object.
     * <p>This also instantiates the timers and buttons. After the buttons
     * are created they are set invisible.
     */
    public ScrollablePopupMenu()
    {
        timerUp =
            new Timer( scrollSpeed,
                       new ActionListener(  )
                {
                    public void actionPerformed( ActionEvent evt )
                    {
                        scrollUp(  );
                    }
                } );
        timerDown =
            new Timer( scrollSpeed,
                       new ActionListener(  )
                {
                    public void actionPerformed( ActionEvent evt )
                    {
                        scrollDown(  );
                    }
                } );
 
        Dimension screenSize = Toolkit.getDefaultToolkit(  ).getScreenSize(  );
        screenHeight = screenSize.getHeight(  ) - 30; //room for toolbar
        if (screenHeight > 600) screenHeight = 600;
 
        createButtons(  );
        hideButtons(  );
    }
 
    /**
     * Add a menu item to the popup menu.
     * 
     * JPopupMenu's add-method is override to keep track of the added items
     * unless they are the up and down buttons.
     *
     * @param menuItem to be added
     *
     * @return added menuitem
     */
    public JMenuItem add( JMenuItem menuItem )
    {
      if (menuItem == downButton || menuItem == upButton) {
        super.add(menuItem);
      } else {
        add((Component)menuItem);
      }
      return menuItem;
    }

    /**
     * JMenu's add-method is override to keep track of the added items.
     * If there are more items that JMenu can display, then the added menuitems
     * will be invisible.
     *
     * @param menuItem to be added
     *
     * @return added menuitem
     */    
    public Component add( Component menuItem ) {      
        add( menuItem, subMenus.size(  ) + 1 );
        subMenus.add( menuItem );
 
        menuHeight += menuItem.getPreferredSize(  ).getHeight(  );
        
        if( menuHeight > screenHeight )
        {
            menuItem.setVisible( false );
            downButton.setVisible( true );
        }
        else
        {
            visibleItems++;
        }
 
        return menuItem;
    }
    
    /**
     * Add a separator to the menu.
     */
    public void addSeparator() {
      add(new Separator());
    }
 
    /**
     * Closes the opened submenus when scrolling starts
     */
    private void closeOpenedSubMenus(  )
    {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager(  );
        MenuElement[] path = manager.getSelectedPath(  );
        int i = 0;
 
        for( ; i < path.length; i++ )
        {
            if( path[ i ] == this )
            {
                break;
            }
        }
 
        MenuElement[] subPath = new MenuElement[ i + 1 ];
 
        try
        {
            System.arraycopy( path, 0, subPath, 0, i + 1 );
            manager.setSelectedPath( subPath );
        }
        catch( Exception ekasd )
        {
        }
    }
 
    /**
     * When timerUp is started it calls constantly this method to
     * make the JMenu scroll upwards. When the top of menu is reached
     * then upButton is set invisible. When scrollUp starts downButton
     * is setVisible.
     */
    private void scrollUp(  )
    {
        closeOpenedSubMenus(  );
 
        if( indexVisible == 0 )
        {
            upButton.setVisible( false );
 
            return;
        }
        else
        {
            indexVisible--;
            ( (JComponent) subMenus.get( indexVisible + visibleItems ) ).setVisible( false );
            ( (JComponent) subMenus.get( indexVisible ) ).setVisible( true );
            downButton.setVisible( true );
            if( indexVisible == 0)
            {
              upButton.setVisible( false );
            }
        }
    }
 
    /**
     * When timerDown is started it calls constantly this method to
     * make the JMenu scroll downwards. When the bottom of menu is reached
     * then downButton is set invisible. When scrolldown starts upButton
     * is setVisible.
     */
    private void scrollDown(  )
    {
        closeOpenedSubMenus(  );
 
        if( ( indexVisible + visibleItems ) == subMenus.size(  ) )
        {
            downButton.setVisible( false );
 
            return;
        }
        else if( ( indexVisible + visibleItems ) > subMenus.size(  ) )
        {
            return;
        }
        else
        {
            try
            {
                ( (JComponent) subMenus.get( indexVisible ) ).setVisible( false );
                ( (JComponent) subMenus.get( indexVisible + visibleItems ) ).setVisible( true );
                upButton.setVisible( true );
                indexVisible++;
                if( ( indexVisible + visibleItems ) == subMenus.size(  ) )
                {
                    downButton.setVisible( false );
                }
            }
            catch( Exception eks )
            {
              eks.printStackTrace();
            }
        }
    }
 
    /**
     * Creates two button: upButton and downButton.
     */
    private void createButtons(  )
    {
        upButton = new JMenuItem(DataViewer.getIcon("icons/up.gif"));
 
        Dimension d = new Dimension( 50, 16 );
        upButton.setPreferredSize( d );
        upButton.setBorderPainted( false );
        upButton.setFocusPainted( false );
        //upButton.setRolloverEnabled( true );
 
        class Up extends MouseAdapter
        {
            /**
             * When mouse enters over the upbutton, timerUp starts the
             * scrolling upwards.
             *
             * @param e MouseEvent
             */
            public void mouseEntered( MouseEvent e )
            {
                try
                {
                    timerUp.start(  );
                }
                catch( Exception ekas )
                {
                }
            }
 
            /**
             * When mouse exites the upbutton, timerUp stops.
             *
             * @param e MouseEvent
             */
            public void mouseExited( MouseEvent e )
            {
                try
                {
                    timerUp.stop(  );
                }
                catch( Exception ekas )
                {
                }
            }
        }
 
        MouseListener scrollUpListener = new Up(  );
        upButton.addMouseListener( scrollUpListener );
        add( upButton );
        
        downButton = new JMenuItem(DataViewer.getIcon("icons/attach.gif"));
        
        downButton.setPreferredSize( d );
        downButton.setBorderPainted( false );
        downButton.setFocusPainted( false );
 
        class Down extends MouseAdapter
        {
            /**
             * When mouse enters over the downbutton, timerDown starts the
             * scrolling downwards.
             *
             * @param e MouseEvent
             */
            public void mouseEntered( MouseEvent e )
            {
                try
                {
                    timerDown.start(  );
                }
                catch( Exception ekas )
                {
                }
            }
 
            /**
             * When mouse exites the downbutton, timerDown stops.
             *
             * @param e MouseEvent
             */
            public void mouseExited( MouseEvent e )
            {
                try
                {
                    timerDown.stop(  );
                }
                catch( Exception ekas )
                {
                }
            }
        }
 
        MouseListener scrollDownListener = new Down(  );
        downButton.addMouseListener( scrollDownListener );
        add( downButton );
    }
 
    /**
     * Hides the scrollButtons.
     */
    public void hideButtons(  )
    {
        upButton.setVisible( false );
        downButton.setVisible( false );
    }
}