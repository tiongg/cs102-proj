package g1t1.features.report;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private float drawRow(PDPageContentStream cs, float yTop, float startX, float colWidth, String[] cells, PDFont font,
            float fontSize, boolean isHeader) throws IOException {

        cs.setLineWidth(0.5f);
        float padX = 6f;
        float padY = 6f;
        float lineSpacing = 2f; // Extra space between wrapped lines
        float cellTextWidth = colWidth - 2 * padX;
        float ascent = font.getFontDescriptor().getAscent() / 1000f * fontSize;

        // Calculate wrapped lines and max row height
        List<List<String>> allCellLines = new ArrayList<>();
        int maxLines = 0;
        for (String cell : cells) {
            List<String> lines = getWrappedLines(cell, font, fontSize, cellTextWidth);
            allCellLines.add(lines);
            if (lines.size() > maxLines) {
                maxLines = lines.size();
            }
        }
        if (maxLines == 0)
            maxLines = 1;

        // Calculate actual row height
        float textBlockHeight = (maxLines * fontSize) + ((maxLines - 1) * lineSpacing);
        float rowHeight = textBlockHeight + 2 * padY;

        // Draw grid lines
        float rowWidth = colWidth * cells.length;
        if (isHeader) {
            cs.moveTo(startX, yTop);
            cs.lineTo(startX + rowWidth, yTop);
            cs.stroke();
        }
        cs.moveTo(startX, yTop - rowHeight);
        cs.lineTo(startX + rowWidth, yTop - rowHeight);
        cs.stroke();

        float xGrid = startX;
        for (int i = 0; i <= cells.length; i++) {
            cs.moveTo(xGrid, yTop);
            cs.lineTo(xGrid, yTop - rowHeight);
            cs.stroke();
            xGrid += colWidth;
        }

        // Draw wrapped text
        float yText = yTop - padY - ascent;
        float xText = startX + padX;

        for (List<String> lines : allCellLines) {
            float currentY = yText;
            for (String line : lines) {
                cs.beginText();
                cs.setFont(font, fontSize);
                cs.newLineAtOffset(xText, currentY);
                cs.showText(line);
                cs.endText();
                currentY -= (fontSize + lineSpacing);
            }
            xText += colWidth;
        }

        return rowHeight;
    }

    // helper function to wrap lines
    private List<String> getWrappedLines(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add(""); // Add an empty line to still render the cell
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty())
                continue;

            String testLine;
            if (line.length() == 0) {
                testLine = word;
            } else {
                testLine = line.toString() + " " + word;
            }
            float width = font.getStringWidth(testLine) / 1000f * fontSize;

            if (width <= maxWidth) {
                line = new StringBuilder(testLine);
            } else {
                if (line.length() > 0) {
                    lines.add(line.toString());
                }
                line = new StringBuilder(word);
                float wordWidth = font.getStringWidth(line.toString()) / 1000f * fontSize; // check for width of word
                if (wordWidth > maxWidth) {
                    String longWord = line.toString();
                    line = new StringBuilder();
                    for (int j = 0; j < longWord.length(); j++) {
                        char c = longWord.charAt(j);
                        String testCharLine = line.toString() + c;
                        float charWidth = font.getStringWidth(testCharLine) / 1000f * fontSize;
                        if (charWidth <= maxWidth) {
                            line.append(c);
                        } else {
                            if (line.length() > 0) {
                                lines.add(line.toString());
                            }
                            line = new StringBuilder(String.valueOf(c));
                        }
                    }
                }
            }
        }
        lines.add(line.toString()); // add the last line
        return lines;
    }

    private float calcRowHeight(String[] cells, PDFont font, float fontSize, float colWidth) throws IOException {
        float padX = 6f;
        float padY = 6f;
        float lineSpacing = 2f;
        float cellTextWidth = colWidth - 2 * padX;

        int maxLines = 0;
        for (String cell : cells) {
            List<String> lines = getWrappedLines(cell, font, fontSize, cellTextWidth);
            if (lines.size() > maxLines) {
                maxLines = lines.size();
            }
        }
        if (maxLines == 0)
            maxLines = 1; // ensure at least 1 line height

        float textBlockHeight = (maxLines * fontSize) + ((maxLines - 1) * lineSpacing);
        return textBlockHeight + 2 * padY;
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
            float headerHeight = drawRow(cs, y, margin, colWidth, header, fontHeader, fontSize, true);
            y -= headerHeight;
            for (SessionAttendance sa : sessAttendances) {
                String[] row = buildRow(sa, report);

                float potentialRowHeight = calcRowHeight(row, fontText, fontSize, colWidth);
                // Re-draws header on new page if not enough space
                if (y - potentialRowHeight < margin) {
                    cs.close();
                    page = new PDPage(pageSize);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = pageSize.getHeight() - margin;
                    
                    //redraw header on new page
                    float newHeaderHeight = drawRow(cs,y,margin,colWidth,header,fontHeader,fontSize,true);
                    y-= newHeaderHeight;
                }
                float actualheight = drawRow(cs, y, margin, colWidth, row, fontText, fontSize, false);
                y-= actualheight;
            }

            cs.close();
            doc.save(getFilepath());
            System.out.println("PDF successfully written to " + getFilepath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
