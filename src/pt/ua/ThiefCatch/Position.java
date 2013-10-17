package pt.ua.ThiefCatch;

public class Position
{
  public Position(int line, int column)
  {
    this.line = line;
    this.column = column;
  }

  Position(Position iPos)
  {
        column = iPos.column();
        line = iPos.line();
    }

  public int line()
  {
    return line;
  }

  public int column()
  {
    return column;
  }
  private int line;
    private int column;

    /**
     * @param line the line to set
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * @param column the column to set
     */
    public void setColumn(int column) {
        this.column = column;
    }

    public boolean equals(Position pos)
    {
        return ((pos.line() == line()) && (pos.column() == column()));
    }

    public boolean equals(int line, int column)
    {
        return ((line == line()) && (column == column()));
    }
}
