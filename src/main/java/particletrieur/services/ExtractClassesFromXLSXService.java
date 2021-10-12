package particletrieur.services;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import particletrieur.models.project.TreeTaxon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class ExtractClassesFromXLSXService {

    public static TreeTaxon Parse(String filename) throws IOException {
        File excelFile = new File(filename);
        FileInputStream fis = new FileInputStream(excelFile);

        // Create an XSSF Workbook object for our XLSX Excel File
        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        // Get first sheet
        XSSFSheet sheet = workbook.getSheetAt(0);

        // Iterate on rows
        Iterator<Row> rowIt = sheet.iterator();

        // Map row to column number
        HashMap<Integer, String> groupMapping = new HashMap<>();

        TreeTaxon treeTaxon = new TreeTaxon("root", "root");

        boolean firstRow = true;
        while (rowIt.hasNext()) {

            Row row = rowIt.next();
            Iterator<Cell> cellIterator = row.cellIterator();

            int idx = 0;
            if (firstRow) {
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (Arrays.asList(TreeTaxon.validGroups).contains(cell.getStringCellValue())) {
                        groupMapping.put(idx, cell.getStringCellValue());
                    }
                    System.out.print(cell.toString() + ";");
                    idx++;
                }
                firstRow = false;
            } else {
                ArrayList<Pair<String, String>> taxonList = new ArrayList<>();
                String name = "unknown";
                for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                    Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    if (!cell.getStringCellValue().equals("")) {
                        name = cell.getStringCellValue();
                    }
                    if (groupMapping.containsKey(idx)) {
                        taxonList.add(new ImmutablePair<>(groupMapping.get(idx), name));
                    }
                    System.out.print(cell.toString() + ";");
                    idx++;
                }
//                while (cellIterator.hasNext()) {
//                    Cell cell = cellIterator.next();
//
//                }
                treeTaxon.addList(taxonList);
            }
            System.out.println();
        }
        workbook.close();
        fis.close();
        return treeTaxon;
    }
}
