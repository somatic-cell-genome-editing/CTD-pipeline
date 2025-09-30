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
import org.apache.poi.ss.usermodel.Row;
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
                parseSectionInfo(sheet);
                break;
            case "module resources":
                parseModuleResources(sheet);
                break;
            default:
        }

    }
    public void parseResources( XSSFSheet sheet) throws Exception {


        System.out.println("title\tname\turl\tsection");
        boolean headerRow=true;
        for (Row row : sheet) {
            if(headerRow){
                headerRow=false;
            }else {
                String description = String.valueOf(row.getCell(0));
                String url= String.valueOf(row.getCell(1)).trim();
                String name = String.valueOf(row.getCell(2)).trim();
                String dateIssued = String.valueOf(row.getCell(3)).trim();
                String ctdSection = String.valueOf(row.getCell(4)).trim();
                CTDResource resource=new CTDResource();
                resource.setResourceDescription(description);
                resource.setResourceName(name);
                resource.setResourceUrl(url);;
                resource.setCtdSection(ctdSection);
                resource.setDateIssued(dateIssued);
                System.out.println(gson.toJson(resource));
                try {
                    if(resource.getResourceName()!=null && !resource.getResourceName().equals("") && !resource.getResourceName().equals("null")
                            && resource.getResourceUrl()!=null  && resource.getResourceUrl()!=null && !resource.getResourceUrl().equals("null"))
                        resourceDAO.insert(resource);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public void parseSectionInfo( XSSFSheet sheet) throws Exception {

        ObjectMapper mapper = JsonMapper.builder().
                enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER).build();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        System.out.println("Module\tSection\tSubSection1\tSubSection2\tSubSection3\tDescription\tRequiredForInitialIND\tRequiredForMarketingApplication\t" +
                "RequiredForAmendment\tPathToFile\tTemplateLinkText\tExampleLinkText\tSubmissionFormat\tNotes\tResources");
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
                String description = String.valueOf(row.getCell(6));
                String requiredForInitialIND = String.valueOf(row.getCell(7));
                String requiredForMarketingApplicationsOnly = String.valueOf(row.getCell(8));
                String submissionTiming = String.valueOf(row.getCell(9));
                String pathToFile = String.valueOf(row.getCell(10));
                String templateLinkText = String.valueOf(row.getCell(11));
                String exampleLinkText = String.valueOf(row.getCell(12));
                String submissionFormat = String.valueOf(row.getCell(13));
                String notes = String.valueOf(row.getCell(14));
                String resources = String.valueOf(row.getCell(15));
                if(!module && sectionCode!=null) {
                    Section section=new Section();
                    section.setSectionCode(sectionCode);
                    section.setRequiredForInitialIND(requiredForInitialIND);
                    section.setSubmissionTiming(submissionTiming);
                    section.setRequiredForMarketingApplicationOnly(requiredForMarketingApplicationsOnly);
                    section.setPathToFile(pathToFile);
                    section.setTemplateLinkText(templateLinkText);
                    section.setExampleLinkText(exampleLinkText);
                    section.setSubmissionFormat(submissionFormat);
                    section.setNotes(notes);
                    section.setSectionDescription(description);
                    section.setResources(resources);
                    System.out.println(gson.toJson(section));
                    update(section);

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

                String pathToFile = String.valueOf(row.getCell(10));
                String templateLinkText = String.valueOf(row.getCell(11));
                String exampleLinkText = String.valueOf(row.getCell(12));

                if(!module && sectionCode!=null) {
                    CTDResource resource=new CTDResource();
                    resource.setCtdSection(sectionCode);
                    resource.setFilePath(pathToFile);
                    resource.setSource("scge");
                    if(templateLinkText!=null && !templateLinkText.equals("")){
                        resource.setResourceName(templateLinkText);
                        resource.setType("template");
                        System.out.println(gson.toJson(resource));

                      resourceDAO.insert(resource);

                    }
                    if(exampleLinkText!=null && !exampleLinkText.equals("")){
                        resource.setResourceName(exampleLinkText);
                        resource.setType("example");
                        System.out.println(gson.toJson(resource));

                        resourceDAO.insert(resource);

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
            sectionDAO.update(section);
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
            case "moduleResources" -> {
                List<String> sheets = Arrays.asList("Module 1", "Module 2", "Module 3", "Module 4", "Module 5");
                for(String sheet:sheets) {
                    //String sheet = "Module 5";
                    parser.parseFile(fileName, sheet,"module resources");
                }
            }
            case "resources" -> {
                String resourcesSheet = "Resources Table";
                parser.parseFile(fileName, resourcesSheet, "resources");
            }
            default -> {
            }
        }

    }
}
