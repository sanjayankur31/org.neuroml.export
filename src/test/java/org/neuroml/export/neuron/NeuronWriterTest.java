package org.neuroml.export.neuron;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.lemsml.jlems.core.logging.E;
import org.lemsml.jlems.core.logging.MinimalMessageHandler;
import org.lemsml.jlems.core.sim.LEMSException;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.io.util.FileUtil;
import org.lemsml.jlems.io.util.JUtil;
import org.neuroml.export.utils.UtilsTest;
import org.neuroml.export.exceptions.GenerationException;
import org.neuroml.export.exceptions.ModelFeatureSupportException;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.util.NeuroMLConverter;
import org.neuroml.model.util.NeuroMLException;

public class NeuronWriterTest extends TestCase {


    public void testHH() throws LEMSException, IOException, GenerationException, NeuroMLException, ModelFeatureSupportException {
        String exampleFilename = "LEMS_NML2_Ex5_DetCell.xml";
        testGetMainScript(exampleFilename);
    }

    public void testFN() throws LEMSException, IOException, GenerationException, NeuroMLException, ModelFeatureSupportException {
        String exampleFilename = "LEMS_NML2_Ex9_FN.xml";
        testGetMainScript(exampleFilename);
    }

    public void testNet2() throws LEMSException, IOException, GenerationException, NeuroMLException, ModelFeatureSupportException {
        String exampleFilename = "LEMS_NML2_Ex12_Net2.xml";
        testGetMainScript(exampleFilename);
    }

    public void testInputsEx() throws LEMSException, IOException, GenerationException, NeuroMLException, JAXBException, ModelFeatureSupportException {
        String exampleFilename = "LEMS_NML2_Ex16_Inputs.xml";
        testGetMainScript(exampleFilename);
    }

    public void testGHK() throws LEMSException, IOException, GenerationException, NeuroMLException, JAXBException, ModelFeatureSupportException {
         String exampleFilename = "LEMS_NML2_Ex18_GHK.xml";
         testGetMainScript(exampleFilename);
    }
    
    public void testGJ() throws LEMSException, IOException, GenerationException, NeuroMLException, JAXBException, ModelFeatureSupportException {
         String exampleFilename = "LEMS_NML2_Ex19a_GapJunctionInstances.xml";
         testGetMainScript(exampleFilename);
    }
    
    public void testAnalog() throws LEMSException, IOException, GenerationException, NeuroMLException, JAXBException, ModelFeatureSupportException {
         String exampleFilename = "LEMS_NML2_Ex20_AnalogSynapses.xml";
         testGetMainScript(exampleFilename);
    }

    public void testSpiketimesEx() throws LEMSException, IOException, GenerationException, NeuroMLException, JAXBException, ModelFeatureSupportException {
        String exampleFilename = "LEMS_NML2_Ex23_Spiketimes.xml";
        testGetMainScript(exampleFilename);
    }

    public void testChannel() throws LEMSException, IOException, GenerationException, NeuroMLException {

        testComponentToMod("NML2_SimpleIonChannel.nml", "NaConductance");
    }

    public void testSynapse() throws LEMSException, IOException, GenerationException, NeuroMLException {

        testComponentToMod("NML2_SynapseTypes.nml", "blockStpSynDepFac");
    }

    public void testIaFCells() throws LEMSException, IOException, GenerationException, NeuroMLException {

        testComponentToMod("NML2_AbstractCells.nml", "iafTau");
        testComponentToMod("NML2_AbstractCells.nml", "iafTauRef");
        testComponentToMod("NML2_AbstractCells.nml", "iaf");
        testComponentToMod("NML2_AbstractCells.nml", "iafRef");
    }

    public void testInputs() throws LEMSException, IOException, GenerationException, NeuroMLException {

        testComponentToMod("NML2_Inputs.nml", "pulseGen0");
        testComponentToMod("NML2_Inputs.nml", "sg0");
        testComponentToMod("NML2_Inputs.nml", "rg0");
        testComponentToMod("NML2_Inputs.nml", "vClamp0");
    }

    public void testComponentToMod(String nmlFilename, String compId) throws LEMSException, IOException, GenerationException, ModelFeatureSupportException, NeuroMLException {
        E.info("Loading: " + nmlFilename);

        String content = JUtil.getRelativeResource(this.getClass(), Utils.NEUROML_EXAMPLES_RESOURCES_DIR+"/"+nmlFilename);

        String nmlLems = NeuroMLConverter.convertNeuroML2ToLems(content);

        Lems lems = Utils.readLemsNeuroMLFile(nmlLems).getLems();
        Component comp = lems.getComponent(compId);
        E.info("Found component: " + comp);

        NeuronWriter nw = new NeuronWriter(lems);
        String modFile = nw.generateModFile(comp);

        String origName = comp.getComponentType().getName();
        String newName = "MOD_" + compId;

        modFile = modFile.replaceAll(origName, newName);
        File newMechFile = new File(UtilsTest.getTempDir(), newName + ".mod");

        FileUtil.writeStringToFile(modFile, newMechFile);
        E.info("Written to file: " + newMechFile);
    }

    public void testSBML() throws LEMSException, IOException, GenerationException, JAXBException, NeuroMLException, ModelFeatureSupportException {

        File exampleSBML = new File("src/test/resources/BIOMD0000000185_LEMS.xml");
        generateMainScript(exampleSBML);
    }

    public void generateMainScript(File localFile) throws LEMSException, IOException, GenerationException, JAXBException, NeuroMLException, ModelFeatureSupportException {

//    	Lems lems = Utils.readLemsNeuroMLFile(FileUtil.readStringFromFile(localFile)).getLems();
        Lems lems = Utils.readLemsNeuroMLFile(localFile).getLems();

        NeuronWriter nw = new NeuronWriter(lems, UtilsTest.getTempDir(), localFile.getName().replaceAll(".xml", "_nrn.py"));
        List<File> outputFiles = nw.convert();

        assertTrue(outputFiles.size() >= 2);

        UtilsTest.checkConvertedFiles(outputFiles);
    }
    
    public void testAcnet() throws LEMSException, IOException, GenerationException, JAXBException, NeuroMLException, ModelFeatureSupportException {

    	testLocalLEMS("LEMS_SomeCells.xml");
	}

    public void testLocalLEMS(String exampleFilename) throws LEMSException, IOException, GenerationException, NeuroMLException, ModelFeatureSupportException {

        MinimalMessageHandler.setVeryMinimal(true);
    	Lems lems = Utils.readLemsNeuroMLFile(new File("src/test/resources/examples/"+exampleFilename)).getLems();
        
        testLems(lems, exampleFilename);
    }

    public void testGetMainScript(String exampleFilename) throws LEMSException, IOException, GenerationException, NeuroMLException, ModelFeatureSupportException {

        MinimalMessageHandler.setVeryMinimal(true);
        Lems lems = UtilsTest.readLemsFileFromExamples(exampleFilename);
        
        testLems(lems, exampleFilename);
    }
    
    private void testLems(Lems lems, String exampleFilename) throws NeuroMLException, ModelFeatureSupportException, GenerationException, LEMSException {

        NeuronWriter nw = new NeuronWriter(lems, UtilsTest.getTempDir(), exampleFilename.replaceAll(".xml", "_nrn.py"));
        List<File> outputFiles = nw.convert();

        assertTrue(outputFiles.size() >= 2);

        UtilsTest.checkConvertedFiles(outputFiles);

    }

}
