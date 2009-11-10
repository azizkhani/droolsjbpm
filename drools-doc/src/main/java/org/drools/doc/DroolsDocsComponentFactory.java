package org.drools.doc;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

public class DroolsDocsComponentFactory {

    private static final int    INTENTATION_LEFT   = 20;

    private static final String INDENT             = "    ";

    private static final Font   CHAPTER_TITLE      = FontFactory.getFont( FontFactory.TIMES,
                                                                          20,
                                                                          Font.BOLD );
    private static final Font   PACKAGE_NAME       = FontFactory.getFont( FontFactory.TIMES,
                                                                          10,
                                                                          Font.BOLD );
    private static final Font   RULE_PACKAGE_TITLE = FontFactory.getFont( FontFactory.TIMES,
                                                                          24,
                                                                          Font.BOLD );
    private static final Font   CATEGORIES_TEXT    = FontFactory.getFont( FontFactory.TIMES,
                                                                          12 );
    private static final Font   BODY_TEXT          = FontFactory.getFont( FontFactory.TIMES,
                                                                          10 );
    static final Font           HEADER_FOOTER_TEXT = FontFactory.getFont( FontFactory.TIMES,
                                                                          8 );

    public static Table newDescription(String description) throws DocumentException {
        if ( description == null || "".equals( description ) ) {
            description = " - ";
        }

        Table table = newTable();

        Cell headerCell = newHeaderCell( "Description",
                                         CATEGORIES_TEXT );
        table.addCell( headerCell );

        table.addCell( newCell( description ) );

        return table;
    }

    private static Table newTable() throws BadElementException {
        Table table = new Table( 1 );

        table.setBorderWidthTop( 1 );
        table.setBorderWidthLeft( 1 );
        table.setBorderWidthRight( 1 );
        table.setBorderWidthBottom( 0 );
        table.setWidth( 100 );
        table.setPadding( 3 );
        table.setAlignment( Table.ALIGN_LEFT );

        return table;
    }

    public static void createOtherItems(Document document,
                                        Map<String, java.util.List<String>> other) throws DocumentException {

        for ( String key : other.keySet() ) {
            document.add( newTable( key,
                                    other.get( key ) ) );
        }
    }

    public static Table newRuleTable(DrlRuleData drl) throws BadElementException,
                                                     DocumentException {
        Table table = newTable();

        Cell headerCell = newHeaderCell( "Attributes",
                                         CATEGORIES_TEXT );
        table.addCell( headerCell );

        for ( String s : drl.header ) {
            table.addCell( newCell( INDENT + s.trim() ) );
        }

        table.addCell( newHeaderCell( INDENT + "WHEN",
                                      BODY_TEXT ) );
        for ( String s : drl.lhs ) {
            table.addCell( newCell( INDENT + INDENT + s.trim() ) );
        }

        table.addCell( newHeaderCell( INDENT + "THEN",
                                      BODY_TEXT ) );
        for ( String s : drl.rhs ) {
            table.addCell( newCell( INDENT + INDENT + s.trim() ) );
        }
        // table.addCell( newEmptyWhenThenCell( "END" ) );

        return table;
    }

    public static Table newTable(final String topic,
                                 Collection<String> items) throws BadElementException,
                                                          DocumentException {
        Table table = newTable();

        Cell headerCell = newHeaderCell( topic,
                                         CATEGORIES_TEXT );
        table.addCell( headerCell );

        if ( items.isEmpty() ) {
            table.addCell( newCell( " - " ) );
        } else {
            for ( String s : items ) {
                table.addCell( newCell( s ) );
            }
        }

        return table;
    }

    public static List createContents(java.util.List<DrlRuleData> rules) {
        List index = new List( true );

        for ( DrlRuleData drlRuleData : rules ) {
            Chunk chunk = new Chunk( drlRuleData.ruleName );
            // chunk.setLocalGoto( item.getName() );
            ListItem listItem = new ListItem( chunk );
            index.add( listItem );
        }

        return index;
    }

    private static Cell newHeaderCell(String text,
                                      Font font) throws BadElementException {
        Cell c = new Cell( new Phrase( text,
                                       font ) );
        c.setBackgroundColor( Color.decode( "#CCCCFF" ) );
        c.setLeading( 10 );
        c.setBorder( 1 );

        return c;
    }

    private static Cell newCell(String text) throws BadElementException {
        Cell c = new Cell( new Phrase( text,
                                       BODY_TEXT ) );
        c.setLeading( 10 );
        c.setBorder( 0 );
        c.setBorderWidthBottom( 1 );

        return c;
    }

    public static HeaderFooter createFooter(String packageName) {
        HeaderFooter footer = new HeaderFooter( new Phrase( packageName + "-",
                                                            HEADER_FOOTER_TEXT ),
                                                true );
        footer.setBorder( 1 );
        footer.setAlignment( Element.ALIGN_RIGHT );

        return footer;
    }

    public static void newRulePage(Document document,
                                   String packageName,
                                   DrlRuleData drlData) throws DocumentException {

        document.add( new Paragraph( packageName,
                                     PACKAGE_NAME ) );
        document.add( new Paragraph( "Rule " + drlData.ruleName,
                                     CHAPTER_TITLE ) );

        // Extends
        int index = drlData.ruleName.lastIndexOf( "extends" );
        if ( index > 0 ) {
            document.add( new Paragraph( "Extends:",
                                         BODY_TEXT ) );

            Paragraph ext = new Paragraph( drlData.ruleName.substring( "extends".length() + index ),
                                           BODY_TEXT );
            ext.setIndentationLeft( INTENTATION_LEFT );
            document.add( ext );
        }

        // Description
        document.add( newDescription( drlData.description ) );

        // DRL
        document.add( newRuleTable( drlData ) );

        // Meta data
        document.add( newTable( "Meta Data",
                                drlData.metadata ) );

        // Other
        createOtherItems( document,
                          drlData.otherInformation );

        document.newPage();
    }

    public static void createFirstPage(Document document,
                                       String currentDate,
                                       DrlPackageData packageData) throws DocumentException {
        Paragraph title = new Paragraph( "\n\n\n\n\n" + packageData.packageName.toUpperCase(),
                                         RULE_PACKAGE_TITLE );
        title.setAlignment( Element.ALIGN_CENTER );
        document.add( title );

        Paragraph date = new Paragraph( "Documentation created: " + currentDate,
                                        BODY_TEXT );
        date.setAlignment( Element.ALIGN_CENTER );
        document.add( date );

        document.add( new Paragraph( "\n\n\n\n\n" + packageData.description,
                                     BODY_TEXT ) );
        document.add( newTable( "Meta Data ",
                                packageData.metadata ) );
        document.add( newTable( "Globals ",
                                packageData.globals ) );
        createOtherItems( document,
                          packageData.otherInformation );
    }
}

class EndPage extends PdfPageEventHelper {
    private final String currentDate;

    public EndPage(String currentDate) {
        this.currentDate = currentDate;
    }

    public void onEndPage(PdfWriter writer,
                          Document document) {

        try {
            Image image = Image.getInstance( DroolsDocsBuilder.class.getResource( "drools-guvnor.png" ) );
            image.setAlignment( Image.RIGHT );
            image.scaleAbsolute( 100,
                                 30 );
            Rectangle page = document.getPageSize();
            PdfPTable head = new PdfPTable( 2 );

            PdfPCell cell1 = new PdfPCell( image );
            cell1.setHorizontalAlignment( Element.ALIGN_LEFT );
            cell1.setBorder( 0 );

            head.addCell( cell1 );

            PdfPCell cell2 = new PdfPCell( new Phrase( currentDate,
                                                       DroolsDocsComponentFactory.HEADER_FOOTER_TEXT ) );
            cell2.setHorizontalAlignment( Element.ALIGN_RIGHT );
            cell2.setBorder( 0 );

            head.addCell( cell2 );

            head.setTotalWidth( page.getWidth() - document.leftMargin() - document.rightMargin() );
            head.writeSelectedRows( 0,
                                    -1,
                                    document.leftMargin(),
                                    page.getHeight() - document.topMargin() + head.getTotalHeight(),
                                    writer.getDirectContent() );

        } catch ( Exception e ) {
            throw new ExceptionConverter( e );
        }
    }
}
