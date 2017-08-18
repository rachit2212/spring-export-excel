package com.tothenew.myapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tothenew.myapp.beans.CompanyModel;
import com.tothenew.myapp.excelUtilities.ReadExcelWithFormula;

@PropertySource("classpath:configuration.properties")
@Controller 
public class ExcelController {

	@Autowired
	private Environment env;
	
	@RequestMapping(value="/excel")
	public String getCompanyData() {
		return "getCompanyData";
	}
	
	@RequestMapping(value="/getExcel", method=RequestMethod.POST)
	public ModelAndView evaluateExcelSheet(@ModelAttribute("SpringWeb")CompanyModel model) {
		boolean isSuccess = ReadExcelWithFormula.createExcel(Boolean.parseBoolean(env.getProperty("mockResponse")), model.getCompanyId());
		ModelAndView excelExportView = new ModelAndView("excelExport");
		excelExportView.addObject("isSuccess", isSuccess);
	    return excelExportView;
	}
	
	@RequestMapping(value="/exportExcel")
	public void exportExcel(HttpServletResponse response) throws IOException {
		File serverFile = new File(File.separator + "tmp" + File.separator + "output.xls");
		FileInputStream in = new FileInputStream(serverFile);
		OutputStream out = response.getOutputStream();

		byte[] buffer= new byte[8192]; 
		int length = 0;

		while ((length = in.read(buffer)) > 0){
		     out.write(buffer, 0, length);
		}
		in.close();
		out.close();
	}
}
