/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hello;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;

import org.dom4j.io.DOMReader;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import hello.service.HelloWorldService;


@SpringBootApplication
public class H2p implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(H2p.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Autowired
	private HelloWorldService helloWorldService;

	@Value("${year:1}")
	private int year;
	int filesCount;
	private	int fileIdx = 0;
	//develop
	private static String basicDir ="/home/roman/jura/";
	private static String fileSeparator = "/";
	DOMReader domReader = new DOMReader();
	DateTime startMillis;
	String domain = "http://workshop-manuals.com";
	private String workDir, dirJsonName, dirLargeHtmlName, dirPdfName;
	
	Path pathStart;
	private int debugSkip;

	@Override
	public void run(String... args) {
		System.out.println(this.helloWorldService.getHelloMessage());
		System.out.println(year);
		workDir = workDir();
		dirJsonName = workDir + "OUT1json/";
		dirLargeHtmlName = workDir+ "OUT1html/";
		dirPdfName = workDir+ "OUT1pdf/";
		pathStart = Paths.get(dirLargeHtmlName);
		
		startMillis = new DateTime();
		System.out.println("--------------------");
		System.out.println(year);
		System.out.println("--------------------");
		System.out.println("The time is now " + dateFormat.format(startMillis.toDate()));
		logger.debug("The time is now " + dateFormat.format(startMillis.toDate()));
		logger.debug(pathStart.toFile()+"");
		filesCount = countFiles2(pathStart.toFile());
		System.out.println("filesCount " + filesCount);
		logger.debug("filesCount " + filesCount);
		try {
			makePdfFromHTML();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void makePdfFromHTML() throws IOException {
		Path pathHtmlLarge = Paths.get(dirLargeHtmlName);
		//		Path pathHtmlLarge = Paths.get(dirPdfName);
		logger.debug("Start folder : "+pathHtmlLarge);
		Files.walkFileTree(pathHtmlLarge, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				final FileVisitResult visitFile = super.visitFile(file, attrs);

				fileIdx++;
				logger.debug(fileIdx + "" + "/" + filesCount + procentWorkTime() + file);

				final String fileName = file.toString();
				logger.debug(fileName);
				final String[] splitFileName = fileName.split("\\.");
				final String fileExtention = splitFileName[splitFileName.length - 1];
				String[] splitPathFileName = fileName.split("/");
				logger.debug(""+splitPathFileName);
				final String fileNameShort = splitPathFileName[splitPathFileName.length - 1];
				logger.debug(""+fileNameShort);

				String hTML_TO_PDF = dirPdfName+ fileNameShort+".pdf";
				File f = new File(hTML_TO_PDF);
				if(f.exists())
				{
					logger.debug("f.exists() --  "+hTML_TO_PDF);
					return visitFile;
				}


				if("html".equals(fileExtention)){
					logger.debug(fileName);
					try {
						savePdf(fileName, hTML_TO_PDF);
						//Files.delete(file);
					} catch (com.lowagie.text.DocumentException | IOException e) {
						System.out.println(fileName);
						e.printStackTrace();
					}
				}
				return visitFile;
			}
		});}

	void savePdf(String htmlOutFileName, String HTML_TO_PDF) throws com.lowagie.text.DocumentException, IOException {
		String url = new File(htmlOutFileName).toURI().toURL().toString();
		logger.debug(procentWorkTime()+" - start - "+HTML_TO_PDF);
		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocument(url);
		renderer.layout();
		OutputStream os = new FileOutputStream(HTML_TO_PDF);
		renderer.createPDF(os);
		os.close();
		logger.debug(procentWorkTime()+" - end - "+HTML_TO_PDF);
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication application = new SpringApplication(H2p.class);
		application.setApplicationContextClass(AnnotationConfigApplicationContext.class);
		SpringApplication.run(H2p.class, args);
	}
	
	public static int countFiles2(File directory) {
		int count = 0;
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				count += countFiles2(file); 
			}else
				count++;
		}
		return count;
	}
	private String workDir() {
		return basicDir + "workshop-manuals"
				+ year
				+ "-"
				+ year
				+ "/";
	}
	static PeriodFormatter hmsFormatter = new PeriodFormatterBuilder()
			.appendHours().appendSuffix("h ")
			.appendMinutes().appendSuffix("m ")
			.appendSeconds().appendSuffix("s ")
			.toFormatter();
	String procentWorkTime() {
		int procent = fileIdx*100/filesCount;
		String workTime = hmsFormatter.print(new Period(startMillis, new DateTime()));
		String procentSecond = " - html2pdf3 - (" + procent + "%, " + workTime + "s)";
		return procentSecond;
	}
}
