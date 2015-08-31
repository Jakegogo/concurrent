/*
 * Copyright (C) 2015 Jack Jiang(cngeeker.com) The BeautyEye Project. 
 * All rights reserved.
 * Project URL:https://github.com/JackJiang2011/beautyeye
 * Version 3.6
 * 
 * Jack Jiang PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * BETableUI.java at 2015-2-1 20:25:41, original version by Jack Jiang.
 * You can contact author with jb2011@163.com.
 */
package basesource.convertor.ui.extended;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.model.ProgressTableModel;
import basesource.convertor.utils.SmoothUtilities;
import org.jb2011.lnf.beautyeye.ch5_table.BETableUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.table.*;
import java.awt.*;

// TODO: Auto-generated Javadoc
/**
 * JTable的进度显示UI实现类。.
 *
 * @author Jack Jiang(jb2011@163.com)
 */
public class RowProgressTableUI extends BETableUI
{
	
	/** The default renderers by column class. */
	UIDefaults defaultRenderersByColumnClass;
	
    /**
     * Creates the ui.
     *
     * @param c the c
     * @return the component ui
     */
    public static ComponentUI createUI(JComponent c) 
    {
        return new RowProgressTableUI();
    }
    
    /**
     * 更新进度显示
     * @param table JTable
     * @param startModelIndex 开始行 从0开始
     * @param endModelIndex 结束行
     */
    public static void updateProgressUI(JTable table, int startModelIndex, int endModelIndex) {
    	if (table.getUI().getClass() != RowProgressTableUI.class) {
    		return;
    	}
    	RowProgressTableUI ui = (RowProgressTableUI) table.getUI();
    	if (ui != null) {
    		ui.repaintRows(startModelIndex, endModelIndex);
    	}
    }


	/**
	 * 清理进度显示
	 * @param table JTable
	 */
	public static void clearProgressUI(JTable table) {
		table.repaint();
	}

    
    /**
     * 重画行
     */
    public void repaintRows(int startModelIndex, int endModelIndex) {
    	if (endModelIndex < 0) {
    		return;
    	}
        if (startModelIndex > endModelIndex) {
            // Too much has changed, punt
        	table.repaint();
            return;
        }
        int modelIndex = startModelIndex;
        
        int height = 0;
        Rectangle firstCellRectangle;
        int viewIndex = table.convertRowIndexToView(modelIndex++);
        if (viewIndex < 0) {
        	return;
        }
        firstCellRectangle = table.getCellRect(viewIndex, 0, true);
        height += firstCellRectangle.height;
        
        while (modelIndex <= endModelIndex) {
            viewIndex = table.convertRowIndexToView(modelIndex++);
            if (viewIndex < 0) {
            	Rectangle dirty = table.getCellRect(viewIndex, 0,
                        false);
            	height += dirty.height;
            }
        }
        table.repaint(firstCellRectangle.x, firstCellRectangle.y, table.getWidth(), height);
    }
    

	/** Paint a representation of the <code>table</code> instance
	 * that was set in installUI().
	 */
	public void paint(Graphics g, JComponent c) {
		Rectangle clip = g.getClipBounds();

		Rectangle bounds = table.getBounds();
		// account for the fact that the graphics has already been translated
		// into the table's bounds
		bounds.x = bounds.y = 0;

		if (table.getRowCount() <= 0 || table.getColumnCount() <= 0 ||
				// this check prevents us from painting the entire table
				// when the clip doesn't intersect our bounds at all
				!bounds.intersects(clip)) {

			paintDropLines(g);
			return;
		}

		boolean ltr = table.getComponentOrientation().isLeftToRight();

		Point upperLeft = clip.getLocation();
		if (!ltr) {
			upperLeft.x++;
		}

		Point lowerRight = new Point(clip.x + clip.width - (ltr ? 1 : 0),
				clip.y + clip.height);

		int rMin = table.rowAtPoint(upperLeft);
		int rMax = table.rowAtPoint(lowerRight);
		// This should never happen (as long as our bounds intersect the clip,
		// which is why we bail above if that is the case).
		if (rMin == -1) {
			rMin = 0;
		}
		// If the table does not have enough rows to fill the view we'll get -1.
		// (We could also get -1 if our bounds don't intersect the clip,
		// which is why we bail above if that is the case).
		// Replace this with the index of the last row.
		if (rMax == -1) {
			rMax = table.getRowCount()-1;
		}

		int cMin = table.columnAtPoint(ltr ? upperLeft : lowerRight);
		int cMax = table.columnAtPoint(ltr ? lowerRight : upperLeft);
		// This should never happen.
		if (cMin == -1) {
			cMin = 0;
		}
		// If the table does not have enough columns to fill the view we'll get -1.
		// Replace this with the index of the last column.
		if (cMax == -1) {
			cMax = table.getColumnCount()-1;
		}
		
		SmoothUtilities.configureGraphics(g);
		
		// Paint the grid.
		paintGrid(g, rMin, rMax, cMin, cMax);

		// Paint the cells.
		paintCells(g, rMin, rMax, cMin, cMax, bounds.width);

		paintDropLines(g);
	}


	private void paintCells(Graphics g, int rMin, int rMax, int cMin, int cMax, int rowWidth) {
		JTableHeader header = table.getTableHeader();
		TableColumn draggedColumn = (header == null) ? null : header.getDraggedColumn();

		TableColumnModel cm = table.getColumnModel();
		int columnMargin = cm.getColumnMargin();

		Rectangle cellRect;
		TableColumn aColumn;
		int columnWidth;
		if (table.getComponentOrientation().isLeftToRight()) {
			for(int row = rMin; row <= rMax; row++) {
				cellRect = table.getCellRect(row, cMin, false);
				for(int column = cMin; column <= cMax; column++) {
					aColumn = cm.getColumn(column);
					columnWidth = aColumn.getWidth();
					cellRect.width = columnWidth - columnMargin;
					if (aColumn != draggedColumn) {
						paintCell(g, cellRect, row, column, rowWidth);
					}
					cellRect.x += columnWidth;
				}
			}
		} else {
			for(int row = rMin; row <= rMax; row++) {
				cellRect = table.getCellRect(row, cMin, false);
				aColumn = cm.getColumn(cMin);
				if (aColumn != draggedColumn) {
					columnWidth = aColumn.getWidth();
					cellRect.width = columnWidth - columnMargin;
					paintCell(g, cellRect, row, cMin, rowWidth);
				}
				for(int column = cMin+1; column <= cMax; column++) {
					aColumn = cm.getColumn(column);
					columnWidth = aColumn.getWidth();
					cellRect.width = columnWidth - columnMargin;
					cellRect.x -= columnWidth;
					if (aColumn != draggedColumn) {
						paintCell(g, cellRect, row, column, rowWidth);
					}
				}
			}
		}

		// Paint the dragged column if we are dragging.
		if (draggedColumn != null) {
			paintDraggedArea(g, rMin, rMax, draggedColumn, header.getDraggedDistance(), rowWidth);
		}

		// Remove any renderers that may be left in the rendererPane.
		rendererPane.removeAll();
	}


	private void paintCell(Graphics g, Rectangle cellRect, int row, int column, int rowWidth) {
		if (table.isEditing() && table.getEditingRow()==row &&
				table.getEditingColumn()==column) {
			Component component = table.getEditorComponent();
			component.setBounds(cellRect);
			component.validate();
		}
		else {
			TableCellRenderer renderer = table.getCellRenderer(row, column);
			Component component = table.prepareRenderer(renderer, row, column);

			TableModel tableModel = table.getModel();
			if (tableModel.getClass() == ProgressTableModel.class) {
				ProgressTableModel progressTableModel = (ProgressTableModel) tableModel;
				int modelRow = table.convertRowIndexToModel(row);
				double progressPct = progressTableModel.getProgress(modelRow);
				int progress = (int) (Math.abs(progressPct) * rowWidth);
				if (progress > cellRect.x) {
					int w = progress - cellRect.x;
					w = w > cellRect.width ? cellRect.width : w;

					if (progressPct > 0) {
						g.setColor(DefaultUIConstant.TABLE_ROW_PROGRESS_BAR_COLOR1);
					} else {
						g.setColor(DefaultUIConstant.TABLE_ROW_PROGRESS_BAR_COLOR3);
					}

					int h = DefaultUIConstant.TABLE_ROW_PROGRESS_BAR_HEIGHT;
					h = h <= 0 ? cellRect.height : h;
					int y = cellRect.y + cellRect.height - h;

					g.fillRect(cellRect.x, y, w, h);
				}
			}

			rendererPane.paintComponent(g, component, table, cellRect.x, cellRect.y,
					cellRect.width, cellRect.height, true);
		}
	}


	/*
         * Paints the grid lines within <I>aRect</I>, using the grid
         * color set with <I>setGridColor</I>. Paints vertical lines
         * if <code>getShowVerticalLines()</code> returns true and paints
         * horizontal lines if <code>getShowHorizontalLines()</code>
         * returns true.
         */
	private void paintGrid(Graphics g, int rMin, int rMax, int cMin, int cMax) {
		g.setColor(table.getGridColor());

		Rectangle minCell = table.getCellRect(rMin, cMin, true);
		Rectangle maxCell = table.getCellRect(rMax, cMax, true);
		Rectangle damagedArea = minCell.union( maxCell );

		boolean showprogress = false;
		ProgressTableModel progressTableModel = null;
		TableModel tableModel = table.getModel();
		if (tableModel.getClass() == ProgressTableModel.class) {
			progressTableModel = (ProgressTableModel) tableModel;
			showprogress = true;
		}

		if (table.getShowHorizontalLines()) {
			int tableWidth = damagedArea.x + damagedArea.width;
			int y = damagedArea.y;
			for (int row = rMin; row <= rMax; row++) {
				y += table.getRowHeight(row);

				if (showprogress) {
					int modelRow = table.convertRowIndexToModel(row);
					double progressPct = progressTableModel.getProgress(modelRow);
					int progress = (int) (Math.abs(progressPct) * tableWidth);

					if (progressPct > 0) {
						g.setColor(DefaultUIConstant.TABLE_ROW_PROGRESS_BAR_COLOR2);
					} else {
						g.setColor(DefaultUIConstant.TABLE_ROW_PROGRESS_BAR_COLOR4);
					}
					g.drawLine(damagedArea.x, y - 1, progress, y - 1);

					g.setColor(table.getGridColor());
					g.drawLine(damagedArea.x + progress, y - 1, tableWidth - 1, y - 1);

				} else {
					g.drawLine(damagedArea.x, y - 1, tableWidth - 1, y - 1);
				}

			}
		}
		if (table.getShowVerticalLines()) {
			TableColumnModel cm = table.getColumnModel();
			int tableHeight = damagedArea.y + damagedArea.height;
			int x;
			if (table.getComponentOrientation().isLeftToRight()) {
				x = damagedArea.x;
				for (int column = cMin; column <= cMax; column++) {
					int w = cm.getColumn(column).getWidth();
					x += w;
					g.drawLine(x - 1, 0, x - 1, tableHeight - 1);
				}
			} else {
				x = damagedArea.x;
				for (int column = cMax; column >= cMin; column--) {
					int w = cm.getColumn(column).getWidth();
					x += w;
					g.drawLine(x - 1, 0, x - 1, tableHeight - 1);
				}
			}
		}
	}


	private void paintDraggedArea(Graphics g, int rMin, int rMax, TableColumn draggedColumn, int distance, int rowWidth) {
		int draggedColumnIndex = viewIndexForColumn(draggedColumn);

		Rectangle minCell = table.getCellRect(rMin, draggedColumnIndex, true);
		Rectangle maxCell = table.getCellRect(rMax, draggedColumnIndex, true);

		Rectangle vacatedColumnRect = minCell.union(maxCell);

		// Paint a gray well in place of the moving column.
		g.setColor(table.getParent().getBackground());
		g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
				vacatedColumnRect.width, vacatedColumnRect.height);

		// Move to the where the cell has been dragged.
		vacatedColumnRect.x += distance;

		// Fill the background.
		g.setColor(table.getBackground());
		g.fillRect(vacatedColumnRect.x, vacatedColumnRect.y,
				vacatedColumnRect.width, vacatedColumnRect.height);

		// Paint the vertical grid lines if necessary.
		if (table.getShowVerticalLines()) {
			g.setColor(table.getGridColor());
			int x1 = vacatedColumnRect.x;
			int y1 = vacatedColumnRect.y;
			int x2 = x1 + vacatedColumnRect.width - 1;
			int y2 = y1 + vacatedColumnRect.height - 1;
			// Left
			g.drawLine(x1-1, y1, x1-1, y2);
			// Right
			g.drawLine(x2, y1, x2, y2);
		}

		for(int row = rMin; row <= rMax; row++) {
			// Render the cell value
			Rectangle r = table.getCellRect(row, draggedColumnIndex, false);
			r.x += distance;
			paintCell(g, r, row, draggedColumnIndex, rowWidth);

			// Paint the (lower) horizontal grid line if necessary.
			if (table.getShowHorizontalLines()) {
				g.setColor(table.getGridColor());
				Rectangle rcr = table.getCellRect(row, draggedColumnIndex, true);
				rcr.x += distance;
				int x1 = rcr.x;
				int y1 = rcr.y;
				int x2 = x1 + rcr.width - 1;
				int y2 = y1 + rcr.height - 1;
				g.drawLine(x1, y2, x2, y2);
			}
		}
	}


	private void paintDropLines(Graphics g) {
		JTable.DropLocation loc = table.getDropLocation();
		if (loc == null) {
			return;
		}

		Color color = UIManager.getColor("Table.dropLineColor");
		Color shortColor = UIManager.getColor("Table.dropLineShortColor");
		if (color == null && shortColor == null) {
			return;
		}

		Rectangle rect;

		rect = getHDropLineRect(loc);
		if (rect != null) {
			int x = rect.x;
			int w = rect.width;
			if (color != null) {
				extendRect(rect, true);
				g.setColor(color);
				g.fillRect(rect.x, rect.y, rect.width, rect.height);
			}
			if (!loc.isInsertColumn() && shortColor != null) {
				g.setColor(shortColor);
				g.fillRect(x, rect.y, w, rect.height);
			}
		}

		rect = getVDropLineRect(loc);
		if (rect != null) {
			int y = rect.y;
			int h = rect.height;
			if (color != null) {
				extendRect(rect, false);
				g.setColor(color);
				g.fillRect(rect.x, rect.y, rect.width, rect.height);
			}
			if (!loc.isInsertRow() && shortColor != null) {
				g.setColor(shortColor);
				g.fillRect(rect.x, y, rect.width, h);
			}
		}
	}

	private Rectangle getHDropLineRect(JTable.DropLocation loc) {
		if (!loc.isInsertRow()) {
			return null;
		}

		int row = loc.getRow();
		int col = loc.getColumn();
		if (col >= table.getColumnCount()) {
			col--;
		}

		Rectangle rect = table.getCellRect(row, col, true);

		if (row >= table.getRowCount()) {
			row--;
			Rectangle prevRect = table.getCellRect(row, col, true);
			rect.y = prevRect.y + prevRect.height;
		}

		if (rect.y == 0) {
			rect.y = -1;
		} else {
			rect.y -= 2;
		}

		rect.height = 3;

		return rect;
	}

	private Rectangle getVDropLineRect(JTable.DropLocation loc) {
		if (!loc.isInsertColumn()) {
			return null;
		}

		boolean ltr = table.getComponentOrientation().isLeftToRight();
		int col = loc.getColumn();
		Rectangle rect = table.getCellRect(loc.getRow(), col, true);

		if (col >= table.getColumnCount()) {
			col--;
			rect = table.getCellRect(loc.getRow(), col, true);
			if (ltr) {
				rect.x = rect.x + rect.width;
			}
		} else if (!ltr) {
			rect.x = rect.x + rect.width;
		}

		if (rect.x == 0) {
			rect.x = -1;
		} else {
			rect.x -= 2;
		}

		rect.width = 3;

		return rect;
	}


	private Rectangle extendRect(Rectangle rect, boolean horizontal) {
		if (rect == null) {
			return rect;
		}

		if (horizontal) {
			rect.x = 0;
			rect.width = table.getWidth();
		} else {
			rect.y = 0;

			if (table.getRowCount() != 0) {
				Rectangle lastRect = table.getCellRect(table.getRowCount() - 1, 0, true);
				rect.height = lastRect.y + lastRect.height;
			} else {
				rect.height = table.getHeight();
			}
		}

		return rect;
	}


	private int viewIndexForColumn(TableColumn aColumn) {
		TableColumnModel cm = table.getColumnModel();
		for (int column = 0; column < cm.getColumnCount(); column++) {
			if (cm.getColumn(column) == aColumn) {
				return column;
			}
		}
		return -1;
	}

}
