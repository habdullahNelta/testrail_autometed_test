package com.idera.testrail.tests;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.annotations.Optional;
import org.yaml.snakeyaml.*;

import javax.swing.*;
import java.io.*;


import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Base {

    public void confirmDialog(String Massage) {
        JDialog.setDefaultLookAndFeelDecorated(true);
        int response = JOptionPane.showConfirmDialog(null, Massage + ", Do you want to continue?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.NO_OPTION) {
            System.out.println("No button clicked");
            System.exit(0);
        } else if (response == JOptionPane.YES_OPTION) {
            System.out.println("Yes button clicked");
        } else if (response == JOptionPane.CLOSED_OPTION) {
            System.out.println("JOptionPane closed");
            System.exit(0);
        }
    }

    protected Map<String, Integer> ImportFilterCSV(String suiteXmlFile) throws Exception {

        Map<String, Integer> IDsMap = new HashMap<>();
        // to read the value from csv
        CSVReader reader = new CSVReader(new FileReader(System.getProperty("user.dir") + "/Test_CSV.csv"));

        String[] nextLine;
        int Run_ID_Var = 0;
        int Suite_ID_Var = 0;

        while ((nextLine = reader.readNext()) != null) {
            // to filter the value by xmlfilename - trim to remove all whitespaces in PackageName
            if (nextLine[2].trim().equals(suiteXmlFile)) {
                // PackageName 0  + Suite_id 1  + XmlFileName 2  +  run_id 3
              //  System.out.println(nextLine[0] + nextLine[1] + nextLine[2] + nextLine[3]);
                if (!nextLine[1].trim().equals("")) {
                    Suite_ID_Var = TestIfIntGiven(nextLine[1], "Suite_ID");
                    //Suite_ID_Var = Integer.parseInt(nextLine[1].trim());
                }
                if (!nextLine[3].trim().equals("")) {
                    Run_ID_Var = TestIfIntGiven(nextLine[3], "Run_ID");
                    //Run_ID_Var = Integer.parseInt(nextLine[3].trim());
                }
            }
        }
        IDsMap.put("Run_ID_Var", Run_ID_Var);
        IDsMap.put("Suite_ID_Var", Suite_ID_Var);
        return IDsMap;
    }

    protected int TestIfIntGiven(String Input, String NameString) throws Exception {
        int num;
        try {
            num = Integer.parseInt(Input.trim());
            // is an integer!
           // System.out.println(NameString + " is an integer");
        } catch (NumberFormatException e) {
            // not an integer!
         //   System.out.println(NameString + " not an integer");
            throw new Exception(NameString + " not an integer");
        }
        return num;
    }

    protected String PathNameFilter(String Path) {
        String filterUntilBackslash = "";
        String reverseSuiteName = "";

        //System.out.println(Path.length());
        for (int i = Path.length() - 1; i >= 0; i--) {
            //  filterUntilBackslash += String.valueOf(SuitName.charAt(i));
            filterUntilBackslash = (new StringBuilder()).append(filterUntilBackslash).append(Path.charAt(i)).toString();
            if (Path.charAt(i) == '/' || Path.charAt(i) == '\\') {
                for (int j = filterUntilBackslash.length() - 2; j >= 0; j--) {
                    // reverseSuiteName += String.valueOf(filterUntilBackslash.charAt(j));
                    reverseSuiteName = (new StringBuilder()).append(reverseSuiteName).append(filterUntilBackslash.charAt(j)).toString();
                }
                break;
            }
        }
        return reverseSuiteName.trim();
    }

    protected void ExportCSV(String[] Input) throws IOException {
        CSVWriter Writer = new CSVWriter(new FileWriter(System.getProperty("user.dir") + "\\Test_CSV.csv", true));
        String[] record = new String[]{"3", "Rahul", "Vaidya", "India", "35"};
        Writer.writeNext(record);
        Writer.close();
    }

    public void logger(Level level, String message) {
        Logger logger = Logger.getLogger(Base.class.getName());
        logger.log(level, message);
    }

    @BeforeTest
    void BeforeMethod() {
        Assert.assertEquals(12, 6 * 2, "Should equal 12");
    }


    @AfterSuite
    @Parameters({"suiteXmlFile"})
    public void TestrailUpdateCSVtoYml(@Optional("no suite name") String suiteXmlFile) throws Exception {
        File appsFolder = new File(suiteXmlFile);

        final String YamlPfad = System.getProperty("user.dir") + "\\trcli-config2.yml";

        //System.out.println(suiteXmlFile);

        String SuiteNameFiltered =  appsFolder.getName();
        //System.out.println("SuiteNameFiltered  "+SuiteNameFiltered);

        // String SuiteNameFiltered = PathNameFilter(suiteXmlFile);
        Map<String, Integer> SuiteAndRunID;
        SuiteAndRunID = ImportFilterCSV(SuiteNameFiltered);


        //read from yaml file
        InputStream inputStream = new FileInputStream(YamlPfad);
        Yaml yaml = new Yaml();
        Map<String, Object> ReadYaml = yaml.load(inputStream);

        final Object host = ReadYaml.get("host");
        final Object project = ReadYaml.get("project");
        final Object username = ReadYaml.get("username");
        final Object key = ReadYaml.get("key");
        final Object title = SuiteNameFiltered;
        final int run_id = SuiteAndRunID.get("Run_ID_Var");
        final int suite_id = SuiteAndRunID.get("Suite_ID_Var");
        final Object fileResult = ReadYaml.get("file");


        Map<Object, Object> WriteYaml = new LinkedHashMap<>();
        WriteYaml.put("host", host);
        WriteYaml.put("project", project);
        WriteYaml.put("username", username);
        WriteYaml.put("key", key);
        WriteYaml.put("title", title);
        // update run_id and suite_id
        if (run_id != 0 && suite_id != 0) {
            WriteYaml.put("run_id", run_id);
            WriteYaml.put("suite_id", suite_id);
            // System.out.println("***the results in suite_id: " + suite_id + " and run_id: " + run_id + "  are updated***");
            logger(Level.INFO, "***the results in suite_id: " + suite_id + " and run_id: " + run_id + "  are updated***");

        } else if (run_id == 0 && suite_id == 0) {
            // System.out.println("no suite_id and run_id is given,they are created");
            logger(Level.WARNING, "no suite_id and run_id is given");
            //  confirmDialog("no suite_id and run_id is given");
        }
        // no run_id then create run_id and update suite_id
        if (run_id == 0 && suite_id != 0) {
            //  System.out.println("no run_id is given");
            logger(Level.WARNING, "no run_id is given");

            WriteYaml.put("suite_id", suite_id);
            //  confirmDialog("no run_id");
        }
        String Filter = PathNameFilter(fileResult.toString());

        // WriteYaml.put("file", fileResult);
        WriteYaml.put("file", System.getProperty("user.dir") + "\\report_sample\\" + Filter);

        // to ordere the values as given
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        yaml = new Yaml(options);

        FileWriter writer = new FileWriter(YamlPfad, false);
        yaml.dump(WriteYaml, writer);

    }
}
