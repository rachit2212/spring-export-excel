package com.tothenew.myapp.excelUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.SharedFormula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class ReadExcelWithFormula {
	static int startColumn=1,endColumn=130;
	
	// company id US30303M1027
	
	public static String urlString = "http://localhost:2891/companies/COMPANYIDTOREPLACE/series?start_date=2015-01-01&end_date=2015-10-25&metrics=allmetrics&score_type=pulse&mode=%7B+%22isSasb%22+%3A+false+%7D";
	public static String apiUrl = "http://localhost:2891/articles?start_date=2015-01-01&end_date=2015-10-25&ISINs=COMPANYIDTOREPLACE&article_cap=10000";
	private static final String excelFilePath = "input_pulse.xls";
	
	public static void createExcel(boolean mockResponse, String companyId) {
		int currentRow = 12;
	    FileInputStream inp = null;
	    FileOutputStream output_file = null;
	    HSSFWorkbook workbook;
	    
	    urlString = urlString.replace("COMPANYIDTOREPLACE", companyId);
	    apiUrl = apiUrl.replace("COMPANYIDTOREPLACE", companyId);
	    
	    try {      
	    	ClassLoader classLoader = ReadExcelWithFormula.class.getClassLoader();
	    	inp = new FileInputStream(classLoader.getResource(excelFilePath).getFile());
	        workbook = new HSSFWorkbook(inp);
			HSSFEvaluationWorkbook formulaParsingWorkbook = HSSFEvaluationWorkbook.create((HSSFWorkbook) workbook);
			SharedFormula sharedFormula = new SharedFormula(SpreadsheetVersion.EXCEL2007);
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			JSONArray jsonarray = ReadInputData.readInputDataFromAPI(apiUrl, mockResponse);			
            HSSFSheet sheet = workbook.getSheetAt(0);
            int totalRows=12+jsonarray.size() ;
	        writeArticlesData(currentRow, formulaParsingWorkbook, sharedFormula, evaluator, jsonarray, sheet,totalRows);
	        writeSeriesData(workbook, sheet, totalRows, mockResponse);
	        
	        	        
	        inp.close();
	        
	        
	        File serverFile = new File(File.separator + "tmp" + File.separator + "output.xls");
	    	serverFile.createNewFile();
	        output_file = new FileOutputStream(serverFile);  
	        //write changes
	    	workbook.write(serverFile);
	    	//close the stream
	    	output_file.close();
	    	
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}
	

	private static void writeArticlesData(int currentRow, HSSFEvaluationWorkbook formulaParsingWorkbook,
			SharedFormula sharedFormula, FormulaEvaluator evaluator, JSONArray jsonarray, HSSFSheet sheet,int totalRows) {
		JSONObject jsonObject;
		Row row = sheet.getRow(8);
		List tvl2CatsKeys = new ArrayList();
		
		if(row != null) {
			for(Cell cell : row) {
				if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getStringCellValue().trim() != "") {
					if(cell.getColumnIndex() != 0 && cell.getColumnIndex() != 1) {			
					tvl2CatsKeys.add(cell.getStringCellValue().trim());
		        }
			}
		  }
		}
		
		for(Object obj : jsonarray) {
			jsonObject = null;
			jsonObject = (JSONObject) obj; 
			handleRecord(sheet,currentRow,jsonObject,formulaParsingWorkbook, sharedFormula,evaluator,tvl2CatsKeys);
			currentRow++;
		}
		
		for (int i = 0; i < tvl2CatsKeys.size(); i++) {
			int cellNumber = searchKey(sheet, 8, (String) tvl2CatsKeys.get(i));
			// raw value will be found (or added) at row : currentRow and
			// column : cellNumber -2
			Cell c = sheet.getRow(8).getCell(cellNumber - 2, Row.CREATE_NULL_AS_BLANK);
			String formula = c.toString();
			if (formula.length() > 3) {
				String newFormula = formula.substring(0, formula.length() - 3) +totalRows + ")";
				c.setCellType(HSSFCell.CELL_TYPE_FORMULA);
				c.setCellFormula(newFormula);
			}

		}
	}



	private static void writeSeriesData(HSSFWorkbook workbook, HSSFSheet sheet, int totalRows, boolean mockResponse) {
		Map<Integer, Long> inputData=new LinkedHashMap<Integer, Long>();
		for (int i = 11; i <totalRows-1; i++) {
			
			Cell cell1 = sheet.getRow(i).getCell(0, Row.CREATE_NULL_AS_BLANK);
			Cell cell2 = sheet.getRow(i+1).getCell(0, Row.CREATE_NULL_AS_BLANK);
			Date d1 = cell1.getDateCellValue();	    
			Date d2 = cell2.getDateCellValue();
			
			if (d1!=null&&d2!=null) {
				d1=CommonUtility.truncateToDay(d1);
				d2=CommonUtility.truncateToDay(d2);
				
				if (d1.getTime() == d2.getTime()) {
					continue;
				} else {
					inputData.put(i+1, CommonUtility.getUTCTime(d1));
				} 
			}else{
				if (d1!=null) {
					d1=CommonUtility.truncateToDay(d1);
					inputData.put(i+1, CommonUtility.getUTCTime(d1));
				}
			}
		}
		HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
		
		//fetch api data
		JSONArray  jsonArr = CommonUtility.getDataFromAPI(urlString, mockResponse);
		
		//iterate over map	        
		Set<Integer> keys=inputData.keySet();
		for (Integer key : keys) {
			Long hourMs=inputData.get(key);
		    //get value of date from map
		    JSONObject currentObj = CommonUtility.getValue(jsonArr,hourMs);
		    

		    if (currentObj!=null) {
				//get pulse score for that date
				JSONObject pulseObj = (JSONObject) currentObj.get("pulse");
				//get row to update which is a key in map	        
				//iterate over pulse score object and update the pulse score in actual field
				CommonUtility.updateCurrentRowActualScore(key, pulseObj, sheet);
			}
		}
	}
	
	 

	private static void handleRecord(HSSFSheet sheet,int currentRow,JSONObject jsonObject,HSSFEvaluationWorkbook 
			formulaParsingWorkbook, SharedFormula sharedFormula,FormulaEvaluator evaluator,List tvl2CatsKeys) {
		    	
		// set date in 1st column : timestamp from JSON and add other values from JSON
		handleCellFromJson(sheet,currentRow-1,jsonObject,tvl2CatsKeys);
		
		// handle all the cell with formula
		for(int i=startColumn;i<=endColumn;i++) {
			handleFormulaCell(sheet,currentRow,i,formulaParsingWorkbook,sharedFormula,evaluator);	
		}
	}

	private static void handleCellFromJson(Sheet sheet,int currentRow,JSONObject jsonObject,List tvl2CatsKeys) {
	
		if(sheet.getRow(currentRow) == null) {
			sheet.createRow(currentRow);
    	 }
    	 sheet.getRow(currentRow).getCell(0, Row.CREATE_NULL_AS_BLANK).setCellValue((Date)jsonObject.get("pubDate")); // set A1=2
					
		//for tvl2Cats values
    	 JSONObject newValuesTvl2Cats = (JSONObject)jsonObject.get("tvl2Cats");
    	 if (newValuesTvl2Cats!=null) {
			try {
				for (int i = 0; i < tvl2CatsKeys.size(); i++) {
					Double modelValue = null;
					String key = (String) tvl2CatsKeys.get(i);
					Object keyValue = newValuesTvl2Cats.get(key);
					if (keyValue != null) {
						if (keyValue instanceof Long) {
							modelValue = Double.longBitsToDouble((Long) keyValue);
						} else {
							modelValue = (Double) keyValue;
						}
					}
					// search this key in excel file
					int cellNumber = searchKey(sheet, 8, key);
					//raw value will be found (or added) at row : currentRow and column : cellNumber -2
					setcellValue(sheet, currentRow, cellNumber - 2, modelValue);

				}
			} catch (Exception npe) {
				npe.printStackTrace();
			} 
		}    	 
	}
	
	public static int searchKey(Sheet sheet, int rowNumber, String searchValue) {
		int value = -1;
		Row row = sheet.getRow(rowNumber);
		if(row !=null) {
			for(Cell cell : row) {
				if (cell.getCellType() == Cell.CELL_TYPE_STRING && cell.getRichStringCellValue().getString().trim().equals(searchValue)) {
					value = cell.getColumnIndex();
					break;
                }
			}
		}
		return value;
	}
	
	private static void handleFormulaCell(HSSFSheet sheet,int currentRow,int column,
			HSSFEvaluationWorkbook formulaParsingWorkbook, SharedFormula sharedFormula,FormulaEvaluator evaluator) {
			
		if(sheet.getRow(currentRow) == null) {
			sheet.createRow(currentRow);
		}	
		Cell sourceCell = sheet.getRow(currentRow-1).getCell(column,Row.CREATE_NULL_AS_BLANK);
		Cell currentCell = sheet.getRow(currentRow).getCell(column,Row.CREATE_NULL_AS_BLANK);
	
		// check if source cell has formula and current cell does not have formula, then, copy formula.
		if(sourceCell.getCellType() == HSSFCell.CELL_TYPE_FORMULA && currentCell.getCellType() != HSSFCell.CELL_TYPE_FORMULA){
			currentCell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
			copyCellFormula(formulaParsingWorkbook,sharedFormula,sourceCell,currentCell);
		} 	
		handleCell(currentCell.getCellType(), currentCell,evaluator);
	}
	
	private static void setcellValue(Sheet sheet, int row, int column,Double value) {	
		if(sheet.getRow(row) == null) {
			sheet.createRow(row);
		}
		if(value!=null){
			sheet.getRow(row).getCell(column, Row.CREATE_NULL_AS_BLANK).setCellValue(value);
		}else if(row==11){
			sheet.getRow(row).getCell(column, Row.CREATE_NULL_AS_BLANK).setCellType(Cell.CELL_TYPE_BLANK);
		}
		 // set A1=2
	}
	
	private static void copyCellFormula(HSSFEvaluationWorkbook formulaParsingWorkbook,SharedFormula sharedFormula,
			Cell source, Cell destination){
	    Ptg[] sharedFormulaPtg = FormulaParser.parse(source.getCellFormula(), formulaParsingWorkbook, FormulaType.CELL, 0);
	    Ptg[] convertedFormulaPtg = sharedFormula.convertSharedFormulas(sharedFormulaPtg, 1, 0);
	    destination.setCellFormula(FormulaRenderer.toFormulaString(formulaParsingWorkbook, convertedFormulaPtg));
	}
	
	private static void handleCell(int type,Cell cell,FormulaEvaluator evaluator) {
	    if (type == HSSFCell.CELL_TYPE_STRING) {
	    //  System.out.println(cell.getStringCellValue());
	    } else if (type == HSSFCell.CELL_TYPE_NUMERIC) {
	      // System.out.println(cell.getNumericCellValue());
	    } else if (type == HSSFCell.CELL_TYPE_BOOLEAN) {
	      // System.out.println(cell.getBooleanCellValue());
	    } else if (type == HSSFCell.CELL_TYPE_FORMULA) {
	    	evaluator.evaluateFormulaCell(cell);
	        handleCell(cell.getCachedFormulaResultType(), cell, evaluator);
	    } else {
	     //  System.out.println("");
	    }
	}	
}