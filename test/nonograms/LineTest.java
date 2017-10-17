/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nonograms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sergey
 */
public class LineTest {

    public LineTest() {
    }

    @Test
    public void TestOverlapBoundaries() {
        String test, result;
        Line testLine;

        test = "4|------";
        result = "4|--XX--";
        testLine = generateLineFromString(test);
        testLine.overlapBoundaries();
        assertEquals(testLine, generateLineFromString(result));

        test = "2 2|------";
        result = "2 2|-X--X-";
        testLine = generateLineFromString(test);
        testLine.overlapBoundaries();
        assertEquals(testLine, generateLineFromString(result));

        test = "/2 5 /2|XX/------/XX";
        result = "/2 5 /2|XX/-XXXX-/XX";
        testLine = generateLineFromString(test);
        testLine.overlapBoundaries();
        assertEquals(testLine, generateLineFromString(result));

        test = "/2 6 /2|XX/------/XX";
        result = "/2 /6 /2|XX/XXXXXX/XX";
        testLine = generateLineFromString(test);
        testLine.overlapBoundaries();
        assertEquals(testLine, generateLineFromString(result));

        test = "/2 3 1 /2|XX/------/XX";
        result = "/2 3 1 /2|XX/-XX---/XX";
        testLine = generateLineFromString(test);
        testLine.overlapBoundaries();
        assertEquals(testLine, generateLineFromString(result));

        test = "2 3 1|--------";
        result = "/2 /3 /1|XX/XXX/X";
        testLine = generateLineFromString(test);
        testLine.overlapBoundaries();
        assertEquals(testLine, generateLineFromString(result));
    }

    @Test
    public void TestFillCellsJumpingFromWalls() {
        String test, result;
        Line testLine;

        test = "4|-XX---";
        result = "4|-XXX--";
        testLine = generateLineFromString(test);
        testLine.jumpFromWalls();
        assertEquals(testLine, generateLineFromString(result));

        test = "4|/XX---";
        result = "/4|/XXXX/";
        testLine = generateLineFromString(test);
        testLine.jumpFromWalls();
        assertEquals(testLine, generateLineFromString(result));

        test = "4|-/XX--";
        result = "4|-/XX--";
        testLine = generateLineFromString(test);
        testLine.jumpFromWalls();
        assertEquals(testLine, generateLineFromString(result));

        test = "2 2|-XX/-X";
        result = "2 /2|-XX/XX";
        testLine = generateLineFromString(test);
        testLine.jumpFromWalls();
        assertEquals(testLine, generateLineFromString(result));

        test = "4|--/--X-";
        result = "4|--/XXX-";
        testLine = generateLineFromString(test);
        testLine.jumpFromWalls();
        assertEquals(testLine, generateLineFromString(result));

        test = "1 1|/-//---/";
        result = "1 1|/-//---/";
        testLine = generateLineFromString(test);
        testLine.jumpFromWalls();
        assertEquals(testLine, generateLineFromString(result));

        test = "4 1 5|--XX-----XXX---";
        result = "4 1 5|--XX-----XXX---";
        testLine = generateLineFromString(test);
        testLine.jumpFromWalls();
        assertEquals(testLine, generateLineFromString(result));
    }

    @Test
    public void TestExludeTooSmallGaps() {
        String test, result;
        Line testLine;

        test = "4|-/XX--";
        result = "4|//XX--";
        testLine = generateLineFromString(test);
        testLine.exludeTooSmallEmptyGaps();
        assertEquals(testLine, generateLineFromString(result));

        test = "/1 4|X/--/XX--";
        result = "/1 4|X////XX--";
        testLine = generateLineFromString(test);
        testLine.exludeTooSmallEmptyGaps();
        assertEquals(testLine, generateLineFromString(result));

        test = "4|--XX/-";
        result = "4|--XX//";
        testLine = generateLineFromString(test);
        testLine.exludeTooSmallEmptyGaps();
        assertEquals(testLine, generateLineFromString(result));

        test = "4|--XX/---";
        result = "4|--XX////";
        testLine = generateLineFromString(test);
        testLine.exludeTooSmallEmptyGaps();
        assertEquals(testLine, generateLineFromString(result));

        test = "4|----/--/--";
        result = "4|----//////";
        testLine = generateLineFromString(test);
        testLine.exludeTooSmallEmptyGaps();
        assertEquals(testLine, generateLineFromString(result));

        test = "2|-/--/---/-/-";
        result = "2|//--/---////";
        testLine = generateLineFromString(test);
        testLine.exludeTooSmallEmptyGaps();
        assertEquals(testLine, generateLineFromString(result));
    }

    @Test
    public void TestExludeUnreachableBorderCells() {
        String test, result;
        Line testLine;

        test = "3|---XX-";
        result = "3|//-XX-";
        testLine = generateLineFromString(test);
        testLine.analyzeInaccessibility();
        assertEquals(testLine, generateLineFromString(result));

        test = "2|----X---";
        result = "2|///-X-//";
        testLine = generateLineFromString(test);
        testLine.analyzeInaccessibility();
        assertEquals(testLine, generateLineFromString(result));

        test = "/1 3|X//--XX-";
        result = "/1 3|X///-XX-";
        testLine = generateLineFromString(test);
        testLine.analyzeInaccessibility();
        assertEquals(testLine, generateLineFromString(result));

        test = "2|--XX----";
        result = "/2|//XX////";
        testLine = generateLineFromString(test);
        testLine.analyzeInaccessibility();
        assertEquals(testLine, generateLineFromString(result));

        test = "2 2|---X--X--";
        result = "2 2|---X--X-/";
        testLine = generateLineFromString(test);
        testLine.analyzeInaccessibility();
        assertEquals(testLine, generateLineFromString(result));

        test = "2 1|---XX--X-";
        result = "/2 /1|///XX//X/";
        testLine = generateLineFromString(test);
        testLine.analyzeInaccessibility();
        assertEquals(testLine, generateLineFromString(result));
    }

    private Line generateLineFromString(String strLine) {
        String[] lineParts = strLine.split("\\|");

        ArrayList<CellGroup> groups = new ArrayList<>();
        String[] groupsStr = lineParts[0].split(" ");
        for (String string : groupsStr) {
            CellGroup cellGroup;
            if (string.startsWith("/")) {
                cellGroup = new CellGroup(Integer.parseInt(string.substring(1, string.length())));
                cellGroup.crossOut();
            } else {
                cellGroup = new CellGroup(Integer.parseInt(string));
            }
            groups.add(cellGroup);
        }

        Line resultLine = new Line(groups);
        for (char cell : lineParts[1].toCharArray()) {
            Cell c = new Cell();
            switch (cell) {
                case '-':
                    c.setState(CellState.UNDEFINED);
                    resultLine.addCell(c);
                    break;
                case '/':
                    c.setState(CellState.EMPTY);
                    resultLine.addCell(c);
                    break;
                case 'X':
                    c.setState(CellState.FILLED);
                    resultLine.addCell(c);
                    break;
            }
        }
        return resultLine;

    }
}
