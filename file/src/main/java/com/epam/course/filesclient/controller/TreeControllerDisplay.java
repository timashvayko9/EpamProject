package com.epam.course.filesclient.controller;

import com.epam.course.filesclient.model.Note;
import com.epam.course.filesclient.model.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class TreeControllerDisplay {


    public static final String HOME = "G:\\qwe";

    public String CURRENT = HOME;

    private final NoteRepository noteRepository;

    @Autowired
    public TreeControllerDisplay(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @GetMapping("/main")
    public String main(Map<String, Object> model, @RequestParam(name = "path", required = false) String path) throws IOException {
        List<String> foldersnames = new ArrayList<>();
        List<String> filenames = new ArrayList<>();

        Iterable<Note> allNotes = noteRepository.findAll();
        List<String> pathsWithNotes = new ArrayList<>();

        if (path != null) {
            if (path.equals("...") && !HOME.equals(CURRENT)) {
                CURRENT = CURRENT.substring(0, CURRENT.lastIndexOf("\\"));
            } else if (pathIsDirectory(path)) {
                CURRENT += "\\" + path;
            } else if (pathIsFile(path)) {
                String content = new String(Files.readAllBytes(Paths.get(CURRENT + "\\" + path)), StandardCharsets.UTF_8);
                model.put("content", content);
                model.put("path", CURRENT + "\\" + path);
                return "content";
            }
            if (!HOME.equals(CURRENT)) {
                foldersnames.add("...");
            }
        }

        File folder = new File(CURRENT);
        File[] listOfFolders = folder.listFiles(File::isDirectory);
        File[] listOfFiles = folder.listFiles(File::isFile);
        foldersnames.addAll(Arrays.stream(listOfFolders).map(x -> x.getName()).collect(Collectors.toList()));
        filenames.addAll(Arrays.stream(listOfFiles).map(x -> x.getName()).collect(Collectors.toList()));

        getAllNotes(allNotes, pathsWithNotes);

        model.put("notes", allNotes);
        model.put("hasnotes", pathsWithNotes);
        model.put("files", filenames);
        model.put("folders", foldersnames);
        return "main";
    }

    private void getAllNotes(Iterable<Note> allNotes, List<String> pathsWithNotes) {
        for (Note allNote : allNotes) {
            allNote.setPath(allNote.getPath().replace(CURRENT + "\\", ""));
            pathsWithNotes.add(allNote.getPath());
        }
    }

    private boolean pathIsDirectory(String path) {
        File file = new File(CURRENT + "\\" + path);
        return file.isDirectory();
    }

    private boolean pathIsFile(String path) {
        File file = new File(CURRENT + "\\" + path);
        return file.isFile();
    }

    @GetMapping("/main/note/create")
    public String createNote(@RequestParam(name = "path") String path, @RequestParam(name = "note") String note) {
        String newPath = CURRENT + "\\" + path;
        Note newNote = new Note(newPath, note);
        noteRepository.save(newNote);
        return "redirect:/main";
    }

    @GetMapping("/main/create")
    public String create(@RequestParam(name = "name") String name, @RequestParam(name = "isDirectory", required = false) String isDirectory) throws IOException {
        if (isDirectory != null) {
            new File(CURRENT + "\\" + name).mkdir();
        } else {
            String text = "";
            Path path = Paths.get(CURRENT + "\\" + name);
            Files.write(path, text.getBytes());
        }
        return "redirect:";
    }

    @PostMapping("/main/upload")
    public String singleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:";
        }
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(CURRENT + "\\" + file.getOriginalFilename());
            Files.write(path, bytes);
            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:";
    }

    @RequestMapping(value = "main/download", method = RequestMethod.GET)
    public ResponseEntity<Object> downloadFile(@RequestParam("filename") String filename) throws IOException {
        String fileName = CURRENT + "\\" + filename;
        File file = new File(fileName);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", String.format("attachment;fileName=\"%s\"", file.getName()));
        headers.add("Cache-Control", "no-cache,no store,must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/txt")).body(resource);

        return responseEntity;
    }

    @PostMapping("/main/delete")
    public String delete(@RequestParam(name = "name") String name) {
        String path = CURRENT + "\\" + name;
        File file = new File(path);
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            file.delete();
        }
        noteRepository.deleteNoteByPath(path);
        return "redirect:";
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @GetMapping("/main/note/delete")
    public String deleteNote(@RequestParam(name = "path") String path) {
        String newPath = CURRENT + "\\" + path;
        noteRepository.deleteNoteByPath(newPath);
        return "redirect:/main";
    }

    @GetMapping("/content/edit")
    public String editFile(@RequestParam(name = "text") String text, @RequestParam(name = "path") String path) throws IOException {
        File file = new File(path);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] strToBytes = text.getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
        return "redirect:/main";
    }
}
