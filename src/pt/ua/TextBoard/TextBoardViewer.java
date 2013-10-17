package pt.ua.TextBoard;

import javax.swing.ImageIcon;

/**
 *  Abstract text board visualization class.
 */
abstract public class TextBoardViewer
{
  /**
   * Constructs a new text board visualizer
   * 
   * @param numLines  board number of lines
   * @param numColumns  board number of columns
   */
  public TextBoardViewer(String name, int numLines, int numColumns)
  {
    assert name != null;
    assert numLines > 0 && numColumns > 0;

    this.name = name;
    this.numLines = numLines;
    this.numColumns = numColumns;
  }

  public int numberOfLines()
  {
    return numLines;
  }

  public int numberOfColumns()
  {
    return numColumns;
  }

  /**
   * Draw the entire board
   */
  abstract public void drawAll();

  /**
   * Draw the cell
   * 
   * @param line  cell line position
   * @param column  cell column position
   * @param text  text to attach to cell
   */
  abstract public void setCell(int line, int column, String text);

  /**
   * Draw the cell
   * 
   * @param line  cell line position
   * @param column  cell column position
   * @param text  text to attach to cell
   * @param foreground  foreground color
   */
  abstract public void setCell(int line, int column, String text, Colors foreground);

  /**
   * Draw the cell
   * 
   * @param line  cell line position
   * @param column  cell column position
   * @param text  text to attach to cell
   * @param foreground  foreground color
   * @param background  background color
   */
  abstract public void setCell(int line, int column, String text, Colors foreground, Colors background);


  /**
   * Draw the cell
   *
   * @param line  cell line position
   * @param column  cell column position
   * @param icon  image icon to use in the cell
   */
  abstract public void setCell(int line, int column, ImageIcon icon);


  /**
   * Draw the cell with default foreground and background colors
   * 
   * @param line  cell line position
   * @param column  cell column position
   */
  abstract public void resetCell(int line,int column);

  /**
   * Terminate text board visualization
   */
  abstract public void terminate();

  protected final String name;
  protected final int numLines;
  protected final int numColumns;

    
}

