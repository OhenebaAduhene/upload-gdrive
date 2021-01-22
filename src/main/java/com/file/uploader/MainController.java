package com.file.uploader;

import com.google.api.services.drive.model.File;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@AllArgsConstructor
@Slf4j
public class MainController {
	private final FileManager fileManager;

	@GetMapping({"/"})
	public ResponseEntity<List<File>> listEverything() throws IOException, GeneralSecurityException {
		List<File> files = fileManager.listEverything();
		return ResponseEntity.ok(files);
	}

	@GetMapping({"/list","/list/{parentId}"})
	public ResponseEntity<List<File>> list(@PathVariable(required = false) String parentId) throws IOException, GeneralSecurityException {
		List<File> files = fileManager.listFolderContent(parentId);
		return ResponseEntity.ok(files);
	}

	@GetMapping("/download/{id}")
	public void download(@PathVariable String id, HttpServletResponse response) throws IOException, GeneralSecurityException {
		fileManager.downloadFile(id, response.getOutputStream());
	}

	@GetMapping("/directory/create")
	public ResponseEntity<String> createDirectory(@RequestParam String path) throws Exception {
		String parentId = fileManager.getFolderId(path);
		return ResponseEntity.ok("parentId: "+parentId);
	}

	@PostMapping(value = "/upload",
			consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE} )
	public ResponseEntity<String> uploadSingleFile(@RequestBody MultipartFile[] files,@RequestParam(required = false) String path) {
		int filesize = files.length;
		AtomicReference<String> fileId = new AtomicReference<>("");
		AtomicReference<String> fileName = new AtomicReference<>("");
		Arrays.asList(files).forEach(
				file->{
					fileId.set(fileManager.uploadFile(file, path));
					fileName.set(file.getOriginalFilename());
				}
		);

		if (filesize > 1){
			return ResponseEntity.ok("files uploaded successfully");
		}
		return ResponseEntity.ok(fileName + ", uploaded successfully");
	}


	@GetMapping("/delete/{id}")
	public void delete(@PathVariable String id) throws Exception {
		fileManager.deleteFile(id);
	}

	@RequestMapping(value = "/preview/cv", method = RequestMethod.GET)
	protected String preivewSection(
			HttpServletRequest request,
			HttpSession httpSession,
			HttpServletResponse response) {
		try {
			byte[] documentInBytes = Files.readAllBytes(Path.of("root"));
			response.setDateHeader("Expires", -1);
			response.setContentType("application/pdf");
			response.setContentLength(documentInBytes.length);
			response.getOutputStream().write(documentInBytes);
		} catch (Exception ignored) {
		}
		return null;
	}
}
