/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nonograms;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Sergey
 */
public class Nonogram {

    private final Line[] rows, columns;
    private final Line[] allLines;

    private boolean isFirstAnalysis = true, wasFinalCheck = false;

    private final LinkedHashSet<Integer> lineNumbersToAnalyze = new LinkedHashSet<>();

    /**
     * Creates Nonogram from the file. First line - height. Second line - width.
     *
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Nonogram(File file) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        int height = Integer.parseInt(br.readLine());
        int width = Integer.parseInt(br.readLine());

        ArrayList<ArrayList<CellGroup>> rowCellGroups = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            ArrayList<CellGroup> numbers = new ArrayList<>();
            String[] strNumbers = br.readLine().split(" ");
            for (String strNumber : strNumbers) {
                numbers.add(new CellGroup(Integer.parseInt(strNumber)));
            }
            rowCellGroups.add(numbers);
        }

        ArrayList<ArrayList<CellGroup>> columnCellGroups = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            ArrayList<CellGroup> numbers = new ArrayList<>();
            String[] strNumbers = br.readLine().split(" ");
            for (String strNumber : strNumbers) {
                numbers.add(new CellGroup(Integer.parseInt(strNumber)));
            }
            columnCellGroups.add(numbers);
        }

        this.allLines = new Line[width + height];

        this.rows = new Line[height];
        for (int i = 0; i < height; i++) {
            this.allLines[i] = this.rows[i] = new Line(rowCellGroups.get(i), Line.LineType.ROW);
        }

        this.columns = new Line[width];
        for (int i = 0; i < width; i++) {
            this.allLines[height + i] = this.columns[i]
                    = new Line(columnCellGroups.get(i), Line.LineType.COLUMN);
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Cell c = new Cell();
                columns[i].addCell(c);
                rows[j].addCell(c);
            }
        }
    }

    public String oneStepAnalysis() {

        String description;
        if (isFirstAnalysis) {
            for (int i = 0; i < allLines.length; i++) {
                lineNumbersToAnalyze.add(i);
            }
            isFirstAnalysis = false;
        }

        if (!lineNumbersToAnalyze.isEmpty()) {
            Iterator<Integer> numbersIterator = lineNumbersToAnalyze.iterator();

            int currentLineNumber = numbersIterator.next();
            numbersIterator.remove();

            final Line currentLine = allLines[currentLineNumber];

            if (currentLine.getLineType() == Line.LineType.ROW) {
                description = "Анализ строки №" + (currentLineNumber + 1) + ".";
            } else {
                description = "Анализ столбца №" + (currentLineNumber - rows.length + 1) + ".";
            }

            String usedMethod = analyzeLine(currentLine);
            if (!usedMethod.isEmpty()) {
                lineNumbersToAnalyze.add(currentLineNumber);
            }

            description += usedMethod;
        } else {
            description = "Анализ окончен!";
        }
        return description;
    }

    private String analyzeLine(final Line currentLine) {
        Collection<Integer> newLineNumbers = currentLine.analyze();
        Collection<Integer> result = new LinkedHashSet<>();

        if (currentLine.getLineType() == Line.LineType.ROW) {
            for (Integer newLineNumber : newLineNumbers) {
                result.add(newLineNumber + rows.length);
            }
        } else {
            result = newLineNumbers;
        }

        lineNumbersToAnalyze.addAll(result);
        if (newLineNumbers.size() > 0) {
            return " Метод: " + currentLine.getLastUsedMethod();
        } else {
            return "";
        }
    }

    public BufferedImage getCurrentState() {

        final int CELL_SIZE = 20; //px
        final int TEXT_PADDING = CELL_SIZE / 5;

        int maxRowCellGroups = 0;
        for (Line row : rows) {
            if (row.getNumbers().size() > maxRowCellGroups) {
                maxRowCellGroups = row.getNumbers().size();
            }
        }

        int maxColumnCellGroups = 0;
        for (Line column : columns) {
            if (column.getNumbers().size() > maxColumnCellGroups) {
                maxColumnCellGroups = column.getNumbers().size();
            }
        }

        BufferedImage result = new BufferedImage(
                (columns.length + maxRowCellGroups + 1) * CELL_SIZE,
                (rows.length + maxColumnCellGroups + 1) * CELL_SIZE,
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = result.getGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, result.getWidth(), result.getHeight());

        g.setColor(Color.BLACK);
        int xCoord = maxRowCellGroups * CELL_SIZE, yCoord = maxColumnCellGroups * CELL_SIZE;
        //top cell groups
        for (int lineNum = 0; lineNum < columns.length; lineNum++) {
            ArrayList<CellGroup> numbersInverted = columns[lineNum].getNumbers();
            Collections.reverse(numbersInverted);

            for (int groupNum = 0; groupNum < numbersInverted.size(); groupNum++) {
                CellGroup group = numbersInverted.get(groupNum);
                g.drawString(String.valueOf(group.getCellNumber()),
                        xCoord + lineNum * CELL_SIZE + (TEXT_PADDING),
                        yCoord - groupNum * CELL_SIZE - (TEXT_PADDING));
                if (group.isCrossedOut()) {
                    g.drawLine(xCoord + lineNum * CELL_SIZE,
                            yCoord - groupNum * CELL_SIZE,
                            xCoord + lineNum * CELL_SIZE + CELL_SIZE,
                            yCoord - groupNum * CELL_SIZE - CELL_SIZE);
                }
            }

            Collections.reverse(numbersInverted);
        }

        //left cell groups
        for (int lineNum = 0; lineNum < rows.length; lineNum++) {
            ArrayList<CellGroup> numbersInverted = rows[lineNum].getNumbers();
            Collections.reverse(numbersInverted);

            for (int groupNum = 0; groupNum < numbersInverted.size(); groupNum++) {
                CellGroup group = numbersInverted.get(groupNum);
                g.drawString(String.valueOf(group.getCellNumber()),
                        xCoord - (groupNum + 1) * CELL_SIZE + (TEXT_PADDING),
                        yCoord + (lineNum + 1) * CELL_SIZE - (TEXT_PADDING));
                if (group.isCrossedOut()) {
                    g.drawLine(xCoord - groupNum * CELL_SIZE,
                            yCoord + lineNum * CELL_SIZE,
                            xCoord - groupNum * CELL_SIZE - CELL_SIZE,
                            yCoord + lineNum * CELL_SIZE + CELL_SIZE);
                }
            }

            Collections.reverse(numbersInverted);
        }

        //bottom numbers
        xCoord = maxRowCellGroups * CELL_SIZE;
        yCoord = (maxColumnCellGroups + rows.length + 1) * CELL_SIZE;
        for (int i = 0; i < columns.length; i++) {
            g.drawString(String.valueOf(i + 1), xCoord + i * CELL_SIZE + TEXT_PADDING, yCoord - TEXT_PADDING);
        }

        //left numbers
        xCoord = (maxRowCellGroups + columns.length) * CELL_SIZE;
        yCoord = (maxColumnCellGroups + 1) * CELL_SIZE;
        for (int i = 0; i < rows.length; i++) {
            g.drawString(String.valueOf(i + 1), xCoord + TEXT_PADDING, yCoord + i * CELL_SIZE - TEXT_PADDING);
        }

        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < columns.length; x++) {
                switch (rows[y].getCells().get(x).getState()) {
                    case UNDEFINED:
                        g.setColor(Color.WHITE);
                        g.fillRect((x + maxRowCellGroups) * CELL_SIZE + 1,
                                (y + maxColumnCellGroups) * CELL_SIZE + 1,
                                CELL_SIZE - 1, CELL_SIZE - 1);
                        break;
                    case EMPTY:
                        g.setColor(Color.WHITE);
                        g.fillRect((x + maxRowCellGroups) * CELL_SIZE + 1,
                                (y + maxColumnCellGroups) * CELL_SIZE + 1,
                                CELL_SIZE - 1, CELL_SIZE - 1);
                        g.setColor(Color.BLACK);
                        g.drawLine((x + maxRowCellGroups) * CELL_SIZE + 1,
                                (y + maxColumnCellGroups) * CELL_SIZE + 1,
                                (x + maxRowCellGroups) * CELL_SIZE + CELL_SIZE - 1,
                                (y + maxColumnCellGroups) * CELL_SIZE + CELL_SIZE - 1);
                        g.drawLine((x + maxRowCellGroups) * CELL_SIZE + CELL_SIZE - 1,
                                (y + maxColumnCellGroups) * CELL_SIZE + 1,
                                (x + maxRowCellGroups) * CELL_SIZE + 1,
                                (y + maxColumnCellGroups) * CELL_SIZE + CELL_SIZE - 1);
                        break;
                    case FILLED:
                        g.setColor(Color.BLACK);
                        g.fillRect((x + maxRowCellGroups) * CELL_SIZE + 1,
                                (y + maxColumnCellGroups) * CELL_SIZE + 1,
                                CELL_SIZE - 1, CELL_SIZE - 1);
                        break;
                }
            }
        }
        try {
            ImageIO.write(result, "png", new File("result.png"));
        } catch (IOException ex) {
            Logger.getLogger(Nonogram.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
