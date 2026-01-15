package structure;

import java.util.ArrayList;
import java.util.List;

public class GameState{
    public Board board;
    public ColorType currentPlayer;
    public List<Stone> blackStones;
    public List<Stone> whiteStones;
    public int blackStonesOut;
    public int whiteStonesOut;

    public GameState(){
        board=new Board();
        whiteStones=new ArrayList<Stone>();
        blackStones=new ArrayList<Stone>();
        currentPlayer=ColorType.WHITE;
        blackStonesOut=0;
        whiteStonesOut=0;
    }

    public GameState copy(){
        GameState newState= new GameState();
        newState.board=this.board.copy();
        newState.currentPlayer=this.currentPlayer;
        newState.blackStonesOut=this.blackStonesOut;
        newState.whiteStonesOut=this.whiteStonesOut;
        for(Stone s:this.blackStones){
            Stone newstone=new Stone(s.color,s.position);
            newstone.isOut=s.isOut;
            newState.blackStones.add(newstone);
        }
        for(Stone s:this.whiteStones){
            Stone newstone= new Stone(s.color,s.position);
            newstone.isOut=s.isOut;
            newState.whiteStones.add(newstone);
        }
        return newState;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(" ---- senet board ---- \n");

        // الصف الأول
        for (int i = 1; i <= 10; i++) {
            sb.append(printStone(board.stones[i]));
        }
        sb.append("\n");

        // الصف الثاني (معكوس)
        for (int i = 20; i >= 11; i--) {
            sb.append(printStone(board.stones[i]));
        }
        sb.append("\n");

        // الصف الثالث
        for (int i = 21; i <= 30; i++) {
            sb.append(printStone(board.stones[i]));
        }
        sb.append("\n");

        sb.append(" --------------------- \n");
        sb.append("white stones out: ").append(whiteStonesOut).append("/7\n");
        sb.append("black stones out: ").append(blackStonesOut).append("/7\n");
        sb.append("current player: ").append(currentPlayer).append("\n");
        sb.append(" --------------------- \n");

        return sb.toString();
    }
    private String printStone(Stone stone) {

        // مربع عادي فاضي
        if (stone.isEmpty() && !SpecialSquares.isSpecialSquare(stone.position)) {
            return "[  " + stone.position + "  ]";
        }

        // مربع خاص
        if (SpecialSquares.isSpecialSquare(stone.position)) {

            if (stone.color == ColorType.WHITE) {
                return "[W" + stone.position + "," +
                    SpecialSquares.getSquareType(stone.position) + "]";
            } else if (stone.color == ColorType.BLACK) {
                return "[B" + stone.position + "," +
                    SpecialSquares.getSquareType(stone.position) + "]";
            } else {
                return "[" + SpecialSquares.getSquareType(stone.position) +
                    "," + stone.position + "]";
            }
        }

        // مربع عادي فيه حجر
        if (stone.color == ColorType.WHITE) {
            return "[W , " + stone.position + "]";
        } else if (stone.color == ColorType.BLACK) {
            return "[B , " + stone.position + "]";
        } else {
            return "[_ , " + stone.position + "]";
        }
    }

}
