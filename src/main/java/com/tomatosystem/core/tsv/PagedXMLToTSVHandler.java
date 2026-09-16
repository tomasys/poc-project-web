package com.tomatosystem.core.tsv;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

public class PagedXMLToTSVHandler extends DefaultHandler {

    private PagedTSVWriter pagedTSVWriter;
    private int ignoreCount;
    private String currentColumnId;
    private StringBuilder currentValue;

    public PagedXMLToTSVHandler(PagedTSVWriter writer, int headerLines) {
        this.pagedTSVWriter = writer;
        this.ignoreCount = headerLines;
        this.currentValue = new StringBuilder();
    }

    @Override
    public void startDocument() throws SAXException {
        // Initialize resources or reset variables here if needed
    }

    @Override
    public void endDocument() throws SAXException {
        // Cleanup resources here
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("Row".equals(qName)) {
//            if (ignoreCount > 0) {
//                ignoreCount--;
//                return;
//            }
            try {
                pagedTSVWriter.beginNewLine();  // Begin a new line for each row
            } catch (IOException e) {
                throw new SAXException("Error starting a new line", e);
            }
        } else if ("Col".equals(qName)) {
            currentColumnId = attributes.getValue("id");
            currentValue.setLength(0); // Reset current value for new column
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentColumnId != null) {
            currentValue.append(ch, start, length);  // Collect characters for the current column
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("Col".equals(qName)) {
            // Write out the value for this column
            try {
                pagedTSVWriter.writCellValue(currentValue.toString().trim());
            } catch (IOException e) {
                throw new SAXException("Error writing cell value", e);
            }
        } else if ("Row".equals(qName)) {
            // End of row, no additional action needed
        }
    }
}