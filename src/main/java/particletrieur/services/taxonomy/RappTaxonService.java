package particletrieur.services.taxonomy;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import particletrieur.models.project.TreeTaxon;
import particletrieur.models.taxonomy.RappTaxon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class RappTaxonService {

    public static TreeTaxon ParseTaxonomicTreeXLSX(String filename) throws IOException {
        File excelFile = new File(filename);
        FileInputStream fis = new FileInputStream(excelFile);

        // Create an XSSF Workbook object for our XLSX Excel File
        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        // Get first sheet
        XSSFSheet sheet = workbook.getSheetAt(1);

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
//            System.out.println();
        }
        workbook.close();
        fis.close();
        return treeTaxon;
    }

    public static List<RappTaxon> parseCodes(String filename) throws IOException {
        File excelFile = new File(filename);
        FileInputStream fis = new FileInputStream(excelFile);

        // Create an XSSF Workbook object for our XLSX Excel File
        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        // Get first sheet
        XSSFSheet sheet = workbook.getSheetAt(2);

        // Iterate on rows
        Iterator<Row> rowIt = sheet.iterator();

        // Map row to column number
        ArrayList<RappTaxon> taxons = new ArrayList<>();

        boolean firstRow = true;
        while (rowIt.hasNext()) {
            Row row = rowIt.next();
            if (!firstRow) {
                int code = 0;
                try {
                    code = (int) row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue();
                }
                catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    continue;
                }
                String type = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                String group = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                String name = row.getCell(3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();

                if (type.equals("")) type = "UnknownType";
                if (group.equals("")) group = "UNKN";

                RappTaxon taxon = new RappTaxon(code, type, group, name);
                taxons.add(taxon);
            }
            firstRow = false;
//            System.out.println();
        }
        workbook.close();
        fis.close();
        return taxons;
    }

}
