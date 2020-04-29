package de.rwth_aachen.dc.lbd;

import org.bimserver.bimbots.BimBotContext;
import org.bimserver.bimbots.BimBotsException;
import org.bimserver.bimbots.BimBotsInput;
import org.bimserver.bimbots.BimBotsOutput;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.SchemaName;

import com.google.common.base.Charsets;

import de.rwth_aachen.dc.lbd.bimserver.plugins.services.RWTH_BimBotAbstractService;

public class LinkedBuildingDataBIMBotService extends RWTH_BimBotAbstractService {

	@Override
	public BimBotsOutput runBimBot(BimBotsInput input, BimBotContext bimBotContext, PluginConfiguration pluginConfiguration) throws BimBotsException {
		IfcModelInterface model = input.getIfcModel();
		bimBotContext.updateProgress("Converting the model", 0);
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("Data size "+input.getData().length);

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
}