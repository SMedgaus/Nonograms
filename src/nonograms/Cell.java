/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nonograms;

/**
 * Class modeling cell of Nonogram.
 * It has three states:
 *  - UNDEFINED, initial;
 *  - EMPTY, 100%, that this cell should be empty;
 *  - FILLED, 100% that this cell should be filled.
 * @author Sergey
 */
public class Cell {
    
    private CellState state = CellState.UNDEFINED;
    
    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;
    }
    
}
