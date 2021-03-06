package com.datascience9.doc.preprocessing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.CustomWord2HtmlConverter;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.w3c.dom.Document;

import com.datascience9.doc.util.DocumentConverterHelper;
import java.util.logging.Logger;

public class Doc2Html extends DocConverter {

  public Doc2Html() {}
  
  public Doc2Html(Logger logger) {
    this.logger = logger;
  }
	
	@Override
	public void extractText(Path input, Path output) {
		File newOutputDir = new File(output.toFile(), 
				DocumentConverterHelper.getFileNameWithoutExtension(input.toFile().getName()));
		newOutputDir.mkdirs();
		try {
			logger.info("start converting ... " + input);
			convert(input.toFile(), newOutputDir);
		} catch (Exception ex) { 
			developerLogger.info("start converting ... " + input);
			developerLogger.log(Level.SEVERE, "ERROR while extracting text from " + input, ex);
			logger.severe("ERROR while extracting text from Word documents. Please report this error to technical team");
		}
	}
	
	@Override
	public void extractTextFromFile(Path input, Path output) {
		try {
			logger.info("start converting ... " + input);
			convert(input.toFile(), output.toFile());
		} catch (Exception ex) { 
			developerLogger.info("start converting ... " + input);
			developerLogger.log(Level.SEVERE, "ERROR while extracting text from " + input, ex);
			logger.severe("ERROR while extracting text from Word documents. Please report this error to technical team");
		}
	}
	/**
	 * Convert doc to HTML.
	 * @param inputFile - a word document.
	 * @param outputDir - an output directory.
	 * @throws Exception - throw an exception.
	 */
	public void convert(File inputFile, File outputDir) throws  Exception {
		
		 InputStream input = new FileInputStream (inputFile);
	        HWPFDocument wordDocument = new HWPFDocument (input);
	        CustomWord2HtmlConverter wordToHtmlConverter = new CustomWord2HtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument() );
	        wordToHtmlConverter.setPicturesManager (new PicturesManager() {
	            public String savePicture (byte[] content, PictureType pictureType, String suggestedName, float widthInches, float heightInches) {
	            		File imageFile = new File(outputDir, suggestedName);
	            		try {
	            		Files.write(imageFile.toPath(), content);
	            		} catch (Exception ex) {
	            			developerLogger.severe("Unable to save image " + suggestedName + " from file " + inputFile.getAbsolutePath());
	            		}
	                return suggestedName;
	            }
	        });
	        
	        wordToHtmlConverter.processDocument (wordDocument);
	        
	        Document htmlDocument = wordToHtmlConverter.getDocument();
	        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	        DOMSource domSource = new DOMSource (htmlDocument);
	        StreamResult streamResult = new StreamResult (outStream);

	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer serializer = tf.newTransformer();
	        serializer.setOutputProperty (OutputKeys.ENCODING, "us");
	        serializer.setOutputProperty (OutputKeys.INDENT, "yes");
	        serializer.setOutputProperty (OutputKeys.METHOD, "html");
	        serializer.transform (domSource, streamResult);
	        outStream.close();

	        String content = new String (outStream.toByteArray() );
	        
	        File outputFile = new File(outputDir, 
	        		DocumentConverterHelper.getFileNameWithNewExtension(inputFile.getName(), "html"));
	        writeStringToFile (outputFile.toPath(), content);
	        
	        File styleFile = new File(outputDir, "style.css");
	        writeStringToFile (styleFile.toPath(), wordToHtmlConverter.htmlDocumentFacade.styles);
	}

}
