package com.example.createnewapp.controller;


import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;

@RestController
@RequestMapping("/files")
public class FileUploudController {

    public static final String uploadedDocx = "C:\\Users\\w2\\Documents\\123\\test\\CA231.docx";
    public static final String dirtyXmlFromUploadedDocxAsZip = "C:\\Users\\w2\\Documents\\123\\test\\CA231.zip\\word\\document.xml";
    public static final String newXmlFile = "C:\\Users\\w2\\Documents\\123\\dirtydocument.xml";

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> processfile(@RequestParam("file") MultipartFile file) throws Exception {
        file.transferTo(new File(uploadedDocx));
        copyFileFromArchiveToAnotherPlace(uploadedDocx);
        String dirty = readFile(newXmlFile);
        String clean = cleanXmlFromZip(dirty);
        copyCleanXmlToArchive(clean);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(uploadedDocx));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    static void copyFileFromArchiveToAnotherPlace(String zipPath) throws IOException {
        Path zipFilePath = Paths.get(zipPath);
        try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, null)) {
            Path source = fs.getPath("/word/document.xml");
            byte[] docBytes = Files.readAllBytes(source);
            Files.write(Paths.get(newXmlFile), docBytes);
        }
    }

    private boolean copyCleanXmlToArchive(String clean) throws IOException {
        Path path = Paths.get(uploadedDocx);
        try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
            Path fileIntoZip = fs.getPath("/word/document.xml");
            try (Writer writer = Files.newBufferedWriter(fileIntoZip, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                writer.write(clean);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    public String cleanXmlFromZip(String xml) {
        return xml.replaceAll("(</w:t>)([.\\S\\s:]{87,133})(<w:t>)", "");
    }

    private String readFile(String path) throws IOException {
        FileReader input = new FileReader(path);
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(input)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private void deleteFileFromArchive(String archivePath) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(Paths.get(archivePath), null)) {
            Path source = fs.getPath("word/document.xml");
            Files.delete(source);
            System.out.println(source.toAbsolutePath());
        }
    }
}






