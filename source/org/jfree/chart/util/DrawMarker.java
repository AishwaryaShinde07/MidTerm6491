package org.jfree.chart.util;



import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.text.TextUtilities;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

public class DrawMarker {
	
    /**
     * Calculates the (x, y) coordinates for drawing a marker label.
     *
     * @param g2  the graphics device.
     * @param orientation  the plot orientation.
     * @param dataArea  the data area.
     * @param markerArea  the rectangle surrounding the marker.
     * @param markerOffset  the marker offset.
     * @param labelOffsetType  the label offset type.
     * @param anchor  the label anchor.
     *
     * @return The coordinates for drawing the marker label.
     */
    private Point2D calculateRangeMarkerTextAnchorPoint(Graphics2D g2,
            PlotOrientation orientation, Rectangle2D dataArea,
            Rectangle2D markerArea, RectangleInsets markerOffset,
            LengthAdjustmentType labelOffsetType, RectangleAnchor anchor) {

        Rectangle2D anchorRect = anchorRect(orientation, markerArea, markerOffset, labelOffsetType);
		return RectangleAnchor.coordinates(anchorRect, anchor);

    }

	private Rectangle2D anchorRect(PlotOrientation orientation, Rectangle2D markerArea, RectangleInsets markerOffset,
			LengthAdjustmentType labelOffsetType) {
		Rectangle2D anchorRect = null;
		if (orientation == PlotOrientation.HORIZONTAL) {
			anchorRect = markerOffset.createAdjustedRectangle(markerArea, labelOffsetType,
					LengthAdjustmentType.CONTRACT);
		} else if (orientation == PlotOrientation.VERTICAL) {
			anchorRect = markerOffset.createAdjustedRectangle(markerArea, LengthAdjustmentType.CONTRACT,
					labelOffsetType);
		}
		return anchorRect;
	}
    
    @FunctionalInterface
	public interface Interface3 {
		Point2D apply(PlotOrientation orientation, Line2D line, RectangleAnchor anchor);
	}

	@FunctionalInterface
	public interface Interface4 {
		Point2D apply(PlotOrientation orientation, Rectangle2D rect, RectangleAnchor anchor);
	}
   
	public void drawDomainRangeMarker(Marker marker, ValueAxis domainAxis, Plot plot, Rectangle2D dataArea,
			Graphics2D g2, RectangleEdge arg0, PlotOrientation arg1, PlotOrientation arg2, Interface3 arg3,
			Interface4 arg4) {
 if (plot instanceof XYPlot)
 {
	 
 
		if (marker instanceof ValueMarker) {
			Line2D line = createLineForValueMarkerInXYPlot(marker, domainAxis, plot, dataArea, arg0, arg1, arg2);
			ValueMarker vm = (ValueMarker) marker;
			double value = vm.getValue();
			Range range = domainAxis.getRange();
			if (!range.contains(value)) {
				return;
			}
			PlotOrientation orientation = ((XYPlot) plot).getOrientation();
			final Composite originalComposite = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, marker.getAlpha()));
			g2.setPaint(marker.getPaint());
			g2.setStroke(marker.getStroke());
			g2.draw(line);
			String label = marker.getLabel();
			RectangleAnchor anchor = marker.getLabelAnchor();
			if (label != null) {
				Font labelFont = marker.getLabelFont();
				g2.setFont(labelFont);
				g2.setPaint(marker.getLabelPaint());
				Point2D coordinates = arg3.apply(orientation, line, anchor);
				TextUtilities.drawAlignedString(label, g2, (float) coordinates.getX(), (float) coordinates.getY(),
						marker.getLabelTextAnchor());
			}
			g2.setComposite(originalComposite);
		} else if (marker instanceof IntervalMarker) {
			IntervalMarker im = (IntervalMarker) marker;
			double start = im.getStartValue();
			double end = im.getEndValue();
			Range range = domainAxis.getRange();
			if (!(range.intersects(start, end))) {
				return;
			}
			double start2d = domainAxis.valueToJava2D(start, dataArea, arg0);
			double end2d = domainAxis.valueToJava2D(end, dataArea, arg0);
			double low = Math.min(start2d, end2d);
			double high = Math.max(start2d, end2d);
			PlotOrientation orientation = ((XYPlot) plot).getOrientation();
			Rectangle2D rect = null;
			if (orientation == arg1) {
				low = Math.max(low, dataArea.getMinY());
				high = Math.min(high, dataArea.getMaxY());
				rect = new Rectangle2D.Double(dataArea.getMinX(), low, dataArea.getWidth(), high - low);
			} else if (orientation == arg2) {
				low = Math.max(low, dataArea.getMinX());
				high = Math.min(high, dataArea.getMaxX());
				rect = new Rectangle2D.Double(low, dataArea.getMinY(), high - low, dataArea.getHeight());
			}
			final Composite originalComposite = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, marker.getAlpha()));
			Paint p = marker.getPaint();
			if (p instanceof GradientPaint) {
				GradientPaint gp = transformGradientPaintForMarker(im, rect, p);
				g2.setPaint(gp);
			} else {
				g2.setPaint(p);
			}
			g2.fill(rect);
			if (im.getOutlinePaint() != null && im.getOutlineStroke() != null) {
				if (orientation == arg2) {
					 Line2D line = createOutlineAlongYAxis(dataArea, start, end, range, start2d, end2d);
					
					g2.setPaint(im.getOutlinePaint());
					g2.setStroke(im.getOutlineStroke());
					if (range.contains(start)) {
						//line.setLine(start2d, y0, start2d, y1);
						g2.draw(line);
					}
					if (range.contains(end)) {
						//line.setLine(end2d, y0, end2d, y1);
						g2.draw(line);
					}
				} else if (orientation == arg1) {
					Line2D line = createOutlineAlongXAxis(dataArea, start, end, range, start2d, end2d);
					g2.setPaint(im.getOutlinePaint());
					g2.setStroke(im.getOutlineStroke());
					if (range.contains(start)) {
						g2.draw(line);
					}
					if (range.contains(end)) {
						g2.draw(line);
					}
				}
			}
			String label = marker.getLabel();
			RectangleAnchor anchor = marker.getLabelAnchor();
			if (label != null) {
				Font labelFont = marker.getLabelFont();
				g2.setFont(labelFont);
				g2.setPaint(marker.getLabelPaint());
				Point2D coordinates = arg4.apply(orientation, rect, anchor);
				TextUtilities.drawAlignedString(label, g2, (float) coordinates.getX(), (float) coordinates.getY(),
						marker.getLabelTextAnchor());
			}
			g2.setComposite(originalComposite);
		}
		return;
 }
 else
 {
	 if (marker instanceof ValueMarker) {
         Line2D line = createLineForValueMarkerInCategoryPlot(marker, domainAxis, plot, dataArea);
		ValueMarker vm = (ValueMarker) marker;
         double value = vm.getValue();
         Range range = domainAxis.getRange();

         if (!range.contains(value)) {
             return;
         }

         final Composite savedComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(
                 AlphaComposite.SRC_OVER, marker.getAlpha()));

         PlotOrientation orientation = ((CategoryPlot) plot).getOrientation();
         g2.setPaint(marker.getPaint());
         g2.setStroke(marker.getStroke());
         g2.draw(line);

         String label = marker.getLabel();
         RectangleAnchor anchor = marker.getLabelAnchor();
         if (label != null) {
             Font labelFont = marker.getLabelFont();
             g2.setFont(labelFont);
             Point2D coordinates = calculateRangeMarkerTextAnchorPoint(
                     g2, orientation, dataArea, line.getBounds2D(),
                     marker.getLabelOffset(), LengthAdjustmentType.EXPAND,
                     anchor);
             g2.setPaint(marker.getLabelPaint());
             TextUtilities.drawAlignedString(label, g2, 
                     (float) coordinates.getX(), (float) coordinates.getY(),
                     marker.getLabelTextAnchor());
         }
         g2.setComposite(savedComposite);
     }
     else if (marker instanceof IntervalMarker) {
         IntervalMarker im = (IntervalMarker) marker;
         double start = im.getStartValue();
         double end = im.getEndValue();
         Range range = domainAxis.getRange();
         if (!(range.intersects(start, end))) {
             return;
         }

         final Composite savedComposite = g2.getComposite();
         g2.setComposite(AlphaComposite.getInstance(
                 AlphaComposite.SRC_OVER, marker.getAlpha()));

         double start2d = domainAxis.valueToJava2D(start, dataArea,
        		 ((CategoryPlot) plot).getRangeAxisEdge());
         double end2d = domainAxis.valueToJava2D(end, dataArea,
        		 ((CategoryPlot) plot).getRangeAxisEdge());
         double low = Math.min(start2d, end2d);
         double high = Math.max(start2d, end2d);
         
       

         PlotOrientation orientation = ((CategoryPlot) plot).getOrientation();
         Rectangle2D rect = null;
         if (orientation == PlotOrientation.HORIZONTAL) {
             // clip left and right bounds to data area
             low = Math.max(low, dataArea.getMinX());
             high = Math.min(high, dataArea.getMaxX());
             rect = new Rectangle2D.Double(low,
                     dataArea.getMinY(), high - low,
                     dataArea.getHeight());
         }
         else if (orientation == PlotOrientation.VERTICAL) {
             // clip top and bottom bounds to data area
             low = Math.max(low, dataArea.getMinY());
             high = Math.min(high, dataArea.getMaxY());
             rect = new Rectangle2D.Double(dataArea.getMinX(),
                     low, dataArea.getWidth(),
                     high - low);
         }
         Paint p = marker.getPaint();
         if (p instanceof GradientPaint) {
        	GradientPaint gp = transformGradientPaintForMarker(im, rect, p);
             g2.setPaint(gp);
         }
         else {
             g2.setPaint(p);
         }
         g2.fill(rect);

         // now draw the outlines, if visible...
         if (im.getOutlinePaint() != null && im.getOutlineStroke() != null) {
             if (orientation == PlotOrientation.VERTICAL) {
            	 Line2D line = createOutlineAlongXAxis(dataArea, start, end, range, start2d, end2d);
                 
                 g2.setPaint(im.getOutlinePaint());
                 g2.setStroke(im.getOutlineStroke());
                 if (range.contains(start)) {
                     //line.setLine(x0, start2d, x1, start2d);
                     g2.draw(line);
                 }
                 if (range.contains(end)) {
                    // line.setLine(x0, end2d, x1, end2d);
                     g2.draw(line);
                 }
             }
             else if (orientation == PlotOrientation.HORIZONTAL) {
                 Line2D line = createOutlineAlongYAxis(dataArea, start, end, range, start2d, end2d);
				g2.setPaint(im.getOutlinePaint());
                 g2.setStroke(im.getOutlineStroke());
                 if (range.contains(start)) {
                     g2.draw(line);
                 }
                 if (range.contains(end)) {
                     g2.draw(line);
                 }
             }
         }

         String label = marker.getLabel();
         RectangleAnchor anchor = marker.getLabelAnchor();
         if (label != null) {
             Font labelFont = marker.getLabelFont();
             g2.setFont(labelFont);
             g2.setPaint(marker.getLabelPaint());
             Point2D coordinates = calculateRangeMarkerTextAnchorPoint(
                     g2, orientation, dataArea, rect,
                     marker.getLabelOffset(), marker.getLabelOffsetType(),
                     anchor);
             TextUtilities.drawAlignedString(label, g2,
                     (float) coordinates.getX(), (float) coordinates.getY(),
                     marker.getLabelTextAnchor());
         }
         g2.setComposite(savedComposite);
     }
	 
 }
	
  }

	//Extracted Method
	private Line2D createLineForValueMarkerInXYPlot(Marker marker, ValueAxis domainAxis, Plot plot,
			Rectangle2D dataArea, RectangleEdge arg0, PlotOrientation arg1, PlotOrientation arg2) {
		ValueMarker vm = (ValueMarker) marker;
		double value = vm.getValue();
		double v = domainAxis.valueToJava2D(value, dataArea, arg0);
		PlotOrientation orientation = ((XYPlot) plot).getOrientation();
		Line2D line = null;
		if (orientation == arg1) {
			line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
		} else if (orientation == arg2) {
			line = new Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
		}
		return line;
	}

	//Extracted Method
	private Line2D createLineForValueMarkerInCategoryPlot(Marker marker, ValueAxis domainAxis, Plot plot, Rectangle2D dataArea) {
		ValueMarker vm = (ValueMarker) marker;
		double value = vm.getValue();
		PlotOrientation orientation = ((CategoryPlot) plot).getOrientation();
		double v = domainAxis.valueToJava2D(value, dataArea, ((CategoryPlot) plot).getRangeAxisEdge());
		Line2D line = null;
		if (orientation == PlotOrientation.HORIZONTAL) {
			line = new Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
		} else if (orientation == PlotOrientation.VERTICAL) {
			line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
		}
		return line;
	}

	//Extracted Method
	private GradientPaint transformGradientPaintForMarker(IntervalMarker im, Rectangle2D rect, Paint p) {
		GradientPaint gp = (GradientPaint) p;
		GradientPaintTransformer t = im.getGradientPaintTransformer();
		if (t != null) {
			gp = t.transform(gp, rect);
		}
		return gp;
	}
	
	//Extracted Method
	private Line2D createOutlineAlongYAxis(Rectangle2D dataArea, double start, double end, Range range, double start2d,
			double end2d) {
		Line2D line = new Line2D.Double();
		double y0 = dataArea.getMinY();
		double y1 = dataArea.getMaxY();
		if (range.contains(start)) {
			line.setLine(start2d, y0, start2d, y1);
		}
		if (range.contains(end)) {
			line.setLine(end2d, y0, end2d, y1);
		}
		return line;
	}
	
	//Extracted Method

	private Line2D createOutlineAlongXAxis(Rectangle2D dataArea, double start, double end, Range range, double start2d, double end2d) {
		Line2D line = new Line2D.Double();
		double x0 = dataArea.getMinX();
		double x1 = dataArea.getMaxX();
		
		if (range.contains(start)) {
			line.setLine(x0, start2d, x1, start2d);
		}
		if (range.contains(end)) {
			line.setLine(x0, end2d, x1, end2d);
		}
		return line;
	}
}

