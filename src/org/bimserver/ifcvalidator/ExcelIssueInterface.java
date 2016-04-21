package org.bimserver.ifcvalidator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.IssueValidationException;
import org.bimserver.validationreport.Type;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExcelIssueInterface implements IssueInterface {

	private WritableCellFormat times;
	private WritableCellFormat timesbold;
	private WritableSheet sheet;
	private ByteArrayOutputStream byteArrayOutputStream;
	private WritableWorkbook workbook;
	private int row;
	private WritableCellFormat error;
	private WritableCellFormat ok;

	public ExcelIssueInterface(Translator translator) {
	    try {
	    	WorkbookSettings wbSettings = new WorkbookSettings();
	    	
	    	wbSettings.setLocale(new Locale("en", "EN"));
	    	
	    	WritableFont times10pt = new WritableFont(WritableFont.ARIAL, 10);
	    	times = new WritableCellFormat(times10pt);
	    	
	    	WritableFont times10ptbold = new WritableFont(WritableFont.ARIAL, 10);
			times10ptbold.setBoldStyle(WritableFont.BOLD);
			timesbold = new WritableCellFormat(times10ptbold);
			
			error = new WritableCellFormat(times10pt);
			error.setBackground(Colour.RED);

			ok = new WritableCellFormat(times10pt);
			ok.setBackground(Colour.LIGHT_GREEN);
			
			byteArrayOutputStream = new ByteArrayOutputStream();
			workbook = Workbook.createWorkbook(byteArrayOutputStream, wbSettings);
			
			sheet = workbook.createSheet("Sheet 1", 0);
			
			sheet.addCell(new Label(0, 0, translator.translate("REPORT_HEADER"), timesbold));
			sheet.addCell(new Label(1, 0, translator.translate("REPORT_TYPE"), timesbold));
			sheet.addCell(new Label(2, 0, translator.translate("REPORT_GUID_OID"), timesbold));
			sheet.addCell(new Label(3, 0, translator.translate("REPORT_MESSAGE"), timesbold));
			sheet.addCell(new Label(4, 0, translator.translate("REPORT_VALUE_IS"), timesbold));
			sheet.addCell(new Label(5, 0, translator.translate("REPORT_VALUE_SHOULD_BE"), timesbold));
			
			row = 1;
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void add(Type messageType, String type, String guid, Long oid, String message, Object is, String shouldBe) throws IssueException {
		try {
			CellFormat cellFormat = messageType == Type.ERROR ? error : ok;
			sheet.addCell(new Label(1, row, type, cellFormat));
			if (guid != null) {
				sheet.addCell(new Label(2, row, guid, cellFormat));
			} else {
				if (oid == -1) {
					sheet.addCell(new Label(2, row, "", cellFormat));
				} else {
					sheet.addCell(new Label(2, row, "" + oid, cellFormat));
				}
			}
			sheet.addCell(new Label(3, row, message, cellFormat));
			sheet.addCell(new Label(4, row, "" + is, cellFormat));
			sheet.addCell(new Label(5, row, shouldBe, cellFormat));
			row++;
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addHeader(String translate) {
		try {
			row++;
			sheet.addCell(new Label(0, row++, translate, timesbold));
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] getBytes() throws IOException {
		for (int x = 0; x < 6; x++) {
			CellView cell = sheet.getColumnView(x);
			cell.setAutosize(true);
			sheet.setColumnView(x, cell);
		}
		workbook.write();
		try {
			workbook.close();
		} catch (WriteException e) {
			throw new IOException(e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	@Override
	public void validate() throws IssueValidationException {
	}

	@Override
	public void setCheckValid(String identifier, boolean valid) {
	}

	@Override
	public void add(Type messageType, String message, Object is, String shouldBe) throws IssueException {
		add(messageType, null,  null,  null, message, is, shouldBe);
	}
}