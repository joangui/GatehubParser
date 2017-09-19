package parser;

import model.Transaction;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.io.*;
import java.text.ParseException;
import java.util.*;

public class GatehubParser {


    static char DEFAULT_SEPARATOR;
    static char DEFAULT_QUOTE;

    Properties properties;

    public GatehubParser(Properties properties)
    {
        this.properties =properties;
    }


    public  List<Transaction> run() throws FileNotFoundException, ParseException {

        String csvFile = properties.getProperty("INPUT_CSV");
        System.out.println("Loading... "+csvFile);
        DEFAULT_SEPARATOR = properties.getProperty("DEFAULT_SEPARATOR",",").toCharArray()[0];
        DEFAULT_QUOTE = properties.getProperty("DEFAULT_QUOTE","\"").toCharArray()[0];

        Scanner scanner = new Scanner(new File(csvFile));
        Map<String,List<List<String>>> transactions = new LinkedHashMap<>();
        List<Transaction> transactionHistory = new ArrayList<>();

        if (scanner.hasNext()) scanner.nextLine(); //REMOVE_HEADER
        while (scanner.hasNext()) {
            List<String> line = CSVParser.parseLine(scanner.nextLine(),DEFAULT_SEPARATOR,DEFAULT_QUOTE);


            List<List<String>> operations = transactions.get(line.get(1));
            if (operations == null) operations = new ArrayList<>();
            operations.add(line);
            transactions.put(line.get(1),operations);
        }

        for (Map.Entry<String, List<List<String>>> entry : transactions.entrySet())
        {
            Transaction t= new Transaction(entry.getValue());
            transactionHistory.add(t);
            System.out.println(t);
        }
        scanner.close();

        return transactionHistory;

    }

    public void serializeTransactionsHistory(List<Transaction> transactionsHistory) throws IOException {
        if(!properties.getProperty("CSV_OUTPUT_FOLDER","").equals(""))
        {
            writeTransactionsIntoCSV(transactionsHistory);
        }
        if(!properties.getProperty("XLS_OUTPUT_FILE","").equals(""))
        {
            writeTransactionsIntoExcel(transactionsHistory);
        }


    }


    private enum CSV_FILE {fees,xrp,btc,bch,eth}

    public void writeTransactionsIntoExcel(List<Transaction> transactionsHistory) throws IOException {
        Workbook wb = new HSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();

        //CELL COLORS
        CellStyle greenStyle = wb.createCellStyle();
        greenStyle.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        greenStyle.setFillPattern(CellStyle.BIG_SPOTS);

        CellStyle redStyle = wb.createCellStyle();
        redStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
        redStyle.setFillPattern(CellStyle.BIG_SPOTS);


        Map<String,Sheet> sheets = new HashMap<>();
        Map<String,Integer> sheetsLines = new HashMap<>();



        for(CSV_FILE f : CSV_FILE.values())
        {
            Sheet sheet = wb.createSheet(f.toString().toUpperCase());
            sheets.put(f.toString(),sheet);
            sheetsLines.put(f.toString(),1);
        }

        //SHEET HEADER
        for(Map.Entry<String,Sheet> entry : sheets.entrySet())
        {
            String sheetname = entry.getKey();
            Sheet sheet = entry.getValue();
            Row row = sheet.createRow(0);
            if(sheetname.equals("fees"))
            {
                row.createCell(0).setCellValue("Date");
                row.createCell(1).setCellValue("XRP");
            }
            else
            {
                row.createCell(0).setCellValue("Date");
                row.createCell(1).setCellValue("Type");
                row.createCell(2).setCellValue("€");
                row.createCell(3).setCellValue(sheetname.toUpperCase());
                row.createCell(4).setCellValue("€/"+sheetname.toUpperCase());
                row.createCell(5).setCellValue("FEE");

            }

        }



        for( Transaction t : transactionsHistory)
        {
            Sheet sheet = null;
            Integer line = 0;
            String sheetName = null;
            switch (t.TYPE){
                case FEE:
                    sheetName="fees";
                    break;
                case SELL:
                case BUY:
                    switch (t.COIN){
                        case XRP:
                            sheetName="xrp";
                            break;
                        case BTC:
                            sheetName="btc";
                            break;
                        case BCH:
                            sheetName="bch";
                            break;
                        case ETH:
                            sheetName="eth";
                            break;
                    }
            }
            sheet=sheets.get(sheetName);
            line = sheetsLines.get(sheetName);
            Row row = sheet.createRow(line);


            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
            Cell cell = row.createCell(0);
            cell.setCellValue(t.DATE);
            cell.setCellStyle(cellStyle);
            sheet.autoSizeColumn(0);


            if(t.TYPE== Transaction.Type.FEE)
            {
                row.createCell(1).setCellValue(t.FEE);
            }
            else {

                row.createCell(1).setCellValue(t.TYPE.toString());
                //row.createCell(2).setCellValue(t.COIN.toString());
                row.createCell(2).setCellValue(t.EUROS_AMOUNT);
                row.createCell(3).setCellValue(t.COIN_AMOUNT);
                row.createCell(4).setCellValue(t.COIN_COST);
                row.createCell(5).setCellValue(t.FEE);
            }
            line++;
            sheetsLines.put(sheetName,line);
        }




        FileOutputStream fileOut = new FileOutputStream(properties.getProperty("XLS_OUTPUT_FILE"));
        wb.write(fileOut);
        fileOut.close();

    }


    public void writeTransactionsIntoCSV(List<Transaction> transactionsHistory) throws IOException {

        Map<String,BufferedWriter> writers = new HashMap<>();

        String folder =  properties.getProperty("CSV_OUTPUT_FOLDER")+"/";

        for(CSV_FILE f : CSV_FILE.values())
        {
            File file = new File(folder+f.toString()+".csv");
            FileOutputStream ffile = new FileOutputStream(file);
            BufferedWriter bwfile = new BufferedWriter(new OutputStreamWriter(ffile));

            writers.put(f.toString(),bwfile);

        }


        for( Transaction t : transactionsHistory)
        {
            BufferedWriter bwfile = null;
            switch (t.TYPE){
                case FEE:
                    bwfile=writers.get("fees");
                    break;
                case SELL:
                case BUY:
                    switch (t.COIN){
                        case XRP:
                            bwfile=writers.get("xrp");
                            break;
                        case BTC:
                            bwfile=writers.get("btc");
                            break;
                        case BCH:
                            bwfile=writers.get("bch");
                            break;
                        case ETH:
                            bwfile=writers.get("eth");
                            break;
                    }
            }

            bwfile.write(t.toString());
            bwfile.newLine();
        }

        for(BufferedWriter bfw : writers.values()) {
            bfw.close();
        }

    }
}
