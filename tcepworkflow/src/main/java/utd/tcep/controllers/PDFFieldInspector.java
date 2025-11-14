package utd.tcep.controllers;

import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

public class PDFFieldInspector {
    public static void main(String[] args) {
        try (PDDocument pdf = PDDocument.load(new File("tcepworkflow/src/main//resources/utd/tcep/Blank TCEP.pdf"))) {
            PDAcroForm form = pdf.getDocumentCatalog().getAcroForm();
            if (form != null) {
                System.out.println("=== PDF Field Names ===");
                for (PDField field : form.getFields()) {
                    System.out.println(field.getFullyQualifiedName());
                }
                System.out.println("=======================");
            } else {
                System.out.println("No AcroForm fields found in this PDF.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}