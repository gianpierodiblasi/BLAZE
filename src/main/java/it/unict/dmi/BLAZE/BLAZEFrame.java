package it.unict.dmi.BLAZE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;

public class BLAZEFrame extends JFrame {

  private static final long serialVersionUID = 1L;

  private BLAZEPanel bPanel = new BLAZEPanel();

  @SuppressWarnings("CallToPrintStackTrace")
  public BLAZEFrame() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setTitle("BLAZE!");
    this.setSize(new Dimension(800, 700));
    this.getContentPane().add(bPanel, BorderLayout.CENTER);
  }

  //MAIN
  public static void main(String[] a) {
    BLAZEFrame f = new BLAZEFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);
    f.setExtendedState(JFrame.MAXIMIZED_BOTH);
  }
  //END MAIN
}
