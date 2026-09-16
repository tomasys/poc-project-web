package com.tomatosystem.core.push;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;
import org.springframework.web.socket.WebSocketSession;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.spring.JSONDataView;
import com.tomatosystem.core.resource.AppProperties;

@Controller
@RequestMapping("/websocket")
public class WebSocketController {
	
	LocalDateTime currentDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String toDate = currentDateTime.format(formatter);
    
	// @TODO 나중에 파일명 바꿀 것
	private String fileName = File.separator +"logs-" + toDate + ".txt";

	@RequestMapping("/onLoad.do")
	public View onLoad(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest)
			throws Exception {

		Map<String, WebSocketSession> sessions = MyWebSocketHandler.getActiveSessions();
		
		System.out.println("WebSocket sessions size : " + sessions.size());

		String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
		String strFileStorePath = AppProperties.getProperty("cmn.fileStorePath");
		File directory = new File(strGlobalFileStorePath + strFileStorePath + fileName);
		List<Map<String, String>> list = new ArrayList<>();
		String filePath = directory.getPath();
		if (directory.exists()) {
			try {
				List<String> lines = Files.readAllLines(Paths.get(filePath));
	
				for (String line : lines) {
					Map<String, String> map = new HashMap<>();
	
					// 콤마로 항목 분리
					String[] pairs = line.split(",");
	
					for (String pair : pairs) {
						String[] keyValue = pair.split("=");
	
						if (keyValue.length == 2) {
							String key = keyValue[0].trim();
							String value = keyValue[1].trim();
							map.put(key, value);
						}
					}
	
					if (!map.isEmpty()) {
						list.add(map);
					}
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		List<Map<String, String>> listConnect = new ArrayList<>();

		int index = 0;
		for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
			Map<String, String> map = new HashMap<>();
			WebSocketSession session = entry.getValue();
			
			String ip = (String) session.getAttributes().get("ip");
			String os = (String) session.getAttributes().get("os");
			String brower = (String) session.getAttributes().get("brower");
			String userType = (String) session.getAttributes().get("userType");
			String userNm = (String) session.getAttributes().get("userNm");
			String acsDate = (String) session.getAttributes().get("acsDate");
			
			System.out.println("WebSocket connect userNm : " + userNm);
			
			map.put("userId", session.getAttributes().get("userId").toString());
			map.put("os", os);
			map.put("ip", ip);
			map.put("browser", brower);
			map.put("userType", userType);
			map.put("userNm", userNm);
			map.put("date", acsDate);

			if (!map.isEmpty()) {
				listConnect.add(map);
			}

			index++;
		}

		dataRequest.setResponse("dsConnectStatus", listConnect);
		dataRequest.setResponse("dsTerminal", list);

		return new JSONDataView();
	}

	@PostMapping("/setLogs.do")
	@ResponseBody
	public String setLogs(@RequestBody Map<String, String> body) {
		System.out.println("클라이언트로부터 받은 텍스트: " + body.get("message"));

		String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
		String strFileStorePath = AppProperties.getProperty("cmn.fileStorePath");
		File directory = new File(strGlobalFileStorePath + strFileStorePath);

		String folderPath = directory.getPath();

		try {
			// 폴더가 없으면 생성
			File folder = new File(folderPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			// 파일 객체 생성
			File file = new File(folderPath + fileName);

			// 파일에 내용 쓰기
			FileWriter writer = new FileWriter(file, true);
			writer.write(body.get("message"));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "저장 완료";
	}

//	@PostMapping("/list.do")
//	public ResponseEntity<String> list(HttpServletRequest request, HttpServletResponse resp, DataRequest dataRequest) {
//
//		// 로컬 경로 설정
//		String strGlobalFileStorePath = AppProperties.getProperty("Globals.fileStorePath");
//		String strFileStorePath = AppProperties.getProperty("cmn.fileStorePath");
//		Path filePath = Paths.get(strGlobalFileStorePath + strFileStorePath + fileName);
//		
//		File directory = new File(strGlobalFileStorePath + strFileStorePath);
//		String folderPath = directory.getPath();
//		
//		try {
//			
//			// 폴더가 없으면 생성
//			File folder = new File(folderPath);
//			if (!folder.exists()) {
//				folder.mkdirs();
//			}
//
//			// 파일 객체 생성
//			File file = new File(folderPath + fileName);
//			String content = "";
//			if (!file.exists()) {
//				// 파일에 내용 쓰기
//				FileWriter writer = new FileWriter(file);
//				writer.write(content);
//				writer.close();
//			}
//			content = Files.readString(filePath, StandardCharsets.UTF_8);
//			return ResponseEntity.ok(content);
//
//		} catch (IOException e) {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
////			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("파일을 읽을 수 없습니다: " + e.getMessage());
//		}
//	}

}
