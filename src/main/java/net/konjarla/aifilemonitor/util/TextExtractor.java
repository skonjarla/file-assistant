package net.konjarla.aifilemonitor.util;

import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextExtractor {

    public static String extractText(File file) throws IOException, TikaException, SAXException {
        try (InputStream stream = new FileInputStream(file)) {
            return extractText(stream, file.getName());
        }
    }

    public static Map<String, String> extractMetadata(File file) throws IOException, TikaException, SAXException {
        try (InputStream stream = new FileInputStream(file)) {
            return extractMetadata(stream, file.getName());
        }
    }

    public static String extractText(InputStream inputStream, String fileNameHint)
            throws IOException, TikaException, SAXException {

        // For PDF files, enable image extraction
        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setExtractInlineImages(true);// extractInlineImages
        pdfConfig.setOcrStrategy(PDFParserConfig.OCR_STRATEGY.OCR_ONLY);
        // pdfConfig.setExtractUniqueInlineImagesOnly(true);
        TesseractOCRConfig ocrConfig = new TesseractOCRConfig();
        ocrConfig.setLanguage("eng");
        // ocrConfig.addOtherTesseractConfig();


        ParseContext parseContext = new ParseContext();
        parseContext.set(TesseractOCRConfig.class, ocrConfig);
        parseContext.set(PDFParserConfig.class, pdfConfig);

        /*EmbeddedDocumentExtractor embeddedDocumentExtractor =
                new EmbeddedDocumentExtractor() {
                    @Override
                    public boolean shouldParseEmbedded(Metadata metadata) {
                        return true;
                    }
                    @Override
                    public void parseEmbedded(InputStream stream, ContentHandler handler, Metadata metadata, boolean outputHtml)
                            throws SAXException, IOException {
                        Path outputDir = new File(fileNameHint + "_").toPath();
                        Files.createDirectories(outputDir);

                        Path outputPath = new File(outputDir.toString() + "/" + metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).toPath();
                        Files.deleteIfExists(outputPath);
                        Files.copy(stream, outputPath);
                    }
                };

        parseContext.set(EmbeddedDocumentExtractor.class, embeddedDocumentExtractor);*/

        parseContext.set(Parser.class, new AutoDetectParser());

        // Create a Tika AutoDetectParser.
        Parser parser = new AutoDetectParser();

        // Create a Metadata object.
        Metadata metadata = new Metadata();

        // If a file name hint is provided, add it to the metadata.
        // This can assist Tika in identifying the file type, especially for streams
        // where the type isn't immediately obvious from the initial bytes.
        if (fileNameHint != null && !fileNameHint.isEmpty()) {
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileNameHint);
        }

        // Create a ContentHandler to process the extracted text.
        ContentHandler handler = new BodyContentHandler();

        // Create a ParseContext.
        //ParseContext context = new ParseContext();

        // Parse the input stream.
        // Tika will automatically detect the file type from the stream's content
        // and potentially use the fileNameHint.
        parser.parse(inputStream, handler, metadata, parseContext);

        // Return the extracted text.
        return handler.toString();
    }

    public static Map<String, String> extractMetadata(InputStream inputStream, String fileNameHint)
            throws IOException, TikaException, SAXException {

        // Create a Tika AutoDetectParser.
        Parser parser = new AutoDetectParser();

        // Create a Metadata object.
        Metadata metadata = new Metadata();

        // If a file name hint is provided, add it to the metadata.
        // This can assist Tika in identifying the file type, especially for streams
        // where the type isn't immediately obvious from the initial bytes.
        if (fileNameHint != null && !fileNameHint.isEmpty()) {
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileNameHint);
        }

        // Create a ContentHandler to process the extracted text.
        ContentHandler handler = new BodyContentHandler();

        // Create a ParseContext.
        ParseContext context = new ParseContext();

        // Parse the input stream.
        // Tika will automatically detect the file type from the stream's content
        // and potentially use the fileNameHint.
        parser.parse(inputStream, handler, metadata, context);

        // Return the extracted text.
        return Arrays.stream(metadata.names())
                .collect(Collectors.toMap(Function.identity(), metadata::get));
    }
}
