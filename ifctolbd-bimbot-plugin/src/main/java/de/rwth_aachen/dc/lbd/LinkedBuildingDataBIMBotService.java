package de.rwth_aachen.dc.lbd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.bimserver.bimbots.BimBotContext;
import org.bimserver.bimbots.BimBotsException;
import org.bimserver.bimbots.BimBotsInput;
import org.bimserver.bimbots.BimBotsOutput;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.SchemaName;
import org.lbd.ifc2lbd.IFCtoLBDConverter_BIM4Ren;

import com.google.common.base.Charsets;

import de.rwth_aachen.dc.lbd.bimserver.plugins.services.RWTH_BimBotAbstractService;

public class LinkedBuildingDataBIMBotService extends RWTH_BimBotAbstractService {
	{
		JenaSystem.init();
		org.apache.jena.query.ARQ.init();
	}

	@Override
	public BimBotsOutput runBimBot(BimBotsInput input, BimBotContext bimBotContext,
			PluginConfiguration pluginConfiguration) throws BimBotsException {

		IfcModelInterface model = input.getIfcModel();
		bimBotContext.updateProgress("Converting the model", 0);

		StringBuilder result_string = new StringBuilder();
		try {
			File tempFile = File.createTempFile("model-", ".ifc");
			tempFile.deleteOnExit();
			FileUtils.writeByteArrayToFile(tempFile, input.getData());

			// System.out.println("Temp ifc file:" + tempFile.getAbsolutePath());

			extractLBD(tempFile, result_string);

		} catch (IOException e) {
			e.printStackTrace();
		}

		BimBotsOutput output = new BimBotsOutput(SchemaName.UNSTRUCTURED_UTF8_TEXT_1_0,
				result_string.toString().getBytes(Charsets.UTF_8));
		output.setTitle("BimBotDemoService Results");
		output.setContentType("text/plain");

		bimBotContext.updateProgress("Done", 100);
		return output;
	}

	private static void extractLBD(File ifcFile, StringBuilder result_string) {
		IFCtoLBDConverter_BIM4Ren lbdconverter = new IFCtoLBDConverter_BIM4Ren();
		Model m = lbdconverter.convert(ifcFile.getAbsolutePath(), "https://dot.dc.rwth-aachen.de/IFCtoLBDset");

		// https://stackoverflow.com/questions/216894/get-an-outputstream-into-a-string
		OutputStream ttl_output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			public String toString() {
				return this.string.toString();
			}
		};
		m.write(System.out, "TTL");
		//m.write(ttl_output, "TTL");
		RDFDataMgr.write(ttl_output, m, RDFFormat.JSONLD_COMPACT_PRETTY);
		result_string.append(ttl_output.toString());
	}

	@Override
	public String getOutputSchema() {
		return SchemaName.UNSTRUCTURED_UTF8_TEXT_1_0.name();
	}

	public static void main(String[] args) {
		StringBuilder result_string = new StringBuilder();
		File ifcFile = new File("c:\\test\\Duplex_A_20110505.ifc");
		LinkedBuildingDataBIMBotService.extractLBD(ifcFile, result_string);
		Writer out = null;
		try {
			out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("c:\\test\\Duplex_A_20110505.jsonld"), "UTF-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			out.write(result_string.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}