package de.rwth_aachen.dc.lbd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.bimserver.bimbots.BimBotContext;
import org.bimserver.bimbots.BimBotsException;
import org.bimserver.bimbots.BimBotsInput;
import org.bimserver.bimbots.BimBotsOutput;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.SchemaName;
import org.lbd.ifc2lbd.IFCtoLBDConverter;

import com.google.common.base.Charsets;

import de.rwth_aachen.dc.lbd.bimserver.plugins.services.RWTH_BimBotAbstractService;

public class LinkedBuildingDataBIMBotService extends RWTH_BimBotAbstractService {

	@Override
	public BimBotsOutput runBimBot(BimBotsInput input, BimBotContext bimBotContext, PluginConfiguration pluginConfiguration) throws BimBotsException {
		IfcModelInterface model = input.getIfcModel();
		bimBotContext.updateProgress("Converting the model", 0);
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("Data size "+input.getData().length);
		
		try {
			File tempFile = File.createTempFile("model-", ".ifc");
			tempFile.deleteOnExit();
			FileUtils.writeByteArrayToFile(tempFile, input.getData());
			
			System.out.println("Temp ifc file:"+tempFile.getAbsolutePath());
			String outputFile = tempFile.getAbsolutePath().substring(0, tempFile.getAbsolutePath().length() - 4) + ".ttl";
			
			new IFCtoLBDConverter(tempFile.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset#", outputFile, 0, true, false,
					true, false, false, false);
			
			System.out.println("result: "+outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		BimBotsOutput output = new BimBotsOutput(SchemaName.UNSTRUCTURED_UTF8_TEXT_1_0, sb.toString().getBytes(Charsets.UTF_8));
		output.setTitle("BimBotDemoService Results");
		output.setContentType("text/plain");

		
		bimBotContext.updateProgress("Done", 100);
		return output;
	}

	@Override
	public String getOutputSchema() {
		return SchemaName.UNSTRUCTURED_UTF8_TEXT_1_0.name();
	}
	
	
	// For testing,,,
	public static void main(String[] args) 
	{
		//String ifcFileName="c:\\ifc\\231110AC-11-Smiley-West-04-07-2007.ifc";
		String ifcFileName="c:\\ifc\\Duplex_A_20110505.ifc";
		File ifcFile=new File(ifcFileName);
		try {
			byte[] fileContent = Files.readAllBytes(ifcFile.toPath());
		
     		File tempFile;
			tempFile = File.createTempFile("model-", ".ifc");
			tempFile.deleteOnExit();
			FileUtils.writeByteArrayToFile(tempFile, fileContent);
			System.out.println("Temp ifc file:"+tempFile.getAbsolutePath());
			String outputFile = tempFile.getAbsolutePath().substring(0, tempFile.getAbsolutePath().length() - 4) + ".ttl";
			
			new IFCtoLBDConverter(ifcFile.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset#", outputFile, 0, true, false,
					true, false, false, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}