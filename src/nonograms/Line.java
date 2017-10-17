/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nonograms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;

/**
 *
 *
 * @author Sergey
 */
public class Line {

    public enum LineType {
        ROW, COLUMN
    }

    private final ArrayList<Cell> cells = new ArrayList<>();
    private final ArrayList<CellGroup> cellGroups;

    private final LineType lineType;
    private boolean isReversed = false;
    private String lastUsedMethod = "undefined";
    private final LinkedHashSet<Integer> modifiedCellNumbers = new LinkedHashSet<>();

    /**
     * Creates a Line with given cell group numbers and its type COLUMN or ROW.
     * Cells are added later with method
     * {@link #addCell(nonograms.Cell) addCell}.
     *
     * @param cellGroups
     * @param lineType
     */
    public Line(ArrayList<CellGroup> cellGroups, LineType lineType) {
        this.cellGroups = cellGroups;
        this.lineType = lineType;
    }

    /**
     * Creates a Line with given cell group numbers and its default type - ROW.
     * Cells are added later with method
     * {@link #addCell(nonograms.Cell) addCell}.
     *
     * @param cellGroups
     */
    public Line(ArrayList<CellGroup> cellGroups) {
        this.cellGroups = cellGroups;
        this.lineType = LineType.ROW;
    }

    /**
     * Adds a cell to the line. Type of cell can be set before adding it.
     *
     * @param cell
     */
    public void addCell(Cell cell) {
        cells.add(cell);
    }

    public ArrayList<CellGroup> getNumbers() {
        return cellGroups;
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public LineType getLineType() {
        return lineType;
    }

    public String getLastUsedMethod() {
        return lastUsedMethod;
    }

    /**
     * Analyzes current line and returns unique numbers of modified cells. It's
     * needed for further analysis.
     *
     * @return unique numbers of modified cells
     */
    public HashSet<Integer> analyze() {
        modifiedCellNumbers.clear();

        int modifiedCellsNumBefore = modifiedCellNumbers.size();

        assureCrossedOutGroups();
        assureNoUndefinedCells();

        overlapBoundaries();
        if (modifiedCellNumbers.size() > modifiedCellsNumBefore) {
            lastUsedMethod = "Пересечение крайних границ";
            return modifiedCellNumbers;
        }

        jumpFromWalls();
        if (modifiedCellNumbers.size() > modifiedCellsNumBefore) {
            lastUsedMethod = "Отталкивание от стен";
            return modifiedCellNumbers;
        }

        exludeTooSmallEmptyGaps();
        if (modifiedCellNumbers.size() > modifiedCellsNumBefore) {
            lastUsedMethod = "Не помещается";
            return modifiedCellNumbers;
        }

        analyzeInaccessibility();
        if (modifiedCellNumbers.size() > modifiedCellsNumBefore) {
            lastUsedMethod = "Недосягаемость";
            return modifiedCellNumbers;
        }

        return modifiedCellNumbers;
    }

    /**
     * Searched for filled cell groups that aren't crossed out and cross them
     * out.
     */
    private void assureCrossedOutGroups() {
        for (int i = 0; i < 2; i++) {

            Iterator<CellGroup> cellGroupsIter = cellGroups.iterator();
            Iterator<NonEmptyCellGap> cellGapsIter = findNonEmptyCellGaps().iterator();

            while (cellGroupsIter.hasNext() && cellGapsIter.hasNext()) {
                NonEmptyCellGap gap = cellGapsIter.next();
                CellGroup group = cellGroupsIter.next();

                if (gap.isFilled()) {
                    group.crossOut();
                } else {
                    break;
                }
            }

            reverseCellsAndGroups();
        }
    }

    /**
     * Searched for filled cell groups that aren't crossed out and cross them
     * out.
     */
    private void assureNoUndefinedCells() {
        if (cellGroups.stream().allMatch(cellGroup -> cellGroup.isCrossedOut())) {
            fillUndefinedCellsWithEmpty();
        }
    }

    /**
     * Line has a cell group, which difference between left and right bounds is
     * less than number of this group, so there is overlapping region of
     * possible filled cells.
     */
    void overlapBoundaries() {

        ArrayList<NonEmptyCellGap> nonEmptyCellGaps = findNonEmptyCellGaps();
        nonEmptyCellGaps.removeIf(cellGap -> cellGap.isFilled());

        NonEmptyCellGap currentWorkingGap;
        if (nonEmptyCellGaps.size() != 1) {
            return;
        } else {
            currentWorkingGap = nonEmptyCellGaps.get(0);
        }

        ArrayList<CellGroup> nonCrossedOutCellGroups = new ArrayList<>(cellGroups);
        nonCrossedOutCellGroups.removeIf(cellGroup -> cellGroup.isCrossedOut());

        final int MIN_SPACE_BETWEEN_CELLS = 1;
        boolean areGroupsCrossedOut = false;

        for (int i = 0; i < nonCrossedOutCellGroups.size(); i++) {
            int boundFromLeft = 0;
            for (int j = 0; j < i; j++) {
                boundFromLeft += nonCrossedOutCellGroups.get(j).getCellNumber();
                boundFromLeft += MIN_SPACE_BETWEEN_CELLS;
            }
            boundFromLeft += nonCrossedOutCellGroups.get(i).getCellNumber();

            int boundFromRight = 0;
            for (int j = nonCrossedOutCellGroups.size() - 1; j > i; j--) {
                boundFromRight += nonCrossedOutCellGroups.get(j).getCellNumber();
                boundFromRight += MIN_SPACE_BETWEEN_CELLS;
            }
            boundFromRight += nonCrossedOutCellGroups.get(i).getCellNumber();

            /*converting length to cells' indexes*/
            boundFromLeft = boundFromLeft + currentWorkingGap.getStartIndex() - 1;
            boundFromRight = cells.size() - (boundFromRight + (cells.size() - (currentWorkingGap.getEndIndex() + 1)));

            final int boundsDifference = boundFromLeft - boundFromRight;

            if (boundsDifference >= 0) {
                fillCells(boundFromRight, boundFromLeft, CellState.FILLED);
                if (nonCrossedOutCellGroups.get(i).getCellNumber() == boundsDifference + 1) {
                    nonCrossedOutCellGroups.get(i).crossOut();
                    areGroupsCrossedOut = true;
                }
            }
        }
        if (areGroupsCrossedOut) {
            for (int i = currentWorkingGap.getStartIndex(); i <= currentWorkingGap.getEndIndex(); i++) {
                if (cells.get(i).getState() == CellState.UNDEFINED) {
                    fillCell(i, CellState.EMPTY);
                }
            }
        }
    }

    /**
     * Try to fill cells "jumping from walls".
     */
    void jumpFromWalls() {

        for (int i = 0; i < 2; i++) {

            ArrayList<NonEmptyCellGap> nonEmptyGaps = findNonEmptyCellGaps();
            NonEmptyCellGap gap = null;
            for (NonEmptyCellGap nonEmptyGap : nonEmptyGaps) {
                if (!nonEmptyGap.isFilled()) {
                    gap = nonEmptyGap;
                    break;
                }
            }

            CellGroup group = null;
            for (CellGroup cellGroup : cellGroups) {
                if (!cellGroup.isCrossedOut()) {
                    group = cellGroup;
                    break;
                }
            }

            if (gap == null || group == null) {
                continue;
            }

            if (gap.getSize() >= group.getCellNumber()) {
                int firstFilledCellIndex = gap.getFirstFilledCellIndex();
                if (firstFilledCellIndex != NonEmptyCellGap.NO_FILLED_CELL) {
                    int lastIndexOfFilling = gap.getStartIndex() + group.getCellNumber() - 1;
                    fillCells(firstFilledCellIndex, lastIndexOfFilling, CellState.FILLED);
                    if (firstFilledCellIndex == gap.getStartIndex()) {
                        group.crossOut();
                        if (lastIndexOfFilling < cells.size() - 1) {
                            fillCell(lastIndexOfFilling + 1, CellState.EMPTY);
                        }
                    }
                }
            }
            /*change direction*/
            reverseCellsAndGroups();
        }

        if (isReversed) {
            reverseCellsAndGroups();
        }
    }

    void exludeTooSmallEmptyGaps() {

        long nonCrossedOutCellGroups
                = cellGroups.stream().filter((CellGroup t) -> !t.isCrossedOut()).count();
        if (nonCrossedOutCellGroups == 1) {

            CellGroup group = null;
            for (CellGroup cellGroup : cellGroups) {
                if (!cellGroup.isCrossedOut()) {
                    group = cellGroup;
                    break;
                }
            }

            ArrayList<NonEmptyCellGap> nonEmptyGaps = findNonEmptyCellGaps();
            for (NonEmptyCellGap gap : nonEmptyGaps) {
                if (!gap.isFilled() && gap.getSize() < group.getCellNumber()) {
                    fillCells(gap.getStartIndex(), gap.getEndIndex(), CellState.EMPTY);
                }
            }
            return;
        }

        //going from the left
        for (int i = 0; i < 2; i++) {
            ArrayList<NonEmptyCellGap> nonEmptyGaps = findNonEmptyCellGaps();

            CellGroup group = null;
            for (CellGroup cellGroup : cellGroups) {
                if (!cellGroup.isCrossedOut()) {
                    group = cellGroup;
                    break;
                }
            }

            NonEmptyCellGap gap = null;
            for (NonEmptyCellGap nonEmptyGap : nonEmptyGaps) {
                if (nonEmptyGap.isUndefined()) {
                    gap = nonEmptyGap;
                }
                break;
            }

            if (gap == null || group == null) {
                continue;
            }

            if (gap.getSize() < group.getCellNumber()) {
                fillCells(gap.getStartIndex(), gap.getEndIndex(), CellState.EMPTY);
            }
            /*change direction*/
            reverseCellsAndGroups();
        }

        if (isReversed) {
            reverseCellsAndGroups();
        }
    }

    void analyzeInaccessibility() {

        //going from the left and then change direction to the right
        for (int i = 0; i < 2; i++) {

            ArrayList<CellGroup> nonCrossedOutGroups = new ArrayList<>(cellGroups);
            nonCrossedOutGroups.removeIf(cellGroup -> cellGroup.isCrossedOut());

            if (nonCrossedOutGroups.size() == 1) {
                ArrayList<NonEmptyCellGap> nonEmptyCellGaps = findNonEmptyCellGaps();
                nonEmptyCellGaps.removeIf(cellGap -> cellGap.isFilled());

                if (nonEmptyCellGaps.stream().anyMatch(cellGap -> !cellGap.isUndefined())) {
                    for (Iterator<NonEmptyCellGap> iterator = nonEmptyCellGaps.iterator(); iterator.hasNext();) {
                        NonEmptyCellGap cellGap = iterator.next();
                        if (cellGap.isUndefined()) {
                            fillCells(cellGap.getStartIndex(), cellGap.getEndIndex(), CellState.EMPTY);
                            iterator.remove();
                        }
                    }

                    NonEmptyCellGap currentGap = nonEmptyCellGaps.get(0);
                    CellGroup currentCellGroup = nonCrossedOutGroups.get(0);
                    int firstFilledCell = currentGap.getFirstFilledCellIndex();
                    int lastFilledCell = currentGap.getLastFilledCellIndex();

                    if (lastFilledCell - firstFilledCell + 1 == currentCellGroup.getCellNumber()) {
                        fillCells(firstFilledCell, lastFilledCell, CellState.FILLED);
                        fillUndefinedCellsWithEmpty();
                        currentCellGroup.crossOut();
                    } else {
                        fillCells(currentGap.getStartIndex(), lastFilledCell - currentCellGroup.getCellNumber(),
                                CellState.EMPTY);
                        fillCells(firstFilledCell + currentCellGroup.getCellNumber(), currentGap.getEndIndex(),
                                CellState.EMPTY);
                    }

                } else {
                    return;
                }
            } else {
                CellGroup group = null;
                for (CellGroup cellGroup : cellGroups) {
                    if (!cellGroup.isCrossedOut()) {
                        group = cellGroup;
                        break;
                    }
                }

                ArrayList<NonEmptyCellGap> nonEmptyGaps = findNonEmptyCellGaps();

                NonEmptyCellGap gap = null;
                for (NonEmptyCellGap nonEmptyGap : nonEmptyGaps) {
                    if (!nonEmptyGap.isFilled()) {
                        gap = nonEmptyGap;
                        break;
                    }
                }

                if (gap == null || group == null) {
                    reverseCellsAndGroups();
                    continue;
                }

                int firstFilledCell = gap.getFirstFilledCellIndex();
                if (firstFilledCell != NonEmptyCellGap.NO_FILLED_CELL) {
                    int endOfFilledGroup = firstFilledCell;
                    for (int j = firstFilledCell; j < gap.getEndIndex(); j++) {
                        Cell cell = cells.get(j);
                        if (cell.getState() != CellState.FILLED) {
                            endOfFilledGroup = j - 1;
                            break;
                        }
                    }
                    int filledGroupSize = endOfFilledGroup - firstFilledCell + 1;

                    boolean hasExtraEmptySpace = endOfFilledGroup > (group.getCellNumber() - 1);
                    if (hasExtraEmptySpace) {

                        int emptySpaceBeforeGroup = (firstFilledCell - gap.getStartIndex());
                        if (emptySpaceBeforeGroup > group.getCellNumber()) {

                            ArrayList<CellGroup> cellGroupsSorted = new ArrayList<>(cellGroups);
                            cellGroupsSorted.removeIf(cellGroup -> cellGroup.isCrossedOut());
                            cellGroupsSorted.sort((CellGroup cg1, CellGroup cg2) -> {
                                return Integer.compare(cg1.getCellNumber(), cg2.getCellNumber());
                            });
                            Collections.reverse(cellGroupsSorted);

                            if (!cellGroupsSorted.get(0).equals(group)
                                    || filledGroupSize <= cellGroupsSorted.get(1).getCellNumber()) {
                                reverseCellsAndGroups();
                                continue;
                            }
                        }
                        fillCells(gap.getStartIndex(), endOfFilledGroup - group.getCellNumber(), CellState.EMPTY);
                        if (filledGroupSize == group.getCellNumber()) {
                            group.crossOut();
                            if (endOfFilledGroup + 1 < cells.size()) {
                                fillCell(endOfFilledGroup + 1, CellState.EMPTY);
                            }
                        }
                    }
                }
                /*change direction*/
                reverseCellsAndGroups();
            }
        }

        if (isReversed) {
            reverseCellsAndGroups();
        }
    }

    /**
     * Stores parameters of non-empty gap.
     */
    private class NonEmptyCellGap {

        public static final int NO_FILLED_CELL = -1;
        private final int startIndex, endIndex;

        public NonEmptyCellGap(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public int getSize() {
            return endIndex - startIndex + 1;
        }

        /**
         * Checks whether the group has ONLY filled cells.
         *
         * @return
         */
        public boolean isFilled() {
            for (int i = startIndex; i <= endIndex; i++) {
                if (cells.get(i).getState() != CellState.FILLED) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Checks whether the group has ONLY undefined cells.
         *
         * @return
         */
        public boolean isUndefined() {
            for (int i = startIndex; i <= endIndex; i++) {
                if (cells.get(i).getState() != CellState.UNDEFINED) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Gets index of first filled cell. If there are no filled cells return
         * -1.
         *
         * @return
         */
        public int getFirstFilledCellIndex() {
            int result = NO_FILLED_CELL;
            for (int i = startIndex; i <= endIndex; i++) {
                Cell get = cells.get(i);
                if (get.getState() == CellState.FILLED) {
                    result = i;
                    break;
                }
            }
            return result;
        }

        /**
         * Gets index of last filled cell. If there are no filled cells return
         * -1.
         *
         * @return
         */
        public int getLastFilledCellIndex() {
            int result = NO_FILLED_CELL;
            for (int i = endIndex; i >= startIndex; i--) {
                Cell get = cells.get(i);
                if (get.getState() == CellState.FILLED) {
                    result = i;
                    break;
                }
            }
            return result;
        }
    }

    private ArrayList<NonEmptyCellGap> findNonEmptyCellGaps() {
        ArrayList<NonEmptyCellGap> resultGaps = new ArrayList<>();

        int startIndex = 0, endIndex;
        boolean isGapStarted = false;
        for (int i = 0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            CellState cellState = c.getState();

            if (!isGapStarted) {
                if (cellState != CellState.EMPTY) {
                    isGapStarted = true;
                    startIndex = i;
                }
            } else {
                if (cellState == CellState.EMPTY) {
                    endIndex = i - 1;
                    isGapStarted = false;
                    resultGaps.add(new NonEmptyCellGap(startIndex, endIndex));
                } else if (i == cells.size() - 1) {
                    endIndex = i;
                    isGapStarted = false;
                    resultGaps.add(new NonEmptyCellGap(startIndex, endIndex));
                }
            }
        }

        if (isGapStarted) {
            resultGaps.add(new NonEmptyCellGap(startIndex, cells.size() - 1));
        }

        return resultGaps;
    }

    private void fillCells(int startIndex, int endIndex, CellState state) {
        for (int i = startIndex; i <= endIndex && i < cells.size(); i++) {
            fillCell(i, state);
        }
    }

    private void fillCell(int index, CellState state) {
        if (cells.get(index).getState() != state) {
            cells.get(index).setState(state);
            if (!isReversed) {
                modifiedCellNumbers.add(index);
            } else {
                modifiedCellNumbers.add(cells.size() - (index + 1));
            }
        }
    }

    private void fillUndefinedCellsWithEmpty() {
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i).getState() == CellState.UNDEFINED) {
                fillCell(i, CellState.EMPTY);
            }
        }
    }

    private void reverseCellsAndGroups() {
        Collections.reverse(cells);
        Collections.reverse(cellGroups);

        isReversed = !isReversed;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.cells);
        hash = 73 * hash + Objects.hashCode(this.cellGroups);
        hash = 73 * hash + Objects.hashCode(this.lineType);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Line other = (Line) obj;
        if (cells.size() != other.getCells().size()) {
            return false;
        }
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i).getState() != other.getCells().get(i).getState()) {
                return false;
            }
        }
        for (int i = 0; i < cellGroups.size(); i++) {
            if (cellGroups.get(i).getCellNumber() != other.getNumbers().get(i).getCellNumber()
                    || cellGroups.get(i).isCrossedOut() != other.getNumbers().get(i).isCrossedOut()) {
                return false;
            }
        }

        return this.lineType == other.lineType;
    }

}
