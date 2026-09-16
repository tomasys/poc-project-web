package com.tomatosystem.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.TableCell.BorderEdge;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xslf.usermodel.XSLFTheme;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBaseStyles;
import org.openxmlformats.schemas.drawingml.x2006.main.CTFontCollection;
import org.openxmlformats.schemas.drawingml.x2006.main.CTFontScheme;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOfficeStyleSheet;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextFont;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.XBConfig;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.spring.JSONDataView;

@Controller
@RequestMapping("/slide")
public class CreateSlideController {
	
	public CreateSlideController() {
	}
	
	private String convertDir = XBConfig.getServletContext().getRealPath("") + File.separator + "png";
	
	private String templateDir = XBConfig.getServletContext().getRealPath("") + "ui" + File.separator + "data" + File.separator + "2_exbuilder6Pros" + File.separator + "template";
	
	private String outDir = XBConfig.getInstance().getFileUploadConfig().getTempDir();
	
	@RequestMapping("/slideToPng.do")
	public void convertPPTtoImg(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {

		String fileName = dataRequest.getParameter("fileName");
		File newFile = null;
		if (!"".equals(fileName)) {
			String path = templateDir + File.separator + fileName;
			newFile = new File(path);
			
			if(!newFile.exists()) {
				return;
			}
			
			// temp 파일 이동
			String fName = newFile.getName().replace(".tmp", "");
			File file = new File(outDir + File.separator + fName);
			Files.copy(newFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// directory 확인
			int realFileNameIndex = newFile.getName().indexOf(".ppt");
			String readlFileName = newFile.getName().substring(0, realFileNameIndex);

			// 'png' folder 생성
			File rootImgFolder = new File(convertDir);
			if (!rootImgFolder.exists() || !rootImgFolder.isDirectory()) {
				rootImgFolder.mkdir();
			}

			// png 폴더 하위 폴더 생성 (파일명으로 폴더명 생성)
			String convertFilePath = convertDir + File.separator + encodingFileName(request, response, readlFileName);
			File folder = new File(convertFilePath);
			if (folder.exists() && folder.isDirectory()) {
				File[] folder_list = folder.listFiles(); // 파일리스트 얻어오기

				for (int j = 0; j < folder_list.length; j++) {
					folder_list[j].delete(); // 파일 삭제
				}
			} else {
				folder.mkdir();
			}

			convertFile(request, response, file, convertFilePath, readlFileName);
		}
	}
	
	@RequestMapping("/getSlideImage.do")
	public View getImage(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		Map<String, Object> paramMap = new HashMap<String, Object>();

		String[] params = dataRequest.getParameterNames();
		for(String name : params) {
			
			String fileName = dataRequest.getParameter(name);
			String strFolderNm = fileName.substring(0, fileName.indexOf(".ppt"));
			
			String folderPath = convertDir + File.separator + encodingFileName(request, response, strFolderNm);
			File folder = new File(folderPath);
			if(folder.exists() && folder.isDirectory()) {
				File[] folder_list = folder.listFiles(); //파일리스트 얻어오기
				
				int fileIndex = 0;
				for (int j = 0; j < folder_list.length; j++) {
					if(folder_list[j].isFile()) {
						String imageName = folder_list[j].getName();
						String num = imageName.substring(imageName.lastIndexOf("_"), imageName.lastIndexOf("."));
						
						String realImagePath = folder_list[j].getPath();
						paramMap.put("src" + num, realImagePath.replace(XBConfig.getServletContext().getRealPath(""), "/"));
						
						fileIndex++;
					}
				}
			}
			
			data.add(paramMap);
		}
		
		// dsImage : alerterColumnLayout = server
		dataRequest.setResponse("dsImage", data);
		
		return new JSONDataView();
	}
	
	@RequestMapping("/createSlide.do")
	public void createTemplate(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		String data = dataRequest.getParameter("data");
		String[] txtArr = data.split("&");
		
		String strParamNm = dataRequest.getParameter("fileName"); // FIXME dmData.getValue("strFileName") 로 수정
		String fileName = encodingFileName(request, response, strParamNm);
		
		// check temp directory exist
		File folder = new File(outDir);
		if (!folder.exists() || !folder.isDirectory()) {
			folder.mkdir();
		}
		
		File file = null; // original File
		File file2 = null; // download Tmp File
		XMLSlideShow ppt = null;
		FileOutputStream out = null;
		
		// read pptx file
		String outFilePath = outDir + File.separator + fileName; // C:\\Temp\\fileName
		file = new File(outFilePath);
		FileInputStream inputstream = new FileInputStream(file);
		ppt = new XMLSlideShow(inputstream);
		
		try {
			// open pptx slide show
			List<XSLFSlide> slides = ppt.getSlides();
			for (int i = 1; i < slides.size(); i++) {
				
				// 맺음말 포함 구분이 "미포함"인 경우 값 받아오는 방식 수정
				String newText = i > txtArr.length ? "": txtArr[i-1]; 
				if(newText == null) continue;
				
				JSONObject jsonArr = parseStingtoJson(request, response, newText);
				String tableConfig = (String) jsonArr.get("table-config"); // 표 정보
				String targetKey = "";
				String headerText = "";
				if(!"".equals(tableConfig) && tableConfig != null) {
					String[] config = tableConfig.split("/");
					targetKey = config[0];
					headerText = config[1];
				}
				
				List<XSLFTextShape> removeShape = new ArrayList<XSLFTextShape>();
				
				XSLFSlide slide = slides.get(i);
				List<XSLFShape> shapes = slide.getShapes();
				
				for (int j = 0; j < shapes.size(); j++) {
					XSLFShape shape = shapes.get(j);
					
					if (shape instanceof XSLFTextShape) {
						
						XSLFTextShape textShape = (XSLFTextShape) shape;
						String text = textShape.getText();
						
						// find Input Tag
						if (text.indexOf("<input>") != -1) {
							// 태그 삭제 후, json 데이터로 대치
							String textNum = text.replace("<input>", "").replace("</input>", "");
							String boxText = (String) jsonArr.get(textNum); // 대치 텍스트
							if("null".equals(boxText)) boxText = null;
							
							removeShape.add(textShape);
							
							if (!"".equals(tableConfig) && tableConfig != null && textNum.equals(targetKey)) {

								// 표그리기
								XSLFTable table = slide.createTable();
								table.setAnchor(textShape.getAnchor());
								
								XSLFTableRow headerRow = table.addRow(); // 헤더 추가
								headerRow.setHeight(30);
								
								// header
								String[] headerTxt = headerText.split(",");
								for (int k = 0; k < headerTxt.length; k++) {
									XSLFTableCell headerCell = headerRow.addCell();
							        XSLFTextParagraph p = headerCell.addNewTextParagraph();
							        p.setTextAlign(TextAlign.CENTER);
							       
							        XSLFTextRun headerRun = p.addNewTextRun();
							        headerRun.setText(headerTxt[k]);
							        headerRun.setFontFamily("맑은 고딕");
							        headerRun.setFontColor(new Color(54, 74, 99));
							        headerCell.setFillColor(new Color(244, 245, 250));
							        
							        // border-style
							        headerCell.setBorderDash(BorderEdge.top, StrokeStyle.LineDash.SOLID);
							        headerCell.setBorderDash(BorderEdge.left, StrokeStyle.LineDash.SOLID);
							        headerCell.setBorderDash(BorderEdge.bottom, StrokeStyle.LineDash.SOLID);
							        headerCell.setBorderDash(BorderEdge.right, StrokeStyle.LineDash.SOLID);
							        // border-color
							        headerCell.setBorderColor(BorderEdge.top, new Color(237, 237, 237));
							        headerCell.setBorderColor(BorderEdge.left, new Color(237, 237, 237));
							        headerCell.setBorderColor(BorderEdge.bottom, new Color(237, 237, 237));
							        headerCell.setBorderColor(BorderEdge.right, new Color(237, 237, 237));
					        		
							        table.setColumnWidth(k, 150);
								}
								
								// rows
								if("".equals(boxText)) continue;
								String[] rows = boxText.split("\n");
							    for (int k2 = 0; k2 < rows.length; k2++) {
							        XSLFTableRow row = table.addRow();
							        row.setHeight(30);
							        
							        String[] rowData = rows[k2].split(",");
							        // cell
						        	for (int k3 = 0; k3 < rowData.length; k3++) {
						        		XSLFTableCell cell = row.addCell();
						        		XSLFTextParagraph p = cell.addNewTextParagraph();
						        		p.setTextAlign(TextAlign.CENTER);
						        		
						        		XSLFTextRun rowRun = p.addNewTextRun();
						        		rowRun.setText(rowData[k3]);
						        		rowRun.setFontFamily("맑은 고딕");
						        		cell.setFillColor(Color.white);
						        		
						        		// border-style
						        		cell.setBorderDash(BorderEdge.top, StrokeStyle.LineDash.SOLID);
						        		cell.setBorderDash(BorderEdge.left, StrokeStyle.LineDash.SOLID);
						        		cell.setBorderDash(BorderEdge.bottom, StrokeStyle.LineDash.SOLID);
						        		cell.setBorderDash(BorderEdge.right, StrokeStyle.LineDash.SOLID);
						        		// border-color
						        		cell.setBorderColor(BorderEdge.top, new Color(237, 237, 237));
						        		cell.setBorderColor(BorderEdge.left, new Color(237, 237, 237));
						        		cell.setBorderColor(BorderEdge.bottom, new Color(237, 237, 237));
						        		cell.setBorderColor(BorderEdge.right, new Color(237, 237, 237));
						        	}
							    }
								
							} else if (!"".equals(boxText) && boxText != null) {
								// 텍스트 대치
								
								PaintStyle textColor = null;
								String textFontFaily = null;
								Double textSize = null;
								Boolean textBold = false;
								
								Rectangle2D anchor = textShape.getAnchor();
								Double lineWidth = textShape.getLineWidth();
								Color lineColor = textShape.getLineColor();
								
								List<XSLFTextParagraph> paragraphs = textShape.getTextParagraphs();
								for (XSLFTextParagraph paragraph : paragraphs) {
									List<XSLFTextRun> runs = paragraph.getTextRuns();
									
									for (XSLFTextRun originRun : runs) {
										textColor = originRun.getFontColor();
										textFontFaily = originRun.getFontFamily();
										textSize = originRun.getFontSize();
										textBold = originRun.isBold();
									}
								}
								
								// create Text box
								XSLFTextBox textBox = slide.createTextBox();
								XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
								XSLFTextRun run = paragraph.addNewTextRun();
								textBox.setAnchor(anchor);
								run.setText(boxText);
								run.setFontFamily("맑은 고딕");
								
								// set Text box style
								textBox.setLineWidth(lineWidth);
								textBox.setLineColor(lineColor);
								run.setBold(textBold);
								
								if (textColor != null)
									run.setFontColor(textColor);
								if (textFontFaily != null)
									run.setFontFamily(textFontFaily);
								if (textSize != null)
									run.setFontSize(textSize);
							}
						}
					}
				}
				
				if (removeShape.size() > 0) {
					
					// delete origin Tag box
					for (XSLFTextShape rmShape : removeShape) {
						slide.removeShape(rmShape);
					}
				}
			}
			
			// saving the changes
			file2 = new File(outDir + File.separator + "temp.pptx");
			Files.copy(file.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
			out = new FileOutputStream(file2);
			ppt.write(out);
			
			downloadFile(request, response, file2, file.getName());
			
			System.out.println("Presentation edited successfully - " + strParamNm);
		} finally {
			if (out != null)
				out.close();
			if (ppt != null)
				ppt.close();
			
			if (file2 != null)
				file2.delete();
		}
	}
	
	@RequestMapping("/preview.do")
	public View preview(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		String data = dataRequest.getParameter("data");
		String[] txtArr = data.split("&");
		
		String strParamNm = dataRequest.getParameter("fileName");
		String fileName = encodingFileName(request, response, strParamNm);
		
		int realFileNameIndex = fileName.indexOf(".ppt");
		String readlFileName = fileName.substring(0, realFileNameIndex);
		
		// check temp directory exist
		File folder = new File(outDir);
		if (!folder.exists() || !folder.isDirectory()) {
			folder.mkdir();
		}
		
		// png/template/preview 폴더 생성
		String previewFilePath = convertDir + File.separator + encodingFileName(request, response, readlFileName) + File.separator + "preview";
		File previewFolder = new File(previewFilePath);
		if (previewFolder.exists() && previewFolder.isDirectory()) {
			File[] folder_list = previewFolder.listFiles(); // 파일리스트 얻어오기

			for (int j = 0; j < folder_list.length; j++) {
				folder_list[j].delete(); // 파일 삭제
			}
		} else {
			previewFolder.mkdir();
		}
		
		File file = null; // original File
		File file2 = null; // download Tmp File
		XMLSlideShow ppt = null;
		FileOutputStream out = null;
		
		// read pptx file
		String outFilePath = outDir + File.separator + fileName; // C:\\Temp\\fileName
		file = new File(outFilePath);
		FileInputStream inputstream = new FileInputStream(file);
		ppt = new XMLSlideShow(inputstream);
		
		try {
			// open pptx slide show
			List<XSLFSlide> slides = ppt.getSlides();
			List<XSLFTextShape> removeShape = null;
			
			for (int i = 0; i < slides.size(); i++) {
				
				removeShape = new ArrayList<XSLFTextShape>();
				
				XSLFSlide slide = slides.get(i);
				List<XSLFShape> shapes = slide.getShapes();
				
				if(i > 0) {
					
					// 맺음말 포함 구분이 "미포함"인 경우 값 받아오는 방식 수정
					String newText = i > txtArr.length ? "": txtArr[i-1]; 
					if(newText == null) continue;
					
					JSONObject jsonArr = parseStingtoJson(request, response, newText);
					String tableConfig = (String) jsonArr.get("table-config"); // 표 정보
					String targetKey = "";
					String headerText = "";
					if(!"".equals(tableConfig) && tableConfig != null) {
						String[] config = tableConfig.split("/");
						targetKey = config[0];
						headerText = config[1];
					}
					
					for (int j = 0; j < shapes.size(); j++) {
						XSLFShape shape = shapes.get(j);
						
						if (shape instanceof XSLFTextShape) {
							
							XSLFTextShape textShape = (XSLFTextShape) shape;
							String text = textShape.getText();
							
							// find Input Tag
							if (text.indexOf("<input>") != -1) {
								// 태그 삭제 후, json 데이터로 대치
								String textNum = text.replace("<input>", "").replace("</input>", "");
								String boxText = (String) jsonArr.get(textNum); // 대치 텍스트
								if("null".equals(boxText)) boxText = null;
								
								removeShape.add(textShape);
								
								if (!"".equals(tableConfig) && tableConfig != null && textNum.equals(targetKey)) {
									
									// 표그리기
									XSLFTable table = slide.createTable();
									table.setAnchor(textShape.getAnchor());
									
									XSLFTableRow headerRow = table.addRow(); // 헤더 추가
									headerRow.setHeight(30);
									
									// header
									String[] headerTxt = headerText.split(",");
									for (int k = 0; k < headerTxt.length; k++) {
										XSLFTableCell headerCell = headerRow.addCell();
										XSLFTextParagraph p = headerCell.addNewTextParagraph();
										p.setTextAlign(TextAlign.CENTER);
										
										XSLFTextRun headerRun = p.addNewTextRun();
										headerRun.setText(headerTxt[k]);
										headerRun.setFontFamily("맑은 고딕");
										headerRun.setFontColor(new Color(54, 74, 99));
										headerCell.setFillColor(new Color(244, 245, 250));
										
										// border-style
										headerCell.setBorderDash(BorderEdge.top, StrokeStyle.LineDash.SOLID);
										headerCell.setBorderDash(BorderEdge.left, StrokeStyle.LineDash.SOLID);
										headerCell.setBorderDash(BorderEdge.bottom, StrokeStyle.LineDash.SOLID);
										headerCell.setBorderDash(BorderEdge.right, StrokeStyle.LineDash.SOLID);
										// border-color
										headerCell.setBorderColor(BorderEdge.top, new Color(237, 237, 237));
										headerCell.setBorderColor(BorderEdge.left, new Color(237, 237, 237));
										headerCell.setBorderColor(BorderEdge.bottom, new Color(237, 237, 237));
										headerCell.setBorderColor(BorderEdge.right, new Color(237, 237, 237));
										
										table.setColumnWidth(k, 150);
									}
									
									// rows
									if("".equals(boxText)) continue;
									String[] rows = boxText.split("\n");
									for (int k2 = 0; k2 < rows.length; k2++) {
										XSLFTableRow row = table.addRow();
										row.setHeight(30);
										
										String[] rowData = rows[k2].split(",");
										// cell
										for (int k3 = 0; k3 < rowData.length; k3++) {
											XSLFTableCell cell = row.addCell();
											XSLFTextParagraph p = cell.addNewTextParagraph();
											p.setTextAlign(TextAlign.CENTER);
											
											XSLFTextRun rowRun = p.addNewTextRun();
											rowRun.setText(rowData[k3]);
											rowRun.setFontFamily("맑은 고딕");
											cell.setFillColor(Color.white);
											
											// border-style
											cell.setBorderDash(BorderEdge.top, StrokeStyle.LineDash.SOLID);
											cell.setBorderDash(BorderEdge.left, StrokeStyle.LineDash.SOLID);
											cell.setBorderDash(BorderEdge.bottom, StrokeStyle.LineDash.SOLID);
											cell.setBorderDash(BorderEdge.right, StrokeStyle.LineDash.SOLID);
											// border-color
											cell.setBorderColor(BorderEdge.top, new Color(237, 237, 237));
											cell.setBorderColor(BorderEdge.left, new Color(237, 237, 237));
											cell.setBorderColor(BorderEdge.bottom, new Color(237, 237, 237));
											cell.setBorderColor(BorderEdge.right, new Color(237, 237, 237));
										}
									}
									
								} else if (!"".equals(boxText) && boxText != null) {
									// 텍스트 대치
									
									PaintStyle textColor = null;
									String textFontFaily = null;
									Double textSize = null;
									Boolean textBold = false;
									
									Rectangle2D anchor = textShape.getAnchor();
									Double lineWidth = textShape.getLineWidth();
									Color lineColor = textShape.getLineColor();
									
									List<XSLFTextParagraph> paragraphs = textShape.getTextParagraphs();
									for (XSLFTextParagraph paragraph : paragraphs) {
										List<XSLFTextRun> runs = paragraph.getTextRuns();
										
										for (XSLFTextRun originRun : runs) {
											textColor = originRun.getFontColor();
											textFontFaily = originRun.getFontFamily();
											textSize = originRun.getFontSize();
											textBold = originRun.isBold();
										}
									}
									
									// create Text box
									XSLFTextBox textBox = slide.createTextBox();
									XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
									XSLFTextRun run = paragraph.addNewTextRun();
									textBox.setAnchor(anchor);
									run.setText(boxText);
									run.setFontFamily("맑은 고딕");
									
									// set Text box style
									textBox.setLineWidth(lineWidth);
									textBox.setLineColor(lineColor);
									run.setBold(textBold);
									
									if (textColor != null)
										run.setFontColor(textColor);
									if (textFontFaily != null)
										run.setFontFamily(textFontFaily);
									if (textSize != null)
										run.setFontSize(textSize);
								}
							}
						}
					}
				}
				
				if (removeShape.size() > 0) {
					
					// delete origin Tag box
					for (XSLFTextShape rmShape : removeShape) {
						slide.removeShape(rmShape);
					}
				}
			}
			
			file2 = new File(outDir + File.separator + "preview_temp.pptx");
			Files.copy(file.toPath(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);
			out = new FileOutputStream(file2);
			ppt.write(out);
			
			list = convertFile(request, response, file2, previewFilePath, readlFileName);
			
		} finally {
			if (out != null)
				out.close();
			if (ppt != null)
				ppt.close();
			if (file2 != null)
				file2.delete();
		}
		
		dataRequest.setResponse("dsPreview", list);
		
		return new JSONDataView();
	}
	
	private List<Map<String, Object>> convertFile(HttpServletRequest request, HttpServletResponse response, File file, String path, String fileName) throws IOException {
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		int index = 1;
		
		// creating an empty presentation
		XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(file));
		if (ppt.getSlideMasters().size() > 0) {
			List<XSLFSlideMaster> masters = ppt.getSlideMasters();
			for (XSLFSlideMaster master : masters) {
				XSLFTheme theme = master.getTheme();

				CTOfficeStyleSheet styleSheet = theme.getXmlObject();
				CTBaseStyles themeElements = styleSheet.getThemeElements();
				CTFontScheme fontScheme = themeElements.getFontScheme();
				CTFontCollection fontCollection = fontScheme.getMajorFont();
				CTTextFont textFont = null;

				// major
				textFont = fontCollection.getEa();
				textFont.setTypeface("맑은 고딕"); //

				textFont = fontCollection.getCs();
				textFont.setTypeface("맑은 고딕");

				textFont = fontCollection.getLatin();
				textFont.setTypeface("맑은 고딕");

				// minor
				fontCollection = fontScheme.getMinorFont();

				textFont = fontCollection.getEa();
				textFont.setTypeface("맑은 고딕");

				textFont = fontCollection.getCs();
				textFont.setTypeface("맑은 고딕");

				textFont = fontCollection.getLatin();
				textFont.setTypeface("맑은 고딕");
			}
		}

		// getting the dimensions and size of the slide
		Dimension pgsize = ppt.getPageSize();
		List<XSLFSlide> slides = ppt.getSlides();

		double zoom = 2; // magnify it by 2
		AffineTransform at = new AffineTransform();
		at.setToScale(zoom, zoom);
		
		List<XSLFTextShape> removeShape = null;
		
		for (int i = 0; i < slides.size(); i++) {
			XSLFSlide slide = slides.get(i);
			
			removeShape = new ArrayList<XSLFTextShape>();
			List<XSLFShape> shapes = slide.getShapes();
			
			for (int j = 0; j < shapes.size(); j++) {
				XSLFShape shape = shapes.get(j);
				if (shape instanceof XSLFTextShape) {
					XSLFTextShape textShape = (XSLFTextShape) shape;
					String text = textShape.getText();
					
					if (text.indexOf("<input>") != -1) {
						removeShape.add(textShape);
					}
				}
			}
			
			if (removeShape.size() > 0) {
				
				// delete origin Tag box
				for (XSLFTextShape rmShape : removeShape) {
					slide.removeShape(rmShape);
				}
			}
			
			BufferedImage img = new BufferedImage((int) Math.ceil(pgsize.width * zoom),
					(int) Math.ceil(pgsize.height * zoom), BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = img.createGraphics();

			// clear the drawing area
			graphics.setTransform(at);
			graphics.setPaint(Color.white);
			graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
			
			// render
			slide.draw(graphics);
			
			// creating an image file as output
			String strImageFileName = path + File.separator + encodingFileName(request, response, fileName) + "_" + index + ".png";
			FileOutputStream out = new FileOutputStream(strImageFileName);
			javax.imageio.ImageIO.write(img, "png", out);
			
			paramMap.put("src" + i, strImageFileName.replace(XBConfig.getServletContext().getRealPath("")+File.separator, "/"));
			
			System.out.println("Image successfully created - " + (fileName + "_" + index + ".png"));
			out.close();

			index++;
		}
		
		list.add(paramMap);
		
		ppt.close();
		System.out.println("------ convert successfully end - " + file.getName() + " -------");
		
		return list;
	}
	
	private void downloadFile(HttpServletRequest request, HttpServletResponse response, File file, String downloadFileName) throws IOException {

		downloadFileName = this.encodingDownloadFileName(request, response, downloadFileName);

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename=\"" + downloadFileName + "\";filename*=UTF-8''" + downloadFileName);
		response.setHeader("Content-Transfer-Encoding", "binary");

		ServletOutputStream out = response.getOutputStream();
		byte[] buffer = new byte[512];
		InputStream in = null;
		try {
			in = new FileInputStream(file);

			for (int size = 0; (size = in.read(buffer)) != -1;) {
				out.write(buffer, 0, size);
			}
		} finally {
			in.close();
			// 스트림 닫기
			out.flush();
			out.close();
		}
	}
	
	
	private JSONObject parseStingtoJson(HttpServletRequest request, HttpServletResponse response, String text) throws ParseException {
		
		text = text.replaceAll("'", "\"");
		
		if (text.indexOf("{") != 0)
			text = "{" + text;
		if (text.indexOf("}") != text.length() - 1)
			text = text + "}";

		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) parser.parse(text);

		return jsonObj;
	}

	private String encodingDownloadFileName(HttpServletRequest request, HttpServletResponse response, String downloadFileName) throws UnsupportedEncodingException {
		String userAgent = request.getHeader("User-Agent");

		if (userAgent.contains("MSIE") || userAgent.contains("Chrome") || userAgent.contains("Firefox") || (userAgent.contains("Windows") && userAgent.contains("Trident"))) {
			downloadFileName = URLEncoder.encode(downloadFileName, "utf-8");
			downloadFileName = downloadFileName.replaceAll("\\+", "%20");

			response.setHeader("Content-Disposition", "attachment;filename=\"" + downloadFileName + "\";filename*=UTF-8''" + downloadFileName);
		} else {
			response.setHeader("Content-Disposition", "attachment;filename=\"" + downloadFileName + "\"");
		}

		return downloadFileName;
	}
	
	
	private String encodingFileName(HttpServletRequest request, HttpServletResponse response, String downloadFileName) throws UnsupportedEncodingException {
		String userAgent = request.getHeader("User-Agent");
		
		if(userAgent.contains("MSIE") || userAgent.contains("Chrome") || userAgent.contains("Firefox") ||(userAgent.contains("Windows") && userAgent.contains("Trident"))){
			downloadFileName = URLEncoder.encode(downloadFileName, "utf-8");
			downloadFileName = downloadFileName.replaceAll("\\+","%20");
        }
		
		return downloadFileName;
	}
}
