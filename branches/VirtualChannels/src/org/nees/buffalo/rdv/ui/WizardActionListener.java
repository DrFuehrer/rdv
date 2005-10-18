package org.nees.buffalo.rdv.ui;

import java.util.EventListener;

public interface WizardActionListener extends EventListener {
  public void wizardBack(String oldID, String newID);

  public void wizardNext(String oldID, String newID);
  
  public void wizardFinish(String id);
  
  public void wizardCancel(String id);
}
