package edu.mcw.scge.platform.pdfExtracter;


import edu.mcw.scge.dao.implementation.ctd.SectionDAO;
import edu.mcw.scge.datamodel.ctd.Section;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.PDFText2HTML;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDFUtils {
    SectionDAO sectionDAO=new SectionDAO();
    public String getTextFromCoordinate(String filepath, String task) throws Exception {
        String result = "";
        BodyContentHandler contenthandler = new BodyContentHandler();
        FileInputStream fstream = new FileInputStream(filepath);

        // Create an object of type Metadata to use
        Metadata data = new Metadata();

        // Create a context parser for the pdf document
        ParseContext context = new ParseContext();

        // PDF document can be parsed using the PDFparser
        // class
        PDFParser pdfparser = new PDFParser();

        // Method parse invoked on PDFParser class
        pdfparser.parse(fstream, contenthandler, data,
                context);

        String content=contenthandler.toString();
        switch (task) {
            case "loadSections":
             //   loadModuleSections(content);
                break;
            case "loadMappings":
              loadMappings(content);

                break;
            default:

        }

        return result;
    }
    public void loadMappings(String content) throws Exception {
      //  extractTable(filePath);
        String indMappings= content.substring(content.indexOf("IND Mapping Section"), content.indexOf("NDA and BLA Mapping Section") );
        String[] lines=indMappings.split("\n");
        for(String line: lines){
            if(line.startsWith("312"))
            System.out.println(line);
        }
    }
    public void extractTable(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath)))
        {
            final double res = 72; // PDF units are at 72 DPI
            PDFTableStripper stripper = new PDFTableStripper();
            stripper.setSortByPosition(true);

            // Choose a region in which to extract a table (here a 6"wide, 9" high rectangle offset 1" from top left of page)
            stripper.setRegion(new Rectangle((int) Math.round(1.0*res), (int) Math.round(1*res), (int) Math.round(6*res), (int) Math.round(9.0*res)));

            // Repeat for each page of PDF
            for (int page = 0; page < document.getNumberOfPages(); ++page)
            {

                if(page>16 && page<24) {
                    System.out.println("Page " + page);
                    PDPage pdPage = document.getPage(page);
                    stripper.extractTable(pdPage);
                    for (int c = 0; c < stripper.getColumns(); ++c) {
                        System.out.println("Column " + c);
                        for (int r = 0; r < stripper.getRows(); ++r) {
                            System.out.println("Row " + r);
                            System.out.println(stripper.getText(r, c));
                        }
                    }
                }
            }
        }
    }
    public void loadModuleSections(String content) throws Exception {
        Map<String, String> modules=new HashMap<>();
        modules.put("1", "Module 1 Administrative information");
        modules.put("2", "Module 2 Summaries");
        modules.put("3", "Module 3 Quality");
        modules.put("4", "Module 4 Nonclinical Study Reports");
        modules.put("5", "Module 5 Clinical Study Reports");
        Map<String, String> moduleEnd=new HashMap<>();
        moduleEnd.put("1", "General investigational plan for initial IND");
        moduleEnd.put("2", "Synopses of individual studies");
        moduleEnd.put("3", "Module 4 Nonclinical Study Reports");
        moduleEnd.put("4", "Module 5 Clinical Study Reports");
        moduleEnd.put("5", "Appendix 1 – Mapping Section");
        String modulesContent= content.substring(0, content.indexOf("IND Mapping Section") );
        String[] lines=modulesContent.split("\n");
        for(String moduleId: modules.keySet()) {
            String endText = moduleEnd.get(moduleId);
//            Module module = new Module();
//            module.setModuleCode(moduleId);
//            module.setModuleName(modules.get(moduleId));
            List<Section> level1 = new ArrayList<>();
            List<Section> level2 = new ArrayList<>();
            List<Section> level3 = new ArrayList<>();
            List<Section> level4 = new ArrayList<>();
            for (String line : lines) {
                if (line.startsWith(moduleId) && line.contains(".")) {
                    String mcode = line.substring(0, line.indexOf(".") );
                    if (mcode.length() == 1) {
                        String sectionId = line.substring(0, line.indexOf(" "));
                        String sectionName = line.substring(line.indexOf(" "));
                        //  System.out.println(line);
                        String[] sectionType = sectionId.split("\\.");
                        Section section = new Section();
                        section.setSectionCode(sectionId.trim());
                        section.setSectionName(sectionName);
                        section.setModuleCode(Integer.parseInt(moduleId));
                        section.setLevel((sectionType.length-1));

                        if (sectionType.length == 2) {
                            section.setParentId(moduleId);
                            level1.add(section);
                        }
                        if (sectionType.length == 3) {
//                            Section section = new Section();
//                            section.setSectionCode(sectionId.trim());
//                            section.setSectionName(sectionName);
                            level2.add(section);
                        }
                        if (sectionType.length == 4) {
//                            Section section = new Section();
//                            section.setSectionCode(sectionId.trim());
//                            section.setSectionName(sectionName);
                            level3.add(section);
                        }
                        if (sectionType.length == 5) {
//                            Section section = new Section();
//                            section.setSectionCode(sectionId.trim());
//                            section.setSectionName(sectionName);
                            level4.add(section);
                        }
                    }
                }
                if (line.indexOf(endText) > 0) {
                    System.out.println("INDEX of END TEXT:" + endText+"\t"+ line.indexOf(endText));
                    //   continue moduleLoop;
                    break ;
                }

            }
            System.out.println("MAIN SECTIONS OF MODULE - "+moduleId+" : ");
            for (Section section : level1) {
                //  sectionDAO.insert(section);
                List<Section> subsections1 = new ArrayList<>();
                for (Section l2Section : level2) {

                    List<Section> subsections2 = new ArrayList<>();
                    String[] l2SectionCode=l2Section.getSectionCode().split("\\.");
                    if (l2SectionCode.length==3 && l2Section.getSectionCode().startsWith(section.getSectionCode() + ".")) {
                        l2Section.setParentId(section.getSectionCode());
                        subsections1.add(l2Section);
                        //      sectionDAO.insert(l2Section);
                        for (Section l3Section : level3) {

                            List<Section> subsections3 = new ArrayList<>();
                            String[] l3SectionCode=l3Section.getSectionCode().split("\\.");

                            if (l3SectionCode.length==4 && l3Section.getSectionCode().startsWith(l2Section.getSectionCode() + ".")) {
                                subsections2.add(l3Section);
                                l3Section.setParentId(l2Section.getSectionCode());
                                //        sectionDAO.insert(l3Section);
                                for(Section l4Section:level4){

                                    String[] l4SectionCode=l4Section.getSectionCode().split("\\.");

                                    if(l4SectionCode.length==5 && l4Section.getSectionCode().startsWith(l3Section.getSectionCode()+".")){
                                        subsections3.add(l4Section);
                                        l4Section.setParentId(l3Section.getSectionCode());
                                        //              sectionDAO.insert(l4Section);
                                    }
                                }
                                l3Section.setSubsections(subsections3);
                            }
                        }
                        l2Section.setSubsections(subsections2);
                    }
                }
                section.setSubsections(subsections1);
                int sectionKey=sectionDAO.getNextKey("ctd_sectiion_key");
                section.setSectionId(sectionKey);
                sectionDAO.insert(section);
                System.out.println(section.getSectionCode() + "\t" + section.getSectionName());
                for (Section ss : section.getSubsections()) {
                    int ssKey=sectionDAO.getNextKey("ctd_sectiion_key");
                    ss.setSectionId(ssKey);
                    sectionDAO.insert(ss);
                    System.out.println("\t" + ss.getSectionCode() + "\t" + ss.getSectionName());
                    for (Section sss : ss.getSubsections()) {
                        int sssKey=sectionDAO.getNextKey("ctd_sectiion_key");
                        sss.setSectionId(sssKey);
                        sectionDAO.insert(sss);
                        System.out.println("\t\t" + sss.getSectionCode() + "\t" + sss.getSectionName());
                        for(Section ssss:sss.getSubsections()){
                            int ssssKey=sectionDAO.getNextKey("ctd_sectiion_key");
                            ssss.setSectionId(ssssKey);
                            sectionDAO.insert(ssss);
                            System.out.println("\t\t\t" + ssss.getSectionCode() + "\t" + ssss.getSectionName());
                        }
                    }
                }
            }
        }
    }
    public static void main(String[] args) throws Exception {


        String file="data/USFDA_eCTDv4_0_CTOC_v1.pdf";
        PDFUtils utils=new PDFUtils();
        String task=args[0];
        String result= utils.getTextFromCoordinate(file,task );
        System.out.println("Done!!");
    }
}
