package g1t1.features.report;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import g1t1.models.sessions.ClassSession;
import g1t1.models.sessions.ModuleSection;
import g1t1.models.sessions.SessionAttendance;

public class PDFReport extends ReportGenerator {
    public PDFReport(String filepath) {
        super(filepath);
    }

    // helper function to write text
    private float drawText(PDPageContentStream cs, PDFont font, float fontSize, float x, float y, String text)
            throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - (fontSize + 2f);
    }

    // helper function to create table
    private void drawRow(PDPageContentStream cs, float yTop, float startX, float colWidth, String[] cells, PDFont font,
            float fontSize, float rowHeight, boolean isHeader) throws IOException {
        cs.setLineWidth(0.5f);
        float x = startX;
        float ascent = font.getFontDescriptor().getAscent() / 1000f * fontSize;
        float padX = 6f;
        float padY = 6f;

        for (String cell : cells) {
            String text = cell;
            float baselineY = yTop - padY - ascent;
            drawText(cs, font, fontSize, x + padX, baselineY, text);
            x += colWidth;
        }

        // grid lines
        float rowWidth = colWidth * cells.length;
        if (isHeader) {
            cs.moveTo(startX, yTop);
            cs.lineTo(startX + rowWidth, yTop);
            cs.stroke();
        }
        cs.moveTo(startX, yTop - rowHeight);
        cs.lineTo(startX + rowWidth, yTop - rowHeight);
        cs.stroke();

        x = startX;
        for (int i = 0; i <= cells.length; i++) {
            cs.moveTo(x, yTop);
            cs.lineTo(x, yTop - rowHeight);
            cs.stroke();
            x += colWidth;
        }
    }

    @Override
    public void generate(Report report) {
        ClassSession session = report.getClassSession();
        ModuleSection section = session.getModuleSection();

        if (section == null) {
            throw new IllegalArgumentException("section cannot be null");
        }

        List<SessionAttendance> sessAttendances = session.getStudentAttendance().values().stream().toList();
        if (sessAttendances == null) {
            throw new IllegalArgumentException("SessionAttendance cannot be null");
        }

        String[] header = buildHeader(report);

        try (PDDocument doc = new PDDocument()) {
            // Fonts
            float fontSize = 11f;
            PDFont fontHeader = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont fontText = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Page size and margins
            PDRectangle pageSize = PDRectangle.A4;
            float margin = 48f;
            float tableTopMargin = 16f;

            // Cell height and width
            float padTop = 6f;
            float padBottom = 6f;
            float rowHeight = padTop + fontSize + padBottom;
            int colCount = header.length;
            float usableWidth = pageSize.getWidth() - 2 * margin;
            float colWidth = usableWidth / colCount;

            // Creating new page
            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = pageSize.getHeight() - margin;

            // Module Section
            String sectionLine = buildClassSection(session)[0];
            y = drawText(cs, fontHeader, fontSize, margin, y, sectionLine);

            // Teacher
            String teacher = "Teacher: Cannot fetch teacher";
            if (report.getTeacher() != null) {
                teacher = "Teacher: " + report.getTeacher().getName();
            }
            y = drawText(cs, fontHeader, fontSize, margin, y, teacher);

            // Timestamp
            if (report.isIncludeTimeStamp()) {
                LocalDateTime currDateTime = LocalDateTime.now();
                DateTimeFormatter formattedDateObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String formattedDate = currDateTime.format(formattedDateObj);
                y = drawText(cs, fontHeader, fontSize, margin, y, "Report Generated on: " + formattedDate);
            }

            y -= tableTopMargin;

            // Table
            drawRow(cs, y, margin, colWidth, header, fontHeader, fontSize, rowHeight, true);
            y -= rowHeight;
            for (SessionAttendance sa : sessAttendances) {
                String[] row = buildRow(sa, report);

                // Re-draws header on new page if not enough space
                if (y - rowHeight < margin) {
                    cs.close();
                    page = new PDPage(pageSize);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = pageSize.getHeight() - margin;
                    drawRow(cs, y, margin, colWidth, header, fontHeader, fontSize, rowHeight, true);
                    y -= rowHeight;
                }
                drawRow(cs, y, margin, colWidth, row, fontText, fontSize, rowHeight, false);
                y -= rowHeight;
            }

            cs.close();
            doc.save(getFilepath());
            System.out.println("PDF successfully written to " + getFilepath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
