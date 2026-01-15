package structure;

public class Stone {

    public ColorType color;
    public int position; // current position
    public boolean isOut; // still in the board or removed

    public Stone(ColorType color, int position) {
        this.color = color;
        this.position = position;
        this.isOut = false;
    }

    public boolean isEmpty() {
        return color == ColorType.NONE;
    }

}
