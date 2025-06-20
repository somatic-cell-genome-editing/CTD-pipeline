package edu.mcw.scge.platform.excelParser;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;


public class ExcelParser {
    public void parseFile(String file,String sheetName) throws Exception {
        System.out.println("FILE:"+ file);
        FileInputStream fs = new FileInputStream(new File(file));
        XSSFWorkbook workbook = new XSSFWorkbook(fs);
        XSSFSheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new Exception("Sheet is null");
        }
        ObjectMapper mapper = JsonMapper.builder().
                enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER).build();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        System.out.println("Module\tSection\tSubSection1\tSubSection2\tSubSection3\tDescription\tRequiredForInitialIND\tRequiredForMarketingApplication\t" +
                "RequiredForAmendment\tTemplateLinkText\tExampleLinkText\tSubmissionFormat\tNotes\tResources");
        for (Row row : sheet) {
            String module= String.valueOf(row.getCell(0));
            String section= String.valueOf(row.getCell(1));
            String subSection1= String.valueOf(row.getCell(2));
            String subSection2= String.valueOf(row.getCell(3));
            String subSection3= String.valueOf(row.getCell(4));

            String description= String.valueOf(row.getCell(6));
            String requiredForInitialIND= String.valueOf(row.getCell(7));
            String requiredForMarketingApplicationsOnly= String.valueOf(row.getCell(8));
            String requiredForAmendment= String.valueOf(row.getCell(9));
            String templateLinkText= String.valueOf(row.getCell(10));
            String exampleLinkText= String.valueOf(row.getCell(11));
            String submissionFormat= String.valueOf(row.getCell(12));
            String notes= String.valueOf(row.getCell(13));
            String resources= String.valueOf(row.getCell(14));
            System.out.println(module+"\t"+section+"\t"+subSection1+"\t"+subSection2+"\t"+subSection3+"\t"+description+"\t"+requiredForInitialIND+"\t"+
                    requiredForMarketingApplicationsOnly+"\t"+requiredForAmendment+"\t"+templateLinkText+"\t"+exampleLinkText+"\t"+submissionFormat+"\t"+notes+"\t"+resources);

        }
    }
    public static void main(String[] args) throws Exception {
        String fileName=System.getenv("FILE_NAME");
        List<String> sheets= Arrays.asList("Module 1","Module 2","Module 3","Module 4","Module 5");
        for(String sheet:sheets) {
//            String sheet = "Module 1";
            ExcelParser parser = new ExcelParser();
            parser.parseFile(fileName, sheet);
        }
    }
}
