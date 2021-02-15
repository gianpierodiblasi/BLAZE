package it.unict.dmi.BLAZE;

import java.awt.image.BufferedImage;

public class BLAZE {

  private final int NOT_ASSIGNED = -1000;
  private final BufferedImage image;
  private final int h;
  private final int w;
  private final int T1;
  private final int T2;
  private final float k;
  private final int q;
  private final int f;
  private int matrixH, matrixW;
  private byte[] assigned;
  private byte[] superMatrix;

  public BLAZE(BufferedImage imageIO, int zoom) {
    image = imageIO;
    T1 = 4;
    T2 = 3;
    k = 1;
    q = 1;
    f = zoom;
    w = image.getWidth();
    h = image.getHeight();
  }

  private byte getYFromRGB(int sRGB) {
    int r = (sRGB & 0x00FF0000) >> 16;
    int g = (sRGB & 0x0000FF00) >> 8;
    int b = (sRGB & 0x000000FF);

    int Y = (int) (0.299F * r + 0.587F * g + 0.114F * b);
    return (byte) (Y - 128);
  }

  private byte rappresentant(int k, int q) {
    k += 128;
    int m = 256 / q;

    for (int i = 1; i <= m; i++) {
      int q1 = q * (i - 1);
      int q2 = q * i - 1;
      if (k >= q1 && k <= q2) {
        return (byte) ((q1 + q2) / 2 - 128);
      }
    }
    return -1;
  }

  private byte range(byte a, byte b, byte c, byte d) {
    return (byte) (Math.max(Math.max(a, b), Math.max(c, d)) - Math.min(Math.min(a, b), Math.min(c, d)));
  }

  @SuppressWarnings("empty-statement")
  private byte evaluateMedianValue(byte[] bins) {
    int cont = 1;
    int sum = bins[0];
    int j;

    for (int i = 1; i < bins.length; i++) {
      for (j = i - 1; j >= 0 && bins[i] != bins[j]; j--);
      if (j < 0) {
        cont++;
        sum += bins[i];
      }
    }
    return (byte) (sum / cont);
  }

  private int[] getRGB(int color) {
    return new int[]{(color & 0x00FF0000) >> 16, (color & 0x0000FF00) >> 8, color & 0x000000FF};
  }

  private int getRGB(int Y, int U, int V) {
    int R = (int) (Y + (1.403 * V));
    int G = (int) (Y - (0.344 * U) - (0.714 * V));
    int B = (int) (Y + (1.770 * U));

    if (R < 0) {
      R = 0;
    } else if (R > 255) {
      R = 255;
    }
    if (G < 0) {
      G = 0;
    } else if (G > 255) {
      G = 255;
    }
    if (B < 0) {
      B = 0;
    } else if (B > 255) {
      B = 255;
    }

    return 0xFF000000 | (R << 16) | (G << 8) | B;
  }

  private void setAssigned(int x, int y) {
    int pos = y * matrixW + x;
    int one = 1 << (7 - pos % 8);
    assigned[pos / 8] |= one;
  }

  private boolean isAssigned(int x, int y) {
    int pos = y * matrixW + x;
    int one = 1 << (7 - pos % 8);
    return (assigned[pos / 8] & one) != 0;
  }

  private void simpleEnlargement() {
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int xf = x * f;
        int yf = y * f;
        superMatrix[yf * matrixW + xf] = getYFromRGB(image.getRGB(x, y));
        this.setAssigned(xf, yf);
      }
    }
  }

  private void fillingHoles1() {
    for (int y = f / 2; y < matrixH; y += f) {
      for (int x = f / 2; x < matrixW; x += f) {
        byte a = superMatrix[(y - (f / 2)) * matrixW + (x - (f / 2))];
        byte b = superMatrix[(y - (f / 2)) * matrixW + (x + (f / 2))];
        byte c = superMatrix[(y + (f / 2)) * matrixW + (x - (f / 2))];
        byte d = superMatrix[(y + (f / 2)) * matrixW + (x + (f / 2))];

        int absAD = Math.abs(a - d);
        int absBC = Math.abs(b - c);

        //Uniformity
        if (range(a, b, c, d) < T1) {
          for (int xx = -(f / 2 - 1); xx <= (f / 2 - 1); xx++) {
            for (int yy = -(f / 2 - 1); yy <= (f / 2 - 1); yy++) {
              float alpha = ((float) (xx + (f / 2))) / f;
              float beta = ((float) (yy + (f / 2))) / f;

              float val = (1 - beta) * ((1 - alpha) * a + alpha * b) + beta * ((1 - alpha) * c + alpha * d);

              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
            }
          }
          this.setAssigned(x, y);
        } //Edge in the SW-NE direction
        else if (absAD > T2 && absAD > absBC) {
          for (int xx = -(f / 2 - 1); xx <= (f / 2 - 1); xx++) {
            for (int yy = -(f / 2 - 1); yy <= (f / 2 - 1); yy++) {
              float alpha = ((float) (f - (xx - yy)) / (2 * f));
              float beta = (xx + yy) / (k * f);

              float val = (1 - Math.abs(beta)) * ((1 - alpha) * b + alpha * c) + Math.abs(beta) * ((1 / k - beta) * a + (1 / k + beta) * d) * k / 2;

              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
            }
          }
          this.setAssigned(x, y);
        } //Edge in the NW-SE direction
        else if (absBC > T2 && absBC > absAD) {
          for (int xx = -(f / 2 - 1); xx <= (f / 2 - 1); xx++) {
            for (int yy = -(f / 2 - 1); yy <= (f / 2 - 1); yy++) {
              float alpha = ((float) (f + (xx + yy))) / (2 * f);
              float beta = -((float) (xx - yy)) / (k * f);

              float val = (1 - Math.abs(beta)) * ((1 - alpha) * a + alpha * d) + Math.abs(beta) * ((1 / k - beta) * b + (1 / k + beta) * c) * k / 2;

              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
            }
          }
          this.setAssigned(x, y);
        } //vertical or horizontal edge
        else if (absAD > T1 && absBC > T1) {
          int diff = (a - d) * (b - c);
          if (diff < 0)//Edge in the E-W direction
          {
            int xx = f / 2;
            for (int yy = -(f / 2 - 1); yy <= f / 2 - 1; yy++) {
              float alpha = ((float) (yy + (f / 2))) / f;

              float valDx = ((1 - alpha) * b + alpha * d);
              float valSx = ((1 - alpha) * a + alpha * c);

              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) valDx;
              superMatrix[(y + yy) * matrixW + (x - xx)] = (byte) valSx;
            }

            this.setAssigned(x - f / 2, y); //V1
            this.setAssigned(x + f / 2, y); //V2
          } else if (diff > 0) //Edge in the N-S direction
          {
            int yy = f / 2;
            for (int xx = -(f / 2 - 1); xx <= f / 2 - 1; xx++) {
              float alpha = ((float) (xx + (f / 2))) / f;

              float valUp = ((1 - alpha) * a + alpha * b);
              float valDw = ((1 - alpha) * c + alpha * d);

              superMatrix[(y - yy) * matrixW + (x + xx)] = (byte) valUp;
              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) valDw;
            }

            this.setAssigned(x, y - f / 2); //H1
            this.setAssigned(x, y + f / 2); //H2
          }
        }
      }
    }
  }

  private void fillingHoles2() {
    for (int y = f / 2; y < matrixH; y += f) {
      for (int x = 0; x < matrixW; x += f) {
        int a = superMatrix[(y - (f / 2)) * matrixW + x];
        int b = superMatrix[(y + (f / 2)) * matrixW + x];
        int X1 = NOT_ASSIGNED;
        if (x != 0 && this.isAssigned(x - f / 2, y)) {
          X1 = superMatrix[y * matrixW + (x - (f / 2))];
        }
        int X2 = NOT_ASSIGNED;
        if (x != matrixW - 1 && this.isAssigned(x + f / 2, y)) {
          X2 = superMatrix[y * matrixW + (x + (f / 2))];
        }

        int absAB = Math.abs(a - b);
        int absX1X2 = Math.abs(X1 - X2);

        if (X1 == NOT_ASSIGNED || X2 == NOT_ASSIGNED) {
          if (absAB < T1) {
            int xx = 0;
            for (int yy = -(f / 2 - 1); yy <= f / 2 - 1; yy++) {
              float alpha = ((float) (yy + (f / 2))) / f;
              float val = (1 - alpha) * a + alpha * b;

              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
            }
            this.setAssigned(x, y);
          }
        } else//x1 and x2 yet assigned
        {
          if (absAB > T2 && absAB > absX1X2) //edge in x1 x2 direction
          {
            int xx = 0;
            for (int yy = -(f / 2 - 1); yy <= f / 2 - 1; yy++) {
              float val = (superMatrix[(y + yy) * matrixW + (x - f / 2)] + superMatrix[(y + yy) * matrixW + (x + f / 2)]) / 2;
              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
            }
            this.setAssigned(x, y);
          } else if (absX1X2 > T2 && absX1X2 > absAB)//edge in a b direction
          {
            int xx = 0;
            for (int yy = -(f / 2 - 1); yy <= f / 2 - 1; yy++) {
              float alpha = ((float) (yy + (f / 2))) / f;
              float val = (1 - alpha) * a + alpha * b;

              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
            }
            this.setAssigned(x, y);
          }
        }
      }
    }

    for (int y = 0; y < matrixH; y += f) {
      for (int x = f / 2; x < matrixW; x += f) {
        int a = superMatrix[y * matrixW + x - (f / 2)];
        int b = superMatrix[y * matrixW + x + (f / 2)];
        int X1 = NOT_ASSIGNED;
        if (y != 0 && this.isAssigned(x, y - f / 2)) {
          X1 = superMatrix[(y - (f / 2)) * matrixW + x];
        }
        int X2 = NOT_ASSIGNED;
        if (y != matrixH - 1 && this.isAssigned(x, y + f / 2)) {
          X2 = superMatrix[(y + (f / 2)) * matrixW + x];
        }

        int absAB = Math.abs(a - b);
        int absX1X2 = Math.abs(X1 - X2);

        if (X1 == NOT_ASSIGNED || X2 == NOT_ASSIGNED) {
          if (absAB < T1) {
            int yy = 0;
            for (int xx = -(f / 2 - 1); xx <= f / 2 - 1; xx++) {
              float alpha = ((float) (xx + (f / 2))) / f;
              float val = (1 - alpha) * a + alpha * b;
              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
            }
            this.setAssigned(x, y);
          }
        } else//x1 and x2 yet assigned
        {
          if (absAB > T2 && absAB > absX1X2)//edge in x1 x2 direction
          {
            int yy = 0;
            for (int xx = -(f / 2 - 1); xx <= f / 2 - 1; xx++) {
              float val = (superMatrix[(y - f / 2) * matrixW + (x + xx)] + superMatrix[(y + f / 2) * matrixW + (x + xx)]) / 2;
              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
            }
            this.setAssigned(x, y);
          } else if (absX1X2 > T2 && absX1X2 > absAB)//edge in a b direction
          {
            int yy = 0;
            for (int xx = -(f / 2 - 1); xx <= f / 2 - 1; xx++) {
              float alpha = ((float) (xx + (f / 2))) / f;
              float val = (1 - alpha) * a + alpha * b;
              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
            }
            this.setAssigned(x, y);
          }
        }
      }
    }
  }

  private void rebinning() {
    for (int y = f / 2; y < matrixH; y += f) {
      for (int x = f / 2; x < matrixW; x += f) {
        if (!this.isAssigned(x, y)) {
          int a = superMatrix[(y - (f / 2)) * matrixW + x - (f / 2)];
          int b = superMatrix[(y - (f / 2)) * matrixW + x + (f / 2)];
          int c = superMatrix[(y + (f / 2)) * matrixW + x - (f / 2)];
          int d = superMatrix[(y + (f / 2)) * matrixW + x + (f / 2)];

          byte[] bins = {rappresentant(a, q), rappresentant(b, q), rappresentant(c, q), rappresentant(d, q)};
          byte valX = this.evaluateMedianValue(bins);
          superMatrix[y * matrixW + x] = valX;

          for (int xx = -(f / 2 - 1); xx <= f / 2 - 1; xx++) {
            for (int yy = -(f / 2 - 1); yy <= f / 2 - 1; yy++) {
              float alpha = ((float) (xx + (f / 2))) / f;
              float beta = ((float) (yy + (f / 2))) / f;

              float val = (1 - (1 / k)) * valX + (1 / k) * ((1 - beta) * ((1 - alpha) * a + alpha * b) + beta * ((1 - alpha) * c + alpha * d));

              superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
            }
          }
        }
      }
    }

    for (int y = f / 2; y < matrixH; y += f) {
      for (int x = 0; x < matrixW; x += f) {
        if (!this.isAssigned(x, y)) {
          int a = superMatrix[(y - (f / 2)) * matrixW + x];
          int b = superMatrix[(y + (f / 2)) * matrixW + x];

          int X1 = NOT_ASSIGNED;
          if (x != 0 && this.isAssigned(x - f / 2, y)) {
            X1 = superMatrix[y * matrixW + (x - (f / 2))];
          }
          int X2 = NOT_ASSIGNED;
          if (x != matrixW - 1 && this.isAssigned(x + f / 2, y)) {
            X2 = superMatrix[y * matrixW + (x + (f / 2))];
          }

          byte[] bins;
          if (X1 != NOT_ASSIGNED && X2 != NOT_ASSIGNED) {
            bins = new byte[]{rappresentant(a, q), rappresentant(b, q), rappresentant(X1, q), rappresentant(X2, q)};
          } else if (X1 != NOT_ASSIGNED) {
            bins = new byte[]{rappresentant(a, q), rappresentant(b, q), rappresentant(X1, q)};
          } else if (X2 != NOT_ASSIGNED) {
            bins = new byte[]{rappresentant(a, q), rappresentant(b, q), rappresentant(X2, q)};
          } else {
            bins = new byte[]{rappresentant(a, q), rappresentant(b, q)};
          }

          byte valP = this.evaluateMedianValue(bins);
          superMatrix[y * matrixW + x] = valP;

          int xx = 0;
          for (int yy = -(f / 2 - 1); yy <= f / 2 - 1; yy++) {
            float alpha = ((float) (yy + (f / 2))) / f;
            float val = (1 - (1 / k)) * valP + (1 / k) * ((1 - alpha) * a + alpha * b);
            superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
          }
        }
      }
    }

    for (int y = 0; y < matrixH; y += f) {
      for (int x = f / 2; x < matrixW; x += f) {
        if (!this.isAssigned(x, y)) {
          int a = superMatrix[y * matrixW + x - (f / 2)];
          int b = superMatrix[y * matrixW + x + (f / 2)];
          int X1 = NOT_ASSIGNED;
          if (y != 0 && this.isAssigned(x, y - f / 2)) {
            X1 = superMatrix[(y - (f / 2)) * matrixW + x];
          }
          int X2 = NOT_ASSIGNED;
          if (y != matrixH - 1 && this.isAssigned(x, y + f / 2)) {
            X2 = superMatrix[(y + (f / 2)) * matrixW + x];
          }

          byte[] bins;
          if (X1 != NOT_ASSIGNED && X2 != NOT_ASSIGNED) {
            bins = new byte[]{rappresentant(a, q), rappresentant(b, q), rappresentant(X1, q), rappresentant(X2, q)};
          } else if (X1 != NOT_ASSIGNED) {
            bins = new byte[]{rappresentant(a, q), rappresentant(b, q), rappresentant(X1, q)};
          } else if (X2 != NOT_ASSIGNED) {
            bins = new byte[]{rappresentant(a, q), rappresentant(b, q), rappresentant(X2, q)};
          } else {
            bins = new byte[]{rappresentant(a, q), rappresentant(b, q)};
          }

          byte valP = this.evaluateMedianValue(bins);
          superMatrix[y * matrixW + x] = valP;

          int yy = 0;
          for (int xx = -(f / 2 - 1); xx <= f / 2 - 1; xx++) {
            float alpha = ((float) (xx + (f / 2))) / f;
            float val = (1 - (1 / k)) * valP + (1 / k) * ((1 - alpha) * a + alpha * b);
            superMatrix[(y + yy) * matrixW + (x + xx)] = (byte) val;
          }
        }
      }
    }
  }

  private BufferedImage createImage() {
    BufferedImage output = new BufferedImage(matrixW, matrixH, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int xf = x * f;
        int yf = y * f;
        int[] RGB = this.getRGB(image.getRGB(x, y));
        int Y = superMatrix[yf * matrixW + xf] + 128;
        int U = (int) ((RGB[2] - Y) * 0.565); //(B-Y)*0565
        int V = (int) ((RGB[0] - Y) * 0.713); //(R-Y)*0.713
        output.setRGB(xf, yf, this.getRGB(Y, U, V));

        int nextX = xf + f;
        int nextY = yf + f;
        if (y != h - 1 && x != w - 1) {
          for (int xx = xf; xx < nextX; xx++) {
            for (int yy = yf; yy < nextY; yy++) {
              Y = superMatrix[yy * matrixW + xx] + 128;
              output.setRGB(xx, yy, this.getRGB(Y, U, V));
            }
          }
        } else if (x == w - 1 && y != h - 1) {
          for (int yy = yf + 1; yy < nextY; yy++) {
            Y = superMatrix[yy * matrixW + xf] + 128;
            output.setRGB(xf, yy, this.getRGB(Y, U, V));
          }
        } else if (y == h - 1 && x != w - 1) {
          for (int xx = xf + 1; xx < nextX; xx++) {
            Y = superMatrix[yf * matrixW + xx] + 128;
            output.setRGB(xx, yf, this.getRGB(Y, U, V));
          }
        }
      }
    }

    return output;
  }

  public BufferedImage makeZoom() {
    matrixH = h * f - (f - 1);
    matrixW = w * f - (f - 1);

    superMatrix = new byte[matrixW * matrixH];
    assigned = new byte[matrixW * matrixH / 8 + 1];

    //First stage: simple enlargement
    this.simpleEnlargement();

    //Second Stage: fillin the holes (part I)
    this.fillingHoles1();

    //Third stage: filling the holes (part II)
    this.fillingHoles2();

    //Final stage: rebinning
    this.rebinning();

    assigned = null;
    System.gc();
    return this.createImage();
  }
}
