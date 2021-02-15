package it.unict.dmi.BLAZE;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Decimation {

  public static void main(String[] args) throws IOException {
    //Bugs: l'immagine originale deve essere a toni di grigio e di tipo RGB!!!!
    //larghezza e altezza devono essere multipli di f
    BufferedImage input = ImageIO.read(new File(args[0]));
    String nomeImg = args[0].substring(0, args[0].length() - 4);
    ImageIO.write(makeDecimation(input, 4), "PNG", new File(nomeImg + "_decimation.png"));
    ImageIO.write(makeCut(input, 4), "PNG", new File(nomeImg + "_compare.png"));
  }

  private static BufferedImage makeDecimation(BufferedImage input, int f) {

    int width = input.getWidth();
    int height = input.getHeight();
    BufferedImage output = new BufferedImage(width / f, height / f, BufferedImage.TYPE_INT_ARGB);

    for (int x = 0; x < width; x += f) {
      for (int y = 0; y < height; y += f) {
        int val = input.getRGB(x, y);
        output.setRGB(x / f, y / f, val);
      }
    }

    return output;
  }

  private static BufferedImage makeCut(BufferedImage input, int f) {

    int width = input.getWidth();
    int height = input.getHeight();
    BufferedImage output = new BufferedImage(width - (f - 1), height - (f - 1), BufferedImage.TYPE_INT_ARGB);

    for (int x = 0; x < width - (f - 1) - 1; x++) {
      for (int y = 0; y < height - (f - 1) - 1; y++) {
        int val = input.getRGB(x, y);
        output.setRGB(x, y, val);
      }
    }

    return output;
  }
}
