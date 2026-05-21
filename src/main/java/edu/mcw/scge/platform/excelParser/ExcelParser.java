package edu.mcw.scge.platform.excelParser;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.gson.Gson;
import edu.mcw.scge.dao.implementation.ctd.CTDResourceDAO;
import edu.mcw.scge.dao.implementation.ctd.SectionDAO;
import edu.mcw.scge.datamodel.ctd.CTDResource;
import edu.mcw.scge.datamodel.ctd.Section;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;

import java.util.Arrays;
import java.util.List;


public class ExcelParser {
    SectionDAO sectionDAO=new SectionDAO();
    CTDResourceDAO resourceDAO=new CTDResourceDAO();
    Gson gson=new Gson();

    public void parseFile(String file,String sheetName, String parserType) throws Exception {
        FileInputStream fs = new FileInputStream(new File(file));
        XSSFWorkbook workbook = new XSSFWorkbook(fs);
        XSSFSheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new Exception("Sheet is null");
        }
        switch (parserType){
            case "resources":
                parseResources(sheet);
                break;
            case "module":
                int moduleNumber= Integer.parseInt(sheetName.substring(sheetName.indexOf(" ")).trim());
                parseSectionInfo(sheet, moduleNumber);
                break;
            case "module resources":
                parseModuleResources(sheet);
                break;
            default:
        }

    }
    public void parseResources( XSSFSheet sheet) throws Exception {


      //  System.out.println("title\tname\turl\tsection\\");
        boolean headerRow=true;
        for (Row row : sheet) {
            if(headerRow){
                headerRow=false;
            }else {
                Cell sectionCode=row.getCell(4);
                if(String.valueOf(sectionCode).contains(",")){
                    String[] ctdSections = String.valueOf(row.getCell(4)).trim().split(",");
                    for (String section : ctdSections) {
                        buildResource(row, section);

                    }
                }else{
                    DataFormatter dataFormatter = new DataFormatter();
                    String section = dataFormatter.formatCellValue(sectionCode);
                    buildResource(row, section);
                }


            }
        }
    }
    public void buildResource(Row row, String ctdSection){
        String description = String.valueOf(row.getCell(0));
        String url = String.valueOf(row.getCell(1)).trim();
        String name = String.valueOf(row.getCell(2)).trim();
        String dateIssued = String.valueOf(row.getCell(3)).trim();


        String type = String.valueOf(row.getCell(5)).trim();
        String filePath = String.valueOf(row.getCell(7)).trim();
        String source = String.valueOf(row.getCell(6)).trim();
        if (!name.equals("null") && !name.equals("")) {
            CTDResource resource = new CTDResource();
            resource.setResourceDescription(description);
            resource.setResourceName(name);
            if( !url.equals("") && !url.equals("null"))
            resource.setResourceUrl(url);
            ;
            resource.setCtdSection(ctdSection);
            resource.setDateIssued(dateIssued);
            resource.setSource(source);
            resource.setType(type);
            if( !filePath.equals("") && !filePath.equals("null"))
            resource.setFilePath(filePath.replace("######", "000000"));
            System.out.println(gson.toJson(resource));
            try {
//                    if(resource.getResourceName()!=null && !resource.getResourceName().equals("") && !resource.getResourceName().equals("null")
//                            && ((resource.getResourceUrl()!=null  && !resource.getResourceUrl().equals("") && !resource.getResourceUrl().equals("null"))
//                    || (resource.getFilePath()!=null && resource.getFilePath().equals("") && !resource.getFilePath().equals("null"))))
               insertResource(resource);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void insertResource(CTDResource resource) throws Exception {
        if (!existsResource(resource)) {
            System.out.println("NEW RESOURCE:"+ resource.getCtdSection());
            resourceDAO.insert(resource);
        }

    }
    public boolean existsResource(CTDResource resource) throws Exception {
        List<CTDResource> existingResource=resourceDAO.getCTDResource(resource);
        if(existingResource!=null && existingResource.size()>0){
            return true;
        }
        return false;
    }
    public void parseSectionInfo( XSSFSheet sheet, int moduleNumber) throws Exception {

        ObjectMapper mapper = JsonMapper.builder().
                enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER).build();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         boolean headerRow=true;
        for (Row row : sheet) {
            if(headerRow){
                headerRow=false;
            }else {
//                boolean module = !String.valueOf(row.getCell(0)).equals("");
                Cell cell=row.getCell(0);
                DataFormatter dataFormatter = new DataFormatter();
                String subSection0= dataFormatter.formatCellValue(cell);
                String subSection1= String.valueOf(row.getCell(1)).trim();
                String subSection2 = String.valueOf(row.getCell(2)).trim();
                String subSection3 = String.valueOf(row.getCell(3)).trim();
                String subSection4 = String.valueOf(row.getCell(4)).trim();
                String sectionCode=nonNullValue(subSection0,subSection1,subSection2,subSection3,subSection4);
                String sectionName=String.valueOf(row.getCell(5));
                String description = String.valueOf(row.getCell(6));
                String requiredForInitialIND = String.valueOf(row.getCell(8));
                String requiredForMarketingApplicationsOnly = String.valueOf(row.getCell(9));
                String submissionTiming = String.valueOf(row.getCell(10));
//                String pathToFile = String.valueOf(row.getCell(10));
//                String templateLinkText = String.valueOf(row.getCell(11));
//                String exampleLinkText = String.valueOf(row.getCell(12));
//                String submissionFormat = String.valueOf(row.getCell(13));
                String notes = String.valueOf(row.getCell(13));
 //               String resources = String.valueOf(row.getCell(15));
//                if(!module && sectionCode!=null) {
                    if(sectionCode!=null && !sectionCode.equals("null") && !sectionCode.equals("")) {
                    Section section=new Section();
                    section.setSectionCode(sectionCode.trim());
                    section.setSectionName(sectionName);
                    section.setModuleCode(moduleNumber);
                    if(sectionCode.lastIndexOf(".")>0) {
                        String parentId = sectionCode.substring(0, sectionCode.lastIndexOf(".")).trim();
                        section.setParentId(parentId);
                    }else section.setParentId(sectionCode.trim());
                    String[] sectionTokens=sectionCode.split("\\.");
                    int level=sectionTokens.length;
                    section.setLevel(level);
                    section.setRequiredForInitialIND(requiredForInitialIND);
                    section.setSubmissionTiming(submissionTiming);
                    section.setRequiredForMarketingApplicationOnly(requiredForMarketingApplicationsOnly);
//                    section.setPathToFile(pathToFile);
//                    section.setTemplateLinkText(templateLinkText);
//                    section.setExampleLinkText(exampleLinkText);
//                    section.setSubmissionFormat(submissionFormat);
                    section.setNotes(notes);
                    section.setSectionDescription(description);
//                    section.setResources(resources);
                    System.out.println(gson.toJson(section));
            //     update(section);

                }
            }
        }
    }
    public void parseModuleResources( XSSFSheet sheet) throws Exception {

        ObjectMapper mapper = JsonMapper.builder().
                enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER).build();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        System.out.println("Module\tSection\tSubSection1\tSubSection2\tSubSection3\tDescription\tRequiredForInitialIND\tRequiredForMarketingApplication\t" +
//                "RequiredForAmendment\tPathToFile\tTemplateLinkText\tExampleLinkText\tSubmissionFormat\tNotes\tResources");
        boolean headerRow=true;
        for (Row row : sheet) {
            if(headerRow){
                headerRow=false;
            }else {
                boolean module = !String.valueOf(row.getCell(0)).equals("");
                String subSection1= String.valueOf(row.getCell(1)).trim();
                String subSection2 = String.valueOf(row.getCell(2)).trim();
                String subSection3 = String.valueOf(row.getCell(3)).trim();
                String subSection4 = String.valueOf(row.getCell(4)).trim();
                String sectionCode=nonNullValue(subSection1,subSection2,subSection3,subSection4);

//                String pathToFile = String.valueOf(row.getCell(10));
//                String templateLinkText = String.valueOf(row.getCell(11));
//                String exampleLinkText = String.valueOf(row.getCell(12));
                String pathToFile = String.valueOf(row.getCell(12));
                String templateLinkText = String.valueOf(row.getCell(13));
                String exampleLinkText = String.valueOf(row.getCell(14));

                if(!module && sectionCode!=null) {
                    CTDResource resource=new CTDResource();
                    resource.setCtdSection(sectionCode);
                    if(pathToFile!=null) {
                        resource.setFilePath(pathToFile.replace("######", "000000"));
                    }
                    resource.setSource("scge");
                    if(templateLinkText!=null && !templateLinkText.equals("")){
                        resource.setResourceName(templateLinkText);
                        resource.setType("template");
                        System.out.println(gson.toJson(resource));

                     insertResource(resource);

                    }
                    if(exampleLinkText!=null && !exampleLinkText.equals("")){
                        resource.setResourceName(exampleLinkText);
                        resource.setType("example");
                        System.out.println(gson.toJson(resource));

                       insertResource(resource);

                    }



                }
            }
        }
    }
    public String nonNullValue(String... sections){
        for(String s:sections){
            if(s!=null && !s.equals("")){
                return s;
            }
        }
        return null;
    }
    public void update(Section section){
        try {
            if(sectionDAO.existsSection(section)) {
                sectionDAO.update(section);
            }else{
                section.setSectionId(sectionDAO.getNextKey("ctd_sectiion_key"));
                sectionDAO.insert(section);
            }
        }catch (Exception exception){
            System.out.println("SECTION CODE:"+section.getSectionCode());
            exception.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {
        String fileName=System.getenv("FILE_NAME");
        ExcelParser parser = new ExcelParser();
        switch (args[0]) {
            case "sectionDetails" -> {
                List<String> sheets = Arrays.asList("Module 1", "Module 2", "Module 3", "Module 4", "Module 5");
                   for(String sheet:sheets) {
                //String sheet = "Module 5";
                parser.parseFile(fileName, sheet,"module");
                }
              }
//            case "moduleResources" -> {
//                //Before running this case, please make sure the excel sheet section columns to be 4 otherwise add missing empty columns
////                List<String> sheets = Arrays.asList("Module 1", "Module 2", "Module 3", "Module 4", "Module 5");
//                List<String> sheets = Arrays.asList("Module 4");
//                for(String sheet:sheets) {
//                    //String sheet = "Module 5";
//                    parser.parseFile(fileName, sheet,"module resources");
//                }
//            }
            case "resources" -> {
                String resourcesSheet = "Resources Table";
                parser.parseFile(fileName, resourcesSheet, "resources");
            }
            default -> {
            }
        }

    }
}
