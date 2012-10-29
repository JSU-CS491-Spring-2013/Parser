/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
// package cs491texttoxml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 *
 * @author Quentin Mayo
 */
public class DiscourseAnalysisTextToXML {
    //This is a array of lines in file
    //I use a array list because we are unsure of the size of the file

    private ArrayList<String> text;
    private ArrayList<String> errors;
    private ArrayList<String> CONJUNCTIONS;
    private String xmlText = "";
    private String textFileName;
    private String conjunctionFileName;
    private boolean noConjunctions = false;
    private boolean conjunctionFileFound = false;
    private boolean textFileFound = false;
    private boolean successfulParse = false;
    private boolean noText = false;

    private boolean uploadText(String filePath, String fileName) {
        File Taskfile = new File(filePath + "/" + fileName);
        if (Taskfile.canRead() == false) {
            return false;
        } else {
            text = new ArrayList<String>();
            try {

                FileInputStream fstream = new FileInputStream(Taskfile);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String temp;
                //Discard First line
                temp = "";
                //now
                while ((temp = br.readLine()) != null) {
                    //.replaceAll("\\s+"," "
                    text.add(trimLeftandRight(temp.trim()));
                }
                in.close();
                if (text.size() > 0) {
                    noText = true;
                }
                textFileFound = true;
                return true;
            } catch (Exception e) {
                System.err.println("Error In Uploading: " + e.getMessage());
                return false;
            }
        }
    }

    private boolean uploadConjunctions(String filePath, String fileName) {
        File Taskfile = new File(filePath + "/" + fileName);
        if (Taskfile.canRead() == false) {
            return false;
        } else {
            CONJUNCTIONS = new ArrayList<String>();
            try {

                FileInputStream fstream = new FileInputStream(Taskfile);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String temp;
                //Discard First line
                temp = "";
                //now
                while ((temp = br.readLine()) != null) {

                    //.replaceAll("\\s+"," "
                    if (temp.length() > 0) {
                        CONJUNCTIONS.add(trimLeftandRight(temp.trim().toUpperCase()));
                    }
                }
                in.close();
                if (CONJUNCTIONS.size() > 0) {
                    noConjunctions = true;
                }
                conjunctionFileFound = true;
                return true;
            } catch (Exception e) {
                System.err.println("Error In Uploading: " + e.getMessage());
                return false;
            }
        }
    }

    public String stringCleaner(String temp) {
        //"'\"\' [],.\n)-'"
        temp = temp.replace("\n", " ");
        temp = trimLeftandRight(temp);
        temp = temp.replace("[", "");
        temp = temp.replace(",", "");
        temp = temp.replace("]", "");
        temp = temp.replace(".", "");
        temp = temp.replace("'", "");
        return temp;
    }

    public ArrayList showConjunctions() {
        return CONJUNCTIONS;
    }

    public boolean conjunctionsUploadSuccessful() {
        if (noConjunctions == true && conjunctionFileFound == true) {
            return true;
        } else {
            return false;
        }
    }

    public boolean textUploadSuccessful() {
        if (textFileFound == true && noText == true) {
            return true;
        } else {
            return false;
        }
    }

    public static ArrayList stringToArrayList(String list, String separator) {
        String[] pieces = list.split(separator);
        for (int i = pieces.length - 1; i >= 0; i--) {
            pieces[i] = pieces[i].trim();
        }
        return new ArrayList(Arrays.asList(pieces));
    }

    public String conj(String line, boolean conjLast, String chap, String ver, boolean first, int errorLocationIncrement) {
        line = line.replace("\n", "").trim();
        String newLine = "";
        if (first == false) {
            if (conjLast == true) {
                newLine = "\t\t<text chapter=\"" + chap + "\" verse=\"" + ver + "\"></text>\n\t</clause>\n\t<clause>\n\t\t<conj>" + line + "</conj>\n";
            } else {
                newLine = "\t</clause>\n\t<clause>\n\t\t<conj>" + line + "</conj>\n";
            }
        } else {
            newLine = "\t\t<conj>" + line + "</conj>\n";
        }

        return newLine;
    }

    public String[] text(String line, boolean lastConj, String chapter, String verse, boolean first, int errorLocationIncrement) {
        line = trimLeftandRight(line);
        String chap = chapter;
        String tempChap = chapter;
        String ver = verse;
        String tempVer = verse;
        int longestConjunction = 0;

        for (int i = 0; i < CONJUNCTIONS.size(); i++) {
            if (CONJUNCTIONS.get(i).split(" ").length > longestConjunction) {
                longestConjunction = CONJUNCTIONS.get(i).split(" ").length;
            }
        }

        int conjunctionSize = 1;
        //trick
        String[] lineArray = new String[1];
        while (conjunctionSize != longestConjunction) {
            ArrayList<String> tempArray = new ArrayList<String>();
            for (int i = 0; i < CONJUNCTIONS.size(); i++) {
                if (CONJUNCTIONS.get(i).split(" ").length == conjunctionSize) {
                    tempArray.add(CONJUNCTIONS.get(i));
                }
            }
            lineArray = line.split(" ");
            String newLine = "";
            for (int i = 0; i < (lineArray.length - (longestConjunction - 1)); i++) {
                String con = "";
                for (int x = 0; x < longestConjunction; x++) {
                    con = con + lineArray[x].replace("\"\' [],.\n)-", "") + " ";

                }
                if (tempArray.contains(stringCleaner(con.toUpperCase()))) {
                    // lineArray[i] = "<pconj>" + lineArray[i];
                    lineArray[i + (longestConjunction - 1)] = lineArray[i + (longestConjunction - 1)];

                }
            }
            longestConjunction = longestConjunction - 1;
        }

        for (int i = 1; i < (lineArray.length); i++) {
            /*if (CONJUNCTIONS.contains(stringCleaner(lineArray[i].replace("'\"\' [],.\n)-", "").toUpperCase()))) {
                lineArray[i] = "<pconj>" + lineArray[i] + "</pconj>";
            }*/

            if (lineArray[i].length() > 1 && lineArray[i].charAt(0) == '-' && Character.isDigit(lineArray[i].charAt(0))) {
                successfulParse = false;
                errors.add("Code A: Negative Chapter at line " + (errorLocationIncrement));
            }

            if (Character.isDigit(lineArray[i].charAt(0))) {
                if (lineArray[i].contains(":")) {
                    String[] cvArray = lineArray[i].split(":");
                    tempChap = cvArray[0].replace("\n", "");

                    try {
                        tempVer = cvArray[1].replace("\n", "");
                    } catch (Exception e) {
                        successfulParse = false;
                        errors.add("Code F:Invalid space At Chapter or Verse at line" + (errorLocationIncrement));
                    }
                    lineArray[i] = "</text>\n\t\t<text chapter=\"" + tempChap + "\" verse=\"" + tempVer + "\">";
                } else {
                    successfulParse = false;

                    //errors.add("Code H: Unknown Error at line " + (errorLocationIncrement));
                }
            }
        }

        String newLine = "";
        String[] newLineArray = new String[3];
        for (int i = 0; i < (lineArray.length); i++) {
            if (Character.isDigit(lineArray[i].charAt(0)) == false && Character.isDigit(lineArray[i].charAt(0))) {
                successfulParse = false;
                errors.add("Code A: Negative Chapter at line " + (errorLocationIncrement));
            }
            newLine = newLine + " " + lineArray[i];
        }

        if (first == false) {
            if (lastConj == true) {
                newLine = "\t\t<text chapter=\"" + chap + "\" verse=\"" + ver + "\">" + newLine + "</text>\n";
            } else {
                newLine = "\t</clause>\n\t<clause>\n\t\t<conj>x</conj>\n\t\t<text chapter=\"" + chap + "\" verse=\"" + ver + "\">" + newLine + "</text>\n";
            }
        } else {
            newLine = "\t\t<conj>x</conj>\n\t\t<text chapter=\"" + chap + "\" verse=\"" + ver + "\">" + newLine + "</text>\n";
            chap = tempChap;
            ver = tempVer;


        }
        newLineArray[0] = chap;
        newLineArray[1] = ver;
        newLineArray[2] = newLine;

        return newLineArray;
    }

    public String[] splitFirst(String source, String splitter) {
        // hold the results as we find them
        Vector rv = new Vector();
        int last = 0;
        int next = 0;

        // find first splitter in source
        next = source.indexOf(splitter, last);
        if (next != -1) {
            // isolate from last thru before next
            rv.add(source.substring(last, next));
            last = next + splitter.length();
        }

        if (last < source.length()) {
            rv.add(source.substring(last, source.length()));
        }

        // convert to array
        return (String[]) rv.toArray(new String[rv.size()]);
    }
    //Checks to see if it is a valid path then then loads it to the conjunction arraylist

    public boolean uploadConjunction(String filePath) {
        File Taskfile = new File(filePath);
        if (Taskfile.canRead() == false) {
            return false;
        } else {
            text = new ArrayList<String>();
            try {
                FileInputStream fstream = new FileInputStream(Taskfile);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String temp;
                //Discard First line
                temp = br.readLine();
                //now
                while ((temp = br.readLine()) != null) {
                    text.add(temp.trim().replaceAll("\\s+", " ").toUpperCase());
                }
                in.close();
                noText = true;
                return true;
            } catch (Exception e) {
                System.err.println("Error In Uploading: " + e.getMessage());
                return false;
            }
        }
    }

    public boolean uploadTextAndConjection(String textFilePath, String conjunctionFilePath) {
        if (uploadText(textFilePath, "") == true && uploadConjunctions(conjunctionFilePath, "") == true) {
            return true;
        } else {
            return false;
        }

    }

    public int toNumber(String input) {
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isStringDigitAndPositve(String x) {
        if (isInteger(x)) {
            if (Integer.parseInt(x) > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDigitAndPositve(char x) {
        if (Character.isDigit(x)) {
            if (Character.getNumericValue(x) > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isStrDigitAndPositve(String x) {
        try {
            if (Integer.parseInt(x) > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isStrDigit(String x) {
        try {
            if (Integer.parseInt(x) > 0) {
                return true;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean inConjunctionList(String text) {
        if (conjunctionFileFound == false) {
            return false;
        } else {
            for (int i = 1; i < CONJUNCTIONS.size(); i++) {
                if (CONJUNCTIONS.get(i).indexOf(text) > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isStrDigitAndNegative(String x) {
        try {
            if (Integer.parseInt(x) < 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean containsNumber(String x) {
        for (int i = 0; i < x.length(); i++) {
            if (isStrDigit(Character.toString(x.charAt(i)))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsLetter(String x) {
        for (int i = 0; i < x.length(); i++) {
            if (Character.isLetter(x.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private void parse(String title) {
        if (!conjunctionFileFound || !textFileFound || !noConjunctions || !noText) {
            System.out.print("Please upload files first");
            successfulParse = false;
        } else {
            errors = new ArrayList<String>();
            successfulParse = true;
            int errorLocationIncrement = 0;
            boolean first = true;
            boolean conjLast = false;
            String chap = "0";
            String ver = "0";
            xmlText = xmlText + ("<?xml version=\"1.0\" ?>\n\n");
            xmlText = xmlText + ("<book bookName=\"" + title + "\">\n");
            xmlText = xmlText + ("\t<clause>\n");
            int lineCount = 0;


            for (int i = 0; i < text.size(); i++) {
                String line = text.get(i);
                errorLocationIncrement = errorLocationIncrement + 1;


                if (line.length() > 0 && trimLeftandRight(text.get(i).replace("'\"\' [],.\n)-'", "")).length() > 0) {
                    lineCount = lineCount + 1;
                    String[] testChapter = splitFirst(text.get(i), " ");
                    String[] testChapter2 = testChapter[0].split(":");
                    if (isStrDigitAndNegative(testChapter2[0])) {
                        errors.add("Code A: Negative Chapter at line " + (errorLocationIncrement) + "\n");
                        successfulParse = false;

                    }
                    String[] lineArray = splitFirst(line, " ");

                    if (Character.isDigit(line.charAt(0))) {
                        String[] cvArray = lineArray[0].split(":");
                        chap = trimLeftandRight(cvArray[0].replace("\n", ""));
                        if (Integer.parseInt(chap) < 1) {
                            errors.add("Code B: Negative Chapter at line " + (errorLocationIncrement) + "\n");
                            successfulParse = false;
                        }
                        try {
                            ver = trimLeftandRight(cvArray[1].replace("\n", ""));
                            if (Integer.parseInt(ver) < 1) {
                                errors.add("Code C: Negative Verse at line " + (errorLocationIncrement) + "\n");
                                successfulParse = false;
                            }
                        } catch (Exception e) {
                            if (containsNumber(text.get(i)) && (text.get(i).contains(":"))) {
                                errors.add("Code E: Invalid space At Chapter or Verse at line  " + (errorLocationIncrement) + "\n");
                            }
                            successfulParse = false;

                        }
                        if (lineArray.length > 1) {
                            line = trimLeftandRight(lineArray[1]);
                        }
                    }

                    if (CONJUNCTIONS.contains(trimLeftandRight(line.toUpperCase().replace("'\"\' [],.\n)-'", "")))) {
                        xmlText = xmlText + conj(line, conjLast, chap, ver, first, errorLocationIncrement);
                        conjLast = true;
                        first = false;
                    } else if (lineArray.length == 1 && containsNumber(lineArray[0])) {
                        if (first == true) {
                            first = true;
                        }
                    } else {
                        String[] textArray = text(line, conjLast, chap, ver, first, errorLocationIncrement);
                        chap = textArray[0];
                        if (Integer.parseInt(chap) < 1) {
                            errors.add("Code D: Negative Chapter at line " + (errorLocationIncrement) + "\n");
                            successfulParse = false;
                        }
                        ver = textArray[1];
                        xmlText = xmlText + textArray[2];
                        conjLast = false;
                        first = false;
                    }
                    if (containsNumber(line) && !(line.contains(":"))) {
                        errors.add("Code G:Found number without colon at line " + (errorLocationIncrement) + "\n");
                        successfulParse = false;
                    }
                    if (line.length() > 0) {
                        String[] textAsArray = line.split(" ");
                        boolean numberAndTexttest = false;
                        for (int x = 0; x < textAsArray.length; x++) {
                            if (containsNumber(textAsArray[x]) && containsLetter(textAsArray[x])) {
                                numberAndTexttest = true;
                            }
                        }
                        if (numberAndTexttest) {
                            errors.add("Code I:Found Number and Text at line " + (errorLocationIncrement) + "\n");
                            successfulParse = false;
                        }

                    }
                    if (conjLast && !first) {
                        if (i + 1 < text.size()) {
                            if (text.get(i + 1).length() > 0) {
                                String[] testChapter3 = splitFirst(text.get(i + 1), " ");
                                String[] testChapter4 = testChapter3[0].split(":");

                                if (isStrDigit(testChapter4[0])) {
                                    xmlText = xmlText + "\t\t" + "<text chapter= \"" + chap + "\" verse= \"" + ver + "\"></text>\n" + "\t";
                                    conjLast = false;
                                    first = false;
                                }
                            }
                        }
                    }
                }
            }
            xmlText = xmlText + "\t</clause>\n</book>";
        }
    }

    public boolean toFile(String filePath, String fileName) {
        try {
            FileWriter fstream = new FileWriter(filePath + fileName);
            BufferedWriter out = new BufferedWriter(fstream);
            //Discard First line
            out.write(xmlText);
            out.close();
            textFileFound = true;
            return true;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }

    }

    public String getXML() {
        return xmlText;
    }

    public DiscourseAnalysisTextToXML(String conjunctionPath, String conjunctFileName, String textPath, String textFileName) {
        uploadText(textPath, textFileName);
        uploadConjunctions(conjunctionPath, conjunctFileName);
    }

    public String trimLeft(String s) {
        return s.replaceAll("^\\s+", "");
    }

    public String trimRight(String s) {
        return s.replaceAll("\\s+$", "");
    }

    public String trimLeftandRight(String s) {
        return trimLeft(trimRight(s));
    }

    public DiscourseAnalysisTextToXML() {
    }

    public DiscourseAnalysisTextToXML(String conjunctionPath, String textPath, boolean parseNow, boolean toFileToScreen, String newFilePath) {
    }

    public boolean isValidConjectionFile(String filepath) {
        return true;
    }

    public ArrayList getErrors() {
        return errors;

    }

    /**
     * @param args the command line arguments
     * /
    public static void main(String[] args) {
    DiscourseAnalysisTextToXML x = new DiscourseAnalysisTextToXML();
    
    
    x.uploadConjunctions(args[0], "");
    if (x.conjunctionsUploadSuccessful()) {
    System.out.println("0");
    } else {
    System.out.println("1");
    }
    
    
    x.uploadText(args[1], "");
    if (x.textUploadSuccessful()) {
    System.out.println("0");
    } else {
    System.out.println("1");
    }
    String temp3 = args[2].replace("|", " ");
    x.parse(temp3);
    
    ArrayList<String> errors = x.getErrors();
    String textErrors = "";
    
    if (errors.size() > 0) {
    System.out.println("1");
    } else {
    System.out.println("0");
    }
    
    if (errors.size() > 0) {
    
    for (int i = 0; i < errors.size(); i++) {
    System.out.println(errors.get(i));
    }
    
    } else {
    System.out.print(x.getXML());
    x.toFile("F:/xampp/htdocs/CS491/Web Application/DiscourseAnalysis/ParsedXMLFiles/", temp3 + ".xml");
    }
    }
     */
    public static void main(String[] args) {
        try {
            DiscourseAnalysisTextToXML x = new DiscourseAnalysisTextToXML();
            
            x.uploadConjunctions("/home/tyler/", "CONJUNCTIONS.txt");
            if (x.conjunctionsUploadSuccessful()) {
                System.out.println("Successful Upload Conjunctions");
            } else {
                System.out.println("Failed to Upload Conjunctions");
            }
            
            x.uploadText("/home/tyler/", "Luke 1.txt");
            if (x.textUploadSuccessful()) {
                System.out.println("Successful Upload Text");
            } else {
                System.out.println("Failed to Upload Text");
            }
            
            x.parse("Luke 1");
            ArrayList<String> errors = x.getErrors();
            if (errors.size() > 0) {
                for (int i = 0; i < errors.size(); i++) {
                    System.out.println(errors.get(i));
                }
                System.out.println("XML file not created.");
            } else {
                System.out.println("Successful Parse");
                x.toFile("/home/tyler/Desktop/", "output.xml");
            }
        } catch (Exception e) {
        }
    }
}