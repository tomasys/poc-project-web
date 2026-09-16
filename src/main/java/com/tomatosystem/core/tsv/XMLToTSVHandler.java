package com.tomatosystem.core.tsv;

import java.io.BufferedWriter;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLToTSVHandler extends DefaultHandler {
    private BufferedWriter writer;
    private StringBuilder currentValue;

    public XMLToTSVHandler(BufferedWriter writer) {
        this.writer = writer;
        this.currentValue = new StringBuilder();
    }
    
    
    @Override
    public void startDocument() throws SAXException {
    }

	@Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("Row".equals(qName)) {
        } else if ("Col".equals(qName)) {
            currentValue.setLength(0); // Reset current value for new column
        }else if("Column".equals(qName)) {
        	try {
				writer.write(attributes.getValue("id").trim());
				writer.write("\t");
			} catch (IOException e) {
				throw new SAXException("Error writing to output", e);
			}
        }
    }
	
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
            currentValue.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if ("Col".equals(qName)) {
                // Write the current column value with a tab separator
                writer.write(currentValue.toString().trim());
                writer.write("\t");
            } else if ("Row".equals(qName)) {
                // End of row - write a newline
                writer.newLine();
            }else if ("ColumnInfo".equals(qName)) {
            	writer.newLine();
            	
            }
        } catch (IOException e) {
            throw new SAXException("Error writing to output", e);
        }
    }
    @Override
    public void endDocument() throws SAXException {
        // Cleanup resources here
    	try {
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new SAXException("Error writing to output", e);
		}
    }
}