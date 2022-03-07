package it.unict.dmi.BLAZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

public class BLAZEPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private JComboBox<String> jComboBox1 = new JComboBox<>(filters);
  private JComboBox<String> jComboBox2 = new JComboBox<>(filters);
  private BorderLayout borderLayout1 = new BorderLayout();
  private JButton open = new JButton();
  private JFileChooser openChooser = new JFileChooser();
  private JFileChooser saveChooser = new JFileChooser();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private PaintPanel pannello1 = new PaintPanel();
  private PaintPanel pannello2 = new PaintPanel();
  private JButton start = new JButton();
  private JPanel ZoomPanel = new JPanel();
  private JProgressBar bar = new JProgressBar();
  private ButtonGroup buttonGroup1 = new ButtonGroup();
  private JRadioButton jRadioButton1 = new JRadioButton();
  private JRadioButton jRadioButton2 = new JRadioButton();
  private JPanel jPanel1 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private FlowLayout flowLayout1 = new FlowLayout();
  private JButton save1 = new JButton();
  private JButton save2 = new JButton();
  private JRadioButton jRadioButton4 = new JRadioButton();
  private JRadioButton jRadioButton6 = new JRadioButton();
  private JRadioButton jRadioButton8 = new JRadioButton();
  private JRadioButton jRadioButton12 = new JRadioButton();

  private final static String[] filters = {"Nearest Neighbor", "Bilinear", "Bicubic", "BLAZE"};
  private int type1 = 1, type2 = 1;
  private int oldType1 = -1, oldType2 = -1;
  private float zoom = 1.0F;
  private boolean zoomChanged = false;
  private BufferedImage image;

  @SuppressWarnings("CallToPrintStackTrace")
  public BLAZEPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void save(RenderedImage image) {
    saveChooser.setSelectedFile(null);
    int res2 = JOptionPane.NO_OPTION;
    File f = null;
    while (res2 == JOptionPane.NO_OPTION) {
      int res = saveChooser.showSaveDialog(this);
      if (res == JFileChooser.APPROVE_OPTION) {
        f = saveChooser.getSelectedFile();
        if (f.exists()) {
          res2 = JOptionPane.showConfirmDialog(this, "The file already exists, overwrite?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        } else {
          res2 = JOptionPane.YES_OPTION;
        }
      } else {
        res2 = JOptionPane.CANCEL_OPTION;
      }
    }
    try {
      if (res2 == JOptionPane.YES_OPTION) {
        ImageIO.write(image, saveChooser.getFileFilter().toString(), f);
      }
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "It's not possible to save the file", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void setComponentEnabled(boolean b1, boolean b2) {
    jComboBox1.setEnabled(b1);
    jComboBox2.setEnabled(b1);
    jRadioButton1.setEnabled(b1);
    jRadioButton2.setEnabled(b1);
    jRadioButton4.setEnabled(b1);
    jRadioButton6.setEnabled(b1);
    jRadioButton8.setEnabled(b1);
    jRadioButton12.setEnabled(b1);
    start.setEnabled(b1);
    save1.setEnabled(b2);
    save2.setEnabled(b2);
  }

  private void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    openChooser.setAcceptAllFileFilterUsed(false);
    openChooser.setFileFilter(new OpenImageFilter());
    saveChooser.setAcceptAllFileFilterUsed(false);
    saveChooser.setFileFilter(new SaveImageFilter());

    jComboBox1.setEnabled(false);
    jComboBox2.setEnabled(false);

    open.setToolTipText("Open");
    open.setIcon(new ImageIcon(ImageIO.read(BLAZEPanel.class.getClassLoader().getResourceAsStream("open.gif"))));
    open.setMargin(new Insets(0, 0, 0, 0));
    start.setEnabled(false);
    start.setToolTipText("Start Zoom");
    start.setIcon(new ImageIcon(ImageIO.read(BLAZEPanel.class.getClassLoader().getResourceAsStream("start.gif"))));
    start.setMargin(new Insets(0, 0, 0, 0));
    save1.setEnabled(false);
    save1.setToolTipText("Save Image on SX panel");
    save1.setIcon(new ImageIcon(ImageIO.read(BLAZEPanel.class.getClassLoader().getResourceAsStream("save.gif"))));
    save1.setMargin(new Insets(0, 0, 0, 0));
    save2.setEnabled(false);
    save2.setToolTipText("Save Image on DX panel");
    save2.setIcon(new ImageIcon(ImageIO.read(BLAZEPanel.class.getClassLoader().getResourceAsStream("save.gif"))));
    save2.setMargin(new Insets(0, 0, 0, 0));

    flowLayout1.setAlignment(FlowLayout.LEFT);
    flowLayout1.setHgap(0);
    flowLayout1.setVgap(0);

    ZoomPanel.setLayout(flowLayout1);
    jRadioButton1.setText("1x");
    jRadioButton1.setEnabled(false);
    jRadioButton2.setText("2x");
    jRadioButton2.setEnabled(false);
    jRadioButton4.setText("4x");
    jRadioButton4.setEnabled(false);
    jRadioButton6.setText("6x");
    jRadioButton6.setEnabled(false);
    jRadioButton8.setText("8x");
    jRadioButton8.setEnabled(false);
    jRadioButton12.setText("12x");
    jRadioButton12.setEnabled(false);

    ZoomPanel.add(open, null);
    ZoomPanel.add(start, null);
    ZoomPanel.add(save1, null);
    ZoomPanel.add(save2, null);
    ZoomPanel.add(jComboBox1, null);
    ZoomPanel.add(jComboBox2, null);
    ZoomPanel.add(jRadioButton1, null);
    ZoomPanel.add(jRadioButton2, null);
    ZoomPanel.add(jRadioButton4, null);
    ZoomPanel.add(jRadioButton6, null);
    ZoomPanel.add(jRadioButton8, null);
    ZoomPanel.add(jRadioButton12, null);
    ZoomPanel.add(bar, null);
    jPanel1.setLayout(gridLayout1);
    this.add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jScrollPane1, null);
    jScrollPane1.getViewport().add(pannello1, null);
    jPanel1.add(jScrollPane2, null);
    this.add(ZoomPanel, BorderLayout.NORTH);
    jScrollPane2.getViewport().add(pannello2, null);

    buttonGroup1.add(jRadioButton2);
    buttonGroup1.add(jRadioButton1);
    buttonGroup1.add(jRadioButton4);
    buttonGroup1.add(jRadioButton6);
    buttonGroup1.add(jRadioButton8);
    buttonGroup1.add(jRadioButton12);

    Listener listener = new Listener();
    jComboBox1.addActionListener(listener);
    jComboBox2.addActionListener(listener);
    save1.addActionListener(listener);
    save2.addActionListener(listener);
    jRadioButton1.addActionListener(listener);
    jRadioButton2.addActionListener(listener);
    jRadioButton4.addActionListener(listener);
    jRadioButton6.addActionListener(listener);
    jRadioButton8.addActionListener(listener);
    jRadioButton12.addActionListener(listener);
    open.addActionListener(listener);
    start.addActionListener(listener);
  }

  private class Listener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source instanceof JComboBox) {
        if (source == jComboBox1) {
          type1 = ((JComboBox) source).getSelectedIndex() + 1;
        } else if (source == jComboBox2) {
          type2 = ((JComboBox) source).getSelectedIndex() + 1;
        }
      } else if (source instanceof JRadioButton) {
        if (jRadioButton1.isSelected()) {
          zoom = 1;
        } else if (jRadioButton2.isSelected()) {
          zoom = 2;
        } else if (jRadioButton4.isSelected()) {
          zoom = 4;
        } else if (jRadioButton6.isSelected()) {
          zoom = 6;
        } else if (jRadioButton8.isSelected()) {
          zoom = 8;
        } else if (jRadioButton12.isSelected()) {
          zoom = 12;
        }
        zoomChanged = true;
      } else if (source == open) {
        int risposta = openChooser.showOpenDialog(BLAZEPanel.this);
        if (risposta == JFileChooser.APPROVE_OPTION)
          try {
          BufferedImage buff = ImageIO.read(openChooser.getSelectedFile());
          int w = buff.getWidth();
          int h = buff.getHeight();
          image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
          Graphics2D g2 = image.createGraphics();
          g2.drawImage(buff, 0, 0, null);
          g2.dispose();

          pannello1.setImage(image);
          pannello2.setImage(image);

          setComponentEnabled(true, false);
          jRadioButton1.setSelected(true);
          zoom = 1;
          oldType1 = -1;
          oldType2 = -1;
          zoomChanged = true;

          jScrollPane1.revalidate();
          jScrollPane2.revalidate();
        } catch (IOException ex) {
        }
      } else if (source == start) {
        (new Thread() {
          @Override
          public void run() {
            bar.setIndeterminate(true);
            setComponentEnabled(false, false);

            if (oldType1 != type1 || zoomChanged) {
              if (type1 != 4) {
                pannello1.setImage(new AffineTransformOp(AffineTransform.getScaleInstance(zoom, zoom), type1).filter(image, null));
              } else if (zoom != 1) {
                pannello1.setImage(new BLAZE(image, (int) zoom).makeZoom());
              } else {
                pannello1.setImage(image);
              }

              oldType1 = type1;
            }

            if (oldType2 != type2 || zoomChanged) {
              if (type2 != 4) {
                pannello2.setImage(new AffineTransformOp(AffineTransform.getScaleInstance(zoom, zoom), type2).filter(image, null));
              } else if (zoom != 1) {
                pannello2.setImage(new BLAZE(image, (int) zoom).makeZoom());
              } else {
                pannello2.setImage(image);
              }

              oldType2 = type2;
            }
            zoomChanged = false;

            bar.setIndeterminate(false);
            setComponentEnabled(true, true);
          }
        }).start();
      } else if (source == save1) {
        save(pannello1.getImage());
      } else if (source == save2) {
        save(pannello2.getImage());
      }
    }
  }

  private class PaintPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private RenderedImage image;
    private final AffineTransform tx = AffineTransform.getTranslateInstance(0, 0);

    private PaintPanel() {
      this.setBackground(Color.black);
    }

    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (image != null) {
        ((Graphics2D) g).drawRenderedImage(image, tx);
      }
    }

    public RenderedImage getImage() {
      return image;
    }

    public void setImage(RenderedImage image) {
      this.image = image;
      this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
      this.revalidate();
      this.repaint();
    }
  }

  private class OpenImageFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
      String str = f.getName().toLowerCase();
      return str.endsWith(".gif") || str.endsWith(".jpg") || str.endsWith(".jpeg")
              || str.endsWith(".png") || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "Image File";
    }
  }

  private class SaveImageFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
      String str = f.getName().toLowerCase();
      return str.endsWith(".png") || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "PNG";
    }

    @Override
    public String toString() {
      return "png";
    }
  }
}
