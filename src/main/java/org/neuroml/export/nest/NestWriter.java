package org.neuroml.export.nest;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.lemsml.export.dlems.DLemsKeywords;
import org.lemsml.export.dlems.DLemsWriter;
import org.lemsml.jlems.core.logging.E;
import org.lemsml.jlems.core.logging.MinimalMessageHandler;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.io.util.FileUtil;
import org.neuroml.export.base.ANeuroMLBaseWriter;
import org.neuroml.export.exception.GenerationException;
import org.neuroml.export.exception.ModelFeatureSupportException;
import org.neuroml.export.utils.support.ModelFeature;
import org.neuroml.export.utils.support.SupportLevelInfo;
import org.neuroml.model.util.NeuroMLException;

@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class NestWriter extends ANeuroMLBaseWriter
{

	private final String runTemplateFile = "nest/run.vm";
	private final String cellTemplateFile = "nest/cell.vm";

	String comm = "#";
	String commPre = "'''";
	String commPost = "'''";

	public ArrayList<File> allGeneratedFiles = new ArrayList<File>();

	public NestWriter(Lems lems) throws ModelFeatureSupportException, LEMSException, NeuroMLException
	{
		super(lems, "NEST");
		MinimalMessageHandler.setVeryMinimal(true);
		E.setDebug(false);
		sli.checkConversionSupported(format, lems);
	}

	@Override
	protected void setSupportedFeatures()
	{
		sli.addSupportInfo(format, ModelFeature.ABSTRACT_CELL_MODEL, SupportLevelInfo.Level.LOW);
		sli.addSupportInfo(format, ModelFeature.COND_BASED_CELL_MODEL, SupportLevelInfo.Level.NONE);
		sli.addSupportInfo(format, ModelFeature.SINGLE_COMP_MODEL, SupportLevelInfo.Level.LOW);
		sli.addSupportInfo(format, ModelFeature.NETWORK_MODEL, SupportLevelInfo.Level.LOW);
		sli.addSupportInfo(format, ModelFeature.MULTI_POPULATION_MODEL, SupportLevelInfo.Level.NONE);
		sli.addSupportInfo(format, ModelFeature.NETWORK_WITH_INPUTS_MODEL, SupportLevelInfo.Level.NONE);
		sli.addSupportInfo(format, ModelFeature.NETWORK_WITH_PROJECTIONS_MODEL, SupportLevelInfo.Level.NONE);
		sli.addSupportInfo(format, ModelFeature.MULTICOMPARTMENTAL_CELL_MODEL, SupportLevelInfo.Level.NONE);
		sli.addSupportInfo(format, ModelFeature.HH_CHANNEL_MODEL, SupportLevelInfo.Level.NONE);
		sli.addSupportInfo(format, ModelFeature.KS_CHANNEL_MODEL, SupportLevelInfo.Level.NONE);
	}

	@Override
	protected void addComment(StringBuilder sb, String comment)
	{

		if(comment.indexOf("\n") < 0) sb.append(comm + comment + "\n");
		else sb.append(commPre + "\n" + comment + "\n" + commPost + "\n");
	}

	public String getMainScript() throws GenerationException, IOException
	{
		ArrayList<File> files = generateMainScriptAndCellFiles(null);
		return FileUtil.readStringFromFile(files.get(0));
	}

	public ArrayList<File> generateMainScriptAndCellFiles(File dirForFiles) throws GenerationException
	{

		StringBuilder mainRunScript = new StringBuilder();
		StringBuilder cellScript = new StringBuilder();

		addComment(mainRunScript, format + " simulator compliant export for:\n\n" + lems.textSummary(false, false));

		addComment(cellScript, format + " simulator compliant export for:\n\n" + lems.textSummary(false, false));

		Velocity.init();

		VelocityContext context = new VelocityContext();

		try
		{
			DLemsWriter somw = new DLemsWriter(lems);
			String som = somw.getMainScript();

			DLemsWriter.putIntoVelocityContext(som, context);

			Properties props = new Properties();
			props.put("resource.loader", "class");
			props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			VelocityEngine ve = new VelocityEngine();
			ve.init(props);
			Template template = ve.getTemplate(runTemplateFile);

			StringWriter sw1 = new StringWriter();

			template.merge(context, sw1);

			mainRunScript.append(sw1);

			template = ve.getTemplate(cellTemplateFile);

			StringWriter sw2 = new StringWriter();

			template.merge(context, sw2);

			cellScript.append(sw2);

			if(dirForFiles != null && dirForFiles.exists())
			{
				E.info("Writing " + format + " files to: " + dirForFiles);
				String name = (String) context.internalGet(DLemsKeywords.NAME.get());
				File mainScriptFile = new File(dirForFiles, "run_" + name + "_nest.py");
				File cellScriptFile = new File(dirForFiles, name + ".nestml");
				FileUtil.writeStringToFile(mainRunScript.toString(), mainScriptFile);
				allGeneratedFiles.add(mainScriptFile);
				FileUtil.writeStringToFile(cellScript.toString(), cellScriptFile);
				allGeneratedFiles.add(cellScriptFile);
			}
			else
			{
				E.info("Not writing " + format + " scripts to files! Problem with target dir: " + dirForFiles);
			}

		}
		catch(IOException e1)
		{
			throw new GenerationException("Problem converting LEMS to dLEMS", e1);
		}
		catch(VelocityException e)
		{
			throw new GenerationException("Problem using Velocity template", e);
		}
		catch(LEMSException e)
		{
			throw new GenerationException("Problem generating the files", e);
		}

		return allGeneratedFiles;

	}

	@Override
	public List<File> convert(Lems lems)
	{
		// TODO Auto-generated method stub
		return null;
	}

}