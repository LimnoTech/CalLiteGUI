package com.limno.calgui;

//import gov.ca.dwr.callite.Report.Writer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultXYDataset;

import com.limno.calgui.Report.Writer;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPRow;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class ReportPDFWriter implements Writer {
	Document document;
	private PdfWriter writer;
	private PdfPTable summaryTable;
	private HeaderFooter footer;
	private Font bigFont;
	private Font smallFont;
	private Font subtitleFont;
	private Font smallBoldFont;

	public ReportPDFWriter() {

	}

	public ReportPDFWriter(String filename) {
		startDocument(filename);
	}

	public void startDocument(String filename) {
		bigFont = FontFactory.getFont("Arial", 14);
		smallFont = FontFactory.getFont("Arial", 10);
		smallBoldFont = FontFactory.getFont("Arial", 10);
		smallBoldFont.setStyle(Font.BOLD);
		subtitleFont = FontFactory.getFont("Arial", 10);
		subtitleFont.setStyle(Font.BOLD);
		document = new Document();
		document.setPageSize(PageSize.A4.rotate());
		document.addCreationDate();
		footer = new HeaderFooter(new Phrase(filename), true);
		document.setFooter(footer);
		try {
			writer = PdfWriter.getInstance(
			// that listens to the document
					document,
					// and directs a PDF-stream to a file
					new FileOutputStream(filename));
			document.open();
		} catch (DocumentException de) {
			System.err.println(de.getMessage());
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}
	}

	public void setAuthor(String author) {
		document.addAuthor(author);
	}
	
	public void writeParagraph(String text) throws DocumentException {
		document.newPage();
		document.add(new Paragraph(text, bigFont));
	}
	
	protected void drawChart(JFreeChart chart) {
		document.newPage();
		new PdfOutline(writer.getRootOutline(),new PdfDestination(PdfDestination.FITH), chart.getTitle().getText());
		PdfContentByte cb = writer.getDirectContent();
		Graphics2D graphics2D = cb.createGraphics(PageSize.A4.getHeight(),
				PageSize.A4.getWidth());
		Rectangle2D r2d2 = new Rectangle2D.Double(36, 36, PageSize.A4
				.getHeight() - 72, PageSize.A4.getWidth() - 72);
		chart.draw(graphics2D, r2d2);
		graphics2D.dispose();
	}

	@Override
	public void addTableHeader(ArrayList<String> headerRow, int[] columnSpans) {
		if (summaryTable == null) {
			summaryTable = new PdfPTable(new float[] { 3, 1, 1, 1, 1, 1, 1, 1,
					1, 1, 1, 1, 1 });
			summaryTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			summaryTable.setHorizontalAlignment(PdfPTable.ALIGN_CENTER);
			summaryTable.setWidthPercentage(100);
			new PdfOutline(writer.getRootOutline(),new PdfDestination(PdfDestination.FITH), "Summary Table");
		}
		addTableRow(headerRow, columnSpans, BOLD, true);
		PdfPRow row = summaryTable.getRow(summaryTable.getRows().size()-1);
		PdfPCell[] cells = row.getCells();
		for (int i = 0; i < cells.length; i++) {
			if (cells[i] != null){
				cells[i].setGrayFill(0.6f);
			}
		}
	}

	@Override
	public void addTableRow(List<String> rowData, int[] columnSpans, int style,
			boolean centered) {
		for (int i = 0; i < rowData.size(); i++) {
			PdfPCell cell = new PdfPCell(new Phrase(rowData.get(i),
					style == BOLD ? smallBoldFont : smallFont));
			if (centered) {
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			} else {
				if (i!=0){
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				} else {
					if (style==BOLD){
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					} else {
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					}
				}
			}
			if (columnSpans != null) {
				cell.setColspan(columnSpans[i]);
			}
			summaryTable.addCell(cell);
		}
	}

	@Override
	public void addTableTitle(String title) {
		try {
			document.newPage();
			document.add(new Paragraph(title, bigFont));
		} catch (DocumentException ex) {
			Logger.getLogger(this.getClass().getName()).severe(ex.getMessage());
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addTableSubTitle(String subtitle) {
		try {
			document.add(new Paragraph(subtitle + "\n", subtitleFont));
		} catch (DocumentException ex) {
			Logger.getLogger(this.getClass().getName()).severe(ex.getMessage());
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void endDocument() {
		document.close();
	}

	@Override
	public void endTable() {
		try {
			document.add(new Phrase("\n"));
			PdfPCell[] cells = summaryTable.getRow(0).getCells();
			setRightAndLeftBorders(cells);
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null) {
					cells[i].setBorderWidthTop(3);
				}
			}
			ArrayList rows = summaryTable.getRows();
			for(int i=1; i < rows.size()-1; i++){
				PdfPCell[] dataCells = ((PdfPRow)rows.get(i)).getCells();
				setRightAndLeftBorders(dataCells);
			}
			PdfPRow lastRow = (PdfPRow) rows.get(rows.size()-1);
			cells = lastRow.getCells();
			setRightAndLeftBorders(cells);
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null) {
					cells[i].setBorderWidthBottom(2);
				}
			}
			document.add(summaryTable);
		} catch (DocumentException ex) {
			Logger.getLogger(this.getClass().getName()).severe(ex.getMessage());
			throw new RuntimeException(ex);
		}
	}
	
	private void setRightAndLeftBorders(PdfPCell[] cells){
		if (cells==null) return;
		if (cells[0] != null){
			cells[0].setBorderWidthLeft(3);
		}
		if (cells[cells.length-1] != null){
			cells[cells.length-1].setBorderWidthRight(3);
		}
	}

	@Override
	public void addExceedancePlot(ArrayList<double[]> buildDataArray,
			String title, String[] seriesName, String xAxisLabel,
			String yAxisLabel) {
		DefaultXYDataset dataset = new DefaultXYDataset();
		for (int i = seriesName.length-1; i >= 0; i--) {
			double[][] seriesData = new double[2][buildDataArray.size()];
			for (int j = 0; j < buildDataArray.size(); j++) {
				double[] data = buildDataArray.get(j);
				seriesData[0][j] = data[0];
				seriesData[1][j] = data[i + 1];
			}
			dataset.addSeries(seriesName[i], seriesData);
		}

		final JFreeChart xyLineChart = ChartFactory.createXYLineChart(title,
				xAxisLabel, yAxisLabel, dataset, true);
		XYPlot xyPlot = xyLineChart.getXYPlot();
		ValueAxis domainAxis = xyPlot.getDomainAxis();
		domainAxis.setInverted(true);
		xyPlot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		xyPlot.setBackgroundPaint(null);
		xyPlot.setRangeGridlinePaint(Color.lightGray);
		xyPlot.setDomainGridlinesVisible(false);
		xyPlot.getDomainAxis().setRange(0, 100);
		drawChart(xyLineChart);
	}

	@Override
	public void addTimeSeriesPlot(ArrayList<double[]> buildDataArray,
			String title, String[] seriesName, String xAxisLabel,
			String yAxisLabel) {
		TimeSeriesCollection datasets = new TimeSeriesCollection();
		for (int i = seriesName.length-1; i >= 0; i--) {
			TimeSeries ts = new TimeSeries(seriesName[i]);
			datasets.addSeries(ts);
		}
		for (int j = 0; j < buildDataArray.size(); j++) {
			double[] dataArray = buildDataArray.get(j);
			Month m = new Month(new Date(Math.round(dataArray[0])));
			for (int i = 0; i < seriesName.length; i++) {
				datasets.getSeries(i).add(m, dataArray[i + 1], false);
			}
		}
		final JFreeChart tsChart = ChartFactory.createTimeSeriesChart(title,
				xAxisLabel, yAxisLabel, datasets, true);
		XYPlot xyPlot = tsChart.getXYPlot();
		xyPlot.setRenderer(new XYStepRenderer());
		xyPlot.setBackgroundPaint(null);
		xyPlot.setDomainGridlinesVisible(false);
		xyPlot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		drawChart(tsChart);
	}

}