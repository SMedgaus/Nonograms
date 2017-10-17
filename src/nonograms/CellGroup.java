/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nonograms;

/**
 * Stores number of cells in current group. Can be crossed out.
 *
 * @author Sergey
 */
public class CellGroup {

    private final int cellNumber;
    private boolean crossedOut;

    public CellGroup(int number) {
        this.cellNumber = number;
        crossedOut = false;
    }

    public boolean isCrossedOut() {
        return crossedOut;
    }

    public void crossOut() {
        this.crossedOut = true;
    }

    public int getCellNumber() {
        return cellNumber;
    }

}
