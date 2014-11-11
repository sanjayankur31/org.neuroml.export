/**
 * 
 */
package org.neuroml.export.info.model;

import org.neuroml.model.GateHHRates;
import org.neuroml.model.GateHHRatesInf;
import org.neuroml.model.GateHHRatesTau;
import org.neuroml.model.GateHHTauInf;
import org.neuroml.model.GateHHUndetermined;
import org.neuroml.model.IonChannel;
import org.neuroml.model.util.NeuroMLException;

/**
 * @author borismarin
 *
 */
public class ChannelInfoExtractor {
	private InfoNode gates = new InfoNode();


	public ChannelInfoExtractor(IonChannel chan) throws NeuroMLException 
	{
		//TODO: use jlems to simulate channels and generate traces to plot
		//Sim simchan = Utils.convertNeuroMLToSim(chan);


		for (GateHHUndetermined g : chan.getGate()){

			HHRateProcessor rateinfo = new HHRateProcessor(g);	
            InfoNode gate = new InfoNode();
            
			gate.put("instances", g.getInstances());
			generateRatePlots(gate, rateinfo);

			gates.put("gate " + g.getId(), gate);
		}


		for (GateHHRates g : chan.getGateHHrates()){

			HHRateProcessor rateinfo = new HHRateProcessor(g);	
            
            InfoNode gate = new InfoNode();
            
			gate.put("instances", g.getInstances());
			generateRatePlots(gate, rateinfo);

			gates.put("gate " + g.getId(), gate);

		}

		for(GateHHRatesInf g : chan.getGateHHratesInf()){
			InfoNode gateinfo = new InfoNode();

			gateinfo.put("instances", g.getInstances());
			gates.put("gate " + g.getId(), gateinfo);
		}

		for(GateHHRatesTau g : chan.getGateHHratesTau()){
			InfoNode gate = new InfoNode();

			gate.put("instances", g.getInstances());
			gates.put("gate " + g.getId(), gate);
		}

		for(GateHHTauInf g : chan.getGateHHtauInf()){
			InfoNode gate = new InfoNode();

			HHTauInfProcessor tii = new HHTauInfProcessor(g);	
			ChannelMLHHExpression inf = tii.getSteadyStateActivation();
			ChannelMLHHExpression tau = tii.getTimeCourse();

			gate.put("steady state activation", new ExpressionNode(inf.toString(), inf.getExpression().getId(), "V", "ms-1"));
			gate.put("time constant", new ExpressionNode(tau.toString(), tau.getExpression().getId(), "V", "ms-1"));
			gate.put("steady state activation plot", PlotNodeGenerator.createPlotNode(inf.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));
			gate.put("time constant plot", PlotNodeGenerator.createPlotNode(tau.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));

			gate.put("instances", g.getInstances());
			gates.put("gate " + g.getId(), gate);
		}
	}

	private void generateRatePlots(InfoNode gate, HHRateProcessor rateinfo) {

		ChannelMLHHExpression fwd = rateinfo.getForwardRate();
		ChannelMLHHExpression rev = rateinfo.getReverseRate();

		gate.put("forward rate",  new ExpressionNode(fwd.toString(), fwd.getExpression().getId(), "V", "ms-1"));
		gate.put("reverse rate",  new ExpressionNode(rev.toString(), rev.getExpression().getId(), "V", "ms-1"));
		gate.put("forward rate plot", PlotNodeGenerator.createPlotNode(fwd.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));
		gate.put("reverse rate plot", PlotNodeGenerator.createPlotNode(rev.getExpression(), -0.08, 0.1, 0.005, "V", "ms-1"));

	}

	public InfoNode getGates() {
		return gates;
	}


}

