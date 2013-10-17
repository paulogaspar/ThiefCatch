package pt.ua.TextBoard;

import static java.lang.System.*;

/**
 * Utilities to use console
 */
public class Console
{
  /**
   * Available colors
   */
  public enum Color
  {
    BLACK,RED,GREEN,YELLOW,BLUE,MAGENTA,CYAN,WHITE,
    LIGHT_BLACK,LIGHT_RED,LIGHT_GREEN,LIGHT_YELLOW,
    LIGHT_BLUE,LIGHT_MAGENTA,LIGHT_CYAN,LIGHT_WHITE
  }

  /**
   * Clears the Console (stdout) 
   */
  public static void clear()
  {
    out.print("\u001B[2J");
  }

  /**
   * Places the cursor at (line,column)
   *
   *  @param column
   *  @param line
   */
  public static void moveCursor(int line,int column)
  {
    assert column >= 0 && line >= 0;

    out.printf("\u001B[%d;%df", 1+line, 1+column);
  }
        
  /**
   * Prints string in console (from current cursor position)
   *
   *  @param s string
   */
  public static void print(String s)
  {
    out.print(s);
  }

  /**
   * Prints string in console (from current cursor position)
   * using a foreground color
   *
   *  @param s string
   *  @param foreground foreground color
   */
  public static void print(String s,Color foreground)
  {
    int fc = foreground.ordinal();
    fc = (fc < 8 ? 30 + fc : 90 + fc - 8);
    out.printf("\u001B[%dm%s\u001B[0m",fc,s);
  }

  /**
   * Prints string in console (from current cursor position)
   * using a foreground color and a background color
   *
   *  @param s string
   *  @param foreground foreground color
   *  @param background background color
   */
  public static void print(String s,Color foreground,Color background)
  {
    int fc = foreground.ordinal();
    fc = (fc < 8 ? 30 + fc : 90 + fc - 8);
    int bc = background.ordinal();
    bc = (bc < 8 ? 40 + bc : 100 + bc - 8);
    out.printf("\u001B[%dm\u001B[%dm%s\u001B[0m",bc,fc,s);
  }

  /**
   * Defines foreground color
   *
   *  @param c foreground color
   */
  public static void setForeground(Color c)
  {
    int fc = c.ordinal();
    fc = (fc < 8 ? 30 + fc : 90 + fc - 8);
    out.printf("\u001B[%dm",fc);
  }

  /**
   * Defines background color
   *
   *  @param c background color
   */
  public static void setBackground(Color c)
  {
    int bc = c.ordinal();
    bc = (bc < 8 ? 40 + bc : 100 + bc - 8);
    out.printf("\u001B[%dm",bc);
  }

  /**
   * Resets all colors to default
   */
  public static void resetColorsToDefault()
  {
    out.print("\u001B[30m\u001B[107m");
  }

  /**
   * Resets all console attributes
   */
  public static void resetAllAttributes()
  {
    out.print("\u001B[0m");
  }

  /**
   * Enable bold attribute
   */
  public static void setBold()
  {
    out.print("\u001B[1m");
  }

  /**
   * Enable faint attribute
   */
  public static void setFaint()
  {
    out.print("\u001B[2m");
  }

  /**
   * Disable bold and faint attributes
   */
  public static void setNormal()
  {
    out.print("\u001B[22m");
  }

  /**
   * Enable italic attribute (not working!)
   */
  public static void setItalic()
  {
    out.print("\u001B[3m");
  }

  /**
   * Enable underline attribute
   */
  public static void setUnderline()
  {
    out.print("\u001B[4m");
  }

  /**
   * Enable slow blink attribute (not working!)
   */
  public static void setSlowBlink()
  {
    out.print("\u001B[5m");
  }

  /**
   * Enable fast blink attribute (not working!)
   */
  public static void setFastBlink()
  {
    out.print("\u001B[6m");
  }

  /**
   * Disable blink attributes (not working!)
   */
  public static void resetBlink()
  {
    out.print("\u001B[25m");
  }

  /**
   * Define negative image attribute
   */
  public static void setNegativeImage()
  {
    out.print("\u001B[7m");
  }

  /**
   * Defines positive image attribute
   */
  public static void setPositiveImage()
  {
    out.print("\u001B[27m");
  }

  /**
   * Enable conceal attribute
   */
  public static void setConceal()
  {
    out.print("\u001B[8m");
  }

  /**
   * Disable conceal attribute
   */
  public static void resetConceal()
  {
    out.print("\u001B[28m");
  }

  /**
   * Hide mouse cursor
   */
  public static void hideCursor()
  {
    out.print("\u001B[?25l");
  }

  /**
   * Show mouse cursor
   */
  public static void showCursor()
  {
    out.print("\u001B[?25h");
  }

  /**
   * Stops the thread pause milliseconds
   *
   *  @param pause value
   */
  public static void waitABit(long pause)
  {
    moveCursor(0,0);
    try
    {
      Thread.sleep(pause);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
      err.println("ERROR: thread interrupted!");
    }
  }
}

